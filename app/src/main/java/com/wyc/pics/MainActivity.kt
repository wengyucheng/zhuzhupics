package com.wyc.pics

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.MediaStore
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.wyc.pics.ui.components.CalendarView
import com.wyc.pics.ui.screens.DetailScreen
import com.wyc.pics.ui.theme.PicsTheme
import com.wyc.pics.utils.ImageUtils
import java.io.File
import java.time.LocalDate

sealed class Screen {
    object Calendar : Screen()
    data class Detail(val date: LocalDate) : Screen()
}

class MainActivity : ComponentActivity() {
    private lateinit var cameraLauncher: ActivityResultLauncher<Intent>
    private lateinit var galleryLauncher: ActivityResultLauncher<Intent>
    private var tempPhotoFile: File? = null
    private var currentDate: LocalDate? = null
    var onImageSaved: ((String?) -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                tempPhotoFile?.let { file ->
                    onImageSaved?.invoke(file.absolutePath)
                }
            } else {
                onImageSaved?.invoke(null)
            }
            tempPhotoFile = null
        }

        galleryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                result.data?.data?.let { uri ->
                    currentDate?.let { date ->
                        val savedPath = ImageUtils.saveImageToGallery(this, uri, date)
                        onImageSaved?.invoke(savedPath)
                    }
                }
            } else {
                onImageSaved?.invoke(null)
            }
        }

        setContent {
            PicsTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    AppContent(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }

    fun launchCamera(date: LocalDate) {
        currentDate = date
        val permissions = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            arrayOf(Manifest.permission.CAMERA)
        } else {
            arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }

        if (permissions.all {
                ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
            }) {
            startCamera(date)
        } else {
            requestPermissions(permissions, REQUEST_CAMERA_PERMISSION)
        }
    }

    fun launchGallery(date: LocalDate) {
        currentDate = date
        val permission = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
            startGallery()
        } else {
            requestPermissions(arrayOf(permission), REQUEST_GALLERY_PERMISSION)
        }
    }

    private fun startCamera(date: LocalDate) {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (intent.resolveActivity(packageManager) != null) {
            tempPhotoFile = ImageUtils.createImageFile(date)
            val photoUri = FileProvider.getUriForFile(
                this,
                "$packageName.fileprovider",
                tempPhotoFile!!
            )
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
            cameraLauncher.launch(intent)
        }
    }

    private fun startGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryLauncher.launch(intent)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String?>,
        grantResults: IntArray,
        deviceId: Int
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults, deviceId)
        when (requestCode) {
            REQUEST_CAMERA_PERMISSION -> {
                if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                    currentDate?.let { startCamera(it) }
                } else {
                    onImageSaved?.invoke(null)
                }
            }
            REQUEST_GALLERY_PERMISSION -> {
                if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                    startGallery()
                } else {
                    onImageSaved?.invoke(null)
                }
            }
        }
    }

    companion object {
        const val REQUEST_CAMERA_PERMISSION = 1001
        const val REQUEST_GALLERY_PERMISSION = 1002
    }
}

@Composable
fun AppContent(
    modifier: Modifier = Modifier
) {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Calendar) }
    val activity = LocalContext.current as MainActivity

    when (currentScreen) {
        is Screen.Calendar -> {
            CalendarView(
                modifier = modifier.fillMaxSize(),
                onDayClick = { date ->
                    currentScreen = Screen.Detail(date)
                }
            )
        }
        is Screen.Detail -> {
            val date = (currentScreen as Screen.Detail).date
            DetailScreen(
                date = date,
                onBack = {
                    currentScreen = Screen.Calendar
                },
                onAddPhoto = { takePhoto ->
                    if (takePhoto) {
                        activity.launchCamera(date)
                    } else {
                        activity.launchGallery(date)
                    }
                }
            )
        }
    }
}