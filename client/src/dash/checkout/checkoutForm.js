export const TEST_CARD = {
    cardName: 'Test Customer',
    cardNumber: '4242 4242 4242 4242',
    expiry: '12/30',
    cvc: '123',
}

export function formatCardNumber(value) {
    return value
        .replace(/\D/g, '')
        .slice(0, 16)
        .replace(/(\d{4})(?=\d)/g, '$1 ')
}

export function formatExpiry(value) {
    const digits = value.replace(/\D/g, '').slice(0, 4)
    return digits.length > 2 ? `${digits.slice(0, 2)}/${digits.slice(2)}` : digits
}

export function validateCheckout({ address, cardName, cardNumber, expiry, cvc }) {
    const errors = {}

    if (address.trim().length < 5) {
        errors.address = 'Enter your delivery address.'
    }
    if (!cardName.trim()) {
        errors.cardName = 'Enter the name on the card.'
    }
    if (cardNumber.replace(/\D/g, '').length !== 16) {
        errors.cardNumber = 'Card number must be 16 digits.'
    }

    const match = /^(\d{2})\/(\d{2})$/.exec(expiry)
    if (!match) {
        errors.expiry = 'Use the format MM/YY.'
    } else {
        const month = Number(match[1])
        const year = 2000 + Number(match[2])
        if (month < 1 || month > 12) {
            errors.expiry = 'Month must be between 01 and 12.'
        } else if (new Date(year, month, 1) <= new Date()) {
            errors.expiry = 'This card has expired.'
        }
    }

    if (!/^\d{3,4}$/.test(cvc)) {
        errors.cvc = 'CVC must be 3 or 4 digits.'
    }

    return errors
}

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