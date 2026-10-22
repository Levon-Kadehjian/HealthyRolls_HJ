import { useState } from 'react'
import { Link } from 'react-router-dom'
import { useSession } from '../auth/useSession'
import { updateSession } from '../auth/session'
import { recordCommand } from '../console/consoleStore'
import PasswordInput from '../components/PasswordInput'
import './AccountPage.css'

export default function AccountPage() {
  const session = useSession()

  const [name, setName] = useState(() => session?.name || '')
  const [email, setEmail] = useState(() => session?.email || '')
  const [address, setAddress] = useState(() => session?.address || '1234 Boulevard Saint-Laurent, Montreal, QC')
  const [role, setRole] = useState(() => session?.role || 'CUSTOMER')
  const [newPassword, setNewPassword] = useState('')
  const [confirmPassword, setConfirmPassword] = useState('')

  const [statusMessage, setStatusMessage] = useState(null)
  const [statusType, setStatusType] = useState('success')
  const [saving, setSaving] = useState(false)

  const handleRoleSelect = (selectedRole) => {
    if (selectedRole === 'COURIER') return
    setRole(selectedRole)
    setStatusMessage(null)
  }

  const handleSubmit = (e) => {
    e.preventDefault()
    setStatusMessage(null)

    if (!name.trim()) {
      setStatusType('error')
      setStatusMessage('Please provide your full name.')
      return
    }

    if (!email.trim()) {
      setStatusType('error')
      setStatusMessage('Please provide an email address.')
      return
    }

    if (newPassword) {
      if (newPassword.length < 6) {
        setStatusType('error')
        setStatusMessage('New password must be at least 6 characters.')
        return
      }
      if (newPassword !== confirmPassword) {
        setStatusType('error')
        setStatusMessage('New passwords do not match.')
        return
      }
    }

    setSaving(true)

    updateSession({
      name: name.trim(),
      email: email.trim(),
      address: address.trim(),
      role,
    })

    recordCommand(
      'UPDATE_PROFILE',
      `role=${role} email=${email.trim()}`,
      'ok',
      'Profile information updated',
    )

    setNewPassword('')
    setConfirmPassword('')
    setStatusType('success')
    setStatusMessage('Your changes have been saved successfully.')
    setSaving(false)
  }

  const isStaff = role === 'RESTAURANT_STAFF'

  return (
    <div className="account-page">
      <header className="page-header">
        <h2>Account Settings</h2>
        <p>Manage your profile, account role, and preferences.</p>
      </header>

      <div className="account-card">
        {statusMessage && (
          <div
            className={`account-status-message account-status-${statusType}`}
            role="status"
          >
            {statusMessage}
          </div>
        )}

        <form onSubmit={handleSubmit}>
          {/* Profile Details */}
          <div className="account-section">
            <h3 className="account-section-title">Personal Information</h3>
            <p className="account-section-desc">Update the details associated with your account.</p>

            <div className="account-grid-2">
              <div className="account-field">
                <label htmlFor="account-name">Full Name</label>
                <input
                  id="account-name"
                  type="text"
                  className="account-input"
                  value={name}
                  onChange={(e) => setName(e.target.value)}
                  placeholder="Jane Doe"
                  required
                />
              </div>

              <div className="account-field">
                <label htmlFor="account-email">Email Address</label>
                <input
                  id="account-email"
                  type="email"
                  className="account-input"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  placeholder="you@example.com"
                  required
                />
              </div>
            </div>

            <div className="account-field">
              <label htmlFor="account-address">Delivery Address</label>
              <input
                id="account-address"
                type="text"
                className="account-input"
                value={address}
                onChange={(e) => setAddress(e.target.value)}
                placeholder="Street address, city, province, postal code"
              />
            </div>
          </div>

          {/* Role Selection */}
          <div className="account-section">
            <h3 className="account-section-title">Account Role</h3>
            <p className="account-section-desc">Select your current operating role.</p>

            <div className="account-role-grid" role="radiogroup" aria-label="Account role">
              <div
                role="radio"
                tabIndex={0}
                aria-checked={role === 'CUSTOMER'}
                className={`account-role-card ${role === 'CUSTOMER' ? 'account-role-selected' : ''}`}
                onClick={() => handleRoleSelect('CUSTOMER')}
                onKeyDown={(e) => (e.key === ' ' || e.key === 'Enter') && handleRoleSelect('CUSTOMER')}
              >
                <span className="account-role-title">Customer</span>
                <span className="account-role-desc">Browse menus and place orders</span>
              </div>

              <div
                role="radio"
                tabIndex={0}
                aria-checked={role === 'RESTAURANT_STAFF'}
                className={`account-role-card ${role === 'RESTAURANT_STAFF' ? 'account-role-selected' : ''}`}
                onClick={() => handleRoleSelect('RESTAURANT_STAFF')}
                onKeyDown={(e) => (e.key === ' ' || e.key === 'Enter') && handleRoleSelect('RESTAURANT_STAFF')}
              >
                <span className="account-role-title">Restaurant Staff</span>
                <span className="account-role-desc">Review and manage kitchen orders</span>
              </div>

              <div
                role="radio"
                aria-checked={false}
                aria-disabled="true"
                className="account-role-card account-role-disabled"
                title="Courier registration is coming in a future update"
              >
                <span className="account-role-tag-soon">Soon</span>
                <span className="account-role-title">Courier</span>
                <span className="account-role-desc">Delivery partner</span>
              </div>
            </div>
          </div>

          {/* Password Section */}
          <div className="account-section">
            <h3 className="account-section-title">Security</h3>
            <p className="account-section-desc">Leave fields blank if you do not wish to change your password.</p>

            <div className="account-grid-2">
              <div className="account-field">
                <label htmlFor="account-new-password">New Password</label>
                <PasswordInput
                  id="account-new-password"
                  className="account-input"
                  value={newPassword}
                  onChange={(e) => setNewPassword(e.target.value)}
                  placeholder="New password (optional)"
                  autoComplete="new-password"
                />
              </div>

              <div className="account-field">
                <label htmlFor="account-confirm-password">Confirm New Password</label>
                <PasswordInput
                  id="account-confirm-password"
                  className="account-input"
                  value={confirmPassword}
                  onChange={(e) => setConfirmPassword(e.target.value)}
                  placeholder="Repeat new password"
                  autoComplete="new-password"
                />
              </div>
            </div>
          </div>

          <div className="account-actions">
            <button type="submit" className="btn-primary" disabled={saving}>
              {saving ? 'Saving...' : 'Save Changes'}
            </button>

            <div className="account-actions-nav">
              {isStaff ? (
                <Link to="/dashboard/kitchen" className="btn-secondary">
                  Kitchen Dashboard
                </Link>
              ) : (
                <Link to="/orders" className="btn-secondary">
                  View Orders
                </Link>
              )}
              <Link to="/" className="btn-secondary">
                Home
              </Link>
            </div>
          </div>
        </form>
      </div>
    </div>
  )
}
