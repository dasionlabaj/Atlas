# Atlas — Stato del Filo

*Questo file non è un report. Non racconta cosa fa Atlas. Tiene il filo del pensiero tra una sessione e l'altra.*

*Esiste per una sola ragione: Atlas lavora a sprint intensi seguiti da pause lunghe. Quando si torna, il rischio non è dimenticare il codice — è dimenticare **dove era arrivato il pensiero**.*

**Punto d'ingresso.** Questo file si apre per primo. È il **primo front end reale di Atlas**, ridotto alla sua funzione essenziale: non *presenta* Atlas, ne *restituisce l'accesso allo stato*. Non è una decorazione, non è un indice, non è la simulazione di un front end futuro. Da qui vedi dove sei su *ogni* fronte e scegli dove rientrare. Il dettaglio di un fronte (`lol_front.md`) è un ramo: ci si entra *da qui*, non al posto di qui.

**Le due domande, in quest'ordine:**

> 1. *(albero)* Dove sono su ogni fronte?
> 2. *(ramo)* Nel fronte attivo, qual era domanda, direzione, prossimo passo?

Se entrambe hanno risposta chiara, il Filo regge. Se la prima è confusa, l'ingresso è sbagliato — non aprire un ramo per compensare.

*(Se la domanda non è «dove sono» ma «che cos'è Atlas», la risposta non vive qui: → [atlas_definition.md](atlas_definition.md).)*

---

## I quattro fronti

| Fronte | Stato | Ultimo tocco | Energia richiesta |
|---|---|---|---|
| LoL | **attivo** | 2026-06-26 | media |
| Studio | dormiente | — | — |
| Progetto / Codice | sullo sfondo | 2026-06-26 | alta |
| Vita reale / Energia | non tracciato | — | — |

Un fronte *dormiente* non è abbandonato. È in pausa con il filo annodato. Un fronte *sullo sfondo* ha pensiero attivo ma non è quello su cui si lavora adesso.

---

## Fronte attivo: LoL

Si entra da qui. Dettaglio del ramo in [lol_front.md](lol_front.md).

### ATLAS RESUME

**Dove eravamo**
Sul timing del recall come misura indiretta della sincronizzazione tra trade, wave, recall e gold. Gli item sono il sintomo, non la causa.

**Perché era importante**
È il primo punto in cui smettiamo di guardare *cosa* è stato comprato e iniziamo a guardare *quando* e *con quale finestra*. Sposta lo sguardo dal risultato al ritmo.

**Cosa fare adesso**
Eseguire **il primo ciclo del Filo** (sotto) usando *questo* file come ingresso. L'osservazione da aggiungere può essere **simulata**: non serve aspettare una ranked. La ranked è uno dei contenuti che attraverseranno il ciclo — non è il ciclo.

**Cosa NON toccare**
- Non riaprire il RankPredictionEngine né la calibrazione dei pesi.
- Non aprire un secondo dominio (Studio) nella stessa sessione.
- Non costruire front end *come codice* per testare il front end: questo file è già il front end reale, ridotto all'essenziale.

---

## La regola del "Cosa NON toccare"

È la parte più importante del Filo, non la meno.

Quando l'energia è bassa, Atlas deve **impedire di riaprire dieci rami insieme**. Un solo fronte attivo per volta. Gli altri restano annodati dove sono. Aprirne un secondo non è progresso: è perdere il filo di entrambi.

---

## Il ciclo

Il primo ciclo della beta **non è la ranked**. È questo:

```
Apro Atlas (questo file)
  ↓
vedo dove sono su ogni fronte
  ↓
entro nel fronte attivo (LoL)
  ↓
ritrovo domanda, direzione, prossimo passo
  ↓
aggiungo un'osservazione — reale o simulata
  ↓
chiudo
  ↓
riapro (il giorno dopo, o tra qualche giorno)
  ↓
posso continuare?
```

La ranked **simula il dominio**. Questo ciclo **verifica il Filo**. Sono due cose diverse: la prima è un contenuto, il secondo è il test.

### I livelli che il ciclo mette alla prova

- **Filo** — il dominio della continuità: cosa va conservato (domanda, direzione, prossimo passo).
- **Infrastruttura** — ciò che conserva il cambiamento tra una sessione e l'altra.
- **Front end** — il luogo in cui la continuità diventa *percepibile*. Questo file non lo simula: lo *è* già, ridotto all'essenziale. Il codice, domani, sarà la stessa funzione con più portata — non una funzione nuova.
- **LoL** — il primo fronte abbastanza ricco da metterli tutti alla prova insieme.

---

## Il test

**L'osservazione non è il test.** Il test è una domanda sola, al momento della riapertura:

> Sono tornato nello stesso punto operativo, senza dover ricostruire da zero il ragionamento?

Non serve ricordare le stesse frasi — la memoria ricostruisce un percorso, non conserva una fotografia. Serve recuperare **domanda, direzione e prossimo passo**. Se il Filo ti permette di *continuare* anziché *ricominciare*, ha respirato.

Non conta se hai vinto o perso la ranked. Non conta nemmeno se il checkpoint era perfetto. L'unica cosa che la Beta 0.1 valida è: **Apertura → Contenuto → Tempo → Riapertura → riprendo il filo o riparto da zero?**

---

## Nota sul codice

Il modello Java (`Front`, `Checkpoint`, `Timing`, `Progression`, `ResumePrompt`) e qualsiasi front end *come codice* sono **rimandati di proposito**.

Codificare adesso congelerebbe una forma che deve ancora respirare. Il front end reale, nella sua forma minima (questo file), è già qui e va messo alla prova subito; la sua forma in *codice* — più portata, stessa funzione — resta dietro lo stesso cancello.

**Il cancello prima del codice — tre cicli riusciti:**

1. Apri `atlas_state.md` → entra nel fronte attivo
2. Aggiungi un'osservazione (reale o simulata) e un CHECKPOINT nel file del fronte
3. A distanza di tempo, riapri `atlas_state.md` → riprendi il filo o riparti da zero?

Se questo ciclo regge **tre volte**, allora il bisogno sarà stato *osservato* e il codice avrà senso. Prima no. Se invece non torna, il codice sarebbe solo un acceleratore di un'idea ancora incompleta — meglio saperlo prima di scrivere una classe.

*"Questo bisogno è stato osservato o lo stiamo anticipando troppo?"*

**Domanda annodata (non aperta):** più avanti il Filo dovrebbe *leggere* `RankPrediction` / `RankGoal` invece di richiedere il rank a mano. Non ora — entra solo dopo il terzo ciclo riuscito.

Questa beta non deve essere bella. Deve fare una cosa sola: **impedire che il pensiero cada tra una sessione e l'altra.**
