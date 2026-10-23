import { useState } from 'react'
import { Link, useLoaderData, useNavigate } from 'react-router-dom'
import { useSession } from '../auth/useSession'
import { useCart } from '../cart/useCart'
import './RestaurantMenuPage.css'

const currency = new Intl.NumberFormat('en-CA', { style: 'currency', currency: 'CAD' })

const STATUS_LABELS = { OPEN: 'Open', PAUSED: 'Paused', CLOSED: 'Closed' }

const UNAVAILABLE_MESSAGES = {
  PAUSED: 'This restaurant is paused and is not taking new orders right now.',
  CLOSED: 'This restaurant is closed and is not taking orders.',
}

export default function RestaurantMenuPage() {
  const restaurant = useLoaderData()
  const { restaurant: cartRestaurant, lines, itemCount, addItem, replaceCart } = useCart()
  const [notice, setNotice] = useState(null)
  const session = useSession()
  const navigate = useNavigate()

  const isOpen = restaurant.status === 'OPEN'
  const cartIsHere = cartRestaurant?.id === restaurant.id
  const cartSummary = { id: restaurant.id, name: restaurant.name, status: restaurant.status }

  function showResult(added, item, prefix = '') {
    setNotice(
      added
        ? { type: 'success', message: `${prefix}${item.name} added to your cart.` }
        : { type: 'warning', message: `${item.name} could not be added.` },
    )
  }

  function handleAdd(item) {
    if (!session) {
      navigate('/login')
    } else if (addItem(cartSummary, item)) {
      showResult(true, item)
    } else if (cartRestaurant && !cartIsHere && lines.length > 0) {
      setNotice({ type: 'conflict', item })
    } else {
      showResult(false, item)
    }
  }

  return (
    <div className="menu-page">
      <div className="menu-back">
        <Link to="/restaurants" className="restaurant-back-link">
          ← All restaurants
        </Link>
      </div>

      <header className="page-header restaurant-header">
        <div>
          <div className="restaurant-title-row">
            <h2>{restaurant.name}</h2>
            <span className={`restaurant-status restaurant-status-${restaurant.status.toLowerCase()}`}>
              {STATUS_LABELS[restaurant.status] ?? restaurant.status}
            </span>
          </div>
          <p>{restaurant.menu.length} items on the menu</p>
        </div>

        {cartIsHere && itemCount > 0 && (
          <Link to="/cart" className="btn-primary">
            View Cart ({itemCount})
          </Link>
        )}
      </header>

      {!isOpen && (
        <div className="restaurant-banner restaurant-banner-warning" role="status">
          {UNAVAILABLE_MESSAGES[restaurant.status] ?? 'This restaurant is not taking orders.'}
        </div>
      )}

      <div className="restaurant-notice" aria-live="polite">
        {notice?.type === 'conflict' ? (
          <div className="restaurant-banner restaurant-banner-warning">
            <span>
              Your cart has items from <strong>{cartRestaurant.name}</strong>. An order can only
              come from one restaurant.
            </span>
            <div className="restaurant-banner-actions">
              <button
                type="button"
                className="btn-primary"
                onClick={() => showResult(replaceCart(cartSummary, notice.item), notice.item, 'Cart cleared. ')}
              >
                Clear Cart and Add
              </button>
              <button type="button" className="btn-secondary" onClick={() => setNotice(null)}>
                Keep Current Cart
              </button>
            </div>
          </div>
        ) : (
          notice && <div className={`restaurant-banner restaurant-banner-${notice.type}`}>{notice.message}</div>
        )}
      </div>

      {restaurant.menu.length === 0 ? (
        <div className="orders-empty-card">
          <h3>No Menu Yet</h3>
          <p>This restaurant has not published a menu.</p>
        </div>
      ) : (
        <div className="menu-grid">
          {restaurant.menu.map((item) => {
            const soldOut = item.status === 'SOLD_OUT'
            const unavailable = soldOut || !isOpen
            const inCart = (cartIsHere && lines.find((line) => line.item.itemId === item.itemId)?.quantity) || 0

            return (
              <article key={item.itemId} className={`menu-card ${unavailable ? 'menu-card-unavailable' : ''}`}>
                <div>
                  <div className="menu-card-top">
                    <h3>{item.name}</h3>
                    {soldOut ? (
                      <span className="menu-tag menu-tag-sold-out">Sold Out</span>
                    ) : (
                      inCart > 0 && <span className="menu-tag">{inCart} in cart</span>
                    )}
                  </div>
                </div>

                <div className="menu-card-footer">
                  <span className="menu-price">{currency.format(Number(item.price))}</span>
                  <button type="button" className="btn-add" disabled={unavailable} onClick={() => handleAdd(item)}>
                    {!isOpen ? 'Unavailable' : soldOut ? 'Sold Out' : 'Add to Cart'}
                  </button>
                </div>
              </article>
            )
          })}
        </div>
      )}
    </div>
  )
}
