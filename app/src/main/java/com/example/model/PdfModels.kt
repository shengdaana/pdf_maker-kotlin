package com.example.model

import android.net.Uri
import java.io.File

/**
 * Represents a single page in the PDF document being composed.
 */
data class ImagePage(
  val id: String = java.util.UUID.randomUUID().toString(),
  val uri: Uri,
  val rotationDegrees: Int = 0, // 0, 90, 180, 270
  val cropAspectRatio: Float? = null, // null for original, or width/height ratio like 1f (square), 0.707f (A4 portrait)
  val autoTrimApplied: Boolean = false,
  val customBitmapCacheKey: Long = System.currentTimeMillis()
)

/**
 * Compression quality preset for PDF generation.
 */
enum class PdfQuality(val displayName: String, val description: String) {
  STANDARD(
    "Standard Quality",
    "Compressed for fast sharing on WhatsApp & email (Recommended)"
  ),
  ORIGINAL(
    "Original Quality",
    "Full image resolution with minimal compression"
  ),
  ALWAYS_ASK(
    "Always Ask",
    "Show quality choices every time you generate a PDF"
  )
}

/**
 * Page margin / photo aspect ratio options.
 */
enum class PageMarginOption(val displayName: String, val description: String) {
  BORDERED(
    "Bordered (Fit A4)",
    "Clean white margins around photos so no document edges are cut off"
  ),
  FULL_BLEED(
    "Full-Bleed (Fill Page)",
    "Photos expand to fill the entire A4 page with zero margins"
  )
}

/**
 * Hidden developer & elder-friendly configuration.
 */
data class DeveloperSettings(
  val defaultQuality: PdfQuality = PdfQuality.ALWAYS_ASK,
  val pageMargin: PageMarginOption = PageMarginOption.BORDERED,
  val autoCropOnImport: Boolean = false,
  val simplifiedMode: Boolean = false, // Hides reorder arrows for elder simplicity
  val defaultSavePath: String = "PDF documents(pdf_maker)",
  val highContrastMode: Boolean = false // High contrast borders, bigger buttons for elders
)

/**
 * Represents a successfully generated PDF file.
 */
data class GeneratedPdf(
  val file: File,
  val uri: Uri,
  val fileName: String,
  val sizeBytes: Long,
  val pageCount: Int,
  val timestamp: Long
)

/**
 * Navigation screen routes.
 */
sealed class AppScreen {
  data object Home : AppScreen()
  data object PreviewBuild : AppScreen()
  data class CropEditor(val pageId: String) : AppScreen()
  data object OutputFinal : AppScreen()
  data object HiddenSettings : AppScreen()
  data object ViewGeneratedPdfs : AppScreen()
}
