# DAMSEL AI READER — Master Product & Engineering Specification

Android — Cloud AI — Multi-Provider — AI Reading Platform

> **Status note (added by the build, not part of the original spec):** this is the full north-star
> specification exactly as given by the product owner. It describes the complete, eventual product —
> a multi-year build. See `android/README.md` for what is actually implemented and running today,
> and Section 88 below ("Final Implementation Rule") for the order this codebase is being built in.

---

## 1. PRODUCT DEFINITION

**Product name**
Damsel AI Reader

**Product category**
Universal AI Reading, Listening, Research, and Learning Platform.

**Primary users**

1. Audiobook lovers.
2. Researchers.
3. Students.
4. Professionals.
5. General readers.
6. People who prefer listening to reading.
7. People who want to understand books, papers, articles, and documents rather than merely consume them.

**Product positioning**
Damsel is not a PDF reader with text-to-speech.
Damsel is an AI reading companion that can:

- read books aloud,
- understand books,
- discuss books,
- summarize books,
- explain difficult content,
- adapt narration,
- help users research,
- remember reading context,
- help users learn,
- organize knowledge,
- and make reading emotionally engaging.

**Experience goal**
The user should feel: "I am reading with an intelligent, elegant companion."
The application must feel: premium, calm, cinematic, elegant, intelligent, extremely responsive,
visually polished, simple despite powerful functionality, never technical unless the user
deliberately opens advanced settings.

## 2. CORE PRODUCT DECISION

Damsel is cloud-AI-first. There is no requirement for offline AI inference.

The app requires internet connectivity for: AI conversations, cloud voice generation, AI
summaries, AI explanations, AI recommendations, cloud synchronization, social functionality,
research functionality, cloud speech processing.

The application may cache: imported books, generated audio segments, thumbnails, metadata,
reading progress, recent AI responses — for performance and session continuity. Caching must
never be presented as a full offline AI mode.

## 3. PRIMARY PRODUCT PILLARS

1. Library
2. Immersive Reader
3. AI Voice Studio
4. Damsel Companion
5. AI Research Workspace
6. Knowledge & Learning
7. Social Reading
8. Gamification
9. Cloud/Provider Platform

## 4. TECHNOLOGY STACK

**Android:** Kotlin, Jetpack Compose, Material 3, Navigation Compose, ViewModel, Kotlin
Coroutines, Flow, Hilt, Room, DataStore, WorkManager, Media3 / ExoPlayer, Android Keystore, Coil,
Paging where useful.

**Architecture:** Clean Architecture + MVVM + modular feature architecture. Do not put business
logic directly into composables. Do not allow UI classes to directly call AI providers. Do not
allow screens to directly manipulate Room.

## 5. PROJECT MODULE STRUCTURE

```text
damsel/
│
├── app/
│
├── core/
│   ├── common/
│   ├── model/
│   ├── database/
│   ├── network/
│   ├── security/
│   ├── analytics/
│   ├── design/
│   ├── audio/
│   ├── storage/
│   └── testing/
│
├── feature/
│   ├── onboarding/
│   ├── home/
│   ├── library/
│   ├── reader/
│   ├── player/
│   ├── voice/
│   ├── companion/
│   ├── research/
│   ├── notes/
│   ├── learning/
│   ├── social/
│   ├── challenges/
│   ├── rewards/
│   ├── statistics/
│   ├── search/
│   ├── downloads/
│   ├── profile/
│   ├── settings/
│   ├── providers/
│   ├── api_keys/
│   └── accessibility/
│
└── backend/
    ├── api/
    ├── auth/
    ├── ai-router/
    ├── voice/
    ├── documents/
    ├── research/
    ├── sync/
    ├── social/
    ├── recommendations/
    ├── notifications/
    ├── analytics/
    └── storage/
```

## 6. APP NAVIGATION

Primary bottom navigation: `Home · Library · Read · Companion · Profile`.
Additional functionality is accessed contextually rather than crowding the bottom bar.

## 7. ONBOARDING

