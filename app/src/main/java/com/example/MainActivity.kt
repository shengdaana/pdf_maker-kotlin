package com.example

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.model.AppScreen
import com.example.ui.PdfMakerViewModel
import com.example.ui.components.GeneratingProgressDialog
import com.example.ui.components.QualitySelectionDialog
import com.example.ui.components.RenamePdfDialog
import com.example.ui.screens.CropEditorScreen
import com.example.ui.screens.GeneratedPdfsScreen
import com.example.ui.screens.HiddenSettingsScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.OutputFinalScreen
import com.example.ui.screens.PreviewBuildScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

  private val viewModel: PdfMakerViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    // Handle incoming shared images from Android gallery
    handleIncomingIntent(intent)

    setContent {
      val currentSettings by viewModel.settings.collectAsState()
      val currentScreen by viewModel.screen.collectAsState()
      val pages by viewModel.pages.collectAsState()
      val isGenerating by viewModel.isGenerating.collectAsState()
      val progress by viewModel.generationProgress.collectAsState()
      val lastPdf by viewModel.lastGeneratedPdf.collectAsState()
      val recentPdfs by viewModel.recentPdfs.collectAsState()
      val showQualityDialog by viewModel.showQualityDialog.collectAsState()
      val showRenameDialog by viewModel.showRenameDialog.collectAsState()
      val statusMessage by viewModel.statusMessage.collectAsState()

      val snackbarHostState = remember { SnackbarHostState() }
      val previewListState = rememberLazyListState()
      val context = LocalContext.current

      // Reset scroll to top when starting a fresh list of pages
      LaunchedEffect(pages.isEmpty()) {
        if (pages.isEmpty()) {
          previewListState.scrollToItem(0)
        }
      }

      LaunchedEffect(statusMessage) {
        statusMessage?.let { msg ->
          snackbarHostState.showSnackbar(msg)
          viewModel.clearStatusMessage()
        }
      }

      MyApplicationTheme(
        theme = currentSettings.theme
      ) {
        Scaffold(
          modifier = Modifier.fillMaxSize(),
          snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { innerPadding ->
          Box(
            modifier = Modifier
              .fillMaxSize()
              .padding(innerPadding)
          ) {
            when (val screen = currentScreen) {
              is AppScreen.Home -> {
                HomeScreen(
                  settings = currentSettings,
                  recentPdfs = recentPdfs,
                  onPhotosSelected = { uris -> viewModel.addImages(uris) },
                  onSeeGeneratedPdfs = {
                    viewModel.refreshRecentPdfs()
                    viewModel.navigateTo(AppScreen.ViewGeneratedPdfs)
                  },
                  onOpenSettings = { viewModel.navigateTo(AppScreen.HiddenSettings) },
                  onOpenPdf = { pdf -> viewModel.openPdf(context, pdf) },
                  onSharePdf = { pdf -> viewModel.sharePdf(context, pdf) }
                )
              }

              is AppScreen.PreviewBuild -> {
                BackHandler {
                  viewModel.navigateTo(AppScreen.Home)
                }

                PreviewBuildScreen(
                  pages = pages,
                  settings = currentSettings,
                  listState = previewListState,
                  onAddMorePhotos = { uris -> viewModel.addImages(uris) },
                  onMoveUp = { index -> viewModel.movePageUp(index) },
                  onMoveDown = { index -> viewModel.movePageDown(index) },
                  onRemovePage = { index -> viewModel.removePage(index) },
                  onMergePages = { index -> viewModel.mergePages(index) },
                  onUnmergePage = { index -> viewModel.unmergePage(index) },
                  onTapCrop = { pageId -> viewModel.navigateTo(AppScreen.CropEditor(pageId)) },
                  onGeneratePdfClicked = { viewModel.onGeneratePdfClicked() },
                  onBackPressed = { viewModel.navigateTo(AppScreen.Home) }
                )
              }

              is AppScreen.CropEditor -> {
                BackHandler {
                  viewModel.navigateTo(AppScreen.PreviewBuild)
                }

                val editingPage = pages.find { it.id == screen.pageId }
                if (editingPage != null) {
                  CropEditorScreen(
                    page = editingPage,
                    language = currentSettings.language,
                    onSave = { rotation, cropRect, cropRatio, autoTrim ->
                      viewModel.updatePageCrop(
                        pageId = editingPage.id,
                        rotationDegrees = rotation,
                        cropRect = cropRect,
                        cropAspectRatio = cropRatio,
                        autoTrim = autoTrim
                      )
                      viewModel.navigateTo(AppScreen.PreviewBuild)
                    },
                    onCancel = { viewModel.navigateTo(AppScreen.PreviewBuild) }
                  )
                } else {
                  viewModel.navigateTo(AppScreen.PreviewBuild)
                }
              }

              is AppScreen.OutputFinal -> {
                BackHandler {
                  viewModel.navigateTo(AppScreen.Home)
                }

                if (lastPdf != null) {
                  OutputFinalScreen(
                    pdf = lastPdf!!,
                    settings = currentSettings,
                    onOpenPdf = { viewModel.openPdf(context, lastPdf) },
                    onSharePdf = { viewModel.sharePdf(context, lastPdf) },
                    onRenameClicked = { viewModel.showRenameDialog() },
                    onCreateAnother = {
                      viewModel.clearAllPages()
                      viewModel.navigateTo(AppScreen.Home)
                    }
                  )
                } else {
                  viewModel.navigateTo(AppScreen.Home)
                }
              }

              is AppScreen.HiddenSettings -> {
                BackHandler {
                  viewModel.navigateTo(AppScreen.Home)
                }

                HiddenSettingsScreen(
                  currentSettings = currentSettings,
                  onUpdateSettings = { newSettings -> viewModel.updateSettings(newSettings) },
                  onBack = { viewModel.navigateTo(AppScreen.Home) }
                )
              }

              is AppScreen.ViewGeneratedPdfs -> {
                BackHandler {
                  viewModel.navigateTo(AppScreen.Home)
                }

                GeneratedPdfsScreen(
                  pdfs = recentPdfs,
                  settings = currentSettings,
                  onOpenPdf = { pdf -> viewModel.openPdf(context, pdf) },
                  onSharePdf = { pdf -> viewModel.sharePdf(context, pdf) },
                  onRenamePdf = { pdf, newName -> viewModel.renamePdf(pdf, newName) },
                  onDeletePdf = { pdf -> viewModel.deletePdf(pdf) },
                  onBackPressed = { viewModel.navigateTo(AppScreen.Home) },
                  onSelectPhotos = { viewModel.navigateTo(AppScreen.Home) }
                )
              }
            }

            // Quality Selection Dialog
            if (showQualityDialog) {
              QualitySelectionDialog(
                onQualitySelected = { quality ->
                  viewModel.onQualitySelected(quality)
                },
                onDismiss = { viewModel.dismissQualityDialog() },
                isHighContrast = currentSettings.highContrastMode
              )
            }

            // Generating Progress Dialog
            if (isGenerating) {
              val (current, total) = progress ?: Pair(1, pages.size.coerceAtLeast(1))
              GeneratingProgressDialog(
                currentPage = current,
                totalPages = total
              )
            }

            // Rename PDF Dialog
            if (showRenameDialog && lastPdf != null) {
              RenamePdfDialog(
                initialName = lastPdf!!.fileName,
                onConfirm = { newName -> viewModel.renameCurrentPdf(newName) },
                onDismiss = { viewModel.dismissRenameDialog() }
              )
            }
          }
        }
      }
    }
  }

  override fun onNewIntent(intent: Intent) {
    super.onNewIntent(intent)
    setIntent(intent)
    handleIncomingIntent(intent)
  }

  /**
   * Primary Entry Point: Android Share Target Integration.
   * Handles incoming shared image(s) from Gallery (ACTION_SEND and ACTION_SEND_MULTIPLE)
   */
  private fun handleIncomingIntent(intent: Intent?) {
    if (intent == null) return
    val action = intent.action
    val type = intent.type

    if (type?.startsWith("image/") == true) {
      when (action) {
        Intent.ACTION_SEND -> {
          val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(Intent.EXTRA_STREAM, Uri::class.java)
          } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)
          }
          if (uri != null) {
            viewModel.addImages(listOf(uri))
          } else if (intent.clipData != null && intent.clipData!!.itemCount > 0) {
            val clipUri = intent.clipData!!.getItemAt(0).uri
            if (clipUri != null) viewModel.addImages(listOf(clipUri))
          }
        }

        Intent.ACTION_SEND_MULTIPLE -> {
          val uris = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM, Uri::class.java)
          } else {
            @Suppress("DEPRECATION")
            intent.getParcelableArrayListExtra<Uri>(Intent.EXTRA_STREAM)
          }
          if (!uris.isNullOrEmpty()) {
            viewModel.addImages(uris)
          } else if (intent.clipData != null) {
            val extracted = mutableListOf<Uri>()
            for (i in 0 until intent.clipData!!.itemCount) {
              val clipUri = intent.clipData!!.getItemAt(i).uri
              if (clipUri != null) extracted.add(clipUri)
            }
            if (extracted.isNotEmpty()) viewModel.addImages(extracted)
          }
        }
      }
    }
  }
}
