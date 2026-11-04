import { useEffect } from 'react'
import { MapContainer, Marker, Polyline, Popup, TileLayer, useMap } from 'react-leaflet'
import L from 'leaflet'
import 'leaflet/dist/leaflet.css'
import './CityMap.css'

const TILE_URL = 'https://tile.openstreetmap.org/{z}/{x}/{y}.png'
const ATTRIBUTION = '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'

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

function FitBounds({ bounds }) {
  const map = useMap()
  const key = JSON.stringify(bounds)
  useEffect(() => {
    const bounds = JSON.parse(key)
    if (bounds.length === 1) {
      map.setView(bounds[0], 15)
    } else if (bounds.length > 1) {
      map.fitBounds(bounds, { padding: [40, 40] })
    }
  }, [map, key])
  return null
}


export default function CityMap({ points = [], lines = [], bounds, onSelect, emptyMessage = 'Nothing to show on the map yet.' }) {
  const fitTo = bounds ?? points.map((point) => point.position)

  if (fitTo.length === 0) {
    return <div className="city-map city-map-empty">{emptyMessage}</div>
  }

  return (
    <MapContainer className="city-map" center={fitTo[0]} zoom={13} scrollWheelZoom>
      <TileLayer url={TILE_URL} attribution={ATTRIBUTION} />
      <FitBounds bounds={fitTo} />
      {lines.map((line) => (
        <Polyline key={line.id} positions={line.positions} pathOptions={{ className: 'map-line' }} />
      ))}
      {points.map((point) => (
        <Marker
          key={point.id}
          position={point.position}
          icon={pinIcon(point.kind, point.status)}
          zIndexOffset={point.kind === 'home' ? 1000 : 0}
          eventHandlers={onSelect ? { click: () => onSelect(point.id) } : undefined}
        >
          {point.popup && <Popup>{point.popup}</Popup>}
        </Marker>
      ))}
    </MapContainer>
  )
}
