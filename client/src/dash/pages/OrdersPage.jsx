import { useState } from 'react'
import { Link, useFetcher, useLoaderData, useRevalidator } from 'react-router-dom'
import {
  ACTIVE_STATUSES,
  CUSTOMER_CANCELLABLE_STATUSES,
  formatDateTime,
  formatMoney,
  newestFirst,
  statusLabel,
} from '../orders/orderHistory'
import './OrdersPage.css'

const shortId = (id) => String(id ?? '').slice(0, 8)

function CancelOrder({ order, fetcher }) {
  const [confirming, setConfirming] = useState(false)
  const busy = fetcher.state !== 'idle'

  if (!CUSTOMER_CANCELLABLE_STATUSES.includes(order.status)) {
    return null
  }

  if (!confirming) {
    return (
      <div className="order-actions">
        <button type="button" className="order-btn order-btn-danger" onClick={() => setConfirming(true)}>
          Cancel order
        </button>
      </div>
    )
  }

  return (
    <fetcher.Form method="post" className="order-confirm">
      <input type="hidden" name="orderId" value={order.orderId} />
      <input type="hidden" name="status" value={order.status} />
      <p className="order-confirm-text">
        <strong>Cancel this order?</strong>
        <span>The restaurant will be notified. This can’t be undone.</span>
      </p>
      <div className="order-confirm-buttons">
        <button type="button" className="order-btn order-btn-neutral" onClick={() => setConfirming(false)} disabled={busy}>
          Keep order
        </button>
        <button type="submit" name="intent" value="CANCEL" className="order-btn order-btn-danger" disabled={busy}>
          {busy ? 'Cancelling…' : 'Yes, cancel'}
        </button>
      </div>
    </fetcher.Form>
  )
}

function OrderCard({ order }) {
  const fetcher = useFetcher()
  const error = fetcher.data?.ok === false ? fetcher.data.error : null
  const placedAt = formatDateTime(order.placedAt)
  const updatedAt = formatDateTime(order.updatedAt)

  return (
    <li className={`order-card order-card-${order.status.toLowerCase()}`}>
      <div className="order-card-header">
        <div>
          <h4>{order.restaurantName ?? 'Restaurant'}</h4>
          <span className="order-card-id">Order #{shortId(order.orderId)}</span>
        </div>
        <span className={`order-status order-status-${order.status.toLowerCase()}`}>{statusLabel(order.status)}</span>
      </div>

      <dl className="order-card-meta">
        {placedAt && (
          <div>
            <dt>Placed</dt>
            <dd>{placedAt}</dd>
          </div>
        )}
        {updatedAt && updatedAt !== placedAt && (
          <div>
            <dt>Last update</dt>
            <dd>{updatedAt}</dd>
          </div>
        )}
      </dl>

      <ul className="order-lines">
        {order.lines.map((line) => (
          <li key={line.menuItemId}>
            <span>
              <span className="order-line-qty">{line.quantity}×</span> {line.name ?? `Item ${shortId(line.menuItemId)}`}
            </span>
            {line.price !== null && <span>{formatMoney(line.price * line.quantity)}</span>}
          </li>
        ))}
      </ul>

      {order.total !== null && (
        <div className="order-total">
          <span>Total</span>
          <span>{formatMoney(order.total)}</span>
        </div>
      )}

      {order.rejectionReason && <p className="order-note">Restaurant note: “{order.rejectionReason}”</p>}

      {error && (
        <p className="order-error" role="alert">
          {error}
        </p>
      )}

      <CancelOrder order={order} fetcher={fetcher} />
    </li>
  )
}

function OrderSection({ title, orders, empty }) {
  return (
    <section className="orders-section">
      <h3>
        {title} <span className="orders-count">{orders.length}</span>
      </h3>
      {orders.length === 0 ? (
        <p className="orders-section-empty">{empty}</p>
      ) : (
        <ul className="order-list">
          {orders.map((order) => (
            <OrderCard key={order.orderId} order={order} />
          ))}
        </ul>
      )}
    </section>
  )
}

export default function OrdersPage() {
  const { orders, failed } = useLoaderData()
  const revalidator = useRevalidator()
  const refreshing = revalidator.state === 'loading'
  const sorted = newestFirst(orders)
  const active = sorted.filter((order) => ACTIVE_STATUSES.includes(order.status))
  const past = sorted.filter((order) => !ACTIVE_STATUSES.includes(order.status))

  return (
    <div className="orders-page">
      <header className="page-header orders-header">
        <div>
          <h2>Order Tracking</h2>
          <p>Keep track of your current and past sushi orders.</p>
        </div>
        <button type="button" className="btn-secondary" onClick={() => revalidator.revalidate()} disabled={refreshing}>
          {refreshing ? 'Refreshing…' : 'Refresh'}
        </button>
      </header>

      {failed && (
        <p className="order-error orders-load-error" role="alert">
          Your orders could not be loaded. Check the output console for details.
        </p>
      )}

      {!failed && orders.length === 0 ? (
        <div className="orders-empty-card">
          <span className="orders-icon">📦</span>
          <h3>No Orders Yet</h3>
          <p>You have not placed any orders yet.</p>
          <Link to="/restaurants" className="btn-primary">
            Browse Restaurants
          </Link>
        </div>
      ) : (
        <>
          <OrderSection title="Active orders" orders={active} empty="You do not have any active orders right now." />
          <OrderSection title="Past orders" orders={past} empty="Completed and cancelled orders will show up here." />
        </>
      )}
    </div>
  )
}
