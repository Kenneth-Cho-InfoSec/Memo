# Arcturus Memo Reimplementation Design

## Intent

Build a runnable, local-first Kotlin/Compose Android app that reproduces the supplied Arcturus Memo APK's visible interface and observable interactions on Android phones and tablets. The user asked for direct implementation, signing, QEMU validation, CI, and an evidence-backed handoff.

## Goals

- Match the supplied build's app-owned pixels and user-visible state changes, including light, dark, and System default themes.
- Support text notes, checklists, search, labels, pin/sort, color/background, archive/trash, reminders, sharing, attachments, drawing, audio, widgets, and launcher surfaces as the reference exposes them.
- Persist notes and attachments locally across edits, process death, and reboot.
- Run without a Google account or network; use local Android substitutes where they preserve the observable user semantics.
- Deliver reproducible source, wrapper, CI, a signed debug APK, and a verification bundle with honest dispositions.

## Non-goals

- Reproducing Google account sign-in, cloud sync, collaborative editing with a remote participant, or any backend that is not supplied.
- Claiming exact parity for states that were not captured or whose behavior depends on a remote service.
- Using the reference signing identity, account credentials, or package ID.

## Constraints

- Kotlin and Jetpack Compose; Android platform integrations for notifications, pickers, recording, widgets, and shares.
- Target the Android 17/API 37 QEMU guest; `compileSdk=37`, `targetSdk=37`, and `minSdk=29` follow the reference APK's API floor.
- Application ID `dev.arcturus.memo`; English `en-US`; local-first operation.
- Room stores notes, checklist rows, labels and relations, sort/order metadata, reminders, and attachment metadata. App-private files store attachment bytes.
- The supplied APK's exact light/dark note background colors are the source of truth for those tokens. App theme choices are Light, Dark, and System default.
- Extracted assets must be selected deliberately and listed with source path, qualifier, origin APK hash, and shipped hash. Recreated vectors are identified as recreated.

## Approaches

1. **Jetpack Compose with Room and Android platform APIs — selected.** This is the requested stack, supports responsive phone/tablet layouts and accessibility semantics, and permits local persistence plus native system surfaces.
2. **Android Views with SQLite.** It could reproduce native behavior but contradicts the requested Compose implementation.
3. **WebView or a browser-based UI.** It would simplify rapid layout iteration but would miss native keyboard, picker, notification, widget, and gesture behavior, and contradicts the requested stack.

The user explicitly selected Compose and requested local storage, so the first approach is a direct reading of the brief rather than a new product decision.

## Product structure

- **Home/navigation:** notes and archive/trash/reminders/labels, responsive list/grid, search, sorting, empty and populated states, drawer, selection mode, FAB.
- **Editor:** text/checklist editing, checklist reorder and checked behavior, pin/archive, labels, color/backgrounds, attachments, drawing/audio, reminder, share/export, undo, keyboard and Back behavior.
- **Settings:** item-order switches, rich-link preference, theme choice, default note-creation mode, sharing preference; persisted locally.
- **Platform surfaces:** Android pickers, camera/audio capture, local reminders/notifications, launcher shortcuts, widgets, and system shares where observed.

## Visual direction

This is a faithful reconstruction of an established visual system, not a redesign. Use the installed APK, captured settings/home/editor states, resource table, and extracted assets as visual authority. Use fixed source tokens from the APK rather than Android dynamic color when it would change Keep-owned pixels. Preserve system UI, insets, IME, keyboard, accessibility, and native chooser surfaces where Keep delegates to Android.

## Open implementation details

- Confirm exact screen geometry, component dimensions, typography, and state transitions from the live APK and extracted resources before coding each screen.
- Treat typed/pasted URL preview generation and remote collaborators as unresolved/service-dependent until stronger reference evidence exists.
- Build local substitutes with clearly recorded semantics for reminders, audio, attachments, and sharing.

## Verification standard

Use matching fixtures and profiles. Preserve raw native-resolution captures, UI hierarchies, recordings, per-state results, pixel diffs, and overlays. A deterministic app-owned region passes only when it matches exactly; mask only measured, demonstrably variable system content. Re-run affected journeys after each visual or behavior fix.
