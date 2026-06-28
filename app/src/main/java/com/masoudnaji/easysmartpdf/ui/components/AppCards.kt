package com.masoudnaji.easysmartpdf.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.masoudnaji.easysmartpdf.ui.theme.CardDimens
import com.masoudnaji.easysmartpdf.ui.theme.Radius
import com.masoudnaji.easysmartpdf.ui.theme.Spacing

/**
 * Standard Card for EasySmartPDF features and selections.
 */
@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Radius.md),
        elevation = CardDefaults.cardElevation(defaultElevation = CardDimens.Elevation),
        colors = CardDefaults.cardColors()
    ) {
        Column(
            modifier = Modifier.padding(Spacing.md),
            content = content
        )
    }
}