- **Screen 1 — Welcome.** Large Damsel branding. "Meet your reading companion." Actions: Get Started, Sign In.
- **Screen 2 — Reading Preferences.** What do you read? Books / Research / Academic papers / Articles / Documents / Everything. Multi-select.
- **Screen 3 — Listening Preferences.** Prefer reading / Prefer listening / Both.
- **Screen 4 — Voice Preference.** Play real voice previews. Choose Female / Male / Neutral / Warm / Deep / Soft / Cinematic. Do not install or activate any device TTS.
- **Screen 5 — Damsel Introduction.** Damsel explains herself conversationally.
- **Screen 6 — Notification Permission.** Explain why notifications are useful.
- **Screen 7 — Account.** Google / Email / Apple when supported / Guest-local session where appropriate.

## 8. HOME SCREEN

Personalized. Top greeting ("Good afternoon, [Name] — Ready to continue?"). Primary
continue-reading card with cover, chapter, % complete, Resume action. Today's Reading minutes.
Damsel Recommendation (contextual, e.g. "You've been reading research papers this week..."). Voice
Shortcut showing the currently selected voice. Daily Challenge card. Recent Books carousel.

## 9. LIBRARY

Sections: Continue Reading, All Books, Audiobooks, Research, Articles, Favorites, Collections,
Recently Added, Finished, Downloads, Shared With Me.

**Supported formats:** PDF, EPUB, MOBI, AZW/AZW3 where legally accessible, DOC, DOCX, TXT, HTML,
RTF, Markdown, CBZ/CBR where implementation permits, imported audiobooks, web articles, research
documents.

Import must automatically: identify file type, extract metadata, detect title, detect author,
extract cover where possible, detect chapters, index text, generate searchable content, generate
AI metadata when requested, add the document to the Library.

## 10. READER

The most important screen. Visually distraction-free. Normal mode shows content, progress,
page/chapter info, minimal controls that disappear after inactivity. Toolbar (when opened): Back,
Table of Contents, Search, Bookmark, Highlight, Note, Ask Damsel, Audio, More.

## 11. READING MODES

Standard, Focus (minimal UI), Immersive (full-screen), Research (content alongside notes/refs/AI
tools), Listening (text visible while Damsel narrates), Night (reduced intensity, warmer display).

## 12. AUDIO READER

Player supports: play, pause, skip backward/forward, chapter navigation, playback speed, sleep
timer, voice selection, voice preset, queue, playback position, background playback, lock-screen
controls, Bluetooth/headset controls. Voice must stream/generate from cloud AI services.

## 13. VOICE SYSTEM

Flagship feature. The app must NEVER expose messages such as "Pitch increased", "Pitch +2", "TTS
parameter changed", "Voice engine changed" — those are internal operations and must never appear
to the reader. The user interacts with natural concepts only.

## 14. VOICE LIBRARY

Launch with at least 10 curated voices — product identities, not impersonations of real people:
Damsel, Merlin, Aurora, Luna, Iris, Athena, Nova, Sage, Echo, Kai. Every voice has: name,
description, gender presentation, accent, language, sample, supported styles, emotional range,
quality rating, provider, latency estimate.

## 15. VOICE STUDIO

Controls: Voice, Emotion, Energy, Warmth, Confidence, Expressiveness, Empathy, Intensity,
Whisper, Narration Style, Speaking Speed, Pause Style, Breathing Style, Dialogue Intensity,
Seriousness, Humor, Romantic Tone. Do not expose dozens of technical signal-processing controls by
default — those live behind "Advanced Voice Settings".

## 16. EMOTION PRESETS

Neutral, Gentle, Happy, Sad, Excited, Dramatic, Romantic, Calm, Serious, Suspenseful, Mysterious,
Angry, Hopeful, Melancholic, Energetic, Friendly, Empathetic, Whispering, Professional,
Motivational.

## 17. VOICE PRESETS

Users can save complete configurations, e.g.:

**Movie Narrator:** Voice: Merlin · Emotion: Dramatic · Energy: High · Speed: 1.05 · Whisper: Off
· Intensity: Strong · Warmth: Medium · Confidence: High · Expressiveness: High · Pause Style:
Cinematic

**Gentle Companion:** Voice: Damsel · Emotion: Gentle · Energy: Low · Speed: 0.95 · Whisper: Off ·
Intensity: Soft · Warmth: High · Confidence: Medium · Expressiveness: Medium · Pause Style:
Natural

Allow: Save preset, Rename, Duplicate, Edit, Delete, Share preset.

