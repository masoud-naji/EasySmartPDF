package com.masoudnaji.easysmartpdf.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.masoudnaji.easysmartpdf.R
import com.masoudnaji.easysmartpdf.ui.components.AppCard
import com.masoudnaji.easysmartpdf.ui.components.FeatureIllustration
import com.masoudnaji.easysmartpdf.ui.components.PdfOperation
import com.masoudnaji.easysmartpdf.ui.theme.EasySmartPDFTheme
import com.masoudnaji.easysmartpdf.ui.theme.Radius
import com.masoudnaji.easysmartpdf.ui.theme.Spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onCreatePicturesClick: () -> Unit,
    onMergePdfClick: () -> Unit,
    onSplitPdfClick: () -> Unit,
    onImageToPdfClick: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 1.dp
            ) {
                Column {
                    Spacer(Modifier.statusBarsPadding())
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.home_title),
                            style = MaterialTheme.typography.titleLarge.copy(fontSize = 20.sp),
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        IconButton(
                            onClick = onSettingsClick,
                            modifier = Modifier.align(Alignment.CenterEnd)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = stringResource(R.string.settings_title),
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = Spacing.screenPadding)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            Spacer(modifier = Modifier.height(Spacing.sm))

            FeatureCard(
                title = stringResource(R.string.feature_pdf_to_images_title),
                description = stringResource(R.string.feature_pdf_to_images_desc),
                actionText = stringResource(R.string.feature_pdf_to_images_action),
                illustration = { FeatureIllustration(PdfOperation.PDF_TO_IMAGES) },
                onClick = onCreatePicturesClick
            )

            FeatureCard(
                title = stringResource(R.string.feature_merge_pdfs_title),
                description = stringResource(R.string.feature_merge_pdfs_desc),
                actionText = stringResource(R.string.feature_merge_pdfs_action),
                illustration = { FeatureIllustration(PdfOperation.MERGE_PDFS) },
                onClick = onMergePdfClick
            )

            FeatureCard(
                title = stringResource(R.string.feature_split_pdf_title),
                description = stringResource(R.string.feature_split_pdf_desc),
                actionText = stringResource(R.string.feature_split_pdf_action),
                illustration = { FeatureIllustration(PdfOperation.SPLIT_PDF) },
                onClick = onSplitPdfClick
            )

            FeatureCard(
                title = stringResource(R.string.feature_images_to_pdf_title),
                description = stringResource(R.string.feature_images_to_pdf_desc),
                actionText = stringResource(R.string.feature_images_to_pdf_action),
                illustration = { FeatureIllustration(PdfOperation.IMAGES_TO_PDF) },
                onClick = onImageToPdfClick
            )

            Spacer(modifier = Modifier.height(Spacing.xl))
        }
    }
}

@Composable
private fun FeatureCard(
    title: String,
    description: String,
    actionText: String,
    illustration: @Composable () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    AppCard(
        modifier = modifier.clickable(onClick = onClick),
        containerColor = Color.White
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(70.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
        ) {
            Box(
                modifier = Modifier
                    .weight(0.5f)
                    .fillMaxHeight(),
                contentAlignment = Alignment.Center
            ) {
                illustration()
            }

            Column(
                modifier = Modifier.weight(0.32f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 15.sp),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )

                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                    color = MaterialTheme.colorScheme.outline,
                    lineHeight = 13.sp,
                    maxLines = 3
                )
            }

            Box(
                modifier = Modifier
                    .weight(0.18f)
                    .size(60.dp, 28.dp)
                    .clip(RoundedCornerShape(Radius.sm))
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = actionText,
                    color = MaterialTheme.colorScheme.onPrimary,
                    style = MaterialTheme.typography.labelMedium.copy(fontSize = 13.sp),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    EasySmartPDFTheme {
        HomeScreen(
            onCreatePicturesClick = {},
            onMergePdfClick = {},
            onSplitPdfClick = {},
            onImageToPdfClick = {},
            onSettingsClick = {}
        )
    }
}
