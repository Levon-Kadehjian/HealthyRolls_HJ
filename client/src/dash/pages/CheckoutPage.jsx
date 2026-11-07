import sushiLogo from '../assets/sushi-logo.png'
import { useEffect, useState } from 'react'
import { getRestaurant } from '../api/restaurants'
import { Link } from 'react-router-dom'
import { useCart } from '../cart/useCart'
import { useSession } from '../auth/useSession'
import { placeOrder } from '../api/orders'
import { describeError, recordCommand, runCommand } from '../console/consoleStore'
import { estimateDeliveryWindow } from '../checkout/checkoutForm'
import './CheckoutPage.css'

const currency = new Intl.NumberFormat('en-CA', { style: 'currency', currency: 'CAD' })

export default function CheckoutPage() {
    const { restaurant, lines, itemCount, subtotal, clearCart } = useCart()
    const session = useSession()
    const deliveryLocation =
    session?.latitude != null && session?.longitude != null ? `${session.latitude}, ${session.longitude}` : 'Delivery location not set'
    const [submitting, setSubmitting] = useState(false)
    const [error, setError] = useState('')
    const [placedOrder, setPlacedOrder] = useState(null)

    const [availability, setAvailability] = useState(null)
    const [availabilityError, setAvailabilityError] = useState('')
    const restaurantId = restaurant?.id

    useEffect(() => {
        if (!restaurantId || placedOrder) return

        let cancelled = false
        let timer

        setAvailability(null)
        setAvailabilityError('')

        async function refreshAvailability() {
            try {
                const latest = await getRestaurant(restaurantId)

                if (!cancelled) {
                    setAvailability(latest)
                    setAvailabilityError('')
                }
            } catch (err) {
                const message = await describeError(err)

                if (!cancelled) {
                    setAvailability(null)
                    setAvailabilityError('Unable to check availability. Retrying…')
                    recordCommand(
                        'CHECK_AVAILABILITY',
                        `restaurant=${restaurantId}`,
                        'error',
                        message,
                    )
                }
            } finally {
                if (!cancelled) {
                    timer = setTimeout(refreshAvailability, 5000)
                }
            }
        }

        refreshAvailability()

        return () => {
            cancelled = true
            clearTimeout(timer)
        }
    }, [restaurantId, placedOrder])

    const latestRestaurant =
        availability?.id === restaurantId ? availability : null

    const hasSoldOutItems = latestRestaurant
        ? lines.some((line) => {
            const latestItem = latestRestaurant.menu.find(
                (item) => item.itemId === line.item.itemId,
            )
            return !latestItem || latestItem.status !== 'AVAILABLE'
        })
        : false

    const unavailableReason = availabilityError
        || (!latestRestaurant ? 'Checking availability…' : '')
        || (latestRestaurant.status === 'PAUSED'
            ? 'This restaurant is paused and is not taking new orders.'
            : '')
        || (latestRestaurant.status === 'CLOSED'
            ? 'This restaurant is closed.'
            : '')
        || (hasSoldOutItems
            ? 'One or more selected items are unavailable. Return to your cart to remove them.'
            : '')

    async function handlePlaceOrder() {

        if (submitting || unavailableReason) return
        setError('')

        setSubmitting(true)
        try {
            const order = await runCommand(
                'PLACE_ORDER',
                `restaurant=${restaurant.name} items=${itemCount}`,
                () => placeOrder(restaurant.id, lines),
                (result) => `order ${String(result.orderId).slice(0, 8)} is ${result.status}`,
            )

            setPlacedOrder({
                ...order,
                restaurantName: restaurant.name,
                total: subtotal,
                deliveryLocation,
                estimatedDelivery: estimateDeliveryWindow(),
            })
            clearCart()
        } catch (err) {
            setError(await describeError(err))
        } finally {
            setSubmitting(false)
        }
    }

    if (placedOrder) {
        return (
            <div className="checkout-page">
                <div className="orders-empty-card">
                    <h3>Order placed.</h3>
                    <p>
                        Your order from <strong>{placedOrder.restaurantName}</strong> was sent to the restaurant.
                    </p>
                    <div className="checkout-confirmation-details">
                        <div className="checkout-summary-row">
                            <span>Order number</span>
                            <span>#{String(placedOrder.orderId).slice(0, 8)}</span>
                        </div>
                        <div className="checkout-summary-row">
                            <span>Status</span>
                            <span className="checkout-status">{placedOrder.status}</span>
                        </div>
                        <div className="checkout-summary-row">
                            <span>Estimated delivery</span>
                            <span>{placedOrder.estimatedDelivery}</span>
                        </div>
                        <div className="checkout-summary-row">
                            <span>Deliver to</span>
                            <span className="checkout-confirmation-value">{placedOrder.deliveryLocation}</span>
                        </div>
                        <div className="checkout-summary-row">
                            <span>Item subtotal</span>
                            <span>
                                {currency.format(placedOrder.total)}
                            </span>
                        </div>
                    </div>
                    <div className="checkout-confirmation-actions">
                        <Link to="/dashboard/orders" className="btn-primary">
                            View My Orders
                        </Link>
                        <Link to="/restaurants" className="btn-secondary">
                            Back to Restaurants
                        </Link>
                    </div>
                </div>
            </div>
        )
    }

    if (lines.length === 0) {
        return (
            <div className="checkout-page">
                <header className="page-header">
                    <h2>Checkout</h2>
                    <p>Review your order before sending it to the restaurant.</p>
                </header>
                <div className="orders-empty-card">
                    <h3>Your Cart Is Empty</h3>
                    <p>Add some rolls to your cart before checking out.</p>
                    <Link to="/restaurants" className="btn-primary">
                        Browse Restaurants
                    </Link>
                </div>
            </div>
        )
    }

    return (
        <div className="checkout-page">
            <header className="page-header">
                <h2>Checkout</h2>
                <p>
                    Ordering from <strong>{restaurant?.name}</strong>
                </p>
            </header>

            <div className="checkout-layout">
                <div className="checkout-main">
                <section className="checkout-section">
                    <h3>Delivery Details</h3>
                    <p>Saved delivery location: {deliveryLocation}</p>
                </section>

                    <section className="checkout-section">
                        <h3>Your Items</h3>
                        <ul className="checkout-lines">
                            {lines.map(({ item, quantity }) => (
                                <li key={item.itemId} className="checkout-line">
                                    <div className="checkout-line-image">
                                        <img src={sushiLogo} alt="" loading="lazy" />
                                    </div>
                                    <div className="checkout-line-info">
                                        <h3>{item.name}</h3>
                                        <span className="checkout-line-unit">
                                            {quantity} × {currency.format(Number(item.price))}
                                        </span>
                                    </div>
                                    <span className="checkout-line-total">{currency.format(Number(item.price) * quantity)}</span>
                                </li>
                            ))}
                        </ul>
                    </section>
                </div>

                <aside className="checkout-summary">
                    <h3>Order Summary</h3>
                    <div className="checkout-summary-row">
                        <span>Items</span>
                        <span>{itemCount}</span>
                    </div>
                    <div className="checkout-summary-row checkout-summary-row-nowrap">
                        <span>Estimated delivery</span>
                        <span>{estimateDeliveryWindow()}</span>
                    </div>
                    <div className="checkout-summary-row checkout-summary-total">
                        <span>Total</span>
                        <span>{currency.format(subtotal)}</span>
                    </div>

                    {unavailableReason && (
                        <p className="checkout-error" role="status">
                            {unavailableReason}
                        </p>
                    )}
                    {error && (
                        <p className="checkout-error" role="alert">
                            {error}
                        </p>
                    )}

                    <button
                        type="button"
                        className="btn-primary checkout-place-btn"
                        onClick={handlePlaceOrder}
                        disabled={submitting || Boolean(unavailableReason)}
                    >
                        {submitting ? 'Placing Order…' : 'Place Order'}
                    </button>
                    <Link to="/cart" className="btn-secondary checkout-back-btn">
                        Back to Cart
                    </Link>
                </aside>
            </div>
        </div>
    )
}