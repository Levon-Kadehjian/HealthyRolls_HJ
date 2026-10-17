import { useLoaderData } from 'react-router-dom'
import RestaurantCard from '../components/RestaurantCard'

export default function RestaurantsPage() {
  const restaurants = useLoaderData()

  return (
    <div className="restaurants-page">
      <header className="page-header">
        <h2>Restaurants</h2>
        <p>Pick a restaurant to browse its menu.</p>
      </header>

      {restaurants.length === 0 ? (
        <div className="orders-empty-card">
          <h3>No Restaurants Yet</h3>
          <p>No restaurants have been loaded into the system.</p>
        </div>
      ) : (
        <div className="restaurant-card-grid">
          {restaurants.map((restaurant) => (
            <RestaurantCard key={restaurant.id} restaurant={restaurant} />
          ))}
        </div>
      )}
    </div>
  )
}
