const canvas     = document.getElementById('stage');
const ctx        = canvas.getContext('2d');
const captionEl  = document.getElementById('caption');
const patternSel = document.getElementById('pattern');
const speedSel   = document.getElementById('speed');

const btnPlay  = document.getElementById('play');
const btnPrev  = document.getElementById('prev');
const btnNext  = document.getElementById('next');
const slider   = document.getElementById('slider');
const stepCtr  = document.getElementById('step-counter');

// Playback state
let frames = [];
let currentIdx = 0;
let isPlaying = false;
let timer = null;

// ============================================================================
//  Playback Engine
// ============================================================================
async function loadPattern() {
    stopPlayback();
    captionEl.textContent = "Loading...";

    try {
        const res = await fetch(`/trace?pattern=${encodeURIComponent(patternSel.value)}`);
        if (!res.ok) {
            const body = await res.json().catch(() => ({}));
            throw new Error(body.error || `HTTP ${res.status}`);
        }
        frames = await res.json();
        currentIdx = 0;

        slider.max = Math.max(0, frames.length - 1);
        slider.value = 0;
        updateUI();
        renderCurrentFrame();
    } catch (err) {
        captionEl.textContent = `Error: ${err.message}`;
        frames = [];
        updateUI();
    }
}

function updateUI() {
    stepCtr.textContent = frames.length ? `${currentIdx + 1} / ${frames.length}` : "0 / 0";

    if (isPlaying) {
        btnPlay.textContent = "⏸ Pause";
        btnPlay.classList.add('playing');
    } else {
        btnPlay.textContent = "▶ Play";
        btnPlay.classList.remove('playing');
    }

    btnPrev.disabled = currentIdx <= 0;
    btnNext.disabled = currentIdx >= frames.length - 1;
    btnPlay.disabled = frames.length === 0;

    slider.value = currentIdx;
}

function renderCurrentFrame() {
    if (!frames.length) {
        ctx.clearRect(0, 0, canvas.width, canvas.height);
        captionEl.textContent = "Select a pattern and press Play";
        return;
    }
    const frame = frames[currentIdx];
    ctx.clearRect(0, 0, canvas.width, canvas.height);

    const renderer = renderers[frame.renderer];
    if (renderer) {
        renderer(frame.data);
    } else {
        drawPlaceholder(frame.renderer || 'unknown', frame.data);
    }
    captionEl.textContent = frame.note || '';
}

function nextStep() {
    if (currentIdx < frames.length - 1) {
        currentIdx++;
        updateUI();
        renderCurrentFrame();
    } else {
        stopPlayback(); // Reached end
    }
}

function prevStep() {
    if (currentIdx > 0) {
        currentIdx--;
        updateUI();
        renderCurrentFrame();
    }
}

function startPlayback() {
    if (isPlaying || frames.length === 0) return;
    if (currentIdx >= frames.length - 1) {
        currentIdx = 0; // Restart if at end
        updateUI();
        renderCurrentFrame();
    }

    isPlaying = true;
    updateUI();
    const ms = parseInt(speedSel.value, 10);

    timer = setInterval(() => {
        if (currentIdx < frames.length - 1) {
            currentIdx++;
            updateUI();
            renderCurrentFrame();
        } else {
            stopPlayback();
        }
    }, ms);
}

function stopPlayback() {
    isPlaying = false;
    clearInterval(timer);
    updateUI();
}

// Event Listeners
btnPlay.addEventListener('click', () => {
    if (isPlaying) stopPlayback();
    else startPlayback();
});
btnPrev.addEventListener('click', () => { stopPlayback(); prevStep(); });
btnNext.addEventListener('click', () => { stopPlayback(); nextStep(); });
slider.addEventListener('input', (e) => {
    stopPlayback();
    currentIdx = parseInt(e.target.value, 10);
    updateUI();
    renderCurrentFrame();
});
speedSel.addEventListener('change', () => {
    if (isPlaying) {
        stopPlayback();
        startPlayback();
    }
});
patternSel.addEventListener('change', loadPattern);

