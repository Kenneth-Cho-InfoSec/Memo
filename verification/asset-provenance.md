# Asset provenance

Reference artifact: `com.google.android.keep_5.26.461.01.90-220684604_minAPI29(arm64-v8a,armeabi-v7a,x86,x86_64)(nodpi)_apkmirror.com.apk`

Reference APK SHA-256: `081bb71dcd4a233f8704a760668ae97c3bb5a5feb81a59cd50a34f393378f7b1`

| Shipped asset | APK resource | Origin | Notes |
|---|---|---|---|
| `app/src/main/res/drawable-{mdpi,hdpi,xhdpi,xxhdpi,xxxhdpi}/ic_lightbulb_outline_yellow500_120.png` | `drawable/ic_lightbulb_outline_yellow500_120` | Supplied Keep APK | Density variants copied from the decoded resource table and used in the empty-notes state. |
| `app/src/main/res/font/product_sans_regular.ttf` | `assets/ProductSans-Regular.ttf` | Supplied Keep APK | Product Sans regular applied to the Compose typography theme. |

Copied file hashes:

| Density | SHA-256 |
|---|---|
| mdpi | `8774fea4c31c1ff74da470729cba8fffe6ea13a6cb6783b4c154f860ca658da2` |
| hdpi | `372bb1fcee313dea92d3b58c14a321ff7e804610a1f4f2bf9474ab9fbb615cd4` |
| xhdpi | `05af60872bb08053edc4992a32be5a1285ca444d779b112822a408b6f0bba179` |
| xxhdpi | `beee997d3dcfdeb7d4651a8a95bcff49d4318fb29dc1fe36a87d4e877edfa05c` |
| xxxhdpi | `1523a7aacb77444b619ad7dbbca07b451ebde34a3266d69b96f67951c0441c53` |
| Product Sans font | `7d4fcd58dada46482135f6a5a432e4cd0b79b6c9b48132bc4b52d805efb14381` |

Compose Material icons used elsewhere are platform library vectors, not extracted APK assets. Note background colors and the custom toolbar/drawer are recreated in Kotlin; remaining resource extraction has not yet been completed.

Settings/collaboration additions:

| Shipped asset | APK resource | Origin | Notes |
|---|---|---|---|
| Collaborator person-add icon | None | Compose Material `Icons.Filled.PersonAdd` | Vector rendered by the pinned Material icons dependency; not copied from the APK. |
| Local “This device” avatar | None | Recreated Compose circle and letter glyph | Synthetic local placeholder. It contains no signed-in account name, address, or profile image. |

Welcome screen additions:

| Shipped asset | APK resource | Origin | Notes |
|---|---|---|---|
| `app/src/main/res/drawable-{ldpi,mdpi,hdpi,xhdpi,xxhdpi,xxxhdpi}/product_logo_keep_2026_color_192.png` | `drawable/product_logo_keep_2026_color_192` | Supplied Keep APK | Six density variants copied byte-for-byte; displayed at 192dp in the first-run page. |
| `app/src/main/res/font/google_sans_compat.xml` | `font/gm3_ref_typeface_brand_regular` | Supplied Keep APK | Google Sans provider alias and `google-sans` system fallback. |
| `app/src/main/res/font/google_sans_text_compat.xml` | `font/gm3_ref_typeface_plain_regular` | Supplied Keep APK | Google Sans Text provider alias and `google-sans-text` system fallback. |
| `app/src/main/res/font/google_sans_text_medium_compat.xml` | `font/gm3_ref_typeface_plain_medium` | Supplied Keep APK | Medium Google Sans Text provider alias and system fallback. |
| `app/src/main/res/values/google_font_provider_certs.xml` | `array/com_google_android_gms_fonts_certs` and `array/com_google_android_gms_fonts_certs_dev` | Supplied Keep APK `res/values/arrays.xml` | Provider certificate arrays retained for AndroidX font resolution. |

Welcome logo SHA-256 values:

| Density | SHA-256 |
|---|---|
| ldpi | `622b47a7a14243db7e1a3e46bb8f7e27ac1c1befe249fb0c86395602bc6f4cb8` |
| mdpi | `2f0f01da271766637b2c167abc4f57a57a69b8fdcbcfda8391c6ec69d4d506b3` |
| hdpi | `80650d801d43ef66f36ce93071728cd43b7ef2e4d349d7c8851912208b0ce5e1` |
| xhdpi | `cf27c7eb769ea7513921c5276b5a27926194c236a270409898d4d4201a4bfcab` |
| xxhdpi | `daa9b14fc15a561715b786221345a426275c3827d1355b3cba54bbe2e7af6bab` |
| xxxhdpi | `29b67009fc99bd41faa8d54306df8fdd4ae470537a7c95acc517dea76c26bca5` |

The welcome layout, interaction timing, text and theme colors are implemented in Kotlin/Compose. Android `TextView` hosts the APK-sourced provider aliases so font metrics follow the reference. The current final image comparison still has unresolved deterministic differences and is recorded as failed in `coverage-matrix.csv`.
