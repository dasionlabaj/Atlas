# Capitolo 5 — Quando LoL ha smesso di essere l'obiettivo

*Questo capitolo racconta un cambiamento che non è stato deciso. È stato scoperto.*

---

## Il momento della generalizzazione

`TestEncoder` nasceva con uno scopo tecnico preciso: verificare che il percettrone funzionasse su un problema pulito, a prescindere dal dominio.

Ma aveva un effetto collaterale più importante di quello dichiarato.

`Trainer` funzionava identicamente con `LoLPerceptor` e con `TestEncoder`. Non c'era una riga di codice che sapeva quale dei due stava usando. Riceveva una lista di `Sample`, allenava, aggiornava i pesi, scriveva il log. La stessa sequenza di operazioni. Gli stessi metodi. Zero modifiche.

Questo significava una cosa precisa e verificabile empiricamente: **il sistema di apprendimento era già domain-agnostic**.

Non per design esplicito anticipato. Per conseguenza logica di aver deciso che `Trainer` non doveva sapere di LoL. Quella decisione, presa per ragioni di pulizia architetturale, aveva prodotto qualcosa di più ampio di quanto fosse stato pianificato.

## La domanda che ha spostato la prospettiva

La domanda fu: se `Trainer` non sa di LoL, chi sa di LoL?

`LoLPerceptor`. Solo lui. Sa come estrarre feature da `MatchPerformance`. Sa come normalizzare KDA, CS/min, Vision. Sa quando una partita è `trainable` e quando non lo è — partite troppo brevi, con troppo poco CS, strutturalmente anomale.

E `LoLPerceptor` è sostituibile. Qualsiasi classe che produca una lista di `Sample` può prendere il suo posto. `Trainer` non cambierebbe di una riga.

A quel punto, League of Legends diventava il **primo dominio**. Non l'unico. Non quello definitivo. Il primo contesto in cui il contratto `Sample` era stato istanziato con dati reali.

## Cosa questo significa per Atlas

Atlas non è nato per analizzare partite di League of Legends.

È nato *da* partite di League of Legends, perché quello era il contesto disponibile — il playground familiare, il dominio in cui i dati erano accessibili e il significato delle feature era verificabile dalla propria esperienza di gioco.

Ma il sistema che è emerso da quel contesto non è un analizzatore di LoL. È un sistema di apprendimento che ha LoL come prima implementazione concreta.

La differenza non è sottile. Un analizzatore di LoL che aggiunge un percettrone è uno strumento verticale. Un sistema di apprendimento che usa LoL come primo banco di prova è un'infrastruttura. Il primo finisce quando finisce il suo dominio. Il secondo finisce — se finisce — solo quando smette di trovare nuovi domini in cui osservare.

**LoL era il pretesto. L'osservazione è lo scopo.**

---

## Una nota sul termine "pretesto"

Pretesto non è usato qui in senso negativo. LoL era — ed è — un dominio reale, con dati reali, con un significato verificabile perché il suo creatore conosce quel gioco dall'interno. Questo lo rendeva il luogo perfetto per costruire qualcosa di vero, non un prototipo astratto.

Ma il limite di LoL come unico obiettivo era già visibile: dataset piccolo, assenza di normalizzazione per ruolo, problema non linearmente separabile con feature semplici. Questi limiti non erano fastidi da risolvere. Erano segnali che indicavano la direzione successiva.

Il passo successivo non è migliorare l'analisi di LoL. È portare un secondo dominio nel sistema e verificare che il contratto tenga.

---

*Questo non è l'ultimo capitolo. Ogni volta che cambia il paradigma, si apre una nuova pagina.*
