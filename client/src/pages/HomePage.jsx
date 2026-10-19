import { Link } from 'react-router-dom'
import sushiLogo from '../assets/sushi-logo.png'
import { useSession } from '../auth/useSession'
import seedData from '../api/seedData.json'
import './HomePage.css'

const RESTAURANT_INFO = {
  'cabd0420-8902-4826-a50e-0492f049622a': {
    neighborhood: 'Old Montreal',
    cuisine: 'Traditional Izakaya & Specialty Maki',
    tags: ['Salmon Avocado', 'Dragon Roll', 'Izakaya'],
  },
  '6305aadf-08f1-4f77-819e-8a54d437fd9e': {
    neighborhood: 'Plateau-Mont-Royal',
    cuisine: 'Creative Rolls, Bowls & Tartares',
    tags: ['Poke Bowls', 'California Roll', 'Edamame'],
  },
  'a931edab-ffd4-4b47-a4c1-6533b237a8cc': {
    neighborhood: 'Downtown Montreal',
    cuisine: 'Classic Nigiri, Sashimi & Tempura',
    tags: ['Nigiri Platters', 'Crispy Tempura', 'Miso'],
  },
}

const highlights = [
  {
    title: 'Fresh ingredients',
    text: 'Local produce and sushi-grade fish, prepared daily.',
    icon: <path d="M11 20A7 7 0 0 1 9.8 6.1C15.5 5 17 4.48 19 2c1 2 2 4.18 2 8 0 5.5-4.78 10-10 10ZM2 21c0-3 1.85-5.36 5.08-6" />,
  },
  {
    title: 'Montreal restaurants',
    text: 'Browse menus from healthy spots across the city.',
    icon: (
      <>
        <path d="M20 10c0 6-8 12-8 12s-8-6-8-12a8 8 0 0 1 16 0Z" />
        <circle cx="12" cy="10" r="3" />
      </>
    ),
  },
  {
    title: 'Fast delivery',
    text: 'Order in a few taps and track it to your door.',
    icon: (
      <>
        <circle cx="12" cy="12" r="10" />
        <path d="M12 6v6l4 2" />
      </>
    ),
  },
]

const STATUS_LABELS = { OPEN: 'Open', PAUSED: 'Paused', CLOSED: 'Closed' }

export default function HomePage() {
  const session = useSession()

  const previews = seedData.restaurants.slice(0, 3).map((restaurant) => {
    const info = RESTAURANT_INFO[restaurant.id] || {
      neighborhood: 'Montreal',
      cuisine: 'Japanese Cuisine',
      tags: ['Sushi', 'Maki', 'Fresh Catch'],
    }
    return {
      ...restaurant,
      neighborhood: info.neighborhood,
      cuisine: info.cuisine,
      tags: info.tags,
    }
  })

  return (
    <div className="landing">
      <section className="landing-hero">
        <img src={sushiLogo} alt="" className="landing-logo" />
        <h1 className="landing-title">HealthyRolls</h1>
        <p className="landing-subtitle">Fresh, healthy rolls from Montreal's best restaurants, delivered to you.</p>
        <div className="landing-actions">
          {session ? (
            <Link to="/restaurants" className="btn-primary">
              Browse restaurants
            </Link>
          ) : (
            <>
              <Link to="/login" className="btn-primary">
                Log in
              </Link>
              <Link to="/signup" className="btn-secondary">
                Sign up
              </Link>
            </>
          )}
        </div>
      </section>

      {/* Restaurant Previews */}
      <section className="landing-previews" aria-labelledby="previews-title">
        <div className="landing-section-header">
          <h2 id="previews-title" className="landing-section-title">Partner Kitchens</h2>
          <p className="landing-section-subtitle">Hand-picked sushi kitchens across Montreal, made to order.</p>
        </div>

        <div className="landing-preview-grid">
          {previews.map((restaurant) => (
            <article key={restaurant.id} className="landing-preview-card">
              <div className="landing-preview-banner">
                <img
                  src={restaurant.imageUrl}
                  alt={restaurant.name}
                  loading="lazy"
                />
                <div className="landing-preview-overlay">
                  <span className="landing-preview-badge-location">{restaurant.neighborhood}</span>
                  <span className="landing-preview-badge-status">
                    <span className={`landing-status-dot landing-status-dot-${restaurant.status.toLowerCase()}`} aria-hidden="true" />
                    {STATUS_LABELS[restaurant.status] ?? restaurant.status}
                  </span>
                </div>
              </div>

              <div className="landing-preview-body">
                <h3 className="landing-preview-name">{restaurant.name}</h3>
                <p className="landing-preview-cuisine">{restaurant.cuisine}</p>

                <div className="landing-preview-tags">
                  {restaurant.tags.map((tag) => (
                    <span key={tag} className="landing-preview-tag">{tag}</span>
                  ))}
                </div>

                <div className="landing-preview-action">
                  {restaurant.status === 'OPEN' ? (
                    <Link
                      to={session ? `/restaurants/${restaurant.id}` : '/login'}
                      className="landing-preview-link"
                    >
                      <span>Order</span>
                      <span aria-hidden="true">&rarr;</span>
                    </Link>
                  ) : (
                    <span className="landing-preview-link landing-preview-link-disabled" aria-disabled="true">
                      <span>{restaurant.status === 'PAUSED' ? 'Not taking orders' : 'Closed'}</span>
                    </span>
                  )}
                </div>
              </div>
            </article>
          ))}
        </div>
      </section>

      <section className="landing-highlights" aria-label="Why HealthyRolls">
        {highlights.map((highlight) => (
          <div key={highlight.title} className="landing-highlight">
            <svg
              className="landing-highlight-icon"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              strokeWidth="1.8"
              strokeLinecap="round"
              strokeLinejoin="round"
              aria-hidden="true"
            >
              {highlight.icon}
            </svg>
            <h3>{highlight.title}</h3>
            <p>{highlight.text}</p>
          </div>
        ))}
      </section>
    </div>
  )
}
