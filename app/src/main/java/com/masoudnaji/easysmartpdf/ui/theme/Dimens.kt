package com.masoudnaji.easysmartpdf.ui.theme

import androidx.compose.ui.unit.dp

object Spacing {
    val none = 0.dp
    val xs = 4.dp
    val sm = 8.dp
    val md = 16.dp
    val lg = 24.dp
    val xl = 32.dp
    val xxl = 48.dp
    
    // Standard screen margins
    val screenPadding = lg
}

object Radius {
    val sm = 8.dp
    val md = 16.dp
    val lg = 24.dp
    val xl = 32.dp
}

object AppIconSize {
    val small = 24.dp
    val medium = 48.dp
    val large = 72.dp
}

object ButtonDimens {
    val Height = 64.dp
    val IconSize = 28.dp
}

object AppAnimation {
    // Keep animations under 300ms as per project rules
    val short = 150
    val medium = 250
    val long = 300
    
    // Preferred transitions: Fade and Scale
    // Avoid: Bounce and distracting motions
}

object CardDimens {
    val Elevation = 2.dp
}
