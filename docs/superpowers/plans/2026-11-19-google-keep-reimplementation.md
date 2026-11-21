# Arcturus Memo Reimplementation Implementation Plan

> **For agentic workers:** This plan is executed natively in this session; no sub-agent delegation is used.

**Goal:** Build and verify a local-first Compose notes app matching the supplied Arcturus Memo APK across the captured phone and tablet states.

**Architecture:** A single Android app uses Compose screens over a Room repository, with preferences stored locally and Android platform APIs for notifications, pickers, media capture, widgets, shortcuts, and sharing. A verification directory ties reference observations, rebuilt screenshots, interaction tests, and pixel comparisons to stable state IDs.

**Tech Stack:** Kotlin 2.4.10; AGP 9.5.1; Gradle 9.6.0; Android API 37; Compose BOM 2026.09.00; Room 2.8.5 with KSP 2.3.10; JDK 21 host, targeting Java/Kotlin 17 bytecode.

**Spec:** `docs/superpowers/specs/2026-11-19-google-keep-reimplementation-design.md`

## Global Constraints

- Use Kotlin and Jetpack Compose; target API 37 and support min API 29.
- Use application ID `dev.arcturus.memo` and never reuse Google account state or credentials.
- Keep notes, relationships, preferences, and attachment data on-device.
- Match reference behavior and visuals in English on phones and tablets; support Light, Dark, and System default.
- Use Android platform features for system pickers, notifications, media capture, widgets, shortcuts, and share intents.
- Keep the project build reproducible with the Gradle wrapper and exact dependency versions.
- Run unit and instrumentation checks because the user explicitly requested testing and verification.

## Review Focus

- Local transactions can leave checklist, label, or attachment relations inconsistent; data tests assert relationship preservation and rollback.
- Editor changes can be lost on Back, process death, or rotation; journey tests assert the expected saved draft state.
- Dark mode and large font scale can clip controls or misrender note backgrounds; QEMU captures cover both theme modes and font scale 1.3.
- Android 17 permission denial or cancellation can strand an editor; UI journeys assert recoverable cancellation and no data loss.
- Time-varying system bars and platform picker content can skew screenshot diffs; comparisons preserve raw captures and document only measured narrow exclusions.

---

### Task 1: Reproducible Android skeleton and first launch

**Files:**
- Create: `settings.gradle.kts`, root `build.gradle.kts`, `gradle/libs.versions.toml`, `gradle.properties`, Gradle wrapper scripts/jar, `app/build.gradle.kts`, `app/src/main/AndroidManifest.xml`.
- Create: `app/src/main/java/dev/arcturus/memo/MainActivity.kt`, `app/src/main/java/dev/arcturus/memo/ui/KeepApp.kt`.
- Create: `README.md`, `.gitignore`, `.github/workflows/android.yml`.

**Interfaces:**
- Consumes: user design and this plan.
- Produces: `dev.arcturus.memo` launchable Compose Activity, wrapper build, debug signing, and baseline CI workflow.

- [ ] Pin AGP 9.5.1, Gradle 9.6.0, Kotlin/Compose compiler 2.4.10, Compose BOM 2026.09.00, Activity Compose 1.13.0 (latest published stable; 1.14.0 was still alpha), Room 2.8.5, and KSP 2.3.10.
- [ ] Set `compileSdk=37`, `targetSdk=37`, and `minSdk=29`; declare only justified permissions.
- [ ] Build `assembleDebug`, install on `emulator-5554`, and record package/activity launch evidence.

### Task 2: Reference capture and asset provenance

**Files:**
- Create: `verification/reference-environment.md`, `verification/screen-inventory.csv`, `verification/coverage-matrix.csv`, `verification/asset-provenance.csv`, `verification/environment/`.
- Create: `verification/reference/phone`, `verification/reference/large-phone`, `verification/reference/tablet`, `verification/captures.sha256`.
- Create: `app/src/main/res/drawable*/` for selected authorized assets and `docs/asset-provenance.md`.

**Interfaces:**
- Consumes: supplied APK, AVD `KeepRef_Compact_API37`, and reference interactions.
- Produces: stable IDs and exact token/assets evidence used by UI and parity tasks.

- [ ] Recheck APK metadata, signature, hash, AVD configuration, display, services, and `adb` fingerprint.
- [ ] Capture first-run and signed-in reference states without exposing account identity in deliverables.
- [ ] Record entry path, fixture, layout profile, visible controls, outcomes, exits, and capture filenames for each reachable state.
- [ ] Extract chosen APK vectors/backgrounds/strings as feasible; record source and shipped SHA-256 values.

### Task 3: Room model and local preference repository

**Files:**
- Create: `app/src/main/java/dev/arcturus/memo/data/KeepDatabase.kt`, `NoteEntity.kt`, `ChecklistItemEntity.kt`, `LabelEntity.kt`, `AttachmentEntity.kt`, `KeepDao.kt`, `KeepRepository.kt`, `KeepPreferences.kt`.
- Test: `app/src/test/java/dev/arcturus/memo/data/KeepRepositoryTest.kt`.

**Interfaces:**
- Produces: Flow-based repository operations for note lists, detail, labels, preferences, trash/archive, ordering, and undo snapshots.

- [ ] Write tests for note CRUD, checklist ordering/check state, label relations, pin/sort, archive/trash/restore, search filtering, and transaction rollback.
- [ ] Run the targeted unit tests and confirm expected failure before repository implementation.
- [ ] Implement Room schema version 1, transactional DAO operations, and repository behavior; rerun tests.

