package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
  settings: DeveloperSettings,
  recentPdfs: List<GeneratedPdf>,
  onPhotosSelected: (List<Uri>) -> Unit,
  onSeeGeneratedPdfs: () -> Unit,
  onOpenSettings: () -> Unit,
  onOpenPdf: (GeneratedPdf) -> Unit,
  onSharePdf: (GeneratedPdf) -> Unit
) {
  val context = LocalContext.current

  // Gallery Picker constrained strictly to system photo/image picker (image/*)
  val galleryLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.PickMultipleVisualMedia()
  ) { uris ->
    if (uris.isNotEmpty()) {
      onPhotosSelected(uris)
    }
  }

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.primary),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = "PDF",
                color = Color.White,
                fontWeight = FontWeight.Black,
                fontSize = 13.sp
              )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(
              text = "pdf_maker",
              fontWeight = FontWeight.Bold,
              fontSize = if (settings.highContrastMode) 24.sp else 20.sp
            )
          }
        },
        actions = {
          // Discreet small settings gear icon in the top-right corner
          IconButton(
            onClick = onOpenSettings,
            modifier = Modifier.testTag("home_settings_button")
          ) {
            Icon(
              imageVector = Icons.Default.Settings,
              contentDescription = "Developer and Family Admin Settings",
              tint = MaterialTheme.colorScheme.onSurfaceVariant
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
        .padding(horizontal = 24.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Spacer(modifier = Modifier.height(24.dp))

      // Friendly hero banner for elders
      Surface(
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        border = if (settings.highContrastMode) BorderStroke(2.dp, Color.Black) else null,
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(
          modifier = Modifier.padding(24.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Box(
            modifier = Modifier
              .size(80.dp)
              .clip(CircleShape)
              .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.PhotoLibrary,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(44.dp)
            )
          }

          Spacer(modifier = Modifier.height(16.dp))

          Text(
            text = "Convert Photos to PDF",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface
          )

          Spacer(modifier = Modifier.height(8.dp))

          Text(
            text = "Select any photos from your phone gallery to create a clean A4 PDF file to send on WhatsApp or save.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }

      Spacer(modifier = Modifier.height(28.dp))

      // PRIMARY ACTION BUTTON: Select From Gallery
      ElderActionButton(
        text = "Select From Gallery",
        icon = Icons.Default.PhotoLibrary,
        onClick = {
          galleryLauncher.launch(
            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
          )
        },
        isPrimary = true,
        isHighContrast = settings.highContrastMode,
        testTag = "select_from_gallery_button"
      )

      Spacer(modifier = Modifier.height(14.dp))

      // SECONDARY ACTION BUTTON: See Generated PDFs
      ElderActionButton(
        text = "See Generated PDFs",
        icon = Icons.Default.Description,
        onClick = onSeeGeneratedPdfs,
        isPrimary = false,
        isHighContrast = settings.highContrastMode,
        testTag = "see_generated_pdfs_button"
      )

      Spacer(modifier = Modifier.height(28.dp))

      // Section: Recent Created PDFs
      if (recentPdfs.isNotEmpty()) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.FolderOpen,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "Recent PDFs (${recentPdfs.size})",
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface
            )
          }

          TextButton(
            onClick = onSeeGeneratedPdfs,
            modifier = Modifier.testTag("home_view_all_pdfs_button")
          ) {
            Text(
              text = "View All",
              fontWeight = FontWeight.Bold,
              style = MaterialTheme.typography.labelLarge
            )
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
          modifier = Modifier.fillMaxWidth().weight(1f),
          verticalArrangement = Arrangement.spacedBy(10.dp),
          contentPadding = PaddingValues(bottom = 24.dp)
        ) {
          items(recentPdfs) { pdf ->
            RecentPdfItemCard(
              pdf = pdf,
              highContrast = settings.highContrastMode,
              onOpen = { onOpenPdf(pdf) },
              onShare = { onSharePdf(pdf) }
            )
          }
        }
      } else {
        Spacer(modifier = Modifier.weight(1f))
        Text(
          text = "Tip: You can also select photos directly in your Gallery and tap 'Share' -> 'pdf_maker'!",
          style = MaterialTheme.typography.bodySmall,
          textAlign = TextAlign.Center,
          color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
          modifier = Modifier.padding(bottom = 24.dp)
        )
      }
    }
  }
}

@Composable
fun RecentPdfItemCard(
  pdf: GeneratedPdf,
  highContrast: Boolean,
  onOpen: () -> Unit,
  onShare: () -> Unit
) {
  val dateStr = SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.getDefault())
    .format(Date(pdf.timestamp))
  val sizeKb = (pdf.sizeBytes / 1024).coerceAtLeast(1)

  Surface(
    shape = RoundedCornerShape(16.dp),
    color = MaterialTheme.colorScheme.surface,
    border = if (highContrast) BorderStroke(1.5.dp, Color.Black) else BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
    modifier = Modifier
      .fillMaxWidth()
      .clickable(onClick = onOpen)
      .testTag("recent_pdf_${pdf.fileName}")
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Box(
        modifier = Modifier
          .size(44.dp)
          .clip(RoundedCornerShape(10.dp))
          .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Default.Description,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.primary,
          modifier = Modifier.size(24.dp)
        )
      }

      Spacer(modifier = Modifier.width(14.dp))

      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = pdf.fileName,
          style = MaterialTheme.typography.bodyLarge,
          fontWeight = FontWeight.Bold,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
          color = MaterialTheme.colorScheme.onSurface
        )
        Text(
          text = "$dateStr • ${sizeKb} KB",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }

      IconButton(
        onClick = onShare,
        modifier = Modifier.testTag("share_recent_${pdf.fileName}")
      ) {
        Icon(
          imageVector = Icons.Default.Share,
          contentDescription = "Share PDF",
          tint = MaterialTheme.colorScheme.primary
        )
      }
    }
  }
}
