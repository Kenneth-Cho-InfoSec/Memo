package dev.arcturus.memo.data

import android.content.Context

enum class ThemeMode { System, Light, Dark }

data class KeepSettings(
    val addNewItemsToBottom: Boolean = true,
    val moveCheckedItemsToBottom: Boolean = true,
    val displayRichLinkPreviews: Boolean = true,
    val themeMode: ThemeMode = ThemeMode.System,
    val createTextNotesByDefault: Boolean = false,
    val enableSharing: Boolean = true,
    val gridView: Boolean = true,
    val sortMode: String = "Last edited",
)

class KeepSettingsStore(context: Context) {
    private val preferences = context.getSharedPreferences("keep_settings", Context.MODE_PRIVATE)

    fun load(): KeepSettings = KeepSettings(
        addNewItemsToBottom = preferences.getBoolean(KEY_ADD_BOTTOM, true),
        moveCheckedItemsToBottom = preferences.getBoolean(KEY_CHECKED_BOTTOM, true),
        displayRichLinkPreviews = preferences.getBoolean(KEY_RICH_LINKS, true),
        themeMode = ThemeMode.entries.firstOrNull {
            it.name == preferences.getString(KEY_THEME, ThemeMode.System.name)
        } ?: ThemeMode.System,
        createTextNotesByDefault = preferences.getBoolean(KEY_TEXT_DEFAULT, false),
        enableSharing = preferences.getBoolean(KEY_SHARING, true),
        gridView = preferences.getBoolean(KEY_GRID, true),
        sortMode = preferences.getString(KEY_SORT, "Last edited") ?: "Last edited",
    )

    fun save(settings: KeepSettings) {
        preferences.edit()
            .putBoolean(KEY_ADD_BOTTOM, settings.addNewItemsToBottom)
            .putBoolean(KEY_CHECKED_BOTTOM, settings.moveCheckedItemsToBottom)
            .putBoolean(KEY_RICH_LINKS, settings.displayRichLinkPreviews)
            .putString(KEY_THEME, settings.themeMode.name)
            .putBoolean(KEY_TEXT_DEFAULT, settings.createTextNotesByDefault)
            .putBoolean(KEY_SHARING, settings.enableSharing)
            .putBoolean(KEY_GRID, settings.gridView)
            .putString(KEY_SORT, settings.sortMode)
            .apply()
    }

    private companion object {
        const val KEY_ADD_BOTTOM = "add_new_items_to_bottom"
        const val KEY_CHECKED_BOTTOM = "move_checked_items_to_bottom"
        const val KEY_RICH_LINKS = "display_rich_link_previews"
        const val KEY_THEME = "theme_mode"
        const val KEY_TEXT_DEFAULT = "create_text_notes_by_default"
        const val KEY_SHARING = "enable_sharing"
        const val KEY_GRID = "grid_view"
        const val KEY_SORT = "sort_mode"
    }
}
