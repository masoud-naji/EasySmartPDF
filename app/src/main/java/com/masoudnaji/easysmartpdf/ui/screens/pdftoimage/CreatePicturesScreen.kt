package com.masoudnaji.easysmartpdf.ui.screens.pdftoimage

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.masoudnaji.easysmartpdf.R
import com.masoudnaji.easysmartpdf.domain.model.ImageQuality
import com.masoudnaji.easysmartpdf.ui.components.AppCard
import com.masoudnaji.easysmartpdf.ui.components.PrimaryButton
import com.masoudnaji.easysmartpdf.ui.components.SecondaryButton
import com.masoudnaji.easysmartpdf.ui.theme.EasySmartPDFTheme
import com.masoudnaji.easysmartpdf.ui.theme.Radius
import com.masoudnaji.easysmartpdf.ui.theme.Spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePicturesScreen(
    onBackClick: () -> Unit,
    onNavigateToProgress: () -> Unit,
    viewModel: CreatePicturesViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    val hasChanges = uiState.pdfInfo != null ||
            uiState.imageQuality != ImageQuality.BALANCED ||
            uiState.pageMode != PageMode.ALL

    var showDiscardDialog by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.errorMessage) {
        val message = uiState.errorMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(message)
        viewModel.onErrorDismissed()
    }

    BackHandler(enabled = hasChanges) {
        showDiscardDialog = true
    }

    val onBack = {
        if (hasChanges) showDiscardDialog = true else onBackClick()
    }

    if (showDiscardDialog) {
        AlertDialog(
            onDismissRequest = { showDiscardDialog = false },
            confirmButton = {
                Button(onClick = { showDiscardDialog = false }) {
                    Text(stringResource(R.string.keep_editing))
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        showDiscardDialog = false
                        onBackClick()
                    }
                ) {
                    Text(stringResource(R.string.discard))
                }
            },
            title = { Text(stringResource(R.string.discard_dialog_title)) },
            text = { Text(stringResource(R.string.discard_dialog_message)) }
        )
    }

    val pdfPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { viewModel.onPdfSelected(it) }
    }

    val isConverting = uiState.conversionState !is ConversionState.Idle
        && uiState.conversionState !is ConversionState.Cancelled

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.create_pictures_title),
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            PrimaryButton(
                text = stringResource(R.string.feature_create_pictures),
                onClick = {
                    viewModel.startConversion()
                    onNavigateToProgress()
                },
                enabled = uiState.pdfInfo != null && !isConverting,
                modifier = Modifier.padding(Spacing.lg)
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

            // Card 1: PDF File
            AppCard {
                Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                    Text(
                        text = stringResource(R.string.pdf_file_card_title),
                        style = MaterialTheme.typography.titleLarge
                    )

                    if (uiState.pdfInfo == null) {
                        Text(
                            text = stringResource(R.string.no_pdf_chosen_yet),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.outline
                        )
                    } else {
                        Column {
                            Text(
                                text = "📄 ${uiState.pdfInfo!!.fileName}",
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = stringResource(R.string.pages_count, uiState.pdfInfo!!.pageCount),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.outline
                            )
                            Text(
                                text = stringResource(R.string.ready_status),
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.tertiary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(Spacing.sm))
                    SecondaryButton(
                        text = stringResource(
                            if (uiState.pdfInfo == null) R.string.choose_pdf else R.string.change_pdf
                        ),
                        onClick = { pdfPickerLauncher.launch(arrayOf("application/pdf")) },
                        enabled = !isConverting
                    )
                }
            }

            // Card 2: Pages
            AppCard {
                Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                    Text(
                        text = stringResource(R.string.pages_card_title),
                        style = MaterialTheme.typography.titleLarge
                    )

                    val maxPages = uiState.pdfInfo?.pageCount ?: 1
                    val showRangeOption = maxPages > 1

                    Column(Modifier.selectableGroup()) {
                        SelectionRow(
                            text = stringResource(R.string.all_pages),
                            selected = uiState.pageMode == PageMode.ALL,
                            onClick = { viewModel.onPageModeChanged(PageMode.ALL) }
                        )
                        if (showRangeOption) {
                            SelectionRow(
                                text = stringResource(R.string.page_range),
                                selected = uiState.pageMode == PageMode.RANGE,
                                onClick = { viewModel.onPageModeChanged(PageMode.RANGE) }
                            )
                        }
                    }

                    if (uiState.pageMode == PageMode.RANGE && showRangeOption) {
                        Column(
                            modifier = Modifier.padding(top = Spacing.sm),
                            verticalArrangement = Arrangement.spacedBy(Spacing.md)
                        ) {
                            PageRangeControl(
                                label = stringResource(R.string.from_page),
                                value = uiState.fromPage,
                                max = maxPages,
                                onValueChange = { viewModel.onFromPageChanged(it) }
                            )
                            PageRangeControl(
                                label = stringResource(R.string.to_page),
                                value = uiState.toPage,
                                max = maxPages,
                                onValueChange = { viewModel.onToPageChanged(it) }
                            )
                        }
                    }

                    if (uiState.pdfInfo != null) {
                        Text(
                            text = stringResource(R.string.pages_available, uiState.pdfInfo!!.pageCount),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.padding(top = Spacing.sm)
                        )
                    }
                }
            }

            // Card 3: Picture Size
            AppCard {
                Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                    Text(
                        text = stringResource(R.string.picture_size_card_title),
                        style = MaterialTheme.typography.titleLarge
                    )

                    Column(Modifier.selectableGroup()) {
                        ImageQuality.entries.forEach { quality ->
                            SelectionRow(
                                text = quality.label(),
                                selected = uiState.imageQuality == quality,
                                onClick = { viewModel.onQualityChanged(quality) }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(Spacing.xl))
        }
    }
}

@Composable
private fun ImageQuality.label(): String = stringResource(
    when (this) {
        ImageQuality.SMALLER -> R.string.size_smaller
        ImageQuality.BALANCED -> R.string.size_balanced
        ImageQuality.BEST -> R.string.size_best
    }
)

@Composable
private fun SelectionRow(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .selectable(
                selected = selected,
                onClick = onClick,
                role = Role.RadioButton
            )
            .padding(vertical = Spacing.md),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = null)
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(start = Spacing.md)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PageRangeControl(
    label: String,
    value: Int,
    max: Int,
    onValueChange: (Int) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = label, style = MaterialTheme.typography.bodyLarge)
            OutlinedTextField(
                value = value.toString(),
                onValueChange = { input ->
                    val parsed = input.filter { it.isDigit() }.toIntOrNull()
                    if (parsed != null) onValueChange(parsed.coerceIn(1, max))
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier
                    .width(96.dp)
                    .height(48.dp),
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold
                ),
                singleLine = true,
                shape = RoundedCornerShape(Radius.sm)
            )
        }
        Slider(
            value = value.toFloat(),
            onValueChange = { onValueChange(it.toInt()) },
            valueRange = 1f..max.toFloat(),
            modifier = Modifier.padding(horizontal = Spacing.xxl),
            track = { sliderState ->
                SliderDefaults.Track(
                    sliderState = sliderState,
                    modifier = Modifier.height(8.dp)
                )
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun CreatePicturesScreenPreview() {
    EasySmartPDFTheme {
        CreatePicturesScreen(onBackClick = {}, onNavigateToProgress = {})
    }
}
