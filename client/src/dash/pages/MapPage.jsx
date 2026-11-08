import { useState } from 'react'
import { Link, useLoaderData, useSearchParams } from 'react-router-dom'
import L from 'leaflet'
import CityMap from '../components/CityMap'
import RestaurantPanel from '../components/RestaurantPanel'
import { BAND_LABELS, BANDS, queueBand } from '../components/queueBand'
import { useSession } from '../auth/useSession'
import './MapPage.css'

const STATUS_LABELS = { OPEN: 'Open', PAUSED: 'Paused', CLOSED: 'Closed' }

const formatDistance = (meters) => (meters < 1000 ? `${Math.round(meters)} m` : `${(meters / 1000).toFixed(1)} km`)

export default function MapPage() {
  const { restaurants, orders, couriers, courier: me } = useLoaderData()
  const session = useSession()
  const [shown, setShown] = useState(new Set(BANDS))
  const [params] = useSearchParams()
  const [selectedId, setSelectedId] = useState(params.get('restaurant'))

  const home = me ? [me.latitude, me.longitude] : session?.latitude != null ? [session.latitude, session.longitude] : null
  const visible = restaurants.filter((restaurant) => shown.has(queueBand(restaurant)))
  const selected = visible.find((restaurant) => restaurant.id === selectedId)
  const distance = home && selected ? L.latLng(home).distanceTo([selected.latitude, selected.longitude]) : null

  const onDuty = couriers.filter((courier) => courier.status !== 'OFFLINE' && courier.courierId !== me?.courierId)

  const bounds = [
    ...restaurants.map((r) => [r.latitude, r.longitude]),
    ...onDuty.map((courier) => [courier.latitude, courier.longitude]),
    ...(home ? [home] : []),
  ]

  const points = [
    ...visible.map((restaurant) => ({
      id: restaurant.id,
      position: [restaurant.latitude, restaurant.longitude],
      kind: 'restaurant',
      status: queueBand(restaurant),
      popup: (
        <div className="map-popup">
          <h4>{restaurant.name}</h4>
          <p><strong>Queued orders: {restaurant.queuedCount}</strong></p>
          {restaurant.status !== 'OPEN' && <p>{STATUS_LABELS[restaurant.status] ?? restaurant.status}</p>}
          {session?.role !== 'COURIER' && <Link to={`/restaurants/${restaurant.id}`}>View menu →</Link>}
        </div>
      ),
    })),
    ...onDuty.map((courier) => ({
      id: courier.courierId,
      position: [courier.latitude, courier.longitude],
      kind: 'courier',
      status: courier.status,
      popup: (
        <div className="map-popup">
          <h4>{courier.name}</h4>
          <p>{courier.status.charAt(0) + courier.status.slice(1).toLowerCase()}</p>
        </div>
      ),
    })),
    ...(home ? [{ id: 'home', position: home, kind: 'home', popup: <div className="map-popup"><h4>You are here</h4></div> }] : []),
  ]
  const lines = home && selected ? [{ id: 'route', positions: [home, [selected.latitude, selected.longitude]] }] : []

  const toggle = (band) =>
    setShown((current) => {
      const next = new Set(current)
      if (!next.delete(band)) next.add(band)
      return next
    })

  return (
    <div className="map-page">
      <header className="page-header">
        <h2>City map</h2>
        <p>See every restaurant and how busy its kitchen is.</p>
      </header>

      <section className="map-panel" aria-label="Restaurant map">
        <div className="map-panel-head">
          <div>
            <h3>Around you</h3>
            <p>Tap a pin to see how far it is.</p>
          </div>
          <div className="map-filters" role="group" aria-label="Filter restaurants by queue length">
            {BANDS.map((band) => (
              <button
                key={band}
                type="button"
                className={`map-filter map-filter-${band}`}
                aria-pressed={shown.has(band)}
                onClick={() => toggle(band)}
              >
                <span className="map-filter-dot" aria-hidden="true" />
                {BAND_LABELS[band]}
                <span className="map-filter-count">{restaurants.filter((r) => queueBand(r) === band).length}</span>
              </button>
            ))}
          </div>
        </div>

        <div className="map-layout">
          <CityMap
            points={points}
            lines={lines}
            bounds={bounds}
            onSelect={setSelectedId}
            onMapClick={() => setSelectedId(null)}
            openPopupId={params.get('restaurant')}
            emptyMessage="No restaurants have been loaded into the system."
          />

          {selected ? (
            <RestaurantPanel
              restaurant={selected}
              orders={orders}
              session={session}
              distanceLabel={distance != null ? formatDistance(distance) : null}
              onClose={() => setSelectedId(null)}
            />
          ) : (
            <aside className="restaurant-panel restaurant-panel-empty" aria-live="polite">
              <p className="map-selection-hint">
                {home ? 'Select a restaurant on the map.' : 'Your location is unavailable, so distances are hidden.'}
              </p>
            </aside>
          )}
        </div>
      </section>

      {session?.role !== 'COURIER' && <Link to="/restaurants" className="map-list-link">Browse the restaurant list →</Link>}
    </div>
  )
}
