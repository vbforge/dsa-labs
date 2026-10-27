package com.vbforge.visual;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The JSON / SSE API.
 *
 * <pre>
 * GET  /api/catalog                      every algorithm + its input schema
 * GET  /api/health                       liveness, version, live sessions
 * GET  /api/stream?algo=&amp;params=&amp;delay=&amp;mode=play|step      SSE trace
 * POST /api/control?session=&amp;cmd=pause|resume|step|stop|delay[&amp;value=ms]
 * </pre>
 *
 * SSE events: {@code session} (id), {@code frame}, {@code done}, {@code fail}.
 * All stream-level problems (unknown algorithm, bad input …) are reported as a
 * {@code fail} event because {@code EventSource} cannot read HTTP error bodies.
 */
final class Api {

    static final String VERSION = "1.0.0";
    static final int MAX_FRAMES = 6_000;
    static final int MAX_SESSIONS = 32;

    private final ConcurrentHashMap<String, TraceSession> sessions = new ConcurrentHashMap<>();
    private final long startedAt = System.currentTimeMillis();

    /** Thrown from the emit callback when a trace exceeds {@link #MAX_FRAMES}. */
    private static final class FrameLimit extends RuntimeException {
        private static final long serialVersionUID = 1L;

        FrameLimit() {
            super("frame limit", null, false, false);
        }
    }

    // ----------------------------------------------------------------- simple

    void catalog(HttpExchange ex) throws IOException {
        if (!Http.requireMethod(ex, "GET")) return;
        Http.json(ex, 200, PatternRunner.catalog());
    }

    void health(HttpExchange ex) throws IOException {
        if (!Http.requireMethod(ex, "GET")) return;
        Http.json(ex, 200, Map.of(
                "status", "ok",
                "version", VERSION,
                "uptimeSeconds", (System.currentTimeMillis() - startedAt) / 1000,
                "liveSessions", sessions.size(),
                "algorithms", PatternRunner.catalog().size()));
    }

    void control(HttpExchange ex) throws IOException {
        if (!Http.requireMethod(ex, "POST")) return;
        ex.getRequestBody().readAllBytes();
        Map<String, String> q = Http.query(ex);
        TraceSession s = sessions.get(q.getOrDefault("session", ""));
        if (s == null) {
            Http.json(ex, 404, Map.of("error", "unknown or finished session"));
            return;
        }
        switch (q.getOrDefault("cmd", "")) {
            case "pause"  -> s.pause();
            case "resume" -> s.resume();
            case "step"   -> s.step();
            case "stop"   -> s.stop();
            case "delay"  -> s.delay(Http.parseLong(q.get("value"), 500));
            default -> {
                Http.json(ex, 400, Map.of("error", "unknown cmd"));
                return;
            }
        }
        Http.json(ex, 200, Map.of("ok", true));
    }

    // -------------------------------------------------------------------- SSE

    void stream(HttpExchange ex) throws IOException {
        if (!Http.requireMethod(ex, "GET")) return;
        Map<String, String> q = Http.query(ex);

        ex.getResponseHeaders().set("Content-Type", "text/event-stream; charset=utf-8");
        ex.getResponseHeaders().set("Cache-Control", "no-cache, no-transform");
        ex.getResponseHeaders().set("X-Accel-Buffering", "no");
        ex.sendResponseHeaders(200, 0); // chunked

        try (OutputStream out = ex.getResponseBody()) {
            runStream(out, q);
        } catch (IOException | UncheckedIOException ignored) {
            // client closed the connection – nothing left to tell it
        }
    }

    private void runStream(OutputStream out, Map<String, String> q) {
        TraceSession session = null;
        long t0 = System.nanoTime();
        int[] count = {0};
        try {
            if (sessions.size() >= MAX_SESSIONS)
                throw new IllegalArgumentException("server busy – too many live traces, try again shortly");

            Algorithm algo = PatternRunner.find(q.get("algo"));
            Params params = Params.parse(q.get("params"));
            session = new TraceSession(UUID.randomUUID().toString().replace("-", ""),
                    Http.parseLong(q.get("delay"), 500), "step".equals(q.get("mode")));
            sessions.put(session.id(), session);
            send(out, "session", Map.of("id", session.id(), "algo", algo.meta().id(), "maxFrames", MAX_FRAMES));

            final TraceSession s = session;
            algo.trace(params, frame -> {
                if (!s.awaitTurn(() -> ping(out))) throw new TraceSession.Cancelled();
                if (count[0] >= MAX_FRAMES) throw new FrameLimit();
                send(out, "frame", frame);
                count[0]++;
                s.pace();
            });
            send(out, "done", done(count[0], t0, false, false));

        } catch (TraceSession.Cancelled e) {
            quietly(() -> send(out, "done", done(count[0], t0, false, true)));
        } catch (FrameLimit e) {
            quietly(() -> send(out, "done", done(count[0], t0, true, false)));
        } catch (UncheckedIOException e) {
            // client vanished mid-trace
        } catch (IllegalArgumentException e) {
            quietly(() -> send(out, "fail", Map.of("message", String.valueOf(e.getMessage()))));
        } catch (RuntimeException e) {
            e.printStackTrace();
            quietly(() -> send(out, "fail", Map.of("message", "internal error: " + e.getClass().getSimpleName())));
        } finally {
            if (session != null) sessions.remove(session.id());
        }
    }

    private static Map<String, Object> done(int frames, long t0, boolean truncated, boolean stopped) {
        return Map.of("frames", frames, "ms", (System.nanoTime() - t0) / 1_000_000,
                "truncated", truncated, "stopped", stopped);
    }

    private static void send(OutputStream out, String event, Object payload) {
        try {
            out.write(("event: " + event + "\ndata: " + Http.GSON.toJson(payload) + "\n\n")
                    .getBytes(StandardCharsets.UTF_8));
            out.flush();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static void ping(OutputStream out) {
        try {
            out.write(": ping\n\n".getBytes(StandardCharsets.UTF_8));
            out.flush();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static void quietly(Runnable r) {
        try {
            r.run();
        } catch (RuntimeException ignored) {
            // connection already gone
        }
    }
}
