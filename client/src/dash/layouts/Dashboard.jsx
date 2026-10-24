import { NavLink, Outlet, useLoaderData } from 'react-router-dom'
import './Dashboard.css'

const menuItems = [
  { to: 'orders', label: 'Orders', hiddenFor: ['RESTAURANT_STAFF'] },
  { to: 'kitchen', label: 'Kitchen', roles: ['RESTAURANT_STAFF'] },
  { to: 'account', label: 'Account' },
]

export default function Dashboard() {
  const { session } = useLoaderData()
  const visibleItems = menuItems.filter(
    (item) =>
      (!item.roles || item.roles.includes(session?.role)) && !item.hiddenFor?.includes(session?.role),
  )

  return (
    <div className="dashboard">
      <aside className="dashboard-sidebar">
        <nav className="dashboard-menu" aria-label="Dashboard">
          {visibleItems.map((item) => (
            <NavLink
              key={item.to}
              to={item.to}
              className={({ isActive }) =>
                `dashboard-link ${isActive ? 'dashboard-link-active' : ''}`
              }
            >
              {item.label}
            </NavLink>
          ))}
        </nav>
      </aside>

      <section className="dashboard-content">
        <Outlet />
      </section>
    </div>
  )
}
