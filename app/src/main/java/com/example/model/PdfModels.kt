package com.example.model

import android.net.Uri
import java.io.File

/**
 * Normalized crop bounds (values 0f to 1f relative to oriented photo).
 */
data class CropRect(
  val left: Float = 0f,
  val top: Float = 0f,
  val right: Float = 1f,
  val bottom: Float = 1f
) {
  val width: Float get() = (right - left).coerceAtLeast(0.01f)
  val height: Float get() = (bottom - top).coerceAtLeast(0.01f)
  val isFull: Boolean get() = left <= 0.005f && top <= 0.005f && right >= 0.995f && bottom >= 0.995f
}

/**
 * Represents a single page in the PDF document being composed.
 */
data class ImagePage(
  val id: String = java.util.UUID.randomUUID().toString(),
  val uri: Uri,
  val originalUri: Uri? = null,
  val rotationDegrees: Int = 0, // 0, 90, 180, 270
  val cropRect: CropRect? = null, // manual free crop area
  val cropAspectRatio: Float? = null, // null for original, or width/height ratio like 1f (square), 0.707f (A4 portrait)
  val isMerged: Boolean = false,
  val originalPages: List<ImagePage>? = null,
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
 * Page layout sizing mode:
 * 1. FREE_DYNAMIC: Page canvas matches each photo's native aspect ratio with zero white margins/borders.
 * 2. A4_STANDARD: Standard A4 page size with clean scaling/centering.
 */
enum class PageLayoutMode(val displayName: String, val description: String) {
  FREE_DYNAMIC(
    "Free / Dynamic (Native Aspect Ratio)",
    "Each PDF page matches the exact shape of your photo with zero white margins"
  ),
  A4_STANDARD(
    "A4 Standard",
    "Standard universal A4 document pages with photo scaled cleanly to fit"
  )
}

// Backward compatibility alias if needed
typealias PageMarginOption = PageLayoutMode

/**
 * Supported Visual Theme Presets:
 * 1. Purple (Modern violet)
 * 2. Light Green (Clean & fresh)
 * 3. Pink (Vibrant & warm)
 * 4. Cobalt Blue (Professional deep blue)
 * 5. High-Contrast Dark (OLED deep black with high contrast)
 */
enum class AppTheme(val displayName: String) {
  PURPLE("Purple"),
  DARK("Dark Mode"),
  LIGHT_GREEN("Light Green"),
  PINK("Pink"),
  COBALT_BLUE("Cobalt Blue"),
  HIGH_CONTRAST_DARK("High-Contrast Dark")
}

/**
 * Supported Languages:
 * 1. English
 * 2. Everyday Hindi (Devanagari)
 * 3. Hinglish (Colloquial Roman Hindi)
 */
enum class AppLanguage(val displayName: String) {
  ENGLISH("English"),
  HINDI("हिन्दी (Hindi)"),
  HINGLISH("Hinglish")
}

/**
 * Hidden developer & elder-friendly configuration.
 */
data class DeveloperSettings(
  val defaultQuality: PdfQuality = PdfQuality.ALWAYS_ASK,
  val pageLayoutMode: PageLayoutMode = PageLayoutMode.FREE_DYNAMIC,
  val pageMargin: PageMarginOption = PageLayoutMode.FREE_DYNAMIC,
  val simplifiedMode: Boolean = false, // Hides reorder arrows for elder simplicity
  val enableMergePages: Boolean = false, // Toggle for merge pages in simplified mode tab (default its off)
  val defaultSavePath: String = "PDF documents(pdf_maker)",
  val theme: AppTheme = AppTheme.PURPLE,
  val language: AppLanguage = AppLanguage.ENGLISH
) {
  val highContrastMode: Boolean get() = theme == AppTheme.HIGH_CONTRAST_DARK
}

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
