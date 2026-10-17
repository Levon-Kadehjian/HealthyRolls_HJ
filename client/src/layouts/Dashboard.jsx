import { NavLink, Outlet } from 'react-router-dom'
import './Dashboard.css'

const menuItems = [
  { to: 'orders', label: 'Orders' },
  { to: 'account', label: 'Account' },
]

export default function Dashboard() {
  return (
    <div className="dashboard">
      <aside className="dashboard-sidebar">
        <nav className="dashboard-menu" aria-label="Dashboard">
          {menuItems.map((item) => (
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
