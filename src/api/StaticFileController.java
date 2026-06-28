package api;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

/**
 * Serve i file statici della UI 0 dalla cartella web/.
 * "/" → index.html. Nient'altro: nessuna logica, è solo la finestra.
 *
 * Vive sotto il context "/" (il meno specifico): "/snapshot" ha un context
 * dedicato e ha la precedenza, quindi i dati non passano mai da qui.
 */
public class StaticFileController implements HttpHandler {

    private final Path baseDir;

    public StaticFileController(Path baseDir) {
        this.baseDir = baseDir.toAbsolutePath().normalize();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String rawPath = exchange.getRequestURI().getPath();
        String relative = "/".equals(rawPath) ? "index.html" : rawPath.substring(1);

        Path file = baseDir.resolve(relative).normalize();

        // Niente path traversal: il file deve restare dentro web/.
        if (!file.startsWith(baseDir) || !Files.exists(file) || Files.isDirectory(file)) {
            respond(exchange, 404, "text/plain; charset=utf-8", "not found".getBytes(StandardCharsets.UTF_8));
            return;
        }

        byte[] body = Files.readAllBytes(file);
        respond(exchange, 200, contentType(file), body);
    }

    private String contentType(Path file) {
        String name = file.getFileName().toString();
        if (name.endsWith(".html")) return "text/html; charset=utf-8";
        if (name.endsWith(".js"))   return "application/javascript; charset=utf-8";
        if (name.endsWith(".css"))  return "text/css; charset=utf-8";
        return "application/octet-stream";
    }

    private void respond(HttpExchange exchange, int status, String contentType, byte[] body) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", contentType);
        exchange.sendResponseHeaders(status, body.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(body);
        }
    }
}
