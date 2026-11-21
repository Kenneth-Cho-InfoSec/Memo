# Arcturus Memo (Android)

A local-first Kotlin and Jetpack Compose implementation built alongside the supplied reference APK (`com.google.android.keep`). The clone uses the distinct application ID `dev.arcturus.memo` and stores notes, checklist items, labels, and note preferences on device.

## Build and install

Requirements: JDK 17 or later, Android SDK platform 37, Android build tools 37.0.0, and the Gradle wrapper included in this project.

```bash
./gradlew assembleDebug
adb install -r dist/arcturus-memo-debug.apk
adb shell am start -n dev.arcturus.memo/.MainActivity
```

The installable, debug-signed handoff build is `dist/arcturus-memo-debug.apk`; the reproducible source project is also packaged as `dist/arcturus-memo-source.zip`. The APK uses the standard Android debug key; no release credentials were provided. Hashes for the APK, source archive, and verification bundle are in `dist/SHA256SUMS`. The screenshots, comparisons, inventory, coverage matrix, environment details, and test logs are packaged in `dist/arcturus-memo-verification-bundle.zip`.

## First launch

The app shows the APK-matched “Capture anything” welcome page on the first local launch. It uses the supplied Keep logo and Google Sans provider aliases, switches between the observed light and dark palettes, and exits to Notes through Get started, Back, a left swipe, or the observed 10-second timeout. The completion flag is stored on-device, so a later process launch opens Notes directly. A deep-linked note bypasses onboarding.

The latest first-run light and dark captures are `verification/rebuilt/compact/welcome-inset-light.png` and `welcome-inset-dark.png`, with matching accessibility hierarchies. Unmasked overlays, 4× diffs, region metrics, and JSON results are under `verification/comparison/welcome-inset/`. App-content exact matching is 99.201225% for light and 99.201356% for dark; full-frame matching is 99.006048% and 99.000209%. The visible CTA pill bounds match the reference. Both strict comparisons still fail because text glyph pixels differ. No pixels are masked. On the tablet landscape light pair, app-content matching is 94.965330%; that strict comparison remains failed.

## Settings and themes

The Settings screen includes the observed options for adding checklist items at the bottom, moving checked items to the bottom, rich link previews, Light/Dark/System theme, creating text notes by default, and sharing. Settings labels and the Theme row respond to taps, and all preference values persist locally. Light and Dark stayed selected independently of Android night mode; System followed guest day/night changes. The five observed switches were toggled and restored to their defaults. Compose typography uses the authorized Product Sans font extracted from the supplied APK.

Dark mode uses a pure black `#000000` app, drawer, and Settings background for OLED displays. Search surfaces and default dark note cards use `#121212` for separation and legibility. This color adjustment is the user-requested exception to the supplied build's charcoal dark surfaces; Light and System behavior remain available.

The sharing preference gates the editor's Collaborator action; Android text Send remains a separate overflow action. The clone provides a local-only Collaborators screen and explains when it cannot send an invite. It does not connect to Google's remote collaboration service or request contacts permission.

## Verification

```bash
./gradlew lintDebug testDebugUnitTest assembleDebug
./gradlew connectedDebugAndroidTest
KEEP_REFERENCE_APK=/path/to/private/keep.apk \
KEEP_REFERENCE_EVIDENCE=/path/to/private/reference/compact \
  scripts/verify-parity.sh
```

The parity script installs the supplied APK on the isolated guest, runs instrumentation, captures the clone to `verification/rebuilt/ci-run/`, and compares it with the private reference directory. The screenshot comparator preserves full-size inputs, writes unmasked 4× pixel diffs, 50% overlays, and JSON results under `verification/comparison/results/`. Settings comparisons are under `verification/comparison/settings-sweep/`; the latest welcome light/dark compact pair is under `verification/comparison/welcome-inset/`, with the tablet landscape light pair under `verification/comparison/welcome-tablet-final/`. These strict visual pairs still have deterministic pixel differences. The comparator reports app-content matching separately from system bars; no pixels are masked. `--strict` fails if any app-content pixel differs.

There is currently no JVM unit-test source, so `testDebugUnitTest` completes as `NO-SOURCE`. Room repository rules are exercised by `connectedDebugAndroidTest` on the QEMU guest.

For the hardware parity job, see `.github/workflows/android.yml`. Configure a private self-hosted Linux runner labelled `android17-qemu`, boot a disposable Android 17/API 37 compact QEMU guest, and make it available over ADB. Set the private repository variables `KEEP_REFERENCE_APK` and `KEEP_REFERENCE_EVIDENCE` to local paths on that runner. The APK and private reference screenshot inputs are not embedded in the workflow. A missing input, wrong guest, failed instrumentation run, or app-content pixel difference is reported as a failing job. Hosted Actions has not been run from this workspace.

## Scope and known gaps

The compact-phone flows exercised locally include first-run Welcome in both themes, Home, the navigation drawer, checklist editing and checked previews, Settings, Light/Dark/System selection, search, labels, archive/trash/restore, local reminders and notifications, image attachment from Android Photo Picker, drawing notes saved as private PNGs, and recorded audio notes saved as private AAC/M4A with playback. Room schema migration from v1 to v2 retained the existing synthetic note and label relationship. Five instrumentation tests passed: three Room repository tests, one settings-store test, and one welcome completion persistence test. For populated flows without a matched original-app capture, the matrix records the local result and leaves visual parity blocked.

Image attachments are copied into app-private storage and were verified through editor, home-card preview, force-stop, and relaunch. Drawing saves local strokes to a private PNG; Audio uses the guest microphone and saves a private AAC/M4A file that the note can play. Audio permission denial returns to Home without creating a note. Reminder scheduling and notifications were exercised locally, but their reference picker/notification flow has not been captured. The welcome page has current light/dark clone captures on the signed-in tablet in portrait and landscape. Tablet portrait uses a reversible `wm size 1600x2560` profile override. A light landscape reference pair remains failed at 94.965330% app-content exact. A new attempt to start the original non-exported `WelcomeActivity` was denied by Android with `SecurityException: not exported`, so there is no matched tablet dark reference; the signed-in account remains intact. Remote collaborator invitations, widgets, large-phone layouts, rotation/font-scale/window-size profiles, and the full reference state inventory remain incomplete. The Settings and common home/editor screens still differ from the reference in deterministic pixels; this build does not claim exact parity.

The build and five-test instrumentation suite ran locally on the Android 17 QEMU guest. GitHub hosted and self-hosted workflow runs remain pending.

## Evidence map

- `verification/environment/` — guest, APK, toolchain, and system-service fingerprints.
- `verification/reference/compact/` — private original-app observations; do not publish with an untrusted repository.
- `verification/rebuilt/compact/` — clone screenshots and accessibility hierarchy dumps.
- `verification/comparison/` — state-pair manifest and generated diffs, overlays, and JSON measurements.
- `verification/screen-state-inventory.csv` — entry paths, fixtures, controls, exits, profiles, and captures for the explored states.
- `verification/coverage-matrix.csv` — observed interactions, device profile, results, and disposition.
- `verification/asset-provenance.md` — copied APK resources and recreated artwork.
- `verification/logs/` — local build and test logs.
