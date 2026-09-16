package com.example.util

import android.content.Context
import android.content.SharedPreferences
import com.example.model.DeveloperSettings
import com.example.model.PageMarginOption
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
      putString(KEY_PAGE_MARGIN, newSettings.pageMargin.name)
      putBoolean(KEY_AUTO_CROP, newSettings.autoCropOnImport)
      putBoolean(KEY_SIMPLIFIED_MODE, newSettings.simplifiedMode)
      putString(KEY_SAVE_PATH, newSettings.defaultSavePath)
      putBoolean(KEY_HIGH_CONTRAST, newSettings.highContrastMode)
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

    val marginName = prefs.getString(KEY_PAGE_MARGIN, PageMarginOption.BORDERED.name)
    val margin = try {
      PageMarginOption.valueOf(marginName ?: PageMarginOption.BORDERED.name)
    } catch (_: Exception) {
      PageMarginOption.BORDERED
    }

    val autoCrop = prefs.getBoolean(KEY_AUTO_CROP, false)
    val simplified = prefs.getBoolean(KEY_SIMPLIFIED_MODE, false)
    val savePath = prefs.getString(KEY_SAVE_PATH, DEFAULT_SAVE_FOLDER) ?: DEFAULT_SAVE_FOLDER
    val highContrast = prefs.getBoolean(KEY_HIGH_CONTRAST, false)

    return DeveloperSettings(
      defaultQuality = quality,
      pageMargin = margin,
      autoCropOnImport = autoCrop,
      simplifiedMode = simplified,
      defaultSavePath = savePath,
      highContrastMode = highContrast
    )
  }

  companion object {
    private const val PREFS_NAME = "pdf_maker_prefs"
    private const val KEY_DEFAULT_QUALITY = "pref_default_quality"
    private const val KEY_PAGE_MARGIN = "pref_page_margin"
    private const val KEY_AUTO_CROP = "pref_auto_crop"
    private const val KEY_SIMPLIFIED_MODE = "pref_simplified_mode"
    private const val KEY_SAVE_PATH = "pref_save_path"
    private const val KEY_HIGH_CONTRAST = "pref_high_contrast"
    const val DEFAULT_SAVE_FOLDER = "PDF documents(pdf_maker)"
  }
}
