import { Link } from 'react-router-dom'
import sushiLogo from '../assets/sushi-logo.png'
import './RestaurantCard.css'

const STATUS_LABELS = {
  PAUSED: 'Paused',
  CLOSED: 'Closed',
}

export default function RestaurantCard({ restaurant }) {
  const statusLabel = STATUS_LABELS[restaurant.status]

  return (
    <Link
      to={`/restaurants/${restaurant.id}`}
      className={`restaurant-card ${statusLabel ? 'restaurant-card-unavailable' : ''}`}
    >
      <div className={`restaurant-card-image ${restaurant.imageUrl ? '' : 'restaurant-card-image-fallback'}`}>
        <img
          src={restaurant.imageUrl || sushiLogo}
          alt={restaurant.imageUrl ? restaurant.name : ''}
          loading="lazy"
        />
        {statusLabel && <span className="restaurant-card-badge">{statusLabel}</span>}
      </div>
      <h3 className="restaurant-card-name">{restaurant.name}</h3>
    </Link>
  )
}
