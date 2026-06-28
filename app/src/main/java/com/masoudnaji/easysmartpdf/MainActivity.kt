package com.masoudnaji.easysmartpdf

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.masoudnaji.easysmartpdf.ui.screens.home.HomeScreen
import com.masoudnaji.easysmartpdf.ui.theme.EasySmartPDFTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EasySmartPDFTheme {
                HomeScreen(
                    onCreatePicturesClick = {
                        // Action will be added when navigation/feature is ready
                    }
                )
            }
        }
    }
}
