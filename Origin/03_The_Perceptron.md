# Capitolo 3 — Il percettrone

*Questo capitolo racconta il momento in cui Atlas ha smesso di avere regole e ha iniziato ad avere pesi.*

---

## Il limite delle soglie fisse

Le soglie hardcoded funzionavano. KDA sopra 3.0 → verde. CS/min sopra 7.0 → verde. Vision sopra 1.2 → verde.

Ma avevano un problema fondamentale: erano opinioni.

Opinioni ragionevoli, basate sull'esperienza, probabilmente corrette in media. Ma statiche. Non imparavano. Non si adattavano al giocatore, al meta, al ruolo, alla patch. E soprattutto: erano decisioni prese fuori dal sistema. Ogni volta che una soglia doveva cambiare, qualcuno doveva cambiarla. Il sistema non aveva voce in capitolo sulla propria calibrazione.

## Imparare invece di decidere

L'idea era semplice nella forma, profonda nella conseguenza: invece di dire "KDA sopra 3.0 = buono", lasciare che il sistema imparasse da sé quando una partita era stata buona.

Il dato disponibile era già lì: per ogni partita, sappiamo se era una vittoria o una sconfitta. E sappiamo KDA, CS/min, Vision Score, Deaths, Danno al minuto.

Un percettrone riceve questi cinque valori, li combina linearmente con dei pesi, e produce una previsione: vittoria o sconfitta prevista? Se la previsione è sbagliata, i pesi si aggiustano. Poco — learning rate 0.01 — ma ogni partita lascia una traccia. I pesi convergono verso la rappresentazione compressa di tutto ciò che il sistema ha visto.

Non è più un'opinione hardcoded. È un apprendimento accumulato.

## La decisione architettuale più importante: Sample

La cosa più importante non fu il percettrone. Fu la scelta di astrarlo dal dominio.

La domanda che si pose fu questa: il percettrone deve sapere di League of Legends?

La risposta fu no. Il percettrone è matematica. Riceve numeri, aggiusta pesi, produce una previsione. Non ha bisogno di sapere cosa significano quei numeri.

Questo ha richiesto un contratto: `Sample`. Un oggetto con tre campi: `features[]` (i numeri), `expected` (0 o 1), `trainable` (se questo sample deve contribuire al training).

`LoLPerceptor` produce `Sample` da `MatchPerformance`. `Trainer` consuma `Sample` e allena `Perceptron`. `Trainer` non sa — e non deve sapere — cosa significano le feature. Sa solo che ci sono numeri e un risultato atteso.

Questa separazione non era eleganza per eleganza. Era la condizione necessaria per tutto ciò che sarebbe venuto dopo: un secondo dominio, un terzo, qualsiasi cosa producesse `Sample` poteva essere appresa da `Trainer` senza toccare una riga del motore.

## PerceptronState: la memoria dei pesi

Ogni ciclo di training aggiornava i pesi. Ma alla fine del programma, quei pesi sparivano.

La soluzione era `PerceptronState` e `PerceptronStateStore`. Alla fine di ogni run, i pesi vengono scritti in `perceptron-state.json`. Al prossimo avvio, vengono ricaricati — con una validazione: se il numero di pesi salvati non corrisponde alla dimensione attesa, il sistema reinizializza piuttosto che crashare. Permette di cambiare dominio senza distruggere lo stato.

Atlas non ricomincia da zero ogni volta. Ogni partita aggiunge all'apprendimento accumulato. I pesi sono la memoria compressa di tutto ciò che il sistema ha osservato. Non i dati grezzi — la loro forma distillata.

---

*Il capitolo successivo racconta cosa è emerso dai primi cicli di training reale.*
