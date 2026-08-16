package com.masoudnaji.easysmartpdf.ui.screens.scanner

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import com.masoudnaji.easysmartpdf.R
import com.masoudnaji.easysmartpdf.ui.components.SecondaryButton
import com.masoudnaji.easysmartpdf.ui.theme.Spacing

@Composable
fun ScannerProgressScreen(
    scannerEntry: NavBackStackEntry,
    onScanComplete: (fileName: String) -> Unit,
    onScanFailed: () -> Unit,
    onScanCancelled: () -> Unit,
    modifier: Modifier = Modifier
) {
    val vm: ScannerViewModel = viewModel(scannerEntry)
    val uiState by vm.uiState.collectAsState()
    val state = uiState.scanState

    var showCancelDialog by remember { mutableStateOf(false) }

    val isRunning = state is ScannerScanState.Started || state is ScannerScanState.InProgress
    BackHandler(enabled = isRunning) { showCancelDialog = true }

    LaunchedEffect(state) {
        when {
            state is ScannerScanState.Completed -> onScanComplete(state.fileName)
            state is ScannerScanState.Cancelled -> onScanCancelled()
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        if (uiState.errorMessage != null) onScanFailed()
    }

    val current = (state as? ScannerScanState.InProgress)?.current ?: 0
    val total = (state as? ScannerScanState.InProgress)?.total ?: 0
    val progress = if (total > 0) current.toFloat() / total.toFloat() else 0f
    val hasPages = total > 0

    if (showCancelDialog) {
        AlertDialog(
            onDismissRequest = { showCancelDialog = false },
            title = { Text(stringResource(R.string.scanner_cancel_dialog_title)) },
            text = { Text(stringResource(R.string.cancel_dialog_message)) },
            confirmButton = {
                Button(onClick = { showCancelDialog = false }) {
                    Text(stringResource(R.string.action_continue))
                }
            },
            dismissButton = {
                OutlinedButton(onClick = {
                    showCancelDialog = false
                    vm.cancelScan()
                }) {
                    Text(stringResource(R.string.action_stop))
                }
            }
        )
    }

    Scaffold(modifier = modifier) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(Spacing.screenPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = if (hasPages)
                    stringResource(R.string.merge_progress_page_of, current, total)
                else
                    stringResource(R.string.progress_starting),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(Spacing.sm))

            Text(
                text = if (hasPages)
                    stringResource(R.string.scanner_progress_processing, current)
                else
                    "",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.outline
            )

            Spacer(modifier = Modifier.height(Spacing.lg))

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(Spacing.xl))

            SecondaryButton(
                text = stringResource(R.string.cancel),
                onClick = { showCancelDialog = true },
                enabled = isRunning
            )
        }
    }
}
