package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Environment
import androidx.core.content.FileProvider
import com.example.model.DeveloperSettings
import com.example.model.GeneratedPdf
import com.example.model.ImagePage
import com.example.model.PageMarginOption
import com.example.model.PdfQuality
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfGenerator {

  // Standard A4 dimensions in points (1 point = 1/72 inch)
  const val A4_WIDTH_PTS = 595
  const val A4_HEIGHT_PTS = 842

  suspend fun createPdf(
    context: Context,
    pages: List<ImagePage>,
    settings: DeveloperSettings,
    selectedQuality: PdfQuality,
    onProgress: (current: Int, total: Int) -> Unit
  ): Result<GeneratedPdf> = withContext(Dispatchers.IO) {
    if (pages.isEmpty()) {
      return@withContext Result.failure(IllegalArgumentException("No pages to generate"))
    }

    val pdfDocument = PdfDocument()
    try {
      val isStandard = selectedQuality == PdfQuality.STANDARD ||
        (selectedQuality == PdfQuality.ALWAYS_ASK && settings.defaultQuality == PdfQuality.STANDARD)

      val targetWidth = if (isStandard) 1240 else 2480
      val targetHeight = if (isStandard) 1754 else 3508
      val jpegQuality = if (isStandard) 75 else 92

      val marginPoints = if (settings.pageMargin == PageMarginOption.BORDERED) 28f else 0f
      val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)

      for (i in pages.indices) {
        onProgress(i + 1, pages.size)
        val pageInfo = PdfDocument.PageInfo.Builder(A4_WIDTH_PTS, A4_HEIGHT_PTS, i + 1).create()
        val pdfPage = pdfDocument.startPage(pageInfo)
        val canvas: Canvas = pdfPage.canvas

        // Paint white background
        canvas.drawColor(Color.WHITE)

        val pageItem = pages[i]
        val rawBitmap = ImageUtils.decodeSampledBitmapFromUri(
          context,
          pageItem.uri,
          targetWidth,
          targetHeight
        )

        if (rawBitmap != null) {
          val autoTrim = pageItem.autoTrimApplied || settings.autoCropOnImport
          val processed = ImageUtils.processBitmap(
            rawBitmap,
            pageItem.rotationDegrees,
            pageItem.cropAspectRatio,
            autoTrim
          )

          // Compress to JPEG stream if standard quality to save PDF weight
          val finalBitmap = if (isStandard) {
            val stream = ByteArrayOutputStream()
            processed.compress(Bitmap.CompressFormat.JPEG, jpegQuality, stream)
            val bytes = stream.toByteArray()
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size) ?: processed
          } else {
            processed
          }

          // Compute destination rect on A4 page
          val availableWidth = A4_WIDTH_PTS - (marginPoints * 2)
          val availableHeight = A4_HEIGHT_PTS - (marginPoints * 2)

          val bmpWidth = finalBitmap.width.toFloat()
          val bmpHeight = finalBitmap.height.toFloat()

          val scale = minOf(availableWidth / bmpWidth, availableHeight / bmpHeight)
          val destWidth = bmpWidth * scale
          val destHeight = bmpHeight * scale

          val destLeft = marginPoints + (availableWidth - destWidth) / 2f
          val destTop = marginPoints + (availableHeight - destHeight) / 2f
          val destRect = RectF(destLeft, destTop, destLeft + destWidth, destTop + destHeight)

          canvas.drawBitmap(finalBitmap, null, destRect, paint)

          if (finalBitmap != processed && finalBitmap != rawBitmap) {
            finalBitmap.recycle()
          }
          if (processed != rawBitmap) {
            processed.recycle()
          }
          rawBitmap.recycle()
        }

        pdfDocument.finishPage(pdfPage)
      }

      // Prepare destination folder
      val folderName = settings.defaultSavePath.ifBlank { "PDF documents(pdf_maker)" }
      val targetDir = getPdfOutputDirectory(context, folderName)
      if (!targetDir.exists()) {
        targetDir.mkdirs()
      }

      val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
      val fileName = "PDF_${timeStamp}.pdf"
      val outputFile = File(targetDir, fileName)

      FileOutputStream(outputFile).use { fos ->
        pdfDocument.writeTo(fos)
      }

      val contentUri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        outputFile
      )

      val generated = GeneratedPdf(
        file = outputFile,
        uri = contentUri,
        fileName = fileName,
        sizeBytes = outputFile.length(),
        pageCount = pages.size,
        timestamp = System.currentTimeMillis()
      )

      Result.success(generated)
    } catch (e: Exception) {
      Result.failure(e)
    } finally {
      pdfDocument.close()
    }
  }

  fun getPdfOutputDirectory(context: Context, folderName: String): File {
    val externalDocs = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
    return if (externalDocs != null) {
      File(externalDocs, folderName)
    } else {
      File(context.filesDir, folderName)
    }
  }

  fun listRecentPdfs(context: Context, folderName: String): List<GeneratedPdf> {
    val dir = getPdfOutputDirectory(context, folderName)
    if (!dir.exists() || !dir.isDirectory) return emptyList()

    return dir.listFiles { file -> file.isFile && file.name.endsWith(".pdf", ignoreCase = true) }
      ?.sortedByDescending { it.lastModified() }
      ?.take(100)
      ?.mapNotNull { file ->
        try {
          val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
          )
          GeneratedPdf(
            file = file,
            uri = uri,
            fileName = file.name,
            sizeBytes = file.length(),
            pageCount = 1, // fast placeholder for list
            timestamp = file.lastModified()
          )
        } catch (_: Exception) {
          null
        }
      } ?: emptyList()
  }

  fun deletePdf(pdf: GeneratedPdf): Boolean {
    return try {
      pdf.file.delete()
    } catch (_: Exception) {
      false
    }
  }

  fun sharePdf(context: Context, pdf: GeneratedPdf) {
    val shareIntent = Intent(Intent.ACTION_SEND).apply {
      type = "application/pdf"
      putExtra(Intent.EXTRA_STREAM, pdf.uri)
      putExtra(Intent.EXTRA_SUBJECT, pdf.fileName)
      addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(shareIntent, "Share PDF via"))
  }

  fun openPdf(context: Context, pdf: GeneratedPdf) {
    val viewIntent = Intent(Intent.ACTION_VIEW).apply {
      setDataAndType(pdf.uri, "application/pdf")
      addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
      addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    try {
      context.startActivity(viewIntent)
    } catch (_: Exception) {
      // Fallback: share if no viewer is installed
      sharePdf(context, pdf)
    }
  }

  fun renamePdf(context: Context, oldPdf: GeneratedPdf, newFileNameWithExt: String): GeneratedPdf? {
    val sanitized = if (newFileNameWithExt.endsWith(".pdf", ignoreCase = true)) {
      newFileNameWithExt
    } else {
      "$newFileNameWithExt.pdf"
    }

    val newFile = File(oldPdf.file.parentFile, sanitized)
    return if (oldPdf.file.renameTo(newFile)) {
      val uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        newFile
      )
      GeneratedPdf(
        file = newFile,
        uri = uri,
        fileName = sanitized,
        sizeBytes = newFile.length(),
        pageCount = oldPdf.pageCount,
        timestamp = System.currentTimeMillis()
      )
    } else {
      null
    }
  }
}
