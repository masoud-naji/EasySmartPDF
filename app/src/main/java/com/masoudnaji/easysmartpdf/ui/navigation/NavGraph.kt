package com.masoudnaji.easysmartpdf.ui.navigation

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.DocumentsContract
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.masoudnaji.easysmartpdf.ui.screens.home.HomeScreen
import com.masoudnaji.easysmartpdf.ui.screens.pdftoimage.CreatePicturesScreen
import com.masoudnaji.easysmartpdf.ui.screens.progress.ProgressScreen
import com.masoudnaji.easysmartpdf.ui.screens.success.SuccessScreen

object Screen {
    const val Home = "home"
    const val CreatePictures = "create_pictures"
    const val Progress = "progress"
    const val SuccessRoute = "success/{savedCount}/{folderName}"

    fun successDestination(savedCount: Int, folderName: String) =
        "success/$savedCount/${Uri.encode(folderName)}"
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
                onBackClick = { navController.popBackStack() },
                onNavigateToProgress = { navController.navigate(Screen.Progress) }
            )
        }

        composable(Screen.Progress) { backStackEntry ->
            val createPicturesEntry = remember(backStackEntry) {
                navController.getBackStackEntry(Screen.CreatePictures)
            }
            ProgressScreen(
                createPicturesEntry = createPicturesEntry,
                onConversionComplete = { savedCount, folderName ->
                    navController.navigate(Screen.successDestination(savedCount, folderName)) {
                        popUpTo(Screen.CreatePictures) { inclusive = true }
                    }
                },
                onConversionFailed = {
                    navController.popBackStack()
                },
                onConversionCancelled = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Screen.SuccessRoute,
            arguments = listOf(
                navArgument("savedCount") { type = NavType.IntType },
                navArgument("folderName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val savedCount = backStackEntry.arguments?.getInt("savedCount") ?: 0
            val folderName = backStackEntry.arguments?.getString("folderName") ?: ""
            val context = LocalContext.current
            SuccessScreen(
                savedCount = savedCount,
                folderName = folderName,
                onOpenFolder = { openFolder(context, folderName) },
                onBackToHome = {
                    navController.navigate(Screen.Home) {
                        popUpTo(Screen.Home) { inclusive = true }
                    }
                }
            )
        }
    }
}

private fun openFolder(context: Context, folderName: String) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val docId = "primary:Pictures/EasySmartPDF/$folderName"
        val uri = DocumentsContract.buildDocumentUri("com.android.externalstorage.documents", docId)
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, DocumentsContract.Document.MIME_TYPE_DIR)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        try {
            context.startActivity(intent)
            return
        } catch (_: ActivityNotFoundException) { }
    }
    // Fallback: open the device gallery
    try {
        context.startActivity(
            Intent(Intent.ACTION_VIEW).apply {
                type = "image/*"
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
        )
    } catch (_: ActivityNotFoundException) { }
}
