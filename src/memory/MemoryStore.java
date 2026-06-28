package memory;

import java.io.IOException;

import model.Observation;

public interface MemoryStore {

    void save(Observation observation) throws IOException;
}
