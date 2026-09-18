package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Environment
import androidx.core.content.FileProvider
import com.example.model.DeveloperSettings
import com.example.model.GeneratedPdf
import com.example.model.ImagePage
import com.example.model.PageLayoutMode
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

      // Compression upgrade pipeline:
      // Standard Quality: downsample long edge to ~1920-2040px with ~70% JPEG quality
      // Original Quality: high decode buffer ~3500px with ~94% quality
      val targetDimension = if (isStandard) 1920 else 3500
      val jpegQuality = if (isStandard) 70 else 94

      val isFreeDynamic = settings.pageLayoutMode == PageLayoutMode.FREE_DYNAMIC
      val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)

      for (i in pages.indices) {
        onProgress(i + 1, pages.size)
        val pageItem = pages[i]

        val rawBitmap = ImageUtils.decodeSampledBitmapFromUri(
          context,
          pageItem.uri,
          targetDimension,
          targetDimension
        )

        if (rawBitmap != null) {
          val processed = ImageUtils.processBitmap(
            rawBitmap,
            pageItem.rotationDegrees,
            pageItem.cropRect,
            pageItem.cropAspectRatio
          )

          // Downsample and compress to JPEG if standard quality to reduce PDF weight significantly
          val downsampled = if (isStandard) {
            ImageUtils.downsampleBitmap(processed, targetDimension)
          } else {
            processed
          }

          val finalBitmap = if (isStandard) {
            val stream = ByteArrayOutputStream()
            downsampled.compress(Bitmap.CompressFormat.JPEG, jpegQuality, stream)
            val bytes = stream.toByteArray()
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size) ?: downsampled
          } else {
            downsampled
          }

          val bmpWidth = finalBitmap.width.toFloat()
          val bmpHeight = finalBitmap.height.toFloat()

          if (isFreeDynamic) {
            // MODE 1: FREE / DYNAMIC
            // Page canvas matches the photo's native aspect ratio with zero margins
            val basePt = 595f
            val (pageW, pageH) = if (bmpWidth >= bmpHeight) {
              val w = (basePt * (bmpWidth / bmpHeight)).toInt().coerceAtLeast(100)
              w to basePt.toInt()
            } else {
              val h = (basePt * (bmpHeight / bmpWidth)).toInt().coerceAtLeast(100)
              basePt.toInt() to h
            }

            val pageInfo = PdfDocument.PageInfo.Builder(pageW, pageH, i + 1).create()
            val pdfPage = pdfDocument.startPage(pageInfo)
            val canvas: Canvas = pdfPage.canvas

            // Direct edge-to-edge drawing with no white margins
            val destRect = RectF(0f, 0f, pageW.toFloat(), pageH.toFloat())
            canvas.drawBitmap(finalBitmap, null, destRect, paint)
            pdfDocument.finishPage(pdfPage)
          } else {
            // MODE 2: A4 STANDARD
            // Standard A4 page canvas with centered and scaled photo
            val pageInfo = PdfDocument.PageInfo.Builder(A4_WIDTH_PTS, A4_HEIGHT_PTS, i + 1).create()
            val pdfPage = pdfDocument.startPage(pageInfo)
            val canvas: Canvas = pdfPage.canvas
            canvas.drawColor(Color.WHITE)

            val marginPoints = 20f
            val availableWidth = A4_WIDTH_PTS - (marginPoints * 2)
            val availableHeight = A4_HEIGHT_PTS - (marginPoints * 2)

            val scale = minOf(availableWidth / bmpWidth, availableHeight / bmpHeight)
            val destWidth = bmpWidth * scale
            val destHeight = bmpHeight * scale

            val destLeft = marginPoints + (availableWidth - destWidth) / 2f
            val destTop = marginPoints + (availableHeight - destHeight) / 2f
            val destRect = RectF(destLeft, destTop, destLeft + destWidth, destTop + destHeight)

            canvas.drawBitmap(finalBitmap, null, destRect, paint)
            pdfDocument.finishPage(pdfPage)
          }

          if (finalBitmap != downsampled && finalBitmap != processed && finalBitmap != rawBitmap) {
            finalBitmap.recycle()
          }
          if (downsampled != processed && downsampled != rawBitmap) {
            downsampled.recycle()
          }
          if (processed != rawBitmap) {
            processed.recycle()
          }
          rawBitmap.recycle()
        } else {
          // Empty fallback page if decode fails
          val pageInfo = PdfDocument.PageInfo.Builder(A4_WIDTH_PTS, A4_HEIGHT_PTS, i + 1).create()
          val pdfPage = pdfDocument.startPage(pageInfo)
          pdfPage.canvas.drawColor(Color.WHITE)
          pdfDocument.finishPage(pdfPage)
        }
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
            pageCount = 1,
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

  fun renamePdf(context: Context, pdf: GeneratedPdf, newFileName: String): GeneratedPdf? {
    return try {
      val cleanName = if (newFileName.endsWith(".pdf", ignoreCase = true)) {
        newFileName
      } else {
        "$newFileName.pdf"
      }
      val newFile = File(pdf.file.parentFile, cleanName)
      if (pdf.file.renameTo(newFile)) {
        val newUri = FileProvider.getUriForFile(
          context,
          "${context.packageName}.fileprovider",
          newFile
        )
        pdf.copy(
          file = newFile,
          uri = newUri,
          fileName = cleanName,
          sizeBytes = newFile.length()
        )
      } else {
        null
      }
    } catch (_: Exception) {
      null
    }
  }

  fun sharePdf(context: Context, pdf: GeneratedPdf) {
    try {
      val intent = Intent(Intent.ACTION_SEND).apply {
        type = "application/pdf"
        putExtra(Intent.EXTRA_STREAM, pdf.uri)
        putExtra(Intent.EXTRA_SUBJECT, pdf.fileName)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
      }
      context.startActivity(Intent.createChooser(intent, "Share PDF via..."))
    } catch (_: Exception) {
    }
  }

  fun batchSharePdfs(context: Context, pdfs: List<GeneratedPdf>) {
    if (pdfs.isEmpty()) return
    if (pdfs.size == 1) {
      sharePdf(context, pdfs.first())
      return
    }
    try {
      val uriList = ArrayList<Uri>(pdfs.map { it.uri })
      val intent = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
        type = "application/pdf"
        putParcelableArrayListExtra(Intent.EXTRA_STREAM, uriList)
        putExtra(Intent.EXTRA_SUBJECT, "${pdfs.size} PDFs from pdf_maker")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
      }
      context.startActivity(Intent.createChooser(intent, "Share ${pdfs.size} PDFs via..."))
    } catch (_: Exception) {
    }
  }

  fun openPdf(context: Context, pdf: GeneratedPdf) {
    try {
      val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(pdf.uri, "application/pdf")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      }
      context.startActivity(intent)
    } catch (_: Exception) {
    }
  }
}
