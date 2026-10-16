import { useRouteError, isRouteErrorResponse, Link } from 'react-router-dom'

export default function ErrorPage() {
  const error = useRouteError()

  let title = 'Unexpected Error'
  let message = 'Something went wrong while rendering this page.'

  if (isRouteErrorResponse(error)) {
    if (error.status === 404) {
      title = '404 - Page Not Found'
      message = "Sorry, the page you are looking for does not exist."
    } else {
      title = `${error.status} - ${error.statusText || 'Error'}`
      message = error.data?.message || message
    }
  } else if (error instanceof Error) {
    message = error.message
  }

  return (
    <div className="error-page">
      <div className="error-card">
        <span className="error-icon">⚠️</span>
        <h1>{title}</h1>
        <p>{message}</p>
        <Link to="/" className="error-home-btn">
          Back to Home
        </Link>
      </div>
    </div>
  )
}
