#!/usr/bin/env bash
set -euo pipefail

project_root="$(cd "$(dirname "$0")/.." && pwd)"
cd "$project_root"

if [[ -z "${KEEP_REFERENCE_APK:-}" || ! -f "$KEEP_REFERENCE_APK" ]]; then
  echo "::error title=Missing prerequisite::Set KEEP_REFERENCE_APK to the authorized Keep APK on this private runner. The APK is not stored in the repository."
  exit 2
fi
if [[ -z "${KEEP_REFERENCE_EVIDENCE:-}" || ! -d "$KEEP_REFERENCE_EVIDENCE" ]]; then
  echo "::error title=Missing prerequisite::Set KEEP_REFERENCE_EVIDENCE to the private directory containing the reference PNG fixtures."
  exit 2
fi
if [[ "$(adb get-state 2>/dev/null || true)" != "device" ]]; then
  echo "::error title=Missing prerequisite::No authorized Android 17 QEMU guest is connected to ADB."
  exit 2
fi
if [[ "$(adb shell getprop ro.build.version.sdk | tr -d '\r')" != "37" ]]; then
  echo "::error title=Wrong guest::Connected device must report API 37 (Android 17)."
  exit 2
fi
if [[ "$(adb shell wm size | tail -n 1 | tr -d '\r')" != "Physical size: 1080x2220" ]]; then
  echo "::error title=Wrong profile::Parity captures require the compact 1080x2220 profile."
  exit 2
fi

for fixture in current-launch.png REF-KEEP-002-settings-system-light.png REF-KEEP-003-theme-picker-system-light.png REF-KEEP-004-settings-explicit-dark.png REF-KEEP-016-drawer-dark.png REF-KEEP-017-settings-toggled-dark.png; do
  if [[ ! -f "$KEEP_REFERENCE_EVIDENCE/$fixture" ]]; then
    echo "::error title=Missing reference fixture::$KEEP_REFERENCE_EVIDENCE/$fixture"
    exit 2
  fi
done

echo "Installing the supplied reference APK on the isolated runner guest."
adb install -r "$KEEP_REFERENCE_APK"
if ! adb shell pm path com.google.android.keep >/dev/null 2>&1; then
  echo "::error title=Reference install failed::com.google.android.keep is not installed after adb install."
  exit 1
fi

./gradlew assembleDebug connectedDebugAndroidTest
python3 scripts/capture_clone_parity.py
python3 scripts/compare_screenshots.py \
  --reference-root "$KEEP_REFERENCE_EVIDENCE" \
  --rebuilt-root verification/rebuilt/ci-run \
  --output-root verification/comparison/results \
  --strict
