package com.kynstore.inventory.util

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Supported Languages in KYN Store:
 * - Tagalog (Filipino)
 * - English
 * - Bisaya (Cebuano)
 */
enum class AppLanguage(
    val code: String,
    val displayName: String,
    val flag: String,
    val nativeName: String
) {
    TAGALOG("tl", "Tagalog (Filipino)", "🇵🇭", "Tagalog"),
    ENGLISH("en", "English", "🇺🇸", "English"),
    BISAYA("ceb", "Bisaya (Cebuano)", "🇵🇭", "Bisaya");

    companion object {
        fun fromCode(code: String): AppLanguage {
            return entries.firstOrNull { it.code.equals(code, ignoreCase = true) } ?: TAGALOG
        }
    }
}

/**
 * Singleton Language Manager with persistent SharedPreferences storage.
 */
object AppLanguageManager {
    private const val PREFS_NAME = "kyn_store_prefs"
    private const val KEY_LANGUAGE = "app_language"

    private val _currentLanguage = MutableStateFlow(AppLanguage.TAGALOG)
    val currentLanguage: StateFlow<AppLanguage> = _currentLanguage.asStateFlow()

    private var isInitialized = false

    fun init(context: Context) {
        if (!isInitialized) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val savedCode = prefs.getString(KEY_LANGUAGE, AppLanguage.TAGALOG.code) ?: AppLanguage.TAGALOG.code
            _currentLanguage.value = AppLanguage.fromCode(savedCode)
            isInitialized = true
        }
    }

    fun setLanguage(context: Context, language: AppLanguage) {
        _currentLanguage.value = language
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_LANGUAGE, language.code).apply()
    }

    /**
     * Standardized "Add" / "Dagdag" button label across all components
     */
    fun getAddLabel(language: AppLanguage): String {
        return when (language) {
            AppLanguage.ENGLISH -> "Add"
            AppLanguage.TAGALOG, AppLanguage.BISAYA -> "Dagdag"
        }
    }
}

@Composable
fun rememberAppLanguage(): AppLanguage {
    val lang by AppLanguageManager.currentLanguage.collectAsState()
    return lang
}
