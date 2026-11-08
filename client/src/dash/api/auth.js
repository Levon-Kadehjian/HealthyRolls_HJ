import { saveSession, updateSession } from '../auth/session'
import { recordCommand } from '../console/consoleStore'

const ROLE_LABELS = {
  CUSTOMER: 'customer',
  RESTAURANT_STAFF: 'restaurant staff',
  COURIER: 'courier',
  OPERATOR: 'operator',
}

async function fetchProfile(accessToken) {
  try {
    const response = await fetch('/api/users/me', {
      headers: { Authorization: `Bearer ${accessToken}` },
    })

    if (!response.ok) {
      const errJson = await response.json().catch(() => ({}))
      const msg = errJson.detail || errJson.message || 'Could not load account information.'
      recordCommand('LOAD_PROFILE', '', 'error', msg)
      return null
    }

    return await response.json()
  } catch {
    const msg = 'Unable to load account information. Please try again.'
    recordCommand('LOAD_PROFILE', '', 'error', msg)
    return null
  }
}

export async function ensureSessionLocation(session) {
  if (!session?.accessToken || session.latitude != null) return

  const profile = await fetchProfile(session.accessToken)

  if (profile?.latitude != null) {
    updateSession({
      latitude: profile.latitude,
      longitude: profile.longitude,
    })
  }
}

export async function loginUser({ email, password, role = 'CUSTOMER', name }) {
  let authData = null
  let profile = null

  try {
    const response = await fetch('/api/users/login', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ email, hashedPassword: password }),
    })

    if (response.ok) {
      authData = await response.json()
      if (authData.role !== role) {
        const msg = `This is not a ${ROLE_LABELS[role] ?? role} account. Choose the role that matches your account.`
        recordCommand('LOGIN', `email=${email} role=${role}`, 'error', msg)
        return { ok: false, error: msg }
      }
      profile = await fetchProfile(authData.accessToken)
    } else if (response.status === 401 || response.status === 400) {
      const errJson = await response.json().catch(() => ({}))
      const msg = errJson.detail || errJson.message || 'Invalid email or password.'
      recordCommand('LOGIN', `email=${email}`, 'error', msg)
      return { ok: false, error: msg }
    }  else {
      const errJson = await response.json().catch(() => ({}))
      const msg = errJson.detail || errJson.message || 'Login failed. Please try again.'
      recordCommand('LOGIN', `email=${email}`, 'error', msg)
      return { ok: false, error: msg }
    }
  } catch {
    const msg = 'Unable to connect to the server. Please try again.'
    recordCommand('LOGIN', `email=${email}`, 'error', msg)
    return { ok: false, error: msg }
  }

  const effectiveRole = authData.role
  const resolvedName = profile?.name || name
  const restaurantId = profile?.restaurantId?.value || profile?.restaurantId || null
  const session = {
    accessToken: authData?.accessToken,
    expiresAt: authData?.expiresAt,
    userId: authData?.userId?.value ?? authData?.userId,
    role: effectiveRole,
    email,
    ...(resolvedName && { name: resolvedName }),
    ...(restaurantId && { restaurantId }),
    ...(profile?.latitude != null && { latitude: profile.latitude, longitude: profile.longitude }),
  }

  saveSession(session)
  recordCommand(
    'LOGIN',
    `role=${effectiveRole} email=${email}`,
    'ok',
    `Authenticated as ${effectiveRole}`,
  )

  return { ok: true, session }
}

export async function registerUser({ name, email, password,latitude, longitude, }) {
  try {
    const response = await fetch('/api/users/register', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        name,
        email,
        hashedPassword: password,
        latitude,
        longitude,
      }),
    })

    if (response.ok) {
      recordCommand('REGISTER', `role=CUSTOMER email=${email}`, 'ok', `Created user ${name}`)
      return loginUser({ email, password, name })
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
    const msg = 'Unable to connect to the server. Please try again.'
    recordCommand('REGISTER', `email=${email}`, 'error', msg)
    return { ok: false, error: msg }
  }

  return loginUser({ email, password, name })
}
