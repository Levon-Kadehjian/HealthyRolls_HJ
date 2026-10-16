import { useLoaderData, Link } from 'react-router-dom'

export default function MenuPage() {
  const items = useLoaderData()

  return (
    <div className="menu-page">
      <header className="page-header">
        <h2>Our Menu</h2>
        <p>Explore our handcrafted rolls, made fresh on order.</p>
      </header>

      <div className="menu-grid">
        {items.map((item) => (
          <div key={item.id} className="menu-card">
            <div className="menu-card-top">
              <h3>{item.name}</h3>
              <span className="menu-tag">{item.tag}</span>
            </div>
            <p className="menu-desc">{item.description}</p>
            <div className="menu-card-footer">
              <span className="menu-price">{item.price}</span>
              <button type="button" className="btn-add">
                Add to Cart
              </button>
            </div>
          </div>
        ))}
      </div>

      <div className="menu-back">
        <Link to="/" className="btn-secondary">
          &larr; Back to Home
        </Link>
      </div>
    </div>
  )
}
