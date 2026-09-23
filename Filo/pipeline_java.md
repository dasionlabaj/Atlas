# Pipeline Java di Atlas

## Confini

`AtlasCoreApi` espone tre capacità aggiuntive: `train`, `predict` e `completePipeline`.
Il core le instrada ad `AiPipeline`. La logica numerica resta in `Perceptron` e `Trainer`; la persistenza resta in `LearningStateService` e nel writer.

L’estrazione delle feature appartiene al perceptor del dominio. Per LoL è `LoLPerceptor.observe(MatchPerformance)`. I nomi e l’ordine delle feature fanno parte del contratto del modello.

La pipeline è un classificatore numerico locale. Non contiene un modello linguistico né una chiamata a servizi generativi.

## Uso minimo

```java
var core = new AtlasCore();
var start = PipelineEvent.start("dataset:versione-1");
var state = core.train(samples, featureNames, 20, start);
var trained = state.trainings().get(state.trainings().size() - 1).event();
var prediction = core.predict(features, featureNames, trained);
var ia = core.completePipeline(new JsonObject(), prediction.event());
var resumed = core.predict(nextFeatures, featureNames, ia);
```

`samples` contiene `Sample` con feature finite, etichetta `0` o `1` e flag `trainable`. `features` e `nextFeatures` sono vettori `double[]` nello stesso ordine di `featureNames`.

Il costruttore del core carica il servizio Riot solo quando viene richiesta una capacità Riot. Training e inferenza locali funzionano senza `atlas.properties`.

## Training, catalogo e checkpoint

1. Validare nomi, dimensioni, numeri, etichette ed epoche.
2. Caricare una memoria compatibile o inizializzarla per il primo training.
3. Allenare una copia numerica dello stato.
4. Aggiungere una voce `TrainingRun` al catalogo.
5. Salvare pesi e catalogo nello stesso `learning-state.json`.

Una voce contiene l’evento `TRAIN`, le epoche della sessione, il totale cumulativo, i campioni allenati e saltati e l’istante di completamento. I vecchi checkpoint senza `trainings` vengono letti con un catalogo vuoto; le epoche già registrate restano disponibili.

Il file viene scritto temporaneamente nella stessa directory e poi sostituito atomicamente. Se il filesystem non supporta questa operazione, il salvataggio fallisce esplicitamente. `learning-log.txt` è un log diagnostico: può contenere un tentativo fallito; il checkpoint è la fonte dei training completati.

Una directory identifica una memoria e deve avere un solo scrittore. I metodi sullo stesso oggetto `AiPipeline` sono sincronizzati. Per modelli con feature diverse usare directory distinte; il nuovo percorso di training rifiuta di sovrascrivere una memoria incompatibile.

## Inferenza

`predict` restituisce una classe, il numero di epoche del modello e un evento `INFER`. Non salva file, non allena e non crea un modello casuale quando manca il checkpoint.

Sono rifiutati vettori di dimensione errata, valori non finiti, ordine o nomi delle feature incompatibili e overflow numerici. Gli input non validi non diventano segnali `IA`.

## La convenzione `{}` → `IA`

`completePipeline` è il confine finale esplicito. Riceve JSON già analizzato, non una stringa da interpretare.

| Output finale | Evento |
| --- | --- |
| `{}` | `IA` |
| Oggetto con campi, array, numero, stringa o booleano | `RESULT` |
| Valore assente o JSON `null` | Errore: output mancante |

La chiusura non invoca automaticamente un modello. Il chiamante può passare l’evento restituito a una successiva inferenza o sessione di training.

```json
{
  "flow_id": "identificatore-del-filo",
  "event_id": "identificatore-del-passaggio",
  "parent_event_id": "identificatore-precedente",
  "context_ref": "dataset:versione-1",
  "kind": "IA"
}
```

Gli identificatori reali sono UUID. `context_ref` è un riferimento assegnato dal chiamante: la pipeline non inventa né recupera automaticamente il contesto. Gli eventi vengono restituiti; per riprendere un evento `IA` in un altro processo, il chiamante deve conservarne il JSON. Il catalogo conserva gli eventi di training.

## Simulazione e verifiche

```bash
bash scripts/pipeline.sh test
bash scripts/pipeline.sh demo
```

La demo usa una memoria temporanea e pesi iniziali deterministici. Impara OR e segue gli eventi `CONTEXT`, `TRAIN`, `INFER`, `IA`, `INFER`. Mostra la directory del checkpoint per poterlo leggere.

Le 42 verifiche coprono apprendimento, persistenza, ripresa, compatibilità dei checkpoint, catalogazione, separazione dei fili, input invalidi, overflow e fallimenti di scrittura. La demo non esegue misure su dati Riot né richieste di rete.

La build storica completa ha un problema preesistente: i tipi `model.snapshot` sono referenziati ma non versionati. La regola di esclusione `snapshot/` è stata limitata alla cartella di runtime alla radice, così non esclude più futuri sorgenti sotto `src/model/snapshot`.
