// UI 0 — la lente. Legge /snapshot e disegna. Nessuna logica, nessuna interpretazione.

const DOT = { GREEN: "🟢", YELLOW: "🟡", RED: "🔴" };

function toggle(id, visible) {
  document.getElementById(id).hidden = !visible;
}

async function load() {
  try {
    const res = await fetch("/snapshot");
    if (!res.ok) throw new Error("HTTP " + res.status);
    render(await res.json());
  } catch (e) {
    document.getElementById("error").hidden = false;
    document.getElementById("error").textContent = "Snapshot non disponibile: " + e.message;
  }
}

function render(snapshot) {
  const summary = snapshot.summary || {};

  const status = document.getElementById("status");
  status.textContent = "status: " + (summary.status || "—");
  status.dataset.status = summary.status || "";

  document.getElementById("summoner").textContent = summary.summoner || "";
  document.getElementById("lastMatch").textContent =
    summary.lastMatchId ? "last: " + summary.lastMatchId : "";

  const signals = document.getElementById("signals");
  signals.innerHTML = "";
  (snapshot.signals || []).forEach(s => {
    const li = document.createElement("li");
    // Semaforo (summary) + cassetto (detail). <details> nativo: il browser apre
    // e chiude, nessuna logica qui. I campi del contratto v2 (reason, soglie)
    // possono mancare su uno snapshot vecchio: Atlas non dichiara ciò che non sa.
    const hasBands =
      typeof s.greenThreshold === "number" && typeof s.yellowThreshold === "number";
    li.innerHTML =
      `<details class="signal">` +
        `<summary>` +
          `<span class="name">${s.name}</span>` +
          `<span class="dot">${DOT[s.level] || "·"}</span>` +
          `<span class="value">${s.value.toFixed(2)}</span>` +
        `</summary>` +
        `<dl class="detail">` +
          `<div><dt>Valore</dt><dd>${s.value.toFixed(2)}</dd></div>` +
          (hasBands ? `<div><dt>Verde</dt><dd>≥ ${s.greenThreshold.toFixed(2)}</dd></div>` : ``) +
          (hasBands ? `<div><dt>Giallo</dt><dd>≥ ${s.yellowThreshold.toFixed(2)}</dd></div>` : ``) +
          (s.reason ? `<div><dt>Lettura</dt><dd>${s.reason}</dd></div>` : ``) +
        `</dl>` +
      `</details>`;
    signals.appendChild(li);
  });

  // Interpretazione della partita — assente se Atlas non vede un pattern chiaro
  const insight = snapshot.matchInsight;
  toggle("insightBlock", !!insight);
  if (insight) {
    const inv = Array.isArray(insight.involvedSignals) ? insight.involvedSignals : [];
    const chips = inv.map(e =>
      `<span class="ev"><span class="ev-source">${e.source}</span>` +
      `<span class="ev-desc">${e.description}</span></span>`).join("");
    const el = document.getElementById("insight");
    el.dataset.pattern = insight.id || "";   // l'id del pattern: parla al contratto, non al giocatore
    el.innerHTML =
      `<p class="title">${insight.title}</p>` +
      `<p class="hypothesis">${insight.hypothesis}</p>` +
      `<p class="confidence">confidence ${insight.confidence.toFixed(0)}</p>` +
      (inv.length
        ? `<details class="evidence"><summary>Segnali coinvolti</summary>` +
            `<div class="ev-list">${chips}</div>` +
          `</details>`
        : ``);
  }

  // Rank — mostrato solo se Atlas lo dichiara
  const rp = snapshot.rankPrediction;
  toggle("rankBlock", !!rp);
  if (rp) {
    let html = `<span class="predicted">${rp.predicted}</span>` +
               `<span class="conf">confidence ${rp.confidence.toFixed(0)}</span>`;
    if (rp.real) {
      html += `<p class="gap">reale ${rp.real} · ${rp.gap}</p>`;
    }
    document.getElementById("rank").innerHTML = html;
  }

  // Focus profile
  const focus = snapshot.focusProfile || [];
  toggle("focusBlock", focus.length > 0);
  const focusList = document.getElementById("focus");
  focusList.innerHTML = "";
  focus.forEach(f => {
    const li = document.createElement("li");
    li.innerHTML =
      `<span class="name">${f.area}</span>` +
      `<span class="trend">${f.trend}</span>` +
      `<span class="value">${f.score.toFixed(0)} · ${f.confidence}</span>`;
    focusList.appendChild(li);
  });

  // Battery
  const bat = snapshot.sessionBattery;
  toggle("batteryBlock", !!bat);
  if (bat) {
    document.getElementById("battery").innerHTML =
      `<span class="value">${bat.value}%</span><span class="label">${bat.label}</span>`;
  }

  const coach = document.getElementById("coach");
  coach.innerHTML = "";
  (snapshot.coachHypotheses || []).forEach(h => {
    const li = document.createElement("li");
    // Evidenze: da cosa nasce l'ipotesi. Stesso gesto del cassetto, un livello su.
    // Possono mancare su uno snapshot vecchio: Atlas non dichiara ciò che non sa.
    const ev = Array.isArray(h.evidence) ? h.evidence : [];
    // Ogni evidenza ha una fonte e una frase leggibile. Guardia per gli snapshot
    // vecchi (v3 = stringa): Atlas non dichiara ciò che non sa.
    const chip = e =>
      typeof e === "string"
        ? `<span class="ev">${e}</span>`
        : `<span class="ev"><span class="ev-source">${e.source}</span>` +
          `<span class="ev-desc">${e.description}</span></span>`;
    const evidence = ev.length
      ? `<details class="evidence">` +
          `<summary>Evidenze</summary>` +
          `<div class="ev-list">` +
            ev.map(chip).join("") +
          `</div>` +
        `</details>`
      : ``;
    li.innerHTML =
      `<p class="observation">${h.observation}</p>` +
      `<p class="hypothesis">${h.hypothesis}</p>` +
      `<p class="confidence">confidence ${h.confidence.toFixed(0)}</p>` +
      evidence;
    coach.appendChild(li);
  });

  const updated = document.getElementById("lastUpdated");
  updated.textContent = snapshot.lastUpdated
    ? "osservato il " + new Date(snapshot.lastUpdated).toLocaleString()
    : "";
}

load();
