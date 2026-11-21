# Motion and editor verification on QEMU — 2026-11-19

## Test guest and installed artifact

- AVD: `KeepRef_Tablet_API37`, ADB serial `emulator-5556` (windowed QEMU Android Emulator; x86_64; Android 17/API 37).
- Guest fingerprint: `google/sdk_gphone16k_x86_64/emu64xa16k:17/CP41.260831.015/16516427:userdebug/dev-keys`.
- Physical display: 2560×1600 at 320 dpi. The guest reported rotation 1 and rendered a 1600×2560 portrait app surface. Font scale was 1.0, Android night mode was on, and the app followed System theme. The guest activity configuration reported en_US; displayed UI was English.
- Package: `dev.arcturus.memo`, version `1.0.0` (versionCode 1), target SDK 37. Debug signing certificate SHA-256: `e033ac9918b9c9d8c8364bd1738ca8d6b43003aa264c53662670858a1b2118d7`.
- APK SHA-256: `722bfe7471867ea1657cfea0d37478ebabe2ee6cebe70812f3da55ee3227baf9` (`dist/keepclone-debug.apk`). Pulled installed `base.apk` had the same SHA-256.
- The Pixel 8 was not used. `KeepRef_Compact_API37` had exited with status 139 while restarting from its incompatible saved renderer snapshot, so these checks used the already-running tablet AVD.

## Reference motion inspected in JADX

JADX output was generated from the supplied Keep APK into `/tmp/keep-jadx`.

- `resources/res/animator/fragment_open_enter.xml`: scale 0.85→1 over 300 ms, with a 50 ms alpha hold followed by a 50 ms fade. `fragment_open_exit.xml` scales 1→1.15 over 300 ms.
- `resources/res/animator/fragment_close_enter.xml`: scale 1.1→1 over 300 ms, alpha delay 66 ms then a 50 ms fade. `fragment_close_exit.xml` scales 1→0.9 over 300 ms.
- Both fragment directions use `resources/res/anim/fragment_fast_out_extra_slow_in.xml`, path `M 0,0 C 0.05,0 0.133333,0.06 0.166666,0.4 C 0.208333,0.82 0.25,1 1,1`.
- `resources/res/anim/m3_bottom_sheet_slide_in.xml` slides from 20% of its parent height while fading in, using emphasized Material easing and medium4 duration (400 ms). The exit translates down 20% with emphasized easing (medium3, 350 ms) and fades (medium2, 300 ms).
- `ColorPickerSwatch.m6131a()` (`sources/com/google/android/apps/keep/p006ui/colorpicker/ColorPickerSwatch.java`) updates selection stroke/check visibility directly. It does not animate the selected swatch; the swatch uses the platform borderless selectable ripple. The clone intentionally keeps the check and border immediate and uses ripple feedback, instead of adding a scale animation the reference does not use.

## Clone implementation and observed checks

- Route changes use a 300 ms horizontal slide in `ui/KeepApp.kt`: forward navigation brings the destination in from the right and moves the current route left; back navigation reverses the direction. The existing KeepMotion fragment easing remains in use. This intentionally differs from the APK scale/fade route transition because the user requested the sliding behavior.
- Home list/grid changes, card reordering, FAB visibility, create-menu rows, and drawer/scrim state have explicit Compose transitions in `ui/home/KeepHomeScreen.kt`. Checklist/text editor switching and checklist item changes animate in `ui/editor/KeepNoteEditorScreen.kt`. Material controls provide their normal pressed/ripple and control-state feedback. Palette selection follows the reference's immediate check/border update. Tap targets use Material buttons or default Compose clickable indication; no target was configured with indication disabled.
- On QEMU, first-run welcome advanced to empty Home, the animated create menu opened, a text note opened, and the note palette and overflow sheet were opened. The palette showed the `Colour` and `Background` rows; selecting a swatch changed the note surface. Overflow showed the timestamp, Reminder, Find in note, Make a copy, Send, Labels, Delete, and Help & feedback.
- The note body `Sensing and logging` survived editor exit, force-stop, and app relaunch. In System dark mode, the default note surface rendered RGB(0,0,0). The white caret was visible while editing on black and on the selected colored note surface.
- No `FATAL EXCEPTION` / `AndroidRuntime` crash was present in the last 2,000 guest logcat lines. This was a manual QEMU smoke check, not a complete animation timing or pixel-parity sweep.

## User-requested route slide

- The original APK uses scale/fade for fragment navigation. The user explicitly requested that opening a screen slide into view, so the clone uses horizontal route motion as an approved visual exception.
- On KeepRef_Tablet_API37, a sequence captured during note opening shows Home exiting left while the editor enters from the right, followed by the settled editor. The transition duration is 300 ms; a frame-timed comparison against the reference animation was not run.
- The latest distributed APK was installed and launched on QEMU. Pulled `base.apk` SHA-256 equals `dist/keepclone-debug.apk` SHA-256 (`7886da35fa1c5c781ffc74b4583a2b3368164ae965bab83404edebacdfe15c00`). The Pixel 8 was not present in `adb devices`, so it was not updated in this pass.

## Captures

All files below are native ADB PNG screencaps unless marked XML. Captures and UI hierarchy dumps are under `verification/rebuilt/compact-qemu/`:

- `home-motion-tablet-qemu.png`, `home-motion-tablet-qemu.xml`
- `create-menu-motion-frame.png`, `create-menu.xml`
- `note-editor-tablet-qemu.png`, `note-editor-tablet-qemu.xml`, `note-editor-landscape-qemu.png`
- `note-palette-tablet-qemu.png`, `note-palette-tablet-qemu.xml`
- `note-overflow-tablet-qemu.png`, `note-overflow-tablet-qemu.xml`
- `note-editor-dark-tablet-qemu.png`
- `note-caret-dark-tablet-qemu.png`, `note-caret-dark-tablet-qemu.xml`
- `note-palette-dark-tablet-qemu.png`, `note-color-dark-tablet-qemu.png`
- `note-caret-colored-dark-tablet-qemu.png`, `note-caret-colored-dark-tablet-qemu.xml`
- `route-slide-open-{early,mid,late,settled}.png` (QEMU note-open horizontal transition)
- `palette-enter-{080ms,200ms,340ms,540ms}.png` and `palette-exit-{080ms,250ms,470ms}.png`
- `home-note-after-editor-qemu.png`, `home-note-after-relaunch-qemu.png`, `home-note-after-relaunch-qemu.xml`
- `keepclone-editor-motion-landscape-qemu.mp4` (H.264, 2560×1600, 4.37 s; the editor and palette sheet are visible)

The user supplied three phone screenshots as visual references. The toolbar, color/background picker, and overflow content were exercised against those observations. A same-resolution reference run and machine pixel diff were not available for these new editor screens; this record does not claim exact editor pixel or motion parity.
