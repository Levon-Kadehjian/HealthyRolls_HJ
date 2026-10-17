import { saveSession } from '../auth/session'
import { recordCommand } from '../console/consoleStore'

export async function loginUser({ email, password, role = 'CUSTOMER' }) {
  let authData = null
  let backendFailed = false

  try {
    const response = await fetch('/api/users/login', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ email, hashedPassword: password }),
    })

    if (response.ok) {
      authData = await response.json()
    } else if (response.status === 401 || response.status === 400) {
      const errJson = await response.json().catch(() => ({}))
      const msg = errJson.detail || errJson.message || 'Invalid email or password.'
      recordCommand('LOGIN', `email=${email}`, 'error', msg)
      return { ok: false, error: msg }
    } else {
      backendFailed = true
    }
  } catch {
    backendFailed = true
  }

  const effectiveRole = authData?.role || role || 'CUSTOMER'
  const session = {
    accessToken: authData?.accessToken || `mock-token-${Date.now()}`,
    expiresAt: authData?.expiresAt || new Date(Date.now() + 24 * 60 * 60 * 1000).toISOString(),
    userId: authData?.userId?.value || authData?.userId || `user-${Date.now()}`,
    role: effectiveRole,
    email,
  }

  saveSession(session)
  recordCommand(
    'LOGIN',
    `role=${effectiveRole} email=${email}${backendFailed ? ' (offline-fallback)' : ''}`,
    'ok',
    `Authenticated as ${effectiveRole}`,
  )

  return { ok: true, session }
}

export async function registerUser({ name, email, password, role = 'CUSTOMER' }) {
  try {
    const response = await fetch('/api/users/register', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ name, email, hashedPassword: password, role }),
    })

    if (response.ok) {
      recordCommand('REGISTER', `role=${role} email=${email}`, 'ok', `Created user ${name}`)
      return loginUser({ email, password, role })
    }

    if (response.status === 409) {
      const errJson = await response.json().catch(() => ({}))
      const msg = errJson.detail || errJson.message || 'An account with this email already exists.'
      recordCommand('REGISTER', `email=${email}`, 'error', msg)
      return { ok: false, error: msg }
    }

    if (!response.ok) {
      const errJson = await response.json().catch(() => ({}))
      const msg = errJson.detail || errJson.message || 'Registration failed.'
      recordCommand('REGISTER', `email=${email}`, 'error', msg)
      return { ok: false, error: msg }
    }
  } catch {
    // Backend offline: log registration and create local session
    recordCommand('REGISTER', `role=${role} email=${email} (offline)`, 'ok', `Created local session for ${name}`)
  }

  return loginUser({ email, password, role })
}
