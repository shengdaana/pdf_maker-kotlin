package com.example.util

import android.content.Context
import android.content.SharedPreferences
import com.example.model.AppLanguage
import com.example.model.AppTheme
import com.example.model.DeveloperSettings
import com.example.model.PageLayoutMode
import com.example.model.PdfQuality
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsManager(context: Context) {
  private val prefs: SharedPreferences =
    context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

  private val _settings = MutableStateFlow(loadSettings())
  val settings: StateFlow<DeveloperSettings> = _settings.asStateFlow()

  fun updateSettings(newSettings: DeveloperSettings) {
    prefs.edit().apply {
      putString(KEY_DEFAULT_QUALITY, newSettings.defaultQuality.name)
      putString(KEY_PAGE_LAYOUT, newSettings.pageLayoutMode.name)
      putBoolean(KEY_SIMPLIFIED_MODE, newSettings.simplifiedMode)
      putBoolean(KEY_ENABLE_MERGE_PAGES, newSettings.enableMergePages)
      putString(KEY_SAVE_PATH, newSettings.defaultSavePath)
      putString(KEY_THEME, newSettings.theme.name)
      putString(KEY_LANGUAGE, newSettings.language.name)
      apply()
    }
    _settings.value = newSettings
  }

  private fun loadSettings(): DeveloperSettings {
    val qualityName = prefs.getString(KEY_DEFAULT_QUALITY, PdfQuality.ALWAYS_ASK.name)
    val quality = try {
      PdfQuality.valueOf(qualityName ?: PdfQuality.ALWAYS_ASK.name)
    } catch (_: Exception) {
      PdfQuality.ALWAYS_ASK
    }

    val layoutName = prefs.getString(KEY_PAGE_LAYOUT, PageLayoutMode.FREE_DYNAMIC.name)
    val layout = try {
      PageLayoutMode.valueOf(layoutName ?: PageLayoutMode.FREE_DYNAMIC.name)
    } catch (_: Exception) {
      PageLayoutMode.FREE_DYNAMIC
    }

    val themeName = prefs.getString(KEY_THEME, AppTheme.PURPLE.name)
    val theme = try {
      AppTheme.valueOf(themeName ?: AppTheme.PURPLE.name)
    } catch (_: Exception) {
      AppTheme.PURPLE
    }

    val langName = prefs.getString(KEY_LANGUAGE, AppLanguage.ENGLISH.name)
    val language = try {
      AppLanguage.valueOf(langName ?: AppLanguage.ENGLISH.name)
    } catch (_: Exception) {
      AppLanguage.ENGLISH
    }

    val simplified = prefs.getBoolean(KEY_SIMPLIFIED_MODE, false)
    val enableMerge = prefs.getBoolean(KEY_ENABLE_MERGE_PAGES, false)
    val savePath = prefs.getString(KEY_SAVE_PATH, DEFAULT_SAVE_FOLDER) ?: DEFAULT_SAVE_FOLDER

    return DeveloperSettings(
      defaultQuality = quality,
      pageLayoutMode = layout,
      pageMargin = layout,
      simplifiedMode = simplified,
      enableMergePages = enableMerge,
      defaultSavePath = savePath,
      theme = theme,
      language = language
    )
  }

  companion object {
    private const val PREFS_NAME = "pdf_maker_prefs"
    private const val KEY_DEFAULT_QUALITY = "pref_default_quality"
    private const val KEY_PAGE_LAYOUT = "pref_page_layout"
    private const val KEY_SIMPLIFIED_MODE = "pref_simplified_mode"
    private const val KEY_ENABLE_MERGE_PAGES = "pref_enable_merge_pages"
    private const val KEY_SAVE_PATH = "pref_save_path"
    private const val KEY_THEME = "pref_theme"
    private const val KEY_LANGUAGE = "pref_language"
    const val DEFAULT_SAVE_FOLDER = "PDF documents(pdf_maker)"
  }
}