// ============================================================================
//  Renderers
// ============================================================================
function drawArrayWithPointers(data) {
    const values = data.values;
    const n = values.length;

    const hasPrefix       = Array.isArray(data.prefix);
    const hasCounts       = Array.isArray(data.counts);
    const midSet          = data.mid !== undefined && data.mid >= 0;
    const hasWindow       = data.left !== undefined && data.left >= 0 &&
        data.right !== undefined && data.right >= 0 &&
        data.right >= data.left;
    const highlight       = new Set(Array.isArray(data.highlight) ? data.highlight : []);
    const prefixHighlight = new Set(Array.isArray(data.prefixHighlight) ? data.prefixHighlight : []);

    const cellW  = Math.min(90, (canvas.width - 100) / n);
    const cellH  = 60;
    const mainY  = 90;
    const prefixY = 200;
    const pointerBaseY = hasPrefix ? 310 : 210;
    const startX = (canvas.width - cellW * n) / 2;

    ctx.fillStyle = '#555';
    ctx.font = '14px ui-monospace, monospace';
    ctx.textAlign = 'left';
    ctx.textBaseline = 'top';
    let my = 16;
    for (const [name, val] of [
        ['target', data.target], ['k', data.k],
        ['sum', data.sum], ['best', data.best],
    ]) {
        if (val !== undefined) {
            ctx.fillText(`${name.padEnd(6)} = ${val}`, 20, my);
            my += 18;
        }
    }

    for (let i = 0; i < n; i++) {
        const x = startX + i * cellW;
        const isPointerL    = i === data.left;
        const isPointerR    = i === data.right;
        const isPointerM    = midSet && i === data.mid;
        const isPointer     = isPointerL || isPointerR || isPointerM;
        const isHighlighted = highlight.has(i);
        const inWindow      = hasWindow && i >= data.left && i <= data.right;

        if      (isHighlighted) ctx.fillStyle = '#c8e6c9';
        else if (isPointer)     ctx.fillStyle = '#ffe08a';
        else if (inWindow)      ctx.fillStyle = '#d6e9ff';
        else                    ctx.fillStyle = '#fff';

        ctx.strokeStyle = isPointer ? '#f57c00'
            : isHighlighted ? '#43a047'
                : '#bbb';
        ctx.lineWidth = (isPointer || isHighlighted) ? 2 : 1;

        ctx.fillRect(x, mainY, cellW, cellH);
        ctx.strokeRect(x, mainY, cellW, cellH);

        ctx.fillStyle = '#222';
        ctx.font = '22px ui-monospace, monospace';
        ctx.textAlign = 'center';
        ctx.textBaseline = 'middle';
        ctx.fillText(String(values[i]), x + cellW / 2, mainY + cellH / 2);

        ctx.fillStyle = '#888';
        ctx.font = '12px ui-monospace, monospace';
        ctx.fillText(i, x + cellW / 2, mainY + cellH + 14);

        if (hasCounts) {
            ctx.fillStyle = '#666';
            ctx.font = '11px ui-monospace, monospace';
            ctx.textBaseline = 'top';
            ctx.fillText(`×${data.counts[i]}`, x + cellW / 2, mainY + cellH + 26);
        }
    }

    if (hasPrefix) {
        ctx.fillStyle = '#666';
        ctx.font = 'bold 12px ui-monospace, monospace';
        ctx.textAlign = 'left';
        ctx.textBaseline = 'middle';
        ctx.fillText('prefix', 20, prefixY + cellH / 2);

        const prefixN = data.prefix.length;
        const pStartX = (canvas.width - cellW * prefixN) / 2;

        for (let i = 0; i < prefixN; i++) {
            const x = pStartX + i * cellW;
            const val = data.prefix[i];
            const isSet = val >= 0;
            const isHi  = prefixHighlight.has(i);

            ctx.fillStyle = isHi ? '#c8e6c9' : isSet ? '#fafafa' : '#f0f0f0';
            ctx.strokeStyle = isHi ? '#43a047' : '#bbb';
            ctx.lineWidth   = isHi ? 2 : 1;

            ctx.fillRect(x, prefixY, cellW, cellH);
            ctx.strokeRect(x, prefixY, cellW, cellH);

            ctx.fillStyle = isSet ? '#222' : '#aaa';
            ctx.font = '20px ui-monospace, monospace';
            ctx.textAlign = 'center';
            ctx.textBaseline = 'middle';
            ctx.fillText(isSet ? String(val) : '–', x + cellW / 2, prefixY + cellH / 2);

            ctx.fillStyle = '#888';
            ctx.font = '11px ui-monospace, monospace';
            ctx.fillText(i, x + cellW / 2, prefixY + cellH + 12);
        }
    }

    const pointers = [];
    if (data.left  !== undefined && data.left  >= 0) pointers.push(['left',  data.left,  '#1976d2']);
    if (midSet)                                      pointers.push(['mid',   data.mid,   '#7b1fa2']);
    if (data.right !== undefined && data.right >= 0) pointers.push(['right', data.right, '#d32f2f']);

    pointers.forEach(([name, idx, color], row) => {
        const x = startX + idx * cellW + cellW / 2;
        const arrowY = pointerBaseY + row * 44;
        drawArrowUp(x, arrowY, color);
        ctx.fillStyle = color;
        ctx.font = 'bold 13px sans-serif';
        ctx.textAlign = 'center';
        ctx.textBaseline = 'top';
        ctx.fillText(name, x, arrowY + 4);
    });
}

