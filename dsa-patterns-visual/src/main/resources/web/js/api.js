// Thin client for the JSON + SSE API.

export async function fetchCatalog() {
    const res = await fetch('/api/catalog');
    if (!res.ok) throw new Error(`catalog: HTTP ${res.status}`);
    return res.json();
}

export async function fetchHealth() {
    const res = await fetch('/api/health', { cache: 'no-store' });
    if (!res.ok) throw new Error(`health: HTTP ${res.status}`);
    return res.json();
}

/**
 * Opens a live trace over Server-Sent Events.
 * handlers: onSession(info) onFrame(frame) onDone(summary) onFail(message)
 * Returns { close(), id } – `id` is filled in once the server announces it.
 */
export function openStream({ algo, params, delay, mode }, h) {
    const qs = new URLSearchParams({ algo, params: JSON.stringify(params), delay: String(delay), mode });
    const es = new EventSource(`/api/stream?${qs}`);
    const handle = { id: null, closed: false, close() { handle.closed = true; es.close(); } };
    const json = e => JSON.parse(e.data);

    es.addEventListener('session', e => { const s = json(e); handle.id = s.id; h.onSession?.(s); });
    es.addEventListener('frame', e => h.onFrame?.(json(e)));
    es.addEventListener('done', e => { handle.close(); h.onDone?.(json(e)); });
    es.addEventListener('fail', e => { handle.close(); h.onFail?.(json(e).message); });
    es.onerror = () => {
        // EventSource would silently reconnect and restart the trace – never allow that.
        if (handle.closed) return;
        handle.close();
        h.onFail?.('connection to the server was lost');
    };
    return handle;
}

/** Fire-and-forget remote control: pause | resume | step | stop | delay. */
export function control(sessionId, cmd, value) {
    if (!sessionId) return Promise.resolve(false);
    const qs = new URLSearchParams({ session: sessionId, cmd });
    if (value !== undefined) qs.set('value', String(value));
    return fetch(`/api/control?${qs}`, { method: 'POST', keepalive: true })
        .then(r => r.ok)
        .catch(() => false);
}

/** Best-effort stop when the tab closes. */
export function beaconStop(sessionId) {
    if (sessionId) navigator.sendBeacon(`/api/control?session=${sessionId}&cmd=stop`);
}
