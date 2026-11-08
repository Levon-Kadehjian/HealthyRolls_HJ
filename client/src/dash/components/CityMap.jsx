import { useEffect, useRef } from 'react'
import { AttributionControl, MapContainer, Marker, Polyline, Popup, TileLayer, useMap, useMapEvents } from 'react-leaflet'
import L from 'leaflet'
import { LocateFixed } from 'lucide-react'
import 'leaflet/dist/leaflet.css'
import './CityMap.css'

const TILE_URL = 'https://tile.openstreetmap.org/{z}/{x}/{y}.png'
const ATTRIBUTION = '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'

const DEFAULT_CENTER = [45.5017, -73.5673] // Montreal coords

const icons = new Map()
function pinIcon(kind, status) {
  const key = `${kind}:${status ?? ''}`
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
    icons.set(
      key,
      L.divIcon({
        className: '',
        html: `<span class="map-pin map-pin-${kind}${status ? ` map-pin-${status.toLowerCase()}` : ''}"></span>`,
        iconSize: [18, 18],
        iconAnchor: [9, 9],
        popupAnchor: [0, -10],
      }),
    )
  }
  return icons.get(key)
}

function fitMap(map, bounds) {
  if (bounds.length === 1) {
    map.setView(bounds[0], 15)
  } else if (bounds.length > 1) {
    map.fitBounds(bounds, { padding: [40, 40] })
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

  return (
    <MapContainer className="city-map" center={fitTo[0] ?? DEFAULT_CENTER} zoom={13} scrollWheelZoom attributionControl={false}>
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
          icon={pinIcon(point.kind, point.status)}
          zIndexOffset={point.kind === 'home' ? 1000 : 0}
          eventHandlers={onSelect ? { click: () => onSelect(point.id) } : undefined}
        >
          {point.popup && <Popup>{point.popup}</Popup>}
        </Marker>
      ))}
      <OpenPopupOnce id={openPopupId} markers={markers} />
    </MapContainer>
  )
}
