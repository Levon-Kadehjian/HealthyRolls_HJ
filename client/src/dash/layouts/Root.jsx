import { Outlet, useNavigation } from 'react-router-dom'
import Navbar from '../components/Navbar'
import Footer from '../components/Footer'
import ConsoleButton from '../components/ConsoleButton'

export default function Root() {
  const navigation = useNavigation()
  const isLoading = navigation.state === 'loading'

  return (
    <div className="app-shell">
      <Navbar />
      {isLoading && (
        <div className="loading-bar-container" role="status" aria-label="Loading page">
          <div className="loading-bar" />
        </div>
      )}
      <main className={`main-content ${isLoading ? 'main-content-loading' : ''}`}>
        <Outlet />
      </main>
      <Footer />
      <ConsoleButton />
    </div>
  )
}
