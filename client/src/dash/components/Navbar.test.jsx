import { act, render, screen, waitFor, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter, useLocation } from 'react-router-dom'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { resetSystem } from '../api/admin'
import { getSession, saveSession } from '../auth/session'
import { CartContext } from '../cart/useCart'
import { clearConsole, getEntries } from '../console/consoleStore'
import Navbar from './Navbar'

// Mock the backend boundary; keep the real session, router and console behaviour.
vi.mock('../api/admin', () => ({ resetSystem: vi.fn() }))

function CurrentRoute() {
  const { pathname } = useLocation()
  return <output aria-label="Current route">{pathname}</output>
}

describe('Navbar characterisation', () => {
  let clearCart
  let confirmReset
  let redirect

  beforeEach(() => {
    localStorage.clear()
    clearConsole()
    resetSystem.mockReset()
    clearCart = vi.fn()
    confirmReset = vi.fn().mockReturnValue(true)
    vi.stubGlobal('confirm', confirmReset)
    redirect = vi.spyOn(window.location, 'assign').mockImplementation(() => {})
  })

  function renderNavbar(role = 'CUSTOMER') {
    if (role) {
      saveSession({
        userId: 'test-user',
        accessToken: 'test-token',
        email: 'liam@example.com',
        role,
        ...(role === 'RESTAURANT_STAFF' && { restaurantId: 'restaurant-1' }),
      })
    }

    render(
      <MemoryRouter initialEntries={['/orders']}>
        <CartContext.Provider value={{ itemCount: 2, clearCart }}>
          <Navbar />
          <CurrentRoute />
        </CartContext.Provider>
      </MemoryRouter>,
    )
    return userEvent.setup()
  }

  async function openAccountMenu(user) {
    await user.click(screen.getByRole('button', { name: 'Account menu' }))
  }

  it('hides account controls when no user is signed in', () => {
    renderNavbar(null)

    expect(screen.queryByRole('button', { name: 'Account menu' })).not.toBeInTheDocument()
    expect(screen.queryByRole('link', { name: 'Orders' })).not.toBeInTheDocument()
    expect(screen.queryByRole('link', { name: /Cart,/ })).not.toBeInTheDocument()
    expect(screen.queryByRole('link', { name: 'Console' })).not.toBeInTheDocument()
  })

  it.each([
    ['CUSTOMER', 'Customer', false],
    ['RESTAURANT_STAFF', 'Staff', false],
    ['COURIER', 'Courier', false],
    ['OPERATOR', 'Operator', true],
  ])('shows the %s account with the correct reset permission', async (role, label, canReset) => {
    const user = renderNavbar(role)

    expect(screen.getByText(label)).toBeVisible()
    if (role === 'OPERATOR') {
      expect(screen.getByRole('link', { name: 'Console' })).toHaveAttribute('href', '/console')
    } else {
      expect(screen.queryByRole('link', { name: 'Console' })).not.toBeInTheDocument()
    }
    await openAccountMenu(user)

    const menu = within(screen.getByRole('menu'))
    expect(menu.getByText('liam@example.com')).toBeVisible()
    expect(menu.getByRole('menuitem', { name: 'Account Settings' })).toHaveAttribute('href', '/account')
    expect(menu.getByRole('menuitem', { name: 'Log out' })).toBeVisible()
    expect(menu.queryByRole('menuitem', { name: 'Reset system' }) !== null).toBe(canReset)
  })

  it('keeps the customer orders and cart links alongside the account controls', () => {
    renderNavbar()

    expect(screen.getByRole('link', { name: 'Orders' })).toHaveAttribute('href', '/orders')
    expect(screen.getByRole('link', { name: 'Cart, 2 items' })).toHaveAttribute('href', '/cart')
  })

  it('toggles the account menu and keeps it open when clicking inside', async () => {
    const user = renderNavbar()
    const button = screen.getByRole('button', { name: 'Account menu' })

    expect(button).toHaveTextContent('L')
    expect(button).toHaveAttribute('aria-expanded', 'false')
    expect(screen.queryByRole('menu')).not.toBeInTheDocument()

    await user.click(button)
    expect(button).toHaveAttribute('aria-expanded', 'true')
    await user.click(screen.getByText('Signed in as'))
    expect(screen.getByRole('menu')).toBeVisible()

    await user.click(button)
    expect(button).toHaveAttribute('aria-expanded', 'false')
    expect(screen.queryByRole('menu')).not.toBeInTheDocument()
  })

  it('closes the account menu when Escape is pressed', async () => {
    const user = renderNavbar()
    await openAccountMenu(user)

    await user.keyboard('{Escape}')

    expect(screen.queryByRole('menu')).not.toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Account menu' })).toHaveAttribute('aria-expanded', 'false')
  })

  it('closes the account menu when clicking outside it', async () => {
    const user = renderNavbar()
    await openAccountMenu(user)

    await user.click(screen.getByLabelText('Current route'))

    expect(screen.queryByRole('menu')).not.toBeInTheDocument()
  })

  it('closes the menu after navigating to account settings', async () => {
    const user = renderNavbar()
    await openAccountMenu(user)

    await user.click(screen.getByRole('menuitem', { name: 'Account Settings' }))

    expect(screen.getByLabelText('Current route')).toHaveTextContent('/account')
    expect(screen.queryByRole('menu')).not.toBeInTheDocument()
  })

  it('logs out, clears the session, records the command and navigates home', async () => {
    const user = renderNavbar()
    await openAccountMenu(user)

    await user.click(screen.getByRole('menuitem', { name: 'Log out' }))

    expect(getSession()).toBeNull()
    expect(screen.getByLabelText('Current route').textContent).toBe('/')
    expect(screen.queryByRole('button', { name: 'Account menu' })).not.toBeInTheDocument()
    expect(getEntries()).toEqual([
      expect.objectContaining({ command: 'LOGOUT', status: 'ok', result: 'session cleared' }),
    ])
    expect(clearCart).not.toHaveBeenCalled()
    expect(redirect).not.toHaveBeenCalled()
  })

  it('leaves the session and cart intact when reset confirmation is cancelled', async () => {
    confirmReset.mockReturnValue(false)
    const user = renderNavbar('OPERATOR')
    await openAccountMenu(user)

    await user.click(screen.getByRole('menuitem', { name: 'Reset system' }))

    expect(confirmReset).toHaveBeenCalledOnce()
    expect(resetSystem).not.toHaveBeenCalled()
    expect(getSession()?.role).toBe('OPERATOR')
    expect(clearCart).not.toHaveBeenCalled()
    expect(redirect).not.toHaveBeenCalled()
    expect(getEntries()).toEqual([])
    expect(screen.getByRole('menuitem', { name: 'Reset system' })).toBeEnabled()
  })

  it('disables reset while pending, then clears local state and redirects after success', async () => {
    let finishReset
    resetSystem.mockReturnValue(new Promise((resolve) => { finishReset = resolve }))
    const user = renderNavbar('OPERATOR')
    await openAccountMenu(user)

    await user.click(screen.getByRole('menuitem', { name: 'Reset system' }))

    expect(confirmReset).toHaveBeenCalledWith(expect.stringContaining('Reset the system?'))
    expect(screen.getByRole('menuitem', { name: 'Resetting…' })).toBeDisabled()
    expect(getEntries()).toEqual([
      expect.objectContaining({ command: 'RESET_SYSTEM', status: 'pending' }),
    ])
    expect(getSession()?.role).toBe('OPERATOR')
    expect(clearCart).not.toHaveBeenCalled()
    expect(redirect).not.toHaveBeenCalled()
    await user.click(screen.getByRole('menuitem', { name: 'Resetting…' }))
    expect(resetSystem).toHaveBeenCalledOnce()

    await act(async () => { finishReset() })

    expect(clearCart).toHaveBeenCalledOnce()
    expect(getSession()).toBeNull()
    expect(redirect).toHaveBeenCalledWith('/login')
    expect(getEntries()).toEqual([
      expect.objectContaining({
        command: 'RESET_SYSTEM', status: 'ok', result: 'Initial system state restored.',
      }),
    ])
  })

  it('displays and logs a failed reset without clearing the session or cart', async () => {
    resetSystem.mockRejectedValue(new Response(
      JSON.stringify({ detail: 'Reset is temporarily unavailable.' }),
      { status: 503, headers: { 'Content-Type': 'application/json' } },
    ))
    const user = renderNavbar('OPERATOR')
    await openAccountMenu(user)

    await user.click(screen.getByRole('menuitem', { name: 'Reset system' }))

    expect(await screen.findByRole('alert')).toHaveTextContent('503 Reset is temporarily unavailable.')
    expect(screen.getByRole('menuitem', { name: 'Reset system' })).toBeEnabled()
    expect(getSession()?.role).toBe('OPERATOR')
    expect(clearCart).not.toHaveBeenCalled()
    expect(redirect).not.toHaveBeenCalled()
    expect(getEntries()).toEqual([
      expect.objectContaining({
        command: 'RESET_SYSTEM', status: 'error', result: '503 Reset is temporarily unavailable.',
      }),
    ])
  })

  it('clears the previous reset error when the operator retries', async () => {
    resetSystem.mockRejectedValueOnce(new Error('Network unavailable.'))
    const user = renderNavbar('OPERATOR')
    await openAccountMenu(user)
    await user.click(screen.getByRole('menuitem', { name: 'Reset system' }))
    expect(await screen.findByRole('alert')).toHaveTextContent('Network unavailable.')

    let finishRetry
    resetSystem.mockReturnValueOnce(new Promise((resolve) => { finishRetry = resolve }))
    await user.click(screen.getByRole('menuitem', { name: 'Reset system' }))

    expect(screen.queryByRole('alert')).not.toBeInTheDocument()
    expect(screen.getByRole('menuitem', { name: 'Resetting…' })).toBeDisabled()
    await act(async () => { finishRetry() })

    await waitFor(() => expect(redirect).toHaveBeenCalledWith('/login'))
    expect(screen.queryByRole('alert')).not.toBeInTheDocument()
    expect(resetSystem).toHaveBeenCalledTimes(2)
    expect(getEntries().map(({ status }) => status)).toEqual(['error', 'ok'])
  })
})
