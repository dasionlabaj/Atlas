#!/usr/bin/env python3
"""ATLAS / Rift Prism: empirical, multi-axis League match *outcome* audit.

This is not Riot's MMR, and match outcomes don't establish matchmaker intent.
Consumes official Match-v5 match/timeline JSON stored locally by prism_collect.py.
Python 3.10+, standard library only.
"""
from __future__ import annotations
import argparse
from collections import Counter
import html
import json
import math
from pathlib import Path
import statistics

ROLES = ("TOP", "JUNGLE", "MIDDLE", "BOTTOM", "UTILITY")
TEAMS = (100, 200)


def gold10_snapshot(match: dict, timeline: dict) -> dict | None:
    """Returns at-10m lane/team gold and XP asymmetry; skips incomplete metadata.

    Role pairing uses Match-v5 teamPosition (not inferred map position).
    Timeline participantFrames maps participant IDs as strings.
    """
    info = match.get("info", {})
    if info.get("queueId") != 420:
        return None
    players: dict[int, dict[str, dict]] = {100: {}, 200: {}}
    ids = set()
    for participant in info.get("participants", []):
        team, role, pid = (participant.get("teamId"), participant.get("teamPosition"), participant.get("participantId"))
        if team not in TEAMS or role not in ROLES or not isinstance(pid, int) or pid in ids:
            return None
        if role in players[team]:
            return None
        ids.add(pid)
        players[team][role] = participant
    if len(ids) != 10 or any(set(players[team]) != set(ROLES) for team in TEAMS):
        return None
    frames = timeline.get("info", {}).get("frames", [])
    if not frames:
        return None
    valid = [frame for frame in frames if isinstance(frame.get("timestamp"), (int, float))]
    if not valid:
        return None
    frame = min(valid, key=lambda x: abs(x["timestamp"] - 600_000))
    if abs(frame["timestamp"] - 600_000) > 75_000:
        return None
    pf = frame.get("participantFrames", {})
    role_gold = {}
    role_xp = {}
    for role in ROLES:
        a_id, b_id = (players[100][role]["participantId"], players[200][role]["participantId"])
        a = pf.get(str(a_id), {})
        b = pf.get(str(b_id), {})
        if not a or not b:
            return None
        try:
            a_gold, b_gold = float(a["totalGold"]), float(b["totalGold"])
            a_xp, b_xp = float(a["xp"]), float(b["xp"])
        except (KeyError, TypeError, ValueError):
            return None
        if any(not math.isfinite(x) or x < 0 for x in (a_gold, b_gold, a_xp, b_xp)):
            return None
        role_gold[role] = round(a_gold - b_gold)
        role_xp[role] = round(a_xp - b_xp)
    total_gold_diff = sum(role_gold.values())
    abs_lanes = [abs(role_gold[r]) for r in ROLES]
    masked = abs(total_gold_diff) <= 1500 and max(abs_lanes) >= 1500
    return {
        "match_id": match.get("metadata", {}).get("matchId", "unknown"),
        "patch": ".".join(str(info.get("gameVersion", "unknown")).split(".")[:2]),
        "minute_sampled": round(frame["timestamp"] / 60_000, 2),
        "team_gold_diff_at_10": total_gold_diff,
        "role_gold_diffs_at_10": role_gold,
        "role_xp_diffs_at_10": role_xp,
        "mean_abs_role_gold_gap_at_10": round(statistics.mean(abs_lanes), 1),
        "max_abs_role_gold_gap_at_10": max(abs_lanes),
        "roles_over_1500_gold_gap_at_10": sum(x >= 1500 for x in abs_lanes),
        "masked_lane_gap": masked,
        "winning_team": next((x.get("teamId") for x in info.get("teams", []) if x.get("win") is True), None),
        "game_minutes": round(_duration_minutes(info), 2),
    }


def _duration_minutes(info: dict) -> float:
    seconds = float(info.get("gameDuration", 0))
    if seconds > 72_000:
        seconds /= 1000
    return seconds / 60


