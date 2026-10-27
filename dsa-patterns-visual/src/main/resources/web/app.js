// DSA // PATTERNS // VISUAL – UI controller.
//
// Playback model
//   frames[]   every frame received so far (the whole trace once `done`)
//   cursor     the frame currently on screen
//   following  true  → new frames from the server are shown as they arrive (live)
//   playing    user intent: auto-advance (live at the head, timer-driven in the buffer)
// Invariant: whenever the user is NOT following the head, the server computation is
// paused. Play resumes it only once the replay has caught up with the buffer.

import { fetchCatalog, fetchHealth, openStream, control, beaconStop } from './js/api.js';
import { buildForm } from './js/inputs.js';
import { createRenderer } from './js/renderers.js';

const $ = id => document.getElementById(id);
const el = {
    canvas: $('stage'), caption: $('caption'), title: $('stageTitle'), tags: $('stageTags'),
    list: $('algoList'), search: $('algoSearch'), formHost: $('formHost'), preset: $('presetSel'),
    pseudo: $('pseudo'), blurb: $('blurb'), cTime: $('cTime'), cSpace: $('cSpace'), insight: $('insight'),
    first: $('btnFirst'), back: $('btnBack'), play: $('btnPlay'), fwd: $('btnFwd'), last: $('btnLast'),
    scrub: $('scrub'), counter: $('frameCounter'), speed: $('speed'), speedLabel: $('speedLabel'),
    log: $('logList'), logCount: $('logCount'), run: $('btnRun'), random: $('btnRandom'), reset: $('btnReset'),
    status: $('statusChip'), fConn: $('fConn'), fFrames: $('fFrames'), fSession: $('fSession'),
    toast: $('toast'), help: $('help'),
};

const DELAYS = [1500, 1000, 700, 500, 350, 200, 120, 60, 30, 10];   // ms per frame, speed 1..10
const renderer = createRenderer(el.canvas);

const S = {
    catalog: [], meta: null, form: null,
    frames: [], cursor: -1,
    stream: null, done: false,
    playing: false, following: false, serverPaused: false, pendingStep: false,
    ctlQueue: [], replayTimer: null,
    delay: 500, fail: null,
};

