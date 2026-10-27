// Canvas renderers – one per `renderer` value of a Frame.
// Each draws from frame.data onto a shared 960×440 logical canvas.

export const W = 960;
export const H = 440;
const TOP = 54;            // content area starts below the info chips
const BOTTOM = H - 70;     // … and ends above the list chips

export const C = {
    bg: '#07050f', cyan: '#00f0ff', magenta: '#ff2bd6', yellow: '#fcee0a', green: '#39ff88',
    purple: '#a66bff', red: '#ff3860', orange: '#ff9e3d', text: '#e6e1ff', mute: '#7a74a8',
    panel: '#0f0b24', line: '#2d2a55', dim: '#1c1a35',
};
const FONT = 'ui-monospace, "JetBrains Mono", "Fira Code", Menlo, Consolas, monospace';

// ---- cell styles -----------------------------------------------------------
const CELL = {          // array / stack / heap / tree marks
    '.': { fill: C.panel,  stroke: C.line,    text: '#d8d2ff' },
    'd': { fill: '#0a0818', stroke: C.dim,     text: '#4b4778' },
    'c': { fill: '#2b2a00', stroke: C.yellow,  text: '#fffbb0', glow: C.yellow },
    'w': { fill: '#052a30', stroke: C.cyan,    text: '#c2fcff', glow: C.cyan },
    'f': { fill: '#06301c', stroke: C.green,   text: '#d0ffe3', glow: C.green },
    'x': { fill: '#350a12', stroke: C.red,     text: '#ffc9d3', glow: C.red },
    'v': { fill: '#06303a', stroke: '#1fa3bd', text: '#aef3ff' },
};
const NODE = Object.assign({}, CELL, {   // trees: f = waiting in stack/queue
    'f': { fill: '#38082f', stroke: C.magenta, text: '#ffd1f6', glow: C.magenta },
});
const GRID = {          // grid renderer codes
    '.': { fill: C.panel,  stroke: C.line,    text: '#d8d2ff' },
    '#': { fill: '#4a4590', stroke: '#7a74d0', text: '#9d98e0' },
    'S': { fill: '#06301c', stroke: C.green,   text: C.green,  glow: C.green },
    'E': { fill: '#38082f', stroke: C.magenta, text: C.magenta, glow: C.magenta },
    'v': { fill: '#06303a', stroke: '#1fa3bd', text: '#aef3ff' },
    'f': { fill: '#38082f', stroke: C.magenta, text: '#ffd1f6', glow: C.magenta },
    'c': { fill: '#2b2a00', stroke: C.yellow,  text: '#fffbb0', glow: C.yellow },
    'p': { fill: '#06301c', stroke: C.green,   text: '#d0ffe3', glow: C.green },
    'm': { fill: '#0b4a2a', stroke: C.green,   text: '#eafff3', glow: C.green },
    'd': { fill: '#241552', stroke: C.purple,  text: '#e4d6ff', glow: C.purple },
    'x': { fill: '#350a12', stroke: C.red,     text: '#ffc9d3', glow: C.red },
    'q': { fill: '#052a30', stroke: C.cyan,    text: C.cyan,   glow: C.cyan },
};
const PTR = { left: C.cyan, lo: C.cyan, right: C.magenta, hi: C.magenta, mid: C.yellow, i: C.yellow };
const PTR_FALLBACK = [C.green, C.purple, C.orange];

