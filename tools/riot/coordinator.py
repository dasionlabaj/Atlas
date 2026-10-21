#!/usr/bin/env python3
"""Public Atlas GitHub Actions coordinator for Riot observations.

Secrets are read from process environment only. Only aggregate metrics are exported.
Do not log or upload raw API responses, PUUIDs or the API key.
"""
from __future__ import annotations
import argparse
import json
import os
from pathlib import Path
import tempfile
from prism_collect import collect
from rift_prism import compute_report, example_record


def summary_from_records(records: list[dict]) -> dict:
    full = compute_report(records)
    # Never publish match identifiers or participant details to a public Actions artifact.
    result = {key: value for key, value in full.items() if key != "matches"}
    result["distribution_scope"] = "aggregated match outcomes only"
    result["contains_raw_player_identifiers"] = False
    return result


def run_live(game_name: str, tag_line: str, region: str, count: int) -> dict:
    key = os.environ.get("RIOT_API_KEY")
    if not key:
        raise ValueError("Missing RIOT_API_KEY GitHub Actions secret")
    # Temporary file is never committed or attached to an artifact.
    with tempfile.TemporaryDirectory(prefix="atlas-riot-") as tmp:
        path = Path(tmp) / "raw.jsonl"
        collect(game_name, tag_line, region, count, key, path)
        records = [json.loads(x) for x in path.read_text(encoding="utf-8").splitlines() if x.strip()]
        return summary_from_records(records)


def main() -> None:
    parser = argparse.ArgumentParser(description="Atlas coordinates Rift Prism through a repo Actions Secret")
    parser.add_argument("mode", choices=["demo", "live"])
    parser.add_argument("--game-name")
    parser.add_argument("--tag-line")
    parser.add_argument("--region", choices=["europe", "americas", "asia", "sea"], default="europe")
    parser.add_argument("--count", type=int, default=20)
    parser.add_argument("--output", type=Path, default=Path("out/riot-summary.json"))
    args = parser.parse_args()
    if args.mode == "demo":
        result = summary_from_records([example_record()])
        result["data_kind"] = "synthetic"
    else:
        if not args.game_name or not args.tag_line:
            parser.error("Live mode needs --game-name and --tag-line")
        result = run_live(args.game_name, args.tag_line, args.region, args.count)
        result["data_kind"] = "real_match_api_observations"
    args.output.parent.mkdir(parents=True, exist_ok=True)
    args.output.write_text(json.dumps(result, indent=2, ensure_ascii=False), encoding="utf-8")
    print(f"Atlas Rift Prism: {result['complete_ranked_solo_samples']} complete samples; "
          f"{result['masked_lane_gap_matches']} matched exploratory imbalance threshold.")
    print("Only aggregate metrics exported. Interpret as outcomes, not Riot hidden MMR.")


if __name__ == "__main__":
    main()
