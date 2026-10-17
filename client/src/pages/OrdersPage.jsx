import { Link } from 'react-router-dom'

export default function OrdersPage() {
  return (
    <div className="orders-page">
      <header className="page-header">
        <h2>Order Tracking</h2>
        <p>Keep track of your current and past sushi orders.</p>
      </header>

      <div className="orders-empty-card">
        <span className="orders-icon">📦</span>
        <h3>No Active Orders</h3>
        <p>You do not have any active orders right now.</p>
        <Link to="/" className="btn-primary">
          Back to Home
        </Link>
      </div>
    </div>
  )
}
