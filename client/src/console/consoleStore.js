const MAX_ENTRIES = 200

let entries = []
let nextId = 1
const listeners = new Set()
const loggedErrors = new WeakSet()

function setEntries(next) {
  entries = next
  listeners.forEach((listener) => listener())
}

export function subscribe(listener) {
  listeners.add(listener)
  return () => listeners.delete(listener)
}

export const getEntries = () => entries

export const clearConsole = () => setEntries([])

function startCommand(command, details = '') {
  const id = nextId++
  setEntries([...entries.slice(-(MAX_ENTRIES - 1)), { id, time: new Date(), command, details, status: 'pending', result: '' }])
  return id
}

function finishCommand(id, status, result) {
  setEntries(entries.map((entry) => (entry.id === id ? { ...entry, status, result } : entry)))
}

export function recordCommand(command, details, status, result) {
  finishCommand(startCommand(command, details), status, result)
}

export async function describeError(error) {
  if (error instanceof Response) {
    const body = await error.clone().text().catch(() => '')
    let detail
    try {
      const parsed = JSON.parse(body)
      detail = parsed.detail || parsed.message || parsed.title || body
    } catch {
      detail = body
    }
    return `${error.status} ${detail || error.statusText}`.trim()
  }
  return error instanceof Error ? error.message || error.name : String(error)
}

export async function runCommand(command, details, action, describeResult) {
  const id = startCommand(command, details)
  try {
    const result = await action()
    finishCommand(id, 'ok', describeResult(result))
    return result
  } catch (error) {
    finishCommand(id, 'error', await describeError(error))
    if (Object(error) === error) {
      loggedErrors.add(error)
    }
    throw error
  }
}

export function captureGlobalErrors() {
  window.addEventListener('error', (event) => {
    if (!loggedErrors.has(event.error)) {
      recordCommand('UNCAUGHT_ERROR', '', 'error', event.message)
    }
  })
  window.addEventListener('unhandledrejection', (event) => {
    if (!loggedErrors.has(event.reason)) {
      describeError(event.reason).then((message) => recordCommand('UNHANDLED_ERROR', '', 'error', message))
    }
  })
}
