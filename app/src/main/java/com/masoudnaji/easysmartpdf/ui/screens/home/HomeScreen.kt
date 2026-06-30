package com.masoudnaji.easysmartpdf.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.automirrored.filled.CallMerge
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.masoudnaji.easysmartpdf.R
import com.masoudnaji.easysmartpdf.ui.components.AppCard
import com.masoudnaji.easysmartpdf.ui.components.PrimaryButton
import com.masoudnaji.easysmartpdf.ui.theme.EasySmartPDFTheme
import com.masoudnaji.easysmartpdf.ui.theme.Spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onCreatePicturesClick: () -> Unit,
    onMergePdfClick: () -> Unit,
    onSplitPdfClick: () -> Unit,
    onImageToPdfClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.home_title),
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = Spacing.screenPadding)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm)
        ) {
            Spacer(modifier = Modifier.height(Spacing.sm))

            FeatureCard(
                title = stringResource(R.string.feature_create_pictures),
                description = stringResource(R.string.feature_create_pictures_desc),
                buttonText = stringResource(R.string.feature_create_pictures),
                icon = Icons.Default.AddPhotoAlternate,
                onClick = onCreatePicturesClick
            )

            FeatureCard(
                title = stringResource(R.string.feature_merge_pdf),
                description = stringResource(R.string.feature_merge_pdf_desc),
                buttonText = stringResource(R.string.feature_merge_pdf),
                icon = Icons.AutoMirrored.Filled.CallMerge,
                onClick = onMergePdfClick
            )

            FeatureCard(
                title = stringResource(R.string.feature_split_pdf),
                description = stringResource(R.string.feature_split_pdf_desc),
                buttonText = stringResource(R.string.feature_split_pdf),
                icon = Icons.Default.ContentCut,
                onClick = onSplitPdfClick
            )

            FeatureCard(
                title = stringResource(R.string.feature_image_to_pdf),
                description = stringResource(R.string.feature_image_to_pdf_desc),
                buttonText = stringResource(R.string.feature_image_to_pdf),
                icon = Icons.Default.Image,
                onClick = onImageToPdfClick
            )
        }
    }
}

@Composable
private fun FeatureCard(
    title: String,
    description: String,
    buttonText: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    AppCard(modifier = modifier) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(28.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(Spacing.xs))

            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(Spacing.xs))

            PrimaryButton(text = buttonText, onClick = onClick)
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
            onImageToPdfClick = {}
        )
    }
}
