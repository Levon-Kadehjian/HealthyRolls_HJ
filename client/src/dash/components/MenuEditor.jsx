import { useEffect, useState } from 'react'
import { useFetcher } from 'react-router-dom'
import './MenuEditor.css'

export default function MenuEditor({ restaurant }) {
  const [editing, setEditing] = useState(null)
  const [name, setName] = useState('')
  const [price, setPrice] = useState('')
  const fetcher = useFetcher()
  const busy = fetcher.state !== 'idle'
  const hasError = fetcher.data?.ok === false
  const message = busy ? '' : hasError ? fetcher.data.error : fetcher.data?.message

  useEffect(() => {
    if (fetcher.data?.ok && fetcher.data.intent !== 'CHANGE_MENU_AVAILABILITY') {
      setEditing(null)
      setName('')
      setPrice('')
    }
  }, [fetcher.data])

  return (
    <section className="menu-editor" aria-label="Manage restaurant menu">
      <h3>Manage menu</h3>
      <fetcher.Form method="post">
        <input type="hidden" name="intent" value={editing ? 'EDIT_MENU_ITEM' : 'ADD_MENU_ITEM'} />
        {editing && <input type="hidden" name="itemId" value={editing} />}
        <fieldset disabled={busy}>
          <legend>{editing ? 'Edit item' : 'Add item'}</legend>
          <label>Name<input name="name" required maxLength={255} value={name} onChange={(event) => setName(event.target.value)} /></label>
          <label>Price ($)<input name="price" required type="number" min="0" step="0.01" value={price} onChange={(event) => setPrice(event.target.value)} /></label>
          <div className="menu-editor-actions">
            <button className="btn-primary" type="submit">{busy ? 'Saving…' : editing ? 'Save changes' : 'Add item'}</button>
            {editing && <button className="btn-secondary" type="button" onClick={() => { setEditing(null); setName(''); setPrice('') }}>Cancel edit</button>}
          </div>
        </fieldset>
      </fetcher.Form>
      {message && <p className={`restaurant-banner ${hasError ? 'restaurant-banner-warning' : 'restaurant-banner-success'}`} role={hasError ? 'alert' : 'status'}>{message}</p>}
      <ul>
        {restaurant.menu.map((item) => (
          <li key={item.itemId}>
            <div className="menu-editor-item">
              <span className="menu-editor-item-name">{item.name}</span>
              <span className={`restaurant-status ${item.status === 'AVAILABLE' ? 'restaurant-status-open' : 'restaurant-status-closed'}`}>{item.status === 'AVAILABLE' ? 'Available' : 'Sold out'}</span>
            </div>
            <div className="menu-editor-actions">
            <button className="btn-secondary" type="button" disabled={busy} onClick={() => { setEditing(item.itemId); setName(item.name); setPrice(String(item.price)) }}>Edit</button>
            <fetcher.Form method="post">
              <input type="hidden" name="intent" value="CHANGE_MENU_AVAILABILITY" />
              <input type="hidden" name="itemId" value={item.itemId} />
              <input type="hidden" name="status" value={item.status === 'AVAILABLE' ? 'SOLD_OUT' : 'AVAILABLE'} />
              <button className="btn-secondary" type="submit" disabled={busy}>
                {item.status === 'AVAILABLE' ? 'Mark sold out' : 'Make available'}
              </button>
            </fetcher.Form>
            </div>
          </li>
        ))}
      </ul>
    </section>
  )
}
