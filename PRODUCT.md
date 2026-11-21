# Product

<!-- impeccable:product-schema 1 -->

## Platform

android

## Stack

Kotlin and Jetpack Compose, as specified by the user. Local persistence uses Room; platform capabilities stay in Android APIs.

## Users

Android phone and tablet users who want to capture and retrieve personal notes, checklists, reminders, drawings, audio, and attachments. This audience is inferred from the requested Arcturus Memo feature and device scope.

## Product Purpose

Provide a local-first Arcturus Memo notes app with the supplied reference APK as the behavioral and visual reference. Success means that users can create, organize, find, and reopen notes without signing into an account or connecting to a network.

## Positioning

The requested app preserves the recognizable Keep interface while keeping user content on the device and replacing service-dependent behavior with local Android capabilities where feasible. This positioning is inferred from the user's offline and local-storage constraints.

## Operating Context

The app is evaluated on Android 17/API 37 QEMU guests without Google Play, in English (`en-US`), on compact phones, large phones, and tablets. Light, dark, system theme, orientation, font scale, keyboard, system insets, relaunch, process death, and reboot are in scope.

## Capabilities and Constraints

- Implement the note types and controls reachable in the supplied build, including text, lists, labels, pinning, sorting, search, color/background, archive, trash/restore, reminders, sharing, attachments, drawing, audio, launcher shortcuts, and widgets where the reference exposes them.
- Store notes, relationships, preferences, and attachment bytes on-device. Preserve edits and relationships through process death and reboot.
- Use a distinct application ID (`dev.arcturus.memo`) so the clone can be installed alongside `com.google.android.keep`.
- Use local reminders, Android pickers/capture, audio recording, drawing, and share/export where those implement the observed user-facing semantics. Clearly describe behavior requiring a remote counterpart.
- Do not require or reuse the reference account, credentials, tokens, or network.
- The supplied APK is version `5.26.461.01.90`, version code `220684604`, package `com.google.android.keep`, SHA-256 `081bb71dcd4a233f8704a760668ae97c3bb5a5feb81a59cd50a34f393378f7b1`.

## Brand Commitments

The user authorizes exact reproduction of the reference interface and visual assets. Arcturus Memo naming, colors, typography, layout, and interaction should follow the supplied reference APK where observable. The package identity remains distinct.

## Evidence on Hand

- Supplied APK at `/home/kenneth/Downloads/com.google.android.keep_5.26.461.01.90-220684604_minAPI29(arm64-v8a,armeabi-v7a,x86,x86_64)(nodpi)_apkmirror.com.apk`.
- A visible Android 17 QEMU guest, `KeepRef_Compact_API37`, is booted and the reference's signed-in Notes screen is empty. No credentials or account data are to be copied into the clone.
- Current baseline capture: `verification/reference/compact/current-launch.png`.
- APK resource and hierarchy captures will be retained with a provenance index under `verification/`.
- No backend, account integration, or evidence for remote service semantics is supplied.

## Product Principles

1. Keep note data private and local by default.
2. Preserve the reference's recognizable behavior and visual system.
3. Make the normal capture, edit, organize, and retrieval flows work without a network.
4. Use Android system surfaces for permissions, pickers, notifications, and sharing.
5. State remote-service limitations accurately instead of implying synchronization.

## Accessibility & Inclusion

Support Android accessibility labels and semantics, system font scaling, keyboard and IME actions, 48 dp touch targets where compatible with reference behavior, rotation, and tablet resizing. Exact parity remains subordinate to operability when accessibility services need additional semantics.
