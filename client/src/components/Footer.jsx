const CURRENT_YEAR = new Date().getFullYear()

export default function Footer() {
  return (
    <footer className="footer">
      <div className="footer-content">
        <p>&copy; {CURRENT_YEAR} HealthyRolls </p>
      </div>
    </footer>
  )
}
