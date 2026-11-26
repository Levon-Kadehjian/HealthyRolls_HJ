import { useEffect, useState } from 'react'
import { listNotifications } from '../api/notifications'
import { useSession } from '../auth/useSession'
import './NotificationPanel.css'

const shortId = (id) => String(id ?? '').slice(0, 8)

const formatTime = (isoString) => {
  if (!isoString) return ''
  try {
    const date = new Date(isoString)
    return date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
  } catch {
    return ''
  }
}

export default function NotificationPanel() {
  const session = useSession()
  const [notifications, setNotifications] = useState([])

  const isEligible = session?.role === 'CUSTOMER' || session?.role === 'OPERATOR'

  useEffect(() => {
    if (!session?.userId || !isEligible) {
      return undefined
    }

    let active = true

    const fetchNotifications = async () => {
      try {
        const data = await listNotifications()
        if (active && Array.isArray(data)) {
          setNotifications(data)
        }
      } catch {
        // Ignore fetch errors during periodic polling
      }
    }

    fetchNotifications()
    const timer = setInterval(fetchNotifications, 5000)

    return () => {
      active = false
      clearInterval(timer)
    }
  }, [session?.userId, session?.role, isEligible])

  if (!isEligible) {
    return null
  }

  return (
    <section className="notification-panel" aria-label="Notifications">
      <div className="notification-panel-header">
        <h3 className="notification-panel-title">
          Notifications
          {notifications.length > 0 && (
            <span className="orders-count">{notifications.length}</span>
          )}
        </h3>
      </div>

      {notifications.length === 0 ? (
        <p className="notification-empty" role="status">
          No notifications are available.
        </p>
      ) : (
        <ul className="notification-list">
          {notifications.map((n) => {
            const isAlert = n.type === 'DISPATCH_ALERT'
            return (
              <li
                key={n.id}
                className={`notification-item ${isAlert ? 'notification-item-alert' : 'notification-item-status'}`}
              >
                <div className="notification-body">
                  <div className="notification-meta">
                    <span
                      className={`notification-type-badge ${isAlert ? 'notification-type-alert' : 'notification-type-status'}`}
                    >
                      {isAlert ? 'Dispatch Alert' : 'Order Update'}
                    </span>
                    {n.orderId && <span>Order #{shortId(n.orderId)}</span>}
                    {n.createdAt && (
                      <span className="notification-time">{formatTime(n.createdAt)}</span>
                    )}
                  </div>
                  <p className="notification-message">{n.message}</p>
                </div>
              </li>
            )
          })}
        </ul>
      )}
    </section>
  )
}
