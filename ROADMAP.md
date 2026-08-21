

# StoryVerse Development Plan

## Architecture Decisions

- **Architectural Pattern: Clean Architecture + MVVM**
  - **Shared KMP Core (`sharedLogic`) — Clean Architecture**:
    - **Domain Layer**: Contains enterprise business rules, Domain Models, Repository Contracts, and Use Cases / Interactors. Pure Kotlin with zero framework dependencies.
    - **Data Layer**: Contains Repository Implementations, Data Sources (Remote Ktor, Local Room DB, Memory/Cache), DTOs, and Mappers.
    - **Core Layer**: Contains cross-cutting concerns (`AppResult`/`AppError`, `DispatcherProvider`, `AppLogger`, base utilities).
    - **DI Layer**: Koin modules configuring and injecting all layers.
  - **Platform Presentation — MVVM**:
    - **Android UI**: Native Jetpack Compose + AndroidX ViewModels consuming Use Cases/Repositories via `StateFlow`/`SharedFlow`.
    - **iOS UI**: Native SwiftUI + Observable ViewModels / Swift Concurrency bridging Shared KMP `StateFlow`/Use Cases.
- **Kotlin Multiplatform shares only core/domain/data/business logic.**
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

- [x] Phase 1 — Project Foundation & KMP Module Architecture
- [x] Phase 2 — Shared Domain Models & Contracts
- [x] Phase 3 — Networking Foundation
- [x] Phase 4 — StorySource Architecture
- [x] Phase 5 — Multi-Language & Unified Localization (i18n)
- [x] Phase 6 — Design System & Multi-Theme Architecture
- [x] Phase 7 — First Real Story Source
- [x] Phase 8 — Room KMP Persistence
- [x] Phase 9 — Android Native App Shell
- [x] Phase 10 — iOS Native App Shell
- [x] Phase 11 — Story Discovery, Search & Detail
- [x] Phase 12 — Reader Core
- [ ] Phase 13 — Reader Preferences, Progress, Bookmark & History
- [ ] Phase 14 — Offline Download Manager
- [ ] Phase 15 — ContentProcessor & Normalization
- [ ] Phase 16 — Local AI Feasibility Spike
- [ ] Phase 17 — LocalAIEngine & Model Manager
- [ ] Phase 18 — Reader AI MVP
- [ ] Phase 19 — Story Memory & Context Builder
- [ ] Phase 20 — AI Story Project Foundation
- [ ] Phase 21 — AI Chapter Generation & Streaming
- [ ] Phase 22 — EPUB & Local Story Import
- [ ] Phase 23 — TTS Architecture & Local Voice Integration
- [ ] Phase 24 — Audio Player & Background Playback
- [ ] Phase 25 — Payments & Entitlements
- [ ] Phase 26 — Testing, Performance, Reliability & Release

---

### Phase 1 — Project Foundation & KMP Module Architecture
**Goal:** Establish a clean, maintainable cross-platform project structure without UI sharing.

**Prerequisites:** Kotlin Multiplatform project template.

**Tasks**
- [x] Audit the generated KMP project and remove/avoid shared Compose UI assumptions.
- [x] Establish shared/domain, shared/data, shared/core or equivalent maintainable module/package structure.
- [x] Ensure Android app depends on shared KMP and owns Compose UI/navigation/ViewModels as appropriate.
- [x] Integrate shared KMP into iOS app/framework and expose to SwiftUI.
- [x] Configure Koin, coroutines, serialization, logging, build variants/config.
- [x] Add baseline unit tests and platform build verification.

**Definition of Done**
- [x] No Compose Multiplatform or shared UI code remains.
- [x] Android and iOS apps compile and run with shared KMP logic accessible.
- [x] Baseline tests pass on both platforms.

---

### Phase 2 — Shared Domain Models & Contracts
**Goal:** Define all core data models, IDs, and repository contracts.

**Prerequisites:** Phase 1 complete.

**Tasks**
- [x] Define StoryId as sourceId + remote/local id (collision-safe).
- [x] Implement Story, StoryDetail, Author, Category, Chapter, ChapterContent, StoryStatus, StorySourceInfo models.
- [x] Implement ReadingProgress, Bookmark, HistoryEntry, DownloadState models.
- [x] Distinguish REMOTE, LOCAL/EPUB, GENERATED story origins without coupling reader logic to origin.
- [x] Define repository contracts and error/result model.

**Definition of Done**
- [x] All domain models and contracts are implemented and tested.
- [x] No platform-specific types leak into shared contracts.

---

### Phase 3 — Networking Foundation
**Goal:** Establish robust, testable networking layer.

**Prerequisites:** Phase 2 complete.

