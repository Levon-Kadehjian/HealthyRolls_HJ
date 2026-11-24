import { Link, NavLink } from 'react-router-dom'
import sushiLogo from '../assets/sushi-logo.png'
import { useCart } from '../cart/useCart'
import { useSession } from '../auth/useSession'
import AccountMenu from './AccountMenu'
import './Navbar.css'
import { ShoppingCart } from 'lucide-react'

const linkClass = ({ isActive }) => `nav-link ${isActive ? 'nav-link-active' : ''}`

export default function Navbar() {
  const { itemCount } = useCart()
  const session = useSession()

  const isStaff = session?.role === 'RESTAURANT_STAFF'
  const isCourier = session?.role === 'COURIER'

  return (
    <header className="site-header">
      <div className="site-nav">
        <Link to={isStaff ? '/kitchen' : isCourier ? '/courier' : '/'} className="site-brand" aria-label="HealthyRolls home">
          <img src={sushiLogo} alt="" className="site-brand-logo" />
          <span className="site-brand-name">HealthyRolls</span>
        </Link>

        <nav className="nav-links" aria-label="Main">
          {!isStaff && !isCourier && (
            <NavLink to="/" end className={linkClass}>
              Home
            </NavLink>
          )}
          {session && !isStaff && !isCourier && (
            <>
              <NavLink to="/restaurants" className={linkClass}>
                Restaurants
              </NavLink>
              <NavLink to="/map" className={linkClass}>
                Map
              </NavLink>
            </>
          )}
          {isCourier && (
            <>
              <NavLink to="/courier" className={linkClass}>
                Deliveries
              </NavLink>
              <NavLink to="/map" className={linkClass}>
                Map
              </NavLink>
            </>
          )}
          {isStaff && (
            <>
              <NavLink to="/kitchen" className={linkClass}>
                Kitchen
              </NavLink>
              {session?.restaurantId && (
                <NavLink to={`/restaurants/${session.restaurantId}`} className={linkClass}>
                  Menu
                </NavLink>
              )}
              <NavLink to="/map" className={linkClass}>
                Map
              </NavLink>
            </>
          )}
          <NavLink to="/about" className={linkClass}>
            About
          </NavLink>
        </nav>

        <div className="nav-actions">
          {session && (
            <>
              {!isStaff && (
                <NavLink to="/orders" className={linkClass}>
                  Orders
                </NavLink>
              )}
              {session.role === 'CUSTOMER' && (
                <NavLink to="/cart" className={({ isActive }) => `nav-icon-link ${isActive ? 'nav-link-active' : ''}`} aria-label={`Cart, ${itemCount} items`}>
                  <ShoppingCart className="nav-icon" strokeWidth={1.9} aria-hidden="true" />
                  {itemCount > 0 && <span className="nav-badge" aria-hidden="true">{itemCount}</span>}
                </NavLink>
              )}
            </>
          )}
          <AccountMenu session={session} />
        </div>
      </div>
    </header>
  )
}
