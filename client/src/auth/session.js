const STORAGE_KEY = 'healthyrolls.session'

const listeners = new Set()

function notify() {
  listeners.forEach((listener) => listener())
}

export function subscribeSession(listener) {
  listeners.add(listener)
  window.addEventListener('storage', listener)
  return () => {
    listeners.delete(listener)
    window.removeEventListener('storage', listener)
  }
}

export function getSessionSnapshot() {
  try {
    return localStorage.getItem(STORAGE_KEY)
  } catch {
    return null
  }
}

export function getSession() {
  try {
    const session = JSON.parse(localStorage.getItem(STORAGE_KEY))
    if (!session?.accessToken) {
      return null
    }
    if (session.expiresAt && new Date(session.expiresAt) <= new Date()) {
      clearSession()
      return null
    }
    return session
  } catch {
    return null
  }
}

export function saveSession({ accessToken, expiresAt, userId, role }) {
  try {
    localStorage.setItem(STORAGE_KEY, JSON.stringify({ accessToken, expiresAt, userId, role }))
  } catch {
    return
  }
  notify()
}

export function clearSession() {
  try {
    localStorage.removeItem(STORAGE_KEY)
  } catch {
    return
  }
  notify()
}
