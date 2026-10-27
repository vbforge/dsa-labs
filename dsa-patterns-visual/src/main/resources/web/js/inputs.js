// Builds the INPUT form from an algorithm's field schema, parses / validates
// user text into the wire format the server expects, and generates random data.

const ri = (a, b) => a + Math.floor(Math.random() * (b - a + 1));
const pick = arr => arr[ri(0, arr.length - 1)];

const fmt = {
    intlist: v => v.join(', '),
    intervals: v => v.map(p => `[${p[0]},${p[1]}]`).join(', '),
    tree: v => v.map(x => (x === null ? 'null' : x)).join(', '),
};

function parseInts(text, label) {
    const parts = text.split(/[\s,;]+/).filter(Boolean);
    return parts.map(p => {
        if (!/^-?\d+$/.test(p)) throw new Error(`${label}: "${p}" is not a whole number`);
        return Number(p);
    });
}

function randomTree() {
    const count = ri(5, 12), vals = [];
    const used = new Set();
    while (vals.length < count) { const v = ri(1, 30); if (!used.has(v)) { used.add(v); vals.push(v); } }
    // grow breadth-first so depth stays small, each child present with p = 0.75
    const nodes = [{ v: vals[0], l: null, r: null }];
    const q = [nodes[0]]; let next = 1;
    while (q.length && next < count) {
        const n = q.shift();
        for (const side of ['l', 'r']) {
            if (next < count && Math.random() < 0.78) {
                const c = { v: vals[next++], l: null, r: null };
                n[side] = c; q.push(c);
            }
        }
        if (!q.length && next < count) { const c = { v: vals[next++], l: null, r: null }; n.l = c; q.push(c); }
    }
    const out = [], bfs = [nodes[0]];
    while (bfs.length) {
        const n = bfs.shift();
        if (!n) { out.push(null); continue; }
        out.push(n.v); bfs.push(n.l, n.r);
    }
    while (out[out.length - 1] === null) out.pop();
    return out;
}

