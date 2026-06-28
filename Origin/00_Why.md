# Capitolo 0 — Perché esisto

*Questo documento non è un README. Non spiega come usare Atlas. Non descrive la sua architettura. Racconta perché esiste.*

*Viene scritto nel giugno 2026, prima che Atlas abbia un'interfaccia grafica, prima che conosca un secondo dominio, prima che chiunque lo usi se non il suo creatore.*

*Viene scritto adesso perché una nascita, a differenza di un commit, accade una volta sola.*

---

## Da dove vengo

Atlas nasce da una domanda semplice, formulata davanti a una partita appena finita su League of Legends.

Non "come posso vincere?", ma "cosa non riesco ancora a vedere?"

Questa distinzione è tutto. Una è una richiesta di aiuto in tempo reale. L'altra è una richiesta di lente. Il creatore di Atlas aveva già una risposta alla prima domanda — esistevano sistemi, overlay, suggeritori automatici. Non voleva quella categoria di strumenti. Voleva qualcosa che osservasse con lui, non al posto suo.

Così Atlas è nato come strumento di analisi post-partita. Non un assistente. Non un coach automatico. Un sistema capace di guardare ciò che era successo e di tradurlo in qualcosa di comprensibile.

## Perché sono fatto così

Ogni decisione architetturale in Atlas segue un principio che non è mai stato scritto esplicitamente, ma che era sempre presente:

**Il processo viene prima della presentazione.**

Niente interfaccia grafica finché la pipeline non era stabile. Niente dashboard finché l'analisi non era corretta. Niente semafori finché le soglie non avevano una logica difendibile.

Questo non era perfezionismo. Era una scelta metodologica precisa: costruire la realtà prima di decidere come mostrarla. Chi fa il contrario — costruisce l'interfaccia prima del modello — finisce spesso per costruire il modello che si adatta all'interfaccia, non viceversa.

C'è una domanda che ha guidato molte decisioni: **"chi paga quando il mondo cambia?"** Quando una soglia diventa obsoleta, deve cambiare la policy, non il dominio. Quando una regola di business evolve, deve cambiare il classificatore, non l'analizzatore. Questo principio ha prodotto `ThresholdProvider`, ha separato `MatchAnalyzer` dalla policy, ha tenuto `Trainer` domain-blind.

## Chi mi ha costruito

Il creatore di Atlas è uno sviluppatore che alterna sprint molto intensi a pause altrettanto intense. Non è il tipo da commit quotidiani. È il tipo da sessioni lunghe in cui i problemi vengono aggrediti fino in fondo, seguite da periodi in cui il progetto aspetta.

Questo non è un difetto. È uno stile. E Atlas, in un certo senso, riflette quello stile: non è costruito per rispondere velocemente, ma per rispondere bene.

Una frase che il suo creatore ha ripetuto in modi diversi, spesso: *"Prima capisco il processo, poi lo implemento."*

Non ha mai inseguito il trucco, la scorciatoia, il risultato immediato. Ha inseguito la comprensione. Questo ha reso lo sviluppo più lento in alcuni momenti, ma ha prodotto un sistema con una coerenza interna che si vede — anche quando non si mostra.

C'è un'altra cosa che vale la pena dire. Questo progetto è nato, in parte, anche come esperimento di comunicazione con l'intelligenza artificiale. Non per far fare ad Atlas ciò che un'IA fa già, ma per costruire un sistema abbastanza trasparente nel suo processo mentale da poter collaborare con un'altra intelligenza — umana o artificiale — in modo significativo. Rendere il ragionamento leggibile, non solo il risultato.

## Che cosa sto cercando di diventare

Atlas non è nato per fermarsi all'analisi di League of Legends.

Questo è diventato chiaro gradualmente, quasi senza che nessuno lo dichiarasse esplicitamente. A un certo punto, il percettrone è diventato domain-blind. `Sample` è diventato la lingua franca. `Trainer` non sapeva più — e non doveva sapere — se stava imparando da partite di LoL o da dati sintetici.

LoL è il primo dominio. Non l'unico.

L'obiettivo finale non è analizzare una partita. È costruire un sistema capace di osservare qualsiasi dominio strutturato, imparare da esso, e tradurre quell'apprendimento in segnali comprensibili.

---

**Atlas non nasce per dare risposte. Nasce per imparare a osservare.**

Questa è la dichiarazione d'intenti. Non uno slogan. Una scelta di design.

Un sistema che dà risposte deve avere ragione. Un sistema che impara a osservare può essere corretto senza essere distrutto.

---

*Il prossimo capitolo racconta il primo contatto con una partita reale.*