function drawArrowUp(x, yBottom, color) {
    ctx.strokeStyle = color;
    ctx.fillStyle   = color;
    ctx.lineWidth   = 2;
    ctx.beginPath();
    ctx.moveTo(x, yBottom);
    ctx.lineTo(x, yBottom - 18);
    ctx.stroke();
    ctx.beginPath();
    ctx.moveTo(x, yBottom - 18);
    ctx.lineTo(x - 6, yBottom - 10);
    ctx.lineTo(x + 6, yBottom - 10);
    ctx.closePath();
    ctx.fill();
}

function drawGrid(data) {
    const grid = data.grid;
    const rows = grid.length;
    const cols = grid[0].length;

    const maxW = canvas.width  - 120;
    const maxH = canvas.height - 120;
    const cellSize = Math.min(70, maxW / cols, maxH / rows);
    const gridW = cellSize * cols;
    const gridH = cellSize * rows;
    const startX = (canvas.width  - gridW) / 2;
    const startY = (canvas.height - gridH) / 2 - 20;

    const cellColors = data.cellColors || {};
    const cellLabels = data.cellLabels || {};
    const current    = data.current;
    const arrow      = data.arrow;

    for (let r = 0; r < rows; r++) {
        for (let c = 0; c < cols; c++) {
            const key = r + ',' + c;
            const x = startX + c * cellSize;
            const y = startY + r * cellSize;

            ctx.fillStyle = cellColors[key] || '#fff';
            ctx.fillRect(x, y, cellSize, cellSize);

            const isCurrent = current && current[0] === r && current[1] === c;
            ctx.strokeStyle = isCurrent ? '#f57c00' : '#bbb';
            ctx.lineWidth   = isCurrent ? 3 : 1;
            ctx.strokeRect(x + 0.5, y + 0.5, cellSize - 1, cellSize - 1);

            let label = cellLabels[key];
            if (label === undefined) {
                const v = grid[r][c];
                label = (v === 0 || v === null || v === undefined) ? '' : String(v);
            }
            if (label !== '') {
                ctx.fillStyle = '#222';
                ctx.font = `bold ${Math.floor(cellSize * 0.32)}px ui-monospace, monospace`;
                ctx.textAlign = 'center';
                ctx.textBaseline = 'middle';
                ctx.fillText(label, x + cellSize / 2, y + cellSize / 2);
            }

            if (arrow && isCurrent) {
                const glyphs = { right: '→', down: '↓', left: '←', up: '↑' };
                const g = glyphs[arrow];
                if (g) {
                    ctx.fillStyle = '#f57c00';
                    ctx.font = `bold ${Math.floor(cellSize * 0.28)}px sans-serif`;
                    ctx.textAlign = 'right';
                    ctx.textBaseline = 'bottom';
                    ctx.fillText(g, x + cellSize - 4, y + cellSize - 4);
                }
            }
        }
    }
}

