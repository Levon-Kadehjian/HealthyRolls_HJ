import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { Clock, Leaf, MapPin } from 'lucide-react'
import sushiLogo from '../assets/sushi-logo.png'
import { useSession } from '../auth/useSession'
import { getRestaurants } from '../api/restaurants'
import { runCommand } from '../console/consoleStore'
import './HomePage.css'

const highlights = [
  {
    title: 'Fresh ingredients',
    text: 'Local produce and sushi-grade fish, prepared daily.',
    Icon: Leaf,
  },
  {
    title: 'Montreal restaurants',
    text: 'Browse menus from healthy spots across the city.',
    Icon: MapPin,
  },
  {
    title: 'Fast delivery',
    text: 'Order in a few taps and track it to your door.',
    Icon: Clock,
  },
]

const STATUS_LABELS = { OPEN: 'Open', PAUSED: 'Paused', CLOSED: 'Closed' }

export default function HomePage() {
  const session = useSession()
  const isOperator = session?.role === 'OPERATOR'

  const [previews, setPreviews] = useState([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const canBrowse = session && session.role !== 'COURIER'

  useEffect(() => {
    let active = true
    setPreviews([])
    setError('')
    setLoading(false)
    if (!canBrowse) return

    setLoading(true)
    runCommand(
      'LIST_RESTAURANTS',
      'Homepage previews',
      getRestaurants,
      (restaurants) => `${restaurants.length} restaurants loaded`,
    )
      .then((restaurants) => {
        if (active) setPreviews(restaurants.slice(0, 3))
      })
      .catch(() => {
        if (active) setError('Unable to load restaurants. Please try again later.')
      })
      .finally(() => {
        if (active) setLoading(false)
      })

    return () => { active = false }
  }, [canBrowse, session])

  return (
    <div className="landing">
      <section className="landing-hero">
        <img src={sushiLogo} alt="" className="landing-logo" />
        <h1 className="landing-title">HealthyRolls</h1>
        <p className="landing-subtitle">Fresh, healthy rolls from Montreal's best restaurants, delivered to you.</p>
        <div className="landing-actions">
          {session ? (
            <>
              <Link to="/restaurants" className="btn-primary">
                Browse restaurants
              </Link>
              {canBrowse && (
                <Link to="/map" className="btn-secondary">
                  View map
                </Link>
              )}
            </>
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

      <section className="landing-previews" aria-labelledby="previews-title">
        <div className="landing-section-header">
          <h2 id="previews-title" className="landing-section-title">Partner Kitchens</h2>
          <p className="landing-section-subtitle">Hand-picked sushi kitchens across Montreal, made to order.</p>
        </div>

        {!session && <p>Log in to browse restaurants and their menus.</p>}
        {session && !canBrowse && <p>Restaurant browsing is not available for courier accounts.</p>}
        {canBrowse && loading && <p>Loading restaurants…</p>}
        {canBrowse && error && <p role="alert">{error}</p>}
        {canBrowse && !loading && !error && previews.length === 0 && <p>No restaurants have been loaded yet.</p>}

        {canBrowse && previews.length > 0 && (
        <div className="landing-preview-grid">
          {previews.map((restaurant) => (
            <article key={restaurant.id} className="landing-preview-card">
              <div className="landing-preview-banner">
                <img
                  src={sushiLogo}
                  alt=""
                  loading="lazy"
                />
                <div className="landing-preview-overlay">
                  <span className="landing-preview-badge-status">
                    <span className={`landing-status-dot landing-status-dot-${restaurant.status.toLowerCase()}`} aria-hidden="true" />
                    {STATUS_LABELS[restaurant.status] ?? restaurant.status}
                  </span>
                </div>
              </div>

              <div className="landing-preview-body">
                <h3 className="landing-preview-name">{restaurant.name}</h3>
                <div className="landing-preview-action">
                  {isOperator || restaurant.status === 'OPEN' ? (
                    <Link
                      to={session ? `/restaurants/${restaurant.id}` : '/login'}
                      className="landing-preview-link"
                    >
                      <span>{isOperator ? 'Manage' : 'Order'}</span>
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
        )}
      </section>

      <section className="landing-highlights" aria-label="Why HealthyRolls">
        {highlights.map((highlight) => (
          <div key={highlight.title} className="landing-highlight">
            <highlight.Icon className="landing-highlight-icon" strokeWidth={1.8} aria-hidden="true" />
            <h3>{highlight.title}</h3>
            <p>{highlight.text}</p>
          </div>
        ))}
      </section>
    </div>
  )
}
