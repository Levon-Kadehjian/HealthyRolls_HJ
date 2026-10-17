import { NavLink } from 'react-router-dom'
import sushiLogo from '../assets/sushi-logo.png'
import './Navbar.css'

export default function Navbar() {
  return (
    <header className="sushi-header">
      <div className="sushi-nav-container">
        {/* Left Column: Home, About Us */}
        <div className="nav-col nav-col-left">
          <nav className="nav-menu-links">
            <NavLink
              to="/"
              end
              className={({ isActive }) =>
                `sushi-nav-link ${isActive ? 'sushi-nav-active' : ''}`
              }
            >
              HOME
            </NavLink>
            <NavLink
              to="/about"
              className={({ isActive }) =>
                `sushi-nav-link ${isActive ? 'sushi-nav-active' : ''}`
              }
            >
              ABOUT US
            </NavLink>
          </nav>
        </div>

        {/* Center Column: Static Brand Display (Not a button) */}
        <div className="nav-col nav-col-center">
          <div className="brand-static" aria-label="HealthyRolls Brand">
            <div className="brand-logo-container">
              <img src={sushiLogo} alt="Sushi Wheel Logo" className="brand-logo-img" />
            </div>
            <h1 className="brand-title">SUSHI WHEEL DELIVERY</h1>
            <div className="brand-pill">
              <span>ROLLS</span>
            </div>
          </div>
        </div>

        {/* Right Column: Order History, My Account */}
        <div className="nav-col nav-col-right">
          <div className="nav-actions-row">
            {/* Order History */}
            <NavLink
              to="/orders"
              className={({ isActive }) =>
                `nav-action-item ${isActive ? 'action-active' : ''}`
              }
            >
              <div className="icon-circle">
                <svg
                  className="nav-svg-icon"
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                  strokeWidth="1.9"
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  aria-hidden="true"
                >
                  <path d="M9 5H7a2 2 0 0 0-2 2v12a2 2 0 0 0 2 2h10a2 2 0 0 0 2-2V7a2 2 0 0 0-2-2h-2" />
                  <rect x="9" y="3" width="6" height="4" rx="1.5" />
                  <path d="M9 14l2 2 4-4" />
                </svg>
              </div>
              <span className="action-label">ORDER HISTORY</span>
            </NavLink>

            {/* My Account */}
            <NavLink
              to="/account"
              className={({ isActive }) =>
                `nav-action-item ${isActive ? 'action-active' : ''}`
              }
            >
              <div className="icon-circle">
                <svg
                  className="nav-svg-icon"
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
              </div>
              <span className="action-label">MY ACCOUNT</span>
            </NavLink>
          </div>
        </div>
      </div>
    </header>
  )
}