function drawStack(data) {
    const values       = data.values;
    const answers      = data.answers || [];
    const currentIdx   = data.currentIdx ?? -1;
    const stackIndices = data.stackIndices || [];

    const n = values.length;
    const cellW = Math.min(70, (canvas.width - 100) / n);
    const cellH = 50;
    const startX = (canvas.width - cellW * n) / 2;

    ctx.fillStyle = '#555';
    ctx.font = 'bold 12px ui-monospace, monospace';
    ctx.textAlign = 'left';
    ctx.textBaseline = 'bottom';
    ctx.fillText('INPUT', 20, 45);

    for (let i = 0; i < n; i++) {
        const x = startX + i * cellW;
        const isCurrent = i === currentIdx;

        ctx.fillStyle = isCurrent ? '#ffe08a' : '#fff';
        ctx.strokeStyle = isCurrent ? '#f57c00' : '#bbb';
        ctx.lineWidth = isCurrent ? 2 : 1;
        ctx.fillRect(x, 55, cellW, cellH);
        ctx.strokeRect(x, 55, cellW, cellH);

        ctx.fillStyle = '#222';
        ctx.font = '18px ui-monospace, monospace';
        ctx.textAlign = 'center';
        ctx.textBaseline = 'middle';
        ctx.fillText(values[i], x + cellW / 2, 55 + cellH / 2);

        ctx.fillStyle = '#888';
        ctx.font = '11px ui-monospace, monospace';
        ctx.fillText(i, x + cellW / 2, 55 + cellH + 12);
    }

    ctx.fillStyle = '#555';
    ctx.font = 'bold 12px ui-monospace, monospace';
    ctx.textAlign = 'left';
    ctx.textBaseline = 'bottom';
    ctx.fillText('ANSWER (days until warmer)', 20, 150);

    for (let i = 0; i < n; i++) {
        const x = startX + i * cellW;
        const v = answers[i];
        const isFilled = v >= 0;

        ctx.fillStyle = isFilled ? '#e3f2fd' : '#fafafa';
        ctx.strokeStyle = '#bbb';
        ctx.lineWidth = 1;
        ctx.fillRect(x, 160, cellW, cellH);
        ctx.strokeRect(x, 160, cellW, cellH);

        ctx.fillStyle = isFilled ? '#1976d2' : '#ccc';
        ctx.font = '18px ui-monospace, monospace';
        ctx.textAlign = 'center';
        ctx.textBaseline = 'middle';
        ctx.fillText(isFilled ? String(v) : '–', x + cellW / 2, 160 + cellH / 2);
    }

    ctx.fillStyle = '#555';
    ctx.font = 'bold 12px ui-monospace, monospace';
    ctx.textAlign = 'left';
    ctx.textBaseline = 'bottom';
    ctx.fillText('STACK (bottom → top)', 20, 275);

    if (stackIndices.length === 0) {
        ctx.fillStyle = '#999';
        ctx.font = 'italic 14px sans-serif';
        ctx.textBaseline = 'middle';
        ctx.fillText('(empty)', 30, 300);
    } else {
        const sCellW = Math.min(60, (canvas.width - 80) / stackIndices.length);
        const sY = 285;
        for (let i = 0; i < stackIndices.length; i++) {
            const idx = stackIndices[i];
            const x = 30 + i * sCellW;
            ctx.fillStyle = '#e8f5e9';
            ctx.strokeStyle = '#43a047';
            ctx.lineWidth = 1.5;
            ctx.fillRect(x, sY, sCellW, cellH);
            ctx.strokeRect(x, sY, sCellW, cellH);

            ctx.fillStyle = '#222';
            ctx.font = '16px ui-monospace, monospace';
            ctx.textAlign = 'center';
            ctx.textBaseline = 'middle';
            ctx.fillText(values[idx], x + sCellW / 2, sY + cellH / 2);

            ctx.fillStyle = '#666';
            ctx.font = '10px ui-monospace, monospace';
            ctx.textBaseline = 'top';
            ctx.fillText('[' + idx + ']', x + sCellW / 2, sY + cellH + 4);
        }
    }
}

