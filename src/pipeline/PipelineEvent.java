package pipeline;

import java.util.Objects;
import java.util.UUID;
import com.google.gson.annotations.SerializedName;

/** Un appiglio immutabile: ogni passaggio conserva filo, contesto e genitore. */
public record PipelineEvent(
        @SerializedName("flow_id") String flowId,
        @SerializedName("event_id") String eventId,
        @SerializedName("parent_event_id") String parentEventId,
        @SerializedName("context_ref") String contextRef,
        Kind kind) {
    public enum Kind { CONTEXT, TRAIN, INFER, RESULT, IA }

    public PipelineEvent {
        if (flowId == null || flowId.isBlank() || eventId == null || eventId.isBlank()
                || contextRef == null || contextRef.isBlank())
            throw new IllegalArgumentException("Filo, evento e contesto sono obbligatori");
        Objects.requireNonNull(kind, "kind");
        if (kind != Kind.CONTEXT && (parentEventId == null || parentEventId.isBlank()))
            throw new IllegalArgumentException("Un passaggio deve avere un evento genitore");
    }

    public static PipelineEvent start(String contextRef) {
        return new PipelineEvent(id(), id(), null, contextRef, Kind.CONTEXT);
    }

    public PipelineEvent next(Kind kind) {
        return new PipelineEvent(flowId, id(), eventId, contextRef, kind);
    }

    private static String id() { return UUID.randomUUID().toString(); }
}
