# OLED dark mode verification — 2026-11-19

- Build: `:app:assembleDebug` succeeded with JDK 21.0.13 and Android SDK platform 37.
- The final Gradle output is `verification/logs/oled-dark-gradle-build.log`; `adb install -r dist/keepclone-debug.apk` returned Success, recorded in `verification/logs/oled-dark-install.log`.
- APK: `dist/keepclone-debug.apk`; SHA-256 `9a553bd4f4ed2ccce846fd9e20aa21cbcd750ab525b1061cd77d554e0e6dd0f1`; package `dev.arcturus.memo`, version 1.0.0 (1), debug signer SHA-256 `e033ac9918b9c9d8c8364bd1738ca8d6b43003aa264c53662670858a1b2118d7`.
- Target: Android 17/API 37 compact AVD (`emulator-5554`), 1080×2220, 440 dpi, font scale 1.0, locale en-US, timezone Europe/Belfast.
- Manual flow: installed updated APK, opened Settings, selected Light and confirmed the light surface, selected Dark and confirmed the Settings surface pixels sample as RGB `(0, 0, 0)`, then force-stopped and relaunched. SharedPreferences retained `theme_mode=Dark`; the re-opened Home screen remained black.
- Captures: `verification/rebuilt/compact/oled-dark-settings-final.png`, `oled-dark-after-restart.png`, `oled-theme-light-check.png`, and `oled-dark-drawer.png`, and `oled-dark-settings-final.xml` (accessibility hierarchy), and `oled-dark-dist-install.png` (installed handoff APK).
- The Pixel 8 serial `37261FDJH0033X` was not present in `adb devices -l` during this run, so this build was not installed on the phone. Both configured API 37 emulators remained available; only the compact AVD was used for this feature check.
- The black background is an explicit user-requested exception to the supplied Keep build's charcoal dark surfaces. This was verified functionally and is recorded in `verification/coverage-matrix.csv`; no exact parity claim is made for this color change.
