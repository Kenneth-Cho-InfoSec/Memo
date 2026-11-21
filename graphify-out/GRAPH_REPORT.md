# Graph Report - google-keep-reimplementation  (2026-11-19)

## Corpus Check
- 131 files · ~813,034 words
- Verdict: corpus is large enough that graph structure adds value.
- Unclassified: 372 file(s) not represented in the graph (top: .xml 297, .log 62, .properties 2)

## Summary
- 578 nodes · 1236 edges · 33 communities (16 shown, 17 thin omitted)
- Extraction: 99% EXTRACTED · 1% INFERRED · 0% AMBIGUOUS · INFERRED: 16 edges (avg confidence: 0.87)
- Token cost: 0 input · 0 output

## Community Hubs (Navigation)
- KeepRoute
- asset-provenance.md
- Product
- Google Keep Reimplementation Design
- ReminderScheduler.kt
- KeepWelcomeScreen.kt
- KeepMotion
- KeepDao
- Review Focus
- AttachmentPreview
- summary.md
- capture_clone_parity.py
- Keep Clone (Android)
- verify-parity.sh
- KeepSettingsScreen.kt
- KeepAudioRecorderScreen.kt
- ci-status.md
- KeepApp.kt
- KeepRepositoryInstrumentedTest.kt
- KeepHomeScreen
- oled-dark-build-2026-11-19.md
- Welcome page implementation and verification
- Motion and editor verification on QEMU — 2026-11-19

## God Nodes (most connected - your core abstractions)
1. `KeepRepository` - 36 edges
2. `NoteEntity` - 32 edges
3. `KeepDao` - 24 edges
4. `KeepApp()` - 21 edges
5. `KeepNoteEditorScreen()` - 15 edges
6. `LabelEntity` - 14 edges
7. `KeepDatabase` - 13 edges
8. `ChecklistItemEntity` - 13 edges
9. `Product` - 12 edges
10. `ReminderScheduler` - 11 edges

## Surprising Connections (you probably didn't know these)
- `Task 5: Text and checklist editor` --references--> `KeepRepository`  [INFERRED]
  docs/superpowers/plans/2026-11-19-google-keep-reimplementation.md → app/src/main/java/dev/kenneth/keepclone/data/KeepRepository.kt
- `Task 2: Reference capture and asset provenance` --references--> `adb()`  [INFERRED]
  docs/superpowers/plans/2026-11-19-google-keep-reimplementation.md → scripts/capture_clone_parity.py
- `Settings and theme follow-up` --references--> `KeepSettingsStoreInstrumentedTest`  [INFERRED]
  verification/logs/manual-journeys-2026-11-19.md → app/src/androidTest/java/dev/kenneth/keepclone/data/KeepSettingsStoreInstrumentedTest.kt
- `KeepApp()` --calls--> `KeepDrawingScreen()`  [INFERRED]
  app/src/main/java/dev/kenneth/keepclone/ui/KeepApp.kt → app/src/main/java/dev/kenneth/keepclone/ui/KeepDrawingScreen.kt
- `KeepApp()` --calls--> `keepWelcomeBackgroundColor()`  [INFERRED]
  app/src/main/java/dev/kenneth/keepclone/ui/KeepApp.kt → app/src/main/java/dev/kenneth/keepclone/ui/KeepWelcomeScreen.kt

## Import Cycles
- None detected.

## Communities (33 total, 17 thin omitted)

### Community 0 - "KeepRoute"
Cohesion: 0.22
Nodes (9): KeepRoute, Audio, Collaborators, Drawing, Editor, Home, Search, Settings (+1 more)

### Community 4 - "Product"
Cohesion: 0.15
Nodes (12): Accessibility & Inclusion, Brand Commitments, Capabilities and Constraints, Evidence on Hand, Operating Context, Platform, Positioning, Product (+4 more)

### Community 5 - "Google Keep Reimplementation Design"
Cohesion: 0.18
Nodes (10): Approaches, Constraints, Goals, Google Keep Reimplementation Design, Intent, Non-goals, Open implementation details, Product structure (+2 more)

### Community 8 - "ReminderScheduler.kt"
Cohesion: 0.06
Nodes (12): MainActivity, NoteReminderReceiver, ReminderBootReceiver, ReminderScheduler, editedLabel(), EditorSheetAction(), FormatChip(), KeepNoteEditorScreen() (+4 more)

### Community 9 - "KeepWelcomeScreen.kt"
Cohesion: 0.05
Nodes (6): DrawingStroke, KeepDrawingScreen(), saveDrawing(), keepWelcomeBackgroundColor(), KeepWelcomeScreen(), KeepWelcomeText()

