package com.vbforge.visual;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

public class Main {

    private static final Gson GSON = new Gson();
    private static final int PORT = 8080;

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);

        server.createContext("/",          ctx -> serveResource(ctx, "/index.html", "text/html"));
        server.createContext("/app.js",    ctx -> serveResource(ctx, "/app.js",     "application/javascript"));
        server.createContext("/style.css", ctx -> serveResource(ctx, "/style.css",  "text/css"));
        server.createContext("/trace",     Main::handleTrace);

        server.start();
        System.out.println("Listening on http://localhost:" + PORT);
    }

    private static void serveResource(HttpExchange ctx, String path, String contentType)
            throws IOException {
        try (InputStream in = Main.class.getResourceAsStream(path)) {
            if (in == null) {
                ctx.sendResponseHeaders(404, -1);
                return;
            }
            byte[] body = in.readAllBytes();
            ctx.getResponseHeaders().add("Content-Type", contentType + "; charset=utf-8");
            ctx.sendResponseHeaders(200, body.length);
            try (OutputStream out = ctx.getResponseBody()) {
                out.write(body);
            }
        }
    }

    private static void handleTrace(HttpExchange ctx) throws IOException {
        try {
            String pattern = queryParam(ctx.getRequestURI().getQuery(), "pattern");
            if (pattern == null) {
                sendJson(ctx, 400, Map.of("error", "missing 'pattern' query parameter"));
                return;
            }
            List<Frame> frames = PatternRunner.run(pattern);
            sendJson(ctx, 200, frames);
        } catch (IllegalArgumentException e) {
            sendJson(ctx, 404, Map.of("error", e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            sendJson(ctx, 500, Map.of("error", "internal error: " + e.getMessage()));
        }
    }

    private static String queryParam(String query, String name) {
        if (query == null) return null;
        for (String pair : query.split("&")) {
            String[] kv = pair.split("=", 2);
            if (name.equals(kv[0])) return kv.length > 1 ? kv[1] : "";
        }
        return null;
    }

    private static void sendJson(HttpExchange ctx, int status, Object payload) throws IOException {
        byte[] body = GSON.toJson(payload).getBytes(StandardCharsets.UTF_8);
        ctx.getResponseHeaders().add("Content-Type", "application/json; charset=utf-8");
        ctx.sendResponseHeaders(status, body.length);
        try (OutputStream out = ctx.getResponseBody()) {
            out.write(body);
        }
    }
}