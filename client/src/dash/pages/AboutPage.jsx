import { Link } from 'react-router-dom'
import './AboutPage.css'

export default function AboutPage() {
  return (
    <div className="page-container about-page">
      <header className="page-header">
        <h2>About HealthyRolls</h2>
        <p>Crafting delicious sushi rolls using sustainable, eco-friendly ingredients.</p>
      </header>

      <section className="feature-card" style={{ maxWidth: '750px', margin: 0, textAlign: 'left' }}>
        <h3>Our Mission</h3>
        <p style={{ marginBottom: '16px' }}>
          HealthyRolls (HJ) was founded with one simple vision: to bring fresh, artisan-quality sushi rolls straight to your doorstep within minutes, using zero-emission delivery and 100% biodegradable packaging.
        </p>
        <p>
          Part of the Concordia SOEN-343 Software Architecture &amp; Design project.
        </p>
        <div style={{ marginTop: '24px' }}>
          <Link to="/" className="btn-primary">Back to Home</Link>
        </div>
      </section>
    </div>
  )
}