function drawIntervals(data) {
    const input    = data.input;
    const statuses = data.statuses;
    const merged   = data.merged || [];
    const running  = data.running;
    const maxCoord = data.maxCoord || 20;

    const xStart = 60;
    const xEnd = canvas.width - 40;
    const displayMax = maxCoord + 2;
    const scale = (xEnd - xStart) / displayMax;
    const x = c => xStart + c * scale;

    const rowH = 22;
    const rowGap = 4;

    ctx.strokeStyle = '#ececec';
    ctx.lineWidth = 1;
    for (let c = 0; c <= displayMax; c += 5) {
        const tx = x(c);
        ctx.beginPath();
        ctx.moveTo(tx, 40);
        ctx.lineTo(tx, 340);
        ctx.stroke();
    }

    ctx.fillStyle = '#555';
    ctx.font = 'bold 12px ui-monospace, monospace';
    ctx.textAlign = 'left';
    ctx.textBaseline = 'bottom';
    ctx.fillText('INPUT', 20, 40);

    const inputStartY = 55;
    for (let i = 0; i < input.length; i++) {
        const iv = input[i];
        const st = statuses[i];
        const y = inputStartY + i * (rowH + rowGap);

        let fill, stroke;
        switch (st) {
            case 'current': fill = '#ffe082'; stroke = '#f57c00'; break;
            case 'next':    fill = '#90caf9'; stroke = '#1976d2'; break;
            case 'merged':  fill = '#c8e6c9'; stroke = '#43a047'; break;
            default:        fill = '#f5f5f5'; stroke = '#aaa';   break;
        }
        const x1 = x(iv[0]), x2 = x(iv[1]);
        ctx.fillStyle = fill;
        ctx.strokeStyle = stroke;
        ctx.lineWidth = 1.5;
        ctx.fillRect(x1, y, x2 - x1, rowH);
        ctx.strokeRect(x1, y, x2 - x1, rowH);

        ctx.fillStyle = '#222';
        ctx.font = '11px ui-monospace, monospace';
        ctx.textAlign = 'center';
        ctx.textBaseline = 'middle';
        const label = `[${iv[0]},${iv[1]}]`;
        if (x2 - x1 > ctx.measureText(label).width + 8) {
            ctx.fillText(label, (x1 + x2) / 2, y + rowH / 2);
        }
    }

    const outputStartY = 200;
    ctx.fillStyle = '#555';
    ctx.font = 'bold 12px ui-monospace, monospace';
    ctx.textAlign = 'left';
    ctx.textBaseline = 'bottom';
    ctx.fillText('OUTPUT (merged so far)', 20, outputStartY - 12);

    let rowIdx = 0;
    for (const iv of merged) {
        drawOutputBar(iv, outputStartY + rowIdx * (rowH + rowGap),
            '#c8e6c9', '#43a047', rowH, x);
        rowIdx++;
    }
    if (running) {
        drawOutputBar(running, outputStartY + rowIdx * (rowH + rowGap),
            '#ffe082', '#f57c00', rowH, x);
    }

    const axisY = 360;
    ctx.strokeStyle = '#666';
    ctx.lineWidth = 1.5;
    ctx.beginPath();
    ctx.moveTo(xStart, axisY);
    ctx.lineTo(xEnd, axisY);
    ctx.stroke();

    ctx.fillStyle = '#666';
    ctx.font = '11px ui-monospace, monospace';
    ctx.textAlign = 'center';
    ctx.textBaseline = 'top';
    for (let c = 0; c <= displayMax; c += 5) {
        const tx = x(c);
        ctx.beginPath();
        ctx.moveTo(tx, axisY);
        ctx.lineTo(tx, axisY + 5);
        ctx.stroke();
        ctx.fillText(c, tx, axisY + 8);
    }
}

