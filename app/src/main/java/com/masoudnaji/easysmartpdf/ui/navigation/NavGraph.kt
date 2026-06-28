package com.masoudnaji.easysmartpdf.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.masoudnaji.easysmartpdf.ui.screens.home.HomeScreen
import com.masoudnaji.easysmartpdf.ui.screens.pdftoimage.CreatePicturesScreen

object Screen {
    const val Home = "home"
    const val CreatePictures = "create_pictures"
}

@Composable
fun EasySmartNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home,
        modifier = modifier
    ) {
        composable(Screen.Home) {
            HomeScreen(
                onCreatePicturesClick = {
                    navController.navigate(Screen.CreatePictures)
                }
            )
        }
        composable(Screen.CreatePictures) {
            CreatePicturesScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
    }
}
