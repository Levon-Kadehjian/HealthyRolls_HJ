import seedData from './seedData.json'
import { recordCommand } from '../console/consoleStore'

const { restaurants } = seedData
let loaded = false

function loadInitialData() {
  if (!loaded) {
    loaded = true
    const itemCount = restaurants.reduce((sum, restaurant) => sum + restaurant.menu.length, 0)
    recordCommand('LOAD_INITIAL_DATA', 'seedData.json', 'ok', `${restaurants.length} restaurants, ${itemCount} menu items`)
  }
  return restaurants
}

export async function getRestaurants() {
  return loadInitialData().map(({ id, name, status, imageUrl }) => ({ id, name, status, imageUrl }))
}

export async function getRestaurant(restaurantId) {
  const restaurant = loadInitialData().find((entry) => entry.id === restaurantId)
  return restaurant && { ...restaurant, menu: restaurant.menu.map((item) => ({ ...item })) }
}
