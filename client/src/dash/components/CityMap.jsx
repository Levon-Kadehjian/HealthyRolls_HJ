import { useEffect, useRef, useState } from 'react'
import { AttributionControl, MapContainer, Marker, Polyline, Popup, TileLayer, Tooltip, useMap, useMapEvents } from 'react-leaflet'
import L from 'leaflet'
import { renderToStaticMarkup } from 'react-dom/server'
import { LocateFixed, Navigation2, Package, Pause } from 'lucide-react'
import 'leaflet/dist/leaflet.css'
import './CityMap.css'

const TILE_URL = 'https://tile.openstreetmap.org/{z}/{x}/{y}.png'
const ATTRIBUTION = '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'

const DEFAULT_CENTER = [45.5017, -73.5673] // Montreal coords

const COURIER_BADGES = {
  IDLE: () => <Pause size={13} fill="currentColor" />,
  ASSIGNED: (heading) => <Navigation2 size={13} fill="currentColor" style={{ transform: `rotate(${heading ?? 0}deg)` }} />,
  CARRYING: () => <Package size={13} />,
}

const icons = new Map()
function pinIcon(kind, status, heading) {
  const key = `${kind}:${status ?? ''}:${heading ?? ''}`
  if (kind === 'home' && !icons.has(key)) {
    icons.set(
      key,
      L.divIcon({
        className: '',
        html: '<span class="map-you"><span class="map-you-pulse"></span><span class="map-you-dot"></span></span>',
        iconSize: [20, 20],
        iconAnchor: [10, 10],
        popupAnchor: [0, -12],
      }),
    )
  }
  if (!icons.has(key)) {
    const Badge = kind === 'courier' ? COURIER_BADGES[status] : null
    const badge = Badge ? renderToStaticMarkup(Badge(heading)) : ''
    const size = badge ? 22 : 18
    icons.set(
      key,
      L.divIcon({
        className: '',
        html: `<span class="map-pin map-pin-${kind}${status ? ` map-pin-${status.toLowerCase()}` : ''}">${badge}</span>`,
        iconSize: [size, size],
        iconAnchor: [size / 2, size / 2],
        popupAnchor: [0, -size / 2 - 1],
      }),
    )
  }
  return icons.get(key)
}

function fitMap(map, bounds) {
  if (bounds.length === 1) {
    map.setView(bounds[0], 15)
  } else if (bounds.length > 1) {
    map.fitBounds(bounds, { padding: [32, 32] })
  } else {
    map.setView(DEFAULT_CENTER, 13)
  }
}

function FitBounds({ bounds }) {
  const map = useMap()
  const key = JSON.stringify(bounds)
  useEffect(() => {
    const bounds = JSON.parse(key)
    if (bounds.length > 0) fitMap(map, bounds)
  }, [map, key])
  return null
}

function RecenterButton({ bounds }) {
  const map = useMap()
  const ref = useRef(null)
  useEffect(() => {
    if (ref.current) L.DomEvent.disableClickPropagation(ref.current)
  }, [])
  return (
    <div className="leaflet-bottom leaflet-left">
      <div className="leaflet-control leaflet-bar city-map-recenter" ref={ref}>
        <button type="button" aria-label="Re-center map" title="Re-center map" onClick={() => fitMap(map, bounds)}>
          <LocateFixed size={18} aria-hidden="true" />
        </button>
      </div>
    </div>
  )
}


function MapClick({ onClick }) {
  useMapEvents({ click: onClick })
  return null
}

function OpenPopupOnce({ id, markers }) {
  useEffect(() => {
    if (id) markers.current.get(id)?.openPopup()
  }, [])
  return null
}

export default function CityMap({ points = [], lines = [], bounds, onSelect, onMapClick, openPopupId, emptyMessage = 'Nothing to show on the map yet.' }) {
  const fitTo = bounds ?? points.map((point) => point.position)
  const markers = useRef(new Map())
  const [openId, setOpenId] = useState(null)
  const select = (id) => {
    markers.current.get(id)?.openPopup()
    onSelect?.(id)
  }

  return (
    <MapContainer className="city-map" center={fitTo[0] ?? DEFAULT_CENTER} zoom={13} zoomSnap={0.1} scrollWheelZoom attributionControl={false}>
      <TileLayer url={TILE_URL} attribution={ATTRIBUTION} />
      <AttributionControl prefix={false} />
      <FitBounds bounds={fitTo} />
      <RecenterButton bounds={fitTo} />
      {onMapClick && <MapClick onClick={onMapClick} />}
      {fitTo.length === 0 && <div className="city-map-empty">{emptyMessage}</div>}
      {lines.map((line) => (
        <Polyline key={line.id} positions={line.positions} pathOptions={{ className: 'map-line' }} />
      ))}
      {points.map((point) => (
        <Marker
          key={point.id}
          ref={(marker) => (marker ? markers.current.set(point.id, marker) : markers.current.delete(point.id))}
          position={point.position}
          icon={pinIcon(point.kind, point.status, point.heading)}
          zIndexOffset={point.kind === 'home' ? 1000 : 0}
          eventHandlers={{
            click: () => onSelect?.(point.id),
            popupopen: () => setOpenId(point.id),
            popupclose: () => setOpenId((id) => (id === point.id ? null : id)),
          }}
        >
          {point.popup && <Popup>{point.popup}</Popup>}
          {point.label && openId !== point.id && (
            <Tooltip
              permanent
              interactive
              opacity={1}
              direction="top"
              offset={[0, -12]}
              className="map-label"
              eventHandlers={{ click: () => select(point.id) }}
            >
              {point.label}
            </Tooltip>
          )}
        </Marker>
      ))}
      <OpenPopupOnce id={openPopupId} markers={markers} />
    </MapContainer>
  )
}
