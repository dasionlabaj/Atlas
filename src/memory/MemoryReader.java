package memory;

import java.io.IOException;
import java.util.List;

import model.Observation;

public interface MemoryReader {

    List<Observation> readAll() throws IOException;
}
