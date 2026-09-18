package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.model.AppLanguage
import com.example.model.CropRect
import com.example.model.ImagePage
import com.example.util.AppStrings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CropEditorScreen(
  page: ImagePage,
  language: AppLanguage = AppLanguage.ENGLISH,
  onSave: (rotation: Int, cropRect: CropRect?, cropRatio: Float?) -> Unit,
  onCancel: () -> Unit
) {
  val context = LocalContext.current
  var currentRotation by remember { mutableIntStateOf(page.rotationDegrees) }
  var currentRatio by remember { mutableStateOf(page.cropAspectRatio) }

  // Manual Crop Rect coordinates (normalized 0f to 1f)
  var cropLeft by remember { mutableFloatStateOf(page.cropRect?.left ?: 0f) }
  var cropTop by remember { mutableFloatStateOf(page.cropRect?.top ?: 0f) }
  var cropRight by remember { mutableFloatStateOf(page.cropRect?.right ?: 1f) }
  var cropBottom by remember { mutableFloatStateOf(page.cropRect?.bottom ?: 1f) }

  fun resetToFullPhoto() {
    cropLeft = 0f
    cropTop = 0f
    cropRight = 1f
    cropBottom = 1f
    currentRatio = null
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
              val rect = if (cropLeft > 0.01f || cropTop > 0.01f || cropRight < 0.99f || cropBottom < 0.99f) {
                CropRect(cropLeft, cropTop, cropRight, cropBottom)
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
      // Main interactive preview area with interactive Free Crop handles
      BoxWithConstraints(
        modifier = Modifier
          .weight(1f)
          .fillMaxWidth()
          .padding(vertical = 8.dp)
          .clip(RoundedCornerShape(16.dp))
          .background(Color(0xFF0F172A)),
        contentAlignment = Alignment.Center
      ) {
        val containerWidthPx = constraints.maxWidth.toFloat()
        val containerHeightPx = constraints.maxHeight.toFloat()

        Box(
          modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
          contentAlignment = Alignment.Center
        ) {
          AsyncImage(
            model = ImageRequest.Builder(context)
              .data(page.uri)
              .crossfade(true)
              .build(),
            contentDescription = "Editing photo",
            modifier = Modifier
              .fillMaxSize()
              .rotate(currentRotation.toFloat()),
            contentScale = ContentScale.Fit
          )

          // Interactive Free Crop Overlay Canvas & Drag Handler
          Box(
            modifier = Modifier
              .fillMaxSize()
              .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                  change.consume()
                  val dx = dragAmount.x / size.width.coerceAtLeast(1)
                  val dy = dragAmount.y / size.height.coerceAtLeast(1)

                  val touchX = change.position.x / size.width.coerceAtLeast(1)
                  val touchY = change.position.y / size.height.coerceAtLeast(1)

                  // Determine closest corner/edge to drag
                  val dLeft = kotlin.math.abs(touchX - cropLeft)
                  val dRight = kotlin.math.abs(touchX - cropRight)
                  val dTop = kotlin.math.abs(touchY - cropTop)
                  val dBottom = kotlin.math.abs(touchY - cropBottom)

                  if (dLeft < dRight && dLeft < 0.25f) {
                    cropLeft = (cropLeft + dx).coerceIn(0f, cropRight - 0.1f)
                  } else if (dRight < 0.25f) {
                    cropRight = (cropRight + dx).coerceIn(cropLeft + 0.1f, 1f)
                  }

                  if (dTop < dBottom && dTop < 0.25f) {
                    cropTop = (cropTop + dy).coerceIn(0f, cropBottom - 0.1f)
                  } else if (dBottom < 0.25f) {
                    cropBottom = (cropBottom + dy).coerceIn(cropTop + 0.1f, 1f)
                  }
                }
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

              // Darkened dimmed outside mask
              // Top
              drawRect(Color.Black.copy(alpha = 0.5f), Offset(0f, 0f), Size(w, rectT))
              // Bottom
              drawRect(Color.Black.copy(alpha = 0.5f), Offset(0f, rectB), Size(w, h - rectB))
              // Left
              drawRect(Color.Black.copy(alpha = 0.5f), Offset(0f, rectT), Size(rectL, rectH))
              // Right
              drawRect(Color.Black.copy(alpha = 0.5f), Offset(rectR, rectT), Size(w - rectR, rectH))

              // Crop bounding box stroke
              drawRect(
                color = Color.White,
                topLeft = Offset(rectL, rectT),
                size = Size(rectW, rectH),
                style = Stroke(width = 3.dp.toPx())
              )

              // Grid 3x3 lines
              val thirdW = rectW / 3f
              val thirdH = rectH / 3f
              drawLine(Color.White.copy(alpha = 0.4f), Offset(rectL + thirdW, rectT), Offset(rectL + thirdW, rectB), 1.dp.toPx())
              drawLine(Color.White.copy(alpha = 0.4f), Offset(rectL + thirdW * 2, rectT), Offset(rectL + thirdW * 2, rectB), 1.dp.toPx())
              drawLine(Color.White.copy(alpha = 0.4f), Offset(rectL, rectT + thirdH), Offset(rectR, rectT + thirdH), 1.dp.toPx())
              drawLine(Color.White.copy(alpha = 0.4f), Offset(rectL, rectT + thirdH * 2), Offset(rectR, rectT + thirdH * 2), 1.dp.toPx())

              // Corner handles
              val handleRadius = 6.dp.toPx()
              drawCircle(Color.White, handleRadius, Offset(rectL, rectT))
              drawCircle(Color.White, handleRadius, Offset(rectR, rectT))
              drawCircle(Color.White, handleRadius, Offset(rectL, rectB))
              drawCircle(Color.White, handleRadius, Offset(rectR, rectB))
            }
          }
        }

        // Floating orientation pill
        Surface(
          shape = RoundedCornerShape(20.dp),
          color = Color.Black.copy(alpha = 0.7f),
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
          // Rotate and Reset Controls (Clean, spacious, never clipping)
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            Button(
              onClick = {
                currentRotation = (currentRotation + 90) % 360
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

          // Crop ratio selector chips (Original, A4 Page, 1:1 Square, 4:3)
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            RatioChip(
              label = "Free/Full",
              selected = currentRatio == null,
              onClick = {
                currentRatio = null
                cropLeft = 0f
                cropTop = 0f
                cropRight = 1f
                cropBottom = 1f
              },
              modifier = Modifier.weight(1f)
            )
            RatioChip(
              label = "A4 Page",
              selected = currentRatio == 0.707f,
              onClick = {
                currentRatio = 0.707f
                // Center A4 ratio inside crop bounds
                cropLeft = 0.1f
                cropRight = 0.9f
                cropTop = 0.05f
                cropBottom = 0.95f
              },
              modifier = Modifier.weight(1f)
            )
            RatioChip(
              label = "1:1 Square",
              selected = currentRatio == 1f,
              onClick = {
                currentRatio = 1f
                cropLeft = 0.15f
                cropRight = 0.85f
                cropTop = 0.15f
                cropBottom = 0.85f
              },
              modifier = Modifier.weight(1f)
            )
            RatioChip(
              label = "4:3 Photo",
              selected = currentRatio == 1.333f,
              onClick = {
                currentRatio = 1.333f
                cropLeft = 0.05f
                cropRight = 0.95f
                cropTop = 0.15f
                cropBottom = 0.85f
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
      .height(36.dp)
      .clickable(onClick = onClick)
  ) {
    Box(contentAlignment = Alignment.Center) {
      Text(
        text = label,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
        color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
      )
    }
  }
}