### Community 15 - "KeepDao"
Cohesion: 0.06
Nodes (8): KeepDao, KeepDatabase, KeepRepository, ChecklistItemEntity, LabelEntity, NoteEntity, NoteLabelCrossRef, NoteWithRelations

### Community 16 - "Review Focus"
Cohesion: 0.15
Nodes (12): Global Constraints, Google Keep Reimplementation Implementation Plan, Review Focus, Task 1: Reproducible Android skeleton and first launch, Task 2: Reference capture and asset provenance, Task 3: Room model and local preference repository, Task 4: Theme, settings, navigation, and home, Task 5: Text and checklist editor (+4 more)

### Community 19 - "AttachmentPreview"
Cohesion: 0.22
Nodes (5): AttachmentPreview(), AudioAttachmentPreview(), decodePreview(), KeepSearchScreen(), NotePreviewCard()

### Community 21 - "capture_clone_parity.py"
Cohesion: 0.12
Nodes (16): adb(), capture(), dump_ui(), main(), require(), tap(), wait_for_text(), compare_pixels() (+8 more)

### Community 22 - "Keep Clone (Android)"
Cohesion: 0.25
Nodes (7): Build and install, Evidence map, First launch, Keep Clone (Android), Scope and known gaps, Settings and themes, Verification

### Community 24 - "KeepSettingsScreen.kt"
Cohesion: 0.06
Nodes (6): KeepSettingsScreen(), label(), SectionTitle(), SettingSwitchRow(), ThemeChoiceRow(), KeepTheme()

### Community 27 - "KeepApp.kt"
Cohesion: 0.07
Nodes (10): KeepSettingsStoreInstrumentedTest, KeepSettings, KeepSettingsStore, ThemeMode, Dark, Light, System, isReturningRoute() (+2 more)

### Community 28 - "KeepRepositoryInstrumentedTest.kt"
Cohesion: 0.09
Nodes (3): KeepOnboardingStoreInstrumentedTest, KeepRepositoryInstrumentedTest, KeepOnboardingStore

### Community 29 - "KeepHomeScreen"
Cohesion: 0.26
Nodes (8): CreateTypeRow(), DrawerItem(), HomeToolbar(), KeepHomeScreen(), NavigationDrawer(), KeepColors, KeepNoteBackgroundAsset, NoteBackgroundColors

### Community 31 - "Welcome page implementation and verification"
Cohesion: 0.40
Nodes (4): Implemented paths, Reference behavior, Verification, Welcome page implementation and verification

### Community 32 - "Motion and editor verification on QEMU — 2026-11-19"
Cohesion: 0.29
Nodes (6): Captures, Clone implementation and observed checks, Motion and editor verification on QEMU — 2026-11-19, Reference motion inspected in JADX, Test guest and installed artifact, User-requested route slide

## Knowledge Gaps
- **11 isolated node(s):** `System`, `Light`, `Dark`, `Welcome`, `Home` (+6 more)
  These have ≤1 connection - possible missing edges or undocumented components. (Counts symbols only; 230 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **17 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `KeepRepository` connect `KeepDao` to `KeepHomeScreen.kt`, `KeepNoteEditorScreen.kt`, `NotePreviewCard.kt`, `ReminderScheduler.kt`, `Review Focus`, `KeepSearchScreen.kt`, `AttachmentPreview`, `KeepApp.kt`, `KeepRepositoryInstrumentedTest.kt`, `KeepHomeScreen`?**
  _High betweenness centrality (0.156) - this node is a cross-community bridge._
- **Are the 5 inferred relationships involving `KeepApp()` (e.g. with `KeepAudioRecorderScreen()` and `KeepCollaboratorsScreen()`) actually correct?**
  _`KeepApp()` has 5 INFERRED edges - model-reasoned connections that need verification._
- **What connects `System`, `Light`, `Dark` to the rest of the system?**
  _11 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `KeepHomeScreen.kt` be split into smaller, more focused modules?**
  _Cohesion score 0.058823529411764705 - nodes in this community are weakly interconnected._
- **Why does `NoteEntity` connect `KeepDao` to `KeepHomeScreen.kt`, `KeepNoteEditorScreen.kt`, `NotePreviewCard.kt`, `ReminderScheduler.kt`, `KeepSearchScreen.kt`, `AttachmentPreview`, `KeepApp.kt`, `KeepHomeScreen`?**
  _High betweenness centrality (0.081) - this node is a cross-community bridge._
- **Should `KeepCollaboratorsScreen.kt` be split into smaller, more focused modules?**
  _Cohesion score 0.14285714285714285 - nodes in this community are weakly interconnected._
- **Why does `Task 5: Text and checklist editor` connect `Review Focus` to `KeepDao`?**
  _High betweenness centrality (0.044) - this node is a cross-community bridge._