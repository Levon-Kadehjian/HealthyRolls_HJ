import { apiRequest } from './http'

export function listNotifications() {
  return apiRequest('/api/notifications')
}

export function markNotificationsRead(notificationIds) {
  return apiRequest('/api/notifications/read', { method: 'POST', body: { notificationIds } })
}

export function markNotificationRead(notificationId) {
  return apiRequest(`/api/notifications/${notificationId}/read`, { method: 'POST' })
}
