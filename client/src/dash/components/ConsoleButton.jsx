import { Link, useLocation } from 'react-router-dom'
import './ConsoleButton.css'

export default function ConsoleButton() {
  const { pathname } = useLocation()

  if (pathname === '/console') {
    return null
  }

  return (
    <Link to="/console" className="console-fab" aria-label="Open output console" title="Output console">
      <svg
        className="console-fab-icon"
        viewBox="0 0 24 24"
        fill="none"
        stroke="currentColor"
        strokeWidth="2"
        strokeLinecap="round"
        strokeLinejoin="round"
        aria-hidden="true"
      >
        <path d="m4 17 6-6-6-6" />
        <path d="M12 19h8" />
      </svg>
    </Link>
  )
}
