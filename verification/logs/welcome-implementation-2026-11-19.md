# Welcome page implementation and verification

## Reference behavior

The supplied APK opens `com.google.android.apps.keep.ui.activities.WelcomeActivity` for its first-run intro. JADX inspection of `WelcomeActivity`, `welcome_activity.xml`, and `welcome_fragment.xml` showed a vertical pager with one visible intro page, a center-inside product logo, a headline, body copy, a `Get started` button, and a 10-second automatic dismissal. The activity’s click handler finishes the intro. The reference also exits through Back and a leftward pager swipe.

Observed copy:

- **Capture anything**
- **Make lists, take photos, speak your mind - whatever works for you, works in Keep.**
- **Get started**

The density variants of `product_logo_keep_2026_color_192` were copied from the authorized APK. The provenance and file hashes are listed in `verification/asset-provenance.md`. The theme uses Android Material 3 dynamic colors; the phone captures resolve to light surface `#FAF8FE`, text `#30323A`, button `#4C5E8B`, and button text `#F9F8FF`. Dark resolves to surface `#0D0E12`, text `#E4E5F0`, button `#B8C6EE`, and button text `#323F60`.

On the compact reference capture (1080×2220, 440 dpi, Android 17/API 37, portrait, en-US, font scale 1), the hierarchy reported the logo image view at `[0,0][1080,1642]`, headline at `[0,1642][1080,1731]`, body at `[0,1775][1080,1883]`, and button at `[391,1949][688,2088]`. The clone uses the same APK image asset, Android ImageView center-inside scaling, Android text views with the theme font aliases, and a Compose button. The latest clone hierarchies are `verification/rebuilt/compact/welcome-inset-light.xml` and `welcome-inset-dark.xml`. `aapt2 dump resources` confirmed that the APK's `gm3_ref_typeface_*` aliases map to the same Google Sans provider resources used by the clone. The CTA keeps a 50.5 dp hit target and draws its colored pill inside a 42.5 dp inset; its measured colored bounds now match the reference at `[391,1960][688,2077]` in both themes.

## Implemented paths

- Fresh local install opens the welcome page. A deep-linked note bypasses onboarding.
- Get started, Back, left swipe, or the 10-second timer returns to Notes.
- The completion flag persists through process restart so the welcome page is shown only once.
- Light and dark themes follow Android’s system palette with legacy color fallbacks.
- The intro remains local and usable without sign-in or network access.

## Verification

The first-run captures and accessibility hierarchies are in `verification/rebuilt/compact/`. The latest unmasked pixel comparisons, full-size pairs, 50% overlays, 4× diffs, and machine-readable results are in `verification/comparison/welcome-inset/`.

| Theme | Full-frame exact | App-content exact | Disposition |
| --- | ---: | ---: | --- |
| Light | 99.006048% | 99.201225% | Failed strict pixel match; text glyph differences remain open |
| Dark | 99.000209% | 99.201356% | Failed strict pixel match; text glyph differences remain open |

The clone's sampled logo region is 100% exact, and the CTA fill bounds match the reference. Text glyph rasterization and a small number of CTA-label pixels still differ. No pixels were masked and no exception was approved. A fresh install was tapped at `(540,2018)`; `welcome-inset-after-cta.png` and its hierarchy confirm that Get started opens Notes. The screen is visually and behaviorally implemented; exact pixel parity is not claimed.

The final run of `./gradlew assembleDebug lintDebug testDebugUnitTest connectedDebugAndroidTest` succeeded on `KeepRef_Compact_API37`. The connected suite passed 5/5 tests, including `welcomeCompletionSurvivesStoreReload`. See `verification/logs/welcome-final-build-test-2026-11-19.log`.

The signed-in tablet AVD was preserved. Current clone captures cover light and dark in portrait and landscape under `verification/rebuilt/tablet/`. Tablet portrait used the reversible `wm size 1600x2560` override on the physical 2560x1600 display. The existing landscape reference capture is preserved separately from a clean first-run state. The clone's tablet light surface is calibrated to the observed reference color `#FBF9FE`; its current landscape pair reaches 94.965330% app-content exact and 94.902295% full-frame exact, with deterministic text and control pixels still differing. A new shell launch attempt for the original non-exported `WelcomeActivity` was rejected with `SecurityException: ... not exported`; no dark tablet reference is available, and the signed-in original app data remains intact.
