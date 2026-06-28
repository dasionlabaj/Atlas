# Atlas — Definizione del tronco

*Questo file non tiene il filo del lavoro: tiene l'identità del progetto. L'hub [atlas_state.md](atlas_state.md) risponde alla domanda «dove mi trovavo?». Questo file ne risponde a un'altra: «che cos'è Atlas?». Sono due domande diverse e vanno tenute in due luoghi diversi.*

*Tre categorie, separate di proposito. Un **principio** è una decisione fondativa — vero perché lo stabiliamo noi. Uno **stato** è ciò che è osservabile oggi — vero perché lo si verifica. Una **traiettoria** è ciò che Atlas potrebbe diventare — non ancora osservato, un'ipotesi viva e non una verità. Confondere le tre è esattamente l'errore che questo file esiste per impedire: non lasciare che una decisione si travesta da osservazione, né che una traiettoria venga scambiata per stato.*

---

> **Atlas è un nucleo riflessivo che aggiorna il proprio stato esclusivamente a partire dalle osservazioni che riceve.**
>
> Questa frase non dipende da Java, da Riot o da League of Legends: descrive l'identità, non l'incarnazione. È già vera oggi ed è verificabile nel progetto — ogni nuovo stato deriva da osservazioni entrate nel sistema, non da conoscenza inventata.

---

## Principi — decisioni fondative

*Veri perché il progetto li ha scelti. Non si verificano: si rispettano, o si cambiano in modo consapevole.*

- **Atlas non pretende conoscenza non osservata.** Non sa ciò che non ha visto entrare.
- **Il refresh è il confine di sincronizzazione con il mondo.** Non è un dettaglio della UI: è la soglia entro cui Atlas può aggiornare il proprio stato. Il pulsante «Aggiorna» è una delle possibili implementazioni del refresh, non il concetto.
- **La UI è una superficie, non il concetto.** Una delle incarnazioni possibili, non l'identità.
- **La coscienza non è un obiettivo progettuale.** Atlas non punta a costruire una coscienza artificiale. Se un fenomeno simile mai emergesse, sarebbe una traiettoria da osservare — non un requisito da soddisfare.

---

## Stato attuale — osservabile oggi

*Veri perché verificabili nel progetto, adesso — non perché il documento lo afferma. Ogni riga porta un'**àncora osservativa**: un luogo in cui la frase può essere rimessa alla prova. Non conta dove punti (oggi un file Java, domani un endpoint, fra un anno un test automatico): conta che quel luogo esista. È questo che preserva il significato della parola «stato» — se la verifica fosse «fidati del documento», la sezione avrebbe già perso la propria natura.*

> **Ultima verifica osservativa: 2026-06-26.**
> Non la data in cui la sezione è stata scritta, ma l'ultima volta che le sue righe sono state rimesse alla prova contro il progetto. È il *refresh rate* del documento: non pretende di essere eternamente vero, dichiara quando è stato sincronizzato l'ultima volta. Chi ri-verifica aggiorna questa data — ed è l'unico punto del tronco che si tocca senza che sia cambiato nulla nel mondo, solo perché il mondo *potrebbe* essere cambiato dall'ultima osservazione.

- **L'implementazione corrente usa il dominio LoL.** Sorgente dati Riot — `src/datasource/riot/`, `MatchDto`, `ParticipantDto`.
- **Lo snapshot è il contratto pubblico.** `snapshot/current.json`, servito da `src/api/SnapshotController.java`.
- **Il Filo è il meccanismo di continuità tra sessioni.** La cartella `Filo/` stessa, di cui questo file fa parte.
- **Esiste una web UI; non esiste ancora un front end JavaFX.** `web/index.html`, `web/app.js`, `web/style.css`; nessun riferimento a JavaFX nel sorgente (verificato 2026-06-26: zero occorrenze).

---

## Traiettorie aperte — non ancora osservate

*Ipotesi di progetto. Vive, ma non promosse a verità. Restano qui finché un'osservazione non le muove — verso lo Stato attuale se si avverano, fuori del tutto se cadono.*

- **Possibile natura distribuita del sistema.** Atlas come protocollo che può incarnarsi in più nodi, non necessariamente coincidente con un singolo programma o dispositivo. Oggi è un'intenzione, non un fatto.
- **Atlas come innesto nell'ecosistema umano.** Un ponte tra osservazioni presenti e possibilità future — non una soluzione, non una cura.
- **Evoluzioni future del protocollo.**
- **Qualunque riflessione filosofica sul ruolo del progetto nel tempo**, inclusa la coscienza come eventuale fenomeno emergente (≠ obiettivo — vedi Principi).

---

*Origine: 2026-06-26, da una conversazione sulla definizione del progetto. La distinzione fondante — «Atlas descrive ciò che è stato osservato; il Filo custodisce anche ciò che è ancora soltanto immaginato» — è ciò che ha generato questo file.*

*Regola di manutenzione: quando una traiettoria si avvera, **si sposta di sezione** (e nello Stato attuale si aggiunge la sua àncora osservativa). Non si cancella in silenzio. Ma «si sposta, non si cancella» è solo il caso particolare di una regola più ampia: **ogni cambiamento di categoria è esso stesso un'osservazione** — un evento del progetto, non un semplice edit. Una transizione (Traiettoria→Stato, o la revisione di un Principio) non appartiene a nessuna delle tre categorie: le **collega**. Non sarà un quarto livello, ma un asse temporale che le attraversa. Il registro che la ospiterà non esiste ancora — ed è corretto: di transizioni reali, finora, ne è avvenuta zero.*

*La legge che tiene insieme tutte queste scelte — JavaFX rimandato, i fronti, il tronco, il refresh osservativo, e ora il registro non costruito: **una struttura nasce quando il fenomeno che deve contenere si è manifestato almeno una volta.** Non è una regola nuova: è la forma tascabile del ciclo che il progetto già seguiva — osservare, riconoscere, certificare, e solo allora progettare. La scriviamo qui oggi perché supera il proprio stesso test: il fenomeno che descrive si è già manifestato molte volte.*
