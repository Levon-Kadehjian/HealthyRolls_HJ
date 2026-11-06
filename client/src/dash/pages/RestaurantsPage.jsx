import { useState } from 'react'
import { Link, useLoaderData } from 'react-router-dom'
import L from 'leaflet'
import RestaurantCard from '../components/RestaurantCard'
import CityMap from '../components/CityMap'
import { BAND_LABELS, BANDS, queueBand } from '../components/queueBand'
import { useSession } from '../auth/useSession'
import './RestaurantsPage.css'

const STATUS_LABELS = { OPEN: 'Open', PAUSED: 'Paused', CLOSED: 'Closed' }

const formatDistance = (meters) => (meters < 1000 ? `${Math.round(meters)} m` : `${(meters / 1000).toFixed(1)} km`)

export default function RestaurantsPage() {
  const restaurants = useLoaderData()
  const session = useSession()
  const [shown, setShown] = useState(new Set(BANDS))
  const [selectedId, setSelectedId] = useState(null)

  const home = session?.latitude != null ? [session.latitude, session.longitude] : null
  const visible = restaurants.filter((restaurant) => shown.has(queueBand(restaurant)))
  const selected = visible.find((restaurant) => restaurant.id === selectedId)
  const distance = home && selected ? L.latLng(home).distanceTo([selected.latitude, selected.longitude]) : null

  const bounds = [...restaurants.map((r) => [r.latitude, r.longitude]), ...(home ? [home] : [])]

  const points = [
    ...visible.map((restaurant) => ({
      id: restaurant.id,
      position: [restaurant.latitude, restaurant.longitude],
      kind: 'restaurant',
      status: queueBand(restaurant),
      popup: (
        <div className="map-popup">
          <h4>{restaurant.name}</h4>
          <p>
            {restaurant.queuedCount} queued
            {restaurant.status !== 'OPEN' && ` · ${STATUS_LABELS[restaurant.status] ?? restaurant.status}`}
          </p>
          <Link to={`/restaurants/${restaurant.id}`}>View menu →</Link>
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
    <div className="restaurants-page">
      <header className="page-header">
        <h2>Restaurants</h2>
        <p>Find a spot near you and browse its menu.</p>
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

        <CityMap
          points={points}
          lines={lines}
          bounds={bounds}
          onSelect={setSelectedId}
          emptyMessage="No restaurants have been loaded into the system."
        />

        <div className="map-selection" aria-live="polite">
          {selected ? (
            <>
              <span className={`map-selection-dot map-filter-${queueBand(selected)}`} aria-hidden="true" />
              <strong>{selected.name}</strong>
              <span className="map-selection-distance">{selected.queuedCount} queued</span>
              {distance != null && <span className="map-selection-distance">{formatDistance(distance)} away</span>}
              <Link className="map-selection-link" to={`/restaurants/${selected.id}`}>View menu →</Link>
            </>
          ) : (
            <span className="map-selection-hint">
              {home ? 'Select a restaurant on the map.' : 'Your location is unavailable, so distances are hidden.'}
            </span>
          )}
        </div>
      </section>

      <div className="restaurants-list-head">
        <h3>All restaurants</h3>
        <span className="restaurants-list-count">{visible.length} of {restaurants.length}</span>
      </div>

      {restaurants.length === 0 ? (
        <div className="orders-empty-card">
          <h3>No Restaurants Yet</h3>
          <p>No restaurants have been loaded into the system.</p>
        </div>
      ) : visible.length === 0 ? (
        <div className="orders-empty-card">
          <h3>Nothing matches</h3>
          <p>Turn a queue filter back on to see restaurants.</p>
        </div>
      ) : (
        <div className="restaurant-card-grid">
          {visible.map((restaurant) => (
            <RestaurantCard key={restaurant.id} restaurant={restaurant} />
          ))}
        </div>
      )}
    </div>
  )
}