def compute_report(records: list[dict]) -> dict:
    valid = []
    skipped = 0
    seen = set()
    for item in records:
        match, timeline = item.get("match", {}), item.get("timeline", {})
        mid = match.get("metadata", {}).get("matchId")
        if mid and mid in seen:
            continue
        if mid:
            seen.add(mid)
        sample = gold10_snapshot(match, timeline)
        if sample is None:
            skipped += 1
        else:
            valid.append(sample)
    n = len(valid)
    masked = sum(row["masked_lane_gap"] for row in valid)
    by_patch = Counter(row["patch"] for row in valid)
    return {
        "project": "ATLAS / Rift Prism 0.2",
        "definition": "At ~10:00: masked if |team total gold difference| <=1500 AND one role gold gap >=1500",
        "complete_ranked_solo_samples": n,
        "skipped_or_unusable_samples": skipped,
        "masked_lane_gap_matches": masked,
        "masked_lane_gap_fraction": round(masked / n, 4) if n else None,
        "masked_lane_gap_wilson95": [round(x, 4) for x in wilson(masked, n)] if n else None,
        "average_max_role_gold_gap_at_10": round(statistics.mean(x["max_abs_role_gold_gap_at_10"] for x in valid), 1) if n else None,
        "average_total_gold_diff_abs_at_10": round(statistics.mean(abs(x["team_gold_diff_at_10"]) for x in valid), 1) if n else None,
        "patch_distribution": dict(by_patch),
        "matches": valid,
        "limitations": [
            "10m gold is an in-game outcome, not hidden MMR or a pre-game strength estimate.",
            "Single-account match history is selected: it is not a representative population.",
            "Thresholds are exploratory; validate against controls, patches, ranks and queue effects.",
            "A high fraction cannot by itself establish intentional manipulation or an incomplete real matchmaker.",
        ],
    }


def wilson(success: int, n: int, z: float = 1.96) -> tuple[float, float]:
    if n == 0:
        return (0.0, 1.0)
    p = success / n
    denom = 1 + z*z/n
    center = (p + z*z/(2*n)) / denom
    half = z * math.sqrt(p*(1-p)/n + z*z/(4*n*n)) / denom
    return (max(0.0, center-half), min(1.0, center+half))


def make_html(report: dict) -> str:
    """Static report; no external JavaScript/CDN; no API key or raw PUUIDs."""
    n = report["complete_ranked_solo_samples"]
    fraction = report["masked_lane_gap_fraction"]
    rows = []
    for m in report["matches"]:
        gaps = m["role_gold_diffs_at_10"]
        cols = "".join(f"<td>{int(gaps[r]):+d}</td>" for r in ROLES)
        rows.append("<tr><td>" + html.escape(str(m["match_id"])) + "</td><td>" +
                    html.escape(str(m["patch"])) + "</td><td>" +
                    str(m["team_gold_diff_at_10"]) + "</td>" + cols +
                    "<td>" + ("SI" if m["masked_lane_gap"] else "NO") + "</td></tr>")
    percent = f"{fraction*100:.1f}%" if fraction is not None else "—"
    return f"""<!doctype html><html lang='it'><head><meta charset='utf-8'>
<meta name='viewport' content='width=device-width, initial-scale=1'><title>ATLAS / Rift Prism</title>
<style>body{{font:16px system-ui;background:#0b1525;color:#e3edf8;margin:2rem auto;max-width:1100px;padding:0 1rem}}
h1{{color:#7ee8f5}}.stats{{display:flex;flex-wrap:wrap;gap:1rem}}.stat{{background:#1b2d41;padding:1rem;border-radius:12px;min-width:170px}}
strong{{font-size:1.8rem;display:block}}table{{border-collapse:collapse;width:100%;font-size:.9rem}}
th,td{{border-bottom:1px solid #355168;padding:.65rem;text-align:right}}td:first-child,th:first-child{{text-align:left}}
.scroll{{overflow:auto}}p,small{{color:#bdd0e0}}th{{color:#7ee8f5}}</style></head><body>
<h1>ATLAS / Rift Prism <small>0.2</small></h1>
<p>Esperimento: un totale oro di squadra equilibrato puo nascondere squilibri di ruolo a 10 minuti.</p>
<section class='stats'><div class='stat'><strong>{n}</strong>campioni completi</div>
<div class='stat'><strong>{percent}</strong>gap di ruolo mascherati</div>
<div class='stat'><strong>{report['masked_lane_gap_matches']}</strong>partite sopra soglia</div></section>
<h2>Differenze di oro a ~10 minuti (squadra 100 − squadra 200)</h2>
<div class='scroll'><table><thead><tr><th>Match</th><th>Patch</th><th>Totale</th>
<th>TOP</th><th>JGL</th><th>MID</th><th>BOT</th><th>SUP</th><th>Gap</th></tr></thead>
<tbody>{''.join(rows)}</tbody></table></div>
<p><small>Criterio: differenza totale <=1500 oro e differenza in almeno una corsia >=1500 oro.
I risultati a 10 minuti non rivelano l'MMR, non stimano la probabilita pre-partita e non dimostrano causalita.
Dati derivati da match API; analisi esplorativa, soggetta a selezione del campione.</small></p>
</body></html>"""


