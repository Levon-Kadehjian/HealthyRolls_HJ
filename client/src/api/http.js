import { clearSession, getSession } from '../auth/session'

export async function apiRequest(path, { method = 'GET', body } = {}) {
  const session = getSession()
  const headers = { Accept: 'application/json' }
  if (body !== undefined) {
    headers['Content-Type'] = 'application/json'
  }
  if (session) {
    headers.Authorization = `Bearer ${session.accessToken}`
  }

  const response = await fetch(path, {
    method,
    headers,
    body: body === undefined ? undefined : JSON.stringify(body),
  })

  if (response.status === 401) {
    clearSession()
  }
  if (!response.ok) {
    throw response
  }
  return response.status === 204 ? null : response.json()
}
