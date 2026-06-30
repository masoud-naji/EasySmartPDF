package com.masoudnaji.easysmartpdf.ui.screens.imagetopdf

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
fun ImageToPdfProgressScreen(
    imageToPdfEntry: NavBackStackEntry,
    onCreateComplete: (fileName: String) -> Unit,
    onCreateFailed: () -> Unit,
    onCreateCancelled: () -> Unit,
    modifier: Modifier = Modifier
) {
    val viewModel: ImageToPdfViewModel = viewModel(imageToPdfEntry)
    val uiState by viewModel.uiState.collectAsState()
    val state = uiState.createState

    var showCancelDialog by remember { mutableStateOf(false) }

    val isRunning = state is ImageToPdfState.Started || state is ImageToPdfState.InProgress
    BackHandler(enabled = isRunning) { showCancelDialog = true }

    LaunchedEffect(state) {
        when {
            state is ImageToPdfState.Completed -> onCreateComplete(state.fileName)
            state is ImageToPdfState.Cancelled -> onCreateCancelled()
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        if (uiState.errorMessage != null) onCreateFailed()
    }

    val current = (state as? ImageToPdfState.InProgress)?.current ?: 0
    val total = (state as? ImageToPdfState.InProgress)?.total ?: 0
    val progress = if (total > 0) current.toFloat() / total.toFloat() else 0f

    if (showCancelDialog) {
        AlertDialog(
            onDismissRequest = { showCancelDialog = false },
            title = { Text(stringResource(R.string.image_to_pdf_cancel_dialog_title)) },
            text = { Text(stringResource(R.string.cancel_dialog_message)) },
            confirmButton = {
                Button(onClick = { showCancelDialog = false }) {
                    Text(stringResource(R.string.action_continue))
                }
            },
            dismissButton = {
                OutlinedButton(onClick = {
                    showCancelDialog = false
                    viewModel.cancelCreation()
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
                text = if (total > 0)
                    stringResource(R.string.image_to_pdf_progress_creating, current, total)
                else
                    stringResource(R.string.progress_starting),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
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
