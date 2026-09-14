# Damsel — working V1 demo

This is a real, runnable front end for Damsel: a client-only reader you open directly in a
browser (no build step, no server) that implements the V1 decisions from
`docs/damsel-architecture.md`.

## Run it

Just open `index.html` in a browser. For file `import` of PDF/DOCX to work you need internet
access (they load pdf.js / mammoth.js from a CDN on demand) — plain `.txt`/`.md` import works
fully offline.

## What actually works right now

- Import `.pdf`, `.docx`, `.txt`, `.md` — parsed client-side, split into readable pages
- Reader with page navigation, text selection
- Settings modal: paste your own API key for OpenAI, Anthropic, Gemini, ElevenLabs (BYOK,
  stored in `localStorage` only — there is no backend in this demo, keys never leave your
  browser except to go straight to that provider's own API)
- AI Router: an editable table mapping each task (summarize / explain / translate / Q&A / voice)
  to whichever provider you want to handle it
- AI actions: summarize the current page, explain a selection, translate a selection, ask
  free-form questions about the page you're on
- Voice: offline via the browser's built-in speech engine (works with zero setup, zero cost,
  zero internet), or cloud via ElevenLabs if you've added that key

## What's stubbed / not yet built

- **Piper/Kokoro downloadable offline voices** — the architecture doc's higher-quality offline
  tier. Wiring these in needs an on-device ONNX runtime bundle per platform; the current offline
  voice option uses the browser's native TTS instead, which works today but is lower quality.
- **Real RAG over whole books** — "ask about this document" currently uses just the page you're
  currently on as context, not the whole book. Fine for short docs, not yet chunked/embedded
  search over long ones.
- **No backend** — this is a pure client demo. A real product needs a server for: encrypting
  BYOK keys server-side instead of localStorage, cross-device sync, Google Drive integration,
  and the AI Router's cost-estimation feature.
