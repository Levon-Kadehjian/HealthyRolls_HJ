import { apiRequest } from './http'

export function getMyCourier() {
  return apiRequest('/api/couriers/me')
}

export function goOnDuty() {
  return apiRequest('/api/couriers/me/on-duty', { method: 'POST' })
}

export function goOffDuty() {
  return apiRequest('/api/couriers/me/off-duty', { method: 'POST' })
}

export function getPendingOffer() {
  return apiRequest('/api/couriers/me/offer')
}

export function acceptOffer(orderId) {
  return apiRequest(`/api/couriers/me/orders/${orderId}/accept`, { method: 'POST' })
}

export function declineOffer(orderId) {
  return apiRequest(`/api/couriers/me/orders/${orderId}/decline`, { method: 'POST' })
}

export function confirmPickup(orderId) {
  return apiRequest(`/api/couriers/me/orders/${orderId}/pickup`, { method: 'POST' })
}

export function confirmDelivery(orderId) {
  return apiRequest(`/api/couriers/me/orders/${orderId}/deliver`, { method: 'POST' })
}