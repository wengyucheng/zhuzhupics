package com.wyc.pics

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import coil.compose.rememberImagePainter
import com.wyc.pics.ui.components.ImagePreview
import com.wyc.pics.utils.ImageUtils
import android.os.Environment
import java.io.File
import java.time.LocalDate

class DetailActivity : ComponentActivity() {
    companion object {
        const val EXTRA_YEAR = "year"
        const val EXTRA_MONTH = "month"
        const val EXTRA_DAY = "day"
        const val REQUEST_CAMERA_PERMISSION = 1001
        const val REQUEST_GALLERY_PERMISSION = 1002
        const val REQUEST_STORAGE_PERMISSION = 1003

        fun start(context: android.content.Context, date: LocalDate) {
            val intent = Intent(context, DetailActivity::class.java).apply {
                putExtra(EXTRA_YEAR, date.year)
                putExtra(EXTRA_MONTH, date.monthValue)
                putExtra(EXTRA_DAY, date.dayOfMonth)
            }
            context.startActivity(intent)
        }
    }

    private lateinit var cameraLauncher: ActivityResultLauncher<Intent>
    private lateinit var galleryLauncher: ActivityResultLauncher<Intent>
    private var tempPhotoFile: File? = null
    private var currentDate: LocalDate? = null
    var onImageSaved: ((String?) -> Unit)? = null
    private var hasStoragePermission = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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

