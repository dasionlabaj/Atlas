package model.coach;

import java.util.List;

import model.Evidence;

/**
 * Il prodotto vero del Coach: non un indicatore, ma un'ipotesi.
 * observation = ciò che Atlas ha visto nei dati
 * hypothesis  = una possibile correlazione, mai un comando ("potrebbe", non "fai")
 * confidence  = quanto i dati sostengono l'ipotesi (0-100), derivata, non scelta a mano
 * evidence    = le osservazioni che hanno DAVVERO costruito l'ipotesi, ognuna con
 *               fonte e frase leggibile (Evidence). Sono le "Evidenze": Atlas
 *               non convince, mostra perché pensa una cosa. Rendono l'ipotesi
 *               verificabile dal livello sottostante.
 */
public record CoachHypothesis(
    String observation,
    String hypothesis,
    double confidence,
    List<Evidence> evidence
) {}