## 18. INTELLIGENT EMOTION MODE

"Auto Emotion" — Damsel analyzes text context and chooses appropriate delivery (a whispered line
reads as whispering, a scream reads with high intensity, a reflective passage reads reflectively,
a dramatic event reads dramatically). User can disable Auto Emotion at any time.

## 19. DIALOGUE MODE

Damsel detects dialogue: narrator uses the selected narrator voice; different characters get
subtle vocal variation rather than arbitrary voice changes. "Character Voices" setting: Off /
Subtle / Expressive / Cinematic.

## 20. AI VOICE PROVIDER ABSTRACTION

Never hardcode a single provider. `interface VoiceProvider` with capabilities: `getVoices()`,
`generateSpeech()`, `streamSpeech()`, `estimateCost()`, `supportsEmotion()`, `supportsStyles()`,
`supportsLanguages()`, `supportsStreaming()`. Providers are interchangeable — e.g. ElevenLabs,
Google, OpenAI-compatible voice services, and future providers — without changing the Reader UI.

## 21. AI ROUTER

All AI requests go through an internal abstraction: `AIRequest`, `AIProvider`, `AIResponse`,
`AIUsage`, `AIRouter`, `ProviderCapabilities`, `RoutingPolicy`. The router determines task,
preferred provider, availability, user-selected provider, BYOK availability, rate limits,
estimated cost, fallback provider.

## 22. AI TASK TYPES

Conversation, Summarization, Research, Question Answering, Translation, Rewriting, Explanation,
Vocabulary, Quiz Generation, Flashcards, Recommendations, Book Analysis, Citation Extraction, Mind
Map Generation, Character Analysis, Timeline Generation, Voice Script Preparation.

## 23. SMART AI ROUTING

Example: user asks "Explain this research methodology" → router resolves task =
explanation/research → checks enabled providers → selects best available → executes → returns →
stores result in book conversation history. Modes: **Auto** (Damsel decides), **Manual** (user
chooses provider/model), **Custom** (user creates routing rules).

## 24. BYOK — BRING YOUR OWN API KEY

`Settings → AI & Providers`. Users add keys independently. Initial adapters: Gemini, OpenAI,
Anthropic, xAI, OpenRouter, ElevenLabs, with more later. Every provider has: Enable/Disable, API
Key, Model, Default for task, Test Connection, Usage, Reset.

## 25. API KEY SECURITY

Keys must never be displayed in full after initial entry, never logged, never sent to analytics,
never written to ordinary Room tables, never included in crash reports. Store locally using
Android Keystore + encrypted storage. Mask keys (`sk-••••••••••91KD`). For providers requiring
server-side secrets, use the backend provider gateway instead of exposing an application master
key.

## 26. API ROUTING PREFERENCE

Configurable defaults: Default AI Provider, Default Voice Provider, Default Research Provider,
Default Translation Provider, Default Summary Provider — or Auto-select best provider.

## 27. DURATION / COST CONTROLS

