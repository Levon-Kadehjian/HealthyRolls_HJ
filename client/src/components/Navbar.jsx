import { useEffect, useRef, useState } from 'react'
import { Link, NavLink, useLocation, useNavigate } from 'react-router-dom'
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
  const location = useLocation()
  const [menuOpen, setMenuOpen] = useState(false)
  const menuRef = useRef(null)

  useEffect(() => {
    setMenuOpen(false)
  }, [location.pathname])

  useEffect(() => {
    if (!menuOpen) {
      return undefined
    }
    const handleClick = (event) => {
      if (menuRef.current && !menuRef.current.contains(event.target)) {
        setMenuOpen(false)
      }
    }
    const handleKey = (event) => {
      if (event.key === 'Escape') {
        setMenuOpen(false)
      }
    }
    document.addEventListener('mousedown', handleClick)
    document.addEventListener('keydown', handleKey)
    return () => {
      document.removeEventListener('mousedown', handleClick)
      document.removeEventListener('keydown', handleKey)
    }
  }, [menuOpen])

  const logout = () => {
    setMenuOpen(false)
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
              <div className="nav-account" ref={menuRef}>
                <button
                  type="button"
                  className="nav-avatar"
                  aria-label="Account menu"
                  aria-haspopup="menu"
                  aria-expanded={menuOpen}
                  onClick={() => setMenuOpen((open) => !open)}
                >
                  {(session.email?.[0] ?? '?').toUpperCase()}
                </button>
                {menuOpen && (
                  <div className="nav-menu" role="menu">
                    {session.email && (
                      <div className="nav-menu-header">
                        <span className="nav-menu-label">Signed in as</span>
                        <span className="nav-menu-email">{session.email}</span>
                      </div>
                    )}
                    <Link to="/account" className="nav-menu-item" role="menuitem">
                      <svg
                        className="nav-menu-icon"
                        viewBox="0 0 24 24"
                        fill="none"
                        stroke="currentColor"
                        strokeWidth="1.9"
                        strokeLinecap="round"
                        strokeLinejoin="round"
                        aria-hidden="true"
                      >
                        <circle cx="12" cy="12" r="3" />
                        <path d="M19.4 15a1.7 1.7 0 0 0 .3 1.8l.1.1a2 2 0 1 1-2.8 2.8l-.1-.1a1.7 1.7 0 0 0-1.8-.3 1.7 1.7 0 0 0-1 1.5V21a2 2 0 1 1-4 0v-.1a1.7 1.7 0 0 0-1.1-1.5 1.7 1.7 0 0 0-1.8.3l-.1.1a2 2 0 1 1-2.8-2.8l.1-.1a1.7 1.7 0 0 0 .3-1.8 1.7 1.7 0 0 0-1.5-1H3a2 2 0 1 1 0-4h.1a1.7 1.7 0 0 0 1.5-1.1 1.7 1.7 0 0 0-.3-1.8l-.1-.1a2 2 0 1 1 2.8-2.8l.1.1a1.7 1.7 0 0 0 1.8.3H9a1.7 1.7 0 0 0 1-1.5V3a2 2 0 1 1 4 0v.1a1.7 1.7 0 0 0 1 1.5 1.7 1.7 0 0 0 1.8-.3l.1-.1a2 2 0 1 1 2.8 2.8l-.1.1a1.7 1.7 0 0 0-.3 1.8V9a1.7 1.7 0 0 0 1.5 1H21a2 2 0 1 1 0 4h-.1a1.7 1.7 0 0 0-1.5 1z" />
                      </svg>
                      Account Settings
                    </Link>
                    <div className="nav-menu-divider" role="separator" />
                    <button type="button" className="nav-menu-item nav-menu-item-danger" role="menuitem" onClick={logout}>
                      <svg
                        className="nav-menu-icon"
                        viewBox="0 0 24 24"
                        fill="none"
                        stroke="currentColor"
                        strokeWidth="1.9"
                        strokeLinecap="round"
                        strokeLinejoin="round"
                        aria-hidden="true"
                      >
                        <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4" />
                        <path d="M16 17l5-5-5-5" />
                        <path d="M21 12H9" />
                      </svg>
                      Log out
                    </button>
                  </div>
                )}
              </div>
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
