package com.wyc.pics.utils

import android.content.Context
import android.net.Uri
import android.os.Environment
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.time.LocalDate
import java.time.format.DateTimeFormatter

object ImageUtils {
    
    private const val DIR_NAME = "pics"
    private const val DATE_FORMAT = "yyyy-MM-dd"
    
    fun getImageDirectory(date: LocalDate): File {
        val downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val dateStr = date.format(DateTimeFormatter.ofPattern(DATE_FORMAT))
        return File(downloadDir, "$DIR_NAME/$dateStr")
    }
    
    fun createImageFile(date: LocalDate): File {
        val dir = getImageDirectory(date)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        val timestamp = System.currentTimeMillis()
        return File(dir, "${timestamp}.png")
    }
    
    fun saveImageToGallery(context: Context, sourceUri: Uri, date: LocalDate): String? {
        return try {
            val inputStream = context.contentResolver.openInputStream(sourceUri)
            inputStream?.use { input ->
                val outputFile = createImageFile(date)
                FileOutputStream(outputFile).use { output ->
                    input.copyTo(output)
                }
                outputFile.absolutePath
            }
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }
    
    fun copyFile(sourcePath: String, targetPath: String): Boolean {
        return try {
            val sourceFile = File(sourcePath)
            val targetFile = File(targetPath)
            
            if (!targetFile.parentFile?.exists()!!) {
                targetFile.parentFile?.mkdirs()
            }
            
            FileInputStream(sourceFile).use { input ->
                FileOutputStream(targetFile).use { output ->
                    input.copyTo(output)
                }
            }
            true
        } catch (e: IOException) {
            e.printStackTrace()
            false
        }
    }
}