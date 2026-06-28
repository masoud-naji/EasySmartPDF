package com.masoudnaji.easysmartpdf

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.masoudnaji.easysmartpdf.ui.navigation.EasySmartNavHost
import com.masoudnaji.easysmartpdf.ui.theme.EasySmartPDFTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EasySmartPDFTheme {
                val navController = rememberNavController()
                EasySmartNavHost(navController = navController)
            }
        }
    }
}
