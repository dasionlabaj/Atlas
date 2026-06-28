package api;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.Path;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpServer;

import service.SnapshotService;

/**
 * La porta. L'unico confine tra il motore e qualsiasi finestra.
 *
 * Micro HTTP server nativo (com.sun.net.httpserver), nessun framework.
 * Espone solo letture dello stato consolidato; non sa nulla del frontend.
 */
public class AtlasHttpServer {

    public static final int DEFAULT_PORT = 8080;

    private final HttpServer server;

    public AtlasHttpServer(int port) throws IOException {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        SnapshotService snapshotService = new SnapshotService();

        this.server = HttpServer.create(new InetSocketAddress(port), 0);
        this.server.createContext("/snapshot", new SnapshotController(snapshotService, gson));
        this.server.createContext("/", new StaticFileController(Path.of("web"))); // UI 0
        this.server.setExecutor(null); // executor di default, sufficiente per uso locale
    }

    public void start() {
        server.start();
        InetSocketAddress addr = server.getAddress();
        System.out.println("Atlas in ascolto su http://localhost:" + addr.getPort());
        System.out.println("  GET /                     UI 0");
        System.out.println("  GET /snapshot             dati (default)");
        System.out.println("  GET /snapshot/<account>   dati di un account");
    }

    public void stop() {
        server.stop(0);
    }

    /** Avvio standalone: java -cp ... api.AtlasHttpServer */
    public static void main(String[] args) throws IOException {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : DEFAULT_PORT;
        new AtlasHttpServer(port).start();
    }
}
