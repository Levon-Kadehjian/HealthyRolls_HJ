import { Link, useLocation } from 'react-router-dom'
import { Terminal } from 'lucide-react'
import './ConsoleButton.css'

export default function ConsoleButton() {
  const { pathname } = useLocation()

  if (pathname === '/console') {
    return null
  }

  return (
    <Link to="/console" className="console-fab" aria-label="Open output console" title="Output console">
      <Terminal className="console-fab-icon" aria-hidden="true" />
    </Link>
  )
}
