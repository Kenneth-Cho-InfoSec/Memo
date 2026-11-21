package dev.arcturus.memo.ui.theme

import androidx.compose.ui.graphics.Color
import dev.arcturus.memo.R

data class KeepColors(
    val background: Color,
    val drawerBackground: Color,
    val drawerScrimAlpha: Float,
    val settingsBackground: Color,
    val foreground: Color,
    val secondaryForeground: Color,
    val searchSurface: Color,
    val fabSurface: Color,
    val fabForeground: Color,
    val switchTrackOn: Color,
    val switchThumbOn: Color,
    val switchTrackOff: Color,
    val switchThumbOff: Color,
    val divider: Color,
)

val KeepLightColors = KeepColors(
    background = Color(0xFFededf6),
    drawerBackground = Color(0xFFededf6),
    drawerScrimAlpha = 0.32f,
    settingsBackground = Color(0xFFf0f4f9),
    foreground = Color(0xFF1F1F1F),
    secondaryForeground = Color(0xFF444746),
    searchSurface = Color(0xFFfaf8fe),
    fabSurface = Color(0xFFb9cbff),
    fabForeground = Color(0xFF18366c),
    switchTrackOn = Color(0xFF0b57d0),
    switchThumbOn = Color.White,
    switchTrackOff = Color(0xFFDDE3EA),
    switchThumbOff = Color(0xFF747775),
    divider = Color(0xFFdadce0),
)

val KeepDarkColors = KeepColors(
    background = Color(0xFF000000),
    drawerBackground = Color(0xFF000000),
    drawerScrimAlpha = 0.68f,
    settingsBackground = Color(0xFF000000),
    foreground = Color(0xFFe3e3e3),
    secondaryForeground = Color(0xFFC4C7C5),
    searchSurface = Color(0xFF121212),
    fabSurface = Color(0xFF43547a),
    fabForeground = Color(0xFFd5defd),
    switchTrackOn = Color(0xFFA8C7FA),
    switchThumbOn = Color(0xFF062E6F),
    switchTrackOff = Color(0xFF333537),
    switchThumbOff = Color(0xFF8E918F),
    divider = Color(0xFF3C4043),
)

// Most options use the supplied APK's colors; the dark default is lifted slightly from black.
data class NoteBackgroundColors(val light: Color, val dark: Color)

val KeepNoteBackgrounds = mapOf(
    "Default" to NoteBackgroundColors(Color(0xFFFFFFFF), Color(0xFF000000)),
    "Coral" to NoteBackgroundColors(Color(0xFFfaafa8), Color(0xFF77172e)),
    "Peach" to NoteBackgroundColors(Color(0xFFf39f76), Color(0xFF692b17)),
    "Sand" to NoteBackgroundColors(Color(0xFFfff8b8), Color(0xFF7c4a03)),
    "Mint" to NoteBackgroundColors(Color(0xFFe2f6d3), Color(0xFF264d3b)),
    "Sage" to NoteBackgroundColors(Color(0xFFb4ddd3), Color(0xFF0c625d)),
    "Fog" to NoteBackgroundColors(Color(0xFFd4e4ed), Color(0xFF256377)),
)

data class KeepNoteBackgroundAsset(
    val name: String,
    val day: Int,
    val night: Int,
    val thumbnailDay: Int,
    val thumbnailNight: Int,
) {
    val selectionKey: String get() = "Background:$name"
}

val KeepNoteBackgroundAssets = listOf(
    KeepNoteBackgroundAsset("Groceries", R.drawable.keep_background_groceries_day, R.drawable.keep_background_groceries_night, R.drawable.keep_background_groceries_thumb_day, R.drawable.keep_background_groceries_thumb_night),
    KeepNoteBackgroundAsset("Food", R.drawable.keep_background_food_day, R.drawable.keep_background_food_night, R.drawable.keep_background_food_thumb_day, R.drawable.keep_background_food_thumb_night),
    KeepNoteBackgroundAsset("Music", R.drawable.keep_background_music_day, R.drawable.keep_background_music_night, R.drawable.keep_background_music_thumb_day, R.drawable.keep_background_music_thumb_night),
    KeepNoteBackgroundAsset("Places", R.drawable.keep_background_places_day, R.drawable.keep_background_places_night, R.drawable.keep_background_places_thumb_day, R.drawable.keep_background_places_thumb_night),
    KeepNoteBackgroundAsset("Celebration", R.drawable.keep_background_celebration_day, R.drawable.keep_background_celebration_night, R.drawable.keep_background_celebration_thumb_day, R.drawable.keep_background_celebration_thumb_night),
    KeepNoteBackgroundAsset("Notes", R.drawable.keep_background_notes_day, R.drawable.keep_background_notes_night, R.drawable.keep_background_notes_thumb_day, R.drawable.keep_background_notes_thumb_night),
    KeepNoteBackgroundAsset("Recipes", R.drawable.keep_background_recipes_day, R.drawable.keep_background_recipes_night, R.drawable.keep_background_recipes_thumb_day, R.drawable.keep_background_recipes_thumb_night),
    KeepNoteBackgroundAsset("Travel", R.drawable.keep_background_travel_day, R.drawable.keep_background_travel_night, R.drawable.keep_background_travel_thumb_day, R.drawable.keep_background_travel_thumb_night),
    KeepNoteBackgroundAsset("Video", R.drawable.keep_background_video_day, R.drawable.keep_background_video_night, R.drawable.keep_background_video_thumb_day, R.drawable.keep_background_video_thumb_night),
)
