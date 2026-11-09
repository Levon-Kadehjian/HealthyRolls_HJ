import { Link, useLoaderData, useRevalidator } from 'react-router-dom'
import OrderActions from '../components/OrderActions'
import { useSession } from '../auth/useSession'
import { formatDateTime, formatMoney, newestFirst, statusLabel } from '../orders/orderHistory'
import './RestaurantOrdersPage.css'

const COLUMNS = [
  { status: 'PLACED', title: 'Incoming', empty: 'No new orders. The counter is quiet.' },
  { status: 'ACCEPTED', title: 'Queued', empty: 'Nothing waiting on the belt.' },
  { status: 'PREPARING', title: 'Preparing', empty: 'Nothing on the cutting board.' },
  { status: 'READY', title: 'Ready for pickup', empty: 'No bento waiting for pickup.' },
]

const shortId = (id) => String(id ?? '').slice(0, 8)

const formatTime = (value) =>
  value ? new Date(value).toLocaleTimeString('en-CA', { hour: '2-digit', minute: '2-digit' }) : null

function sortForColumn(status, orders) {
  if (status !== 'ACCEPTED') {
    return orders
  }
  return [...orders].sort((a, b) => new Date(a.acceptedAt ?? 0) - new Date(b.acceptedAt ?? 0))
}

function OrderLines({ lines }) {
  return (
    <ul className="kitchen-lines">
      {lines.map((line) => (
        <li key={line.menuItemId}>
          <span className="kitchen-line-qty">{line.quantity}×</span>
          <span>{line.name ?? `Item ${shortId(line.menuItemId)}`}</span>
        </li>
      ))}
    </ul>
  )
}

function OrderCard({ order, position }) {
  const acceptedAt = formatTime(order.acceptedAt)

  return (
    <li className={`kitchen-card kitchen-card-${order.status.toLowerCase()}`}>
      <div className="kitchen-card-header">
        <h4>Order #{shortId(order.orderId)}</h4>
        {position !== undefined && <span className="kitchen-badge">#{position} in queue</span>}
      </div>
      {acceptedAt && <span className="kitchen-card-meta">Accepted {acceptedAt}</span>}
      <OrderLines lines={order.lines ?? []} />
      {order.status === 'ACCEPTED' && (
        <p className="kitchen-card-note">Starts automatically when the line is free.</p>
      )}
      {order.status === 'READY' && (<p className="kitchen-card-note">Awaiting pickup.</p>)}
      <OrderActions order={order} />
    </li>
  )
}

export default function RestaurantOrdersPage() {
  const { orders, restaurantName, failed } = useLoaderData()
  const session = useSession()
  const revalidator = useRevalidator()
  const refreshing = revalidator.state === 'loading'
  const history = newestFirst(orders)

  return (
    <div className="kitchen-page">
      <header className="page-header kitchen-header">
        <div>
          <h2>Kitchen Orders{restaurantName ? ` · ${restaurantName}` : ''}</h2>
          <p>Take orders at the counter, roll them in turn, and plate them for pickup.</p>
        </div>
        <div className="kitchen-header-actions">
        <button
          type="button"
          className="btn-secondary"
          onClick={() => revalidator.revalidate()}
          disabled={refreshing}
        >
          {refreshing ? 'Refreshing…' : 'Refresh'}
        </button>
        {session?.restaurantId && <Link className="btn-secondary" to={`/restaurants/${session.restaurantId}`}>Manage restaurant</Link>}
        </div>
      </header>

      {failed && (
        <p className="kitchen-load-error" role="alert">
          Orders could not be loaded. Check the output console for details.
        </p>
      )}

      <div className="kitchen-board">
        {COLUMNS.map((column) => {
          const columnOrders = sortForColumn(
            column.status,
            orders.filter((order) => order.status === column.status),
          )
          return (
            <section
              key={column.status}
              className={`kitchen-column kitchen-column-${column.status.toLowerCase()}`}
              aria-labelledby={`col-${column.status}`}
            >
              <h3 id={`col-${column.status}`} className="kitchen-column-title">
                <span className="kitchen-column-name">{column.title}</span>
                <span className="kitchen-count" aria-label={`${columnOrders.length} orders`}>
                  {columnOrders.length}
                </span>
              </h3>
              {columnOrders.length === 0 ? (
                <div className="kitchen-column-empty">
                  <p>{column.empty}</p>
                </div>
              ) : (
                <ul className="kitchen-cards">
                  {columnOrders.map((order, index) => (
                    <OrderCard
                      key={order.orderId}
                      order={order}
                      position={column.status === 'ACCEPTED' ? index + 1 : undefined}
                    />
                  ))}
                </ul>
              )}
            </section>
          )
        })}
      </div>

      <section className="kitchen-history" aria-labelledby="kitchen-history-title">
        <h3 id="kitchen-history-title" className="kitchen-history-title">
          Order history <span className="kitchen-count">{history.length}</span>
        </h3>
        {history.length === 0 ? (
          <p className="kitchen-history-empty">No orders have been placed at this restaurant yet.</p>
        ) : (
          <div className="kitchen-history-scroll">
            <table className="kitchen-history-table">
              <thead>
                <tr>
                  <th scope="col">Order</th>
                  <th scope="col">Placed</th>
                  <th scope="col">Items</th>
                  <th scope="col">Total</th>
                  <th scope="col">Status</th>
                </tr>
              </thead>
              <tbody>
                {history.map((order) => (
                  <tr key={order.orderId}>
                    <td className="kitchen-history-id">#{shortId(order.orderId)}</td>
                    <td>{formatDateTime(order.placedAt) ?? '—'}</td>
                    <td>
                      {order.lines
                        .map((line) => `${line.quantity}× ${line.name ?? `Item ${shortId(line.menuItemId)}`}`)
                        .join(', ')}
                    </td>
                    <td>{order.total !== null ? formatMoney(order.total) : '—'}</td>
                    <td>
                      <span className={`kitchen-history-status kitchen-history-status-${order.status.toLowerCase()}`}>
                        {statusLabel(order.status)}
                      </span>
                      {order.rejectionReason && (
                        <span className="kitchen-history-reason">“{order.rejectionReason}”</span>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </section>
    </div>
  )
}
