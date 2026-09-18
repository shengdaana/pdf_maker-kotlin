package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Compress
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.MergeType
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AppLanguage
import com.example.model.AppTheme
import com.example.model.DeveloperSettings
import com.example.model.PageLayoutMode
import com.example.model.PdfQuality
import com.example.ui.theme.CobaltPrimary
import com.example.ui.theme.ElderYellowAccent
import com.example.ui.theme.GreenPrimary
import com.example.ui.theme.PinkPrimary
import com.example.ui.theme.PurplePrimary
import com.example.util.AppStrings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HiddenSettingsScreen(
  currentSettings: DeveloperSettings,
  onUpdateSettings: (DeveloperSettings) -> Unit,
  onBack: () -> Unit
) {
  var showEditFolderDialog by remember { mutableStateOf(false) }
  var tempFolderName by remember { mutableStateOf(currentSettings.defaultSavePath) }

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Text(
            text = AppStrings.get(currentSettings.language, "settings_title"),
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleLarge
          )
        },
        navigationIcon = {
          IconButton(
            onClick = onBack,
            modifier = Modifier.testTag("settings_back_button")
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = "Back"
            )
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
        .padding(horizontal = 18.dp)
        .verticalScroll(rememberScrollState()),
      verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
      // Explanatory Banner
      Surface(
        shape = RoundedCornerShape(14.dp),
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
            text = AppStrings.get(currentSettings.language, "settings_subtitle"),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer
          )
        }
      }

      // 1. THEMES PICKER (DROPDOWN)
      SettingsCard(
        title = "1. " + AppStrings.get(currentSettings.language, "theme_title"),
        icon = Icons.Default.ColorLens,
        subtitle = "Choose from modern color styles & dark mode"
      ) {
        SettingsDropdownSelector(
          label = AppStrings.get(currentSettings.language, "theme_title"),
          options = AppTheme.entries,
          selectedOption = currentSettings.theme,
          optionTitle = { it.displayName },
          leadingContent = { theme ->
            val colorPreview = when (theme) {
              AppTheme.PURPLE -> PurplePrimary
              AppTheme.DARK -> Color(0xFF1E293B)
              AppTheme.LIGHT_GREEN -> GreenPrimary
              AppTheme.PINK -> PinkPrimary
              AppTheme.COBALT_BLUE -> CobaltPrimary
              AppTheme.HIGH_CONTRAST_DARK -> ElderYellowAccent
            }
            Box(
              modifier = Modifier
                .size(20.dp)
                .clip(CircleShape)
                .background(colorPreview)
            )
          },
          onOptionSelected = { theme ->
            onUpdateSettings(currentSettings.copy(theme = theme))
          }
        )
      }

      // 2. LANGUAGE PICKER (DROPDOWN)
      SettingsCard(
        title = "2. " + AppStrings.get(currentSettings.language, "language_title"),
        icon = Icons.Default.Language,
        subtitle = "English, Everyday Hindi, and Hinglish"
      ) {
        SettingsDropdownSelector(
          label = AppStrings.get(currentSettings.language, "language_title"),
          options = AppLanguage.entries,
          selectedOption = currentSettings.language,
          optionTitle = { it.displayName },
          leadingContent = {
            Icon(
              imageVector = Icons.Default.Language,
              contentDescription = null,
              modifier = Modifier.size(20.dp),
              tint = MaterialTheme.colorScheme.primary
            )
          },
          onOptionSelected = { lang ->
            onUpdateSettings(currentSettings.copy(language = lang))
          }
        )
      }

      // 3. PDF PAGE SIZING & CANVAS LAYOUT (DROPDOWN)
      SettingsCard(
        title = "3. " + AppStrings.get(currentSettings.language, "page_layout_title"),
        icon = Icons.Default.Crop,
        subtitle = "Choose how photos map to PDF pages"
      ) {
        SettingsDropdownSelector(
          label = AppStrings.get(currentSettings.language, "page_layout_title"),
          options = PageLayoutMode.entries,
          selectedOption = currentSettings.pageLayoutMode,
          optionTitle = { it.displayName },
          optionSubtitle = { it.description },
          onOptionSelected = { mode ->
            onUpdateSettings(currentSettings.copy(pageLayoutMode = mode, pageMargin = mode))
          }
        )
      }

      // 4. DEFAULT PDF QUALITY PRESET (DROPDOWN)
      SettingsCard(
        title = "4. " + AppStrings.get(currentSettings.language, "compression_title"),
        icon = Icons.Default.Compress,
        subtitle = "Standard Quality downsamples to ~1920px for light PDF weight"
      ) {
        SettingsDropdownSelector(
          label = AppStrings.get(currentSettings.language, "compression_title"),
          options = PdfQuality.entries,
          selectedOption = currentSettings.defaultQuality,
          optionTitle = { it.displayName },
          optionSubtitle = { it.description },
          leadingContent = {
            Icon(
              imageVector = Icons.Default.Compress,
              contentDescription = null,
              modifier = Modifier.size(20.dp),
              tint = MaterialTheme.colorScheme.primary
            )
          },
          onOptionSelected = { quality ->
            onUpdateSettings(currentSettings.copy(defaultQuality = quality))
          }
        )
      }

      // 5. SIMPLIFIED MODE & PAGE CONTROLS (WITH MERGE PAGES TOGGLE)
      SettingsCard(
        title = "5. Simplified Mode & Controls",
        icon = Icons.Default.SwapVert,
        subtitle = "Configure page controls and preview layout"
      ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
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
                text = "Fewer buttons on cards for a cleaner experience",
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

          HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

          // Toggle for Merge Pages Between Pages (Default: OFF)
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = AppStrings.get(currentSettings.language, "merge_pages_toggle_title"),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
              )
              Text(
                text = AppStrings.get(currentSettings.language, "merge_pages_toggle_desc"),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
            Switch(
              checked = currentSettings.enableMergePages,
              onCheckedChange = { checked ->
                onUpdateSettings(currentSettings.copy(enableMergePages = checked))
              }
            )
          }
        }
      }

      // 6. DEFAULT SAVE LOCATION PATH
      SettingsCard(
        title = "6. " + AppStrings.get(currentSettings.language, "save_folder_title"),
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

      // Privacy & Security Statement Card
      SettingsCard(
        title = "Privacy & Security",
        icon = Icons.Default.Security,
        subtitle = "Zero data collection, zero telemetry, 100% offline"
      ) {
        Column(
          verticalArrangement = Arrangement.spacedBy(12.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
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
                text = "pdf_maker runs 100% offline. No analytics, no accounts, and no internet access required.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(28.dp))
    }
  }

  // Edit Folder Dialog
  if (showEditFolderDialog) {
    AlertDialog(
      onDismissRequest = { showEditFolderDialog = false },
      title = { Text("Set Save Folder Name") },
      text = {
        Column {
          Text(
            text = "PDF files will be saved in your phone's Documents folder under this subfolder.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Spacer(modifier = Modifier.height(14.dp))
          OutlinedTextField(
            value = tempFolderName,
            onValueChange = { tempFolderName = it },
            label = { Text("Folder Name") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
          )
        }
      },
      confirmButton = {
        TextButton(
          onClick = {
            val clean = tempFolderName.trim()
            if (clean.isNotBlank()) {
              onUpdateSettings(currentSettings.copy(defaultSavePath = clean))
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
    modifier = Modifier.fillMaxWidth()
  ) {
    Column(modifier = Modifier.padding(18.dp)) {
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
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
          )
          Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }

      Spacer(modifier = Modifier.height(12.dp))
      HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
      Spacer(modifier = Modifier.height(12.dp))

      content()
    }
  }
}

@Composable
private fun <T> SettingsDropdownSelector(
  label: String,
  options: List<T>,
  selectedOption: T,
  optionTitle: (T) -> String,
  optionSubtitle: ((T) -> String)? = null,
  leadingContent: (@Composable (T) -> Unit)? = null,
  onOptionSelected: (T) -> Unit
) {
  var expanded by remember { mutableStateOf(false) }

  Box(modifier = Modifier.fillMaxWidth()) {
    Surface(
      shape = RoundedCornerShape(12.dp),
      border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
      color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
      modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(12.dp))
        .clickable { expanded = true }
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        leadingContent?.let {
          it(selectedOption)
          Spacer(modifier = Modifier.width(12.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold
          )
          Text(
            text = optionTitle(selectedOption),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
          )
          optionSubtitle?.let { sub ->
            val subtitleText = sub(selectedOption)
            if (subtitleText.isNotBlank()) {
              Text(
                text = subtitleText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
        }
        Icon(
          imageVector = if (expanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
          contentDescription = "Select $label",
          tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    }

    DropdownMenu(
      expanded = expanded,
      onDismissRequest = { expanded = false },
      modifier = Modifier
        .fillMaxWidth(0.85f)
        .background(MaterialTheme.colorScheme.surface)
    ) {
      options.forEach { option ->
        val isSelected = option == selectedOption
        DropdownMenuItem(
          text = {
            Column {
              Text(
                text = optionTitle(option),
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
              )
              optionSubtitle?.let { sub ->
                val s = sub(option)
                if (s.isNotBlank()) {
                  Text(
                    text = s,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                }
              }
            }
          },
          leadingIcon = leadingContent?.let {
            { it(option) }
          },
          trailingIcon = if (isSelected) {
            {
              Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
              )
            }
          } else null,
          onClick = {
            onOptionSelected(option)
            expanded = false
          }
        )
      }
    }
  }
}