Show optional usage info (e.g. "Provider: Gemini — Usage this month: 2.4M tokens"; "Voice usage:
23 minutes"). Do not interrupt reading with cost popups unless the user explicitly enabled usage
warnings.

## 28. DAMSEL COMPANION

Feels like a personified intelligent reading partner, not a chatbot bolted on. Can: explain
passages, answer questions, summarize chapters, discuss characters, remember previous
conversations, suggest related ideas, help research, quiz the user, recommend books, encourage
reading, explain difficult vocabulary.

## 29. COMPANION CONTEXT

Every conversation understands: current book, current chapter, current paragraph, current
selection, book metadata, user highlights, book notes, recent conversation, relevant previous
conversations. A user can ask "What did she mean here?" and Damsel understands what "here" means.

## 30. BOOK-SPECIFIC AI MEMORY

Each book has its own AI context: Conversations, Notes, Highlights, Summaries, Questions,
Research, Flashcards, AI Memory.

## 31. GLOBAL DAMSEL MEMORY

Separate from book memory. May optionally remember: favorite genres, reading habits, learning
goals, favorite voices, favorite styles, research interests, preferred explanation depth. User
must be able to view, edit, delete, and disable this memory.

## 32. PERSONALIZED AUDIO SUMMARIES

"Generate Audio Summary" produces a text summary, a structured summary, and spoken narration.
Options: 2/5/10 minutes, Detailed, Executive, Academic, Story-style. Uses the user's selected
voice/preset.

## 33. AI RESEARCH WORKSPACE

Tools: Summary, Ask Damsel, Notes, Highlights, References, Citations, Related Documents, Concept
Map, Timeline, Key Findings, Methodology, Definitions, Research Questions.

## 34. RESEARCH ASSISTANT

Example queries: "What is the main argument?", "Find evidence for this claim.", "Compare these two
papers.", "What methodologies were used?", "What are the limitations?", "Explain this for a
beginner." Damsel answers with source references back to the document where possible.

## 35. MULTI-DOCUMENT RESEARCH

Users select multiple documents into a Research Set and ask cross-document questions (e.g.
"Compare the arguments across all four."). The AI context engine retrieves relevant passages
rather than blindly sending entire documents.

## 36. SEARCH

Global search across books, documents, notes, highlights, conversations, authors, extracted text,
research sets. Results indicate source (e.g. "Found in: Atomic Habits — Chapter 4 — Page 73").
Tapping a result opens the exact location.

## 37. NOTES

Plain notes, selected-text notes, voice notes, AI-assisted notes. AI actions: summarize, expand,
turn into flashcard, turn into question, organize, tag.

## 38. HIGHLIGHTS

Colors: yellow, blue, purple, green, red. Each highlight can have a note, tags, AI explanation,
share action.

## 39. LEARNING SYSTEM

Flashcards (Q→A), Quiz (multiple choice), Recall (open answer), Explain (user explains a concept
and Damsel evaluates understanding).

## 40. READING PACE ENGINE

Relaxed / Natural / Focused / Fast / Custom. AI may adapt speaking speed, pauses, sentence
grouping, paragraph transitions — changes must be gradual, never abrupt.

## 41. IMMERSIVE AUDIO BEHAVIOR

While narrating: highlight currently spoken text, auto-scroll, maintain position, keep voice
visible, avoid UI interruptions, prevent unnecessary screen changes. On pause: remember exact
position. On resume: continue from that exact position.

## 42. READING PROGRESS

Track real activity only, never fabricate progress. A session counts only after meaningful
interaction. Track: session start/end, active reading duration, pages viewed, characters/words
consumed, audio duration, chapters completed, pause/resume events, book position. A reading day
counts only when the user actually reads/listens past the app's configured minimum activity
threshold.

## 43. STREAKS

Based on real activity against the daily goal (e.g. goal 20 min, user reads 21 min → day
complete). Never mark a day complete just because the app opened or a date passed.

## 44. DAILY GOALS

Minutes listened, pages read, chapters completed, sessions, books, words. Shown as e.g. "Today's
Goal — 20 minutes — 14/20 — 70%".

## 45. GAMIFICATION

Rewards reading, finishing chapters/books, maintaining goals, completing research tasks, learning
via quizzes, joining challenges. System: XP, Levels, Achievements, Badges, Streaks, Milestones,
Challenges, Rewards. Must not feel childish — visual style stays elegant and premium.

## 46. READING CHALLENGES

E.g. "Read 100 pages this week", "Finish a research paper", "Read 30 minutes for 7 days",
"Complete 3 books this month", "Read a book outside your normal genre".

## 47. READING CLUBS

Private, public, or invitation-only. Features: club library, member list, shared progress,
discussions, comments, challenges, scheduled reading sessions, shared highlights.

## 48. COLLABORATIVE READING

A club picks a book, start date, target chapters, weekly goal. Members see group progress (e.g.
"The Great Gatsby — Club Progress 67% — You 72% — Group 67%").

## 49. LIVE READING ROOMS

Future-ready architecture should support synchronized reading position, voice narration,
reactions, discussion, host controls.

## 50. STATISTICS

Dashboard: Minutes Read, Minutes Listened, Books Finished, Pages Read, Words Read, Longest
Session, Current Streak, Best Streak, Favorite Genres, Favorite Voices, Favorite Reading Times.
Visuals: weekly/monthly/yearly charts, heatmap, book completion chart.

## 51. PROFILE

Avatar, name, level, XP, books completed, reading streak, favorite genre, achievements, favorite
voice, reading statistics.

## 52. SETTINGS

Groups: **Reading** (font, text size, spacing, theme, margins, auto-scroll, highlighting),
**Audio** (default voice, default preset, speed, auto emotion, character voices, background
playback), **AI** (provider, model, routing, memory, summaries), **API Keys** (providers, keys,
connection testing), **Account** (profile, devices, security), **Privacy** (AI memory, analytics,
personalization), **Accessibility** (font size, contrast, reduced motion, screen reader support,
haptic settings).

## 53. CLOUD SYNC

Sync: library metadata, reading progress, highlights, notes, bookmarks, goals, achievements, AI
conversations, settings, voice presets, Damsel memory. Books themselves use cloud storage only
when the user explicitly uploads/saves them there.

## 54. CONFLICT RESOLUTION

Every synced object requires `id`, `createdAt`, `updatedAt`, `deviceId`, `version`. Deterministic
policy per type: progress → latest active position; notes → merge; settings → latest update;
achievements → union; highlights → merge.

## 55. NOTIFICATION SYSTEM

Useful, not annoying (e.g. "You've got 10 minutes left to complete today's reading goal.", "Your
reading club starts in 30 minutes.", "You left Chapter 8 at a fascinating point."). Fully
customizable.

## 56. DESIGN SYSTEM

Minimalist luxury inspired by Apple, without copying Apple's UI. High-quality typography,
restrained gradients, deep blacks, soft whites, subtle purple/lavender accent, elegant cards,
generous whitespace, smooth animations, subtle blur. Avoid: excessive neon, clutter, giant labels,
unnecessary borders, childish gamification, excessive glassmorphism, intrusive popups.

## 57. ANIMATION

Animations communicate state (book added: cover → soft scale → library placement; reading
complete: subtle completion animation; voice change: waveform morph; achievement: restrained
celebratory animation). No unnecessary animation while reading.

## 58. PERFORMANCE TARGETS

60 FPS minimum UI, 120 FPS where supported. Reader opens quickly. Large books must not freeze UI.
Parsing off main thread. AI calls never block UI. Audio generation never blocks UI. Search is
asynchronous. Image decoding is asynchronous.

## 59. ACCESSIBILITY

TalkBack, dynamic font sizes, high contrast, large controls, reduced motion, semantic labels,
one-handed use, keyboard support where applicable, adjustable text spacing.

## 60. ERROR HANDLING

Never display raw exceptions to users. Bad: `NullPointerException at VoiceRepository.kt:148`.
Good: "Damsel couldn't generate that voice segment. Tap Retry." Technical detail belongs in logs
only.

## 61. NETWORK STATES

Handle: Connecting, Connected, Slow, Temporarily unavailable, Provider unavailable, Rate limited,
Invalid API key, Quota exceeded — always explained in plain language (e.g. "Your Gemini API key
has reached its provider limit. You can switch providers or add another key.").

## 62. AI RESPONSE STREAMING

Stream AI responses progressively where supported — never make the user wait for a full response
when partial output can safely be shown. For voice: request manageable segments, stream/play audio
as early as possible, prefetch the next segment, maintain a small playback buffer, avoid long
silence between segments.

## 63. AUDIO PIPELINE

```text
Book Text → Text Cleaner → Sentence Segmenter → Context Analyzer → Emotion/Style Planner
→ Voice Router → Voice Provider → Audio Stream → Audio Buffer → Media3 Player → User
```

## 64. TEXT PREPROCESSING

Remove/normalize: broken whitespace, page numbers, repeated headers, OCR artifacts, malformed
punctuation. Preserve: paragraphs, quotations, headings, lists, dialogue, citations. The voice
engine must receive clean text.

## 65. DOCUMENT UNDERSTANDING

For each imported document, build: Document, Sections, Chapters, Paragraphs, Sentences, Metadata,
Embeddings, Entities, References — to support AI retrieval and search.

## 66. RETRIEVAL ARCHITECTURE

Long books must not be blindly sent to an LLM.

```text
Document → Parser → Chunker → Metadata extraction → Embedding generation → Vector index
→ Query → Retriever → Relevant chunks → AI model
```

Needed for book Q&A, research, summaries, comparisons, memory.

## 67. AI MEMORY ARCHITECTURE

Distinct scopes, never mixed indiscriminately: Session Memory, Book Memory, Research Workspace
Memory, User Memory, Conversation Memory.

## 68. SECURITY

Encrypted local sensitive storage, Android Keystore, TLS, secure authentication, token rotation,
server-side authorization, signed requests where appropriate, API key masking, no secrets in Git,
no secrets in logs, no API keys in analytics.

## 69. BACKEND ARCHITECTURE

Services: API Gateway, Auth Service, User Service, Library Service, Document Service, AI Router,
Voice Service, Memory Service, Research Service, Sync Service, Social Service, Gamification
Service, Recommendation Service, Notification Service, Analytics Service, Storage Service. May
initially deploy as a modular monolith for simplicity/cost, but code boundaries must stay
service-oriented so they can be split later. Do NOT prematurely deploy dozens of microservices.

## 70. BACKEND DATABASE

Relational database for authoritative data. Core tables: `users, devices, books, documents,
document_sections, reading_sessions, reading_progress, bookmarks, highlights, notes,
conversations, messages, ai_memories, voice_profiles, voice_presets, providers,
provider_connections, api_key_metadata, goals, achievements, challenges, clubs, club_members,
notifications, sync_events, analytics_events`. Object storage for large files/audio. A vector
database/vector-capable store for embeddings. Redis or equivalent for caching.

## 71. FILE STORAGE

Object storage handles uploaded books, covers, generated audio, voice previews, user media. Use
signed URLs. Never expose raw storage credentials to the Android app.

## 72. API DESIGN

Versioned APIs: `/api/v1/auth, /users, /books, /reader, /ai, /voices, /research, /social,
/rewards, /sync`. Every endpoint: authentication, authorization, validation, structured errors,
logging, rate limiting where required.

## 73. TESTING

Unit, repository, ViewModel, database, AI router, voice router, parser, UI, integration, API,
security, performance tests. Critical acceptance tests must verify: progress is never fabricated;
books reopen at exact position; voice presets actually modify requests; provider switching works;
BYOK keys work; invalid keys fail gracefully; AI context is book-specific; synced data doesn't
duplicate; clubs don't expose private content incorrectly.

## 74. LOGGING

Structured logging. Every production error includes timestamp, user/session id, feature,
operation, provider, request type, error category, device info, app version. Never log API keys,
book contents unnecessarily, private notes, private conversations, auth tokens.

## 75. ANALYTICS

Track product behavior, not private content: `book_imported, reading_started, reading_paused,
reading_completed, voice_selected, voice_preset_created, ai_request, summary_generated,
goal_completed, challenge_completed, club_joined, search_used, feature_opened`. Users can opt out
where applicable.

## 76. FEATURE FLAGS

All major new features flagged, e.g. `voice_studio_v2, research_workspace, character_voices,
live_reading_rooms, multi_provider_router` — for controlled releases.

## 77. PROVIDER FAILURE STRATEGY

```text
Provider failure → check alternate provider → check user BYOK provider
→ offer alternative voice → explain simply
```

Never crash the reader.

## 78. AI PROVIDER CONFIGURATION

Adapters expose capabilities: `supportsStreaming, supportsLongContext, supportsVision,
supportsAudio, supportsStructuredOutput, supportsEmbeddings, supportsReasoning,
supportsToolCalling`. The AI Router uses these when selecting a provider.

## 79. FUTURE INTEGRATIONS

Leave adapters for: Google Drive, Dropbox, OneDrive, Notion, Obsidian, Readwise, Pocket, Zotero,
Mendeley, GitHub, a browser extension, desktop client, web application.

## 80. FUTURE PLATFORM

Eventually: Android, iOS, Web, Windows, macOS, Browser Extension, Developer API, Education
Platform, Research Platform. The core domain model must not depend on Android.

## 81. IMPORTANT PRODUCT RULES

1. Do not implement fake functionality. If a provider isn't configured, show a proper unavailable state.
2. Do not simulate reading activity. Progress must come exclusively from real user actions.
3. Do not use Android system TTS as the primary voice engine. Use configured cloud AI voice providers.
4. Do not show internal AI/voice engineering parameters during reading.
5. Do not hardcode a single AI provider.
6. Do not hardcode a single voice provider.
7. Never put provider secrets in source code.
8. Every network operation must have loading, success, failure, and retry states.
9. No screen should freeze while AI/network work is occurring.
10. The visual interface must remain elegant even when advanced functionality is enabled.

## 82. DEFINITION OF "FULLY FUNCTIONAL"

Complete only when the user can actually: create an account; import a real PDF; open it; navigate
chapters/pages; search it; highlight text; bookmark pages; add notes; play AI narration; select
different voices; change emotional style/energy/speed; turn whispering on/off; save a voice
preset; resume narration and reading from the exact previous position; ask Damsel about selected
text and about the entire book; generate a summary and an audio summary; create flashcards;
generate a quiz; track genuine reading activity; earn XP from genuine activity; complete real
reading goals; maintain real streaks; view statistics; add books/collections; create/join a
reading club; participate in challenges; connect an AI provider; enter a personal API key;
enable/disable providers independently; test a provider connection; select provider routing; sync
account data across devices; recover account data; manage privacy and memory; use accessibility
features; receive useful notifications; recover gracefully from provider/network errors.

## 83. QUALITY BAR

Should feel like a finished commercial product, not a developer demo with many features:
beautiful, responsive, stable, polished, predictable, fast, accessible, intelligent, extensible.
Every feature must have: Loading, Empty, Success, Failure, Retry, Permission, Unavailable states.

## 84. DESIGN PHILOSOPHY FOR DAMSEL

Damsel must not scream that it is an AI application — AI should feel invisible. "AI generated
summary" → **"Damsel's Summary"**. "TTS configuration" → **"Voice Studio"**. "LLM chat" → **"Ask
Damsel"**. "Model routing" → **"Intelligence"**. The technology disappears behind the experience.

## 85. THE CORE LOOP

```text
Find Book → Open Book → Read/Listen → Ask Damsel → Understand → Highlight/Note
→ Continue → Reach Goal → Earn Reward → Return Tomorrow
```

## 86. THE EMOTIONAL LOOP

```text
Curiosity → Reading → Understanding → Immersion → Progress → Reward
→ Connection with Damsel → Return
```

More important than adding endless features.

## 87. DEVELOPMENT ORDER

- **Stage 1 — Foundation:** project cleanup, modular architecture, DI, Room, networking,
  authentication, design system, navigation, settings.
- **Stage 2 — Real Reader:** file import, parser architecture, PDF, EPUB, document indexing,
  bookmarks, highlights, notes, progress.
- **Stage 3 — Cloud Voice:** voice provider interface, provider adapters, voice list, previews,
  streaming, playback, voice selection, voice presets, emotion settings.
- **Stage 4 — Damsel Intelligence:** AI gateway, provider router, BYOK, Ask Damsel, summaries,
  book Q&A, memory, personalized audio summaries.
- **Stage 5 — Learning:** flashcards, quizzes, vocabulary, research workspace, multi-document
  analysis.
- **Stage 6 — Engagement:** goals, streaks, XP, achievements, statistics, challenges.
- **Stage 7 — Social:** clubs, shared shelves, discussions, collaborative reading.
- **Stage 8 — Cloud Scale:** sync, storage, notifications, analytics, feature flags, monitoring.

## 88. FINAL IMPLEMENTATION RULE

Build a working vertical slice first, with real services, before expanding:

```text
Login → Import PDF → Open PDF → Select text → Ask Damsel → Select AI voice
→ Choose emotional style → Generate/play speech → Save progress → Resume later
```

Only after this slice works end-to-end should the remaining feature modules be built around the
same interfaces.

## 89. SUCCESS CRITERIA

A first-time user should understand the product without a tutorial: "This is where I read." "This
is where I listen." "This is where I talk to Damsel." "This is where I research." "This is where I
see my progress." Everything else unfolds naturally.

## 90. PRODUCT NORTH STAR

Kindle's library + Audible's listening + Speechify's accessibility + ElevenReader's voices +
NotebookLM's document intelligence + an intelligent personal reading companion — unified into one
elegant Android application.

> "Open a book. Damsel understands it. Damsel can read it beautifully. You can shape exactly how
> she reads it. And whenever you get stuck, she is already there."

**END OF MASTER SPECIFICATION**
