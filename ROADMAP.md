

# StoryVerse Development Plan

## Architecture Decisions

- **Kotlin Multiplatform shares only core/domain/data/business logic.**
- **Android UI is native Jetpack Compose.**
- **iOS UI is native SwiftUI.**
- **UI is NOT shared, and Compose Multiplatform must NOT be used for shared UI.**
- **Shared stack includes:**
  - Kotlin Coroutines & Flow/StateFlow
  - Koin for dependency injection
  - Ktor Client for networking
  - kotlinx.serialization for serialization
  - Room KMP for persistence
  - DataStore where appropriate
  - Kermit for logging
- **Platform-specific capabilities** (e.g., AI runtimes, audio/background playback, payments, OS integration) live behind shared interfaces when useful and are implemented natively per platform.

---

## Progress Summary

- [ ] Phase 1 — Project Foundation & KMP Module Architecture
- [ ] Phase 2 — Shared Domain Models & Contracts
- [ ] Phase 3 — Networking Foundation
- [ ] Phase 4 — StorySource Architecture
- [ ] Phase 5 — First Real Story Source
- [ ] Phase 6 — Room KMP Persistence
- [ ] Phase 7 — Android Native App Shell
- [ ] Phase 8 — iOS Native App Shell
- [ ] Phase 9 — Story Discovery, Search & Detail
- [ ] Phase 10 — Reader Core
- [ ] Phase 11 — Reader Preferences, Progress, Bookmark & History
- [ ] Phase 12 — Offline Download Manager
- [ ] Phase 13 — ContentProcessor & Normalization
- [ ] Phase 14 — Local AI Feasibility Spike
- [ ] Phase 15 — LocalAIEngine & Model Manager
- [ ] Phase 16 — Reader AI MVP
- [ ] Phase 17 — Story Memory & Context Builder
- [ ] Phase 18 — AI Story Project Foundation
- [ ] Phase 19 — AI Chapter Generation & Streaming
- [ ] Phase 20 — EPUB & Local Story Import
- [ ] Phase 21 — TTS Architecture & Local Voice Integration
- [ ] Phase 22 — Audio Player & Background Playback
- [ ] Phase 23 — Payments & Entitlements
- [ ] Phase 24 — Testing, Performance, Reliability & Release

---

### Phase 1 — Project Foundation & KMP Module Architecture
**Goal:** Establish a clean, maintainable cross-platform project structure without UI sharing.

**Prerequisites:** Kotlin Multiplatform project template.

**Tasks**
- [ ] Audit the generated KMP project and remove/avoid shared Compose UI assumptions.
- [ ] Establish shared/domain, shared/data, shared/core or equivalent maintainable module/package structure.
- [ ] Ensure Android app depends on shared KMP and owns Compose UI/navigation/ViewModels as appropriate.
- [ ] Integrate shared KMP into iOS app/framework and expose to SwiftUI.
- [ ] Configure Koin, coroutines, serialization, logging, build variants/config.
- [ ] Add baseline unit tests and platform build verification.

**Definition of Done**
- [ ] No Compose Multiplatform or shared UI code remains.
- [ ] Android and iOS apps compile and run with shared KMP logic accessible.
- [ ] Baseline tests pass on both platforms.

---

### Phase 2 — Shared Domain Models & Contracts
**Goal:** Define all core data models, IDs, and repository contracts.

**Prerequisites:** Phase 1 complete.

**Tasks**
- [ ] Define StoryId as sourceId + remote/local id (collision-safe).
- [ ] Implement Story, StoryDetail, Author, Category, Chapter, ChapterContent, StoryStatus, StorySourceInfo models.
- [ ] Implement ReadingProgress, Bookmark, HistoryEntry, DownloadState models.
- [ ] Distinguish REMOTE, LOCAL/EPUB, GENERATED story origins without coupling reader logic to origin.
- [ ] Define repository contracts and error/result model.

**Definition of Done**
- [ ] All domain models and contracts are implemented and tested.
- [ ] No platform-specific types leak into shared contracts.

---

### Phase 3 — Networking Foundation
**Goal:** Establish robust, testable networking layer.

**Prerequisites:** Phase 2 complete.

