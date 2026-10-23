import sushiLogo from '../assets/sushi-logo.png'
import { Link } from 'react-router-dom'
import { useCart } from '../cart/useCart'
import './CartPage.css'

const currency = new Intl.NumberFormat('en-CA', { style: 'currency', currency: 'CAD' })

export default function CartPage() {
  const { restaurant, lines, itemCount, subtotal, hasSoldOutItems, maxQuantity, updateQuantity, removeItem, clearCart } =
    useCart()

  return (
    <div className="cart-page">
      <header className="page-header">
        <h2>Your Cart</h2>
        <p>
          {lines.length === 0 ? (
            'Review your rolls before placing your order.'
          ) : (
            <>
              Ordering from <strong>{restaurant?.name}</strong>
            </>
          )}
        </p>
      </header>

      {lines.length === 0 ? (
        <div className="orders-empty-card">
          <h3>Your Cart Is Empty</h3>
          <p>Browse a restaurant menu and add some rolls to get started.</p>
          <Link to="/restaurants" className="btn-primary">
            Browse Restaurants
          </Link>
        </div>
      ) : (
        <div className="cart-layout">
          <ul className="cart-lines">
            {lines.map(({ item, quantity }) => {
              const soldOut = item.status === 'SOLD_OUT'
              return (
                <li key={item.itemId} className={`cart-line ${soldOut ? 'cart-line-sold-out' : ''}`}>
                  <div className="cart-line-image">
                    <img src={sushiLogo} alt="" loading="lazy" />
                  </div>
                  <div className="cart-line-info">
                    <h3>{item.name}</h3>
                    <span className="cart-line-unit">{currency.format(Number(item.price))} each</span>
                    {soldOut && <span className="cart-sold-out-tag">Sold out</span>}
                  </div>

                  <div className="cart-quantity" role="group" aria-label={`Quantity of ${item.name}`}>
                    <button
                      type="button"
                      className="cart-quantity-btn"
                      onClick={() => updateQuantity(item.itemId, quantity - 1)}
                      disabled={quantity <= 1}
                      aria-label={`Decrease ${item.name}`}
                    >
                      −
                    </button>
                    <span className="cart-quantity-value" aria-live="polite">
                      {quantity}
                    </span>
                    <button
                      type="button"
                      className="cart-quantity-btn"
                      onClick={() => updateQuantity(item.itemId, quantity + 1)}
                      disabled={quantity >= maxQuantity}
                      aria-label={`Increase ${item.name}`}
                    >
                      +
                    </button>
                  </div>

                  <span className="cart-line-total">{currency.format(Number(item.price) * quantity)}</span>

                  <button
                    type="button"
                    className="cart-remove-btn"
                    onClick={() => removeItem(item.itemId)}
                    aria-label={`Remove ${item.name}`}
                  >
                    Remove
                  </button>
                </li>
              )
            })}
          </ul>

          <aside className="cart-summary">
            <h3>Order Summary</h3>
            <div className="cart-summary-row">
              <span>Items</span>
              <span>{itemCount}</span>
            </div>
            <div className="cart-summary-row cart-summary-total">
              <span>Subtotal</span>
              <span>{currency.format(subtotal)}</span>
            </div>

            {hasSoldOutItems ? (
              <>
                <p className="cart-warning" role="alert">
                  Remove sold-out items before checking out.
                </p>
                <button type="button" className="btn-primary cart-checkout-btn" disabled>
                  Proceed to Checkout
                </button>
              </>
            ) : (
              <Link to="/checkout" className="btn-primary cart-checkout-btn">
                Proceed to Checkout
              </Link>
            )}

            <button type="button" className="btn-secondary cart-clear-btn" onClick={clearCart}>
              Clear Cart
            </button>
          </aside>
        </div>
      )}
    </div>
  )
}