/** One concrete control per schema field. Each returns { el, get, set, random }. */
function makeField(f) {
    const o = f.opts || {};
    const wrap = document.createElement('div');
    wrap.className = 'field';
    wrap.dataset.key = f.key;
    const label = document.createElement('label');
    label.textContent = f.label;
    const err = document.createElement('div');
    err.className = 'field-err';
    wrap.append(label);
    const hint = f.hint ? Object.assign(document.createElement('div'), { className: 'field-hint', textContent: f.hint }) : null;

    const textInput = (type = 'text') => {
        const i = document.createElement('input');
        i.type = type; i.spellcheck = false; i.autocomplete = 'off'; i.id = `f-${f.key}`;
        label.htmlFor = i.id;
        return i;
    };
    let api;

    switch (f.type) {
        case 'intlist': {
            const i = textInput();
            wrap.append(i);
            api = {
                get: () => {
                    const v = parseInts(i.value, f.label);
                    if (v.length < o.minLen || v.length > o.maxLen) throw new Error(`${f.label}: enter ${o.minLen}–${o.maxLen} numbers (got ${v.length})`);
                    v.forEach(x => { if (x < o.min || x > o.max) throw new Error(`${f.label}: ${x} is outside ${o.min}…${o.max}`); });
                    return v;
                },
                set: v => { i.value = fmt.intlist(v); },
                random: () => {
                    const lo = Math.max(o.min, 1) <= Math.min(o.max, 30) ? Math.max(o.min, 1) : o.min;
                    const hi = Math.max(o.min, 1) <= Math.min(o.max, 30) ? Math.min(o.max, 30) : o.max;
                    const len = ri(Math.max(o.minLen, 5), Math.min(o.maxLen, 10));
                    i.value = fmt.intlist(Array.from({ length: len }, () => ri(lo, hi)));
                },
                input: i,
            };
            break;
        }
        case 'int': {
            const i = textInput('number');
            i.min = o.min; i.max = o.max; i.step = 1;
            wrap.append(i);
            api = {
                get: () => {
                    if (i.value.trim() === '' || !/^-?\d+$/.test(i.value.trim())) throw new Error(`${f.label}: enter a whole number`);
                    const v = Number(i.value);
                    if (v < o.min || v > o.max) throw new Error(`${f.label}: must be between ${o.min} and ${o.max}`);
                    return v;
                },
                set: v => { i.value = v; },
                random: ctx => {
                    const [kind, src, ...rest] = (o.random || '').split(':');
                    const other = src && ctx.peek(src);
                    if (kind === 'element' && other?.length) i.value = pick(other);
                    else if (kind === 'pairsum' && other?.length > 1) {
                        const a = ri(0, other.length - 1); let b = ri(0, other.length - 1);
                        if (a === b) b = (a + 1) % other.length;
                        i.value = other[a] + other[b];
                    } else if (kind === 'upto' && other?.length) i.value = ri(1, Math.min(o.max, other.length, 5));
                    else i.value = ri(Math.max(o.min, 4), Math.min(o.max, 7));
                },
                input: i, dependsOn: o.random ? o.random.split(':')[1] : null,
            };
            break;
        }
        case 'text': {
            const i = textInput();
            i.maxLength = o.maxLen;
            wrap.append(i);
            api = {
                get: () => {
                    if (!i.value.length) throw new Error(`${f.label}: cannot be empty`);
                    return i.value;
                },
                set: v => { i.value = v; },
                random: () => {
                    const alpha = o.alphabet || 'abcde';
                    const len = ri(Math.min(6, o.maxLen), Math.min(o.maxLen, 12));
                    i.value = Array.from({ length: len }, () => alpha[ri(0, alpha.length - 1)]).join('');
                },
                input: i,
            };
            break;
        }
        case 'intervals': {
            const i = textInput();
            wrap.append(i);
            api = {
                get: () => {
                    const nums = (i.value.match(/-?\d+/g) || []).map(Number);
                    if (!nums.length || nums.length % 2) throw new Error(`${f.label}: give [start,end] pairs`);
                    const out = [];
                    for (let k = 0; k < nums.length; k += 2) {
                        const [s, e] = [nums[k], nums[k + 1]];
                        if (s > e) throw new Error(`${f.label}: [${s},${e}] starts after it ends`);
                        if (s < o.min || e > o.max) throw new Error(`${f.label}: values must be within ${o.min}…${o.max}`);
                        out.push([s, e]);
                    }
                    if (out.length > o.maxCount) throw new Error(`${f.label}: at most ${o.maxCount} intervals`);
                    return out;
                },
                set: v => { i.value = fmt.intervals(v); },
                random: () => {
                    const n = ri(4, 7);
                    i.value = fmt.intervals(Array.from({ length: n }, () => { const s = ri(0, 30); return [s, s + ri(1, 9)]; }));
                },
                input: i,
            };
            break;
        }
        case 'tree': {
            const i = textInput();
            wrap.append(i);
            api = {
                get: () => {
                    const toks = i.value.split(/[\s,;]+/).filter(Boolean);
                    const v = toks.map(t => {
                        if (/^(null|none|nil|n|#|-)$/i.test(t)) return null;
                        if (!/^-?\d+$/.test(t)) throw new Error(`${f.label}: "${t}" is not a number or null`);
                        return Number(t);
                    });
                    if (!v.length || v[0] === null) throw new Error(`${f.label}: start with a root value`);
                    if (v.length > o.maxNodes) throw new Error(`${f.label}: at most ${o.maxNodes} entries`);
                    return v;
                },
                set: v => { i.value = fmt.tree(v); },
                random: () => { i.value = fmt.tree(randomTree()); },
                input: i,
            };
            break;
        }
        case 'select': {
            const s = document.createElement('select');
            s.id = `f-${f.key}`; label.htmlFor = s.id;
            o.options.forEach(opt => s.add(new Option(opt, opt)));
            wrap.append(s);
            api = { get: () => s.value, set: v => { s.value = v; }, random: () => {}, input: s };
            break;
        }
        case 'maze':
            api = mazeEditor(wrap, f, o);
            break;
        default:
            throw new Error(`unsupported field type ${f.type}`);
    }
    if (hint) wrap.append(hint);
    wrap.append(err);
    api.el = wrap;
    api.err = err;
    return api;
}

// ---- grid / maze editor ------------------------------------------------------
function mazeEditor(wrap, f, o) {
    let rows = 8, cols = 15, cells = [];     // cells[r][c] ∈ '.', '#', 'S', 'E'
    let brush = '#', painting = false;

    const bar = document.createElement('div');
    bar.className = 'maze-bar';
    const CLS = { '#': 'wall', '.': 'open', S: 'start', E: 'end' };
    const brushes = [['#', 'WALL'], ['.', 'ERASE'], ['S', 'START'], ['E', 'END']];
    const btns = brushes.map(([code, name]) => {
        const b = document.createElement('button');
        b.type = 'button'; b.textContent = name; b.dataset.brush = code;
        b.className = 'tool tool-' + CLS[code];
        b.addEventListener('click', () => { brush = code; sync(); });
        bar.append(b);
        return b;
    });

    const size = document.createElement('div');
    size.className = 'maze-size';
    const rIn = Object.assign(document.createElement('input'), { type: 'number', min: 2, max: o.maxRows, title: 'rows' });
    const cIn = Object.assign(document.createElement('input'), { type: 'number', min: 2, max: o.maxCols, title: 'columns' });
    const mk = (t, fn) => { const b = document.createElement('button'); b.type = 'button'; b.className = 'tool'; b.textContent = t; b.addEventListener('click', fn); return b; };
    size.append(rIn, document.createTextNode('×'), cIn,
        mk('MAZE', () => randomize()), mk('CLEAR', () => { cells = cells.map(r => r.map(c => (c === '#' ? '.' : c))); draw(); }));

    const board = document.createElement('div');
    board.className = 'maze-board';
    wrap.append(bar, size, board);

    function blank(r, c, old) {
        const g = Array.from({ length: r }, (_, y) => Array.from({ length: c }, (_, x) => old?.[y]?.[x] ?? '.'));
        const flat = g.flat();
        if (!flat.includes('S')) { const i = flat.findIndex(ch => ch !== 'E'); g[Math.floor(i / c)][i % c] = 'S'; }
        if (!flat.includes('E') || !g.flat().includes('E')) {
            const f2 = g.flat(); let i = f2.length - 1;
            while (f2[i] === 'S') i--;
            g[Math.floor(i / c)][i % c] = 'E';
        }
        return g;
    }
    function resize() {
        const r = Math.max(2, Math.min(o.maxRows, Number(rIn.value) || rows));
        const c = Math.max(2, Math.min(o.maxCols, Number(cIn.value) || cols));
        rows = r; cols = c;
        cells = blank(rows, cols, cells);
        draw();
    }
    rIn.addEventListener('change', resize);
    cIn.addEventListener('change', resize);

    function draw() {
        rIn.value = rows; cIn.value = cols;
        board.style.gridTemplateColumns = `repeat(${cols}, 1fr)`;
        board.replaceChildren(...cells.flatMap((row, r) => row.map((ch, c) => {
            const d = document.createElement('div');
            d.className = 'mc mc-' + CLS[ch];
            d.dataset.r = r; d.dataset.c = c;
            d.textContent = ch === 'S' || ch === 'E' ? ch : '';
            return d;
        })));
        sync();
    }
    function sync() { btns.forEach(b => b.classList.toggle('on', b.dataset.brush === brush)); }

    function paint(target) {
        if (!target?.dataset || target.dataset.r === undefined) return;
        const r = +target.dataset.r, c = +target.dataset.c, cur = cells[r][c];
        if (brush === '#' || brush === '.') {
            if (cur === 'S' || cur === 'E' || cur === brush) return;
            cells[r][c] = brush;
        } else {
            if (cur === 'S' || cur === 'E') return;
            for (const row of cells) for (let x = 0; x < row.length; x++) if (row[x] === brush) row[x] = '.';
            cells[r][c] = brush;
            painting = false;
            return draw();
        }
        target.className = 'mc mc-' + CLS[brush];
    }
    board.addEventListener('pointerdown', e => { painting = true; board.setPointerCapture?.(e.pointerId); paint(e.target); e.preventDefault(); });
    board.addEventListener('pointermove', e => { if (painting) paint(document.elementFromPoint(e.clientX, e.clientY)); });
    const stop = () => { painting = false; };
    board.addEventListener('pointerup', stop);
    board.addEventListener('pointercancel', stop);

    function randomize() {
        cells = cells.map(row => row.map(ch => (ch === 'S' || ch === 'E' ? ch : Math.random() < 0.27 ? '#' : '.')));
        draw();
    }
    draw();

    return {
        get: () => cells.map(r => r.join('')),
        set: v => { rows = v.length; cols = v[0].length; cells = v.map(s => s.split('')); draw(); },
        random: randomize,
        input: null,
    };
}

/**
 * Renders the form for `meta` into `container`.
 * Returns { read(), write(values), randomize(), onEnter(fn) }.
 */
export function buildForm(container, meta, { onEnter } = {}) {
    container.replaceChildren();
    const fields = new Map();
    for (const f of meta.fields) {
        const ctl = makeField(f);
        fields.set(f.key, { f, ctl });
        container.append(ctl.el);
        ctl.input?.addEventListener('keydown', e => { if (e.key === 'Enter') onEnter?.(); });
        ctl.input?.addEventListener('input', () => { ctl.err.textContent = ''; ctl.el.classList.remove('bad'); });
    }
    const peek = key => { try { return fields.get(key)?.ctl.get(); } catch { return null; } };

    const form = {
        /** @returns {{ ok: boolean, values?: object, errors?: string[] }} */
        read() {
            const values = {}, errors = [];
            for (const [key, { ctl }] of fields) {
                try { values[key] = ctl.get(); ctl.err.textContent = ''; ctl.el.classList.remove('bad'); }
                catch (e) { errors.push(e.message); ctl.err.textContent = e.message; ctl.el.classList.add('bad'); }
            }
            return errors.length ? { ok: false, errors } : { ok: true, values };
        },
        write(values) {
            for (const [key, { ctl }] of fields) if (values[key] !== undefined) ctl.set(values[key]);
            for (const { ctl } of fields.values()) { ctl.err.textContent = ''; ctl.el.classList.remove('bad'); }
        },
        randomize() {
            const ctx = { peek };
            // fields without dependencies first, so `element:nums` etc. see fresh data
            const order = [...fields.values()].sort((a, b) => (a.ctl.dependsOn ? 1 : 0) - (b.ctl.dependsOn ? 1 : 0));
            order.forEach(({ ctl }) => ctl.random(ctx));
            [...fields.values()].forEach(({ ctl }) => { ctl.err.textContent = ''; ctl.el.classList.remove('bad'); });
        },
    };
    form.write(Object.fromEntries(meta.fields.map(f => [f.key, f.def])));
    return form;
}
