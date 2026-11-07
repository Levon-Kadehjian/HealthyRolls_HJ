function formatClock(date) {
    const hours = date.getHours() % 12 || 12
    const minutes = String(date.getMinutes()).padStart(2, '0')
    return `${hours}:${minutes}`
}

function formatPeriod(date) {
    return date.getHours() < 12 ? 'a.m.' : 'p.m.'
}

export function estimateDeliveryWindow() {
    const now = Date.now()
    const earliest = new Date(now + 30 * 60 * 1000)
    const latest = new Date(now + 45 * 60 * 1000)

    if (formatPeriod(earliest) === formatPeriod(latest)) {
        return `${formatClock(earliest)} – ${formatClock(latest)} ${formatPeriod(latest)}`
    }
    return `${formatClock(earliest)} ${formatPeriod(earliest)} – ${formatClock(latest)} ${formatPeriod(latest)}`
}