import { apiRequest } from './http'

export function addMenuItem(restaurantId, details) {
  return apiRequest(`/api/restaurants/${encodeURIComponent(restaurantId)}/menu`, { method: 'POST', body: details })
}

export function editMenuItem(restaurantId, itemId, details) {
  return apiRequest(`/api/restaurants/${encodeURIComponent(restaurantId)}/menu/${encodeURIComponent(itemId)}`, { method: 'PUT', body: details })
}

export function changeMenuAvailability(restaurantId, itemId, status) {
  return apiRequest(`/api/restaurants/${encodeURIComponent(restaurantId)}/menu/${encodeURIComponent(itemId)}/availability`, { method: 'PUT', body: { status } })
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
