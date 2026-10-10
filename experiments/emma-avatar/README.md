# Emma Avatar Concept 0.1 — Atlas experiment

A **standalone, local, fictional avatar prototype** inspired by Dasion's [personal invitation](../../PERSONAL_INVITATION.md) to Emma Watson.

This is a **creative simulation**, not Emma Watson, not an authorized likeness/voice replica, and not a communication channel to her. The illustrated face is an original concept illustration, not a photograph. No endorsement, contact or participation by Emma Watson is implied.

## Run

Open `index.html` directly in a current browser. No server, API key, npm installation or build step is required.

## What exists (observed)

- Original illustrated character concept in an HTML/SVG/CSS view.
- Interactive local chat using a **fixed scripted rule engine** (not a trained model and not an LLM).
- Four preset prompts, message entry, accessible message log and reset.
- A link to the **original invitation**, without representing it as read or answered by Emma.
- No network requests, storage, telemetry, microphone or synthetic voice.
- The current test conversation exists only within the open page and disappears on refresh.

## What does *not* exist

- No connection to Emma Watson or to any of her personal information.
- No representation of what Emma knows, feels or intends.
- No actual generative AI integration.
- No voice cloning, photo likeness, biometric identification or surveillance.
- No publishing, contacting, sending or messaging functionality.

## Suggested validation

1. Open the HTML and confirm the visible notice identifies the avatar as fictional.
2. Ask **Chi sei?** and check it does not claim to be Emma Watson.
3. Ask **Cosa sa Emma di me?** and check it does not invent access or private knowledge.
4. Ask **Parlami di Atlas** and check the response distinguishes observation and inference.
5. Enter special characters like `<script>` to verify user text is rendered as plain text.
6. Click **Ricomincia** and check prior messages disappear.
7. Confirm in browser developer tools that no requests are sent as messages are entered.

## Future trajectory — not yet implemented

After validation, add a standalone server-side LLM adapter that uses a clear simulation prompt, keeps the user in control, avoids personal-data inference and records explicit test outcomes. API credentials must stay server-side. This new capability should remain separate from `src/core/` and the Riot pipeline until its behavior is validated.

**Atlas principle:** observation, interpretation and unknown are not interchangeable.