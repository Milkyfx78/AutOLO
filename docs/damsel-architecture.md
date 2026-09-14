# Damsel AI Reader — Architecture Specification v1

**Status:** Draft for build kickoff
**Scope:** Product decisions, system architecture, data model, AI orchestration, voice pipeline (cloud + offline), security, scalability, roadmap.

---

## 1. Product Definition

**Damsel is a Universal AI Reading Platform.**

It reads and narrates anything: PDF, EPUB, MOBI, DOCX, websites, articles, research papers, textbooks, code documentation, RSS feeds, and cloud documents. The reading engine is content-format-agnostic; format support is added via pluggable parsers, not architectural rewrites.

**Primary audience for V1:** Professionals (fast comprehension, summaries, multitasking via audio). Students are the natural V2 expansion — the core engine serves both with only prompt/UX tuning.

**Long-term ambition:** Replace Kindle + Audible + Speechify + ElevenReader as one product — a single app for acquiring, reading, listening to, and understanding written content.

---

## 2. Core Principles

1. **AI-agnostic, not AI-owned.** Damsel is a routing and experience layer over multiple AI providers, not a wrapper around one model.
2. **BYOK by default.** Users supply their own API keys for cloud AI/voice. Damsel never resells model access; it orchestrates it.
3. **Cloud-first, offline-resilient.** No offline-first constraint. Cloud AI is the primary engine; local caching and offline TTS exist for performance, cost-avoidance, and no-signal reading — not as the baseline architecture.
4. **One adaptive companion.** A single AI personality ("Damsel") that adapts tone/depth to content and user — not multiple bot personas to build and maintain.
5. **Privacy-respecting personalization.** Long-term memory is a core differentiator, with full user visibility and deletion control.

---

## 3. High-Level System Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                         Client Apps                              │
│        (Web / iOS / Android / Desktop — shared core SDK)         │
├─────────────────────────────────────────────────────────────────┤
│  Reader Engine   │  Voice Studio UI  │  AI Chat/Companion UI     │
│  Local Cache     │  Offline TTS      │  Settings / BYOK Vault    │
│  (IndexedDB/     │  (Piper/Kokoro    │                            │
│   SQLite)        │   on-device)      │                            │
└─────────┬─────────────────┬───────────────────┬──────────────────┘
          │ HTTPS/gRPC       │                   │
