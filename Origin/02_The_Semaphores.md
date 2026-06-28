# Capitolo 2 — I semafori

*Questo capitolo racconta una delle decisioni di design più importanti: perché i segnali di Atlas sono semafori, non voti.*

---

## Il problema del giudizio

Immagina un sistema che, alla fine di ogni partita, ti dice: "Performance: 6/10."

È intuitivo. È immediato. È sbagliato.

Un 6/10 in una partita da 45 minuti persa al nexus dopo una rimonta fallita è una cosa. Un 6/10 in una partita di 22 minuti vinta senza difficoltà è un'altra. Un 6/10 nella prima partita dopo due settimane di pausa è un'altra ancora.

Il numero è identico. La realtà che rappresenta è completamente diversa.

Un voto sintetizza troppo. Comprime informazione che è poi irrecuperabile. Non puoi sapere cosa c'era dietro a quel 6 — era un KDA mediocre con una visione ottima, o una partita tecnica da 8 con tre afk in squadra? Il voto ha già digerito tutto. Non c'è modo di tornare al dato.

## La scelta del semaforo

Atlas usa segnali, non voti. Ogni `StatSignal` porta un valore numerico grezzo, una direzione (`SignalColor`: GREEN, YELLOW, RED) e un'etichetta.

Verde non significa "bravo". Significa "questo indicatore è sopra la soglia contestuale."
Rosso non significa "hai sbagliato". Significa "questo indicatore è sotto la soglia contestuale."

La differenza è sottile ma decisiva. Un segnale rosso sul Vision Score in una partita in cui eri jungler senza controllo del lato destro della mappa dice qualcosa. Lo stesso segnale rosso in una partita in cui eri toplaner e hai ignorato completamente la visione dice qualcosa di diverso.

Il segnale è lo stesso. La lettura cambia. E quella possibilità di lettura differenziata non sarebbe sopravvissuta a un voto unico.

## Il principio invariante

C'è una regola in Atlas che non ha eccezioni:

**Il contesto non cambia il punteggio. Cambia la lettura del punteggio.**

`Observation.score` è invariante. Viene calcolato dai segnali grezzi e non viene mai modificato per motivi contestuali.

Il `BreakDetector` rileva che sono passate più di 72 ore dall'ultima partita. Questa informazione viene conservata come `context_note`. Non abbassa o alza lo score. Non tocca niente nel dato. Dice solo: "questa partita è stata giocata dopo una pausa. Tienilo presente quando la leggi."

Perché questa rigidità?

Perché un sistema che abbassa il giudizio quando trova una giustificazione non sta imparando. Sta assolvendo. E un sistema che assolve non è una lente — è uno scudo.

**La lente non è uno scudo.**

Questo principio è uno dei fondamenti di Atlas. Non perché il contesto non conti — conta moltissimo — ma perché il contesto deve arricchire la lettura, non alterare il dato. Se abbassiamo lo score "perché eri stanco", perdiamo l'informazione che quella partita era al di sotto del tuo livello. Quella informazione, domani, potrebbe essere esattamente quello che serve per capire un pattern.

Il contesto spiega. Non giustifica. E non riscrive.

---

*Il capitolo successivo racconta il momento in cui Atlas ha smesso di avere regole e ha iniziato ad avere pesi.*
