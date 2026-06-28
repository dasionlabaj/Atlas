# Capitolo 1 — La prima osservazione

*Questo capitolo racconta cosa è successo quando Atlas ha visto per la prima volta una partita vera.*

---

## La prima partita

Il primo passo era semplice: scaricare una partita dall'API di Riot Games e trasformarla in qualcosa di leggibile.

Semplice da dire. Molto meno da fare.

L'API di Riot restituisce JSON. JSON enormi, con decine di campi, annidamenti multipli, informazioni su tutti e dieci i giocatori di ogni partita. Il primo problema non era analizzare la partita. Era trovare la partita giusta nel JSON. Poi capire quale campo conteneva i kill. Quale i death. Quale gli assist. Quale la durata. Dove stava il vision score. Dove il danno totale.

Questo lavoro — il mapping da JSON grezzo a struttura Java — ha prodotto il primo DTO. E subito dopo è emersa la prima distinzione importante:

**I dati di una partita non sono ancora un'analisi di una partita.**

Un `MatchDTO` è quello che Riot dice. Una `MatchPerformance` è quello che Atlas capisce. Sono due cose diverse, e tenerle separate era fondamentale. Mescolarle avrebbe significato che ogni cambiamento nel formato dell'API avrebbe contaminato la logica di analisi. La separazione era un confine, non un dettaglio.

## Dalla partita all'osservazione

Una volta che Atlas poteva leggere una partita, la domanda successiva era: cosa farsene?

La risposta iniziale erano i classici indicatori: KDA, CS al minuto, Vision Score. Tradotti in segnali — non in voti, non in giudizi, ma in orientamenti. Verde se il valore era sopra soglia. Giallo se vicino. Rosso se sotto.

Ma c'era un problema con "sopra soglia": rispetto a cosa?

Le soglie iniziali erano hardcoded. Valori ragionevoli, presi dall'esperienza. Funzionavano, ma erano fragili. Se il meta cambiava — se il CS medio saliva perché era cambiata la gold economy — le soglie dovevano cambiare manualmente. Qualcuno doveva aprire il codice, cercare la costante, modificarla.

Questo non era un sistema che imparava. Era un sistema che aspettava di essere corretto.

Quella fragilità è il seme che ha prodotto il percettrone. Ma quello è il capitolo tre.

## Perché una partita non bastava

Guardare una partita e dire "KDA 3.2, buono" non diceva niente. Buono rispetto a cosa? Rispetto a ieri? Rispetto all'ultima settimana? Rispetto al tuo picco?

Capire una performance richiede confronto. Non giudizio assoluto, confronto.

Questa realizzazione ha prodotto il secondo passo del sistema: non una partita, ma una sequenza. `FileMemoryStore`. `atlas-memory.txt`. La lista crescente delle osservazioni nel tempo.

Ogni `Observation` aveva uno `score` — un numero calcolato dai segnali. Non un voto della performance umana. Un indice di quanto quella partita si collocava rispetto al range osservato.

E quella lista era il primo abbozzo di ciò che sarebbe diventata la memoria di Atlas: non un singolo punto, ma una traiettoria.

---

*Il capitolo successivo racconta perché i segnali di Atlas sono semafori, non voti.*