**Tasks**
- [ ] Configure Ktor Client common settings (timeouts, headers, logging, error mapping).
- [ ] Set up Android engine and iOS Darwin engine.
- [ ] Integrate kotlinx.serialization.
- [ ] Implement safe retry logic only where appropriate.
- [ ] Map network DTOs to domain models.
- [ ] Write tests with Ktor mock engine.

**Definition of Done**
- [ ] Networking layer is testable and supports both platforms.
- [ ] Domain/data separation is maintained.

---

### Phase 4 — StorySource Architecture
**Goal:** Abstract and implement extensible story sources.

**Prerequisites:** Phase 3 complete.

**Tasks**
- [ ] Define StorySource contract for home/discovery, search/pagination, story detail, chapter list, chapter content.
- [ ] Implement StorySourceRegistry/resolver by sourceId.
- [ ] Add source capability metadata.
- [ ] Prevent source implementations from leaking source-specific DTOs into domain/UI.
- [ ] Prepare for RemoteStorySource, EpubStorySource, LocalStorySource, GeneratedStorySource.

**Definition of Done**
- [ ] At least one stub source implemented and tested.
- [ ] Registry and contract documented and validated.

---

### Phase 5 — First Real Story Source
**Goal:** Integrate a real, usable story source end-to-end.

**Prerequisites:** Phase 4 complete.

**Tasks**
- [ ] Integrate one actual usable story source (e.g., web or open API).
- [ ] Implement home lists/rankings/categories where available.
- [ ] Implement search with pagination.
- [ ] Fetch and map story metadata/detail.
- [ ] Fetch chapter list and chapter content.
- [ ] Handle malformed content, mapping, and rate/network failures.
- [ ] Write tests and fixtures for the implemented source.

**Definition of Done**
- [ ] End-to-end flow from story source to domain models is working and tested.
- [ ] Error handling and edge cases are covered.

---

### Phase 6 — Room KMP Persistence
**Goal:** Provide robust local persistence on both platforms.

**Prerequisites:** Phase 5 complete.

**Tasks**
- [ ] Configure Room for Android and iOS.
- [ ] Define entities/DAOs for stories, chapter metadata, chapter content, reading progress, bookmarks, history, downloads, source metadata.
- [ ] Ensure queries avoid loading all chapter bodies when querying library/story metadata.
- [ ] Implement repository cache policy and migrations.
- [ ] Write database tests.

**Definition of Done**
- [ ] Data persists and loads correctly on both platforms.
- [ ] Migration and cache policy are tested.

---

### Phase 7 — Android Native App Shell
**Goal:** Build the native Android app shell using Jetpack Compose.

**Prerequisites:** Phase 6 complete.

**Tasks**
- [ ] Implement navigation graph: Home, Search, Library, Story Detail, Reader, Settings.
- [ ] Build Android design system/theme.
- [ ] Connect ViewModels to StateFlow and lifecycle.
- [ ] Inject dependencies with Koin and integrate shared repositories.
- [ ] Handle loading/error/empty states in UI.

**Definition of Done**
- [ ] All main screens are navigable and display stub data.
- [ ] State management and dependency injection are functional.

---

### Phase 8 — iOS Native App Shell
**Goal:** Build the native iOS app shell using SwiftUI.

**Prerequisites:** Phase 6 complete.

**Tasks**
- [ ] Implement NavigationStack/tab structure for Home, Search, Library, Detail, Reader, Settings.
- [ ] Bridge shared Flow/state to Swift-friendly observable state.
- [ ] Access shared KMP dependencies.
- [ ] Handle loading/error/empty states in UI.

**Definition of Done**
- [ ] All main screens are navigable and display stub data.
- [ ] Shared logic is observable and usable in SwiftUI.

---

### Phase 9 — Story Discovery, Search & Detail
**Goal:** Implement discovery, search, and detail flows in both native UIs.

**Prerequisites:** Phase 7 & 8 complete.

**Tasks**
- [ ] Display home sections sourced through StorySource.
- [ ] Implement search with debounce and pagination.
- [ ] Show story detail metadata, description, categories, chapter count/status.
- [ ] Show chapter list with efficient handling for large lists.
- [ ] Add/open/read actions and show source-aware errors.
- [ ] Implement all above on both Android and iOS UIs.

