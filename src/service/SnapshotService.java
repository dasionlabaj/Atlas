package service;

import model.snapshot.AtlasSnapshot;
import model.snapshot.RiotAccountKey;
import reader.SnapshotReader;
import writer.SnapshotWriter;

/**
 * Espone lo stato consolidato all'API.
 *
 * Non costruisce più nulla: legge l'artefatto che il motore ha pubblicato dopo
 * Analizza / Allena. La lente guarda un file, non il motore vivo — il server può
 * partire anche quando il motore è spento.
 *
 * NOTA DI ROTTA: questo oggi *recupera* uno snapshot, non lo *serve*. Quando lo
 * storage andrà oltre il file (SQLite → Redis → Postgres), il nome naturale sarà
 * SnapshotRepository / SnapshotStore e SnapshotReader diventerà l'implementazione
 * file-based dietro un'interfaccia. La UI non deve accorgersene: il confine regge.
 */
public class SnapshotService {

    private final SnapshotReader reader = new SnapshotReader();

    /** Lo snapshot di default (compatibilità): l'ultimo pubblicato in current.json. */
    public AtlasSnapshot current() {
        return reader.read(SnapshotWriter.DEFAULT_PATH);
    }

    /**
     * Lo snapshot di uno specifico account. Se quell'account non è ancora stato
     * osservato il reader ritorna uno stato IDLE vuoto ma valido: la porta
     * risponde comunque, non è un errore.
     */
    public AtlasSnapshot current(RiotAccountKey accountKey) {
        return reader.read(SnapshotWriter.accountPath(accountKey));
    }
}
