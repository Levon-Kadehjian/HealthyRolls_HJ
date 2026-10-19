const MAX_ENTRIES = 1000
const STORAGE_KEY = 'healthyrolls.console'

function parseEntries(raw) {
  try {
    const stored = JSON.parse(raw)
    if (!Array.isArray(stored)) {
      return []
    }
    return stored.map((entry) => ({ ...entry, time: new Date(entry.time) }))
  } catch {
    return []
  }
}

function readStoredEntries() {
  try {
    return localStorage.getItem(STORAGE_KEY)
  } catch {
    return null
  }
}

function loadEntries() {
  return parseEntries(readStoredEntries()).map((entry) =>
    entry.status === 'pending' ? { ...entry, status: 'error', result: 'interrupted by page reload' } : entry,
  )
}

function saveEntries() {
  try {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(entries))
  } catch {
    return
  }
}

function highestId(list) {
  return list.reduce((max, entry) => Math.max(max, entry.id), 0)
}

let entries = loadEntries()
let nextId = highestId(entries) + 1
const listeners = new Set()
const loggedErrors = new WeakSet()

function notify() {
  listeners.forEach((listener) => listener())
}

function setEntries(next) {
  entries = next
  saveEntries()
  notify()
}

window.addEventListener('storage', (event) => {
  if (event.key !== STORAGE_KEY && event.key !== null) {
    return
  }
  entries = parseEntries(event.newValue ?? readStoredEntries())
  nextId = Math.max(nextId, highestId(entries) + 1)
  notify()
})

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
