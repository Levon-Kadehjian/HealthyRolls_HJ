import { Link, NavLink, useNavigate } from 'react-router-dom'
import sushiLogo from '../assets/sushi-logo.png'
import { useCart } from '../cart/useCart'
import { useSession } from '../auth/useSession'
import { clearSession } from '../auth/session'
import { recordCommand } from '../console/consoleStore'
import './Navbar.css'

const linkClass = ({ isActive }) => `nav-link ${isActive ? 'nav-link-active' : ''}`

export default function Navbar() {
  const { itemCount } = useCart()
  const session = useSession()
  const navigate = useNavigate()

  const logout = () => {
    clearSession()
    recordCommand('LOGOUT', '', 'ok', 'session cleared')
    navigate('/')
  }

  const isStaff = session?.role === 'RESTAURANT_STAFF'

  return (
    <header className="site-header">
      <div className="site-nav">
        <Link to={isStaff ? '/dashboard/kitchen' : '/'} className="site-brand" aria-label="HealthyRolls home">
          <img src={sushiLogo} alt="" className="site-brand-logo" />
          <span className="site-brand-name">HealthyRolls</span>
        </Link>

        <nav className="nav-links" aria-label="Main">
          {!isStaff && (
            <NavLink to="/" end className={linkClass}>
              Home
            </NavLink>
          )}
          {session && !isStaff && (
            <NavLink to="/restaurants" className={linkClass}>
              Restaurants
            </NavLink>
          )}
          {isStaff && (
            <NavLink to="/dashboard/kitchen" className={linkClass}>
              Kitchen
            </NavLink>
          )}
          <NavLink to="/about" className={linkClass}>
            About
          </NavLink>
        </nav>

        <div className="nav-actions">
          {session ? (
            <>
              {isStaff && <span className="nav-role-tag">Staff</span>}
              {!isStaff && (
                <NavLink to="/orders" className={linkClass}>
                  Orders
                </NavLink>
              )}
              {!isStaff && (
                <NavLink to="/cart" className={({ isActive }) => `nav-icon-link ${isActive ? 'nav-link-active' : ''}`} aria-label={`Cart, ${itemCount} items`}>
                  <svg
                    className="nav-icon"
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="currentColor"
                    strokeWidth="1.9"
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    aria-hidden="true"
                  >
                    <circle cx="9" cy="20" r="1.5" />
                    <circle cx="18" cy="20" r="1.5" />
                    <path d="M2 3h3l2.7 12.4a2 2 0 0 0 2 1.6h7.6a2 2 0 0 0 2-1.5L21 8H6" />
                  </svg>
                  {itemCount > 0 && <span className="nav-badge" aria-hidden="true">{itemCount}</span>}
                </NavLink>
              )}
              <NavLink to="/account" className={({ isActive }) => `nav-icon-link ${isActive ? 'nav-link-active' : ''}`} aria-label="My account">
                <svg
                  className="nav-icon"
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                  strokeWidth="1.9"
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  aria-hidden="true"
                >
                  <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2" />
                  <circle cx="12" cy="7" r="4" />
                </svg>
              </NavLink>
              <button type="button" className="nav-button nav-button-ghost" onClick={logout}>
                Log out
              </button>
            </>
          ) : (
            <Link to="/login" className="nav-button">
              Log in
            </Link>
          )}
        </div>
      </div>
    </header>
  )
}
