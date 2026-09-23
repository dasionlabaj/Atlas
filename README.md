# Atlas

### Osservare. Imparare. Continuare.

![Un nastro continuo in metallo con riflessi lime](docs/assets/continuity.webp)

**Il laboratorio software di [Dasion Labaj](https://github.com/dasionlabaj).**
Atlas parte dalle osservazioni, le trasforma in caratteristiche numeriche e conserva ciò che apprende. Un nucleo piccolo collega servizi, modello e memoria.

[Pipeline Java](Filo/pipeline_java.md) · [Demo offline](src/main/AiPipelineDemo.java) · [Codice della vetrina](docs/index.html) · [L’idea di Atlas](Filo/atlas_definition.md)

## Provalo in due comandi

Con **JDK 17+** e Bash, dalla cartella del repository:

```bash
bash scripts/pipeline.sh test
bash scripts/pipeline.sh demo
```

La demo allena il percettrone sulla funzione logica OR, esegue due inferenze e mostra come un evento `IA` conserva il collegamento al precedente. Usa dati sintetici e una memoria temporanea separata. Non richiede chiavi API.

## La pipeline

**Osservazione → feature → validazione → percettrone → risultato.**

Il training riceve campioni con un risultato atteso, aggiorna i pesi e salva un checkpoint. L’inferenza applica i pesi già disponibili.

| Gesto | Responsabilità |
| --- | --- |
| `VIEW` | Leggere l’ultima osservazione Riot dalla cache. |
| `REFRESH` | Acquisire una nuova osservazione Riot, se presente. |
| `TRAIN` | Apprendere dai campioni e salvare pesi e catalogo dei training. |
| `INFER` | Calcolare una classe `0` o `1` senza modificare i pesi. |

Il modello attuale è un **percettrone binario**. La demo verifica un caso sintetico semplice; non misura la qualità delle previsioni sulle partite.

## Un appiglio per ogni passaggio

Ogni evento porta `flow_id`, `event_id`, `parent_event_id` e `context_ref`.

Nel protocollo Atlas, **un oggetto JSON vuoto alla fine della pipeline genera il segnale `IA`**. È una convenzione esplicita per continuare il flusso. Il contesto rimane collegato attraverso i riferimenti dell’evento.

Ogni training viene catalogato nel checkpoint con epoche, campioni usati, campioni saltati, data e provenienza. I pesi e il catalogo vengono sostituiti insieme, con una scrittura atomica.

## Dentro il codice

| Modulo | Cosa contiene |
| --- | --- |
| [`core`](src/core) | La porta verso le capacità di Atlas. |
| [`perceptor`](src/perceptor) | Trasformazione delle osservazioni in feature. |
| [`pipeline`](src/pipeline) | Validazione, training, inferenza e chiusura degli eventi. |
| [`learning`](src/learning) | Il modello numerico. |
| [`service`](src/service) | Memoria del modello e servizi di dominio. |
| [`Filo`](Filo) | Definizioni, scelte e continuità del progetto. |
| [`docs`](docs) | La vetrina statica: HTML, CSS e una demo interattiva del concetto. |

### Stato della build

La pipeline e le sue dipendenze compilano con il comando indicato sopra; la suite esegue **42 verifiche**. La build completa dell’applicazione storica richiede ancora i sorgenti `model.snapshot`, assenti nel repository al momento dell’integrazione. Il comando della pipeline non compila quella parte.

---

**Dasion Labaj** · Idee in movimento. Contesto che rimane.
