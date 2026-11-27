import { useEffect, useRef, useState } from 'react'
import { useLocation } from 'react-router-dom'
import { Bell } from 'lucide-react'
import { listNotifications, markNotificationRead, markNotificationsRead } from '../api/notifications'
import { useSession } from '../auth/useSession'
import { describeError, recordCommand, runCommand } from '../console/consoleStore'
import './NotificationPanel.css'

const shortId = (id) => String(id ?? '').slice(0, 8)

const formatTime = (isoString) => {
  if (!isoString) return ''
  try {
    return new Date(isoString).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
  } catch {
    return ''
  }
}

export default function NotificationPanel() {
  const session = useSession()
  const location = useLocation()
  const [notifications, setNotifications] = useState([])
  const [open, setOpen] = useState(false)
  const [marking, setMarking] = useState(false)
  const menuRef = useRef(null)
  const listRef = useRef(null)

  const isEligible = session?.role === 'CUSTOMER' || session?.role === 'OPERATOR'
  const unread = notifications.filter((n) => !n.read)

  useEffect(() => {
    if (!session?.userId || !isEligible) {
      return undefined
    }

    let active = true
    let failing = false

    const fetchNotifications = async () => {
      try {
        const data = await listNotifications()
        if (active && Array.isArray(data)) {
          setNotifications(data)
        }
        if (active && failing) {
          failing = false
          recordCommand('LIST_NOTIFICATIONS', '', 'ok', 'Notifications loaded again.')
        }
      } catch (error) {
        if (active && !failing) {
          failing = true
          recordCommand('LIST_NOTIFICATIONS', '', 'error', await describeError(error))
        }
      }
    }

    fetchNotifications()
    const timer = setInterval(fetchNotifications, 5000)

    return () => {
      active = false
      clearInterval(timer)
    }
  }, [session?.userId, session?.role, isEligible])

  useEffect(() => {
    setOpen(false)
  }, [location.pathname])

  useEffect(() => {
    if (open && listRef.current) {
      listRef.current.scrollTop = listRef.current.scrollHeight
    }
  }, [open])

  useEffect(() => {
    if (!open) {
      return undefined
    }
    const handleClick = (event) => {
      if (menuRef.current && !menuRef.current.contains(event.target)) {
        setOpen(false)
      }
    }
    const handleKey = (event) => {
      if (event.key === 'Escape') {
        setOpen(false)
      }
    }
    document.addEventListener('mousedown', handleClick)
    document.addEventListener('keydown', handleKey)
    return () => {
      document.removeEventListener('mousedown', handleClick)
      document.removeEventListener('keydown', handleKey)
    }
  }, [open])

  async function markAllRead() {
    setMarking(true)
    const ids = new Set(unread.map((n) => n.id))
    try {
      await runCommand(
        'MARK_ALL_NOTIFICATIONS_READ',
        `${ids.size} notifications`,
        () => markNotificationsRead([...ids]),
        () => `${ids.size} notifications marked as read.`,
      )
      setNotifications((current) =>
        current.map((n) => (ids.has(n.id) ? { ...n, read: true } : n)),
      )
    } catch {
      return
    } finally {
      setMarking(false)
    }
  }

  async function markOneRead(notification) {
    if (notification.read) {
      return
    }
    try {
      await runCommand(
        'MARK_NOTIFICATION_READ',
        `#${shortId(notification.id)}`,
        () => markNotificationRead(notification.id),
        () => 'Notification marked as read.',
      )
      setNotifications((current) =>
        current.map((n) => (n.id === notification.id ? { ...n, read: true } : n)),
      )
    } catch {
      return
    }
  }

  if (!isEligible) {
    return null
  }

  return (
    <div className="nav-notifications" ref={menuRef}>
      <button
        type="button"
        className="nav-icon-link nav-bell"
        aria-label={`Notifications, ${unread.length} unread`}
        aria-haspopup="menu"
        aria-expanded={open}
        onClick={() => setOpen((value) => !value)}
      >
        <Bell className="nav-icon" strokeWidth={1.9} aria-hidden="true" />
        {unread.length > 0 && (
          <span className="nav-bell-badge" aria-hidden="true">
            {unread.length > 99 ? '99+' : unread.length}
          </span>
        )}
      </button>

      {open && (
        <div className="nav-menu notification-menu" role="menu" aria-label="Notifications">
          <div className="notification-menu-header">Notifications</div>

          {notifications.length === 0 ? (
            <p className="notification-empty" role="status">
              No notifications are available.
            </p>
          ) : (
            <ul className="notification-list" ref={listRef}>
              {notifications.toReversed().map((n) => {
                const isAlert = n.type === 'DISPATCH_ALERT'
                return (
                  <li key={n.id}>
                    <button
                      type="button"
                      className={`notification-item ${isAlert ? 'notification-item-alert' : 'notification-item-status'} ${n.read ? 'notification-item-read' : ''}`}
                      onClick={() => markOneRead(n)}
                      title={n.read ? undefined : 'Mark as read'}
                    >
                      <span className="notification-meta">
                        <span
                          className={`notification-type-badge ${isAlert ? 'notification-type-alert' : 'notification-type-status'}`}
                        >
                          {isAlert ? 'Dispatch Alert' : 'Order Update'}
                        </span>
                        {n.orderId && <span>Order #{shortId(n.orderId)}</span>}
                        {n.createdAt && (
                          <span className="notification-time">{formatTime(n.createdAt)}</span>
                        )}
                      </span>
                      <span className="notification-message">{n.message}</span>
                    </button>
                  </li>
                )
              })}
            </ul>
          )}

          <div className="nav-menu-divider" role="separator" />
          <button
            type="button"
            className="nav-menu-item notification-mark-all"
            role="menuitem"
            onClick={markAllRead}
            disabled={marking || unread.length === 0}
          >
            {marking ? 'Marking…' : 'Mark all as read'}
          </button>
        </div>
      )}
    </div>
  )
}
