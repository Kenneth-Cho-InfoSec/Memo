package dev.arcturus.memo.data

import android.content.Context

class KeepOnboardingStore(context: Context) {
    private val preferences = context.applicationContext
        .getSharedPreferences("keep_onboarding", Context.MODE_PRIVATE)

    fun hasSeenWelcome(): Boolean = preferences.getBoolean(KEY_WELCOME_SEEN, false)

    fun markWelcomeSeen() {
        preferences.edit().putBoolean(KEY_WELCOME_SEEN, true).apply()
    }

    private companion object {
        const val KEY_WELCOME_SEEN = "welcome_seen"
    }
}
