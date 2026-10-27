package com.vbforge.visual;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

/**
 * DSA Patterns // Visual – entry point.
 *
 * <pre>
 * java -jar dsa-visual.jar                    # http://localhost:8080
 * java -jar dsa-visual.jar --port 9090
 * java -jar dsa-visual.jar --host 0.0.0.0     # expose on the network (Docker, LAN)
 * PORT=9090 HOST=0.0.0.0 java -jar dsa-visual.jar
 * </pre>
 */
public class Main {

    public static void main(String[] args) throws IOException {
        int port = envInt("PORT", 8080);
        String host = System.getenv().getOrDefault("HOST", "");

        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--port" -> port = Integer.parseInt(args[++i]);
                case "--host" -> host = args[++i];
                case "-h", "--help" -> {
                    System.out.println("usage: java -jar dsa-visual.jar [--port 8080] [--host 127.0.0.1]");
                    return;
                }
                default -> {
                    System.err.println("unknown argument: " + args[i]);
                    System.exit(2);
                }
            }
        }

        InetAddress bind = host.isBlank() ? InetAddress.getLoopbackAddress() : InetAddress.getByName(host);
        HttpServer server = HttpServer.create(new InetSocketAddress(bind, port), 0);
        // one virtual thread per request: SSE streams may park for a long time
        server.setExecutor(Executors.newVirtualThreadPerTaskExecutor());

        Api api = new Api();
        server.createContext("/", new StaticHandler());
        server.createContext("/api/catalog", api::catalog);
        server.createContext("/api/health", api::health);
        server.createContext("/api/stream", api::stream);
        server.createContext("/api/control", api::control);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> server.stop(1), "shutdown"));
        server.start();

        System.out.println("""

                  DSA // PATTERNS // VISUAL   v%s
                  ----------------------------------------
                  online  >  http://%s:%d
                  traces  >  %d algorithms loaded
                  stop    >  Ctrl+C
                """.formatted(Api.VERSION,
                bind.isAnyLocalAddress() ? "localhost" : bind.getHostAddress(),
                port, PatternRunner.catalog().size()));
    }

    private static int envInt(String name, int fallback) {
        try {
            String v = System.getenv(name);
            return v == null ? fallback : Integer.parseInt(v.trim());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }
}
