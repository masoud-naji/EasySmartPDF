package com.masoudnaji.easysmartpdf.ui.components

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.masoudnaji.easysmartpdf.ui.theme.ButtonDimens
import com.masoudnaji.easysmartpdf.ui.theme.Radius

/**
 * Primary Button for EasySmartPDF.
 * Height: 64dp, Rounded corners, Full width by default.
 */
@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(ButtonDimens.Height),
        shape = RoundedCornerShape(Radius.md),
        enabled = enabled,
        elevation = ButtonDefaults.buttonElevation()
    ) {
        Text(text = text)
    }
}

/**
 * Secondary Button for EasySmartPDF.
 * Outlined, Height: 64dp.
 */
@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(ButtonDimens.Height),
        shape = RoundedCornerShape(Radius.md),
        enabled = enabled
    ) {
        Text(text = text)
    }
}

/**
 * Text Button for EasySmartPDF.
 * Used for low-priority actions like "Done" or "Dismiss".
 */
@Composable
fun AppTextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    TextButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled
    ) {
        Text(text = text)
    }
}
