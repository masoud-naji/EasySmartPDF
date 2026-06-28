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
import androidx.compose.material.icons.filled.CallMerge
import androidx.compose.material.icons.filled.ContentCut
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
import androidx.compose.ui.tooling.preview.Preview
import com.masoudnaji.easysmartpdf.R
import com.masoudnaji.easysmartpdf.ui.components.AppCard
import com.masoudnaji.easysmartpdf.ui.theme.AppIconSize
import com.masoudnaji.easysmartpdf.ui.theme.EasySmartPDFTheme
import com.masoudnaji.easysmartpdf.ui.theme.Spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onCreatePicturesClick: () -> Unit,
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
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
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
            verticalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            Spacer(modifier = Modifier.height(Spacing.sm))

            // Primary Feature: Create Pictures
            FeatureCard(
                title = stringResource(R.string.feature_create_pictures),
                description = stringResource(R.string.feature_create_pictures_desc),
                icon = Icons.Default.AddPhotoAlternate,
                onClick = onCreatePicturesClick,
                isAvailable = true
            )

            // Future Feature: Merge PDFs
            FeatureCard(
                title = stringResource(R.string.feature_merge_pdf),
                description = stringResource(R.string.coming_soon),
                icon = Icons.Default.CallMerge,
                onClick = {},
                isAvailable = false
            )

            // Future Feature: Split PDF
            FeatureCard(
                title = stringResource(R.string.feature_split_pdf),
                description = stringResource(R.string.coming_soon),
                icon = Icons.Default.ContentCut,
                onClick = {},
                isAvailable = false
            )

            Spacer(modifier = Modifier.height(Spacing.xl))
        }
    }
}

@Composable
private fun FeatureCard(
    title: String,
    description: String,
    icon: ImageVector,
    onClick: () -> Unit,
    isAvailable: Boolean,
    modifier: Modifier = Modifier
) {
    AppCard(
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .padding(Spacing.sm)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(AppIconSize.medium),
                tint = if (isAvailable) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
            )
            
            Spacer(modifier = Modifier.height(Spacing.md))
            
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = if (isAvailable) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline
            )
            
            Text(
                text = description,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.outline,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            
            if (isAvailable) {
                Spacer(modifier = Modifier.height(Spacing.md))
                com.masoudnaji.easysmartpdf.ui.components.PrimaryButton(
                    text = stringResource(R.string.feature_create_pictures),
                    onClick = onClick
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    EasySmartPDFTheme {
        HomeScreen(onCreatePicturesClick = {})
    }
}
