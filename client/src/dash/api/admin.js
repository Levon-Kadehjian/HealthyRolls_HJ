import { apiRequest } from './http'

export function resetSystem() {
  return apiRequest('/api/admin/reset', { method: 'POST' })
}