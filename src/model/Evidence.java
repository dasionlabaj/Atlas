package model;

/**
 * Un'evidenza: la fonte di un'affermazione, resa leggibile. È proprietà di Atlas,
 * non di un singolo livello — la usano il Coach e MatchInsight (domani forse il
 * Rank). Un solo vocabolario dell'evidenza in tutto il programma.
 *
 * source      = da quale osservazione nasce (es. "Deaths") — il riferimento verificabile
 * description = la frase parlante, scritta DOVE il significato è noto, non derivata
 *               dal sensore: il gauge Deaths "in calo" = morti "in aumento".
 *
 * Regola: ogni affermazione espone le sue evidenze; ogni evidenza ha una fonte e
 * una frase leggibile.
 */
public record Evidence(
    String source,
    String description
) {}
