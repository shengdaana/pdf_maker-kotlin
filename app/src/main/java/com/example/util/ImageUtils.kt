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
import androidx.exifinterface.media.ExifInterface
import com.example.model.CropRect
import com.example.model.ImagePage
import java.io.File
import java.io.FileOutputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object ImageUtils {

  /**
   * Reads EXIF orientation from the image Uri.
   */
  fun getExifOrientation(context: Context, uri: Uri): Int {
    return try {
      if (uri.scheme == "file" && uri.path != null) {
        val exif = ExifInterface(uri.path!!)
        exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
      } else {
        context.contentResolver.openInputStream(uri)?.use { stream ->
          val exif = ExifInterface(stream)
          exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
        } ?: ExifInterface.ORIENTATION_NORMAL
      }
    } catch (_: Exception) {
      ExifInterface.ORIENTATION_NORMAL
    }
  }

  /**
   * Decodes a bitmap from a content Uri with bounds check to prevent OOM,
   * AND immediately applies EXIF orientation normalization so the decoded bitmap
   * is guaranteed to be 0° upright visual orientation before any further processing.
   */
  fun decodeSampledBitmapFromUri(
    context: Context,
    uri: Uri,
    reqWidth: Int,
    reqHeight: Int
  ): Bitmap? {
    return try {
      val orientation = getExifOrientation(context, uri)
      val isRotated90or270 = orientation == ExifInterface.ORIENTATION_ROTATE_90 ||
        orientation == ExifInterface.ORIENTATION_ROTATE_270 ||
        orientation == ExifInterface.ORIENTATION_TRANSPOSE ||
        orientation == ExifInterface.ORIENTATION_TRANSVERSE

      val options = BitmapFactory.Options().apply {
        inJustDecodeBounds = true
      }
      context.contentResolver.openInputStream(uri)?.use { stream ->
        BitmapFactory.decodeStream(stream, null, options)
      }

      options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight, isRotated90or270)
      options.inJustDecodeBounds = false
      options.inPreferredConfig = Bitmap.Config.ARGB_8888

      val decoded = context.contentResolver.openInputStream(uri)?.use { stream ->
        BitmapFactory.decodeStream(stream, null, options)
      } ?: return null

      // Normalize EXIF orientation to ensure 0° visual upright orientation
      val matrix = Matrix()
      when (orientation) {
        ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
        ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
        ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
        ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.postScale(-1f, 1f)
        ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.postScale(1f, -1f)
        ExifInterface.ORIENTATION_TRANSPOSE -> {
          matrix.postRotate(90f)
          matrix.postScale(-1f, 1f)
        }
        ExifInterface.ORIENTATION_TRANSVERSE -> {
          matrix.postRotate(270f)
          matrix.postScale(-1f, 1f)
        }
      }

      if (!matrix.isIdentity) {
        val normalized = Bitmap.createBitmap(decoded, 0, 0, decoded.width, decoded.height, matrix, true)
        if (normalized != decoded) {
          decoded.recycle()
        }
        normalized
      } else {
        decoded
      }
    } catch (_: Exception) {
      null
    }
  }

  private fun calculateInSampleSize(
    options: BitmapFactory.Options,
    reqWidth: Int,
    reqHeight: Int,
    isSwapped: Boolean = false
  ): Int {
    val rawH = options.outHeight
    val rawW = options.outWidth
    val height = if (isSwapped) rawW else rawH
    val width = if (isSwapped) rawH else rawW
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
   * Applies user rotation, manual free crop bounds, and aspect ratio crop.
   * Auto-crop has been completely removed to prioritize precise manual Free Crop.
   */
  fun processBitmap(
    source: Bitmap,
    rotationDegrees: Int,
    cropRect: CropRect?,
    targetAspectRatio: Float? = null
  ): Bitmap {
    var result = source

    // 1. Apply user rotation if any (relative to already EXIF-normalized upright bitmap)
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

    // 3. Apply target aspect ratio crop if explicitly requested (e.g., A4 or 1:1)
    if (targetAspectRatio != null && targetAspectRatio > 0f) {
      val currentAspect = result.width.toFloat() / result.height.toFloat()
      val rect: Rect = if (currentAspect > targetAspectRatio) {
        val newWidth = (result.height * targetAspectRatio).toInt().coerceAtMost(result.width)
        val xOffset = (result.width - newWidth) / 2
        Rect(xOffset, 0, xOffset + newWidth, result.height)
      } else {
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
   * Helper to decode and apply transforms (EXIF normalization, user rotation, manual crop) to an ImagePage.
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
      targetAspectRatio = page.cropAspectRatio
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
        combinedBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
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
