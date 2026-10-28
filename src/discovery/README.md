# Atlas — Distribution Trace v0.1

Esperimento **isolato**, ispirato alla domanda: come si muove un'informazione dal
repository GitHub verso ricerca web, sito e possibili canali di promozione?

## Stati osservabili (non equivalenti)

| Evento | Canale | Cosa possiamo affermare |
| --- | --- | --- |
| `GITHUB_PUSH` | GitHub | Esiste un commit/push citato da una fonte. |
| `GITHUB_METADATA` | GitHub | Una fonte descrive i metadati del repository. |
| `GITHUB_SEARCH_RESULT` | GitHub | Una fonte riporta un risultato della ricerca GitHub. |
| `WEB_SEARCH_RESULT` | Web search | Una fonte riporta un risultato della ricerca web. |
| `SITE_VIEW` | Analisi sito | Una fonte riporta una visita; non l'identita' del visitatore. |
| `PAID_IMPRESSION` | Campagna | Una fonte riporta un'impression pubblicitaria. |
| `DIRECT_RESPONSE` | Direct | Una fonte documenta una risposta esplicita. |

**Un push non dimostra indicizzazione, impression, visualizzazione o contatto.**
Ogni stato e' registrato separatamente. GitHub gestisce il proprio archivio e
i metadati/repository; GitHub Search ha il proprio indice. Motori web e
piattaforme pubblicitarie gestiscono propri sistemi. Atlas non riproduce
algoritmi proprietari, ranking o reti di distribuzione di terze parti.

## Pattern riutilizzabile

`DistributionTrace` e' un registro in-memory, append-only, con:
- Canali e stadi tipizzati.
- Fonte HTTPS esplicita o `HYPOTHESIS` senza fonte.
- De-duplicazione esatta per evitare ricircolo di una stessa osservazione.
- Capacita' massima 512 osservazioni, ID e note limitati.
- Conteggi separati fra ipotesi e osservazioni con URL di supporto.
- **Nessuna inferenza automatica di audience o contatti personali.**

Una URL e' una **referenza fornita dal chiamante**, non prova verificata
automaticamente: occorre controllare autenticita' e contenuto della fonte
in un'integrazione successiva. Non mettere in `assetId`, `note` o URL
identita', credenziali, query riservate o dati personali. Il registro non e'
connesso ad Atlas Core, al Neural Seed, a un browser, a GitHub API,
a un servizio ads, a un database o a un endpoint HTTP.

## Test (Java, nessuna dipendenza aggiuntiva)

```sh
mkdir -p build/distribution-trace
javac -d build/distribution-trace src/discovery/DistributionTrace.java tests/discovery/DistributionTraceTest.java
java -cp build/distribution-trace discovery.DistributionTraceTest
```

## Prossimi passi: non implementati

1. Lettura autorizzata di metadati GitHub tramite REST/Webhook, con verifica
   di fonte ed eventuali firme webhook.
2. Osservazione **separata** dei risultati web e delle analytics aggregate.
3. Dataset sintetici per studiare regolarita' osservabili, mai per presumere
   chi abbia letto o ricevuto un contenuto.
4. Valutare quali pattern e implementazioni originali possano essere concessi
   in licenza o commercializzati, nel rispetto di privacy e condizioni
   contrattuali dei fornitori.

Questo modulo non effettua marketing, targeting o pubblicazione.
