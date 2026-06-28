package com.masoudnaji.easysmartpdf.ui.screens.pdftoimage

import android.content.Context
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.provider.OpenableColumns
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.masoudnaji.easysmartpdf.R
import com.masoudnaji.easysmartpdf.ui.components.AppCard
import com.masoudnaji.easysmartpdf.ui.components.PrimaryButton
import com.masoudnaji.easysmartpdf.ui.components.SecondaryButton
import com.masoudnaji.easysmartpdf.ui.theme.EasySmartPDFTheme
import com.masoudnaji.easysmartpdf.ui.theme.Spacing
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePicturesScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var selectedSize by remember { mutableStateOf(SizeOption.BALANCED) }
    var fileName by remember { mutableStateOf<String?>(null) }
    var pageCount by remember { mutableStateOf<Int?>(null) }

    val pdfPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            try {
                fileName = getFileName(context, it)
                pageCount = getPageCount(context, it)
            } catch (e: Exception) {
                scope.launch {
                    snackbarHostState.showSnackbar(
                        message = context.getString(R.string.error_open_pdf)
                    )
                }
            }
        }
    }

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
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            PrimaryButton(
                text = stringResource(R.string.feature_create_pictures),
                onClick = { /* No action yet */ },
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
                    
                    if (fileName == null) {
                        Text(
                            text = stringResource(R.string.no_pdf_chosen_yet),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.outline
                        )
                    } else {
                        Column {
                            Text(
                                text = fileName!!,
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (pageCount != null) {
                                Text(
                                    text = stringResource(R.string.pages_count, pageCount!!),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                            Text(
                                text = stringResource(R.string.ready_status),
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.tertiary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(Spacing.sm))
                    SecondaryButton(
                        text = stringResource(if (fileName == null) R.string.choose_pdf else R.string.change_file),
                        onClick = { pdfPickerLauncher.launch(arrayOf("application/pdf")) }
                    )
                }
            }

            // Card 2: Picture Size
            AppCard {
                Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                    Text(
                        text = stringResource(R.string.picture_size_card_title),
                        style = MaterialTheme.typography.titleLarge
                    )
                    
                    Column(Modifier.selectableGroup()) {
                        SizeOption.entries.forEach { option ->
                            SizeRadioButton(
                                text = stringResource(option.labelRes),
                                selected = selectedSize == option,
                                onClick = { selectedSize = option }
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
private fun SizeRadioButton(
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
        RadioButton(
            selected = selected,
            onClick = null // handled by selectable Row
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(start = Spacing.md)
        )
    }
}

private fun getFileName(context: Context, uri: Uri): String? {
    var name: String? = null
    context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
        if (cursor.moveToFirst()) {
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (nameIndex != -1) {
                name = cursor.getString(nameIndex)
            }
        }
    }
    return name ?: uri.path?.substringAfterLast('/')
}

private fun getPageCount(context: Context, uri: Uri): Int {
    return try {
        val pfd = context.contentResolver.openFileDescriptor(uri, "r")
        if (pfd != null) {
            val renderer = PdfRenderer(pfd)
            val count = renderer.pageCount
            renderer.close()
            pfd.close()
            count
        } else 0
    } catch (e: Exception) {
        0
    }
}

private enum class SizeOption(val labelRes: Int) {
    SMALLER(R.string.size_smaller),
    BALANCED(R.string.size_balanced),
    BEST(R.string.size_best)
}

@Preview(showBackground = true)
@Composable
fun CreatePicturesScreenPreview() {
    EasySmartPDFTheme {
        CreatePicturesScreen(onBackClick = {})
    }
}
