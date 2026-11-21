package dev.arcturus.memo.data

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class KeepOnboardingStoreInstrumentedTest {
    @Test
    fun welcomeCompletionSurvivesStoreReload() {
        val testContext = InstrumentationRegistry.getInstrumentation().targetContext
        val preferences = testContext.getSharedPreferences("keep_onboarding", 0)
        val firstStore = KeepOnboardingStore(testContext)

        try {
            preferences.edit().clear().commit()
            assertFalse(firstStore.hasSeenWelcome())

            firstStore.markWelcomeSeen()

            assertTrue(KeepOnboardingStore(testContext).hasSeenWelcome())
        } finally {
            preferences.edit().clear().commit()
        }
    }
}
