package resonance.model;

import java.util.List;

public class MatchMemory {

    public static final String SCHEMA = "resonance.match_memory.v0.1";

    public String schema;
    public CompletionStatus completion_status;
    public MatchInfo match;
    public String context_note;
    public SignalMap signals;
    public List<Anchor> anchors;
    public Reflection reflection;
    public List<String> unknowns;
    public List<Inference> inferences;

    public MatchMemory(String schema, CompletionStatus completionStatus, MatchInfo match, String contextNote,
                       SignalMap signals, List<Anchor> anchors, Reflection reflection,
                       List<String> unknowns, List<Inference> inferences) {
        this.schema = schema;
        this.completion_status = completionStatus;
        this.match = match;
        this.context_note = contextNote;
        this.signals = signals;
        this.anchors = anchors;
        this.reflection = reflection;
        this.unknowns = unknowns;
        this.inferences = inferences;
    }

    public static class MatchInfo {
        public String matchId;
        public String champion;
        public String role;
        public String result;

        public MatchInfo(String matchId, String champion, String role, String result) {
            this.matchId = matchId;
            this.champion = champion;
            this.role = role;
            this.result = result;
        }
    }

    public static class SignalMap {
        public String macro;
        public String micro;
        public String mental;

        public SignalMap(String macro, String micro, String mental) {
            this.macro = macro;
            this.micro = micro;
            this.mental = mental;
        }
    }

    public static class Anchor {
        public String time;
        public String type;
        public String text;
        public String source;
        public String certainty;

        public Anchor(String time, String type, String text, String source, String certainty) {
            this.time = time;
            this.type = type;
            this.text = text;
            this.source = source;
            this.certainty = certainty;
        }
    }

    public static class Reflection {
        public String what_remained;
        public String what_i_understood;
        public String what_i_learned;
        public String open_question;

        public Reflection(String whatRemained, String whatIUnderstood, String whatILearned, String openQuestion) {
            this.what_remained = whatRemained;
            this.what_i_understood = whatIUnderstood;
            this.what_i_learned = whatILearned;
            this.open_question = openQuestion;
        }
    }

    public static class Inference {
        public String text;
        public List<String> based_on;
        public String certainty;

        public Inference(String text, List<String> basedOn, String certainty) {
            this.text = text;
            this.based_on = basedOn;
            this.certainty = certainty;
        }
    }

    public static class CompletionStatus {
        public String api_facts;
        public String player_memory;
        public boolean ready_for_chat;

        public CompletionStatus(String apiFacts, String playerMemory, boolean readyForChat) {
            this.api_facts = apiFacts;
            this.player_memory = playerMemory;
            this.ready_for_chat = readyForChat;
        }

        public static CompletionStatus skeleton() {
            return new CompletionStatus("filled", "missing", false);
        }
    }

    public static MatchMemory skeleton(String matchId, String champion, String role, boolean win, String contextNote) {
        return new MatchMemory(
            SCHEMA,
            CompletionStatus.skeleton(),
            new MatchInfo(matchId, champion, role, win ? "WIN" : "LOSS"),
            contextNote,
            new SignalMap(null, null, null),
            new java.util.ArrayList<Anchor>(),
            new Reflection(null, null, null, null),
            new java.util.ArrayList<String>(),
            new java.util.ArrayList<Inference>()
        );
    }
}
