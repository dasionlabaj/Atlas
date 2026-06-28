package resonance.writer;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import resonance.model.MatchMemory;

public class MatchMemoryWriter {

    private static final String DIR = "memory";
    private static final Gson GSON = new GsonBuilder()
        .setPrettyPrinting()
        .serializeNulls()
        .create();

    public String write(MatchMemory memory) throws IOException {
        new File(DIR).mkdirs();
        String path = DIR + "/" + memory.match.matchId + "_memory.json";
        try (FileWriter fw = new FileWriter(path)) {
            GSON.toJson(memory, fw);
        }
        return path;
    }
}
