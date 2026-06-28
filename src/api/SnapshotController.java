package api;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import model.snapshot.AtlasSnapshot;
import model.snapshot.RiotAccountKey;
import service.SnapshotService;

/**
 * Espone GET /snapshot.
 *
 * Solo lettura: il controller non ordina nulla al motore, lo interroga e basta.
 * Nessun POST /learn, nessun POST /think — quelli sarebbero comandi alla corteccia.
 */
public class SnapshotController implements HttpHandler {

    private final SnapshotService service;
    private final Gson gson;

    public SnapshotController(SnapshotService service, Gson gson) {
        this.service = service;
        this.gson = gson;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        // Una finestra web potrà leggere da un'origine diversa.
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");

        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            respond(exchange, 405, "{\"error\":\"method not allowed\"}");
            return;
        }

        try {
            AtlasSnapshot snapshot = resolve(exchange);
            respond(exchange, 200, gson.toJson(snapshot));
        } catch (IllegalArgumentException e) {
            respond(exchange, 400, "{\"error\":\"account key non valida\"}");
        } catch (RuntimeException e) {
            respond(exchange, 500, "{\"error\":\"snapshot non disponibile\"}");
        }
    }

    /**
     * Una porta, due indirizzi:
     *   /snapshot              → lo snapshot di default (current.json)
     *   /snapshot/{accountKey} → lo snapshot di quello specifico account
     */
    private AtlasSnapshot resolve(HttpExchange exchange) {
        String segment = accountSegment(exchange.getRequestURI().getPath());
        if (segment.isEmpty()) {
            return service.current();
        }
        return service.current(RiotAccountKey.fromPathSegment(segment));
    }

    /** Ciò che resta dopo "/snapshot/": "" per la porta di default, "daxs-euw" per un account. */
    private static String accountSegment(String path) {
        String prefix = "/snapshot";
        if (path.length() <= prefix.length() || path.charAt(prefix.length()) != '/') {
            return "";
        }
        String rest = path.substring(prefix.length() + 1);
        while (rest.endsWith("/")) {
            rest = rest.substring(0, rest.length() - 1);
        }
        return rest;
    }

    private void respond(HttpExchange exchange, int status, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }
}
