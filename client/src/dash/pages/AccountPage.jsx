import { Link } from 'react-router-dom'
import { useSession } from '../auth/useSession'
import './AccountPage.css'

const ROLE_LABELS = {
  CUSTOMER: 'Customer',
  RESTAURANT_STAFF: 'Restaurant Staff',
  COURIER: 'Courier',
  OPERATOR: 'Operator',
}

export default function AccountPage() {
  const session = useSession()
  const isCustomer = session?.role === 'CUSTOMER'
  const isStaff = session?.role === 'RESTAURANT_STAFF'
  const isCourier = session?.role === 'COURIER'

  const deliveryLocation =
    session?.latitude != null && session?.longitude != null
      ? `${session.latitude}, ${session.longitude}`
      : 'Delivery location not set'

  return (
    <div className="account-page">
      <header className="page-header">
        <h2>Account Information</h2>
        <p>View your account details.</p>
      </header>

      <div className="account-card">
        <div className="account-section">
          <h3 className="account-section-title">Personal Information</h3>

          <div className="account-grid-2">
            <div className="account-field">
              <label htmlFor="account-name">Full Name</label>
              <input
                id="account-name"
                className="account-input"
                value={session?.name ?? ''}
                readOnly
              />
            </div>

            <div className="account-field">
              <label htmlFor="account-email">Email Address</label>
              <input
                id="account-email"
                className="account-input"
                value={session?.email ?? ''}
                readOnly
              />
            </div>
          </div>

          <div className="account-field">
            <label htmlFor="account-role">Role</label>
            <input
              id="account-role"
              className="account-input"
              value={ROLE_LABELS[session?.role] ?? session?.role ?? ''}
              readOnly
            />
          </div>

          {isCustomer && (
            <div className="account-field">
              <label htmlFor="account-location">Delivery Location</label>
              <input
                id="account-location"
                className="account-input"
                value={deliveryLocation}
                readOnly
              />
            </div>
          )}
        </div>

        <div className="account-actions">
          <div className="account-actions-nav">
            <Link
              to={
                isCourier
                  ? '/courier'
                  : isStaff
                    ? '/kitchen'
                    : '/orders'
              }
              className="btn-secondary"
            >
              {isCourier
                ? 'My Deliveries'
                : isStaff
                  ? 'Kitchen'
                  : 'View Orders'}
            </Link>

            <Link to="/" className="btn-secondary">
              Home
            </Link>
          </div>
        </div>
      </div>
    </div>
  )
}