// ------------------------------------------------------------------ utilities
function toast(msg, kind = 'info') {
    el.toast.textContent = msg;
    el.toast.className = `show ${kind}`;
    clearTimeout(toast.t);
    toast.t = setTimeout(() => { el.toast.className = ''; }, kind === 'err' ? 5200 : 2400);
}
const store = {
    get: (k, d) => { try { return localStorage.getItem(k) ?? d; } catch { return d; } },
    set: (k, v) => { try { localStorage.setItem(k, v); } catch { /* private mode */ } },
};
const b64e = o => btoa(String.fromCharCode(...new TextEncoder().encode(JSON.stringify(o))))
    .replace(/\+/g, '-').replace(/\//g, '_').replace(/=+$/, '');
const b64d = s => JSON.parse(new TextDecoder().decode(Uint8Array.from(
    atob(s.replace(/-/g, '+').replace(/_/g, '/')), c => c.charCodeAt(0))));

// ----------------------------------------------------------- server control
function srv(cmd, value) {
    const s = S.stream;
    if (!s || s.closed) return;
    if (cmd === 'pause') { if (S.serverPaused) return; S.serverPaused = true; }
    else if (cmd === 'resume') { if (!S.serverPaused) return; S.serverPaused = false; }
    else if (cmd === 'step') S.serverPaused = true;
    if (s.id) control(s.id, cmd, value); else S.ctlQueue.push([cmd, value]);
}

function endStream() {
    stopReplay();
    const s = S.stream;
    if (s && !s.closed) { if (s.id) control(s.id, 'stop'); s.close(); }
    S.stream = null;
}

function resetTrace() {
    S.frames = []; S.cursor = -1; S.done = false; S.fail = null;
    S.playing = false; S.following = false; S.pendingStep = false; S.serverPaused = false; S.ctlQueue = [];
    el.log.replaceChildren(); logBuf.length = 0;
    el.caption.innerHTML = '&nbsp;';
    highlightLine(null);
    updateTransport();
}

// ---------------------------------------------------------------------- run
function run(mode = 'play') {
    const r = S.form.read();
    if (!r.ok) { toast(r.errors[0], 'err'); return; }
    endStream();
    resetTrace();
    S.playing = mode === 'play';
    S.following = true;
    S.serverPaused = mode === 'step';
    renderer.message('computing…', '#00f0ff');
    history.replaceState(null, '', `#/${S.meta.id}?p=${b64e(r.values)}`);

    S.stream = openStream({ algo: S.meta.id, params: r.values, delay: S.delay, mode }, {
        onSession(info) {
            el.fSession.textContent = `session ${info.id.slice(0, 8)}`;
            S.ctlQueue.splice(0).forEach(([c, v]) => control(info.id, c, v));
        },
        onFrame(frame) {
            S.frames.push(frame);
            queueLog(frame, S.frames.length - 1);
            if (S.following || S.pendingStep) { S.pendingStep = false; goTo(S.frames.length - 1); }
            else updateTransport();
        },
        onDone(sum) {
            S.done = true;
            S.pendingStep = false;
            S.stream = null;
            if (sum.truncated) toast(`Trace truncated at ${sum.frames} frames – try a smaller input`, 'err');
            if (S.following) S.playing = false;
            updateTransport();
        },
        onFail(message) {
            S.stream = null; S.playing = false; S.fail = message;
            if (!S.frames.length) renderer.message(message);
            toast(message, 'err');
            updateTransport();
        },
    });
    updateTransport();
}

// ------------------------------------------------------------------ playback
function stopReplay() { clearTimeout(S.replayTimer); S.replayTimer = null; }

function startReplay() {
    stopReplay();
    S.playing = true;
    const tick = () => {
        if (!S.playing) return;
        if (S.cursor < S.frames.length - 1) {
            goTo(S.cursor + 1);
            S.replayTimer = setTimeout(tick, S.delay);
        } else {
            S.replayTimer = null;
            if (S.stream && !S.done) { S.following = true; srv('resume'); }   // caught up → go live
            else S.playing = false;
            updateTransport();
        }
    };
    S.replayTimer = setTimeout(tick, S.delay);
    updateTransport();
}

function play() {
    if (!S.frames.length && !S.stream) return run('play');
    S.playing = true;
    if (S.cursor < S.frames.length - 1) { S.following = false; return startReplay(); }
    if (S.stream && !S.done) { S.following = true; srv('resume'); return updateTransport(); }
    goTo(0); S.following = false; startReplay();                               // finished → replay from the top
}

function pause() {
    S.playing = false; S.following = false; S.pendingStep = false;
    stopReplay();
    srv('pause');
    updateTransport();
}

function stepForward() {
    if (!S.frames.length && !S.stream) return run('step');
    const last = S.frames.length - 1;
    pause();
    if (S.cursor < last) goTo(S.cursor + 1);
    else if (S.stream && !S.done) { S.pendingStep = true; srv('step'); }
}

function stepBack() { pause(); if (S.cursor > 0) goTo(S.cursor - 1); }
function toFirst() { pause(); if (S.frames.length) goTo(0); }
function toLast() { pause(); if (S.frames.length) goTo(S.frames.length - 1); }
function scrubTo(i) { pause(); goTo(i); }

function goTo(i) {
    if (i < 0 || i >= S.frames.length) return;
    flushLog();
    S.cursor = i;
    const f = S.frames[i];
    renderer.draw(f);
    el.caption.textContent = f.note || '\u00a0';
    highlightLine(f.line);
    markLog(i);
    updateTransport();
}

// -------------------------------------------------------------------- UI sync
function updateTransport() {
    const n = S.frames.length, live = !!S.stream && !S.done;
    el.scrub.max = Math.max(0, n - 1);
    el.scrub.value = Math.max(0, S.cursor);
    el.scrub.disabled = n < 2;
    el.scrub.style.setProperty('--pos', n > 1 ? `${(S.cursor / (n - 1)) * 100}%` : '0%');
    el.counter.textContent = n ? `${S.cursor + 1} / ${n}${live ? '+' : ''}` : '0 / 0';
    el.fFrames.textContent = `frames ${n}${live ? ' · streaming' : ''}`;

    el.first.disabled = el.back.disabled = S.cursor <= 0;
    el.last.disabled = S.cursor >= n - 1;
    el.fwd.disabled = S.cursor >= n - 1 && !live && n > 0;
    el.play.textContent = S.playing ? '⏸' : '▶';
    el.play.classList.toggle('active', S.playing);

    let label, cls;
    if (S.fail) [label, cls] = ['ERROR', 'err'];
    else if (live && !S.serverPaused) [label, cls] = ['STREAMING', 'live'];
    else if (live) [label, cls] = ['SERVER PAUSED', 'paused'];
    else if (S.done) [label, cls] = ['TRACE COMPLETE', 'done'];
    else [label, cls] = ['IDLE', 'idle'];
    el.status.className = `status ${cls}`;
    el.status.lastElementChild.textContent = label;
}

function highlightLine(line) {
    for (const li of el.pseudo.children) li.classList.remove('on');
    if (line !== null && line !== undefined) el.pseudo.children[line]?.classList.add('on');
}

// ------------------------------------------------------------------ trace log
const logBuf = [];
let logRaf = 0;
function queueLog(frame, i) {
    logBuf.push([frame, i]);
    if (!logRaf) logRaf = requestAnimationFrame(flushLog);
}
function flushLog() {
    logRaf = 0;
    if (!logBuf.length) return;
    const frag = document.createDocumentFragment();
    for (const [f, i] of logBuf.splice(0)) {
        const li = document.createElement('li');
        li.dataset.i = i;
        li.innerHTML = '<b></b><span></span>';
        li.firstChild.textContent = String(i + 1).padStart(3, '0');
        li.lastChild.textContent = f.note || '';
        frag.append(li);
    }
    el.log.append(frag);
    el.logCount.textContent = `· ${el.log.children.length}`;
}
let markedLog = null;
function markLog(i) {
    markedLog?.classList.remove('cur');
    markedLog = el.log.children[i] || null;
    if (!markedLog) return;
    markedLog.classList.add('cur');
    const box = el.log;
    const top = markedLog.offsetTop, bottom = top + markedLog.offsetHeight;
    if (top < box.scrollTop || bottom > box.scrollTop + box.clientHeight)
        box.scrollTop = top - box.clientHeight / 2 + markedLog.offsetHeight / 2;
}
el.log.addEventListener('click', e => {
    const li = e.target.closest('li');
    if (li) scrubTo(Number(li.dataset.i));
});

// -------------------------------------------------------- algorithm selection
function renderSidebar(filter = '') {
    const q = filter.trim().toLowerCase();
    const groups = new Map();
    for (const m of S.catalog) {
        if (q && !(`${m.name} ${m.pattern}`.toLowerCase().includes(q))) continue;
        if (!groups.has(m.pattern)) groups.set(m.pattern, []);
        groups.get(m.pattern).push(m);
    }
    el.list.replaceChildren();
    if (!groups.size) el.list.innerHTML = '<p class="empty">no match</p>';
    for (const [pattern, items] of groups) {
        const g = document.createElement('div');
        g.className = 'group';
        g.innerHTML = '<h3></h3>';
        g.firstChild.textContent = pattern;
        for (const m of items) {
            const b = document.createElement('button');
            b.className = 'algo' + (S.meta?.id === m.id ? ' on' : '');
            b.dataset.id = m.id;
            b.innerHTML = '<span></span><small></small>';
            b.firstChild.textContent = m.name;
            b.lastChild.textContent = m.time;
            g.append(b);
        }
        el.list.append(g);
    }
}
el.list.addEventListener('click', e => {
    const b = e.target.closest('.algo');
    if (b) selectAlgo(b.dataset.id);
});
el.search.addEventListener('input', () => renderSidebar(el.search.value));

function selectAlgo(id, values) {
    const meta = S.catalog.find(m => m.id === id) || S.catalog[0];
    endStream();
    resetTrace();
    S.meta = meta;
    el.fSession.textContent = 'session –';

    S.form = buildForm(el.formHost, meta, { onEnter: () => run('play') });
    if (values) S.form.write(values);

    el.preset.replaceChildren(new Option('— load a preset —', ''));
    meta.presets.forEach((p, i) => el.preset.add(new Option(p.name, String(i))));

    el.title.textContent = meta.name;
    el.tags.innerHTML = '';
    for (const t of [meta.pattern, `time ${meta.time}`, `space ${meta.space}`]) {
        const s = document.createElement('span'); s.textContent = t; el.tags.append(s);
    }
    el.pseudo.replaceChildren(...meta.pseudo.map(line => {
        const li = document.createElement('li');
        const c = document.createElement('code'); c.textContent = line;
        li.append(c); return li;
    }));
    el.blurb.textContent = meta.blurb;
    el.cTime.textContent = meta.time;
    el.cSpace.textContent = meta.space;
    el.insight.textContent = meta.insight;
    document.title = `${meta.name} · DSA // PATTERNS // VISUAL`;

    renderSidebar(el.search.value);
    renderer.idle(meta.name);
    history.replaceState(null, '', `#/${meta.id}`);
}

// ------------------------------------------------------------ input actions
el.run.addEventListener('click', () => run('play'));
el.random.addEventListener('click', () => { S.form.randomize(); toast('random data loaded'); });
el.reset.addEventListener('click', () => {
    S.form.write(Object.fromEntries(S.meta.fields.map(f => [f.key, f.def])));
    toast('defaults restored');
});
el.preset.addEventListener('change', () => {
    const p = S.meta.presets[Number(el.preset.value)];
    if (p) { S.form.write(p.values); toast(`preset: ${p.name}`); }
});

// ----------------------------------------------------------------- transport
el.play.addEventListener('click', () => (S.playing ? pause() : play()));
el.fwd.addEventListener('click', stepForward);
el.back.addEventListener('click', stepBack);
el.first.addEventListener('click', toFirst);
el.last.addEventListener('click', toLast);
el.scrub.addEventListener('input', () => scrubTo(Number(el.scrub.value)));

function setSpeed(v, announce = false) {
    v = Math.max(1, Math.min(10, v));
    el.speed.value = v;
    S.delay = DELAYS[v - 1];
    el.speedLabel.textContent = `${S.delay} ms`;
    store.set('dsa.speed', v);
    srv('delay', S.delay);
    if (announce) toast(`speed ${v}/10 · ${S.delay} ms per step`);
}
el.speed.addEventListener('input', () => setSpeed(Number(el.speed.value)));

// ------------------------------------------------------------------ header
$('btnShare').addEventListener('click', async () => {
    const r = S.form.read();
    if (r.ok) history.replaceState(null, '', `#/${S.meta.id}?p=${b64e(r.values)}`);
    try { await navigator.clipboard.writeText(location.href); toast('link copied'); }
    catch { toast(location.href); }
});
$('btnExport').addEventListener('click', () => {
    if (!S.frames.length) return toast('nothing to export yet – run a trace first', 'err');
    const r = S.form.read();
    const blob = new Blob([JSON.stringify({ algo: S.meta.id, params: r.values ?? null, complete: S.done, frames: S.frames }, null, 1)],
        { type: 'application/json' });
    const a = Object.assign(document.createElement('a'), { href: URL.createObjectURL(blob), download: `trace-${S.meta.id}.json` });
    a.click();
    setTimeout(() => URL.revokeObjectURL(a.href), 1000);
});
function setFx(on) { document.body.classList.toggle('fx-off', !on); store.set('dsa.fx', on ? '1' : '0'); }
$('btnFx').addEventListener('click', () => setFx(document.body.classList.contains('fx-off')));
$('btnHelp').addEventListener('click', () => el.help.showModal());

// ---------------------------------------------------------------- keyboard
document.addEventListener('keydown', e => {
    if (e.ctrlKey || e.metaKey || e.altKey || el.help.open) return;
    const t = e.target;
    const typing = t.matches?.('input:not([type=range]), textarea, select');
    if (typing) return;
    const onRange = t.matches?.('input[type=range]');
    switch (e.key) {
        case ' ': if (t.matches?.('button')) return; e.preventDefault(); S.playing ? pause() : play(); break;
        case 'ArrowRight': if (onRange) return; e.preventDefault(); stepForward(); break;
        case 'ArrowLeft': if (onRange) return; e.preventDefault(); stepBack(); break;
        case 'Home': if (onRange) return; e.preventDefault(); toFirst(); break;
        case 'End': if (onRange) return; e.preventDefault(); toLast(); break;
        case '+': case '=': setSpeed(Number(el.speed.value) + 1, true); break;
        case '-': case '_': setSpeed(Number(el.speed.value) - 1, true); break;
        case 'r': case 'R': run('play'); break;
        case '?': el.help.showModal(); break;
        default:
    }
});
window.addEventListener('beforeunload', () => beaconStop(S.stream?.id));

// ------------------------------------------------------------------ health
async function pollHealth() {
    try {
        const h = await fetchHealth();
        el.fConn.className = 'led on';
        el.fConn.textContent = `SERVER ONLINE · v${h.version} · up ${fmtUp(h.uptimeSeconds)} · ${h.liveSessions} live`;
    } catch {
        el.fConn.className = 'led off';
        el.fConn.textContent = 'SERVER OFFLINE';
    }
}
const fmtUp = s => (s < 90 ? `${s}s` : s < 5400 ? `${Math.round(s / 60)}m` : `${(s / 3600).toFixed(1)}h`);

// -------------------------------------------------------------------- boot
function parseHash() {
    const m = location.hash.match(/^#\/([\w-]+)(?:\?p=([\w-]+))?/);
    if (!m) return {};
    let values;
    try { values = m[2] ? b64d(m[2]) : undefined; } catch { /* ignore a corrupt link */ }
    return { id: m[1], values };
}

async function boot() {
    setFx(store.get('dsa.fx', '1') === '1');
    setSpeed(Number(store.get('dsa.speed', 5)));
    renderer.idle('');
    pollHealth();
    setInterval(pollHealth, 8000);
    try {
        S.catalog = await fetchCatalog();
    } catch (e) {
        renderer.message('cannot reach the server');
        el.title.textContent = 'OFFLINE';
        toast(String(e.message || e), 'err');
        return;
    }
    const { id, values } = parseHash();
    selectAlgo(id || S.catalog[0].id, values);
    if (values) toast('input loaded from link – press RUN');
}
boot();
