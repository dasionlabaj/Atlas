# Capitolo 4 — Il primo apprendimento

*Questo capitolo racconta cosa ha imparato Atlas nelle prime sessioni di training reale — e cosa ci ha insegnato a nostra volta.*

---

## I primi numeri

Con 20 partite, learning rate 0.03, 10 epoche: plateau fisso a 68.4%.

Non era entusiasmante. Ma era un inizio — il sistema stava già preferendo qualcosa a qualcos'altro.

Con 50 partite, lo stesso rate: oscillazione tra 70% e 79.2%. Più match significava più segnale. Ma l'oscillazione suggeriva instabilità: il learning rate era ancora troppo alto, i pesi rimbalzavano invece di convergere.

Con 50 partite, learning rate 0.01, 20 epoche: zona stabile 75–79.2%, con 79.2% come frequenza dominante.

Questo divenne il baseline. Non un risultato finale, ma un punto di riferimento onesto.

## La sorpresa del CS/min

La cosa più interessante non era la percentuale di accuratezza. Era la direzione dei pesi.

- f0 KDA: fortemente positivo. Atteso.
- f2 Vision/min: lievemente positivo. Atteso.
- f4 Danno/min: positivo stabile. Atteso.
- f3 Deaths: quasi zero. Parzialmente atteso — assorbito già da KDA.
- f1 CS/min: **negativo**.

Questo era inaspettato. Più CS al minuto portava il sistema a prevedere sconfitta?

La spiegazione, dopo riflessione: il CS/min non è uniforme tra ruoli. Un support con CS/min basso e vittoria, un toplaner con CS/min alto e sconfitta — su un dataset che mescola ruoli senza distinzione, il segnale viene distorto. Il sistema non sbagliava. Diceva la verità su dati che non erano stati progettati per distinguere il ruolo.

Non era un bug. Era il sistema che mostrava un limite nel dataset, non in sé stesso.

Questo è esattamente il tipo di osservazione che Atlas dovrebbe essere in grado di fare. Non "la risposta è sbagliata", ma "la risposta è giusta dati questi dati, e questi dati hanno questa forma".

## La prova del controllo: TestEncoder

Per verificare che il plateau non fosse un difetto del percettrone, fu creato `TestEncoder`.

Problema sintetico: due feature. Regola: f0 + f1 > 1.0 → vittoria. Problema linearmente separabile per costruzione — esiste per definizione un iperpiano che lo divide perfettamente.

Risultato: convergenza a 100% dall'epoca 11.

Il percettrone funzionava. Il plateau su LoL non era un limite della matematica. Era un limite del problema: League of Legends, con cinque feature semplici non normalizzate per ruolo, non è linearmente separabile.

Questo non era una sconfitta. Era informazione. Sapere che il problema non è linearmente separabile con queste feature dice qualcosa di preciso: servono feature migliori, o un modello più espressivo, o entrambi. Ma il percettrone ha già dato tutto quello che poteva dare.

---

*Il capitolo successivo racconta il momento in cui League of Legends ha smesso di essere l'obiettivo.*
