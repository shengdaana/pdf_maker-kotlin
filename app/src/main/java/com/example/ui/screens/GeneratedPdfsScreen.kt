package com.example.ui.screens

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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DriveFileRenameOutline
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DeveloperSettings
import com.example.model.GeneratedPdf
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GeneratedPdfsScreen(
  pdfs: List<GeneratedPdf>,
  settings: DeveloperSettings,
  onOpenPdf: (GeneratedPdf) -> Unit,
  onSharePdf: (GeneratedPdf) -> Unit,
  onRenamePdf: (GeneratedPdf, String) -> Unit,
  onDeletePdf: (GeneratedPdf) -> Unit,
  onBackPressed: () -> Unit,
  onSelectPhotos: () -> Unit
) {
  var pdfToRename by remember { mutableStateOf<GeneratedPdf?>(null) }
  var pdfToDelete by remember { mutableStateOf<GeneratedPdf?>(null) }

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Column {
            Text(
              text = "Generated PDFs",
              fontWeight = FontWeight.Bold,
              fontSize = if (settings.highContrastMode) 22.sp else 19.sp
            )
            Text(
              text = "${pdfs.size} ${if (pdfs.size == 1) "document" else "documents"} in Documents/${settings.defaultSavePath}",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
          }
        },
        navigationIcon = {
          IconButton(
            onClick = onBackPressed,
            modifier = Modifier.testTag("back_from_generated_pdfs")
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
    if (pdfs.isEmpty()) {
      // Empty state
      Box(
        modifier = Modifier
          .fillMaxSize()
          .padding(innerPadding)
          .padding(horizontal = 32.dp),
        contentAlignment = Alignment.Center
      ) {
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.Center
        ) {
          Box(
            modifier = Modifier
              .size(96.dp)
              .clip(CircleShape)
              .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.FolderOpen,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(52.dp)
            )
          }

          Spacer(modifier = Modifier.height(20.dp))

          Text(
            text = "No PDFs Created Yet",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface
          )

          Spacer(modifier = Modifier.height(8.dp))

          Text(
            text = "All PDFs created with pdf_maker will automatically appear here and stay saved in your Documents folder.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )

          Spacer(modifier = Modifier.height(28.dp))

          Button(
            onClick = onSelectPhotos,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
              .fillMaxWidth()
              .height(56.dp)
              .testTag("empty_state_create_pdf_button")
          ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "Create Your First PDF",
              fontWeight = FontWeight.Bold,
              fontSize = 16.sp
            )
          }
        }
      }
    } else {
      // List of generated PDFs
      LazyColumn(
        modifier = Modifier
          .fillMaxSize()
          .padding(innerPadding)
          .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp)
      ) {
        items(pdfs, key = { it.file.absolutePath }) { pdf ->
          GeneratedPdfCard(
            pdf = pdf,
            highContrast = settings.highContrastMode,
            onOpen = { onOpenPdf(pdf) },
            onShare = { onSharePdf(pdf) },
            onRename = { pdfToRename = pdf },
            onDelete = { pdfToDelete = pdf }
          )
        }
      }
    }
  }

  // Rename Dialog
  if (pdfToRename != null) {
    val targetPdf = pdfToRename!!
    var newName by remember {
      mutableStateOf(targetPdf.fileName.removeSuffix(".pdf"))
    }

    AlertDialog(
      onDismissRequest = { pdfToRename = null },
      title = {
        Text(
          text = "Rename PDF",
          fontWeight = FontWeight.Bold
        )
      },
      text = {
        Column {
          Text(
            text = "Enter a new name for this PDF:",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Spacer(modifier = Modifier.height(12.dp))
          OutlinedTextField(
            value = newName,
            onValueChange = { newName = it },
            singleLine = true,
            suffix = { Text(".pdf") },
            modifier = Modifier
              .fillMaxWidth()
              .testTag("rename_pdf_input")
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            if (newName.isNotBlank()) {
              onRenamePdf(targetPdf, newName.trim())
            }
            pdfToRename = null
          },
          modifier = Modifier.testTag("rename_pdf_confirm")
        ) {
          Text("Save")
        }
      },
      dismissButton = {
        TextButton(onClick = { pdfToRename = null }) {
          Text("Cancel")
        }
      }
    )
  }

  // Delete Confirmation Dialog
  if (pdfToDelete != null) {
    val targetPdf = pdfToDelete!!
    AlertDialog(
      onDismissRequest = { pdfToDelete = null },
      icon = {
        Icon(
          imageVector = Icons.Default.DeleteOutline,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.error,
          modifier = Modifier.size(32.dp)
        )
      },
      title = {
        Text(
          text = "Delete PDF?",
          fontWeight = FontWeight.Bold
        )
      },
      text = {
        Text(
          text = "Are you sure you want to permanently delete \"${targetPdf.fileName}\"?",
          style = MaterialTheme.typography.bodyMedium
        )
      },
      confirmButton = {
        Button(
          onClick = {
            onDeletePdf(targetPdf)
            pdfToDelete = null
          },
          colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
          modifier = Modifier.testTag("delete_pdf_confirm")
        ) {
          Text("Delete", color = Color.White)
        }
      },
      dismissButton = {
        TextButton(onClick = { pdfToDelete = null }) {
          Text("Cancel")
        }
      }
    )
  }
}

