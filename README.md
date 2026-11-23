# Arcturus Memo

Arcturus Memo is a local-first Android note-taking app built with Kotlin and Jetpack Compose. It supports fast capture, rich note organization, reminders, attachments, and an integrated A64 Studio assembly workspace - all stored on-device.

## Highlights

- **Quick capture** - Create text notes, checklists, drawings, audio notes, image notes, or assembly-code notes from the home `+` menu.
- **Mixed notes** - Combine plain text blocks and checkbox items in the same note. Add checkboxes inline, continue typing text below a list, and keep note content editable as your structure changes.
- **Checklist controls** - Add, check, reorder, and remove list items. Checked items can be moved to the bottom based on settings.
- **Organization** - Pin important notes, add labels, archive notes, move notes to trash, restore them, or delete them permanently.
- **Search** - Quickly find notes from the dedicated search screen.
- **Reminders** - Set note reminders and receive local notifications.
- **Attachments & capture** - Attach images from the Android photo picker, create drawing notes saved as private PNG files, and record audio notes saved as private audio files for playback.
- **Export & sharing** - Export notes as TXT, PDF, DOC/HTML, or HTML. Share note content through Android sharing.
- **Editor actions** - Pin/unpin, archive/unarchive, share, export, reminder, label, color/background, formatting, and collaborator options are available from the note editor.
- **Themes & settings** - Light, Dark, and System theme support with an OLED-friendly dark appearance. Settings persist locally for checklist behavior, sharing, and note defaults.
- **Assembly workspace** - Open **Assembly Code** from the `+` menu to launch the integrated A64 Studio workspace inside Arcturus Memo for ARM64 assembly editing and tooling.

## Build and install

Requirements: JDK 17+ (JDK 21 recommended), Android SDK Platform 37, Android Build Tools 37.0.0, and Android NDK 28.2.13676358.

```bash
JAVA_HOME=/home/kenneth/Android/jdk-21 ANDROID_HOME=/home/kenneth/Android/Sdk ./gradlew assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

- **Application ID:** `dev.arcturus.memo`
- **Min SDK:** 29
- **Target/Compile SDK:** 37
- **ABI:** `arm64-v8a` (required by the integrated A64 native core)

## Project notes

- Data is stored locally using Room.
- The UI is built with Jetpack Compose and Material 3.
- A64 Studio is integrated as an in-app assembly workspace; the Rust core is packaged as `liba64core.so`.
- Debug APK output: `app/build/outputs/apk/debug/app-debug.apk`
