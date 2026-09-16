# Damsel — Android vertical slice

This is a real, compiling Android app — not a mockup. It implements the vertical slice called for
in `docs/damsel-master-spec.md` §88: **import a PDF → open it → select text → Ask Damsel → pick a
voice → choose an emotional style → hear it read aloud → your place is saved and resumed.**

It was built and verified with `./gradlew assembleDebug` in this repo (Kotlin 1.9.24, AGP 8.5.2,
compileSdk 34, minSdk 26) — the command produces a real installable APK.

## Run it

Open the `android/` folder in Android Studio (Koala or newer), let it sync, and run the `app`
configuration on a device or emulator. Or from a terminal with the Android SDK installed:

```
cd android
./gradlew assembleDebug
adb install app/build/outputs/apk/debug/app-debug.apk
```

On first launch: tap **Add a book**, pick a PDF, then open **AI & Providers** (top-right gear) and
paste an OpenAI API key — that one key powers both Ask Damsel and voice narration in this slice.

## What's real here (per spec §81 Rule 1 — no fake functionality)

- **PDF import** extracts genuine per-page text with pdfbox-android, not a placeholder.
- **Reading progress** is a real Room row (`reading_progress`) written every time you turn a page,
  and read back to resume at the exact page on reopen — never fabricated (§42).
- **BYOK key storage** uses Android Keystore-backed `EncryptedSharedPreferences` (§25) — the key is
  masked everywhere in the UI after entry and never touches Room or logs.
- **Ask Damsel** and **voice narration** are live network calls to OpenAI (chat completions and
  `gpt-4o-mini-tts`) using your own key — if no key is set, the UI says so plainly instead of
  faking a response.
- **Text selection** uses a real read-only `BasicTextField` selection range, so "Ask about this
  selection" sends the text you actually highlighted, not the whole page.
- Both **AiRouter** and **VoiceRouter** exist as real abstractions between the UI and the concrete
  OpenAI adapters (spec §20/§21, Rules 5–6) — adding Anthropic/Gemini/ElevenLabs later means adding
  an adapter class and registering it in the router's provider list, not touching any screen.
- **Highlights** (spec §38) are real Room rows tied to an exact text range, rendered back as colored
  spans over the page text — pick a color after selecting text, add an optional note, view/delete
  them from the highlights icon in the top bar.
- **Audio player controls**: seek bar, ±10s skip, pause/resume, five speed presets (0.75x–2x
  applied live via `MediaPlayer.PlaybackParams`), and a real sleep timer that stops narration and
  counts down on-screen — not decoration, all wired to the actual `MediaPlayer` instance.
- **Genre tags** on your own library (long-press a book to tag it, filter chips at the top of your
  shelf) — this is deliberately *not* a fake book-discovery/catalog screen; there's no content
  backend behind it, just organizing what you've actually imported.

## What's intentionally not here yet

This is Stage 1–4's vertical slice only (spec §87). Not yet built: EPUB/DOCX import, the full
10-voice library (only OpenAI's 6 voices are wired up — the Voice Studio sheet says so), bookmarks
and free-form notes, the research workspace, learning system (flashcards/quizzes), gamification,
social/clubs, cloud sync, and the backend services section 69–72 describes. A real book-discovery
catalog (browse-by-genre with cover art, like a bookstore) needs a content backend and hasn't been
built — see the genre-tagging note above for the honest, scoped-down version that ships instead.
The module boundaries (`ai/`, `voice/`, `data/`, `ui/screens/`) are laid out so those become
additions, not a rewrite, and match the eventual `core/`+`feature/` structure in spec §5 once the
app outgrows a single module.

## Fixed: crash on "read aloud"

An earlier build could crash the whole app instead of showing an error when narration playback hit
a bad audio response — `MediaPlayer.setDataSource()`/`prepareAsync()` were called with no
try/catch inside a coroutine, so any failure there had nowhere safe to land and took the app down
with it. `ReaderViewModel.playAudio()` now wraps all of that, and the OpenAI provider/voice
adapters catch `Exception` broadly (not just `IOException`) so an unexpected response shape ends in
a plain on-screen error instead of a crash.

## Known trade-off

Voice playback uses `android.media.MediaPlayer` for this slice rather than the Media3/ExoPlayer
stack the spec calls for (§4, §12) — background playback and lock-screen controls need ExoPlayer's
`MediaSessionService`, which is real additional work for Stage 3, not something to fake here.
