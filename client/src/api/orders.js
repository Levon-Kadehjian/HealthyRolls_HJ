import { apiRequest } from './http'

export function listOrders() {
  return apiRequest('/api/orders')
}

export function applyOrderAction(orderId, action, reason = null) {
  return apiRequest(`/api/orders/${orderId}/actions`, {
    method: 'POST',
    body: { action, reason },
  })
}
