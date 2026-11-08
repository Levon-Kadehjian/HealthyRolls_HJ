import { Link, useFetcher } from 'react-router-dom'
import OrderActions from './OrderActions'
import RestaurantStatusControls from './RestaurantStatusControls'
import { restaurantActions } from './restaurantActions'
import { BAND_LABELS, queueBand } from './queueBand'
import { canCancelOrder, statusLabel } from '../orders/orderHistory'

const shortId = (id) => String(id ?? '').slice(0, 8)
const QUEUE_STATUSES = ['PLACED', 'ACCEPTED', 'PREPARING', 'READY']
const itemCount = (order) => (order.lines ?? []).reduce((total, line) => total + line.quantity, 0)

function CancelButton({ order, role }) {
  const fetcher = useFetcher()
  if (!canCancelOrder(role, order.status)) return null
  const busy = fetcher.state !== 'idle'
  const error = fetcher.data?.ok === false ? fetcher.data.error : null
  return (
    <fetcher.Form method="post">
      <input type="hidden" name="orderId" value={order.orderId} />
      <input type="hidden" name="status" value={order.status} />
      {error && <p className="kitchen-action-error" role="alert">{error}</p>}
      <button type="submit" name="intent" value="CANCEL" className="btn-secondary kitchen-btn" disabled={busy}>
        {busy ? 'Cancelling…' : 'Cancel order'}
      </button>
    </fetcher.Form>
  )
}

function OrderRow({ order, actions, role }) {
  return (
    <li className="panel-order">
      <div className="panel-order-head">
        <strong>#{shortId(order.orderId)}</strong>
        <span className={`kitchen-history-status kitchen-history-status-${order.status.toLowerCase()}`}>
          {statusLabel(order.status)}
        </span>
        <span className="panel-order-items">{itemCount(order)} items</span>
      </div>
      {actions.handleOrders && <OrderActions order={order} />}
      {(actions.cancelOrders || actions.seeOwnOrders) && <CancelButton order={order} role={role} />}
    </li>
  )
}

export default function RestaurantPanel({ restaurant, orders, session, distanceLabel, onClose }) {
  const actions = restaurantActions(session, restaurant)
  const band = queueBand(restaurant)
  const restaurantOrders = orders.filter((order) => order.restaurantId === restaurant.id)
  const queue = restaurantOrders
    .filter((order) => QUEUE_STATUSES.includes(order.status))
    .sort((a, b) => QUEUE_STATUSES.indexOf(a.status) - QUEUE_STATUSES.indexOf(b.status)
      || new Date(a.acceptedAt ?? 0) - new Date(b.acceptedAt ?? 0))
  const showOrders = actions.seeQueue || actions.seeOwnOrders
  const visibleOrders = actions.seeQueue ? queue : restaurantOrders

  return (
    <aside className="restaurant-panel" aria-label={`${restaurant.name} details`}>
      <header className="restaurant-panel-head">
        <span className={`map-selection-dot map-filter-${band}`} aria-hidden="true" />
        <h4>{restaurant.name}</h4>
        <button type="button" className="restaurant-panel-close" aria-label="Close details" onClick={onClose}>×</button>
      </header>
      <div className="restaurant-panel-tags">
        <span className="map-selection-distance">{BAND_LABELS[band]}</span>
        {restaurant.status !== 'OPEN' && <span className="map-selection-distance">{statusLabel(restaurant.status)}</span>}
        {distanceLabel && <span className="map-selection-distance">{distanceLabel} away</span>}
      </div>

      <dl className="restaurant-panel-metrics">
        <div><dt>Queued</dt><dd>{restaurant.queuedCount}</dd></div>
        <div><dt>Preparing</dt><dd>{restaurant.preparingCount}</dd></div>
        <div><dt>Ready</dt><dd>{restaurant.readyCount}</dd></div>
      </dl>

      <div className="restaurant-panel-actions">
        {actions.viewMenu && <Link className="btn-secondary" to={`/restaurants/${restaurant.id}`}>View menu</Link>}
        {actions.placeOrder && (
          actions.placeOrderBlockedReason ? (
            <button type="button" className="btn-primary" disabled title={actions.placeOrderBlockedReason}>
              Place order
            </button>
          ) : (
            <Link className="btn-primary" to={`/restaurants/${restaurant.id}`}>Place order</Link>
          )
        )}
        {actions.placeOrder && actions.placeOrderBlockedReason && (
          <span className="panel-note">{actions.placeOrderBlockedReason}</span>
        )}
      </div>

      {actions.statusControls && <RestaurantStatusControls restaurant={restaurant} session={session} />}

      {showOrders && (
        <section className="restaurant-panel-orders" aria-label={actions.seeQueue ? 'Order queue' : 'Your orders here'}>
          <h5>{actions.seeQueue ? 'Order queue' : 'Your orders here'}</h5>
          {visibleOrders.length === 0 ? (
            <p className="panel-note">{actions.seeQueue ? 'Nothing in the queue.' : 'You have no orders at this restaurant.'}</p>
          ) : (
            <ul className="panel-orders">
              {visibleOrders.map((order) => (
                <OrderRow key={order.orderId} order={order} actions={actions} role={session.role} />
              ))}
            </ul>
          )}
        </section>
      )}
    </aside>
  )
}
