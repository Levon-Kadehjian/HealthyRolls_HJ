import { Link } from 'react-router-dom'

export default function HomePage() {
  return (
    <div className="home-page">
      <section className="hero-section">
        <h1 className="hero-title">Fresh, Healthy Rolls Delivered Fast</h1>
        <p className="hero-subtitle">
          Crafted with organic ingredients, sustainable packaging, and rich flavors.
        </p>
        <div className="hero-actions">
          <Link to="/menu" className="btn-primary">
            Browse Menu
          </Link>
          <Link to="/orders" className="btn-secondary">
            Track Orders
          </Link>
        </div>
      </section>

      <section className="features-grid">
        <div className="feature-card">
          <span className="feature-icon">🥑</span>
          <h3>Fresh Ingredients</h3>
          <p>Locally sourced produce and premium sushi-grade fish every day.</p>
        </div>
        <div className="feature-card">
          <span className="feature-icon">⚡</span>
          <h3>Fast Ordering</h3>
          <p>Seamless ordering powered by our React Router Data Architecture.</p>
        </div>
        <div className="feature-card">
          <span className="feature-icon">🌱</span>
          <h3>100% Eco-Friendly</h3>
          <p>Biodegradable packaging and sustainable culinary practices.</p>
        </div>
      </section>
    </div>
  )
}