def example_record() -> dict:
    """Deterministic, explicitly synthetic API-shaped example with hidden lane gap."""
    players = []
    for i, role in enumerate(ROLES):
        players.append({"participantId":i+1, "teamId":100,"teamPosition":role})
        players.append({"participantId":i+6,"teamId":200,"teamPosition":role})
    blue_gold = [5400, 4200, 5000, 3500, 2500]
    red_gold  = [3500, 4500, 4800, 4900, 2900]
    frames = {}
    for i, gold in enumerate(blue_gold):
        frames[str(i+1)] = {"totalGold":gold,"xp":4000+i*100}
    for i, gold in enumerate(red_gold):
        frames[str(i+6)] = {"totalGold":gold,"xp":4100+i*100}
    return {"match":{"metadata":{"matchId":"SYNTHETIC_DEMO_001"},
                     "info":{"queueId":420,"gameDuration":1800,"gameVersion":"16.20.1",
                             "participants":players,"teams":[{"teamId":100,"win":True},{"teamId":200,"win":False}]}},
            "timeline":{"info":{"frames":[{"timestamp":600000,"participantFrames":frames}]}}}


def main() -> None:
    parser = argparse.ArgumentParser(description="ATLAS Rift Prism 0.2 (offline timeline audit)")
    sub = parser.add_subparsers(dest="command", required=True)
    sub.add_parser("demo", help="Synthetic role-vs-total counterexample")
    report = sub.add_parser("report", help="Build a real report from local JSONL collected via Riot API")
    report.add_argument("--input", type=Path, default=Path("data/rift-prism-records.jsonl"))
    report.add_argument("--output", type=Path, default=Path("data/rift-prism-report.json"))
    report.add_argument("--html", type=Path, default=Path("data/rift-prism-report.html"))
    args = parser.parse_args()
    if args.command == "demo":
        result = compute_report([example_record()])
        print(json.dumps(result, indent=2, ensure_ascii=False))
    else:
        records = [json.loads(line) for line in args.input.read_text(encoding="utf-8").splitlines() if line.strip()]
        result = compute_report(records)
        args.output.parent.mkdir(parents=True, exist_ok=True)
        args.html.parent.mkdir(parents=True, exist_ok=True)
        args.output.write_text(json.dumps(result,indent=2,ensure_ascii=False),encoding="utf-8")
        args.html.write_text(make_html(result),encoding="utf-8")
        print(f"Report: {args.html} ({result['complete_ranked_solo_samples']} usable samples)")

if __name__ == "__main__":
    main()
