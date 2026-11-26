import { apiRequest } from './http'

export function listNotifications() {
  return apiRequest('/api/notifications')
}