**Tasks**
- [x] Configure Ktor Client common settings (timeouts, headers, logging, error mapping).
- [x] Set up Android engine and iOS Darwin engine.
- [x] Integrate kotlinx.serialization.
- [x] Implement safe retry logic only where appropriate.
- [x] Map network DTOs to domain models.
- [x] Write tests with Ktor mock engine.

**Definition of Done**
- [x] Networking layer is testable and supports both platforms.
- [x] Domain/data separation is maintained.

---

### Phase 4 — StorySource Architecture
**Goal:** Abstract and implement extensible story sources.

**Prerequisites:** Phase 3 complete.

**Tasks**
- [x] Define StorySource contract for home/discovery, search/pagination, story detail, chapter list, chapter content.
- [x] Implement StorySourceRegistry/resolver by sourceId.
- [x] Add source capability metadata.
- [x] Prevent source implementations from leaking source-specific DTOs into domain/UI.
- [x] Prepare for RemoteStorySource, EpubStorySource, LocalStorySource, GeneratedStorySource.

**Definition of Done**
- [x] At least one stub source implemented and tested.
- [x] Registry and contract documented and validated.

---

### Phase 5 — Multi-Language & Unified Localization (i18n)
**Goal:** Establish a centralized single source of truth for localized text strings and language switching, making it easy to add any new languages in the future.

**Prerequisites:** Phase 4 complete.

**Tasks**
- [x] Define shared string key catalog (`AppStringKeys` / `LocalizedStrings`) and translation architecture in `sharedLogic`.
- [x] Provide baseline translations for Vietnamese (`vi`) and English (`en`).
- [x] Implement `LocalizationRepository` / `LanguageManager` to query strings, select language (`AppLanguage: VI, EN`), and observe language changes via `StateFlow`.
- [x] Provide native consumption bridges:
  - Android Jetpack Compose helper (`rememberAppString(key)` / LocalProvider).
  - iOS SwiftUI helper (`AppStrings[key]` / Observable Object).
- [x] Support dynamic runtime language switching without restarting the entire app.
- [x] Write unit tests verifying 100% translation key parity between Vietnamese and English (no missing keys).

**Definition of Done**
- [x] Text strings are centralized and shared; adding a new language only requires adding a translation dictionary.
- [x] Language switching is reactive and tested on both platforms.

---

### Phase 6 — Design System & Multi-Theme Architecture
**Goal:** Build a flexible, multi-theme color palette system tailored for story reading, allowing seamless switching and easy addition of custom themes.

**Prerequisites:** Phase 5 complete.