┌─────────▼─────────────────▼───────────────────▼──────────────────┐
│                        API Gateway (BFF)                          │
│         Auth · Rate limiting · Request shaping · Billing hooks    │
├─────────────────────────────────────────────────────────────────┤
│  Library Service │ AI Router Service │ Voice Service │ Memory Svc │
│  (docs, sync,    │ (model selection, │ (TTS provider │ (user      │
│   ingestion)     │  cost estimation) │  abstraction) │  profile,  │
│                   │                   │                │  history)  │
├─────────────────────────────────────────────────────────────────┤
│              Content Ingestion & Parsing Pipeline                 │
│   PDF · EPUB · MOBI · DOCX · HTML/Article · RSS · Cloud docs       │
├─────────────────────────────────────────────────────────────────┤
│                     Provider Abstraction Layer                    │
│  Text AI: OpenAI · Anthropic · Gemini · OpenRouter (BYOK)          │
│  Voice:   ElevenLabs · OpenAI TTS · Google TTS (BYOK, cloud)       │
│  Offline: Piper · Kokoro (on-device inference, no key needed)      │
├─────────────────────────────────────────────────────────────────┤
│  Postgres (core data) │ Object storage (files, audio) │ Vector DB  │
│  Redis (cache/queue)  │ CDN (static + voice model files)           │
└─────────────────────────────────────────────────────────────────┘
```

**Pattern:** BFF (Backend-for-Frontend) + service-oriented backend. Not full microservices day one — services are logically separated but can deploy as one binary initially, split out as load demands (Library, AI Router, Voice, Memory are the four natural seams).

---

## 4. Feature Scope

### V1 (launch)
- Import & read: PDF, EPUB, DOCX, web articles (via URL), plain text
- Google Drive sync
- AI features: explain paragraph, summarize (page/chapter/book), ask-questions-about-book (RAG over the doc), instant translate, vocabulary builder
- Voice: cloud (OpenAI, ElevenLabs) + offline (Piper, Kokoro), basic preset sliders (speed, pitch, emotion-lite)
- BYOK for: OpenAI, Anthropic, Google Gemini, ElevenLabs
- AI Router v1: static routing table, user-overridable, token/cost estimate shown before each call
- Memory: reading history, favorite voice/provider/genre, stored per user, viewable/deletable

### V2
- MOBI, RSS, code docs, Notion/Obsidian/GitHub/Pocket/Instapaper integrations
- Quizzes, flashcards, mind maps, timelines, character tracking, citation generator
- Full Voice Studio (emotion matrix + fine control sliders), shareable voice presets
- OpenRouter, Azure OpenAI, xAI/Grok, DeepSeek as additional BYOK providers
- Smarter AI Router (usage-pattern-based, not just static rules)

### V3+
- Formula/code explanation modes (STEM, programming books)
- Multi-device real-time sync of reading position + annotations
- Community preset marketplace

---

## 5. Offline Voice: Piper & Kokoro

Cloud voice providers require internet + a valid API key. To keep Damsel usable with no key and no signal, V1 adds a **third voice tier**: on-device TTS via **Piper** and **Kokoro**.

**In plain terms:** these are voice models small enough to download once (like a song) and run directly on the user's device afterward — no internet, no API key, no per-use cost.

### Design implications

- **Voice Picker UI** shows three tiers side by side: Cloud (🔑 needs key), Offline (📥 download once), and any future BYOK options — same interface, different badge.
- **Model download & storage manager**: voice models (typically 20–150MB each) are fetched from a CDN-hosted model registry on first use, cached locally (IndexedDB/filesystem depending on platform), with a settings screen to view/delete downloaded voices and see space used.
- **On-device inference engine**: Piper runs via a small native/WASM runtime bundled per platform (ONNX-based); Kokoro similarly via ONNX runtime. Both are CPU-friendly and don't require a GPU.
- **Same Voice Service interface**: the app-level Voice Service abstracts "speak(text, voiceConfig)" — it doesn't matter to calling code whether the backend is ElevenLabs over HTTPS or Piper running locally. This keeps the Voice Studio UI, presets, and playback pipeline provider-agnostic across all three tiers.
- **Fallback logic**: if a cloud call fails (no key, no network, rate limit), the app can offer to fall back to an offline voice automatically — good for resilience, no offline-first redesign required elsewhere.

This only affects the Voice Service and client storage layer — it does not change the cloud-first architecture of the AI/text side.

---

## 6. AI Router

A routing layer sits between the app and provider APIs. Goals: pick a good default model per task, let users override, and show cost before committing.

```
Task type        → Default provider (editable per user)
─────────────────────────────────────────────────────────
Summarization     → Gemini (cheap, fast, long context)
Deep reasoning/Q&A → GPT / Claude
Coding docs        → Claude
Creative rewrite   → Grok (if key present) / GPT
Translation        → Google
Voice              → user's chosen voice tier/provider
```

V1 implementation: a simple, editable JSON routing table per task type, stored server-side per user, with a UI to override any row. Before each paid call, show estimated tokens and cost using each provider's published pricing. No ML-based routing in V1 — that's a V2+ investment once there's usage data to justify it.

---

## 7. Data Model (core entities)

- **User** — profile, subscription tier, preferences
- **APIKey** (BYOK) — provider, encrypted key, enabled/disabled, per-user
- **Document** — source type, storage ref, parsed structure, sync source (if cloud-linked)
- **ReadingSession** — position, duration, device
- **Highlight/Annotation**
- **VoicePreset** — name, provider, voice id, parameters, owner, shared flag
- **ConversationMemory** — per-document chat history + long-term summarized user memory
- **UsageLedger** — per-call token/cost tracking per provider, for the cost estimator and billing transparency

API keys are encrypted at rest (per-user envelope encryption), never logged, never sent to any provider other than their own.

---

## 8. Security & Privacy

- BYOK keys encrypted at rest, decrypted only in-memory per request, never persisted in logs or caches.
- Per-provider request isolation — a user's OpenAI key is never used for an Anthropic call, enforced at the Provider Abstraction Layer.
- Memory data (reading history, preferences) exportable and deletable by the user on demand (GDPR-style compliance from day one, given the long-term personalization strategy).
- Offline voice models run fully on-device — no text sent anywhere for that tier, which is itself a privacy selling point.

---

## 9. Scalability Notes

- Stateless API layer behind the gateway — horizontal scaling is straightforward.
- Object storage + CDN for documents and generated audio (cache TTS output per text+voice+params hash to avoid re-generating identical audio).
- Vector DB for per-document RAG (ask-questions-about-book) — scoped per document, not a single giant index, to keep retrieval fast and cheap.
- Voice model files for Piper/Kokoro served from CDN, versioned, so app updates don't require re-downloading unchanged voices.

---

## 10. Roadmap Summary

| Phase | Focus |
|---|---|
| V1 | Universal reader core, professional-focused AI features, BYOK (4 providers), cloud + offline voice, basic AI Router, Google Drive sync |
| V2 | More integrations, full Voice Studio, more BYOK providers, quizzes/flashcards/mind maps, smarter routing |
| V3 | STEM/code modes, community preset sharing, multi-device real-time sync |
| Long-term | Position as the reading+learning OS across formats and providers — the "replace Kindle/Audible/Speechify" endpoint |

---

*This document reflects the 12 product decisions locked on 2026-09-14, plus the addition of Piper/Kokoro offline voice support.*