function drawOutputBar(iv, y, fill, stroke, rowH, x) {
    const x1 = x(iv[0]), x2 = x(iv[1]);
    ctx.fillStyle = fill;
    ctx.strokeStyle = stroke;
    ctx.lineWidth = 1.5;
    ctx.fillRect(x1, y, x2 - x1, rowH);
    ctx.strokeRect(x1, y, x2 - x1, rowH);

    ctx.fillStyle = '#222';
    ctx.font = '11px ui-monospace, monospace';
    ctx.textAlign = 'center';
    ctx.textBaseline = 'middle';
    ctx.fillText(`[${iv[0]},${iv[1]}]`, (x1 + x2) / 2, y + rowH / 2);
}

function drawHeap(data) {
    const values     = data.values;
    const freqs      = data.frequencies;
    const currentIdx = data.currentIdx ?? -1;
    const heapVals   = data.heapValues || [];
    const k          = data.k;
    const n          = values.length;

    const maxFreq = Math.max(...freqs, 1);
    const chartY = 100;
    const chartH = 220;
    const barW = 90;
    const barGap = 60;
    const totalW = n * barW + (n - 1) * barGap;
    const startX = (canvas.width - totalW) / 2;

    const heapSet = new Set(heapVals);

    ctx.fillStyle = '#555';
    ctx.font = 'bold 14px ui-monospace, monospace';
    ctx.textAlign = 'left';
    ctx.textBaseline = 'top';
    ctx.fillText(`Top ${k} Frequent Elements`, 20, 20);

    const baselineY = chartY + chartH;
    ctx.strokeStyle = '#ccc';
    ctx.lineWidth = 1;
    ctx.beginPath();
    ctx.moveTo(20, baselineY);
    ctx.lineTo(canvas.width - 20, baselineY);
    ctx.stroke();

    for (let i = 0; i < n; i++) {
        const x = startX + i * (barW + barGap);
        const freq = freqs[i];
        const height = (freq / maxFreq) * chartH;
        const barY = baselineY - height;
        const inHeap = heapSet.has(values[i]);
        const isCurrent = i === currentIdx;

        ctx.fillStyle = inHeap ? '#66bb6a' : '#e0e0e0';
        ctx.fillRect(x, barY, barW, height);

        ctx.strokeStyle = isCurrent ? '#f57c00'
            : inHeap   ? '#2e7d32'
                :            '#999';
        ctx.lineWidth   = isCurrent ? 3 : 1.5;
        ctx.strokeRect(x, barY, barW, height);

        ctx.fillStyle = inHeap ? '#fff' : '#666';
        ctx.font = 'bold 20px ui-monospace, monospace';
        ctx.textAlign = 'center';
        ctx.textBaseline = 'middle';
        ctx.fillText(String(freq), x + barW / 2, barY + height / 2);

        ctx.fillStyle = '#222';
        ctx.font = 'bold 18px ui-monospace, monospace';
        ctx.textBaseline = 'bottom';
        ctx.fillText(String(values[i]), x + barW / 2, barY - 6);

        ctx.fillStyle = '#888';
        ctx.font = '11px ui-monospace, monospace';
        ctx.textBaseline = 'top';
        ctx.fillText(`idx ${i}`, x + barW / 2, baselineY + 6);
    }
}