**Definition of Done**
- [ ] Discovery, search, and detail flows work and are consistent across platforms.
- [ ] Reader can be launched from detail view.

---

### Phase 10 — Reader Core
**Goal:** Implement the core reading experience.

**Prerequisites:** Phase 9 complete.

**Tasks**
- [ ] Implement ReaderRepository/open-story flow (origin-agnostic).
- [ ] Fetch/cache current chapter and prefetch adjacent chapters.
- [ ] Support next/previous chapter navigation.
- [ ] Implement scroll-based reading (page-mode optional/future).
- [ ] Share reader state model only where sensible; keep UI native.
- [ ] Handle huge chapters and chapter load failures robustly.

**Definition of Done**
- [ ] Reader works with REMOTE, LOCAL, and GENERATED stories.
- [ ] Reader is robust against large chapters and errors.

---

### Phase 11 — Reader Preferences, Progress, Bookmark & History
**Goal:** Add user preferences, progress tracking, bookmarks, and history.

**Prerequisites:** Phase 10 complete.

**Tasks**
- [ ] Implement font size/family, line spacing, paragraph spacing, margins, theme/background.
- [ ] Persist preferences.
- [ ] Persist chapter + position progress with throttling/debounce.
- [ ] Resume reading from last position.
- [ ] Bookmark story/chapter/position.
- [ ] Implement recent/history and cleanup policy.

**Definition of Done**
- [ ] Preferences persist and apply correctly.
- [ ] Progress, bookmarks, and history are robust and tested.

---

### Phase 12 — Offline Download Manager
**Goal:** Enable offline reading with download management.

**Prerequisites:** Phase 11 complete.

**Tasks**
- [ ] Implement shared download queue/domain state.
- [ ] Support downloading story/chapter ranges.
- [ ] Enforce bounded concurrency (e.g., 4 concurrent downloads).
- [ ] Support pause/resume/cancel/retry.
- [ ] Persist progress and failure state.
- [ ] Check network/storage and cleanup/delete downloaded content.
- [ ] Reader seamlessly uses cached chapter content offline.

**Definition of Done**
- [ ] Download manager is robust and tested.
- [ ] Offline reading works seamlessly.

---

### Phase 13 — ContentProcessor & Normalization
**Goal:** Normalize chapter content for reading, AI, and TTS.

**Prerequisites:** Phase 12 complete.

**Tasks**
- [ ] Implement RawChapterContent → ProcessedContent pipeline.
- [ ] Parse HTML/extract text as needed.
- [ ] Remove navigation junk/source boilerplate/ads.
- [ ] Normalize whitespace, paragraphs, punctuation/Unicode (preserve Vietnamese).
- [ ] Preserve paragraph boundaries.
- [ ] Write fixture tests with real/malformed inputs.
- [ ] Cache processed form/version if needed.

**Definition of Done**
- [ ] ProcessedContent is robust and used by Reader, AI, and TTS.
- [ ] Tests cover diverse real-world chapter content.

---

### Phase 14 — Local AI Feasibility Spike
**Goal:** Evaluate and benchmark on-device AI runtimes.

**Prerequisites:** First real StorySource, Room persistence, and usable Reader working.

**Tasks**
- [ ] Evaluate realistic on-device runtime candidates for Android and iOS (e.g., Gemma, llama.cpp).
- [ ] Build minimal inference spike behind platform code.
- [ ] Benchmark model download size, disk, model load time, RAM peak, first-token latency, tokens/sec, long-context, Vietnamese quality, battery/thermal.
- [ ] Test on low/mid/high representative devices.
- [ ] Record go/no-go and selected runtime/model strategy.

**Definition of Done**
- [ ] Feasibility, benchmarks, and runtime strategy are documented.
- [ ] No production AI code until this phase is complete.

---

### Phase 15 — LocalAIEngine & Model Manager
**Goal:** Abstract and implement local AI engine and model management.

**Prerequisites:** Phase 14 complete.

**Tasks**
- [ ] Define LocalAIEngine contract: load/unload, generate streaming/cancel, capabilities.
- [ ] Implement platform adapters for chosen runtime.
- [ ] Model metadata: quantization, context, capability info.
- [ ] Model download: progress, resume, checksum, versioning, storage-space validation.
- [ ] Active model selection, delete model, unload under memory pressure.
- [ ] Never couple ViewModels directly to Gemma/llama-specific APIs.

