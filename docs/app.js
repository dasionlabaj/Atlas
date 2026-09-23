'use strict';
const steps = [
  ['01 / CONTESTO', 'Si parte da un’osservazione e da un riferimento che permette di ritrovarla.'],
  ['02 / TRAINING', 'L’esercizio ha un obiettivo e un criterio. Il risultato conserva il riferimento di partenza.'],
  ['03 / SEGNALE', 'Nel protocollo di Atlas, un JSON vuoto alla fine del percorso genera l’evento IA.'],
  ['04 / IA', 'L’evento conserva il riferimento al contesto e al passaggio precedente, pronto per una nuova elaborazione.'],
  ['05 / RIPRESA', 'Il passo successivo si riaggancia allo stesso filo: contesto, training ed evento precedente restano collegati.']
];
const button = document.getElementById('flow-button');
const label = document.getElementById('step-label');
const description = document.getElementById('step-description');
const nodes = [...document.querySelectorAll('.flow-node')];
let current = 0;
button.hidden = false;
button.addEventListener('click', () => {
  current = (current + 1) % steps.length;
  label.textContent = steps[current][0];
  description.textContent = steps[current][1];
  nodes.forEach((node, index) => {
    node.classList.toggle('is-active', index === current);
    node.classList.toggle('is-complete', index < current);
    if (index === current) node.setAttribute('aria-current', 'step');
    else node.removeAttribute('aria-current');
  });
  button.innerHTML = current === steps.length - 1 ? 'Ricomincia <span aria-hidden="true">↺</span>' : 'Passo successivo <span aria-hidden="true">→</span>';
});
nodes[0].setAttribute('aria-current', 'step');