function drawTree(data) {
    const tree         = data.tree;
    const visitedOrder = data.visitedOrder || [];
    const currentVal   = data.currentVal ?? -1;
    const visitedSet   = new Set(visitedOrder);

    if (!tree) {
        ctx.fillStyle = '#bbb';
        ctx.font = '16px sans-serif';
        ctx.textAlign = 'center';
        ctx.textBaseline = 'middle';
        ctx.fillText('(empty tree)', canvas.width / 2, canvas.height / 2);
        return;
    }

    const positions = {};
    let xCounter = 0;
    let maxDepth = 0;

    function walk(node, depth) {
        if (!node) return;
        walk(node.left, depth + 1);
        positions[node.val] = [xCounter++, depth];
        maxDepth = Math.max(maxDepth, depth);
        walk(node.right, depth + 1);
    }
    walk(tree, 0);

    const maxX = xCounter - 1;
    const padX = 70;
    const padTop = 70;
    const padBottom = 100;

    const cellW = (canvas.width - 2 * padX) / Math.max(maxX + 1, 1);
    const cellH = (canvas.height - padTop - padBottom) / Math.max(maxDepth, 1);
    const radius = Math.min(24, cellW / 3, cellH / 2.5);

    const rootX = padX + positions[tree.val][0] * cellW + cellW / 2;
    const offsetX = (canvas.width / 2) - rootX;

    const px = val => padX + positions[val][0] * cellW + cellW / 2 + offsetX;
    const py = val => padTop + positions[val][1] * cellH;

    ctx.strokeStyle = '#bbb';
    ctx.lineWidth = 1.5;
    function drawEdges(node) {
        if (!node) return;
        const x = px(node.val), y = py(node.val);
        if (node.left) {
            ctx.beginPath();
            ctx.moveTo(x, y);
            ctx.lineTo(px(node.left.val), py(node.left.val));
            ctx.stroke();
            drawEdges(node.left);
        }
        if (node.right) {
            ctx.beginPath();
            ctx.moveTo(x, y);
            ctx.lineTo(px(node.right.val), py(node.right.val));
            ctx.stroke();
            drawEdges(node.right);
        }
    }
    drawEdges(tree);

    function drawNodes(node) {
        if (!node) return;
        const x = px(node.val), y = py(node.val);
        const isVisited = visitedSet.has(node.val);
        const isCurrent = node.val === currentVal;

        ctx.beginPath();
        ctx.arc(x, y, radius, 0, Math.PI * 2);
        if (isCurrent)      ctx.fillStyle = '#ffe082';
        else if (isVisited) ctx.fillStyle = '#c8e6c9';
        else                ctx.fillStyle = '#fff';
        ctx.fill();

        ctx.strokeStyle = isCurrent ? '#f57c00' : isVisited ? '#43a047' : '#bbb';
        ctx.lineWidth   = (isCurrent || isVisited) ? 2.5 : 1.5;
        ctx.stroke();

        ctx.fillStyle = '#222';
        ctx.font = `bold ${Math.floor(radius * 1.1)}px ui-monospace, monospace`;
        ctx.textAlign = 'center';
        ctx.textBaseline = 'middle';
        ctx.fillText(String(node.val), x, y);

        drawNodes(node.left);
        drawNodes(node.right);
    }
    drawNodes(tree);

    const footerY = canvas.height - 30;
    ctx.fillStyle = '#555';
    ctx.font = 'bold 13px ui-monospace, monospace';
    ctx.textAlign = 'center';
    ctx.textBaseline = 'middle';
    const orderStr = visitedOrder.length === 0 ? '(none)' : visitedOrder.join(' → ');
    ctx.fillText(`visited: ${orderStr}`, canvas.width / 2, footerY);
}

