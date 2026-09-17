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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CallSplit
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MergeType
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.example.model.AppLanguage
import com.example.model.DeveloperSettings
import com.example.model.ImagePage
import com.example.util.AppStrings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreviewBuildScreen(
  pages: List<ImagePage>,
  settings: DeveloperSettings,
  listState: LazyListState = rememberLazyListState(),
  onAddMorePhotos: (List<Uri>) -> Unit,
  onMoveUp: (Int) -> Unit,
  onMoveDown: (Int) -> Unit,
  onRemovePage: (Int) -> Unit,
  onMergePages: (Int) -> Unit,
  onUnmergePage: (Int) -> Unit,
  onTapCrop: (String) -> Unit,
  onGeneratePdfClicked: () -> Unit,
  onBackPressed: () -> Unit
) {
  val context = LocalContext.current

  // Gallery Picker for adding more images
  val galleryLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.PickMultipleVisualMedia()
  ) { uris ->
    if (uris.isNotEmpty()) {
      onAddMorePhotos(uris)
    }
  }

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Column {
            Text(
              text = "Arrange PDF Pages",
              fontWeight = FontWeight.Bold,
              fontSize = if (settings.highContrastMode) 22.sp else 18.sp
            )
            Text(
              text = "${pages.size} ${if (pages.size == 1) "Page" else "Pages"} selected",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        },
        navigationIcon = {
          IconButton(onClick = onBackPressed) {
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
    bottomBar = {
      // Bottom Bar with Add More Images & Wide Prominent Generate PDF Button
      Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp,
        border = if (settings.highContrastMode) BorderStroke(2.dp, Color.Black) else null,
        modifier = Modifier
          .fillMaxWidth()
          .navigationBarsPadding()
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            // "+" Add More Images button
            FilledTonalButton(
              onClick = {
                galleryLauncher.launch(
                  PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
              },
              shape = RoundedCornerShape(14.dp),
              modifier = Modifier
                .height(58.dp)
                .testTag("add_more_images_button"),
              colors = ButtonDefaults.filledTonalButtonColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
              )
            ) {
              Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = AppStrings.get(settings.language, "add_more_photos"),
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
              )
            }

            // Wide, prominent "Generate PDF" button
            Button(
              onClick = onGeneratePdfClicked,
              shape = RoundedCornerShape(14.dp),
              colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
              ),
              border = if (settings.highContrastMode) BorderStroke(2.dp, MaterialTheme.colorScheme.outline) else null,
              modifier = Modifier
                .weight(1f)
                .height(58.dp)
                .testTag("generate_pdf_button")
            ) {
              Icon(
                imageVector = Icons.Default.PictureAsPdf,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = AppStrings.get(settings.language, "create_pdf_button"),
                fontWeight = FontWeight.Bold,
                fontSize = if (settings.highContrastMode) 18.sp else 16.sp
              )
            }
          }
        }
      }
    },
    containerColor = MaterialTheme.colorScheme.background
  ) { innerPadding ->
    LazyColumn(
      state = listState,
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
        .padding(horizontal = 16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp),
      contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp)
    ) {
      item(key = "header_instruction_banner") {
        Surface(
          shape = RoundedCornerShape(12.dp),
          color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = Icons.Default.Crop,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
              text = "Tap any photo to rotate or crop. Use arrows to reorder pages.",
              style = MaterialTheme.typography.bodySmall,
              fontWeight = FontWeight.Medium,
              color = MaterialTheme.colorScheme.onPrimaryContainer
            )
          }
        }
      }

      itemsIndexed(pages, key = { _, page -> page.id }) { index, page ->
        PageCardItem(
          pageIndex = index,
          totalPages = pages.size,
          page = page,
          simplifiedMode = settings.simplifiedMode,
          highContrast = settings.highContrastMode,
          language = settings.language,
          onMoveUp = { onMoveUp(index) },
          onMoveDown = { onMoveDown(index) },
          onRemove = { onRemovePage(index) },
          onUnmerge = { onUnmergePage(index) },
          onTapCrop = { onTapCrop(page.id) }
        )

        // Show merge button between this page and the next page when enabled
        if (settings.enableMergePages && index < pages.size - 1) {
          Spacer(modifier = Modifier.height(6.dp))
          MergeBetweenPagesDivider(
            pageNumber1 = index + 1,
            pageNumber2 = index + 2,
            language = settings.language,
            onMerge = { onMergePages(index) }
          )
          Spacer(modifier = Modifier.height(6.dp))
        }
      }
    }
  }
}

