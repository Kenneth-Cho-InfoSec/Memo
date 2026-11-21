package dev.arcturus.memo.data

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class KeepSettingsStoreInstrumentedTest {
    @Test
    fun everyPreferenceSurvivesStoreReload() {
        val testContext = InstrumentationRegistry.getInstrumentation().context
        val store = KeepSettingsStore(testContext)
        val expected = KeepSettings(
            addNewItemsToBottom = false,
            moveCheckedItemsToBottom = false,
            displayRichLinkPreviews = false,
            themeMode = ThemeMode.Dark,
            createTextNotesByDefault = true,
            enableSharing = false,
            gridView = false,
            sortMode = "Title",
        )

        try {
            store.save(expected)
            assertEquals(expected, store.load())
        } finally {
            testContext.getSharedPreferences("keep_settings", 0).edit().clear().commit()
        }
    }
}
