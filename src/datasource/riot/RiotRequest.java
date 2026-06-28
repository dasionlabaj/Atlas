package datasource.riot;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

class RiotRequest {
    private final HttpClient http = HttpClient.newHttpClient();
    private final String apiKey;

    RiotRequest(String apiKey) {
        this.apiKey = apiKey;
    }

    String get(String url) throws IOException {
        int retries = 5;
        while (true) {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("X-Riot-Token", apiKey)
                    .build();
                HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() == 429) {
                    if (retries-- <= 0) throw new IOException("Riot API 429 (rate limit esaurito) — " + url);
                    int waitSec = response.headers().firstValue("Retry-After")
                        .map(Integer::parseInt).orElse(10);
                    System.out.printf("  [rate limit] attendo %ds...%n", waitSec);
                    Thread.sleep(waitSec * 1000L + 500);
                    continue;
                }
                if (response.statusCode() != 200) {
                    throw new IOException("Riot API " + response.statusCode() + " — " + url);
                }
                return response.body();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IOException("Request interrupted", e);
            }
        }
    }
}
