package com.vbforge.visual;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.regex.Pattern;

/** Serves the classpath folder {@code /web} (the UI) with conservative headers. */
final class StaticHandler implements HttpHandler {

    private static final Pattern SAFE_PATH = Pattern.compile("/[A-Za-z0-9_./-]*");
    private static final Map<String, String> TYPES = Map.of(
            "html", "text/html; charset=utf-8",
            "js",   "text/javascript; charset=utf-8",
            "css",  "text/css; charset=utf-8",
            "svg",  "image/svg+xml",
            "json", "application/json; charset=utf-8",
            "png",  "image/png",
            "ico",  "image/x-icon");

    private static final String CSP = "default-src 'self'; img-src 'self' data:; "
            + "style-src 'self' 'unsafe-inline'; base-uri 'none'; frame-ancestors 'none'";

    @Override
    public void handle(HttpExchange ex) throws IOException {
        String method = ex.getRequestMethod();
        boolean head = "HEAD".equals(method);
        if (!head && !"GET".equals(method)) {
            ex.getResponseHeaders().set("Allow", "GET, HEAD");
            ex.sendResponseHeaders(405, -1);
            return;
        }

        String path = ex.getRequestURI().getPath();
        if ("/".equals(path)) path = "/index.html";
        if (!SAFE_PATH.matcher(path).matches() || path.contains("..") || path.contains("//") || path.endsWith("/")) {
            notFound(ex);
            return;
        }

        try (InputStream in = StaticHandler.class.getResourceAsStream("/web" + path)) {
            if (in == null) {
                notFound(ex);
                return;
            }
            byte[] body = in.readAllBytes();
            String ext = path.substring(path.lastIndexOf('.') + 1);
            var h = ex.getResponseHeaders();
            h.set("Content-Type", TYPES.getOrDefault(ext, "application/octet-stream"));
            h.set("Cache-Control", "no-cache");
            h.set("X-Content-Type-Options", "nosniff");
            h.set("Referrer-Policy", "no-referrer");
            h.set("Content-Security-Policy", CSP);
            if (head) {
                h.set("Content-Length", String.valueOf(body.length));
                ex.sendResponseHeaders(200, -1);
                return;
            }
            ex.sendResponseHeaders(200, body.length);
            try (OutputStream out = ex.getResponseBody()) {
                out.write(body);
            }
        }
    }

    private static void notFound(HttpExchange ex) throws IOException {
        byte[] body = "404 // signal lost".getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", "text/plain; charset=utf-8");
        ex.sendResponseHeaders(404, body.length);
        try (OutputStream out = ex.getResponseBody()) {
            out.write(body);
        }
    }
}
