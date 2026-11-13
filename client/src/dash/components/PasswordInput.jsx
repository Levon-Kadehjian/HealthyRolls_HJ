import { useState } from 'react'
import './PasswordInput.css'
import { Eye, EyeOff } from 'lucide-react'

export default function PasswordInput({
  id,
  name,
  value,
  onChange,
  placeholder,
  required = false,
  autoComplete,
  minLength,
  className = '',
}) {
  const [show, setShow] = useState(false)

  return (
    <div className="password-input-wrapper">
      <input
        id={id}
        name={name}
        type={show ? 'text' : 'password'}
        className={className}
        value={value}
        onChange={onChange}
        placeholder={placeholder}
        required={required}
        autoComplete={autoComplete}
        minLength={minLength}
      />
      <button
        type="button"
        className="password-toggle-btn"
        onClick={() => setShow((prev) => !prev)}
        aria-label={show ? 'Hide password' : 'Show password'}
        title={show ? 'Hide password' : 'Show password'}
      >
        {show ? (
          <Eye size={18} aria-hidden="true" />
        ) : (
          <EyeOff size={18} aria-hidden="true" />
        )}
      </button>
    </div>
  )
}
