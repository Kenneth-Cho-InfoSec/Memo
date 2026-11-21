# CI execution status

- Local Gradle lint, unit-test task, debug APK build, and connected Android instrumentation ran on the Android 17/API 37 QEMU guest. The Gradle build completed successfully; the connected suite reports four passing tests (three repository tests and one settings-store persistence test). `testDebugUnitTest` is `NO-SOURCE`.
- Screenshot parity ran locally. The strict settings sweep intentionally exits 1 because five tested settings/theme images still differ at the pixel level; see `settings-sweep-parity.log` and `../comparison/settings-sweep/summary.json`.
- GitHub Actions hosted and self-hosted jobs have not run from this workspace. The hardware parity job requires a configured `android17-qemu` self-hosted runner and private reference inputs described in the README.