**Definition of Done**
- [ ] Model management is robust and replaceable.
- [ ] Shared interface is clean and ViewModel-agnostic.

---

### Phase 16 — Reader AI MVP
**Goal:** Add basic AI-powered reading features.

**Prerequisites:** Phase 15 complete.

**Tasks**
- [ ] Implement chapter summarization.
- [ ] Implement paragraph explanation.
- [ ] Implement question-answering about chapter/context.
- [ ] (Optional) Implement rewrite/translate after core tasks.
- [ ] Stream generation to native UI.
- [ ] Handle cancellation and model-not-installed UX.
- [ ] Measure prompt size, latency, and quality.

**Definition of Done**
- [ ] AI features work on both platforms and stream to UI.
- [ ] User experience is robust for missing/cancelled models.

---

### Phase 17 — Story Memory & Context Builder
**Goal:** Persist and build context for AI generation.

**Prerequisites:** Phase 16 complete.

**Tasks**
- [ ] Implement chapter summaries, character records, world facts/lore, timeline/events.
- [ ] Build ContextBuilder to select current chapter, recent summaries, relevant memory within token budget.
- [ ] Persist and version generated memory.
- [ ] Regenerate/invalidate when source chapter changes.
- [ ] Avoid stuffing entire novels into context.

**Definition of Done**
- [ ] ContextBuilder is robust and efficient.
- [ ] Memory is persisted and updated as needed.

---

### Phase 18 — AI Story Project Foundation
**Goal:** Enable user-driven AI story creation projects.

**Prerequisites:** Phase 17 complete.

**Tasks**
- [ ] Implement AIStoryProject entity and repository.
- [ ] Support user premise, genres/tags, tone/style, target length/language.
- [ ] Define world, character profiles, relationships, arcs, outline.
- [ ] Implement ChapterOutline model.
- [ ] Allow editing of generated artifacts before chapter generation.

**Definition of Done**
- [ ] Users can create and edit AI story projects and outlines.
- [ ] All project data is persisted.

---

### Phase 19 — AI Chapter Generation & Streaming
**Goal:** Generate and stream AI-written chapters.

**Prerequisites:** Phase 18 complete.

**Tasks**
- [ ] Build prompts from project/world/characters/story memory/outline/recent chapters.
- [ ] Stream output to UI.
- [ ] Support cancel/regenerate/continue.
- [ ] Save drafts incrementally and recover interrupted generation.
- [ ] Generated chapters become normal Story/Chapter domain data with GENERATED origin.
- [ ] Update story memory after accepted generation.
- [ ] Guard context/token budgets.

**Definition of Done**
- [ ] Chapter generation is robust, recoverable, and integrated with Reader.
- [ ] Context/token limits are enforced.

---

### Phase 20 — EPUB & Local Story Import
**Goal:** Support importing and reading local EPUBs and files.

**Prerequisites:** Phase 19 complete.

**Tasks**
- [ ] Support importing EPUB/local files.
- [ ] Parse metadata, cover, TOC/spine, chapters.
- [ ] Normalize through ContentProcessor.
- [ ] Store using LOCAL/EPUB StorySource implementation.
- [ ] Handle duplicate/import versioning.
- [ ] Reader uses same domain/repository path as remote/generated stories.

**Definition of Done**
- [ ] EPUB/local stories can be imported and read like remote/generated stories.
- [ ] Metadata and content are normalized.

---

### Phase 21 — TTS Architecture & Local Voice Integration
**Goal:** Add robust TTS with local voice models.

**Prerequisites:** Stable ContentProcessor (Phase 13+) complete.

**Tasks**
- [ ] Define shared TtsEngine contract: model/profile lifecycle, synthesize/stream/cancel, capabilities.
- [ ] Research/integrate Onimi/VoiceStudio-derived local TTS if feasible.
- [ ] Implement VoiceProfile model and local model manager.
- [ ] Chunk text by paragraphs/sentences, handle punctuation, validate Vietnamese pronunciation.
- [ ] Benchmark synthesis speed, RAM, model size, thermal/battery.

