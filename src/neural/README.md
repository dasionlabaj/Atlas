# Atlas Neural Seed v0.1 — esperimento isolato

**Stato: prototipo Java esplicito, non collegato al runtime o alle API Atlas.**

La capsula usa **esattamente due perceptron** tramite le classi esistenti
`learning.Perceptron` e `learning.PerceptronState`.

- **Fondazione**: riceve un vettore di feature numeriche normalizzate in `[-1,1]`
  e restituisce un bit di classificazione.
- **Unificazione**: riceve lo stesso vettore **più il bit emesso da Fondazione**.
  È quindi un livello successivo, non un modello indipendente. I due perceptron
  usano la stessa label esplicita durante l'addestramento.

I due output possono concordare o discordare. La concordanza **non certifica**
verità, comprensione, accuratezza generalizzabile o memoria delle conversazioni.
Non esiste ancora un encoder dal linguaggio a feature: i dati li passa il chiamante.

## Confini e limiti

- Nessun thread, loop autonomo, accesso alla rete, accesso alla memoria ChatGPT,
  integrazione GitHub o aggiornamento di altri moduli.
- L'addestramento parte **solo** da `learn(batch, epochs)`.
- Massimo 16 feature; 128 esempi per batch; 20 epoche per chiamata;
  4096 presentazioni complessive di esempio per ciascun perceptron.
- Validazione prima delle modifiche; pesi e bias confinati a `[-8,8]`.
- `checkpoint()` e `restore()` consentono rollback in memoria.
- `saveCheckpoint(path)` e `loadCheckpoint(path)` sono operazioni su file
  **esplicite**, con formato versionato e convalida prima del ripristino.
  Per checkpoint operativi scegliere un percorso locale non versionato e
  tenere i dati personali fuori da Git.

## Collaudo isolato

Dalla radice del repository, con un JDK installato:

```sh
mkdir -p build/neural-seed
javac -d build/neural-seed src/learning/Perceptron.java src/learning/PerceptronState.java src/neural/NeuralSeed.java tests/neural/NeuralSeedTest.java
java -cp build/neural-seed neural.NeuralSeedTest
```

Il test usa solo vettori sintetici e verifica dimensioni, ruoli,
addestramento, convalida, budget, serializzazione e ripristino.

## Prossimi passi (non implementati)

Definire il significato delle feature e delle label, misurare errori su dati
non visti, collegare la capsula ad Atlas con permessi e prove separati.
Non collegare mai questa prova a un processo di aggiornamento automatico
senza un controllo esplicito dell'origine dei dati e dell'autorizzazione.