function drawPath(data) {
    const n           = data.n;
    const queens      = data.queens || [];
    const currentRow  = data.currentRow  ?? -1;
    const currentCol  = data.currentCol  ?? -1;
    const rejectedRow = data.rejectedRow ?? -1;
    const rejected    = new Set(data.rejected || []);

    const solved = queens.length === n && queens.every(q => q >= 0);

    const cellSize = Math.min(70,
        (canvas.width  - 200) / n,
        (canvas.height - 160) / n);
    const boardW = cellSize * n;
    const boardH = cellSize * n;
    const startX = (canvas.width - boardW) / 2;
    const startY = 60;

    ctx.fillStyle = solved ? '#e8f5e9' : '#fafafa';
    ctx.fillRect(startX - 2, startY - 2, boardW + 4, boardH + 4);
    ctx.strokeStyle = solved ? '#43a047' : '#ccc';
    ctx.lineWidth = solved ? 2.5 : 1.5;
    ctx.strokeRect(startX - 2, startY - 2, boardW + 4, boardH + 4);

    for (let r = 0; r < n; r++) {
        for (let c = 0; c < n; c++) {
            const x = startX + c * cellSize;
            const y = startY + r * cellSize;
            const isCurrent  = r === currentRow && c === currentCol;
            const isRejected = r === rejectedRow && rejected.has(c);

            ctx.fillStyle = (r + c) % 2 === 0 ? '#ffffff' : '#f0f0f0';
            if (isRejected) ctx.fillStyle = '#ffcdd2';
            if (isCurrent)  ctx.fillStyle = '#ffe082';
            if (solved)     ctx.fillStyle = (r + c) % 2 === 0 ? '#c8e6c9' : '#b2dfb6';

            ctx.fillRect(x, y, cellSize, cellSize);

            ctx.strokeStyle = isCurrent ? '#f57c00' : '#ccc';
            ctx.lineWidth   = isCurrent ? 3 : 1;
            ctx.strokeRect(x + 0.5, y + 0.5, cellSize - 1, cellSize - 1);

            if (queens[r] === c) {
                const cx = x + cellSize / 2;
                const cy = y + cellSize / 2;
                const rr = cellSize * 0.26;

                ctx.beginPath();
                ctx.arc(cx, cy, rr, 0, Math.PI * 2);
                ctx.fillStyle = solved ? '#1b5e20' : '#2e7d32';
                ctx.fill();
                ctx.strokeStyle = '#1b5e20';
                ctx.lineWidth = 2;
                ctx.stroke();

                ctx.fillStyle = '#fff';
                ctx.font = `bold ${Math.floor(cellSize * 0.36)}px serif`;
                ctx.textAlign = 'center';
                ctx.textBaseline = 'middle';
                ctx.fillText('♛', cx, cy + 1);
            }
        }
    }

    ctx.fillStyle = '#888';
    ctx.font = '11px ui-monospace, monospace';
    ctx.textAlign = 'center';
    ctx.textBaseline = 'top';
    for (let c = 0; c < n; c++) {
        ctx.fillText(c, startX + c * cellSize + cellSize / 2, startY + boardH + 6);
    }
    ctx.textAlign = 'right';
    ctx.textBaseline = 'middle';
    for (let r = 0; r < n; r++) {
        ctx.fillText(r, startX - 8, startY + r * cellSize + cellSize / 2);
    }

    if (rejectedRow !== -1 && rejected.size > 0) {
        const cols = Array.from(rejected).sort((a, b) => a - b);
        ctx.fillStyle = '#c62828';
        ctx.font = '12px ui-monospace, monospace';
        ctx.textAlign = 'left';
        ctx.textBaseline = 'top';
        ctx.fillText(`rejected in row ${rejectedRow}: cols ${cols.join(', ')}`,
            20, canvas.height - 28);
    }
}

function drawPlaceholder(name, data) {
    ctx.fillStyle = '#bbb';
    ctx.font = '16px sans-serif';
    ctx.textAlign = 'center';
    ctx.textBaseline = 'middle';
    ctx.fillText(`[${name} renderer not yet implemented]`,
        canvas.width / 2, canvas.height / 2 - 12);
    ctx.font = '12px ui-monospace, monospace';
    ctx.fillText(JSON.stringify(data).slice(0, 120),
        canvas.width / 2, canvas.height / 2 + 16);
}

const renderers = {
    'array+pointers': drawArrayWithPointers,
    'grid':           drawGrid,
    'stack':          drawStack,
    'intervals':      drawIntervals,
    'heap':           drawHeap,
    'tree':           drawTree,
    'path':           drawPath,
};

// Initialize
patternSel.value = "two-pointers";
loadPattern();