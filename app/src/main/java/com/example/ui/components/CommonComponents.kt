package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.PdfQuality

/**
 * Large, ultra-accessible high-contrast button designed specifically for elders
 * and non-technical users.
 */
@Composable
fun ElderActionButton(
  text: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  icon: ImageVector? = null,
  isPrimary: Boolean = true,
  isHighContrast: Boolean = false,
  testTag: String = ""
) {
  val containerColor = when {
    isHighContrast && isPrimary -> Color(0xFFB71C1C)
    isHighContrast && !isPrimary -> Color(0xFFF5F5F5)
    isPrimary -> MaterialTheme.colorScheme.primary
    else -> MaterialTheme.colorScheme.surfaceVariant
  }

  val contentColor = when {
    isHighContrast && isPrimary -> Color.White
    isHighContrast && !isPrimary -> Color.Black
    isPrimary -> MaterialTheme.colorScheme.onPrimary
    else -> MaterialTheme.colorScheme.onSurfaceVariant
  }

  val borderStroke = if (isHighContrast) {
    BorderStroke(2.dp, Color.Black)
  } else if (!isPrimary) {
    BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
  } else {
    null
  }

  Button(
    onClick = onClick,
    modifier = modifier
      .fillMaxWidth()
      .height(60.dp)
      .testTag(testTag),
    shape = RoundedCornerShape(16.dp),
    colors = ButtonDefaults.buttonColors(
      containerColor = containerColor,
      contentColor = contentColor
    ),
    border = borderStroke,
    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp)
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.Center
    ) {
      if (icon != null) {
        Icon(
          imageVector = icon,
          contentDescription = null,
          modifier = Modifier.size(28.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
      }
      Text(
        text = text,
        fontSize = if (isHighContrast) 20.sp else 18.sp,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center
      )
    }
  }
}

/**
 * Quality Selection Modal with 2 clear options:
 * 1. Standard Quality: Compressed JPEG (~75% quality, scaled to A4) for quick WhatsApp sending.
 * 2. Original Quality: Minimal compression preserving original resolution.
 */
@Composable
fun QualitySelectionDialog(
  onQualitySelected: (PdfQuality) -> Unit,
  onDismiss: () -> Unit,
  isHighContrast: Boolean = false
) {
  Dialog(onDismissRequest = onDismiss) {
    Surface(
      shape = RoundedCornerShape(24.dp),
      color = MaterialTheme.colorScheme.surface,
      tonalElevation = 8.dp,
      border = if (isHighContrast) BorderStroke(2.dp, Color.Black) else null,
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)
        .testTag("quality_selection_dialog")
    ) {
      Column(
        modifier = Modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Select PDF Quality",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
          )
          IconButton(onClick = onDismiss) {
            Icon(Icons.Default.Close, contentDescription = "Close dialog")
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Option 1: Standard Quality
        QualityOptionCard(
          title = "Standard Quality",
          subtitle = "Compressed for fast sharing on WhatsApp & Email (Recommended)",
          badge = "Small File Size",
          icon = Icons.Default.Speed,
          isRecommended = true,
          onClick = { onQualitySelected(PdfQuality.STANDARD) },
          testTag = "select_standard_quality"
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Option 2: Original Quality
        QualityOptionCard(
          title = "Original Quality",
          subtitle = "Full resolution preserving maximum photo clarity",
          badge = "Larger File",
          icon = Icons.Default.HighQuality,
          isRecommended = false,
          onClick = { onQualitySelected(PdfQuality.ORIGINAL) },
          testTag = "select_original_quality"
        )
      }
    }
  }
}

@Composable
private fun QualityOptionCard(
  title: String,
  subtitle: String,
  badge: String,
  icon: ImageVector,
  isRecommended: Boolean,
  onClick: () -> Unit,
  testTag: String
) {
  Surface(
    onClick = onClick,
    shape = RoundedCornerShape(16.dp),
    color = if (isRecommended) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
    border = if (isRecommended) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
    modifier = Modifier
      .fillMaxWidth()
      .testTag(testTag)
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isRecommended) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(28.dp)
          )
          Spacer(modifier = Modifier.width(10.dp))
          Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = if (isRecommended) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
          )
        }

        Surface(
          shape = RoundedCornerShape(8.dp),
          color = if (isRecommended) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.2f)
        ) {
          Text(
            text = badge,
            style = MaterialTheme.typography.labelSmall,
            color = if (isRecommended) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            fontWeight = FontWeight.SemiBold
          )
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      Text(
        text = subtitle,
        style = MaterialTheme.typography.bodyMedium,
        color = if (isRecommended) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f)
      )
    }
  }
}

/**
 * Loading Spinner / Progress indicator during PDF generation.
 */
@Composable
fun GeneratingProgressDialog(
  currentPage: Int,
  totalPages: Int
) {
  Dialog(
    onDismissRequest = {},
    properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
  ) {
    Surface(
      shape = RoundedCornerShape(24.dp),
      color = MaterialTheme.colorScheme.surface,
      tonalElevation = 8.dp,
      modifier = Modifier
        .fillMaxWidth()
        .padding(20.dp)
        .testTag("generating_progress_dialog")
    ) {
      Column(
        modifier = Modifier.padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        CircularProgressIndicator(
          modifier = Modifier.size(64.dp),
          color = MaterialTheme.colorScheme.primary,
          strokeWidth = 5.dp
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
          text = "Creating your PDF...",
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
          text = "Processing page $currentPage of $totalPages\nPlease wait",
          style = MaterialTheme.typography.bodyLarge,
          textAlign = TextAlign.Center,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    }
  }
}

/**
 * Rename Dialog to edit the PDF filename.
 */
@Composable
fun RenamePdfDialog(
  initialName: String,
  onConfirm: (String) -> Unit,
  onDismiss: () -> Unit
) {
  var text by remember {
    mutableStateOf(initialName.removeSuffix(".pdf"))
  }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Text(
        text = "Rename PDF",
        fontWeight = FontWeight.Bold
      )
    },
    text = {
      Column {
        Text(
          text = "Enter a new name for your document:",
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
          value = text,
          onValueChange = { text = it },
          singleLine = true,
          label = { Text("File name") },
          suffix = { Text(".pdf") },
          modifier = Modifier
            .fillMaxWidth()
            .testTag("rename_text_field")
        )
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (text.isNotBlank()) {
            onConfirm(text.trim())
          }
        },
        modifier = Modifier.testTag("rename_confirm_button")
      ) {
        Text("Save Name")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Cancel")
      }
    }
  )
}
