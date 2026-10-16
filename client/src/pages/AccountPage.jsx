import { Link } from 'react-router-dom'

export default function AccountPage() {
  return (
    <div className="page-container">
      <header className="page-header">
        <h2>My Account</h2>
        <p>Manage your profile, delivery addresses, and payment methods.</p>
      </header>

      <div className="feature-card" style={{ maxWidth: '500px', margin: '0 auto', textAlign: 'left' }}>
        <h3>Profile Overview</h3>
        <p style={{ marginTop: '8px' }}>👤 <strong>Member Status:</strong> Gold Roll Member</p>
        <p style={{ marginTop: '8px' }}>📍 <strong>Default Address:</strong> Concordia Campus, Montreal</p>
        <div style={{ marginTop: '24px', display: 'flex', gap: '10px' }}>
          <Link to="/orders" className="btn-primary">View Past Orders</Link>
          <Link to="/" className="btn-secondary">Back to Home</Link>
        </div>
      </div>
    </div>
  )
}
