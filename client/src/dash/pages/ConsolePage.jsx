import { useEffect, useRef, useState, useSyncExternalStore } from 'react'
import { clearConsole, getEntries, subscribe } from '../console/consoleStore'
import './ConsolePage.css'

const STATUS_LABELS = { pending: '…', ok: 'OK', error: 'ERR' }

export default function ConsolePage() {
  const entries = useSyncExternalStore(subscribe, getEntries)
  const [errorsOnly, setErrorsOnly] = useState(false)
  const listRef = useRef(null)
  const stickToBottom = useRef(true)

  const errors = entries.filter((entry) => entry.status === 'error')
  const visible = errorsOnly ? errors : entries

  useEffect(() => {
    const list = listRef.current
    if (list && stickToBottom.current) {
      list.scrollTop = list.scrollHeight
    }
  }, [visible])

  function handleScroll() {
    const list = listRef.current
    stickToBottom.current = list.scrollHeight - list.scrollTop - list.clientHeight < 24
  }

  return (
    <div className="console-page">
      <header className="page-header">
        <h2>Output Console</h2>
        <p>Every command and every error from this session.</p>
      </header>

      <section className="console-panel" aria-label="Output console">
        <div className="console-toolbar">
          <span className="console-prompt">&gt;_</span>
          <span className="console-stats">
            {entries.length} {entries.length === 1 ? 'entry' : 'entries'} · {errors.length}{' '}
            {errors.length === 1 ? 'error' : 'errors'}
          </span>
          <div className="console-actions">
            <button
              type="button"
              className={`console-btn ${errorsOnly ? 'console-btn-active' : ''}`}
              onClick={() => setErrorsOnly((value) => !value)}
              aria-pressed={errorsOnly}
            >
              Errors only
            </button>
            <button type="button" className="console-btn" onClick={clearConsole} disabled={entries.length === 0}>
              Clear
            </button>
          </div>
        </div>

        <ol className="console-list" ref={listRef} onScroll={handleScroll} aria-live="polite">
          {visible.length === 0 ? (
            <li className="console-empty">
              {errorsOnly ? 'No errors logged.' : 'No commands yet.'}
            </li>
          ) : (
            visible.map((entry) => (
              <li key={entry.id} className={`console-line console-${entry.status}`}>
                <time className="console-time" dateTime={entry.time.toISOString()}>
                  {entry.time.toLocaleTimeString('en-CA', { hour12: false })}
                </time>
                <span className="console-status">{STATUS_LABELS[entry.status]}</span>
                <span className="console-body">
                  <span className="console-command">{entry.command}</span>
                  {entry.details && <span className="console-details"> {entry.details}</span>}
                  {entry.result && <span className="console-result"> → {entry.result}</span>}
                </span>
              </li>
            ))
          )}
        </ol>
      </section>
    </div>
  )
}