@Composable
fun GeneratedPdfCard(
  pdf: GeneratedPdf,
  highContrast: Boolean,
  onOpen: () -> Unit,
  onShare: () -> Unit,
  onRename: () -> Unit,
  onDelete: () -> Unit
) {
  val dateStr = SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.getDefault())
    .format(Date(pdf.timestamp))
  val sizeText = if (pdf.sizeBytes >= 1024 * 1024) {
    String.format(Locale.getDefault(), "%.1f MB", pdf.sizeBytes / (1024f * 1024f))
  } else {
    "${(pdf.sizeBytes / 1024).coerceAtLeast(1)} KB"
  }

  Card(
    shape = RoundedCornerShape(18.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    border = if (highContrast) BorderStroke(2.dp, Color.Black) else BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
    modifier = Modifier
      .fillMaxWidth()
      .testTag("generated_pdf_card_${pdf.fileName}")
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      // Header: PDF Icon + Name + Date
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clickable(onClick = onOpen),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Box(
          modifier = Modifier
            .size(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
              if (highContrast) Color(0xFFB71C1C) else MaterialTheme.colorScheme.primaryContainer
            ),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.PictureAsPdf,
            contentDescription = null,
            tint = if (highContrast) Color.White else MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(28.dp)
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
          Spacer(modifier = Modifier.height(2.dp))
          Text(
            text = "$dateStr • $sizeText",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Action row: Open/View, Share, Rename, Delete
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        // Open Button
        Button(
          onClick = onOpen,
          shape = RoundedCornerShape(10.dp),
          colors = ButtonDefaults.buttonColors(
            containerColor = if (highContrast) Color.Black else MaterialTheme.colorScheme.primary
          ),
          modifier = Modifier
            .weight(1f)
            .height(42.dp)
            .testTag("open_pdf_${pdf.fileName}")
        ) {
          Icon(
            imageVector = Icons.Default.Visibility,
            contentDescription = null,
            modifier = Modifier.size(18.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text("Open", fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }

        // Share Button
        Surface(
          onClick = onShare,
          shape = RoundedCornerShape(10.dp),
          color = MaterialTheme.colorScheme.surfaceVariant,
          modifier = Modifier
            .height(42.dp)
            .testTag("share_pdf_${pdf.fileName}")
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = Icons.Default.Share,
              contentDescription = "Share",
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = "Share",
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.SemiBold,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }

        // Rename Button
        IconButton(
          onClick = onRename,
          modifier = Modifier
            .size(42.dp)
            .testTag("rename_pdf_action_${pdf.fileName}")
        ) {
          Icon(
            imageVector = Icons.Default.DriveFileRenameOutline,
            contentDescription = "Rename",
            tint = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }

        // Delete Button
        IconButton(
          onClick = onDelete,
          modifier = Modifier
            .size(42.dp)
            .testTag("delete_pdf_action_${pdf.fileName}")
        ) {
          Icon(
            imageVector = Icons.Default.DeleteOutline,
            contentDescription = "Delete",
            tint = MaterialTheme.colorScheme.error
          )
        }
      }
    }
  }
}
