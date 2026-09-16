package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.model.ImagePage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CropEditorScreen(
  page: ImagePage,
  onSave: (rotation: Int, cropRatio: Float?, autoTrim: Boolean) -> Unit,
  onCancel: () -> Unit
) {
  val context = LocalContext.current
  var currentRotation by remember { mutableIntStateOf(page.rotationDegrees) }
  var currentRatio by remember { mutableStateOf(page.cropAspectRatio) }
  var autoTrim by remember { mutableStateOf(page.autoTrimApplied) }

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Text(
            text = "Edit Page & Crop",
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
              text = "Cancel",
              fontWeight = FontWeight.Bold,
              fontSize = 16.sp
            )
          }

          Button(
            onClick = {
              onSave(currentRotation, currentRatio, autoTrim)
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
              text = "Save Page",
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
        .padding(horizontal = 20.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      // Main interactive preview area
      Box(
        modifier = Modifier
          .weight(1f)
          .fillMaxWidth()
          .padding(vertical = 12.dp)
          .clip(RoundedCornerShape(16.dp))
          .background(Color(0xFF1E293B)),
        contentAlignment = Alignment.Center
      ) {
        val aspectModifier = when (currentRatio) {
          1f -> Modifier.aspectRatio(1f)
          0.707f -> Modifier.aspectRatio(0.707f)
          1.333f -> Modifier.aspectRatio(1.333f)
          else -> Modifier.fillMaxSize()
        }

        Box(
          modifier = aspectModifier
            .padding(16.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color.Black),
          contentAlignment = Alignment.Center
        ) {
          AsyncImage(
            model = ImageRequest.Builder(context)
              .data(page.uri)
              .crossfade(true)
              .build(),
            contentDescription = "Editing image",
            modifier = Modifier
              .fillMaxSize()
              .rotate(currentRotation.toFloat()),
            contentScale = ContentScale.Fit
          )
        }

        // Floating orientation pill
        Surface(
          shape = RoundedCornerShape(20.dp),
          color = Color.Black.copy(alpha = 0.65f),
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
        Column(modifier = Modifier.padding(16.dp)) {
          Text(
            text = "Page Adjustments",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
          )

          Spacer(modifier = Modifier.height(12.dp))

          // Rotate & Auto-Trim row
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
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
              Icon(Icons.Default.RotateRight, contentDescription = null)
              Spacer(modifier = Modifier.width(6.dp))
              Text("Rotate 90°", fontWeight = FontWeight.Bold)
            }

            Surface(
              shape = RoundedCornerShape(12.dp),
              color = if (autoTrim) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
              border = if (autoTrim) BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null,
              modifier = Modifier
                .weight(1f)
                .height(48.dp)
                .clickable { autoTrim = !autoTrim }
                .testTag("auto_trim_button")
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.padding(horizontal = 12.dp)
              ) {
                Icon(
                  Icons.Default.AutoFixHigh,
                  contentDescription = null,
                  tint = if (autoTrim) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = if (autoTrim) "Auto-Trim: ON" else "Auto-Trim: OFF",
                  fontWeight = FontWeight.Bold,
                  fontSize = 13.sp,
                  color = if (autoTrim) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          Text(
            text = "Crop / Page Aspect Ratio",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )

          Spacer(modifier = Modifier.height(8.dp))

          // Crop ratio selector chips
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            RatioChip(
              label = "Original",
              selected = currentRatio == null,
              onClick = { currentRatio = null },
              modifier = Modifier.weight(1f)
            )
            RatioChip(
              label = "A4 Page",
              selected = currentRatio == 0.707f,
              onClick = { currentRatio = 0.707f },
              modifier = Modifier.weight(1f)
            )
            RatioChip(
              label = "1:1 Square",
              selected = currentRatio == 1f,
              onClick = { currentRatio = 1f },
              modifier = Modifier.weight(1f)
            )
            RatioChip(
              label = "4:3 Photo",
              selected = currentRatio == 1.333f,
              onClick = { currentRatio = 1.333f },
              modifier = Modifier.weight(1f)
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(16.dp))
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
        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
        color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
      )
    }
  }
}
