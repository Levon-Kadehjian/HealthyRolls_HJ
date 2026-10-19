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

export function placeOrder(restaurantId, lines) {
  return apiRequest('/api/orders', {
    method: 'POST',
    body: {
      restaurantId,
      lines: lines.map(({ item, quantity }) => ({ menuItemId: item.itemId, quantity })),
    },
  })
}