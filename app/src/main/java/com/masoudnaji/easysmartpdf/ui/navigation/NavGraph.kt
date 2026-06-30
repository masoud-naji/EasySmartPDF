package com.masoudnaji.easysmartpdf.ui.navigation

import android.content.ActivityNotFoundException
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.DocumentsContract
import android.provider.MediaStore
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
import com.masoudnaji.easysmartpdf.ui.screens.imagetopdf.ImageToPdfProgressScreen
import com.masoudnaji.easysmartpdf.ui.screens.imagetopdf.ImageToPdfScreen
import com.masoudnaji.easysmartpdf.ui.screens.imagetopdf.ImageToPdfSuccessScreen
import com.masoudnaji.easysmartpdf.ui.screens.merge.MergePdfScreen
import com.masoudnaji.easysmartpdf.ui.screens.merge.MergeProgressScreen
import com.masoudnaji.easysmartpdf.ui.screens.merge.MergeSuccessScreen
import com.masoudnaji.easysmartpdf.ui.screens.pdftoimage.CreatePicturesScreen
import com.masoudnaji.easysmartpdf.ui.screens.progress.ProgressScreen
import com.masoudnaji.easysmartpdf.ui.screens.split.SplitPdfScreen
import com.masoudnaji.easysmartpdf.ui.screens.split.SplitProgressScreen
import com.masoudnaji.easysmartpdf.ui.screens.split.SplitSuccessScreen
import com.masoudnaji.easysmartpdf.ui.screens.success.SuccessScreen

object Screen {
    const val Home = "home"
    const val CreatePictures = "create_pictures"
    const val Progress = "progress"
    const val SuccessRoute = "success/{savedCount}/{folderName}"
    const val MergePdf = "merge_pdf"
    const val MergeProgress = "merge_progress"
    const val MergeSuccessRoute = "merge_success/{fileName}"
    const val SplitPdf = "split_pdf"
    const val SplitProgress = "split_progress"
    const val SplitSuccessRoute = "split_success/{fileCount}/{folderName}"
    const val ImageToPdf = "image_to_pdf"
    const val ImageToPdfProgress = "image_to_pdf_progress"
    const val ImageToPdfSuccessRoute = "image_to_pdf_success/{fileName}"

    fun successDestination(savedCount: Int, folderName: String) =
        "success/$savedCount/${Uri.encode(folderName)}"

    fun mergeSuccessDestination(fileName: String) =
        "merge_success/${Uri.encode(fileName)}"

    fun splitSuccessDestination(fileCount: Int, folderName: String) =
        "split_success/$fileCount/${Uri.encode(folderName)}"

    fun imageToPdfSuccessDestination(fileName: String) =
        "image_to_pdf_success/${Uri.encode(fileName)}"
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
                onCreatePicturesClick = { navController.navigate(Screen.CreatePictures) },
                onMergePdfClick = { navController.navigate(Screen.MergePdf) },
                onSplitPdfClick = { navController.navigate(Screen.SplitPdf) },
                onImageToPdfClick = { navController.navigate(Screen.ImageToPdf) }
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
                onConversionFailed = { navController.popBackStack() },
                onConversionCancelled = { navController.popBackStack() }
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

        // Merge PDF flow
        composable(Screen.MergePdf) {
            MergePdfScreen(
                onBackClick = { navController.popBackStack() },
                onNavigateToProgress = { navController.navigate(Screen.MergeProgress) }
            )
        }

