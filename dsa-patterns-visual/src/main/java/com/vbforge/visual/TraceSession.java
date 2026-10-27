package com.vbforge.visual;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Remote control for one live trace.
 *
 * <p>The trace runs on the SSE handler's thread and calls {@link #awaitTurn}
 * before every frame and {@link #pace} after it. Pausing therefore halts the
 * <em>computation itself</em> – not just the display – and {@link #step}
 * releases exactly one more frame while staying paused.
 *
 * <p>Uses a {@link ReentrantLock} rather than {@code synchronized}/{@code wait}
 * so that sessions parked on virtual threads never pin a carrier thread.
 */
final class TraceSession {

    /** Thrown inside the trace (via the emit callback) to unwind it. */
    static final class Cancelled extends RuntimeException {
        private static final long serialVersionUID = 1L;

        Cancelled() {
            super("trace cancelled", null, false, false);
        }
    }

    private static final long HEARTBEAT_MS = 15_000;
    private static final long IDLE_LIMIT_NS = TimeUnit.MINUTES.toNanos(15);
    static final long MIN_DELAY_MS = 0, MAX_DELAY_MS = 5_000;

    private final String id;
    private final ReentrantLock lock = new ReentrantLock();
    private final Condition changed = lock.newCondition();

    private boolean paused;
    private boolean stopped;
    private int tokens;
    private long delayMs;
    private long lastTouch = System.nanoTime();

    /** @param stepMode start paused, with the first frame already released */
    TraceSession(String id, long delayMs, boolean stepMode) {
        this.id = id;
        this.delayMs = clamp(delayMs);
        if (stepMode) {
            this.paused = true;
            this.tokens = 1;
        }
    }

    String id() {
        return id;
    }

    // ---------------------------------------------------------------- controls

    void pause()  { mutate(() -> paused = true); }
    void resume() { mutate(() -> { paused = false; tokens = 0; }); }
    void step()   { mutate(() -> { paused = true; tokens++; }); }
    void stop()   { mutate(() -> stopped = true); }
    void delay(long ms) { mutate(() -> delayMs = clamp(ms)); }

    private void mutate(Runnable change) {
        lock.lock();
        try {
            change.run();
            lastTouch = System.nanoTime();
            changed.signalAll();
        } finally {
            lock.unlock();
        }
    }

    // ------------------------------------------------------- trace-side hooks

    /**
     * Blocks while paused (sending a heartbeat every 15 s so a vanished client
     * is noticed). Returns false when the trace should stop.
     */
    boolean awaitTurn(Runnable heartbeat) {
        while (true) {
            lock.lock();
            try {
                if (stopped) return false;
                if (!paused) return true;
                if (tokens > 0) {
                    tokens--;
                    return true;
                }
                if (System.nanoTime() - lastTouch > IDLE_LIMIT_NS) {
                    stopped = true;
                    return false;
                }
                changed.await(HEARTBEAT_MS, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                stopped = true;
                return false;
            } finally {
                lock.unlock();
            }
            heartbeat.run(); // outside the lock – it does I/O
        }
    }

    /** Sleeps for the current frame delay unless paused / stopped / re-timed meanwhile. */
    void pace() {
        long start = System.nanoTime();
        lock.lock();
        try {
            while (!paused && !stopped) {
                long remaining = start + TimeUnit.MILLISECONDS.toNanos(delayMs) - System.nanoTime();
                if (remaining <= 0) return;
                changed.awaitNanos(remaining);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            stopped = true;
        } finally {
            lock.unlock();
        }
    }

    private static long clamp(long ms) {
        return Math.max(MIN_DELAY_MS, Math.min(MAX_DELAY_MS, ms));
    }
}
