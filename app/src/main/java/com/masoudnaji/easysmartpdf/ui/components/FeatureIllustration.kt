package com.masoudnaji.easysmartpdf.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.masoudnaji.easysmartpdf.R
import com.masoudnaji.easysmartpdf.ui.theme.EasySmartPDFTheme

/**
 * Manually control every icon size in every card here.
 */
private object IllustrationDefaults {
    // --- CARD 1: PDF TO IMAGES ---
    val Card1_PdfSize = 40.dp
    val Card1_ImagesSize = 40.dp

    // --- CARD 2: MERGE PDFS ---
    val Card2_PdfStackSize = 32.dp  // Individual icons inside the stack
    val Card2_TargetPdfSize = 40.dp

    // --- CARD 3: SPLIT PDF ---
    val Card3_SourcePdfSize = 40.dp
    val Card3_PdfStackSize = 32.dp  // Individual icons inside the stack

    // --- CARD 4: IMAGES TO PDF ---
    val Card4_ImagesSize = 40.dp
    val Card4_PdfSize = 40.dp

    // --- SHARED SETTINGS ---
    val StackOffset = 8.dp
    val ArrowSize = 20.dp
    val SpacingGap = 10.dp

    // Visual normalization (compensates for internal padding in PNGs)
    const val GlobalPdfScale = 1.0f
    const val GlobalImagesScale = 1.05f 
}

enum class PdfOperation {
    PDF_TO_IMAGES,
    MERGE_PDFS,
    SPLIT_PDF,
    IMAGES_TO_PDF
}

@Composable
fun FeatureIllustration(
    operation: PdfOperation,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        when (operation) {
            PdfOperation.PDF_TO_IMAGES -> IllustrationLayout(
                source = { PdfIcon(size = IllustrationDefaults.Card1_PdfSize) },
                target = { ImagesIcon(size = IllustrationDefaults.Card1_ImagesSize) }
            )
            PdfOperation.MERGE_PDFS -> IllustrationLayout(
                source = { PdfStack(size = IllustrationDefaults.Card2_PdfStackSize) },
                target = { PdfIcon(size = IllustrationDefaults.Card2_TargetPdfSize) }
            )
            PdfOperation.SPLIT_PDF -> IllustrationLayout(
                source = { PdfIcon(size = IllustrationDefaults.Card3_SourcePdfSize) },
                target = { PdfStack(size = IllustrationDefaults.Card3_PdfStackSize) }
            )
            PdfOperation.IMAGES_TO_PDF -> IllustrationLayout(
                source = { ImagesIcon(size = IllustrationDefaults.Card4_ImagesSize) },
                target = { PdfIcon(size = IllustrationDefaults.Card4_PdfSize) }
            )
        }
    }
}

@Composable
private fun IllustrationLayout(
    source: @Composable () -> Unit,
    target: @Composable () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(IllustrationDefaults.SpacingGap)
    ) {
        source()
        IllustrationArrow()
        target()
    }
}

@Composable
private fun PdfIcon(
    size: Dp,
    modifier: Modifier = Modifier
) {
    Image(
        painter = painterResource(R.drawable.ic_pdf),
        contentDescription = null,
        modifier = modifier.height(size * IllustrationDefaults.GlobalPdfScale)
    )
}

@Composable
private fun ImagesIcon(
    size: Dp,
    modifier: Modifier = Modifier
) {
    Image(
        painter = painterResource(R.drawable.ic_images),
        contentDescription = null,
        modifier = modifier.height(size * IllustrationDefaults.GlobalImagesScale)
    )
}

@Composable
private fun PdfStack(size: Dp) {
    Box(
        modifier = Modifier.padding(
            bottom = IllustrationDefaults.StackOffset,
            end = IllustrationDefaults.StackOffset
        )
    ) {
        Box(
            modifier = Modifier.offset(
                x = IllustrationDefaults.StackOffset,
                y = IllustrationDefaults.StackOffset
            )
        ) {
            PdfIcon(size = size)
        }
        PdfIcon(size = size)
    }
}

@Composable
private fun IllustrationArrow() {
    Icon(
        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
        contentDescription = null,
        modifier = Modifier.size(IllustrationDefaults.ArrowSize),
        tint = MaterialTheme.colorScheme.outline
    )
}

@Preview(showBackground = true)
@Composable
private fun FeatureIllustrationPreview() {
    EasySmartPDFTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            PdfOperation.entries.forEach { operation ->
                FeatureIllustration(operation = operation)
            }
        }
    }
}