        composable(Screen.MergeProgress) { backStackEntry ->
            val mergePdfEntry = remember(backStackEntry) {
                navController.getBackStackEntry(Screen.MergePdf)
            }
            MergeProgressScreen(
                mergePdfEntry = mergePdfEntry,
                onMergeComplete = { fileName ->
                    navController.navigate(Screen.mergeSuccessDestination(fileName)) {
                        popUpTo(Screen.MergePdf) { inclusive = true }
                    }
                },
                onMergeFailed = { navController.popBackStack() },
                onMergeCancelled = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.MergeSuccessRoute,
            arguments = listOf(
                navArgument("fileName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val fileName = backStackEntry.arguments?.getString("fileName") ?: ""
            val context = LocalContext.current
            MergeSuccessScreen(
                fileName = fileName,
                onOpenFile = { openMergedFile(context, fileName) },
                onBackToHome = {
                    navController.navigate(Screen.Home) {
                        popUpTo(Screen.Home) { inclusive = true }
                    }
                }
            )
        }

        // Split PDF flow
        composable(Screen.SplitPdf) {
            SplitPdfScreen(
                onBackClick = { navController.popBackStack() },
                onNavigateToProgress = { navController.navigate(Screen.SplitProgress) }
            )
        }

        composable(Screen.SplitProgress) { backStackEntry ->
            val splitPdfEntry = remember(backStackEntry) {
                navController.getBackStackEntry(Screen.SplitPdf)
            }
            SplitProgressScreen(
                splitPdfEntry = splitPdfEntry,
                onSplitComplete = { fileCount, folderName ->
                    navController.navigate(Screen.splitSuccessDestination(fileCount, folderName)) {
                        popUpTo(Screen.SplitPdf) { inclusive = true }
                    }
                },
                onSplitFailed = { navController.popBackStack() },
                onSplitCancelled = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.SplitSuccessRoute,
            arguments = listOf(
                navArgument("fileCount") { type = NavType.IntType },
                navArgument("folderName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val fileCount = backStackEntry.arguments?.getInt("fileCount") ?: 0
            val folderName = backStackEntry.arguments?.getString("folderName") ?: ""
            val context = LocalContext.current
            SplitSuccessScreen(
                fileCount = fileCount,
                folderName = folderName,
                onOpenFolder = { openSplitFolder(context, folderName) },
                onBackToHome = {
                    navController.navigate(Screen.Home) {
                        popUpTo(Screen.Home) { inclusive = true }
                    }
                }
            )
        }

        // Image to PDF flow
        composable(Screen.ImageToPdf) {
            ImageToPdfScreen(
                onBackClick = { navController.popBackStack() },
                onNavigateToProgress = { navController.navigate(Screen.ImageToPdfProgress) }
            )
        }

        composable(Screen.ImageToPdfProgress) { backStackEntry ->
            val imageToPdfEntry = remember(backStackEntry) {
                navController.getBackStackEntry(Screen.ImageToPdf)
            }
            ImageToPdfProgressScreen(
                imageToPdfEntry = imageToPdfEntry,
                onCreateComplete = { fileName ->
                    navController.navigate(Screen.imageToPdfSuccessDestination(fileName)) {
                        popUpTo(Screen.ImageToPdf) { inclusive = true }
                    }
                },
                onCreateFailed = { navController.popBackStack() },
                onCreateCancelled = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.ImageToPdfSuccessRoute,
            arguments = listOf(
                navArgument("fileName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val fileName = backStackEntry.arguments?.getString("fileName") ?: ""
            val context = LocalContext.current
            ImageToPdfSuccessScreen(
                fileName = fileName,
                onOpenFile = { openImageToPdfFile(context, fileName) },
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
    try {
        context.startActivity(Intent(Intent.ACTION_VIEW).apply {
            type = "image/*"
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        })
    } catch (_: ActivityNotFoundException) { }
}

private fun openMergedFile(context: Context, fileName: String) {
    val fileUri = queryPdfUri(context, fileName)
    if (fileUri != null) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(fileUri, "application/pdf")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
        }
        try {
            context.startActivity(intent)
            return
        } catch (_: ActivityNotFoundException) { }
    }
    try {
        context.startActivity(Intent(Intent.ACTION_VIEW).apply {
            type = "application/pdf"
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        })
    } catch (_: ActivityNotFoundException) { }
}

private fun openImageToPdfFile(context: Context, fileName: String) {
    val fileUri = queryPdfUri(context, fileName)
    if (fileUri != null) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(fileUri, "application/pdf")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
        }
        try {
            context.startActivity(intent)
            return
        } catch (_: ActivityNotFoundException) { }
    }
    try {
        context.startActivity(Intent(Intent.ACTION_VIEW).apply {
            type = "application/pdf"
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        })
    } catch (_: ActivityNotFoundException) { }
}

private fun openSplitFolder(context: Context, folderName: String) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val docId = if (folderName.isEmpty())
            "primary:Documents/EasySmartPDF/Split"
        else
            "primary:Documents/EasySmartPDF/Split/$folderName"
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
    try {
        context.startActivity(Intent(Intent.ACTION_VIEW).apply {
            type = "application/pdf"
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        })
    } catch (_: ActivityNotFoundException) { }
}

private fun queryPdfUri(context: Context, fileName: String): Uri? {
    val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
        MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL)
    else
        MediaStore.Files.getContentUri("external")
    val projection = arrayOf(MediaStore.Files.FileColumns._ID)
    val selection = "${MediaStore.Files.FileColumns.DISPLAY_NAME} = ?"
    context.contentResolver.query(collection, projection, selection, arrayOf(fileName), null)
        ?.use { cursor ->
            if (cursor.moveToFirst()) {
                val id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID))
                return ContentUris.withAppendedId(collection, id)
            }
        }
    return null
}
