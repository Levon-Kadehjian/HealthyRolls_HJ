import { useState, useEffect } from 'react'
import { Link, useFetcher, useFetchers, useLoaderData, useRevalidator } from 'react-router-dom'
import { useSession } from '../auth/useSession'
import {
  ACTIVE_STATUSES,
  canCancelOrder,
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

function StalledOrders({ stalled, orders }) {
  const fetcher = useFetcher()
  const busy = fetcher.state !== 'idle'
  const pendingOrderId = busy ? fetcher.formData?.get('orderId') : null
  const error = fetcher.data?.ok === false ? fetcher.data.error : null
  const result = fetcher.data?.ok ? fetcher.data.reassignment : null
  const restaurantName = (orderId) => orders.find((order) => order.orderId === orderId)?.restaurantName ?? 'Restaurant'

  return (
    <section className="orders-section">
      <h3>
        Stalled orders <span className="orders-count">{stalled.length}</span>
      </h3>
      {error && (
        <p className="order-error" role="alert">
          {error}
        </p>
      )}
      {result && (
        <p className="order-note" role="status">
          Order #{shortId(result.orderId)} released from {result.previousCourierName}.{' '}
          {result.queued
            ? 'No courier is available, so it is waiting in the dispatch queue.'
            : `Offered to ${result.offeredCourierName}.`}
        </p>
      )}
      {stalled.length === 0 ? (
        <p className="orders-section-empty">No ready order is held by an offline courier.</p>
      ) : (
        <ul className="order-list">
          {stalled.map((item) => (
            <li key={item.orderId} className="order-card">
              <div className="order-card-header">
                <div>
                  <h4>{restaurantName(item.orderId)}</h4>
                  <span className="order-card-id">Order #{shortId(item.orderId)}</span>
                </div>
              </div>
              <p className="order-note">Accepted by {item.courierName}, who is now offline.</p>
              <fetcher.Form method="post" className="order-actions">
                <input type="hidden" name="orderId" value={item.orderId} />
                <button type="submit" name="intent" value="REASSIGN_STALLED_ORDER" className="order-btn order-btn-neutral" disabled={busy}>
                  {pendingOrderId === item.orderId ? 'Reassigning…' : 'Reassign order'}
                </button>
              </fetcher.Form>
            </li>
          ))}
        </ul>
      )}
    </section>
  )
}

export default function OrdersPage() {
  const { orders, failed, stalled } = useLoaderData()
  const session = useSession()
  const isOperator = session?.role === 'OPERATOR'
  const revalidator = useRevalidator()
  const refreshing = revalidator.state === 'loading'
  const sorted = newestFirst(orders)
  const active = sorted.filter((order) => ACTIVE_STATUSES.includes(order.status))
  const preparationQueues = Object.values(
    active.reduce((queues, order) => {
      if (order.status !== 'ACCEPTED') return queues
  
      const id = order.restaurantId
  
      if (!queues[id]) {
        queues[id] = {
          restaurantId: id,
          name: order.restaurantName ?? 'Restaurant',
          count: 0,
        }
      }
  
      queues[id].count += 1
      return queues
    }, {}),
  )
  
  const busyRestaurants = preparationQueues.filter((restaurant) => restaurant.count >= 6,)

  const past = sorted.filter((order) => !ACTIVE_STATUSES.includes(order.status))
  const waiting = active.filter((order) => order.status === 'READY' && order.courierId == null,)
  
  const fetchers = useFetchers()
  const actionInProgress = fetchers.some(
    (fetcher) => fetcher.state !== 'idle',
  )
  
  useEffect(() => {
    if (!isOperator) return undefined
  
    const timer = setInterval(() => {
      if (revalidator.state === 'idle' && !actionInProgress) {
        revalidator.revalidate()
      }
    }, 5000)
  
    return () => clearInterval(timer)
  }, [isOperator, revalidator, actionInProgress])

  return (
    <div className="orders-page">
      <header className="page-header orders-header">
        <div>
          <h2>{isOperator ? 'All Orders' : 'Order Tracking'}</h2>
          <p>{isOperator ? 'View current and past orders across all restaurants.' : 'Keep track of your current and past sushi orders.'}</p>
        </div>
        <button type="button" className="btn-secondary" onClick={() => revalidator.revalidate()} disabled={refreshing}>
          {refreshing ? 'Refreshing…' : 'Refresh'}
        </button>
      </header>

      {failed && (
        <p className="order-error orders-load-error" role="alert">
          {isOperator ? 'Orders could not be loaded.' : 'Your orders could not be loaded.'} Check the output console for details.
        </p>
      )}
      {isOperator && !failed && busyRestaurants.length > 0 && (
        <section className="orders-section" aria-label="Preparation queue warnings">
          <h3>Long preparation queues</h3>

          <div role="status">
            {busyRestaurants.map((restaurant) => (
              <p
                key={restaurant.restaurantId}
                className="orders-section-empty"
              >
                <strong>{restaurant.name}</strong> has {restaurant.count} orders
                waiting for preparation. You can pause the restaurant from its
                management page to stop new orders while existing orders finish.
              </p>
            ))}
          </div>
        </section>
      )}
      {isOperator && !failed && (
        <section className="orders-section">
          <h3>
            Waiting for courier
            <span className="orders-count">{waiting.length}</span>
          </h3>

          <p className="orders-section-empty" role="status">
            {waiting.length > 0
              ? `${waiting.length} ready ${
                  waiting.length === 1 ? 'order is' : 'orders are'
                } waiting for an available courier. Assignment will retry automatically.`
              : 'No orders are waiting for an available courier.'}
          </p>

          {waiting.length > 0 && (
            <ul>
              {waiting.map((order) => (
                <li key={order.orderId}>
                  Order #{shortId(order.orderId)}
                  {' — '}
                  {order.restaurantName ?? 'Restaurant'}
                </li>
              ))}
            </ul>
          )}
        </section>
      )}
      {isOperator && stalled && <StalledOrders stalled={stalled} orders={orders} />}

      {!failed && orders.length === 0 ? (
        <div className="orders-empty-card">
          <span className="orders-icon">📦</span>
          <h3>No Orders Yet</h3>
          <p>{isOperator ? 'No orders have been placed yet.' : 'You have not placed any orders yet.'}</p>
          <Link to="/restaurants" className="btn-primary">
            Browse Restaurants
          </Link>
        </div>
      ) : (
        <>
          <OrderSection
            title="Active orders"
            orders={active}
            empty={isOperator ? 'There are no active orders right now.' : 'You do not have any active orders right now.'}
          />
          <OrderSection title="Past orders" orders={past} empty="Completed and cancelled orders will show up here." />
        </>
      )}
    </div>
  )
}
