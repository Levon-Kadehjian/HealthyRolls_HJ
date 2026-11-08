import { useLoaderData } from 'react-router-dom'
import CityMap from '../components/CityMap'
import './RestaurantsPage.css'

const STATUS_LABELS = {
  OPEN: 'Open',
  PAUSED: 'Paused',
  CLOSED: 'Closed',
  OFFLINE: 'Offline',
  IDLE: 'Idle',
  ASSIGNED: 'Assigned',
  CARRYING: 'Carrying',
}

export default function CourierMapPage() {
  const { courier, restaurants, couriers } = useLoaderData()

  const home = courier ? [courier.latitude, courier.longitude] : null
  const onDuty = couriers.filter((other) => other.status !== 'OFFLINE' && other.courierId !== courier?.courierId)

  const bounds = [
    ...restaurants.map((restaurant) => [restaurant.latitude, restaurant.longitude]),
    ...onDuty.map((other) => [other.latitude, other.longitude]),
    ...(home ? [home] : []),
  ]

  const points = [
    ...restaurants.map((restaurant) => ({
      id: restaurant.id,
      position: [restaurant.latitude, restaurant.longitude],
      kind: 'restaurant',
      status: restaurant.status,
      popup: (
        <div className="map-popup">
          <h4>{restaurant.name}</h4>
          <p>{STATUS_LABELS[restaurant.status] ?? restaurant.status}</p>
        </div>
      ),
    })),
    ...onDuty.map((other) => ({
      id: other.courierId,
      position: [other.latitude, other.longitude],
      kind: 'courier',
      status: other.status,
      popup: (
        <div className="map-popup">
          <h4>{other.name}</h4>
          <p>{STATUS_LABELS[other.status] ?? other.status}</p>
        </div>
      ),
    })),
    ...(home ? [{ id: 'home', position: home, kind: 'home', popup: <div className="map-popup"><h4>You are here</h4></div> }] : []),
  ]

  return (
    <div className="restaurants-page">
      <header className="page-header">
        <h2>Map</h2>
        <p>Restaurants and on-duty couriers.</p>
      </header>

      <section className="map-panel" aria-label="Courier map">
        <CityMap points={points} bounds={bounds} emptyMessage="Nothing to show on the map yet." />
      </section>
    </div>
  )
}