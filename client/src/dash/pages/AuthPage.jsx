import { useEffect, useState } from 'react'
import { Link, useNavigate, useSearchParams } from 'react-router-dom'
import sushiLogo from '../assets/sushi-logo.png'
import { useSession } from '../auth/useSession'
import { loginUser, registerUser } from '../api/auth'
import { recordCommand } from '../console/consoleStore'
import PasswordInput from '../components/PasswordInput'
import './AuthPage.css'

const ROLE_BUTTON_LABELS = {
  CUSTOMER: 'Customer',
  RESTAURANT_STAFF: 'Staff',
  COURIER: 'Courier',
  OPERATOR: 'Operator',
}

export default function AuthPage({ initialMode = 'login' }) {
  const [searchParams] = useSearchParams()
  const navigate = useNavigate()
  const session = useSession()

  const [mode, setMode] = useState(() => searchParams.get('mode') || initialMode)
  const [role, setRole] = useState('CUSTOMER')
  const [name, setName] = useState('')
  const [email, setEmail] = useState('')
  const [latitude, setLatitude] = useState('')
  const [longitude, setLongitude] = useState('')
  const [password, setPassword] = useState('')
  const [confirmPassword, setConfirmPassword] = useState('')
  const [error, setError] = useState('')
  const [submitting, setSubmitting] = useState(false)

  // Redirect if already authenticated
  useEffect(() => {
    if (session) {
      if (session.role === 'RESTAURANT_STAFF') {
        navigate('/dashboard/kitchen', { replace: true })
      } else if (session.role === 'COURIER') {
        navigate('/dashboard/courier', { replace: true })
      } else {
        navigate('/restaurants', { replace: true })
      }
    }
  }, [session, navigate])

  const handleRoleSelect = (selectedRole) => {
    //if (selectedRole === 'COURIER') return (unblock the courrier)
    setRole(selectedRole)
    setError('')
  }

  const handleQuickFill = (targetRole) => {
    setRole(targetRole)
    setError('')
    if (targetRole === 'CUSTOMER') {
      setEmail('customer@healthyrolls.com')
      setPassword('customer123')
      setName('Test Customer')
    } else if (targetRole === 'RESTAURANT_STAFF') {
      setEmail('staff@kyo.bar')
      setPassword('staff123')
      setName('Kyo Bar Staff')
    } else if (targetRole === 'COURIER') {
      setEmail('courier@healthyrolls.com')
      setPassword('courier123')
      setName('Test Courier')
    } else if (targetRole === 'OPERATOR') {
      setEmail('operator@healthyrolls.com')
      setPassword('operator123')
      setName('Test Operator')
    }
  }

  const reportValidationError = (message) => {
    setError(message)
    recordCommand(mode === 'signup' ? 'REGISTER' : 'LOGIN', 'Form validation', 'error', message)
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')

    if (mode === 'signup') {
      const lat = Number(latitude)
      const lon = Number(longitude)
      
      if (
        latitude.trim() === '' ||
        longitude.trim() === '' ||
        !Number.isFinite(lat) ||
        !Number.isFinite(lon) ||
        lat < -90 ||
        lat > 90 ||
        lon < -180 ||
        lon > 180
      ) {
        reportValidationError('Enter a valid delivery latitude and longitude.')
        return
      }
      if (!name.trim()) {
        reportValidationError('Please enter your full name.')
        return
      }
      if (!password.trim() || password.length < 6) {
        reportValidationError('Password must be at least 6 characters.')
        return
      }
      if (password !== confirmPassword) {
        reportValidationError('Passwords do not match.')
        return
      }
    }

    setSubmitting(true)
    try {
      const res =
        mode === 'signup'
          ? await registerUser({ name: name.trim(), email: email.trim(), password, latitude: Number(latitude), longitude: Number(longitude),})
          : await loginUser({ email: email.trim(), password, role })

      if (!res.ok) {
        setError(res.error || 'Authentication failed. Please try again.')
        setSubmitting(false)
        return
      }

      // Successful login/signup: navigate according to role
      const effectiveRole = res.session?.role || (mode === 'signup' ? 'CUSTOMER' : role)
      if (effectiveRole === 'RESTAURANT_STAFF') {
        navigate('/dashboard/kitchen')
      } else if (effectiveRole === 'COURIER') {
        navigate('/dashboard/courier')
      } else {
        navigate('/restaurants')
      }
    } catch {
      setError('An unexpected error occurred. Please try again.')
      setSubmitting(false)
    }
  }

  return (
    <div className="auth-page">
      <div className="auth-card">
        <Link to="/" className="auth-back" aria-label="Back to home">
          <span aria-hidden="true">&larr;</span>
          Back
        </Link>
        <header className="auth-header">
          <img src={sushiLogo} alt="HealthyRolls" className="auth-logo" />
          <h2>{mode === 'signup' ? 'Create an Account' : 'Welcome to HealthyRolls'}</h2>
          <p className="auth-subtitle">
            {mode === 'signup'
              ? 'Create a customer account to start ordering rolls'
              : 'Sign in to browse restaurants or manage your kitchen'}
          </p>
        </header>

        <div className="auth-tabs" role="tablist" aria-label="Authentication modes">
          <button
            type="button"
            role="tab"
            aria-selected={mode === 'login'}
            className={`auth-tab ${mode === 'login' ? 'auth-tab-active' : ''}`}
            onClick={() => {
              setMode('login')
              setError('')
            }}
          >
            Log In
          </button>
          <button
            type="button"
            role="tab"
            aria-selected={mode === 'signup'}
            className={`auth-tab ${mode === 'signup' ? 'auth-tab-active' : ''}`}
            onClick={() => {
              setMode('signup')
              setError('')
            }}
          >
            Sign Up
          </button>
        </div>

        <form
          onSubmit={handleSubmit}
          onInvalid={(event) => reportValidationError(event.target.validationMessage)}
          className="auth-form"
        >
          {mode === 'login' && (
          <div className="auth-role-section">
            <span className="auth-role-label">Choose your role</span>
            <div className="auth-role-grid" role="radiogroup" aria-label="Select role">
              {/* Customer */}
              <div
                role="radio"
                tabIndex={0}
                aria-checked={role === 'CUSTOMER'}
                className={`auth-role-card ${role === 'CUSTOMER' ? 'auth-role-selected' : ''}`}
                onClick={() => handleRoleSelect('CUSTOMER')}
                onKeyDown={(e) => (e.key === ' ' || e.key === 'Enter') && handleRoleSelect('CUSTOMER')}
              >
                <div className="auth-role-icon">
                  <svg viewBox="0 0 24 24" fill="none" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                    <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2" />
                    <circle cx="12" cy="7" r="4" />
                  </svg>
                </div>
                <span className="auth-role-title">Customer</span>
                <span className="auth-role-desc">Browse & order sushi</span>
                {role === 'CUSTOMER' && <span className="auth-role-check">✓</span>}
              </div>

              {/* Restaurant Staff */}
              <div
                role="radio"
                tabIndex={0}
                aria-checked={role === 'RESTAURANT_STAFF'}
                className={`auth-role-card ${role === 'RESTAURANT_STAFF' ? 'auth-role-selected' : ''}`}
                onClick={() => handleRoleSelect('RESTAURANT_STAFF')}
                onKeyDown={(e) => (e.key === ' ' || e.key === 'Enter') && handleRoleSelect('RESTAURANT_STAFF')}
              >
                <div className="auth-role-icon">
                  <svg viewBox="0 0 24 24" fill="none" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                    <path d="M6 13.87A4 4 0 0 1 7.41 6a5.11 5.11 0 0 1 1.05-1.54 5 5 0 0 1 7.08 0A5.11 5.11 0 0 1 16.59 6 4 4 0 0 1 18 13.87V21H6Z" />
                    <line x1="6" y1="17" x2="18" y2="17" />
                  </svg>
                </div>
                <span className="auth-role-title">Restaurant Staff</span>
                <span className="auth-role-desc">Kitchen orders & tickets</span>
                {role === 'RESTAURANT_STAFF' && <span className="auth-role-check">✓</span>}
              </div>

              {/* Courier */}
              <div
                role="radio"
                tabIndex={0}
                aria-checked={role === 'COURIER'}
                className={`auth-role-card ${role === 'COURIER' ? 'auth-role-selected' : ''}`}
                onClick={() => handleRoleSelect('COURIER')}
                onKeyDown={(e) => (e.key === ' ' || e.key === 'Enter') && handleRoleSelect('COURIER')}
              >
                <div className="auth-role-icon">
                  <svg viewBox="0 0 24 24" fill="none" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                    <circle cx="18.5" cy="17.5" r="3.5" />
                    <circle cx="5.5" cy="17.5" r="3.5" />
                    <circle cx="15" cy="5" r="1" />
                    <path d="M12 17.5V14l-3-3 4-3 2 3h2" />
                  </svg>
                </div>
                <span className="auth-role-title">Courier</span>
                <span className="auth-role-desc">Delivery partner</span>
                {role === 'COURIER' && <span className="auth-role-check">✓</span>}
              </div>

              {/* Operator */}
              <div
                role="radio"
                tabIndex={0}
                aria-checked={role === 'OPERATOR'}
                className={`auth-role-card ${role === 'OPERATOR' ? 'auth-role-selected' : ''}`}
                onClick={() => handleRoleSelect('OPERATOR')}
                onKeyDown={(e) => (e.key === ' ' || e.key === 'Enter') && handleRoleSelect('OPERATOR')}
              >
                <div className="auth-role-icon">
                  <svg viewBox="0 0 24 24" fill="none" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                    <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z" />
                  </svg>
                </div>
                <span className="auth-role-title">Operator</span>
                <span className="auth-role-desc">Platform admin</span>
                {role === 'OPERATOR' && <span className="auth-role-check">✓</span>}
              </div>
            </div>
          </div>

          )}

          {error && (
            <div className="auth-error" role="alert">
              <span>{error}</span>
            </div>
          )}

          {mode === 'signup' && (
            <div className="auth-field">
              <label htmlFor="auth-name">Full Name</label>
              <input
                id="auth-name"
                type="text"
                className="auth-input"
                placeholder="e.g. Jane Doe"
                value={name}
                onChange={(e) => setName(e.target.value)}
                required
                autoComplete="name"
              />
            </div>
          )}
          {mode === 'signup' && (
            <>
              <div className="auth-field">
                <label htmlFor="auth-latitude">Delivery latitude</label>
                <input
                  id="auth-latitude"
                  type="number"
                  className="auth-input"
                  min="-90"
                  max="90"
                  step="any"
                  value={latitude}
                  onChange={(e) => setLatitude(e.target.value)}
                  required
                />
              </div>

              <div className="auth-field">
                <label htmlFor="auth-longitude">Delivery longitude</label>
                <input
                  id="auth-longitude"
                  type="number"
                  className="auth-input"
                  min="-180"
                  max="180"
                  step="any"
                  value={longitude}
                  onChange={(e) => setLongitude(e.target.value)}
                  required
                />
              </div>
            </>
          )}

          <div className="auth-field">
            <label htmlFor="auth-email">Email Address</label>
            <input
              id="auth-email"
              type="email"
              className="auth-input"
              placeholder="you@example.com"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              required
              autoComplete="email"
            />
          </div>

          <div className="auth-field">
            <label htmlFor="auth-password">Password</label>
            <PasswordInput
              id="auth-password"
              className="auth-input"
              placeholder={mode === 'signup' ? 'At least 6 characters' : 'Enter your password'}
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
              autoComplete={mode === 'signup' ? 'new-password' : 'current-password'}
            />
          </div>

          {mode === 'signup' && (
            <div className="auth-field">
              <label htmlFor="auth-confirm-password">Confirm Password</label>
              <PasswordInput
                id="auth-confirm-password"
                className="auth-input"
                placeholder="Repeat your password"
                value={confirmPassword}
                onChange={(e) => setConfirmPassword(e.target.value)}
                required
                autoComplete="new-password"
              />
            </div>
          )}

          <button type="submit" className="btn-primary auth-submit-btn" disabled={submitting}>
            {submitting
              ? 'Please wait…'
              : mode === 'signup'
                ? 'Sign Up as Customer'
                : `Log In as ${ROLE_BUTTON_LABELS[role]}`}
          </button>
        </form>

        {mode === 'login' && (
        <div className="auth-demo-bar">
          <span className="auth-demo-label">Quick autofill:</span>
          <div className="auth-demo-buttons">
            <button
              type="button"
              className="auth-demo-btn"
              onClick={() => handleQuickFill('CUSTOMER')}
            >
              Fill Customer
            </button>
            <button
              type="button"
              className="auth-demo-btn"
              onClick={() => handleQuickFill('RESTAURANT_STAFF')}
            >
              Fill Staff
            </button>           
            <button
              type="button"
              className="auth-demo-btn"
              onClick={() => handleQuickFill('COURIER')}
            >
              Fill Courier
            </button>
            <button
              type="button"
              className="auth-demo-btn"
              onClick={() => handleQuickFill('OPERATOR')}
            >
              Fill Operator
            </button>            
          </div>
        </div>
        

        )}

        <footer className="auth-footer">
          {mode === 'signup' ? (
            <p>
              Already have an account?{' '}
              <button
                type="button"
                className="auth-switch-btn"
                onClick={() => {
                  setMode('login')
                  setError('')
                }}
              >
                Log in here
              </button>
            </p>
          ) : (
            <p>
              Don't have an account yet?{' '}
              <button
                type="button"
                className="auth-switch-btn"
                onClick={() => {
                  setMode('signup')
                  setError('')
                }}
              >
                Sign up here
              </button>
            </p>
          )}
        </footer>
      </div>
    </div>
  )
}