        checkStoragePermission()
    }

    private fun checkStoragePermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            if (Environment.isExternalStorageManager()) {
                hasStoragePermission = true
                showContent()
            } else {
                try {
                    val intent = Intent(android.provider.Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                    intent.data = android.net.Uri.parse("package:$packageName")
                    startActivityForResult(intent, REQUEST_STORAGE_PERMISSION)
                } catch (e: Exception) {
                    val intent = Intent(android.provider.Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                    startActivityForResult(intent, REQUEST_STORAGE_PERMISSION)
                }
            }
        } else {
            val permissions = arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            if (permissions.all {
                    ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
                }) {
                hasStoragePermission = true
                showContent()
            } else {
                requestPermissions(permissions, REQUEST_STORAGE_PERMISSION)
            }
        }
    }

    private fun showContent() {
        setContent {
            val year = intent.getIntExtra(EXTRA_YEAR, LocalDate.now().year)
            val month = intent.getIntExtra(EXTRA_MONTH, LocalDate.now().monthValue)
            val day = intent.getIntExtra(EXTRA_DAY, LocalDate.now().dayOfMonth)
            val date = LocalDate.of(year, month, day)

            DetailScreenContent(date)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_STORAGE_PERMISSION) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                if (Environment.isExternalStorageManager()) {
                    hasStoragePermission = true
                    showContent()
                } else {
                    finish()
                }
            }
        }
    }

    fun takePhoto() {
        currentDate = LocalDate.of(
            intent.getIntExtra(EXTRA_YEAR, LocalDate.now().year),
            intent.getIntExtra(EXTRA_MONTH, LocalDate.now().monthValue),
            intent.getIntExtra(EXTRA_DAY, LocalDate.now().dayOfMonth)
        )
        val permissions = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            arrayOf(Manifest.permission.CAMERA)
        } else {
            arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }

        if (permissions.all {
                ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
            }) {
            startCamera()
        } else {
            requestPermissions(permissions, REQUEST_CAMERA_PERMISSION)
        }
    }

    fun pickPhoto() {
        currentDate = LocalDate.of(
            intent.getIntExtra(EXTRA_YEAR, LocalDate.now().year),
            intent.getIntExtra(EXTRA_MONTH, LocalDate.now().monthValue),
            intent.getIntExtra(EXTRA_DAY, LocalDate.now().dayOfMonth)
        )
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

    private fun startCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (intent.resolveActivity(packageManager) != null) {
            currentDate?.let { date ->
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
                    startCamera()
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
            REQUEST_STORAGE_PERMISSION -> {
                if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                    hasStoragePermission = true
                    showContent()
                } else {
                    finish()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreenContent(date: LocalDate) {
    val context = LocalContext.current
    val showDialog = remember { mutableStateOf(false) }
    val images = remember { mutableStateListOf<String>() }
    val previewIndex = remember { mutableStateOf<Int?>(null) }
    val noteText = remember { mutableStateOf("") }
    val keyboardShown = remember { mutableStateOf(false) }

    LaunchedEffect(date) {
        loadImagesForDate(context, date, images)
        loadBookmark(context, date, noteText)
    }

    DisposableEffect(Unit) {
        val callback: (String?) -> Unit = { path ->
            if (path != null) {
                images.add(path)
            }
        }
        (context as? DetailActivity)?.onImageSaved = callback
        onDispose {
            (context as? DetailActivity)?.onImageSaved = null
            if (keyboardShown.value) {
                saveBookmark(context, date, noteText.value)
            }
        }
    }

    val view = LocalView.current
    DisposableEffect(view) {
        val listener = android.view.ViewTreeObserver.OnGlobalLayoutListener {
            val r = android.graphics.Rect()
            view.getWindowVisibleDisplayFrame(r)
            val screenHeight = view.rootView.height
            val keypadHeight = screenHeight - r.bottom
            if (keypadHeight > screenHeight * 0.15) {
                keyboardShown.value = true
            } else if (keyboardShown.value) {
                saveBookmark(context, date, noteText.value)
            }
        }
        view.viewTreeObserver.addOnGlobalLayoutListener(listener)
        onDispose {
            view.viewTreeObserver.removeOnGlobalLayoutListener(listener)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "${date.year}年${date.monthValue}月${date.dayOfMonth}日") },
                navigationIcon = {
                    IconButton(onClick = { (context as? DetailActivity)?.finish() }) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(onClick = { showDialog.value = true }) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "添加")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier
            .padding(padding)
            .verticalScroll(rememberScrollState())) {
                if (images.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "暂无照片")
                    }
                } else {
                    Column {
                        for (rowIndex in images.indices step 4) {
                            Row(modifier = Modifier.fillMaxWidth()) {
                                for (colIndex in 0 until 4) {
                                    val itemIndex = rowIndex + colIndex
                                    if (itemIndex < images.size) {
                                        Image(
                                            painter = rememberImagePainter(images[itemIndex]),
                                            contentDescription = null,
                                            modifier = Modifier
                                                .weight(1f)
                                                .aspectRatio(1f)
                                                .clickable {
                                                    previewIndex.value = itemIndex
                                                }
                                        )
                                    } else {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }
                }
                
                BasicTextField(
                    value = noteText.value,
                    onValueChange = { noteText.value = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    decorationBox = { innerTextField ->
                        Box {
                            if (noteText.value.isEmpty()) {
                                Text(
                                    text = "添加备注",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                )
                            }
                            innerTextField()
                        }
                    }
                )
            }
    }

    if (showDialog.value) {
        AlertDialog(
            onDismissRequest = { showDialog.value = false },
            title = { Text(text = "选择图片来源") },
            text = {
                Column {
                    TextButton(
                        onClick = {
                            showDialog.value = false
                            (context as? DetailActivity)?.takePhoto()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "拍照")
                    }
                    TextButton(
                        onClick = {
                            showDialog.value = false
                            (context as? DetailActivity)?.pickPhoto()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "相册")
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDialog.value = false }) {
                    Text(text = "取消")
                }
            }
        )
    }

    previewIndex.value?.let { index ->
        ImagePreview(
            imagePaths = images,
            initialIndex = index,
            onClose = {
                previewIndex.value = null
            }
        )
    }
}

private fun loadImagesForDate(context: android.content.Context, date: LocalDate, images: MutableList<String>) {
    images.clear()
    val dir = ImageUtils.getImageDirectory(date)
    if (dir.exists()) {
        dir.listFiles()?.filter { it.extension.equals("png", ignoreCase = true) }?.forEach {
            images.add(it.absolutePath)
        }
    }
}

private fun saveBookmark(context: android.content.Context, date: LocalDate, text: String) {
    Log.e("wyc", "saveBookmark->date:" + date.toString())
    Log.e("wyc", "saveBookmark->text:" + text)
    try {
        val picsDir = ImageUtils.getImageDirectory(date)
        if (!picsDir.exists()) {
            val created = picsDir.mkdirs()
        }
        val file = File(picsDir, "bookmark.txt")
        Log.e("wyc", "saveBookmark->file:" + file.absolutePath)
        val fos = java.io.FileOutputStream(file)
        fos.write(text.toByteArray())
        fos.flush()
        fos.close()
        Log.e("wyc", "saveBookmark->写入成功")
    } catch (e: Exception) {
        Log.e("wyc","写入失败"+e.message)
        e.printStackTrace()
    }
}

private fun loadBookmark(context: android.content.Context, date: LocalDate, noteText: androidx.compose.runtime.MutableState<String>) {
    try {
        val picsDir = ImageUtils.getImageDirectory(date)
        val file = File(picsDir, "bookmark.txt")
        Log.e("wyc", "loadBookmark->file:" + file.absolutePath)
        if (file.exists() && file.canRead()) {
            noteText.value = file.bufferedReader().readText()
            Log.e("wyc", "loadBookmark->读取成功:" + noteText.value)
        } else {
            Log.e("wyc", "loadBookmark->文件不存在或不可读")
        }
    } catch (e: Exception) {
        Log.e("wyc","读取失败"+e.message)
        e.printStackTrace()
    }
}