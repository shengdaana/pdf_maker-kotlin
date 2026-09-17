package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Rect
import android.net.Uri
import com.example.model.CropRect
import com.example.model.ImagePage
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.max
import kotlin.math.min

object ImageUtils {

  /**
   * Decodes a bitmap from a content Uri with bounds check to prevent OOM.
   * Target bounds accommodate standard ~1800-2048px or original high-res ~3500px.
   */
  fun decodeSampledBitmapFromUri(
    context: Context,
    uri: Uri,
    reqWidth: Int,
    reqHeight: Int
  ): Bitmap? {
    return try {
      val options = BitmapFactory.Options().apply {
        inJustDecodeBounds = true
      }
      context.contentResolver.openInputStream(uri)?.use { stream ->
        BitmapFactory.decodeStream(stream, null, options)
      }

      options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight)
      options.inJustDecodeBounds = false
      options.inPreferredConfig = Bitmap.Config.ARGB_8888

      context.contentResolver.openInputStream(uri)?.use { stream ->
        BitmapFactory.decodeStream(stream, null, options)
      }
    } catch (_: Exception) {
      null
    }
  }

  private fun calculateInSampleSize(
    options: BitmapFactory.Options,
    reqWidth: Int,
    reqHeight: Int
  ): Int {
    val (height: Int, width: Int) = options.outHeight to options.outWidth
    var inSampleSize = 1

    if (height > reqHeight || width > reqWidth) {
      val halfHeight: Int = height / 2
      val halfWidth: Int = width / 2

      while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
        inSampleSize *= 2
      }
    }
    return inSampleSize
  }

  /**
   * Downsamples a bitmap to a maximum bounding box preserving aspect ratio.
   * For Standard Quality: maxDimension ~1800-2040px.
   */
  fun downsampleBitmap(source: Bitmap, maxDimension: Int): Bitmap {
    val currentMax = maxOf(source.width, source.height)
    if (currentMax <= maxDimension) return source

    val scale = maxDimension.toFloat() / currentMax.toFloat()
    val targetW = (source.width * scale).toInt().coerceAtLeast(1)
    val targetH = (source.height * scale).toInt().coerceAtLeast(1)

    return Bitmap.createScaledBitmap(source, targetW, targetH, true)
  }

  /**
   * Applies user rotation, manual crop bounds, aspect ratio crop, and auto-trim if enabled.
   */
  fun processBitmap(
    source: Bitmap,
    rotationDegrees: Int,
    cropRect: CropRect?,
    targetAspectRatio: Float?, // width / height
    autoTrim: Boolean
  ): Bitmap {
    var result = source

    // 1. Apply user rotation if any
    if (rotationDegrees % 360 != 0) {
      val matrix = Matrix().apply { postRotate(rotationDegrees.toFloat()) }
      val rotated = Bitmap.createBitmap(
        result, 0, 0, result.width, result.height, matrix, true
      )
      if (rotated != result && result != source) {
        result.recycle()
      }
      result = rotated
    }

    // 2. Apply manual free crop bounds if user dragged handles
    if (cropRect != null && !cropRect.isFull) {
      val leftPx = (cropRect.left * result.width).toInt().coerceIn(0, result.width - 1)
      val topPx = (cropRect.top * result.height).toInt().coerceIn(0, result.height - 1)
      val rightPx = (cropRect.right * result.width).toInt().coerceIn(leftPx + 10, result.width)
      val bottomPx = (cropRect.bottom * result.height).toInt().coerceIn(topPx + 10, result.height)

      val cropW = rightPx - leftPx
      val cropH = bottomPx - topPx
      if (cropW > 10 && cropH > 10) {
        val cropped = Bitmap.createBitmap(result, leftPx, topPx, cropW, cropH)
        if (cropped != result && result != source) {
          result.recycle()
        }
        result = cropped
      }
    }

    // 3. Apply auto-trim with safety buffer margin if requested
    if (autoTrim) {
      val trimmed = autoTrimBitmapWithBuffer(result)
      if (trimmed != result) {
        if (result != source) result.recycle()
        result = trimmed
      }
    }

    // 4. Apply target aspect ratio crop if explicitly requested (e.g., A4 or 1:1)
    if (targetAspectRatio != null && targetAspectRatio > 0f) {
      val currentAspect = result.width.toFloat() / result.height.toFloat()
      val rect: Rect = if (currentAspect > targetAspectRatio) {
        // Wider than desired -> trim left and right
        val newWidth = (result.height * targetAspectRatio).toInt().coerceAtMost(result.width)
        val xOffset = (result.width - newWidth) / 2
        Rect(xOffset, 0, xOffset + newWidth, result.height)
      } else {
        // Taller than desired -> trim top and bottom
        val newHeight = (result.width / targetAspectRatio).toInt().coerceAtMost(result.height)
        val yOffset = (result.height - newHeight) / 2
        Rect(0, yOffset, result.width, yOffset + newHeight)
      }

      val cropped = Bitmap.createBitmap(
        result,
        rect.left,
        rect.top,
        rect.width(),
        rect.height()
      )
      if (cropped != result && result != source) {
        result.recycle()
      }
      result = cropped
    }

    return result
  }

  /**
   * Detects dark borders or scanner shadows around document photos and trims them,
   * preserving a generous safety buffer margin (~4-6%) so document text and stamps are NEVER cut off.
   */
  private fun autoTrimBitmapWithBuffer(bitmap: Bitmap): Bitmap {
    val width = bitmap.width
    val height = bitmap.height
    if (width < 60 || height < 60) return bitmap

    val sampleStep = max(1, min(width, height) / 200)
    var top = 0
    var bottom = height - 1
    var left = 0
    var right = width - 1

    val darkThreshold = 40 // Conservative threshold for dark shadow/background

    // Scan top
    topLoop@ for (y in 0 until (height / 5) step sampleStep) {
      var darkCount = 0
      var total = 0
      for (x in 0 until width step sampleStep * 2) {
        val pixel = bitmap.getPixel(x, y)
        val lum = (Color.red(pixel) + Color.green(pixel) + Color.blue(pixel)) / 3
        if (lum < darkThreshold) darkCount++
        total++
      }
      if (total > 0 && (darkCount.toFloat() / total) > 0.65f) {
        top = y + sampleStep
      } else {
        break@topLoop
      }
    }

    // Scan bottom
    bottomLoop@ for (y in height - 1 downTo (height * 4 / 5) step sampleStep) {
      var darkCount = 0
      var total = 0
      for (x in 0 until width step sampleStep * 2) {
        val pixel = bitmap.getPixel(x, y)
        val lum = (Color.red(pixel) + Color.green(pixel) + Color.blue(pixel)) / 3
        if (lum < darkThreshold) darkCount++
        total++
      }
      if (total > 0 && (darkCount.toFloat() / total) > 0.65f) {
        bottom = y - sampleStep
      } else {
        break@bottomLoop
      }
    }

    // Scan left
    leftLoop@ for (x in 0 until (width / 5) step sampleStep) {
      var darkCount = 0
      var total = 0
      for (y in 0 until height step sampleStep * 2) {
        val pixel = bitmap.getPixel(x, y)
        val lum = (Color.red(pixel) + Color.green(pixel) + Color.blue(pixel)) / 3
        if (lum < darkThreshold) darkCount++
        total++
      }
      if (total > 0 && (darkCount.toFloat() / total) > 0.65f) {
        left = x + sampleStep
      } else {
        break@leftLoop
      }
    }

    // Scan right
    rightLoop@ for (x in width - 1 downTo (width * 4 / 5) step sampleStep) {
      var darkCount = 0
      var total = 0
      for (y in 0 until height step sampleStep * 2) {
        val pixel = bitmap.getPixel(x, y)
        val lum = (Color.red(pixel) + Color.green(pixel) + Color.blue(pixel)) / 3
        if (lum < darkThreshold) darkCount++
        total++
      }
      if (total > 0 && (darkCount.toFloat() / total) > 0.65f) {
        right = x - sampleStep
      } else {
        break@rightLoop
      }
    }

    // APPLY SAFETY BUFFER MARGIN: Expand outwards by 5% of dimensions to never clip document content
    val hBuffer = (width * 0.05f).toInt()
    val vBuffer = (height * 0.05f).toInt()

    val safeLeft = (left - hBuffer).coerceAtLeast(0)
    val safeTop = (top - vBuffer).coerceAtLeast(0)
    val safeRight = (right + hBuffer).coerceAtMost(width - 1)
    val safeBottom = (bottom + vBuffer).coerceAtMost(height - 1)

    val trimmedWidth = safeRight - safeLeft + 1
    val trimmedHeight = safeBottom - safeTop + 1

    if (trimmedWidth > width * 0.75f && trimmedHeight > height * 0.75f &&
      (safeLeft > 0 || safeTop > 0 || safeRight < width - 1 || safeBottom < height - 1)
    ) {
      return Bitmap.createBitmap(bitmap, safeLeft, safeTop, trimmedWidth, trimmedHeight)
    }

    return bitmap
  }

  /**
   * Helper to decode and apply transforms (rotation, crop, auto-trim) to an ImagePage.
   */
  fun getProcessedPageBitmap(
    context: Context,
    page: ImagePage,
    reqWidth: Int = 1600,
    reqHeight: Int = 2200
  ): Bitmap? {
    val decoded = decodeSampledBitmapFromUri(context, page.uri, reqWidth, reqHeight) ?: return null
    return processBitmap(
      source = decoded,
      rotationDegrees = page.rotationDegrees,
      cropRect = page.cropRect,
      targetAspectRatio = page.cropAspectRatio,
      autoTrim = page.autoTrimApplied
    )
  }

  /**
   * Merges two ImagePages into a single dual-photo composite image.
   * Stacks them vertically with clean margins and divider, saving the result to a cache file.
   */
  suspend fun mergeTwoImages(
    context: Context,
    page1: ImagePage,
    page2: ImagePage
  ): File? = withContext(Dispatchers.IO) {
    try {
      val bmp1 = getProcessedPageBitmap(context, page1, reqWidth = 1600, reqHeight = 2200) ?: return@withContext null
      val bmp2 = getProcessedPageBitmap(context, page2, reqWidth = 1600, reqHeight = 2200) ?: return@withContext null

      // Target canvas width for crisp document rendering
      val targetWidth = 1600
      val scale1 = targetWidth.toFloat() / bmp1.width
      val h1 = (bmp1.height * scale1).toInt().coerceAtLeast(100)

      val scale2 = targetWidth.toFloat() / bmp2.width
      val h2 = (bmp2.height * scale2).toInt().coerceAtLeast(100)

      val gap = 24
      val combinedHeight = h1 + gap + h2

      val combinedBitmap = Bitmap.createBitmap(targetWidth, combinedHeight, Bitmap.Config.ARGB_8888)
      val canvas = Canvas(combinedBitmap)
      canvas.drawColor(Color.WHITE)

      val paint = Paint(Paint.FILTER_BITMAP_FLAG or Paint.ANTI_ALIAS_FLAG)

      // Draw top image
      val rect1 = Rect(0, 0, targetWidth, h1)
      canvas.drawBitmap(bmp1, null, rect1, paint)

      // Subtle divider line
      val linePaint = Paint().apply {
        color = Color.LTGRAY
        strokeWidth = 3f
      }
      canvas.drawLine(40f, h1 + (gap / 2f), targetWidth - 40f, h1 + (gap / 2f), linePaint)

      // Draw bottom image
      val rect2 = Rect(0, h1 + gap, targetWidth, combinedHeight)
      canvas.drawBitmap(bmp2, null, rect2, paint)

      val dir = File(context.cacheDir, "merged_pages").apply { mkdirs() }
      val file = File(dir, "merged_${System.currentTimeMillis()}.jpg")
      FileOutputStream(file).use { out ->
        combinedBitmap.compress(Bitmap.CompressFormat.JPEG, 88, out)
      }

      bmp1.recycle()
      bmp2.recycle()
      combinedBitmap.recycle()

      file
    } catch (_: Exception) {
      null
    }
  }
}