### Task 4: Theme, settings, navigation, and home

**Files:**
- Create: `ui/theme/KeepTheme.kt`, `KeepColors.kt`, `KeepTypography.kt`, `ui/navigation/KeepDestination.kt`, `ui/home/HomeScreen.kt`, `ui/home/NoteGrid.kt`, `ui/navigation/NavigationDrawer.kt`, `ui/settings/SettingsScreen.kt`.
- Test: `app/src/androidTest/java/dev/arcturus/memo/ThemeSettingsTest.kt`.

**Interfaces:**
- Consumes: preference repository and `verification` reference tokens.
- Produces: home, drawer, settings, list/grid, search entry, selection, FAB, and light/dark/system theme surfaces.

- [ ] Add semantic UI tests for setting labels and values, theme selection, drawer destinations, list/grid preference, search opening, and FAB actions.
- [ ] Implement reference-matched color, type, insets, app bars, card geometry, drawer, settings, and responsive phone/tablet layout.
- [ ] Capture paired light/dark/home/settings screenshots and fix measured app-owned discrepancies.

### Task 5: Text and checklist editor

**Files:**
- Create: `ui/editor/NoteEditorScreen.kt`, `ChecklistEditor.kt`, `EditorToolbar.kt`, `EditorActions.kt`, `ui/components/ChecklistRow.kt`, `ui/components/UndoSnackbar.kt`.
- Test: `app/src/androidTest/java/dev/arcturus/memo/EditorJourneyTest.kt`.

**Interfaces:**
- Consumes: `KeepRepository` note/detail flows and shared theme components.
- Produces: create/edit text and list notes, checklist ordering/check behavior, keyboard/IME handling, autosave, undo, pin/archive, labels, and back navigation.

- [ ] Add journeys for create/edit, checklist insertion order, check-to-bottom behavior, drag reorder, undo, Back cancellation, and restart persistence.
- [ ] Implement editor state and transactional saves; run unit and instrumentation checks.

### Task 6: Search, organization, backgrounds, reminders, and service substitutes

**Files:**
- Create: `ui/search/SearchScreen.kt`, `ui/labels/LabelScreen.kt`, `ui/notes/NoteActions.kt`, `ui/background/BackgroundPicker.kt`, `platform/ReminderScheduler.kt`, `platform/ReminderReceiver.kt`, `platform/LocalShare.kt`.
- Test: repository and UI tests for search, labels, sort, archive/trash, colors, reminders, and share cancellation.

**Interfaces:**
- Consumes: repository models and APK-derived colors/background assets.
- Produces: searchable/filtered note lists and local substitutes for eligible service-dependent actions.

- [ ] Implement search across titles, text, checklist rows, and labels; stable sort, pinning, archive, trash, restore, and batch selection.
- [ ] Implement exact note color/background tokens and durable user preferences.
- [ ] Implement local AlarmManager/notification reminders and Android chooser-based export; document remote collaboration and preview-generation limits.
- [ ] Verify eligible local flows in airplane mode and permission-denied/canceled paths.

### Task 7: Attachments, drawing, audio, launcher, and widgets

**Files:**
- Create: `platform/AttachmentPicker.kt`, `platform/AudioRecorder.kt`, `ui/drawing/DrawingScreen.kt`, `platform/KeepWidgetProvider.kt`, `res/xml/shortcuts.xml`, widget layouts and previews.
- Test: `app/src/androidTest/java/dev/arcturus/memo/AttachmentJourneyTest.kt`.

**Interfaces:**
- Consumes: attachment metadata/file repository and note editor callbacks.
- Produces: image/file selection, camera intent, local audio recording, vector/bitmap drawing save, shortcuts, and note collection/single-note widgets where supported.

- [ ] Implement with Activity Result APIs and app-private files; canceling a picker must leave edits intact.
- [ ] Test granted and denied permissions, file persistence, reboot-safe links, and widget navigation.

### Task 8: Parity automation, regression, CI, and handoff

**Files:**
- Create: `verification/scripts/capture.sh`, `compare.py`, `run_critical_journeys.sh`, per-state machine-readable results, diff/overlay outputs, CI artifact upload configuration.
- Modify: `.github/workflows/android.yml`, `README.md`, coverage matrix.

**Interfaces:**
- Consumes: runnable debug APK, reference captures, fixtures, and AVD profiles.
- Produces: signed installable APK, complete checks, screenshot pairs/diffs, CI workflow, and handoff evidence.

- [ ] Run unit tests, lint, instrumentation journeys, process restart, guest reboot, and permission variations on Android 17 QEMU.
- [ ] For each applicable stable state, generate reference/rebuilt images, overlays, raw diffs, JSON comparison metrics, and a truthful disposition.
- [ ] Configure GitHub Actions standard lint/test/build and self-hosted Android 17 QEMU/KVM parity jobs with explicit prerequisite failures.
- [ ] Verify the final APK hash, signature, package ID, installation, launch, and core journeys; list all gaps and hosted job state.

## Version sources

- [Android Gradle Plugin compatibility and API-level support](https://developer.android.com/build/releases/agp-9-5-0-release-notes)
- [Gradle 9.6 release](https://docs.gradle.org/9.6.0/release-notes.html)
- [Compose BOM and compiler plugin setup](https://developer.android.com/develop/ui/compose/setup-compose-dependencies-and-compiler)
- [Room 2.8.5 release](https://developer.android.com/jetpack/androidx/releases/room)
- [Kotlin releases](https://kotlinlang.org/docs/releases.html)
