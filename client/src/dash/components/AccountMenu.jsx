import { useEffect, useRef, useState } from 'react'
import { Link, useLocation, useNavigate } from 'react-router-dom'
import { LogOut, RotateCcw, Settings } from 'lucide-react'
import { useCart } from '../cart/useCart'
import { clearSession } from '../auth/session'
import { describeError, recordCommand, runCommand } from '../console/consoleStore'
import { resetSystem } from '../api/admin'

export default function AccountMenu({ session }) {
  const { clearCart } = useCart()
  const navigate = useNavigate()
  const location = useLocation()
  const [menuOpen, setMenuOpen] = useState(false)
  const menuRef = useRef(null)
  const [resetting, setResetting] = useState(false)
  const [resetError, setResetError] = useState('')

  async function handleReset() {
    if (
      !window.confirm(
        'Reset the system? This removes current orders and restores the seeded accounts, restaurants, menus and couriers.',
      )
    ) {
      return
    }

    setResetting(true)
    setResetError('')

    try {
      await runCommand(
        'RESET_SYSTEM',
        '',
        resetSystem,
        () => 'Initial system state restored.',
      )
    } catch (error) {
      setResetError(await describeError(error))
      setResetting(false)
      return
    }

    clearCart()
    clearSession()
    window.location.assign('/login')
  }

  useEffect(() => {
    setMenuOpen(false)
  }, [location.pathname])

  useEffect(() => {
    if (!menuOpen) {
      return undefined
    }
    const handleClick = (event) => {
      if (menuRef.current && !menuRef.current.contains(event.target)) {
        setMenuOpen(false)
      }
    }
    const handleKey = (event) => {
      if (event.key === 'Escape') {
        setMenuOpen(false)
      }
    }
    document.addEventListener('mousedown', handleClick)
    document.addEventListener('keydown', handleKey)
    return () => {
      document.removeEventListener('mousedown', handleClick)
      document.removeEventListener('keydown', handleKey)
    }
  }, [menuOpen])

  const logout = () => {
    setMenuOpen(false)
    clearSession()
    recordCommand('LOGOUT', '', 'ok', 'session cleared')
    navigate('/')
  }

  if (!session) {
    return null
  }

  const isStaff = session.role === 'RESTAURANT_STAFF'
  const isCourier = session.role === 'COURIER'
  const isCustomer = session.role === 'CUSTOMER'
  const isOperator = session.role === 'OPERATOR'

  return (
    <div className="nav-account" ref={menuRef}>
      {isStaff && <span className="nav-role-tag">Staff</span>}
      {isCourier && <span className="nav-role-tag">Courier</span>}
      {isCustomer && <span className="nav-role-tag">Customer</span>}
      {isOperator && <span className="nav-role-tag">Operator</span>}
      <button
        type="button"
        className="nav-avatar"
        aria-label="Account menu"
        aria-haspopup="menu"
        aria-expanded={menuOpen}
        onClick={() => setMenuOpen((open) => !open)}
      >
        {(session.email?.[0] ?? '?').toUpperCase()}
      </button>
      {menuOpen && (
        <div className="nav-menu" role="menu">
          {session.email && (
            <div className="nav-menu-header">
              <span className="nav-menu-label">Signed in as</span>
              <span className="nav-menu-email">{session.email}</span>
            </div>
          )}
          <Link to="/account" className="nav-menu-item" role="menuitem">
            <Settings className="nav-menu-icon" strokeWidth={1.9} aria-hidden="true" />
            Account Settings
          </Link>
          {session.role === 'OPERATOR' && (
            <button
              type="button"
              className="nav-menu-item"
              role="menuitem"
              onClick={handleReset}
              disabled={resetting}
            >
              <RotateCcw className="nav-menu-icon" strokeWidth={1.9} aria-hidden="true" />
              {resetting ? 'Resetting…' : 'Reset system'}
            </button>
          )}
          {resetError && (
            <p className="nav-menu-error" role="alert">{resetError}</p>
          )}
          <div className="nav-menu-divider" role="separator" />
          <button type="button" className="nav-menu-item nav-menu-item-danger" role="menuitem" onClick={logout}>
            <LogOut className="nav-menu-icon" strokeWidth={1.9} aria-hidden="true" />
            Log out
          </button>
        </div>
      )}
    </div>
  )
}
