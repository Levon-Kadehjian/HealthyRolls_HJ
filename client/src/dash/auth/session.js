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

export function saveSession(data) {
  try {
    const existing = getSession() || {}
    const isSameUser = existing.userId && data.userId && existing.userId === data.userId
    const nextSession = isSameUser ? { ...existing, ...data } : data
    localStorage.setItem(STORAGE_KEY, JSON.stringify(nextSession))
  } catch {
    return
  }
  notify()
}

export function updateSession(partial) {
  try {
    const existing = getSession()
    if (!existing) return
    const updated = { ...existing, ...partial }
    localStorage.setItem(STORAGE_KEY, JSON.stringify(updated))
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