@Composable
fun PageCardItem(
  pageIndex: Int,
  totalPages: Int,
  page: ImagePage,
  simplifiedMode: Boolean,
  highContrast: Boolean,
  language: AppLanguage,
  onMoveUp: () -> Unit,
  onMoveDown: () -> Unit,
  onRemove: () -> Unit,
  onUnmerge: () -> Unit,
  onTapCrop: () -> Unit
) {
  val context = LocalContext.current

  Card(
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    border = if (highContrast) BorderStroke(2.dp, Color.Black) else BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
    modifier = Modifier
      .fillMaxWidth()
      .testTag("page_card_$pageIndex")
  ) {
    Column(modifier = Modifier.padding(12.dp)) {
      // Top row: Page number header & delete button & merged indicators
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.primary
          ) {
            Text(
              text = "Page ${pageIndex + 1} of $totalPages",
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.Bold,
              color = Color.White,
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
            )
          }

          if (page.isMerged) {
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = MaterialTheme.colorScheme.tertiaryContainer
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(
                  imageVector = Icons.Default.MergeType,
                  contentDescription = null,
                  tint = MaterialTheme.colorScheme.onTertiaryContainer,
                  modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = AppStrings.get(language, "merged_badge"),
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.onTertiaryContainer
                )
              }
            }
          }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
          if (page.isMerged && page.originalPages != null) {
            TextButton(
              onClick = onUnmerge,
              contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
            ) {
              Icon(
                imageVector = Icons.Default.CallSplit,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = AppStrings.get(language, "unmerge_page_btn"),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold
              )
            }
            Spacer(modifier = Modifier.width(4.dp))
          }

          // Tap to edit / crop label
          Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.clickable(onClick = onTapCrop)
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(
                imageVector = Icons.Default.Crop,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.primary
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = "Crop / Rotate",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }

          Spacer(modifier = Modifier.width(8.dp))

          IconButton(
            onClick = onRemove,
            modifier = Modifier
              .size(32.dp)
              .testTag("delete_page_$pageIndex")
          ) {
            Icon(
              imageVector = Icons.Default.DeleteOutline,
              contentDescription = "Remove page",
              tint = MaterialTheme.colorScheme.error
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Content row: A4-proportioned image preview + Reorder arrows on the side
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Image thumbnail (represents A4 page preview, 1:1.414 aspect ratio)
        Box(
          modifier = Modifier
            .weight(1f)
            .height(210.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFE0E0E0))
            .clickable(onClick = onTapCrop)
            .testTag("tap_image_$pageIndex"),
          contentAlignment = Alignment.Center
        ) {
          AsyncImage(
            model = ImageRequest.Builder(context)
              .data(page.uri)
              .crossfade(true)
              .build(),
            contentDescription = "Page ${pageIndex + 1} preview",
            modifier = Modifier
              .fillMaxSize()
              .rotate(page.rotationDegrees.toFloat()),
            contentScale = ContentScale.Fit
          )

          // Subtle hint overlay
          Surface(
            shape = RoundedCornerShape(6.dp),
            color = Color.Black.copy(alpha = 0.55f),
            modifier = Modifier
              .align(Alignment.BottomEnd)
              .padding(8.dp)
          ) {
            Text(
              text = if (page.rotationDegrees != 0) "Rotated ${page.rotationDegrees}°" else "Tap to edit",
              style = MaterialTheme.typography.labelSmall,
              color = Color.White,
              modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
          }
        }

        // Side Page Ordering Controls: Up (▲) and Down (▼) arrow buttons
        // Hidden when Simplified Mode is enabled in Developer Settings
        if (!simplifiedMode) {
          Spacer(modifier = Modifier.width(12.dp))
          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            // Move Up (▲)
            Surface(
              onClick = onMoveUp,
              enabled = pageIndex > 0,
              shape = CircleShape,
              color = if (pageIndex > 0) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
              border = if (highContrast && pageIndex > 0) BorderStroke(1.5.dp, Color.Black) else null,
              modifier = Modifier
                .size(48.dp)
                .testTag("move_up_$pageIndex")
            ) {
              Box(contentAlignment = Alignment.Center) {
                Icon(
                  imageVector = Icons.Default.KeyboardArrowUp,
                  contentDescription = "Move page up",
                  tint = if (pageIndex > 0) MaterialTheme.colorScheme.onPrimaryContainer else Color.Gray,
                  modifier = Modifier.size(32.dp)
                )
              }
            }

            // Move Down (▼)
            Surface(
              onClick = onMoveDown,
              enabled = pageIndex < totalPages - 1,
              shape = CircleShape,
              color = if (pageIndex < totalPages - 1) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
              border = if (highContrast && pageIndex < totalPages - 1) BorderStroke(1.5.dp, Color.Black) else null,
              modifier = Modifier
                .size(48.dp)
                .testTag("move_down_$pageIndex")
            ) {
              Box(contentAlignment = Alignment.Center) {
                Icon(
                  imageVector = Icons.Default.KeyboardArrowDown,
                  contentDescription = "Move page down",
                  tint = if (pageIndex < totalPages - 1) MaterialTheme.colorScheme.onPrimaryContainer else Color.Gray,
                  modifier = Modifier.size(32.dp)
                )
              }
            }
          }
        }
      }
    }
  }
}

@Composable
fun MergeBetweenPagesDivider(
  pageNumber1: Int,
  pageNumber2: Int,
  language: AppLanguage,
  onMerge: () -> Unit
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 4.dp, horizontal = 8.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.Center
  ) {
    HorizontalDivider(
      modifier = Modifier.weight(1f),
      color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
    )

    Spacer(modifier = Modifier.width(8.dp))

    OutlinedButton(
      onClick = onMerge,
      shape = RoundedCornerShape(20.dp),
      colors = ButtonDefaults.outlinedButtonColors(
        containerColor = MaterialTheme.colorScheme.surface
      ),
      border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
      contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
      modifier = Modifier.testTag("merge_pages_button_${pageNumber1}_$pageNumber2")
    ) {
      Icon(
        imageVector = Icons.Default.MergeType,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.primary,
        modifier = Modifier.size(16.dp)
      )
      Spacer(modifier = Modifier.width(6.dp))
      Text(
        text = "${AppStrings.get(language, "merge_pages_btn")} ($pageNumber1 + $pageNumber2)",
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary
      )
    }

    Spacer(modifier = Modifier.width(8.dp))

    HorizontalDivider(
      modifier = Modifier.weight(1f),
      color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
    )
  }
}