**Definition of Done**
- [ ] Local TTS works for ProcessedContent and benchmarks are documented.
- [ ] Voice model management is robust.

---

### Phase 22 — Audio Player & Background Playback
**Goal:** Enable audio playback and background controls.

**Prerequisites:** Phase 21 complete.

**Tasks**
- [ ] Use platform-native audio stack: Android Media3, iOS AVFoundation/AVAudioSession.
- [ ] Queue TTS/generated audio segments in correct order.
- [ ] Implement play/pause/seek/next/previous chapter.
- [ ] Support background playback, lock-screen/remote controls, audio focus/interruption/headset handling.
- [ ] Cache audio by story/chapter/voice/model/settings/content version.
- [ ] Resume playback and implement cleanup policy.

**Definition of Done**
- [ ] Audio player works with TTS-generated content.
- [ ] Background playback and controls are robust.

---

### Phase 23 — Payments & Entitlements
**Goal:** Add premium features with platform-native payments.

**Prerequisites:** Phase 22 complete.

**Tasks**
- [ ] Define which features are premium (do not lock basic offline reader).
- [ ] Integrate Google Play Billing and StoreKit.
- [ ] Implement shared entitlement abstraction/state, platform purchase implementations.
- [ ] Support restore purchases.
- [ ] Define receipt/server validation if backend is needed.
- [ ] Gate premium features (model packs, advanced generation, voices, limits) only after validation.

**Definition of Done**
- [ ] Payments and entitlement flow are robust and tested.
- [ ] Premium gates are clear and non-intrusive.

---

### Phase 24 — Testing, Performance, Reliability & Release
**Goal:** Ensure quality, reliability, and prepare for release.

**Prerequisites:** All previous phases complete.

**Tasks**
- [ ] Write shared unit tests for repositories, parsers, ContentProcessor, context builder, download logic.
- [ ] Write StorySource fixture/integration tests.
- [ ] Write Room migration tests.
- [ ] Implement Android UI/smoke tests and iOS UI/smoke tests.
- [ ] Profile Reader with large content.
- [ ] Profile memory/leaks (model load/unload, reader, covers, audio).
- [ ] Test offline/network recovery.
- [ ] Review crash/logging/privacy.
- [ ] Prepare store release configuration and staged beta/release.

**Definition of Done**
- [ ] All critical paths are tested.
- [ ] Release builds are stable and pass QA.

---

## Milestones

- **MVP Reader:** Phases 1-10
- **Offline Reader:** Phases 11-12, 20
- **Local AI Prototype:** Phases 13-15
- **AI Reader:** Phases 16-17
- **AI Story Creator:** Phases 18-19
- **Voice & Audio:** Phases 21-22
- **Monetization / Production:** Phases 23-24

---

## Critical Ordering Rules

1. **First real story source + persistence + Reader before local AI.**
2. **ContentProcessor must be complete before production Reader AI and before TTS.**
3. **Benchmark local AI before committing architecture to a specific model/runtime.**
4. **AI-generated stories use the same Story/Chapter/Reader pipeline as remote/local stories.**
5. **TTS consumes ProcessedContent, not raw scraped/source content.**
6. **UI remains platform-native throughout the project.**

---

## Architecture Decision Log

| Decision                                   | Status         | Reason                                                                                   |
|---------------------------------------------|----------------|------------------------------------------------------------------------------------------|
| Native UI split (Compose for Android, SwiftUI for iOS) | Accepted      | Native look/feel, maintainability, avoid Compose Multiplatform instability                |
| StorySource abstraction                     | Accepted       | Enables extensibility for remote, local, and AI sources                                  |
| Room KMP for persistence                    | Accepted       | Shared database logic, cross-platform support                                            |
| Ktor for networking                        | Accepted       | Multiplatform, coroutine-friendly, testable                                              |
| ViewModel/StateFlow for state management    | Accepted       | Shared business logic, platform-native UI state handling                                 |
| Local AI runtime (Gemma/llama.cpp etc.)    | Pending        | To be decided after benchmarking in feasibility spike                                    |
| TTS runtime (Onimi/VoiceStudio, etc.)      | Pending        | To be decided after feasibility and platform packaging validation                        |
