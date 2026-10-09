# Atlas

**An observation-first software project for understanding structured data.**

Atlas began with a question after a *League of Legends* match: **“What am I not seeing yet?”** Rather than offering live coaching or pretending to know a player's intentions, the project aims to turn recorded observations into inspectable, understandable signals.

Atlas is an independent project created by **Dasion Labaj**.

## Principles

- **Observe before interpreting.** Measured events, interpretations, and uncertainty should remain distinguishable.
- **Do not claim unobserved knowledge.** New states must be grounded in inputs the system actually received.
- **Make reasoning inspectable.** The process behind a signal matters, not just the final output.
- **Keep identity distinct from implementation.** Interfaces, data sources, and programming languages can evolve without redefining the project's purpose.
- **Separate present capabilities from future directions.** A proposal is not an implemented feature.

## Repository guide

| Path | Purpose |
| --- | --- |
| [`src/`](src/) | Source code, including analysis, data ingestion, learning, and API components. |
| [`web/`](web/) | Web interface files. |
| [`Filo/`](Filo/) | Continuity notes: where work stopped and how to resume it. |
| [`Origin/`](Origin/) | The project's original motivations and evolving understanding. |
| [`lib/`](lib/) | Supporting project library files. |

For more detail, see [the project's definition](Filo/atlas_definition.md), [its continuity checkpoint](Filo/atlas_state.md), and [why Atlas exists](Origin/00_Why.md).

## Scope and status

The first implemented domain is *League of Legends* post-match observation, with Java source files and a web interface in this repository. The longer-term possibility of working with other structured domains is a **direction**, not a claim that those integrations are already available.

The historical notes in `Filo/` and `Origin/` reflect when they were written; they should not be mistaken for automatically verified, current release documentation.

## Independent project

Atlas is not affiliated with or endorsed by Riot Games. *League of Legends* and related names are the property of their respective owners.

## A note from the creator

Alongside the technical work, the creator has written [a personal, respectful invitation to Emma Watson](PERSONAL_INVITATION.md). It is **not part of the software**, and it does not imply any affiliation, contact, endorsement, or involvement by Emma Watson.
