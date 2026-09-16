package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessibilityNew
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Compress
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DeveloperSettings
import com.example.model.PageMarginOption
import com.example.model.PdfQuality

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HiddenSettingsScreen(
  currentSettings: DeveloperSettings,
  onUpdateSettings: (DeveloperSettings) -> Unit,
  onBackPressed: () -> Unit
) {
  var showEditFolderDialog by remember { mutableStateOf(false) }
  var tempFolderName by remember { mutableStateOf(currentSettings.defaultSavePath) }

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Column {
            Text(
              text = "Developer & Admin Settings",
              fontWeight = FontWeight.Bold,
              fontSize = 18.sp
            )
            Text(
              text = "Pre-configure app for relatives & elders",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        },
        navigationIcon = {
          IconButton(onClick = onBackPressed) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = MaterialTheme.colorScheme.background
        )
      )
    },
    containerColor = MaterialTheme.colorScheme.background
  ) { innerPadding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
        .verticalScroll(rememberScrollState())
        .padding(horizontal = 20.dp, vertical = 12.dp),
      verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
      // Explanatory Banner
      Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier.padding(14.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(
            imageVector = Icons.Default.Settings,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
          )
          Spacer(modifier = Modifier.width(12.dp))
          Text(
            text = "These settings allow tech-savvy family members to customize the experience per relative's comfort level.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer
          )
        }
      }

      // Feature 1: Default PDF Compression Toggle
      SettingsCard(
        title = "1. Default PDF Compression",
        icon = Icons.Default.Compress,
        subtitle = "Set default quality or prompt on each generation"
      ) {
        PdfQuality.entries.forEach { quality ->
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clickable {
                onUpdateSettings(currentSettings.copy(defaultQuality = quality))
              }
              .padding(vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            RadioButton(
              selected = currentSettings.defaultQuality == quality,
              onClick = { onUpdateSettings(currentSettings.copy(defaultQuality = quality)) }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
              Text(
                text = quality.displayName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
              )
              Text(
                text = quality.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
        }
      }

      // Feature 2: Page Margin Control (Photo Ratio)
      SettingsCard(
        title = "2. Page Margin Control",
        icon = Icons.Default.Crop,
        subtitle = "Prevent document text and edges from getting clipped"
      ) {
        PageMarginOption.entries.forEach { option ->
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clickable {
                onUpdateSettings(currentSettings.copy(pageMargin = option))
              }
              .padding(vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            RadioButton(
              selected = currentSettings.pageMargin == option,
              onClick = { onUpdateSettings(currentSettings.copy(pageMargin = option)) }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
              Text(
                text = option.displayName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
              )
              Text(
                text = option.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
        }
      }

      // Feature 3: Auto-Crop on Import
      SettingsCard(
        title = "3. Auto-Crop on Import",
        icon = Icons.Default.AutoFixHigh,
        subtitle = "Automatically trim dark borders or tables when photos are loaded"
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = "Auto-detect & trim dark borders",
              style = MaterialTheme.typography.bodyMedium,
              fontWeight = FontWeight.SemiBold
            )
            Text(
              text = "Applies smart border detection on imported photos",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
          Switch(
            checked = currentSettings.autoCropOnImport,
            onCheckedChange = { checked ->
              onUpdateSettings(currentSettings.copy(autoCropOnImport = checked))
            }
          )
        }
      }

      // Feature 4: Simplified Mode (Hide Reorder Arrows)
      SettingsCard(
        title = "4. Simplified Mode for Elders",
        icon = Icons.Default.SwapVert,
        subtitle = "Hide up/down reorder arrows to prevent elder confusion"
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = "Hide Reorder Arrows",
              style = MaterialTheme.typography.bodyMedium,
              fontWeight = FontWeight.SemiBold
            )
            Text(
              text = "Keeps only essential buttons on page cards",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
          Switch(
            checked = currentSettings.simplifiedMode,
            onCheckedChange = { checked ->
              onUpdateSettings(currentSettings.copy(simplifiedMode = checked))
            }
          )
        }
      }

      // Feature 5: Default Save Location Path
      SettingsCard(
        title = "5. Default Save Location Path",
        icon = Icons.Default.Folder,
        subtitle = "Target output folder in device Documents"
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = "Documents/${currentSettings.defaultSavePath}",
              style = MaterialTheme.typography.bodyMedium,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.primary
            )
            Text(
              text = "Tap change to customize the folder name",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
          TextButton(
            onClick = {
              tempFolderName = currentSettings.defaultSavePath
              showEditFolderDialog = true
            }
          ) {
            Text("Change")
          }
        }
      }

      // Feature 6: Dark Mode / Contrast Booster
      SettingsCard(
        title = "6. Contrast Booster for Elders",
        icon = Icons.Default.AccessibilityNew,
        subtitle = "Increase button contrast and text size for elders with lower vision"
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = "High Contrast Mode",
              style = MaterialTheme.typography.bodyMedium,
              fontWeight = FontWeight.SemiBold
            )
            Text(
              text = "Deep black borders, larger text, high visibility buttons",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
          Switch(
            checked = currentSettings.highContrastMode,
            onCheckedChange = { checked ->
              onUpdateSettings(currentSettings.copy(highContrastMode = checked))
            }
          )
        }
      }

      // About & Privacy Statement Card
      SettingsCard(
        title = "About & Privacy",
        icon = Icons.Default.Security,
        subtitle = "Zero data collection, zero telemetry, 100% open source"
      ) {
        Column(
          verticalArrangement = Arrangement.spacedBy(12.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          // Privacy Banner
          Surface(
            shape = RoundedCornerShape(10.dp),
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(
              modifier = Modifier.padding(12.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(
                imageVector = Icons.Default.VerifiedUser,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
              )
              Spacer(modifier = Modifier.width(10.dp))
              Text(
                text = "Privacy Guarantee: This app does not collect, record, or share any personal data, images, or documents.",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
              )
            }
          }

          // Bullet points / Details
          Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            AboutPoint(
              icon = Icons.Default.Lock,
              title = "No Trackers or Telemetry",
              description = "There are zero analytics SDKs, zero background trackers, and zero telemetry probes in this app."
            )

            AboutPoint(
              icon = Icons.Default.Shield,
              title = "No Internet Access Required",
              description = "The app does not declare or use the Android INTERNET permission. It runs 100% locally and offline on your device."
            )

            AboutPoint(
              icon = Icons.Default.Info,
              title = "Open Source PDF & Compression",
              description = "All image processing, JPEG compression, and PDF generation use native Android Open Source Project (AOSP) libraries (PdfDocument and Skia engine)."
            )
          }

          HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "pdf_maker • Version 1.0",
              style = MaterialTheme.typography.labelMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
              text = "Apache 2.0 / AOSP",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.primary,
              fontWeight = FontWeight.Bold
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(24.dp))
    }
  }

  // Edit Save Folder Name Dialog
  if (showEditFolderDialog) {
    AlertDialog(
      onDismissRequest = { showEditFolderDialog = false },
      title = { Text("Change Save Folder") },
      text = {
        Column {
          Text(
            text = "Enter the subfolder name inside Documents:",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Spacer(modifier = Modifier.height(10.dp))
          OutlinedTextField(
            value = tempFolderName,
            onValueChange = { tempFolderName = it },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
          )
        }
      },
      confirmButton = {
        TextButton(
          onClick = {
            if (tempFolderName.isNotBlank()) {
              onUpdateSettings(currentSettings.copy(defaultSavePath = tempFolderName.trim()))
            }
            showEditFolderDialog = false
          }
        ) {
          Text("Save")
        }
      },
      dismissButton = {
        TextButton(onClick = { showEditFolderDialog = false }) {
          Text("Cancel")
        }
      }
    )
  }
}

@Composable
private fun SettingsCard(
  title: String,
  icon: ImageVector,
  subtitle: String,
  content: @Composable () -> Unit
) {
  Card(
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
    modifier = Modifier.fillMaxWidth()
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          imageVector = icon,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.primary,
          modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column {
          Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
          )
          Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }

      Spacer(modifier = Modifier.height(12.dp))
      HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
      Spacer(modifier = Modifier.height(12.dp))

      content()
    }
  }
}

@Composable
private fun AboutPoint(
  icon: ImageVector,
  title: String,
  description: String
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 4.dp),
    verticalAlignment = Alignment.Top
  ) {
    Box(
      modifier = Modifier
        .size(32.dp)
        .padding(top = 2.dp),
      contentAlignment = Alignment.Center
    ) {
      Icon(
        imageVector = icon,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.primary,
        modifier = Modifier.size(20.dp)
      )
    }
    Spacer(modifier = Modifier.width(10.dp))
    Column {
      Text(
        text = title,
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface
      )
      Spacer(modifier = Modifier.height(2.dp))
      Text(
        text = description,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
    }
  }
}

