import { Link } from 'react-router-dom'
import { useSession } from '../auth/useSession'

export default function AccountPage() {
  const session = useSession()
  const isStaff = session?.role === 'RESTAURANT_STAFF'

  return (
    <div className="page-container">
      <header className="page-header">
        <h2>My Account</h2>
        <p>Manage your profile, delivery addresses, and preferences.</p>
      </header>

      <div className="feature-card" style={{ maxWidth: '500px', margin: '0 auto', textAlign: 'left' }}>
        <h3>Profile Overview</h3>
        {session?.email && (
          <p style={{ marginTop: '8px' }}>
            ✉️ <strong>Email:</strong> {session.email}
          </p>
        )}
        <p style={{ marginTop: '8px' }}>
          🏷️ <strong>Account Role:</strong>{' '}
          <span style={{ fontWeight: 600, color: 'var(--accent)' }}>
            {isStaff ? 'Restaurant Staff' : 'Customer'}
          </span>
        </p>
        <p style={{ marginTop: '8px' }}>👤 <strong>Member Status:</strong> Gold Roll Member</p>
        <p style={{ marginTop: '8px' }}>📍 <strong>Default Address:</strong> Concordia Campus, Montreal</p>
        <div style={{ marginTop: '24px', display: 'flex', flexWrap: 'wrap', gap: '10px' }}>
          {isStaff ? (
            <Link to="/dashboard/kitchen" className="btn-primary">
              Kitchen Orders
            </Link>
          ) : (
            <Link to="/orders" className="btn-primary">
              View Past Orders
            </Link>
          )}
          <Link to="/" className="btn-secondary">
            Back to Home
          </Link>
        </div>
      </div>
    </div>
  )
}
