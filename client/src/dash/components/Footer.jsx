import { FaFacebook, FaInstagram, FaXTwitter } from 'react-icons/fa6'
import './Footer.css'

export default function Footer() {
  return (
    <footer className="sushi-footer">
      <div className="sushi-footer-container">
        {/* Column 1: Company */}
        <div className="footer-col">
          <h4 className="footer-heading">COMPANY</h4>
          <ul className="footer-list">
            <li className="footer-text-item">HealthyRolls Inc.</li>
          </ul>
        </div>

        {/* Column 2: Legal */}
        <div className="footer-col">
          <h4 className="footer-heading">LEGAL</h4>
          <ul className="footer-list">
            <li className="footer-text-item">Terms &amp; condition</li>
            <li className="footer-text-item">Privacy Policy</li>
          </ul>
        </div>

        {/* Column 3: Support */}
        <div className="footer-col">
          <h4 className="footer-heading">SUPPORT</h4>
          <ul className="footer-list">
            <li className="footer-text-item">Support Us</li>
            <li className="footer-text-item">Question Support</li>
          </ul>
        </div>

        {/* Column 4: Follow Us (Plain Static Icons) */}
        <div className="footer-col">
          <h4 className="footer-heading">FOLLOW US</h4>
          <div className="footer-social-icons">
            {/* Facebook Icon */}
            <div className="social-icon-badge" aria-label="Facebook">
              <FaFacebook size={15} aria-hidden="true" />
            </div>

            {/* X / Twitter Icon */}
            <div className="social-icon-badge" aria-label="X">
              <FaXTwitter size={16} aria-hidden="true" />
            </div>

            {/* Instagram Icon */}
            <div className="social-icon-badge" aria-label="Instagram">
              <FaInstagram size={16} aria-hidden="true" />
            </div>
          </div>
        </div>
      </div>
    </footer>
  )
}
