import { apiRequest } from './http'

export function pauseRestaurant(restaurantId) {
  return apiRequest(`/api/restaurants/${encodeURIComponent(restaurantId)}/pause`, { method: 'POST' })
}

export function reopenRestaurant(restaurantId) {
  return apiRequest(`/api/restaurants/${encodeURIComponent(restaurantId)}/reopen`, { method: 'POST' })
}

export function addMenuItem(restaurantId, details) {
  return apiRequest(`/api/restaurants/${encodeURIComponent(restaurantId)}/menu`, { method: 'POST', body: details })
}

export function editMenuItem(restaurantId, itemId, details) {
  return apiRequest(`/api/restaurants/${encodeURIComponent(restaurantId)}/menu/${encodeURIComponent(itemId)}`, { method: 'PUT', body: details })
}

export function changeMenuAvailability(restaurantId, itemId, status) {
  return apiRequest(`/api/restaurants/${encodeURIComponent(restaurantId)}/menu/${encodeURIComponent(itemId)}/availability`, { method: 'PUT', body: { status } })
}

export function getMapRestaurants() {
  return apiRequest('/api/map/restaurants')
}

export async function getRestaurants() {
  return apiRequest('/api/restaurants')
}

export async function getRestaurant(restaurantId) {
  const id = encodeURIComponent(restaurantId)
  const restaurant = await apiRequest(`/api/restaurants/${id}`)
  const menu = await apiRequest(`/api/restaurants/${id}/menu`)
  return { ...restaurant, menu }
}
