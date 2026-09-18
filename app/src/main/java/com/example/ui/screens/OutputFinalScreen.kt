package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DriveFileRenameOutline
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DeveloperSettings
import com.example.model.GeneratedPdf
import com.example.ui.components.ElderActionButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OutputFinalScreen(
  pdf: GeneratedPdf,
  settings: DeveloperSettings,
  onOpenPdf: () -> Unit,
  onSharePdf: () -> Unit,
  onRenameClicked: () -> Unit,
  onCreateAnother: () -> Unit
) {
  val context = LocalContext.current
  val sizeKb = (pdf.sizeBytes / 1024).coerceAtLeast(1)

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Text(
            text = "PDF Ready!",
            fontWeight = FontWeight.Bold
          )
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
        .padding(horizontal = 24.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Spacer(modifier = Modifier.height(16.dp))

      // Success Check Badge
      Box(
        modifier = Modifier
          .size(88.dp)
          .clip(CircleShape)
          .background(Color(0xFFE8F5E9)),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Default.CheckCircle,
          contentDescription = null,
          tint = Color(0xFF2E7D32),
          modifier = Modifier.size(56.dp)
        )
      }

      Spacer(modifier = Modifier.height(16.dp))

      Text(
        text = "Your PDF is Created!",
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Black,
        textAlign = TextAlign.Center,
        color = MaterialTheme.colorScheme.onSurface
      )

      Spacer(modifier = Modifier.height(8.dp))

      Text(
        text = "Saved automatically to your device",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )

      Spacer(modifier = Modifier.height(24.dp))

      // Document Info Card
      Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = if (settings.highContrastMode) BorderStroke(2.dp, Color.Black) else BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("pdf_info_card")
      ) {
        Column(modifier = Modifier.padding(20.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.primaryContainer),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.PictureAsPdf,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
              )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = pdf.fileName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
              Text(
                text = "${pdf.pageCount} ${if (pdf.pageCount == 1) "page" else "pages"} • $sizeKb KB",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          // Save location row
          Surface(
            shape = RoundedCornerShape(10.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(
                imageVector = Icons.Default.Folder,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = "Folder: ${settings.defaultSavePath}",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(28.dp))

      // 3 LARGE ACTION BUTTONS AS REQUIRED: Save, Share, Rename
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
      ) {
        // 1. SHARE BUTTON (Sends directly to WhatsApp / Email)
        ElderActionButton(
          text = "Share PDF",
          icon = Icons.Default.Share,
          onClick = onSharePdf,
          isPrimary = true,
          isHighContrast = settings.highContrastMode,
          testTag = "final_share_button"
        )

        // 2. SAVE / VIEW BUTTON (Confirms saved location & opens in viewer)
        ElderActionButton(
          text = "View / Save Document",
          icon = Icons.Default.Visibility,
          onClick = {
            Toast.makeText(
              context,
              "Saved to Documents/${settings.defaultSavePath}",
              Toast.LENGTH_LONG
            ).show()
            onOpenPdf()
          },
          isPrimary = false,
          isHighContrast = settings.highContrastMode,
          testTag = "final_view_save_button"
        )

        // 3. RENAME BUTTON (Opens dialog to change filename)
        ElderActionButton(
          text = "Rename PDF",
          icon = Icons.Default.DriveFileRenameOutline,
          onClick = onRenameClicked,
          isPrimary = false,
          isHighContrast = settings.highContrastMode,
          testTag = "final_rename_button"
        )
      }

      Spacer(modifier = Modifier.weight(1f))

      // Return / Make another PDF button
      OutlinedButton(
        onClick = onCreateAnother,
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
          .fillMaxWidth()
          .defaultMinSize(minHeight = 48.dp)
          .wrapContentHeight()
          .padding(bottom = 16.dp)
          .testTag("create_another_pdf_button")
      ) {
        Icon(Icons.Default.Home, contentDescription = null)
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = "Create Another PDF",
          fontWeight = FontWeight.Bold,
          fontSize = 16.sp
        )
      }
    }
  }
}
