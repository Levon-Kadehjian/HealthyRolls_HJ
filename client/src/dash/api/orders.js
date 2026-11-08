import { apiRequest } from './http'

export function listOrders() {
  return apiRequest('/api/orders')
}

export function listOrderEvents() {
  return apiRequest('/api/events')
}

export function getBillingHistory() {
  return apiRequest('/api/billing/history')
}

export function refundOrder(orderId) {
  return apiRequest(`/api/billing/orders/${orderId}/refund`, {
    method: 'POST',
  })
}

export function applyOrderAction(orderId, action, reason = null) {
  return apiRequest(`/api/orders/${orderId}/actions`, {
    method: 'POST',
    body: { action, reason },
  })
}

export function placeOrder(restaurantId, lines) {
  return apiRequest('/api/orders', {
    method: 'POST',
    body: {
      restaurantId,
      lines: lines.map(({ item, quantity }) => ({ menuItemId: item.itemId, quantity })),
    },
  })
}
