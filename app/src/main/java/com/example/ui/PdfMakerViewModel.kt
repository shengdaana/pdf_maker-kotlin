package com.example.ui

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.model.AppScreen
import com.example.model.CropRect
import com.example.model.DeveloperSettings
import com.example.model.GeneratedPdf
import com.example.model.ImagePage
import com.example.model.PdfQuality
import com.example.util.ImageUtils
import com.example.util.PdfGenerator
import com.example.util.SettingsManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PdfMakerViewModel(application: Application) : AndroidViewModel(application) {

  private val settingsManager = SettingsManager(application)

  private val _screen = MutableStateFlow<AppScreen>(AppScreen.Home)
  val screen: StateFlow<AppScreen> = _screen.asStateFlow()

  private val _pages = MutableStateFlow<List<ImagePage>>(emptyList())
  val pages: StateFlow<List<ImagePage>> = _pages.asStateFlow()

  val settings: StateFlow<DeveloperSettings> = settingsManager.settings

  private val _isGenerating = MutableStateFlow(false)
  val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

  private val _generationProgress = MutableStateFlow<Pair<Int, Int>?>(null)
  val generationProgress: StateFlow<Pair<Int, Int>?> = _generationProgress.asStateFlow()

  private val _lastGeneratedPdf = MutableStateFlow<GeneratedPdf?>(null)
  val lastGeneratedPdf: StateFlow<GeneratedPdf?> = _lastGeneratedPdf.asStateFlow()

  private val _recentPdfs = MutableStateFlow<List<GeneratedPdf>>(emptyList())
  val recentPdfs: StateFlow<List<GeneratedPdf>> = _recentPdfs.asStateFlow()

  private val _statusMessage = MutableStateFlow<String?>(null)
  val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

  private val _showQualityDialog = MutableStateFlow(false)
  val showQualityDialog: StateFlow<Boolean> = _showQualityDialog.asStateFlow()

  private val _showRenameDialog = MutableStateFlow(false)
  val showRenameDialog: StateFlow<Boolean> = _showRenameDialog.asStateFlow()

  init {
    refreshRecentPdfs()
  }

  fun refreshRecentPdfs() {
    val currentSettings = settings.value
    viewModelScope.launch {
      val recents = PdfGenerator.listRecentPdfs(
        getApplication(),
        currentSettings.defaultSavePath
      )
      _recentPdfs.value = recents
    }
  }

  fun navigateTo(newScreen: AppScreen) {
    _screen.value = newScreen
  }

  fun addImages(uris: List<Uri>) {
    if (uris.isEmpty()) return
    val currentList = _pages.value.toMutableList()
    val autoTrimDefault = settings.value.autoCropOnImport
    uris.forEach { uri ->
      currentList.add(
        ImagePage(
          uri = uri,
          autoTrimApplied = autoTrimDefault
        )
      )
    }
    _pages.value = currentList
    _screen.value = AppScreen.PreviewBuild
  }

  fun movePageUp(index: Int) {
    if (index <= 0 || index >= _pages.value.size) return
    val list = _pages.value.toMutableList()
    val item = list.removeAt(index)
    list.add(index - 1, item)
    _pages.value = list
  }

  fun movePageDown(index: Int) {
    if (index < 0 || index >= _pages.value.size - 1) return
    val list = _pages.value.toMutableList()
    val item = list.removeAt(index)
    list.add(index + 1, item)
    _pages.value = list
  }

  fun removePage(index: Int) {
    if (index < 0 || index >= _pages.value.size) return
    val list = _pages.value.toMutableList()
    list.removeAt(index)
    _pages.value = list
    if (list.isEmpty()) {
      _screen.value = AppScreen.Home
    }
  }

  fun clearAllPages() {
    _pages.value = emptyList()
    _screen.value = AppScreen.Home
  }

  fun updatePageCrop(
    pageId: String,
    rotationDegrees: Int,
    cropRect: CropRect?,
    cropAspectRatio: Float?,
    autoTrim: Boolean
  ) {
    val list = _pages.value.map { page ->
      if (page.id == pageId) {
        page.copy(
          rotationDegrees = rotationDegrees,
          cropRect = cropRect,
          cropAspectRatio = cropAspectRatio,
          autoTrimApplied = autoTrim,
          customBitmapCacheKey = System.currentTimeMillis()
        )
      } else {
        page
      }
    }
    _pages.value = list
  }

  fun mergePages(firstIndex: Int) {
    if (firstIndex < 0 || firstIndex >= _pages.value.size - 1) return
    viewModelScope.launch {
      val page1 = _pages.value[firstIndex]
      val page2 = _pages.value[firstIndex + 1]
      val mergedFile = ImageUtils.mergeTwoImages(getApplication(), page1, page2)
      if (mergedFile != null) {
        val mergedPage = ImagePage(
          uri = Uri.fromFile(mergedFile),
          isMerged = true,
          originalPages = listOf(page1, page2)
        )
        val list = _pages.value.toMutableList()
        list.removeAt(firstIndex + 1)
        list[firstIndex] = mergedPage
        _pages.value = list
      }
    }
  }

  fun unmergePage(index: Int) {
    if (index < 0 || index >= _pages.value.size) return
    val page = _pages.value[index]
    val originals = page.originalPages ?: return
    val list = _pages.value.toMutableList()
    list.removeAt(index)
    list.addAll(index, originals)
    _pages.value = list
  }

  fun onGeneratePdfClicked() {
    val currentSetting = settings.value.defaultQuality
    if (currentSetting == PdfQuality.ALWAYS_ASK) {
      _showQualityDialog.value = true
    } else {
      executePdfGeneration(currentSetting)
    }
  }

  fun dismissQualityDialog() {
    _showQualityDialog.value = false
  }

  fun onQualitySelected(quality: PdfQuality) {
    _showQualityDialog.value = false
    executePdfGeneration(quality)
  }

  private fun executePdfGeneration(quality: PdfQuality) {
    if (_pages.value.isEmpty()) return
    _isGenerating.value = true
    _generationProgress.value = Pair(1, _pages.value.size)

    viewModelScope.launch {
      val result = PdfGenerator.createPdf(
        context = getApplication(),
        pages = _pages.value,
        settings = settings.value,
        selectedQuality = quality,
        onProgress = { current, total ->
          _generationProgress.value = Pair(current, total)
        }
      )

      _isGenerating.value = false
      _generationProgress.value = null

      result.onSuccess { pdf ->
        _lastGeneratedPdf.value = pdf
        _screen.value = AppScreen.OutputFinal
        refreshRecentPdfs()
      }.onFailure { err ->
        _statusMessage.value = "Failed to create PDF: ${err.localizedMessage ?: "Unknown error"}"
      }
    }
  }

  fun showRenameDialog() {
    _showRenameDialog.value = true
  }

  fun dismissRenameDialog() {
    _showRenameDialog.value = false
  }

  fun renameCurrentPdf(newName: String) {
    val current = _lastGeneratedPdf.value ?: return
    _showRenameDialog.value = false
    renamePdf(current, newName)
  }

  fun renamePdf(pdf: GeneratedPdf, newName: String) {
    val renamed = PdfGenerator.renamePdf(getApplication(), pdf, newName)
    if (renamed != null) {
      if (_lastGeneratedPdf.value?.file?.absolutePath == pdf.file.absolutePath) {
        _lastGeneratedPdf.value = renamed
      }
      refreshRecentPdfs()
      _statusMessage.value = "PDF renamed to ${renamed.fileName}"
    } else {
      _statusMessage.value = "Failed to rename file"
    }
  }

  fun deletePdf(pdf: GeneratedPdf) {
    viewModelScope.launch {
      val success = PdfGenerator.deletePdf(pdf)
      if (success) {
        if (_lastGeneratedPdf.value?.file?.absolutePath == pdf.file.absolutePath) {
          _lastGeneratedPdf.value = null
        }
        refreshRecentPdfs()
        _statusMessage.value = "Deleted ${pdf.fileName}"
      } else {
        _statusMessage.value = "Failed to delete file"
      }
    }
  }

  fun sharePdf(context: Context, pdf: GeneratedPdf? = _lastGeneratedPdf.value) {
    if (pdf != null) {
      PdfGenerator.sharePdf(context, pdf)
    }
  }

  fun openPdf(context: Context, pdf: GeneratedPdf? = _lastGeneratedPdf.value) {
    if (pdf != null) {
      PdfGenerator.openPdf(context, pdf)
    }
  }

  fun updateSettings(newSettings: DeveloperSettings) {
    settingsManager.updateSettings(newSettings)
    refreshRecentPdfs()
  }

  fun clearStatusMessage() {
    _statusMessage.value = null
  }
}
