#!/usr/bin/env python3
"""Riot API reader for ATLAS Rift Prism; reads match + timeline using own token.

Raw match/timeline data remains only on disk in gitignored data/.
NO Riot client injection, gameplay automation or hidden MMR endpoints.
"""
from __future__ import annotations
import argparse
import getpass
import json
import os
from pathlib import Path
import sys
import time
import urllib.error
import urllib.parse
import urllib.request

ALLOWED = ("europe", "americas", "asia", "sea")


def get_json(region: str, route: str, token: str, delay: float = 1.3):
    """Throttle each request and back off if rate limited. Never include token in URL."""
    if region not in ALLOWED or not route.startswith("/") or "//" in route:
        raise ValueError("Invalid Riot API route/region")
    uri = f"https://{region}.api.riotgames.com{route}"
    for attempt in range(5):
        time.sleep(delay)
        req = urllib.request.Request(uri, headers={"X-Riot-Token": token, "Accept":"application/json"})
        try:
            with urllib.request.urlopen(req, timeout=25) as response:
                return json.load(response)
        except urllib.error.HTTPError as exc:
            if exc.code in (401,403):
                raise RuntimeError("Riot refused API key (missing, expired or no access)") from exc
            if exc.code == 404:
                raise RuntimeError("Riot did not find the specified account or match") from exc
            if exc.code not in (429,500,502,503,504) or attempt == 4:
                raise RuntimeError(f"Riot responded HTTP {exc.code}; operation stopped") from exc
            try:
                retry = float(exc.headers.get("Retry-After",""))
            except (ValueError, TypeError):
                retry = float(2**attempt)
            time.sleep(min(120, max(1, retry)))
    raise RuntimeError("Retry limit reached")


def collect(game_name: str, tag_line: str, region: str, count: int, token: str,
            out: Path = Path("data/rift-prism-records.jsonl")) -> dict:
    if not (1 <= count <= 100):
        raise ValueError("--count must be 1..100 (pagination comes later)")
    quote = lambda x: urllib.parse.quote(x, safe="")
    account = get_json(region, f"/riot/account/v1/accounts/by-riot-id/{quote(game_name)}/{quote(tag_line)}", token)
    puuid = account["puuid"]
    query = urllib.parse.urlencode({"queue":420,"count":count})
    ids = get_json(region,f"/lol/match/v5/matches/by-puuid/{quote(puuid)}/ids?{query}",token)
    existing = set()
    if out.exists():
        for line in out.read_text(encoding="utf-8").splitlines():
            if line.strip():
                mid = json.loads(line).get("match",{}).get("metadata",{}).get("matchId")
                if mid:
                    existing.add(mid)
    out.parent.mkdir(parents=True,exist_ok=True)
    saved = 0
    with out.open("a",encoding="utf-8") as file:
        for index, mid in enumerate(ids,1):
            if mid in existing:
                continue
            print(f"[{index}/{len(ids)}] Reading official Riot match and timeline", file=sys.stderr)
            match = get_json(region,f"/lol/match/v5/matches/{quote(mid)}",token)
            timeline = get_json(region,f"/lol/match/v5/matches/{quote(mid)}/timeline",token)
            file.write(json.dumps({"match":match,"timeline":timeline},ensure_ascii=False)+"\n")
            file.flush()
            existing.add(mid)
            saved += 1
    return {"requested":count,"match_ids_found":len(ids),"newly_saved":saved,"output":str(out),
            "privacy":"Raw data includes participant identifiers; keep file locally and out of git."}


def main() -> None:
    p = argparse.ArgumentParser(description="Rift Prism API collection (local only, development key safe)")
    p.add_argument("--game-name", required=True,help="Riot ID name (not summoner name)")
    p.add_argument("--tag-line",required=True,help="Riot ID tag without #")
    p.add_argument("--region",choices=ALLOWED,default="europe")
    p.add_argument("--count",type=int,default=20)
    p.add_argument("--out",type=Path,default=Path("data/rift-prism-records.jsonl"))
    args = p.parse_args()
    token = os.environ.get("RIOT_API_KEY")
    if not token and sys.stdin.isatty():
        token = getpass.getpass("Enter Riot API key (not saved): ").strip()
    if not token:
        p.error("Set RIOT_API_KEY or run interactively to paste key privately")
    try:
        print(json.dumps(collect(args.game_name,args.tag_line,args.region,args.count,token,args.out),indent=2))
    except (RuntimeError,ValueError,OSError,KeyError) as exc:
        p.exit(1, f"Collection failed: {exc}\n")

if __name__ == "__main__":
    main()