**Tasks**
- [x] Define core semantic color tokens (`ThemeColors`: Primary, OnPrimary, Secondary, Background, Surface, Card, Border, TextPrimary, TextSecondary, ReaderBackground, ReaderTextColor, Accent, Success, Warning, Error).
- [x] Design and implement reading themes tailored for StoryVerse:
  - **Light**: Default clean, high-contrast reading theme.
  - **Dark**: Night mode with soft contrast to prevent eye strain.
  - **Sepia / Warm**: Warm paper tone (amber/beige) optimized for long-form reading.
  - **Parchment / Vintage**: Vintage classic book paper tone.
  - **Midnight / OLED**: Pure black (#000000) for OLED battery efficiency.
  - **Forest / Emerald**: Calming dark green tone for soothing reading.
- [x] Implement `ThemeRepository` / `ThemeManager` in `sharedLogic` to persist and observe current theme (`AppThemePreset`, `ThemeMode: LIGHT, DARK, SYSTEM`).
- [x] Bridge theme tokens to:
  - Android Jetpack Compose `StoryVerseTheme` (Color, Typography, Shape, Dimensions).
  - iOS SwiftUI `StoryVerseTheme` (Color extensions, Font styles, Spacings).
- [x] Write unit tests verifying color token completeness and contrast compliance.

**Definition of Done**
- [x] Unified theme palette is defined and ready for Android Compose & iOS SwiftUI.
- [x] Theme switching is reactive and adding a new theme requires only defining a new `ThemeColors` preset.

---

### Phase 7 — First Real Story Source
**Goal:** Integrate a real, usable story source end-to-end.

**Prerequisites:** Phase 6 complete.

**Tasks**
- [x] Integrate one actual usable story source (e.g., web or open API / Drive Catalog with 10,000+ real EPUB stories).
- [x] Implement home lists/rankings/categories where available (Featured, Tiên Hiệp & Huyền Huyễn, Đô Thị & Khoa Huyễn, Latest).
- [x] Implement search with pagination (by title, author, category/subject, and sorting).
- [x] Fetch and map story metadata/detail (filename parser, author extraction, slug mapping, EPUB downloadUrl/size metadata).
- [x] Fetch chapter list and chapter content.
- [x] Handle malformed content, mapping, and rate/network failures.
- [x] Write tests and fixtures for the implemented source.

**Definition of Done**
- [x] End-to-end flow from story source to domain models is working and tested.
- [x] Error handling and edge cases are covered.

---

### Phase 8 — Room KMP Persistence
**Goal:** Implement cross-platform local database persistence using Room KMP with SQLite driver.

**Prerequisites:** Phase 7 complete.

**Tasks**
- [x] Configure Room KMP (`androidx.room` 2.7.2 + `androidx.sqlite:sqlite-bundled` 2.6.0) with KSP multiplatform code generator.
- [x] Define entities/DAOs for stories, chapter metadata, chapter content, reading progress, bookmarks, history, downloads.
- [x] Ensure queries avoid loading all chapter bodies when querying library/story metadata (normalized `ChapterContentDao` separation).
- [x] Implement repository cache policy (`LocalStoryCache`, `RoomReaderRepository`).
- [x] Write database tests for all DAOs and repositories with 100% pass on Android & iOS simulator.

**Definition of Done**
- [x] Data persists and loads correctly on both platforms.
- [x] Migration and cache policy are tested.

---

### Phase 9 — Android Native App Shell
**Goal:** Build the native Android app shell using Jetpack Compose.

**Prerequisites:** Phase 8 complete.

**Tasks**
- [x] Implement navigation graph: Home, Search, Library, Story Detail, Reader, Settings with BottomNavigationBar.
- [x] Connect Jetpack Compose theme to multi-theme system (`StoryVerseTheme`, dynamic swatches).
- [x] Eliminate theme switching latency and header color desync:
  - Removed Material 3 `TopAppBar` tonal tint & animation cache; implemented direct, lightweight `StoryVerseTopBar` bound to `LocalStoryVerseColors`.
  - Moved `StoryVerseTopBar` directly into each screen's composable hierarchy (eliminated root Scaffold state hoisting and 1-frame `SideEffect` delay).
  - Synchronized `ThemeRepositoryImpl` state flow emission (`_currentColors` before `_currentPreset`) and window status bar background.
- [x] Connect ViewModels to StateFlow, lifecycle, and shared domain models.
- [x] Inject dependencies with Koin and integrate shared repositories and Room DAOs.
- [x] Handle loading, error (with retry), and empty states in UI.

**Definition of Done**
- [x] All main screens are navigable and display live real/catalog data.
- [x] State management and dependency injection are functional.
- [x] Theme switching updates header, body, bottom bar, and status bar synchronously in a single frame without lag or color mismatch.

---

### Phase 10 — iOS Native App Shell
**Goal:** Build the native iOS app shell using SwiftUI.

**Prerequisites:** Phase 8 complete.

**Tasks**
- [x] Implement NavigationStack/tab structure for Home, Search, Library, Detail, Reader, Settings with custom styled `MainTabView`.
- [x] Bridge shared Flow/state to Swift-friendly observable state (`ThemeManager`, `LocalizationManager`, and MVVM Observable ViewModels).
- [x] Access shared KMP dependencies (`KoinHelper` injecting repositories, local cache, story sources, and Room persistence).
- [x] Handle loading, error (with retry), and empty states in UI (`LoadingView`, `ErrorView`, `EmptyView`).

**Definition of Done**
- [x] All main screens are navigable and display live/catalog data.
- [x] Shared logic is observable and usable in SwiftUI with real-time multi-theme and language switching.
- [x] `xcodebuild` succeeds and multiplatform tests pass on iOS simulator (`:sharedLogic:iosSimulatorArm64Test`).

---

### Phase 11 — Story Discovery, Search & Detail
**Goal:** Implement discovery, search, and detail flows in both native UIs.

**Prerequisites:** Phase 9 & 10 complete.

**Tasks**
- [x] Display home sections sourced through StorySource (Hero Featured Carousel, Popular Horizontal List, Latest Vertical List).
- [x] Implement search with 300ms debounce, category filtering chips, and infinite pagination.
- [x] High-performance inverted category indexing and $O(1)$ search in Drive catalog without CPU stuttering on 10,000+ items.
- [x] Show story detail metadata, description, categories, chapter count/status.
- [x] Show chapter list with efficient handling for large lists.
- [x] Add/open/read actions and show source-aware errors.
- [x] Library tabs with smooth continuous swiping and animated indicator tracking on both platforms.
- [x] Implement all above on both Android (Jetpack Compose) and iOS (SwiftUI) with 100% feature and visual parity.

**Definition of Done**
- [x] Discovery, search, and detail flows work and are consistent across platforms.
- [x] Reader can be launched from detail view.
- [x] Unit tests pass and builds succeed on both Android and iOS.

---

### Phase 12 — Reader Core
**Goal:** Implement the core reading experience.

**Prerequisites:** Phase 11 complete.

**Tasks**
- [x] Implement ReaderUseCase & session flow (origin-agnostic across REMOTE, LOCAL, and GENERATED stories).
- [x] Fetch/cache current chapter and asynchronous background prefetch for adjacent chapters (next & previous).
- [x] Support next/previous chapter navigation with end-of-chapter jump buttons and floating bottom controls.
- [x] Table of Contents (TOC) sheet / modal with full chapter list, active chapter indicator, and instant jumping.
- [x] Implement scroll-based reading with individual paragraph rendering, word count, and estimated reading time badges.
- [x] Immersive fullscreen reading toggle with animated slide overlays for top/bottom navigation controls.
- [x] Bookmark toggle and automatic reading progress & history recording.
- [x] Handle large chapters, source fallback, and chapter load failures robustly.
- [x] Full feature and visual parity across Android (Jetpack Compose) and iOS (SwiftUI).

**Definition of Done**
- [x] Reader works with REMOTE, LOCAL, and GENERATED stories.
- [x] Reader is robust against large chapters and errors.
- [x] Unit tests pass across commonTest, Android, and iOS simulator.

---

### Phase 13 — Reader Preferences, Progress, Bookmark & History
**Goal:** Add user preferences, progress tracking, bookmarks, and history.

**Prerequisites:** Phase 12 complete.

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

### Phase 14 — Offline Download Manager
**Goal:** Enable offline reading with download management.

**Prerequisites:** Phase 13 complete.

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

### Phase 15 — ContentProcessor & Normalization
**Goal:** Normalize chapter content for reading, AI, and TTS.

**Prerequisites:** Phase 14 complete.

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

### Phase 16 — Local AI Feasibility Spike
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

### Phase 17 — LocalAIEngine & Model Manager
**Goal:** Abstract and implement local AI engine and model management.

**Prerequisites:** Phase 16 complete.

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

### Phase 18 — Reader AI MVP
**Goal:** Add basic AI-powered reading features.

**Prerequisites:** Phase 17 complete.

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

### Phase 19 — Story Memory & Context Builder
**Goal:** Persist and build context for AI generation.

**Prerequisites:** Phase 18 complete.

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

### Phase 20 — AI Story Project Foundation
**Goal:** Enable user-driven AI story creation projects.

**Prerequisites:** Phase 19 complete.

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

### Phase 21 — AI Chapter Generation & Streaming
**Goal:** Generate and stream AI-written chapters.

**Prerequisites:** Phase 20 complete.

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

### Phase 22 — EPUB & Local Story Import
**Goal:** Support importing and reading local EPUBs and files.

**Prerequisites:** Phase 21 complete.

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

### Phase 23 — TTS Architecture & Local Voice Integration
**Goal:** Add robust TTS with local voice models.

**Prerequisites:** Stable ContentProcessor (Phase 15+) complete.

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

### Phase 24 — Audio Player & Background Playback
**Goal:** Enable audio playback and background controls.

**Prerequisites:** Phase 23 complete.

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

### Phase 25 — Payments & Entitlements
**Goal:** Add premium features with platform-native payments.

**Prerequisites:** Phase 24 complete.

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

### Phase 26 — Testing, Performance, Reliability & Release
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

- **MVP Foundation & Design:** Phases 1-6 (KMP Foundation, Domain, Network, Sources, i18n, Multi-Theme)
- **MVP Reader App:** Phases 7-12 (Real Source, Room DB, Android & iOS Shells, Discovery & Reader)
- **Offline Reader & Customization:** Phases 13-15, 22
- **Local AI Prototype & Reader AI:** Phases 16-19
- **AI Story Creator:** Phases 20-21
- **Voice & Audio:** Phases 23-24
- **Monetization & Release:** Phases 25-26

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
| Clean Architecture + MVVM                   | Accepted       | Strict separation of concerns (Core/Domain/Data in KMP) with reactive native ViewModels  |
| Native UI split (Compose for Android, SwiftUI for iOS) | Accepted      | Native look/feel, maintainability, avoid Compose Multiplatform instability                |
| StorySource abstraction                     | Accepted       | Enables extensibility for remote, local, and AI sources                                  |
| Room KMP for persistence                    | Accepted       | Shared database logic, cross-platform support                                            |
| Ktor for networking                        | Accepted       | Multiplatform, coroutine-friendly, testable                                              |
| ViewModel/StateFlow for state management    | Accepted       | Shared business logic, platform-native UI state handling                                 |
| Local AI runtime (Gemma/llama.cpp etc.)    | Pending        | To be decided after benchmarking in feasibility spike                                    |
| TTS runtime (Onimi/VoiceStudio, etc.)      | Pending        | To be decided after feasibility and platform packaging validation                        |
