package com.wyc.pics.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import com.wyc.pics.ui.components.ImagePreview
import com.wyc.pics.utils.ImageUtils
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    date: LocalDate,
    onBack: () -> Unit,
    onAddPhoto: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val showDialog = remember { mutableStateOf(false) }
    val images = remember { mutableStateListOf<String>() }
    val previewIndex = remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(date) {
        loadImagesForDate(context, date, images)
    }

    DisposableEffect(Unit) {
        val callback: (String?) -> Unit = { path ->
            if (path != null) {
                images.add(path)
            }
        }
        (context as? com.wyc.pics.MainActivity)?.onImageSaved = callback
        onDispose {
            (context as? com.wyc.pics.MainActivity)?.onImageSaved = null
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "${date.year}年${date.monthValue}月${date.dayOfMonth}日") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
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
        Column(modifier = Modifier.padding(padding)) {
            if (images.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "暂无照片")
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    modifier = Modifier.fillMaxSize(),
                    content = {
                        itemsIndexed(images) { index, imagePath ->
                            Image(
                                painter = rememberImagePainter(imagePath),
                                contentDescription = null,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(1f)
                                    .clickable {
                                        previewIndex.value = index
                                    }
                            )
                        }
                    }
                )
            }
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
                            onAddPhoto(true)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "拍照")
                    }
                    TextButton(
                        onClick = {
                            showDialog.value = false
                            onAddPhoto(false)
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