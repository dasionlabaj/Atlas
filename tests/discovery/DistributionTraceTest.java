package discovery;

import java.time.Instant;
import java.util.Map;
import java.util.Set;

/** Test senza rete, account, tracker o dati personali. */
public final class DistributionTraceTest {
    private static void check(boolean condition, String name) {
        if (!condition) throw new AssertionError(name);
    }

    private static void rejected(Runnable action, String name) {
        try {
            action.run();
            throw new AssertionError("Doveva fallire: " + name);
        } catch (IllegalArgumentException expected) {
            // valido
        }
    }

    public static void main(String[] args) {
        DistributionTrace trace = new DistributionTrace();
        Instant when = Instant.parse("2026-10-28T08:00:00Z");
        DistributionTrace.Record pushed = new DistributionTrace.Record(
            "atlas-demo", DistributionTrace.Stage.GITHUB_PUSH,
            DistributionTrace.Channel.GITHUB,
            DistributionTrace.Basis.SOURCE_REFERENCED,
            "https://github.com/example/atlas/commit/abcdef", when, "Push riportato");

        check(trace.append(pushed), "Nuova evidenza");
        check(!trace.append(pushed), "De-duplicazione");
        Set<DistributionTrace.Stage> recorded = trace.referencedStages("atlas-demo");
        check(recorded.contains(DistributionTrace.Stage.GITHUB_PUSH), "Push documentato");
        check(!recorded.contains(DistributionTrace.Stage.WEB_SEARCH_RESULT),
                "Push non implica indicizzazione web");
        check(!recorded.contains(DistributionTrace.Stage.DIRECT_RESPONSE),
                "Push non implica contatto con qualcuno");

        trace.append(new DistributionTrace.Record(
            "atlas-demo", DistributionTrace.Stage.WEB_SEARCH_RESULT,
            DistributionTrace.Channel.WEB_SEARCH, DistributionTrace.Basis.HYPOTHESIS,
            "", when, "Potrebbe comparire in una ricerca"));
        check(!trace.referencedStages("atlas-demo").contains(
                DistributionTrace.Stage.WEB_SEARCH_RESULT), "Ipotesi non diventa fatto");

        trace.append(new DistributionTrace.Record(
            "atlas-demo", DistributionTrace.Stage.PAID_IMPRESSION,
            DistributionTrace.Channel.PAID_CAMPAIGN,
            DistributionTrace.Basis.SOURCE_REFERENCED,
            "https://example.org/campaign/report", when, "Report campagna"));
        Map<DistributionTrace.Stage, Integer> sources = trace.counts(
            "atlas-demo", DistributionTrace.Basis.SOURCE_REFERENCED);
        check(sources.get(DistributionTrace.Stage.PAID_IMPRESSION) == 1,
                "Canale pubblicitario separato");
        check(!trace.referencedStages("atlas-demo").contains(
                DistributionTrace.Stage.DIRECT_RESPONSE), "Impression non implica risposta");

        rejected(() -> new DistributionTrace.Record(
            "demo", DistributionTrace.Stage.GITHUB_PUSH,
            DistributionTrace.Channel.DIRECT, DistributionTrace.Basis.HYPOTHESIS,
            "", when, ""), "Stadio/canale incompatibili");
        rejected(() -> new DistributionTrace.Record(
            "demo", DistributionTrace.Stage.GITHUB_METADATA,
            DistributionTrace.Channel.GITHUB, DistributionTrace.Basis.SOURCE_REFERENCED,
            "", when, ""), "Fonte mancante");
        rejected(() -> new DistributionTrace.Record(
            "demo", DistributionTrace.Stage.GITHUB_METADATA,
            DistributionTrace.Channel.GITHUB, DistributionTrace.Basis.SOURCE_REFERENCED,
            "http://example.org", when, ""), "URL non HTTPS");
        check(trace.snapshot().size() == 3, "Registro immutato da rifiuti");
        try {
            trace.snapshot().clear();
            throw new AssertionError("Snapshot modificabile");
        } catch (UnsupportedOperationException expected) {
            // valido
        }

        System.out.println("PASS: provenienza, canali separati, deduplica e nessuna consegna inferita");
    }
}
