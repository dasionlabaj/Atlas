package datasource.riot;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import model.MatchPerformance;

public class RiotApiClient {
    private final RiotRequest request;

    public RiotApiClient(String apiKey) {
        this.request = new RiotRequest(apiKey);
    }

    public String getPuuid(String gameName, String tagLine) throws IOException {
        String url = RiotEndpoints.ACCOUNT
            .replace("{gameName}", gameName)
            .replace("{tagLine}", tagLine);
        return extractStringValue(request.get(url), "puuid");
    }

    public List<String> getMatchIds(String puuid, int count) throws IOException {
        String url = RiotEndpoints.MATCH_IDS
            .replace("{puuid}", puuid)
            .replace("{count}", String.valueOf(count));
        return parseJsonStringArray(request.get(url));
    }

    public MatchPerformance getMatch(String matchId, String puuid) throws IOException {
        String url = RiotEndpoints.MATCH.replace("{matchId}", matchId);
        String json = request.get(url);
        double minutes = extractDoubleValue(json, "gameDuration") / 60.0;
        String participantJson = extractJsonObjectContainingKeyValue(json, "puuid", puuid);
        int cs = extractIntValue(participantJson, "totalMinionsKilled") +
                extractIntValue(participantJson, "neutralMinionsKilled");
        int damageDealt = extractIntValue(participantJson, "totalDamageDealtToChampions");
        boolean win = extractBoolValue(participantJson, "win");
        String champion = extractStringValue(participantJson, "championName");
        String role = extractStringValue(participantJson, "teamPosition");
        long gameStartTimestamp = extractLongValue(json, "gameStartTimestamp");
        return new MatchPerformance(
            matchId,
            extractIntValue(participantJson, "kills"),
            extractIntValue(participantJson, "deaths"),
            extractIntValue(participantJson, "assists"),
            cs,
            minutes,
            extractIntValue(participantJson, "visionScore"),
            damageDealt,
            win,
            champion,
            role,
            gameStartTimestamp);
    }

    private static String extractStringValue(String json, String key) {
        Pattern pattern = Pattern.compile("\"" + Pattern.quote(key) + "\"\\s*:\\s*\"((?:\\\\.|[^\\\\\"])*?)\"");
        Matcher matcher = pattern.matcher(json);
        if (matcher.find()) {
            return matcher.group(1);
        }
        throw new IllegalArgumentException("Impossibile trovare il valore stringa per la chiave: " + key);
    }

    private static int extractIntValue(String json, String key) {
        Pattern pattern = Pattern.compile("\"" + Pattern.quote(key) + "\"\\s*:\\s*(-?\\d+)");
        Matcher matcher = pattern.matcher(json);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        }
        throw new IllegalArgumentException("Impossibile trovare il valore numerico per la chiave: " + key);
    }

    private static long extractLongValue(String json, String key) {
        Pattern pattern = Pattern.compile("\"" + Pattern.quote(key) + "\"\\s*:\\s*(-?\\d+)");
        Matcher matcher = pattern.matcher(json);
        if (matcher.find()) {
            return Long.parseLong(matcher.group(1));
        }
        throw new IllegalArgumentException("Impossibile trovare il valore long per la chiave: " + key);
    }

    private static boolean extractBoolValue(String json, String key) {
        Pattern pattern = Pattern.compile("\"" + Pattern.quote(key) + "\"\\s*:\\s*(true|false)");
        Matcher matcher = pattern.matcher(json);
        if (matcher.find()) {
            return Boolean.parseBoolean(matcher.group(1));
        }
        throw new IllegalArgumentException("Impossibile trovare il valore booleano per la chiave: " + key);
    }

    private static double extractDoubleValue(String json, String key) {
        Pattern pattern = Pattern.compile("\"" + Pattern.quote(key) + "\"\\s*:\\s*(-?\\d+(?:\\.\\d+)?)");
        Matcher matcher = pattern.matcher(json);
        if (matcher.find()) {
            return Double.parseDouble(matcher.group(1));
        }
        throw new IllegalArgumentException("Impossibile trovare il valore numerico per la chiave: " + key);
    }

    private static List<String> parseJsonStringArray(String json) {
        int start = json.indexOf('[');
        int end = json.lastIndexOf(']');
        if (start < 0 || end < 0 || end <= start) {
            return new ArrayList<>();
        }
        List<String> values = new ArrayList<>();
        boolean inString = false;
        StringBuilder current = new StringBuilder();
        for (int i = start + 1; i < end; i++) {
            char c = json.charAt(i);
            if (c == '"' && !isEscaped(json, i)) {
                inString = !inString;
                if (!inString) {
                    values.add(current.toString());
                    current.setLength(0);
                }
                continue;
            }
            if (inString) {
                current.append(c);
            }
        }
        return values;
    }

    private static String extractJsonObjectContainingKeyValue(String json, String key, String value) {
        String needle = "\"" + key + "\":\"" + value + "\"";

        int infoPos = json.indexOf("\"info\":");
        if (infoPos < 0) throw new IllegalArgumentException("PUUID non trovato nella partita: " + value);
        int partPos = json.indexOf("\"participants\":[", infoPos);
        if (partPos < 0) throw new IllegalArgumentException("PUUID non trovato nella partita: " + value);

        int i = json.indexOf('[', partPos) + 1;
        while (i < json.length()) {
            while (i < json.length() && json.charAt(i) != '{' && json.charAt(i) != ']') i++;
            if (i >= json.length() || json.charAt(i) == ']') break;

            int end = findEnclosingObjectEnd(json, i);
            if (end < 0) break;
            String candidate = json.substring(i, end + 1);
            if (candidate.contains(needle)) return candidate;
            i = end + 1;
        }

        throw new IllegalArgumentException("PUUID non trovato nella partita: " + value);
    }

    private static int findEnclosingObjectEnd(String json, int index) {
        boolean inString = false;
        int depth = 0;
        for (int i = index; i < json.length(); i++) {
            char c = json.charAt(i);
            if (c == '"' && !isEscaped(json, i)) {
                inString = !inString;
                continue;
            }
            if (inString) {
                continue;
            }
            if (c == '{') {
                depth++;
            } else if (c == '}') {
                depth--;
                if (depth == 0) {
                    return i;
                }
            }
        }
        return -1;
    }

    private static boolean isEscaped(String json, int index) {
        int backslashes = 0;
        for (int i = index - 1; i >= 0 && json.charAt(i) == '\\'; i--) {
            backslashes++;
        }
        return backslashes % 2 == 1;
    }
}
