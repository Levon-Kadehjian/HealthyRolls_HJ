import { useState } from 'react'
import { Link } from 'react-router-dom'
import { useCart } from '../cart/useCart'
import { useSession } from '../auth/useSession'
import { placeOrder } from '../api/orders'
import { describeError, recordCommand, runCommand } from '../console/consoleStore'
import {
    TEST_CARD,
    estimateDeliveryWindow,
    formatCardNumber,
    formatExpiry,
    validateCheckout,
} from '../checkout/checkoutForm'
import './CheckoutPage.css'

const currency = new Intl.NumberFormat('en-CA', { style: 'currency', currency: 'CAD' })

const EMPTY_FORM = {
    address: '',
    unit: '',
    instructions: '',
    cardName: '',
    cardNumber: '',
    expiry: '',
    cvc: '',
}

export default function CheckoutPage() {
    const { restaurant, lines, itemCount, subtotal, hasSoldOutItems, clearCart } = useCart()
    const session = useSession()
    const [form, setForm] = useState(() => ({ ...EMPTY_FORM, ...TEST_CARD, address: session?.address || '' }))
    const [fieldErrors, setFieldErrors] = useState({})
    const [submitting, setSubmitting] = useState(false)
    const [error, setError] = useState('')
    const [placedOrder, setPlacedOrder] = useState(null)

    function updateField(name, value) {
        setForm((current) => ({ ...current, [name]: value }))
        setFieldErrors((current) => ({ ...current, [name]: undefined }))
    }

    async function handlePlaceOrder() {
        setError('')

        const errors = validateCheckout(form)
        setFieldErrors(errors)
        if (Object.keys(errors).length > 0) {
            recordCommand('PLACE_ORDER', `restaurant=${restaurant.name}`, 'error', 'checkout form incomplete')
            return
        }

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
                address: form.unit.trim() ? `${form.address.trim()}, ${form.unit.trim()}` : form.address.trim(),
                instructions: form.instructions.trim(),
                cardLast4: form.cardNumber.replace(/\D/g, '').slice(-4),
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
                    <h3>Order Placed!</h3>
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
                            <span className="checkout-confirmation-value">{placedOrder.address}</span>
                        </div>
                        {placedOrder.instructions && (
                            <div className="checkout-summary-row">
                                <span>Instructions</span>
                                <span className="checkout-confirmation-value">{placedOrder.instructions}</span>
                            </div>
                        )}
                        <div className="checkout-summary-row">
                            <span>Paid</span>
                            <span>
                                {currency.format(placedOrder.total)} · card ending {placedOrder.cardLast4} (test card)
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

                        <label className="checkout-field">
                            <span>Delivery address</span>
                            <input
                                className={`checkout-input ${fieldErrors.address ? 'checkout-input-invalid' : ''}`}
                                type="text"
                                placeholder="1455 De Maisonneuve Blvd W, Montreal"
                                value={form.address}
                                onChange={(e) => updateField('address', e.target.value)}
                                autoComplete="street-address"
                            />
                            {fieldErrors.address && <span className="checkout-field-error">{fieldErrors.address}</span>}
                        </label>

                        <label className="checkout-field">
                            <span>Apartment / unit (optional)</span>
                            <input
                                className="checkout-input"
                                type="text"
                                placeholder="Apt 1901"
                                value={form.unit}
                                onChange={(e) => updateField('unit', e.target.value)}
                            />
                        </label>

                        <label className="checkout-field">
                            <span>Delivery instructions (optional)</span>
                            <textarea
                                className="checkout-input checkout-textarea"
                                placeholder="Ring the buzzer, leave at the door…"
                                maxLength={200}
                                value={form.instructions}
                                onChange={(e) => updateField('instructions', e.target.value)}
                            />
                        </label>
                    </section>

                    <section className="checkout-section">
                        <h3>Payment</h3>
                        <label className="checkout-field">
                            <span>Name on card</span>
                            <input
                                className={`checkout-input ${fieldErrors.cardName ? 'checkout-input-invalid' : ''}`}
                                type="text"
                                value={form.cardName}
                                onChange={(e) => updateField('cardName', e.target.value)}
                                autoComplete="off"
                            />
                            {fieldErrors.cardName && <span className="checkout-field-error">{fieldErrors.cardName}</span>}
                        </label>

                        <label className="checkout-field">
                            <span>Card number</span>
                            <input
                                className={`checkout-input ${fieldErrors.cardNumber ? 'checkout-input-invalid' : ''}`}
                                type="text"
                                inputMode="numeric"
                                placeholder="4242 4242 4242 4242"
                                value={form.cardNumber}
                                onChange={(e) => updateField('cardNumber', formatCardNumber(e.target.value))}
                                autoComplete="off"
                            />
                            {fieldErrors.cardNumber && <span className="checkout-field-error">{fieldErrors.cardNumber}</span>}
                        </label>

                        <div className="checkout-field-row">
                            <label className="checkout-field">
                                <span>Expiry</span>
                                <input
                                    className={`checkout-input ${fieldErrors.expiry ? 'checkout-input-invalid' : ''}`}
                                    type="text"
                                    inputMode="numeric"
                                    placeholder="MM/YY"
                                    value={form.expiry}
                                    onChange={(e) => updateField('expiry', formatExpiry(e.target.value))}
                                    autoComplete="off"
                                />
                                {fieldErrors.expiry && <span className="checkout-field-error">{fieldErrors.expiry}</span>}
                            </label>

                            <label className="checkout-field">
                                <span>CVC</span>
                                <input
                                    className={`checkout-input ${fieldErrors.cvc ? 'checkout-input-invalid' : ''}`}
                                    type="text"
                                    inputMode="numeric"
                                    placeholder="123"
                                    value={form.cvc}
                                    onChange={(e) => updateField('cvc', e.target.value.replace(/\D/g, '').slice(0, 4))}
                                    autoComplete="off"
                                />
                                {fieldErrors.cvc && <span className="checkout-field-error">{fieldErrors.cvc}</span>}
                            </label>
                        </div>
                    </section>

                    <section className="checkout-section">
                        <h3>Your Items</h3>
                        <ul className="checkout-lines">
                            {lines.map(({ item, quantity }) => (
                                <li key={item.itemId} className="checkout-line">
                                    <div className="checkout-line-image">
                                        {item.imageUrl && <img src={item.imageUrl} alt={item.name} loading="lazy" />}
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

                    {hasSoldOutItems && (
                        <p className="checkout-error" role="alert">
                            Your cart has sold-out items. Go back to your cart to remove them.
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
                        disabled={submitting || hasSoldOutItems}
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