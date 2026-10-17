import { Link } from 'react-router-dom'
import sushiLogo from '../assets/sushi-logo.png'
import { useSession } from '../auth/useSession'
import './HomePage.css'

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

export default function HomePage() {
  const session = useSession()

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
            <Link to="/login" className="btn-primary">
              Log in
            </Link>
          )}
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
