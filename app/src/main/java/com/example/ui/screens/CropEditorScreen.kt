package com.example.ui.screens

import android.graphics.Bitmap
import android.graphics.Matrix
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AppLanguage
import com.example.model.CropRect
import com.example.model.ImagePage
import com.example.util.AppStrings
import com.example.util.ImageUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CropEditorScreen(
  page: ImagePage,
  language: AppLanguage = AppLanguage.ENGLISH,
  onSave: (rotation: Int, cropRect: CropRect?, cropRatio: Float?) -> Unit,
  onCancel: () -> Unit
) {
  val context = LocalContext.current
  val sourceUri = page.originalUri ?: page.uri

  var baseBitmap by remember { mutableStateOf<Bitmap?>(null) }
  var isLoading by remember { mutableStateOf(true) }

  var currentRotation by remember { mutableIntStateOf(page.rotationDegrees) }
  var currentRatio by remember { mutableStateOf(page.cropAspectRatio) }

  // Manual Crop Rect coordinates (normalized 0f to 1f relative to rotated image)
  var cropLeft by remember { mutableFloatStateOf(page.cropRect?.left ?: 0f) }
  var cropTop by remember { mutableFloatStateOf(page.cropRect?.top ?: 0f) }
  var cropRight by remember { mutableFloatStateOf(page.cropRect?.right ?: 1f) }
  var cropBottom by remember { mutableFloatStateOf(page.cropRect?.bottom ?: 1f) }

  // 0: None, 1: TopLeft, 2: TopRight, 3: BottomLeft, 4: BottomRight, 5: Top, 6: Bottom, 7: Left, 8: Right, 9: Move
  var activeDragMode by remember { mutableIntStateOf(0) }

  // Load decoded base bitmap (EXIF-normalized)
  LaunchedEffect(sourceUri) {
    isLoading = true
    withContext(Dispatchers.IO) {
      val bmp = ImageUtils.decodeSampledBitmapFromUri(context, sourceUri, 2048, 2048)
      withContext(Dispatchers.Main) {
        baseBitmap = bmp
        isLoading = false
      }
    }
  }

  // Active rotated display bitmap
  val displayBitmap = remember(baseBitmap, currentRotation) {
    val src = baseBitmap ?: return@remember null
    if (currentRotation % 360 != 0) {
      val matrix = Matrix().apply { postRotate(currentRotation.toFloat()) }
      Bitmap.createBitmap(src, 0, 0, src.width, src.height, matrix, true)
    } else {
      src
    }
  }

  val bmpWidth = displayBitmap?.width ?: 1
  val bmpHeight = displayBitmap?.height ?: 1
  val imageAspect = bmpWidth.toFloat() / bmpHeight.toFloat()

  fun resetToFullPhoto() {
    cropLeft = 0f
    cropTop = 0f
    cropRight = 1f
    cropBottom = 1f
    currentRatio = null
  }

  fun applyPresetRatio(targetAspect: Float) {
    currentRatio = targetAspect
    if (imageAspect > targetAspect) {
      // Photo is wider than target aspect ratio -> fit height, crop width centered
      val visibleWidthRatio = (targetAspect / imageAspect).coerceIn(0.1f, 1f)
      val insetX = (1f - visibleWidthRatio) / 2f
      cropLeft = insetX
      cropRight = 1f - insetX
      cropTop = 0f
      cropBottom = 1f
    } else {
      // Photo is taller than target aspect ratio -> fit width, crop height centered
      val visibleHeightRatio = (imageAspect / targetAspect).coerceIn(0.1f, 1f)
      val insetY = (1f - visibleHeightRatio) / 2f
      cropTop = insetY
      cropBottom = 1f - insetY
      cropLeft = 0f
      cropRight = 1f
    }
  }

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Text(
            text = AppStrings.get(language, "save_page"),
            fontWeight = FontWeight.Bold
          )
        },
        navigationIcon = {
          IconButton(onClick = onCancel) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = "Cancel"
            )
          }
        },
        actions = {
          // Reset to Full Photo button
          IconButton(
            onClick = { resetToFullPhoto() },
            modifier = Modifier.testTag("reset_full_photo_button")
          ) {
            Icon(
              imageVector = Icons.Default.Refresh,
              contentDescription = "Reset to Full Photo",
              tint = MaterialTheme.colorScheme.primary
            )
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = MaterialTheme.colorScheme.background
        )
      )
    },
    bottomBar = {
      Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp,
        modifier = Modifier
          .fillMaxWidth()
          .navigationBarsPadding()
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
          horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          OutlinedButton(
            onClick = onCancel,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
              .weight(1f)
              .height(54.dp)
              .testTag("crop_cancel_button")
          ) {
            Text(
              text = AppStrings.get(language, "cancel"),
              fontWeight = FontWeight.Bold,
              fontSize = 16.sp
            )
          }

          Button(
            onClick = {
              val rect = if (cropLeft > 0.005f || cropTop > 0.005f || cropRight < 0.995f || cropBottom < 0.995f) {
                CropRect(
                  left = cropLeft.coerceIn(0f, 0.95f),
                  top = cropTop.coerceIn(0f, 0.95f),
                  right = cropRight.coerceIn(0.05f, 1f),
                  bottom = cropBottom.coerceIn(0.05f, 1f)
                )
              } else {
                null
              }
              onSave(currentRotation, rect, currentRatio)
            },
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
              .weight(1f)
              .height(54.dp)
              .testTag("crop_save_button")
          ) {
            Icon(Icons.Default.Check, contentDescription = null)
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = AppStrings.get(language, "save_page"),
              fontWeight = FontWeight.Bold,
              fontSize = 16.sp
            )
          }
        }
      }
    },
    containerColor = MaterialTheme.colorScheme.background
  ) { innerPadding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
        .padding(horizontal = 16.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      // Main interactive preview area with WYSIWYG crop canvas
      BoxWithConstraints(
        modifier = Modifier
          .weight(1f)
          .fillMaxWidth()
          .padding(vertical = 8.dp)
          .clip(RoundedCornerShape(16.dp))
          .background(Color(0xFF0F172A)),
        contentAlignment = Alignment.Center
      ) {
        if (isLoading || displayBitmap == null) {
          CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        } else {
          // Inner container sized precisely to the aspect ratio of the photo
          Box(
            modifier = Modifier
              .padding(12.dp)
              .aspectRatio(imageAspect, matchHeightConstraintsFirst = constraints.maxHeight * imageAspect < constraints.maxWidth)
              .clip(RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
          ) {
            // Render rotated bitmap with exact aspect ratio
            Image(
              bitmap = displayBitmap.asImageBitmap(),
              contentDescription = "Editing Photo",
              modifier = Modifier.fillMaxSize(),
              contentScale = ContentScale.FillBounds
            )

            // Interactive Drag Gesture & Overlay Canvas
            val primaryColor = MaterialTheme.colorScheme.primary
            Box(
              modifier = Modifier
                .fillMaxSize()
                .pointerInput(cropLeft, cropTop, cropRight, cropBottom) {
                  val touchHitRadiusPx = 56.dp.toPx()

                  detectDragGestures(
                    onDragStart = { offset ->
                      val w = size.width.toFloat()
                      val h = size.height.toFloat()
                      val pX = offset.x
                      val pY = offset.y

                      val cornerTL = Offset(cropLeft * w, cropTop * h)
                      val cornerTR = Offset(cropRight * w, cropTop * h)
                      val cornerBL = Offset(cropLeft * w, cropBottom * h)
                      val cornerBR = Offset(cropRight * w, cropBottom * h)

                      fun distSq(a: Offset, x: Float, y: Float): Float {
                        val dx = a.x - x
                        val dy = a.y - y
                        return dx * dx + dy * dy
                      }
                      val hitRadiusSq = touchHitRadiusPx * touchHitRadiusPx

                      val dTL = distSq(cornerTL, pX, pY)
                      val dTR = distSq(cornerTR, pX, pY)
                      val dBL = distSq(cornerBL, pX, pY)
                      val dBR = distSq(cornerBR, pX, pY)

                      activeDragMode = when {
                        dTL <= hitRadiusSq && dTL <= minOf(dTR, dBL, dBR) -> 1
                        dTR <= hitRadiusSq && dTR <= minOf(dTL, dBL, dBR) -> 2
                        dBL <= hitRadiusSq && dBL <= minOf(dTL, dTR, dBR) -> 3
                        dBR <= hitRadiusSq -> 4
                        // Edge touches
                        kotlin.math.abs(pY - cornerTL.y) <= touchHitRadiusPx && pX in (cornerTL.x - 20f)..(cornerTR.x + 20f) -> 5
                        kotlin.math.abs(pY - cornerBL.y) <= touchHitRadiusPx && pX in (cornerBL.x - 20f)..(cornerBR.x + 20f) -> 6
                        kotlin.math.abs(pX - cornerTL.x) <= touchHitRadiusPx && pY in (cornerTL.y - 20f)..(cornerBL.y + 20f) -> 7
                        kotlin.math.abs(pX - cornerTR.x) <= touchHitRadiusPx && pY in (cornerTR.y - 20f)..(cornerBR.y + 20f) -> 8
                        // Inside box -> move
                        pX in cornerTL.x..cornerTR.x && pY in cornerTL.y..cornerBL.y -> 9
                        else -> 0
                      }
                    },
                    onDragEnd = {
                      activeDragMode = 0
                    },
                    onDragCancel = {
                      activeDragMode = 0
                    },
                    onDrag = { change, dragAmount ->
                      change.consume()
                      val dx = dragAmount.x / size.width.coerceAtLeast(1)
                      val dy = dragAmount.y / size.height.coerceAtLeast(1)
                      val minSpan = 0.08f

                      when (activeDragMode) {
                        1 -> { // TopLeft
                          cropLeft = (cropLeft + dx).coerceIn(0f, cropRight - minSpan)
                          cropTop = (cropTop + dy).coerceIn(0f, cropBottom - minSpan)
                        }
                        2 -> { // TopRight
                          cropRight = (cropRight + dx).coerceIn(cropLeft + minSpan, 1f)
                          cropTop = (cropTop + dy).coerceIn(0f, cropBottom - minSpan)
                        }
                        3 -> { // BottomLeft
                          cropLeft = (cropLeft + dx).coerceIn(0f, cropRight - minSpan)
                          cropBottom = (cropBottom + dy).coerceIn(cropTop + minSpan, 1f)
                        }
                        4 -> { // BottomRight
                          cropRight = (cropRight + dx).coerceIn(cropLeft + minSpan, 1f)
                          cropBottom = (cropBottom + dy).coerceIn(cropTop + minSpan, 1f)
                        }
                        5 -> { // Top edge
                          cropTop = (cropTop + dy).coerceIn(0f, cropBottom - minSpan)
                        }
                        6 -> { // Bottom edge
                          cropBottom = (cropBottom + dy).coerceIn(cropTop + minSpan, 1f)
                        }
                        7 -> { // Left edge
                          cropLeft = (cropLeft + dx).coerceIn(0f, cropRight - minSpan)
                        }
                        8 -> { // Right edge
                          cropRight = (cropRight + dx).coerceIn(cropLeft + minSpan, 1f)
                        }
                        9 -> { // Pan / Move entire box
                          val width = cropRight - cropLeft
                          val height = cropBottom - cropTop
                          val newLeft = (cropLeft + dx).coerceIn(0f, 1f - width)
                          val newTop = (cropTop + dy).coerceIn(0f, 1f - height)
                          cropLeft = newLeft
                          cropRight = newLeft + width
                          cropTop = newTop
                          cropBottom = newTop + height
                        }
                      }
                    }
                  )
                }
            ) {
              Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                val rectL = cropLeft * w
                val rectT = cropTop * h
                val rectR = cropRight * w
                val rectB = cropBottom * h
                val rectW = (rectR - rectL).coerceAtLeast(1f)
                val rectH = (rectB - rectT).coerceAtLeast(1f)

                // Darkened mask outside crop box
                drawRect(Color.Black.copy(alpha = 0.58f), Offset(0f, 0f), Size(w, rectT))
                drawRect(Color.Black.copy(alpha = 0.58f), Offset(0f, rectB), Size(w, h - rectB))
                drawRect(Color.Black.copy(alpha = 0.58f), Offset(0f, rectT), Size(rectL, rectH))
                drawRect(Color.Black.copy(alpha = 0.58f), Offset(rectR, rectT), Size(w - rectR, rectH))

                // Crop bounding frame
                drawRect(
                  color = Color.White,
                  topLeft = Offset(rectL, rectT),
                  size = Size(rectW, rectH),
                  style = Stroke(width = 2.5.dp.toPx())
                )

                // 3x3 Composition grid
                val thirdW = rectW / 3f
                val thirdH = rectH / 3f
                drawLine(Color.White.copy(alpha = 0.35f), Offset(rectL + thirdW, rectT), Offset(rectL + thirdW, rectB), 1.dp.toPx())
                drawLine(Color.White.copy(alpha = 0.35f), Offset(rectL + thirdW * 2, rectT), Offset(rectL + thirdW * 2, rectB), 1.dp.toPx())
                drawLine(Color.White.copy(alpha = 0.35f), Offset(rectL, rectT + thirdH), Offset(rectR, rectT + thirdH), 1.dp.toPx())
                drawLine(Color.White.copy(alpha = 0.35f), Offset(rectL, rectT + thirdH * 2), Offset(rectR, rectT + thirdH * 2), 1.dp.toPx())

                // High-Contrast Dual-Layer Corner Handles with active touch feedback
                fun drawEnhancedHandle(center: Offset, isActive: Boolean) {
                  val baseRadius = 14.dp.toPx()
                  val activeRadius = 22.dp.toPx()
                  val currentRadius = if (isActive) activeRadius else baseRadius

                  // Outer glowing halo if actively dragged
                  if (isActive) {
                    drawCircle(primaryColor.copy(alpha = 0.35f), currentRadius + 8.dp.toPx(), center)
                  }

                  // Solid dark outer border
                  drawCircle(Color(0xFF0F172A), currentRadius + 3.dp.toPx(), center)
                  // Bright white inner fill
                  drawCircle(Color.White, currentRadius, center)
                  // Center primary dot
                  val innerDotRadius = if (isActive) 7.dp.toPx() else 4.5.dp.toPx()
                  drawCircle(primaryColor, innerDotRadius, center)
                }

                // Draw corners (1: TL, 2: TR, 3: BL, 4: BR)
                drawEnhancedHandle(Offset(rectL, rectT), activeDragMode == 1)
                drawEnhancedHandle(Offset(rectR, rectT), activeDragMode == 2)
                drawEnhancedHandle(Offset(rectL, rectB), activeDragMode == 3)
                drawEnhancedHandle(Offset(rectR, rectB), activeDragMode == 4)

                // High-Contrast Edge Handles (5: Top, 6: Bottom, 7: Left, 8: Right)
                fun drawEdgePill(center: Offset, isHorizontal: Boolean, isActive: Boolean) {
                  val pillLength = (if (isActive) 34.dp else 24.dp).toPx()
                  val pillThickness = (if (isActive) 9.dp else 7.dp).toPx()

                  val pillSize = if (isHorizontal) Size(pillLength, pillThickness) else Size(pillThickness, pillLength)
                  val pillTopLeft = Offset(center.x - pillSize.width / 2f, center.y - pillSize.height / 2f)

                  // Dark border
                  drawRoundRect(
                    color = Color(0xFF0F172A),
                    topLeft = Offset(pillTopLeft.x - 2.dp.toPx(), pillTopLeft.y - 2.dp.toPx()),
                    size = Size(pillSize.width + 4.dp.toPx(), pillSize.height + 4.dp.toPx()),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(pillThickness)
                  )
                  // White fill
                  drawRoundRect(
                    color = if (isActive) primaryColor else Color.White,
                    topLeft = pillTopLeft,
                    size = pillSize,
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(pillThickness / 2f)
                  )
                }

                drawEdgePill(Offset(rectL + (rectW / 2f), rectT), isHorizontal = true, isActive = activeDragMode == 5)
                drawEdgePill(Offset(rectL + (rectW / 2f), rectB), isHorizontal = true, isActive = activeDragMode == 6)
                drawEdgePill(Offset(rectL, rectT + (rectH / 2f)), isHorizontal = false, isActive = activeDragMode == 7)
                drawEdgePill(Offset(rectR, rectT + (rectH / 2f)), isHorizontal = false, isActive = activeDragMode == 8)
              }
            }
          }
        }

        // Floating orientation pill
        Surface(
          shape = RoundedCornerShape(20.dp),
          color = Color.Black.copy(alpha = 0.75f),
          modifier = Modifier
            .align(Alignment.TopEnd)
            .padding(12.dp)
        ) {
          Text(
            text = "$currentRotation°",
            color = Color.White,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
          )
        }
      }

      // Action controls toolbar
      Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          // Rotate and Reset Controls
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            Button(
              onClick = {
                currentRotation = (currentRotation + 90) % 360
                resetToFullPhoto()
              },
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier
                .weight(1f)
                .height(48.dp)
                .testTag("rotate_button")
            ) {
              Icon(Icons.Default.RotateRight, contentDescription = null, modifier = Modifier.size(20.dp))
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = "Rotate 90°",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                maxLines = 1,
                softWrap = false
              )
            }

            OutlinedButton(
              onClick = { resetToFullPhoto() },
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier
                .weight(1f)
                .height(48.dp)
                .testTag("reset_crop_button")
            ) {
              Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(20.dp))
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = "Reset",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                maxLines = 1,
                softWrap = false
              )
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          // Clean, distinct aspect ratio preset buttons: Free/Full, A4 Page, 1:1 Square, 4:3 Photo
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            RatioChip(
              label = "Free/Full",
              selected = currentRatio == null,
              onClick = { resetToFullPhoto() },
              modifier = Modifier.weight(1f)
            )
            RatioChip(
              label = "A4 Page",
              selected = currentRatio == 0.7071f || currentRatio == 1.4142f,
              onClick = {
                val a4Ratio = if (imageAspect >= 1f) 1.4142f else 0.7071f
                applyPresetRatio(a4Ratio)
              },
              modifier = Modifier.weight(1f)
            )
            RatioChip(
              label = "1:1 Square",
              selected = currentRatio == 1f,
              onClick = { applyPresetRatio(1f) },
              modifier = Modifier.weight(1f)
            )
            RatioChip(
              label = "4:3 Photo",
              selected = currentRatio == 1.3333f || currentRatio == 0.75f,
              onClick = {
                val photoRatio = if (imageAspect >= 1f) (4f / 3f) else (3f / 4f)
                applyPresetRatio(photoRatio)
              },
              modifier = Modifier.weight(1f)
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(8.dp))
    }
  }
}

@Composable
private fun RatioChip(
  label: String,
  selected: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Surface(
    shape = RoundedCornerShape(10.dp),
    color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
    modifier = modifier
      .height(38.dp)
      .clickable(onClick = onClick)
  ) {
    Box(contentAlignment = Alignment.Center) {
      Text(
        text = label,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
        color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
      )
    }
  }
}
