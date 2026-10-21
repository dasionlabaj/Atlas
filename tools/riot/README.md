# ATLAS / Riot Coordinator

Central GitHub Actions entrypoint for **Rift Prism** (experimental League of Legends match outcomes).

The repository dasionlabaj/Atlas is public. The Riot key MUST NOT be written to source code, commit history, issue threads, workflow inputs or logs. The workflow is manual only, with read-only contents permission, and exports only aggregate JSON statistics.

## One-time secure setup

After merging the PR with the new workflow:

1. Open the Atlas repository on GitHub.
2. Select Settings -> Secrets and variables -> Actions -> Secrets.
3. Select New repository secret.
4. Secret name: RIOT_API_KEY
5. Secret value: paste your Riot Developer API key **directly into GitHub**.
6. Select Add secret.

The connector cannot configure secret values, so the repo owner performs this step privately. A repository-level secret cannot automatically be used from Atlas-Lab (a different repository). To update an expired development key, replace the value in GitHub Settings under the same name.

CLI alternative (run on YOUR computer): gh secret set RIOT_API_KEY --repo dasionlabaj/Atlas

## Starting a run

Go to Atlas -> Actions -> ATLAS - Riot Coordinator -> Run workflow.
This is available after the workflow is merged into the default branch.

- Mode demo: synthetic data, no Riot key required.
- Mode live: requires the repository secret plus Riot ID name, tag, region and count.
- The Riot ID and tag workflow inputs may be publicly visible in this public repository. Do not enter private details or credentials as inputs.
- The artifact atlas-riot-aggregate contains aggregate-only findings. It is publicly downloadable in public Actions runs and retained for seven days.

No raw Riot match responses, PUUIDs, names, or per-match IDs are uploaded. Raw data is processed in a temporary GitHub runner directory deleted after processing. Do not add secrets or raw responses to logs.

## Run locally, with no key

Run from Atlas repository root:

    python tools/riot/coordinator.py demo --output out/riot-summary.json
    python -m unittest discover -s tools/riot/tests -v

## Local live run (optional)

In PowerShell, enter a temporary key and run from the Atlas root:

    $env:RIOT_API_KEY = Read-Host "Enter Riot key"
    python tools/riot/coordinator.py live --game-name "YOUR_RIOT_ID_NAME" --tag-line "YOUR_TAG" --region europe --count 20
    Remove-Item Env:\RIOT_API_KEY

Avoid committing the local out/ folder.

## Interpretation

A match with a small total team gold difference can have a large role-to-role gap at 10 minutes. The synthetic sample has a team total difference of zero and top gap of 1,900 gold. Live reports estimate the frequency using official match and timeline endpoints, not hidden Riot MMR. These are descriptive *outcomes*; they do not prove intentional manipulation or show which inputs Riot uses for matchmaking. Stronger tests need pre-game ability proxies, matched cohorts, and held-out evaluation.

Riot Games and League of Legends are trademarks of their owners; Atlas is independent.