export function createRenderer(canvas) {
    const dpr = Math.min(globalThis.devicePixelRatio || 1, 2);
    canvas.width = W * dpr;
    canvas.height = H * dpr;
    const ctx = canvas.getContext('2d');
    ctx.setTransform(dpr, 0, 0, dpr, 0, 0);

    // ---- primitives ---------------------------------------------------------
    const glow = (c, b = 14) => { ctx.shadowColor = c; ctx.shadowBlur = b; };
    const noGlow = () => { ctx.shadowBlur = 0; ctx.shadowColor = 'transparent'; };
    const font = (px, bold = false) => { ctx.font = `${bold ? 'bold ' : ''}${px}px ${FONT}`; };
    function txt(s, x, y, { size = 14, color = C.text, align = 'center', base = 'middle', bold = false, g = null } = {}) {
        font(size, bold);
        ctx.fillStyle = color;
        ctx.textAlign = align;
        ctx.textBaseline = base;
        if (g) glow(g, 8);
        ctx.fillText(String(s), x, y);
        noGlow();
    }
    function cut(x, y, w, h, k = 6) {           // rect with clipped corners
        ctx.beginPath();
        ctx.moveTo(x + k, y); ctx.lineTo(x + w - k, y); ctx.lineTo(x + w, y + k);
        ctx.lineTo(x + w, y + h - k); ctx.lineTo(x + w - k, y + h); ctx.lineTo(x + k, y + h);
        ctx.lineTo(x, y + h - k); ctx.lineTo(x, y + k); ctx.closePath();
    }
    function cell(x, y, w, h, st, label, { size, round = false } = {}) {
        if (st.glow) glow(st.glow, 16);
        ctx.fillStyle = st.fill;
        ctx.strokeStyle = st.stroke;
        ctx.lineWidth = st.glow ? 2 : 1;
        if (round) { ctx.beginPath(); ctx.arc(x + w / 2, y + h / 2, w / 2, 0, Math.PI * 2); }
        else cut(x, y, w, h, Math.min(6, w / 5));
        ctx.fill(); ctx.stroke();
        noGlow();
        if (label !== undefined && label !== null && label !== '') {
            const s = String(label);
            const px = size || Math.max(9, Math.min(22, (w - 6) / Math.max(1.6, s.length * 0.62)));
            txt(s, x + w / 2, y + h / 2 + 1, { size: px, color: st.text, bold: true });
        }
    }
    function arrowUp(x, yBottom, color, len = 16) {
        ctx.strokeStyle = ctx.fillStyle = color;
        ctx.lineWidth = 2;
        glow(color, 10);
        ctx.beginPath(); ctx.moveTo(x, yBottom); ctx.lineTo(x, yBottom - len + 6); ctx.stroke();
        ctx.beginPath(); ctx.moveTo(x, yBottom - len); ctx.lineTo(x - 6, yBottom - len + 9); ctx.lineTo(x + 6, yBottom - len + 9);
        ctx.closePath(); ctx.fill();
        noGlow();
    }
    function arrowDown(x, yTop, color, len = 16) {
        ctx.strokeStyle = ctx.fillStyle = color;
        ctx.lineWidth = 2;
        glow(color, 10);
        ctx.beginPath(); ctx.moveTo(x, yTop); ctx.lineTo(x, yTop + len - 6); ctx.stroke();
        ctx.beginPath(); ctx.moveTo(x, yTop + len); ctx.lineTo(x - 6, yTop + len - 9); ctx.lineTo(x + 6, yTop + len - 9);
        ctx.closePath(); ctx.fill();
        noGlow();
    }
    function chip(label, value, x, y, color = C.cyan) {
        const s = value === undefined ? String(label) : `${label} ${value}`;
        font(12, true);
        const w = ctx.measureText(s).width + 18;
        ctx.fillStyle = 'rgba(0,0,0,.35)';
        ctx.strokeStyle = color;
        ctx.lineWidth = 1;
        cut(x, y, w, 22, 5); ctx.fill(); ctx.stroke();
        if (value === undefined) txt(s, x + 9, y + 12, { size: 12, color, align: 'left', bold: true });
        else {
            txt(label, x + 9, y + 12, { size: 12, color: C.mute, align: 'left' });
            const lw = ctx.measureText(label + ' ').width;
            txt(value, x + 9 + lw, y + 12, { size: 12, color, align: 'left', bold: true });
        }
        return w + 8;
    }

    function background() {
        ctx.fillStyle = C.bg;
        ctx.fillRect(0, 0, W, H);
        ctx.strokeStyle = 'rgba(0,240,255,0.045)';
        ctx.lineWidth = 1;
        ctx.beginPath();
        for (let x = 0; x <= W; x += 40) { ctx.moveTo(x + .5, 0); ctx.lineTo(x + .5, H); }
        for (let y = 0; y <= H; y += 40) { ctx.moveTo(0, y + .5); ctx.lineTo(W, y + .5); }
        ctx.stroke();
        const g = ctx.createRadialGradient(W / 2, H / 2, H / 3, W / 2, H / 2, W * 0.7);
        g.addColorStop(0, 'rgba(0,0,0,0)');
        g.addColorStop(1, 'rgba(0,0,0,.45)');
        ctx.fillStyle = g;
        ctx.fillRect(0, 0, W, H);
    }

    /** info chips (top) + labelled item rows (bottom) shared by all renderers */
    function hud(d) {
        let x = 16;
        const palette = [C.cyan, C.magenta, C.yellow, C.green];
        let k = 0;
        for (const [key, val] of Object.entries(d.info || {})) {
            if (val === null || val === undefined) continue;
            x += chip(key, val, x, 14, palette[k++ % palette.length]);
        }
        const lists = d.lists || [];
        lists.forEach((l, i) => {
            const y = H - 20 - (lists.length - 1 - i) * 30;
            txt(l.label, 16, y, { size: 12, color: C.mute, align: 'left' });
            font(12);
            let cx = 16 + ctx.measureText(l.label).width + 16;
            if (!l.items.length) txt('∅', cx + 6, y, { size: 13, color: C.mute, align: 'left' });
            for (const it of l.items) {
                const s = String(it);
                font(13, true);
                const w = Math.max(26, ctx.measureText(s).width + 14);
                if (cx + w > W - 16) { txt('…', cx + 4, y, { size: 14, color: C.mute, align: 'left' }); break; }
                ctx.fillStyle = 'rgba(0,240,255,.07)';
                ctx.strokeStyle = 'rgba(0,240,255,.5)';
                ctx.lineWidth = 1;
                cut(cx, y - 11, w, 22, 5); ctx.fill(); ctx.stroke();
                txt(s, cx + w / 2, y + 1, { size: 13, color: C.text, bold: true });
                cx += w + 6;
            }
        });
    }

    // ---- array --------------------------------------------------------------
    // data: values[], marks, pointers{name:idx}, window{from,to}, info, lists
    function array(d) {
        const v = d.values, n = v.length, marks = d.marks || '';
        const cw = Math.min(96, (W - 80) / n), ch = n > 14 ? 46 : n < 9 ? 66 : 58;
        const x0 = (W - cw * n) / 2, y0 = 150;

        if (d.window && d.window.to >= d.window.from) {
            const wx = x0 + d.window.from * cw - 5, ww = (d.window.to - d.window.from + 1) * cw + 10;
            ctx.setLineDash([6, 4]);
            ctx.strokeStyle = C.cyan; ctx.lineWidth = 1.5; glow(C.cyan, 12);
            cut(wx, y0 - 14, ww, ch + 28, 8); ctx.stroke();
            noGlow(); ctx.setLineDash([]);
            txt(`window · ${d.window.to - d.window.from + 1}`, wx + ww / 2, y0 - 26, { size: 11, color: C.cyan, bold: true });
        }
        for (let i = 0; i < n; i++) {
            const x = x0 + i * cw;
            cell(x + 2, y0, cw - 4, ch, CELL[marks[i]] || CELL['.'], v[i], { size: n > 14 ? 14 : undefined });
            txt(i, x + cw / 2, y0 + ch + 14, { size: 11, color: C.mute });
        }
        // pointers, stacked when several share an index
        const slot = {}; let fb = 0;
        for (const [name, idx] of Object.entries(d.pointers || {})) {
            if (idx === null || idx === undefined || idx < 0 || idx >= n) continue;
            const color = PTR[name] || PTR_FALLBACK[fb++ % PTR_FALLBACK.length];
            const row = slot[idx] = (slot[idx] ?? -1) + 1;
            const x = x0 + idx * cw + cw / 2, yb = y0 + ch + 76 + row * 40;
            arrowUp(x, yb - 16, color, 22);
            txt(name, x, yb - 4, { size: 13, color, bold: true, g: color });
        }
        hud(d);
    }

    // ---- stack (monotonic) ---------------------------------------------------
    // data: values[], cur, stack[idx…bottom→top], result[], marks, info
    function stack(d) {
        const v = d.values, n = v.length, marks = d.marks || '';
        const cw = Math.min(70, (W - 140) / n), ch = 44;
        const x0 = 120 + ((W - 140) - cw * n) / 2, y1 = 82, y2 = 176;

        txt('input', 16, y1 + ch / 2, { size: 12, color: C.mute, align: 'left' });
        txt('answer', 16, y2 + ch / 2, { size: 12, color: C.mute, align: 'left' });
        for (let i = 0; i < n; i++) {
            const x = x0 + i * cw;
            cell(x + 2, y1, cw - 4, ch, CELL[marks[i]] || CELL['.'], v[i]);
            txt(i, x + cw / 2, y1 + ch + 12, { size: 10, color: C.mute });
            const r = d.result[i];
            const st = r === null || r === undefined ? CELL['d'] : (r < 0 ? CELL['x'] : CELL['f']);
            cell(x + 2, y2, cw - 4, ch, st, r === null || r === undefined ? '?' : r);
        }
        if (d.cur >= 0 && d.cur < n) arrowDown(x0 + d.cur * cw + cw / 2, y1 - 26, C.yellow, 22);

        // the stack: horizontal tray, bottom on the left
        const ty = 290, sw = 64, sh = 50;
        txt('STACK', 16, ty + sh / 2 - 8, { size: 12, color: C.cyan, align: 'left', bold: true });
        txt('bottom → top', 16, ty + sh / 2 + 10, { size: 10, color: C.mute, align: 'left' });
        const tx = 120;
        ctx.strokeStyle = C.cyan; ctx.lineWidth = 2; glow(C.cyan, 10);
        ctx.beginPath(); ctx.moveTo(tx - 6, ty - 6); ctx.lineTo(tx - 6, ty + sh + 6); ctx.lineTo(tx + Math.max(6, d.stack.length + 1) * (sw + 6), ty + sh + 6); ctx.stroke();
        noGlow();
        if (!d.stack.length) txt('(empty)', tx + 40, ty + sh / 2, { size: 13, color: C.mute, align: 'left' });
        d.stack.forEach((idx, k) => {
            const top = k === d.stack.length - 1;
            const x = tx + k * (sw + 6);
            cell(x, ty, sw, sh, top ? CELL['w'] : CELL['v'], v[idx]);
            txt(`#${idx}`, x + sw / 2, ty + sh + 16, { size: 11, color: top ? C.cyan : C.mute });
            if (top) txt('top', x + sw / 2, ty - 14, { size: 11, color: C.cyan, bold: true, g: C.cyan });
        });
        hud(d);
    }

    // ---- intervals -------------------------------------------------------------
    // data: items[{s,e,st}], merged[[s,e]], info
    const IV = {
        p: { fill: 'rgba(122,116,168,.25)', stroke: '#5b5690', text: '#b9b4e6' },
        c: { fill: 'rgba(252,238,10,.28)',  stroke: C.yellow,  text: '#fffbb0', glow: C.yellow },
        a: { fill: 'rgba(0,240,255,.25)',   stroke: C.cyan,    text: '#c2fcff', glow: C.cyan },
        k: { fill: 'rgba(255,43,214,.25)',  stroke: C.magenta, text: '#ffd1f6', glow: C.magenta },
    };
    function intervals(d) {
        const items = d.items, merged = d.merged || [];
        const all = items.flatMap(i => [i.s, i.e]);
        const lo = Math.min(...all), hi = Math.max(...all);
        const L = 96, R = W - 36, span = Math.max(1, hi - lo);
        const X = v => L + (v - lo) / span * (R - L);
        const rowH = Math.max(14, Math.min(28, (BOTTOM - TOP - 62) / items.length));
        const bh = rowH * 0.62;

        // axis
        const ay = BOTTOM - 4;
        ctx.strokeStyle = C.line; ctx.lineWidth = 1;
        ctx.beginPath(); ctx.moveTo(L, ay); ctx.lineTo(R, ay); ctx.stroke();
        const step = Math.max(1, Math.ceil(span / 20));
        for (let v = lo; v <= hi; v += step) {
            ctx.beginPath(); ctx.moveTo(X(v), ay - 3); ctx.lineTo(X(v), ay + 3); ctx.stroke();
            txt(v, X(v), ay + 14, { size: 10, color: C.mute });
        }
        // guide at the end of the last merged group
        if (merged.length) {
            const e = merged[merged.length - 1][1];
            ctx.setLineDash([4, 4]); ctx.strokeStyle = 'rgba(57,255,136,.55)';
            ctx.beginPath(); ctx.moveTo(X(e), TOP - 4); ctx.lineTo(X(e), ay); ctx.stroke();
            ctx.setLineDash([]);
            txt(`last end ${e}`, X(e), TOP - 12, { size: 10, color: C.green });
        }
        items.forEach((it, r) => {
            const y = TOP + 8 + r * rowH;
            txt(`[${it.s},${it.e}]`, 16, y + bh / 2, { size: 11, color: C.mute, align: 'left' });
            const st = IV[it.st] || IV.p;
            const x1 = X(it.s), w = Math.max(6, X(it.e) - x1);
            if (st.glow) glow(st.glow, 12);
            ctx.fillStyle = st.fill; ctx.strokeStyle = st.stroke; ctx.lineWidth = 1.5;
            cut(x1, y, w, bh, 4); ctx.fill(); ctx.stroke();
            noGlow();
        });
        // merged lane
        const my = BOTTOM - 44;
        txt('MERGED', 16, my + 9, { size: 11, color: C.green, align: 'left', bold: true });
        merged.forEach(m => {
            const x1 = X(m[0]), w = Math.max(10, X(m[1]) - x1);
            glow(C.green, 14);
            ctx.fillStyle = 'rgba(57,255,136,.28)'; ctx.strokeStyle = C.green; ctx.lineWidth = 2;
            cut(x1, my, w, 20, 5); ctx.fill(); ctx.stroke();
            noGlow();
            if (w > 44) txt(`${m[0]}–${m[1]}`, x1 + w / 2, my + 11, { size: 11, color: '#eafff3', bold: true });
        });
        hud(d);
    }

    // ---- heap -------------------------------------------------------------------
    // data: values[] (stream), cur, heap[], k, marks, info, lists
    function heap(d) {
        const v = d.values, n = v.length;
        const cw = Math.min(44, (W - 120) / n), x0 = 90 + ((W - 110) - cw * n) / 2, y0 = 52;
        txt('stream', 16, y0 + 17, { size: 12, color: C.mute, align: 'left' });
        for (let i = 0; i < n; i++) {
            const st = i < d.cur ? CELL['d'] : i === d.cur ? CELL['c'] : CELL['.'];
            cell(x0 + i * cw + 1, y0, cw - 2, 34, st, v[i], { size: 13 });
        }

        const h = d.heap, size = h.length, marks = d.marks || '';
        // tree layout
        const levels = Math.max(1, Math.floor(Math.log2(Math.max(1, size))) + 1);
        const ty = 126, gap = Math.min(64, (BOTTOM - 90 - ty) / Math.max(1, levels - 1 || 1));
        const pos = i => {
            const l = Math.floor(Math.log2(i + 1)), p = i - (2 ** l - 1);
            return { x: (p + 0.5) / 2 ** l * (W - 80) + 40, y: ty + l * gap };
        };
        ctx.lineWidth = 2; ctx.strokeStyle = 'rgba(0,240,255,.35)';
        for (let i = 1; i < size; i++) {
            const a = pos(i), b = pos((i - 1) >> 1);
            ctx.beginPath(); ctx.moveTo(a.x, a.y); ctx.lineTo(b.x, b.y); ctx.stroke();
        }
        for (let i = 0; i < size; i++) {
            const p = pos(i);
            cell(p.x - 19, p.y - 19, 38, 38, (CELL[marks[i]] || CELL['.']), h[i], { round: true, size: 14 });
        }
        if (!size) txt('(empty heap)', W / 2, ty + 20, { size: 14, color: C.mute });
        // array view of the heap
        const ay = BOTTOM - 26, aw = 40;
        txt(`heap[] min-heap · cap ${d.k}`, 16, ay - 20, { size: 11, color: C.mute, align: 'left' });
        for (let i = 0; i < d.k; i++) {
            const x = 16 + i * (aw + 4);
            if (i < size) cell(x, ay - 10, aw, 30, CELL[marks[i]] || CELL['.'], h[i], { size: 13 });
            else {
                ctx.setLineDash([3, 3]); ctx.strokeStyle = C.line; ctx.lineWidth = 1;
                cut(x, ay - 10, aw, 30, 5); ctx.stroke(); ctx.setLineDash([]);
            }
        }
        hud(d);
    }

    // ---- grid -------------------------------------------------------------------
    // data: rows, cols, cells[] (codes), nums[][] (<0 = none) | text[][], rowLabels, colLabels, checker, info, lists
    function grid(d) {
        const R = d.rows, Cn = d.cols;
        const hasLabels = d.rowLabels || d.colLabels;
        const lm = hasLabels ? 34 : 0, tm = d.colLabels ? 22 : 0;
        const availH = BOTTOM - TOP - tm + 8, availW = W - 60 - lm;
        const s = Math.min(54, availW / Cn, availH / R);
        const x0 = (W - s * Cn - lm) / 2 + lm, y0 = TOP + tm + (availH - s * R) / 2 - 2;
        const px = s > 34 ? 18 : s > 24 ? 13 : 10;

        for (let r = 0; r < R; r++) {
            if (d.rowLabels) txt(d.rowLabels[r], x0 - 16, y0 + r * s + s / 2, { size: 12, color: C.mute });
            for (let c = 0; c < Cn; c++) {
                const code = d.cells[r][c];
                let st = GRID[code] || GRID['.'];
                if (d.checker && code === '.' && (r + c) % 2) st = { ...st, fill: '#140f30' };
                let label = '';
                if (d.text && d.text[r][c] !== undefined && d.text[r][c] !== null) label = d.text[r][c];
                else if (d.nums && d.nums[r][c] >= 0) label = d.nums[r][c];
                if (code === 'S' || code === 'E') label = label || code;
                cell(x0 + c * s + 1, y0 + r * s + 1, s - 2, s - 2, st, label, { size: px });
            }
        }
        if (d.colLabels) d.colLabels.forEach((l, c) => txt(l, x0 + c * s + s / 2, y0 - 12, { size: 12, color: C.mute }));
        hud(d);
    }

    // ---- tree -------------------------------------------------------------------
    // data: tree{v[],l[],r[]}, marks, info, lists
    function tree(d) {
        const t = d.tree, m = t.v.length, marks = d.marks || '';
        const xi = new Array(m).fill(0), dep = new Array(m).fill(0);
        let counter = 0, maxDepth = 0;
        (function walk(n, depth) {                   // in-order position = x, depth = y
            if (n < 0) return;
            walk(t.l[n], depth + 1);
            xi[n] = counter++; dep[n] = depth; maxDepth = Math.max(maxDepth, depth);
            walk(t.r[n], depth + 1);
        })(0, 0);
        const lists = (d.lists || []).length;
        const bottom = H - 24 - lists * 30;
        const gapY = Math.min(70, (bottom - TOP - 44) / Math.max(1, maxDepth));
        const P = n => ({ x: 50 + (xi[n] + 0.5) * (W - 100) / m, y: TOP + 30 + dep[n] * gapY });

        ctx.lineWidth = 2;
        for (let n = 0; n < m; n++) for (const ch of [t.l[n], t.r[n]]) {
            if (ch < 0) continue;
            const a = P(n), b = P(ch);
            ctx.strokeStyle = marks[ch] === 'v' && marks[n] === 'v' ? 'rgba(57,255,136,.55)' : 'rgba(0,240,255,.35)';
            ctx.beginPath(); ctx.moveTo(a.x, a.y); ctx.lineTo(b.x, b.y); ctx.stroke();
        }
        for (let n = 0; n < m; n++) {
            const p = P(n);
            cell(p.x - 20, p.y - 20, 40, 40, NODE[marks[n]] || NODE['.'], t.v[n], { round: true, size: 15 });
        }
        hud(d);
    }

    const table = { array, stack, intervals, heap, grid, tree };

    return {
        draw(frame) {
            background();
            const fn = table[frame.renderer];
            if (!fn) return this.message(`unknown renderer "${frame.renderer}"`, C.red);
            fn(frame.data);
        },
        idle(name) {
            background();
            txt('// AWAITING INPUT', W / 2, H / 2 - 30, { size: 30, color: C.cyan, bold: true, g: C.cyan });
            txt(name || '', W / 2, H / 2 + 12, { size: 16, color: C.magenta, g: C.magenta });
            txt('set the data on the right, then RUN  ▸', W / 2, H / 2 + 46, { size: 13, color: C.mute });
        },
        message(msg, color = C.red) {
            background();
            txt('!! SIGNAL LOST', W / 2, H / 2 - 24, { size: 28, color, bold: true, g: color });
            txt(msg, W / 2, H / 2 + 18, { size: 14, color: C.text });
        },
    };
}
