import { apiRequest } from './http'

export async function getRestaurants() {
  return apiRequest('/api/restaurants')
}

export async function getRestaurant(restaurantId) {
  const id = encodeURIComponent(restaurantId)
  const restaurant = await apiRequest(`/api/restaurants/${id}`)
  const menu = await apiRequest(`/api/restaurants/${id}/menu`)
  return { ...restaurant, menu }
}
