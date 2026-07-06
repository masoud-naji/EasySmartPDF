package com.masoudnaji.easysmartpdf

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.compose.rememberNavController
import com.masoudnaji.easysmartpdf.ui.navigation.EasySmartNavHost
import com.masoudnaji.easysmartpdf.ui.screens.settings.SettingsViewModel
import com.masoudnaji.easysmartpdf.ui.theme.EasySmartPDFTheme

class MainActivity : AppCompatActivity() {

    private val settingsViewModel: SettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val prefs by settingsViewModel.preferences.collectAsState()
            EasySmartPDFTheme(appTheme = prefs.theme) {
                val navController = rememberNavController()
                EasySmartNavHost(
                    navController = navController,
                    settingsViewModel = settingsViewModel
                )
            }
        }
    }
}
