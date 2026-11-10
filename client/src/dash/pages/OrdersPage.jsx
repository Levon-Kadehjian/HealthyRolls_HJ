import { useState } from 'react'
import { Link, useFetcher, useLoaderData, useRevalidator } from 'react-router-dom'
import { useSession } from '../auth/useSession'
import {
  ACTIVE_STATUSES,
  canCancelOrder,
  canRefundOrder,
  formatDateTime,
  formatMoney,
  newestFirst,
  statusLabel,
} from '../orders/orderHistory'
import './OrdersPage.css'

const shortId = (id) => String(id ?? '').slice(0, 8)

function CancelOrder({ order, fetcher }) {
  const session = useSession()
  const [confirming, setConfirming] = useState(false)
  const busy = fetcher.state !== 'idle'

  if (!canCancelOrder(session?.role, order.status)) {
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
          {busy ? 'Cancelling…' : 'Yes, cancel'
        }</button>
      </div>
    </fetcher.Form>
  )
}

function RefundOrder({ order, fetcher }) {
  const session = useSession()
  const [confirming, setConfirming] = useState(false)
  const busy = fetcher.state !== 'idle'

  if (!canRefundOrder(session?.role, order.status)) {
    return null
  }

  if (!confirming) {
    return (
      <div className="order-actions">
        <button type="button" className="order-btn order-btn-danger" onClick={() => setConfirming(true)}>
          Refund order
        </button>
      </div>
    )
  }

  return (
    <fetcher.Form method="post" className="order-confirm">
      <input type="hidden" name="orderId" value={order.orderId} />
      <input type="hidden" name="status" value={order.status} />
      <p className="order-confirm-text">
        <strong>Refund this order?</strong>
        <span>The customer will receive a full refund. This cannot be undone.</span>
      </p>
      <div className="order-confirm-buttons">
        <button type="button" className="order-btn order-btn-neutral" onClick={() => setConfirming(false)} disabled={busy}>
          Keep order
        </button>
        <button type="submit" name="intent" value="REFUND" className="order-btn order-btn-danger" disabled={busy}>
          {busy ? 'Refunding…' : 'Yes, refund'
        }</button>
      </div>
    </fetcher.Form>
  )
}

function OrderCard({ order, isCourier }) {
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
            {!isCourier && line.price !== null && <span>{formatMoney(line.price * line.quantity)}</span>}
          </li>
        ))}
      </ul>

      {!isCourier && (order.total !== null ? (
        <div className="order-total">
          <span>{order.billedTotal != null ? 'Billed Total' : 'Total'}</span>
          <span>{formatMoney(order.total)}</span>
        </div>
      ) : (
        order.subtotal !== null && (
          <div className="order-total">
            <span>Subtotal</span>
            <span>{formatMoney(order.subtotal)}</span>
          </div>
        )
      ))}

      {!isCourier && order.refunded && (
        <p className="order-note" style={{ color: '#c0392b', fontWeight: 600 }}>
          Refunded: {order.refundAmount != null ? formatMoney(order.refundAmount) : 'Full refund issued'}
        </p>
      )}

      {order.rejectionReason && <p className="order-note">Restaurant note: “{order.rejectionReason}”</p>}

      {error && (
        <p className="order-error" role="alert">
          {error}
        </p>
      )}

      <CancelOrder order={order} fetcher={fetcher} />
      <RefundOrder order={order} fetcher={fetcher} />
    </li>
  )
}

function OrderSection({ title, orders, empty, isCourier }) {
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
            <OrderCard key={order.orderId} order={order} isCourier={isCourier} />
          ))}
        </ul>
      )}
    </section>
  )
}

export default function OrdersPage() {
  const { orders, failed } = useLoaderData()
  const session = useSession()
  const isOperator = session?.role === 'OPERATOR'
  const isCourier = session?.role === 'COURIER'
  const revalidator = useRevalidator()
  const refreshing = revalidator.state === 'loading'
  const sorted = newestFirst(orders)
  const active = sorted.filter((order) => ACTIVE_STATUSES.includes(order.status))
  const past = sorted.filter((order) => !ACTIVE_STATUSES.includes(order.status))

  const pageTitle = isOperator ? 'All Orders' : isCourier ? 'Delivery History' : 'Order Tracking'
  const pageSubtitle = isOperator
    ? 'View current and past orders across all restaurants.'
    : isCourier
      ? 'View your assigned and completed deliveries.'
      : 'Keep track of your current and past sushi orders.'

  const activeTitle = isCourier ? 'Active deliveries' : 'Active orders'
  const pastTitle = isCourier ? 'Past deliveries' : 'Past orders'
  const activeEmpty = isOperator
    ? 'There are no active orders right now.'
    : isCourier
      ? 'You do not have any active deliveries right now.'
      : 'You do not have any active orders right now.'
  const pastEmpty = isCourier
    ? 'Completed deliveries will show up here.'
    : 'Completed and cancelled orders will show up here.'

  return (
    <div className="orders-page">
      <header className="page-header orders-header">
        <div>
          <h2>{pageTitle}</h2>
          <p>{pageSubtitle}</p>
        </div>
        <button type="button" className="btn-secondary" onClick={() => revalidator.revalidate()} disabled={refreshing}>
          {refreshing ? 'Refreshing…' : 'Refresh'}
        </button>
      </header>

      {failed && (
        <p className="order-error orders-load-error" role="alert">
          {isOperator ? 'Orders could not be loaded.' : isCourier ? 'Deliveries could not be loaded.' : 'Your orders could not be loaded.'} Check the output console for details.
        </p>
      )}

      {!failed && orders.length === 0 ? (
        <div className="orders-empty-card">
          <span className="orders-icon">📦</span>
          <h3>{isCourier ? 'No Deliveries Yet' : 'No Orders Yet'}</h3>
          <p>
            {isOperator
              ? 'No orders have been placed yet.'
              : isCourier
                ? 'You have not been assigned any deliveries yet.'
                : 'You have not placed any orders yet.'}
          </p>
          {!isCourier && (
            <Link to="/restaurants" className="btn-primary">
              Browse Restaurants
            </Link>
          )}
        </div>
      ) : (
        <>
          <OrderSection
            title={activeTitle}
            orders={active}
            empty={activeEmpty}
            isCourier={isCourier}
          />
          <OrderSection
            title={pastTitle}
            orders={past}
            empty={pastEmpty}
            isCourier={isCourier}
          />
        </>
      )}
    </div>
  )
}
