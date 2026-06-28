package resonance.view;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import resonance.model.MatchMemory;

public class MatchMemoryPromptView {

    private static final Gson GSON = new GsonBuilder()
        .setPrettyPrinting()
        .serializeNulls()
        .create();

    public String render(MatchMemory memory) {
        return GSON.toJson(memory);
    }

    public void print(MatchMemory memory) {
        System.out.println("--- MATCH MEMORY ---");
        System.out.println(render(memory));
        System.out.println("--- END ---");
    }
}
