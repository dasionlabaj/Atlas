# Atlas I/O API — pre-progetto v0.1

Primo nucleo di **ingresso / uscita** di Atlas, indipendente dall'interfaccia di Entrelia.

```text
Entrelia / sorgenti autorizzate
          | POST JSON + bearer token
          v
    Atlas I/O Worker -- D1 (persistenza)
          | GET JSON + bearer token
          v
 AI connector / MCP adapter (futuro)
```

## Contratto HTTP

- `GET /health` pubblico: stato del servizio (nessun dato personale).
- `POST /v1/observations` protetto: inserisce una osservazione JSON. L'ID è generato dal server; `source` e `kind` sono obbligatori, `payload` è un oggetto JSON.
- `GET /v1/observations?limit=20&kind=lol.match` protetto: ultime osservazioni per data di inserimento.
- `GET /v1/observations/:id` protetto: osservazione singola.

Risposte in JSON, errori uniformi `{ error: { code, message } }`. Limite di 64 KiB per richiesta. CORS disabilitato: uso server-to-server. **Non esporre il bearer token nel JavaScript pubblico di Entrelia**: deve essere conservato come segreto del Worker backend che chiama Atlas.

## Deploy proposto — Cloudflare Workers + D1

1. Creare un database D1 e inserire il suo ID in `wrangler.toml` (sostituire `REPLACE_WITH_D1_DATABASE_ID`).
2. Eseguire `npx wrangler d1 migrations apply atlas-io --remote` dalla cartella `atlas-io`.
3. Configurare `ATLAS_IO_TOKEN` con `npx wrangler secret put ATLAS_IO_TOKEN`; usare un token casuale lungo, diverso dalla chiave Riot.
4. Eseguire `npx wrangler deploy`.
5. Collegare il backend di Entrelia agli endpoint autorizzati. In futuro aggiungere un adapter MCP con autenticazione propria, controlli di accesso e strumenti di sola lettura.

## Esempio

```bash
curl -X POST "https://YOUR-WORKER.workers.dev/v1/observations" \
 -H "Authorization: Bearer YOUR_SECRET" \
 -H "Content-Type: application/json" \
 --data '{"source":"riot","kind":"lol.match","payload":{"matchId":"EUW1_example","damage":{"physical":1200,"magic":800,"true":120}}}'
```

Il Worker **non importa automaticamente** statistiche Riot: riceve dati dal backend autorizzato. Non è ancora collegato a ChatGPT/MCP e non è stato distribuito in produzione. Prima di gestire dati reali implementare rate limiting, rotazione chiavi, politiche di retention e valutazione privacy.
