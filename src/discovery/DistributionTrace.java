package discovery;

import java.net.URI;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Registra in modo esplicito le tracce del percorso pubblico di un contenuto.
 *
 * Questa classe NON pubblica, NON interroga GitHub, NON esegue tracking,
 * NON identifica lettori e NON deduce ricezioni da un commit o da una ricerca.
 * Una referenza documenta la fonte dichiarata, ma non ne verifica l'autenticita'.
 */
public final class DistributionTrace {
    public static final int MAX_RECORDS = 512;

    public enum Channel {
        GITHUB, WEB_SEARCH, SITE_ANALYTICS, PAID_CAMPAIGN, DIRECT
    }

    public enum Stage {
        GITHUB_PUSH,
        GITHUB_METADATA,
        GITHUB_SEARCH_RESULT,
        WEB_SEARCH_RESULT,
        SITE_VIEW,
        PAID_IMPRESSION,
        DIRECT_RESPONSE
    }

    public enum Basis {
        HYPOTHESIS, SOURCE_REFERENCED
    }

    public static final class Record {
        private final String assetId;
        private final Stage stage;
        private final Channel channel;
        private final Basis basis;
        private final String reference;
        private final Instant observedAt;
        private final String note;

        public Record(String assetId, Stage stage, Channel channel, Basis basis,
                      String reference, Instant observedAt, String note) {
            if (assetId == null || !assetId.matches("[a-zA-Z0-9._:-]{1,64}")) {
                throw new IllegalArgumentException("ID di pubblicazione non valido");
            }
            this.stage = Objects.requireNonNull(stage, "stage");
            this.channel = Objects.requireNonNull(channel, "channel");
            this.basis = Objects.requireNonNull(basis, "basis");
            if (!compatible(stage, channel)) {
                throw new IllegalArgumentException("Canale incoerente con lo stadio");
            }
            this.observedAt = Objects.requireNonNull(observedAt, "observedAt");
            this.reference = reference == null ? "" : reference.trim();
            if (this.reference.length() > 512) {
                throw new IllegalArgumentException("Reference troppo lunga");
            }
            if (!this.reference.isEmpty() && !httpsUrl(this.reference)) {
                throw new IllegalArgumentException("La reference deve essere HTTPS");
            }
            if (basis == Basis.SOURCE_REFERENCED && this.reference.isEmpty()) {
                throw new IllegalArgumentException("Fonte richiesta per SOURCE_REFERENCED");
            }
            this.note = note == null ? "" : note.trim();
            if (this.note.length() > 240) {
                throw new IllegalArgumentException("Nota troppo lunga");
            }
            this.assetId = assetId;
        }

        public String assetId() { return assetId; }
        public Stage stage() { return stage; }
        public Channel channel() { return channel; }
        public Basis basis() { return basis; }
        public String reference() { return reference; }
        public Instant observedAt() { return observedAt; }
        public String note() { return note; }

        private String dedupKey() {
            return assetId + "\n" + stage + "\n" + channel + "\n"
                 + basis + "\n" + reference + "\n" + observedAt + "\n" + note;
        }
    }

    private final List<Record> records = new ArrayList<>();
    private final Set<String> dedupKeys = new HashSet<>();

    /** Restituisce false per un record identico gia' registrato (anti-duplicazione). */
    public synchronized boolean append(Record record) {
        Objects.requireNonNull(record, "record");
        String key = record.dedupKey();
        if (dedupKeys.contains(key)) return false;
        if (records.size() >= MAX_RECORDS) {
            throw new IllegalStateException("Capacita' del registro esaurita");
        }
        records.add(record);
        dedupKeys.add(key);
        return true;
    }

    public synchronized List<Record> snapshot() {
        return Collections.unmodifiableList(new ArrayList<>(records));
    }

    /**
     * Restituisce SOLO gli stadi con almeno una fonte esplicita dichiarata.
     * Nessuno stadio viene dedotto dalla presenza di un altro.
     * Questa non e' una verifica remota dell'URL o del suo contenuto.
     */
    public synchronized Set<Stage> referencedStages(String assetId) {
        EnumSet<Stage> found = EnumSet.noneOf(Stage.class);
        for (Record record : records) {
            if (record.assetId.equals(assetId)
                    && record.basis == Basis.SOURCE_REFERENCED) {
                found.add(record.stage);
            }
        }
        return Collections.unmodifiableSet(found);
    }

    /** Conta separatamente ipotesi e affermazioni accompagnate da una fonte. */
    public synchronized Map<Stage, Integer> counts(String assetId, Basis basis) {
        Objects.requireNonNull(basis, "basis");
        EnumMap<Stage, Integer> counts = new EnumMap<>(Stage.class);
        for (Record record : records) {
            if (record.assetId.equals(assetId) && record.basis == basis) {
                counts.merge(record.stage, 1, Integer::sum);
            }
        }
        return Collections.unmodifiableMap(counts);
    }

    private static boolean httpsUrl(String input) {
        try {
            URI uri = URI.create(input);
            return "https".equalsIgnoreCase(uri.getScheme())
                    && uri.getHost() != null && !uri.getHost().isEmpty()
                    && uri.getUserInfo() == null;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private static boolean compatible(Stage stage, Channel channel) {
        switch (stage) {
            case GITHUB_PUSH:
            case GITHUB_METADATA:
            case GITHUB_SEARCH_RESULT:
                return channel == Channel.GITHUB;
            case WEB_SEARCH_RESULT:
                return channel == Channel.WEB_SEARCH;
            case SITE_VIEW:
                return channel == Channel.SITE_ANALYTICS;
            case PAID_IMPRESSION:
                return channel == Channel.PAID_CAMPAIGN;
            case DIRECT_RESPONSE:
                return channel == Channel.DIRECT;
            default:
                return false;
        }
    }
}
