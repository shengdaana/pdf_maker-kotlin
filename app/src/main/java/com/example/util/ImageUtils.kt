package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.net.Uri
import androidx.core.graphics.scale
import java.io.InputStream
import kotlin.math.max
import kotlin.math.min

object ImageUtils {

  /**
   * Decodes a bitmap from a content Uri with bounds check to prevent OOM.
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
   * Applies user rotation, aspect ratio crop, and auto-trim if enabled.
   */
  fun processBitmap(
    source: Bitmap,
    rotationDegrees: Int,
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

    // 2. Apply auto-trim if requested (trims dark borders or uneven scanner edges)
    if (autoTrim) {
      val trimmed = autoTrimBitmap(result)
      if (trimmed != result) {
        if (result != source) result.recycle()
        result = trimmed
      }
    }

    // 3. Apply target aspect ratio crop if requested
    if (targetAspectRatio != null && targetAspectRatio > 0f) {
      val currentAspect = result.width.toFloat() / result.height.toFloat()
      val cropRect: Rect = if (currentAspect > targetAspectRatio) {
        // Image is wider than desired ratio -> crop horizontal edges
        val newWidth = (result.height * targetAspectRatio).toInt().coerceAtMost(result.width)
        val xOffset = (result.width - newWidth) / 2
        Rect(xOffset, 0, xOffset + newWidth, result.height)
      } else {
        // Image is taller than desired ratio -> crop vertical edges
        val newHeight = (result.width / targetAspectRatio).toInt().coerceAtMost(result.height)
        val yOffset = (result.height - newHeight) / 2
        Rect(0, yOffset, result.width, yOffset + newHeight)
      }

      val cropped = Bitmap.createBitmap(
        result,
        cropRect.left,
        cropRect.top,
        cropRect.width(),
        cropRect.height()
      )
      if (cropped != result && result != source) {
        result.recycle()
      }
      result = cropped
    }

    return result
  }

  /**
   * Detects dark borders or scanner shadows around document photos and crops them.
   */
  private fun autoTrimBitmap(bitmap: Bitmap): Bitmap {
    val width = bitmap.width
    val height = bitmap.height
    if (width < 50 || height < 50) return bitmap

    val sampleStep = max(1, min(width, height) / 200)
    var top = 0
    var bottom = height - 1
    var left = 0
    var right = width - 1

    val darkThreshold = 45 // Lum threshold for dark borders

    // Scan top
    topLoop@ for (y in 0 until height / 4 step sampleStep) {
      var darkCount = 0
      var total = 0
      for (x in 0 until width step sampleStep * 2) {
        val pixel = bitmap.getPixel(x, y)
        val lum = (Color.red(pixel) + Color.green(pixel) + Color.blue(pixel)) / 3
        if (lum < darkThreshold) darkCount++
        total++
      }
      if (total > 0 && (darkCount.toFloat() / total) > 0.6f) {
        top = y + sampleStep
      } else {
        break@topLoop
      }
    }

    // Scan bottom
    bottomLoop@ for (y in height - 1 downTo (height * 3 / 4) step sampleStep) {
      var darkCount = 0
      var total = 0
      for (x in 0 until width step sampleStep * 2) {
        val pixel = bitmap.getPixel(x, y)
        val lum = (Color.red(pixel) + Color.green(pixel) + Color.blue(pixel)) / 3
        if (lum < darkThreshold) darkCount++
        total++
      }
      if (total > 0 && (darkCount.toFloat() / total) > 0.6f) {
        bottom = y - sampleStep
      } else {
        break@bottomLoop
      }
    }

    // Scan left
    leftLoop@ for (x in 0 until width / 4 step sampleStep) {
      var darkCount = 0
      var total = 0
      for (y in 0 until height step sampleStep * 2) {
        val pixel = bitmap.getPixel(x, y)
        val lum = (Color.red(pixel) + Color.green(pixel) + Color.blue(pixel)) / 3
        if (lum < darkThreshold) darkCount++
        total++
      }
      if (total > 0 && (darkCount.toFloat() / total) > 0.6f) {
        left = x + sampleStep
      } else {
        break@leftLoop
      }
    }

    // Scan right
    rightLoop@ for (x in width - 1 downTo (width * 3 / 4) step sampleStep) {
      var darkCount = 0
      var total = 0
      for (y in 0 until height step sampleStep * 2) {
        val pixel = bitmap.getPixel(x, y)
        val lum = (Color.red(pixel) + Color.green(pixel) + Color.blue(pixel)) / 3
        if (lum < darkThreshold) darkCount++
        total++
      }
      if (total > 0 && (darkCount.toFloat() / total) > 0.6f) {
        right = x - sampleStep
      } else {
        break@rightLoop
      }
    }

    val trimmedWidth = right - left + 1
    val trimmedHeight = bottom - top + 1

    if (trimmedWidth > width * 0.7f && trimmedHeight > height * 0.7f &&
      (left > 0 || top > 0 || right < width - 1 || bottom < height - 1)
    ) {
      return Bitmap.createBitmap(bitmap, left, top, trimmedWidth, trimmedHeight)
    }

    return bitmap
  }
}
