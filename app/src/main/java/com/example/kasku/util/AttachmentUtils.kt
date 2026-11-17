package com.example.kasku.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import java.io.File
import java.io.FileOutputStream

object AttachmentUtils {

    fun copyUriToInternalStorage(context: Context, uri: Uri): Pair<String, String>? {
        return try {
            val contentResolver = context.contentResolver
            val mimeType = contentResolver.getType(uri)
            val extension = when {
                mimeType?.contains("pdf", ignoreCase = true) == true -> "pdf"
                mimeType?.contains("png", ignoreCase = true) == true -> "png"
                mimeType?.contains("jpeg", ignoreCase = true) == true || mimeType?.contains("jpg", ignoreCase = true) == true -> "jpg"
                else -> "bin"
            }

            val dir = File(context.filesDir, "lampiran_kasku")
            if (!dir.exists()) {
                dir.mkdirs()
            }

            val fileName = "Lampiran_${System.currentTimeMillis()}.$extension"
            val destFile = File(dir, fileName)

            contentResolver.openInputStream(uri)?.use { inputStream ->
                FileOutputStream(destFile).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }

            Pair(destFile.absolutePath, fileName)
        } catch (e: Exception) {
            Log.e("AttachmentUtils", "Error copying file: ${e.message}", e)
            null
        }
    }

    fun deletePhysicalFile(filePath: String) {
        if (filePath.isBlank()) return
        try {
            val file = File(filePath)
            if (file.exists()) {
                file.delete()
            }
        } catch (e: Exception) {
            Log.e("AttachmentUtils", "Error deleting file: ${e.message}", e)
        }
    }

    fun loadSampledBitmap(filePath: String, reqWidth: Int = 400, reqHeight: Int = 400): Bitmap? {
        if (filePath.isBlank()) return null
        return try {
            val file = File(filePath)
            if (!file.exists()) return null

            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeFile(filePath, options)

            var inSampleSize = 1
            if (options.outHeight > reqHeight || options.outWidth > reqWidth) {
                val halfHeight = options.outHeight / 2
                val halfWidth = options.outWidth / 2
                while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                    inSampleSize *= 2
                }
            }

            val decodeOptions = BitmapFactory.Options().apply {
                this.inSampleSize = inSampleSize
            }
            BitmapFactory.decodeFile(filePath, decodeOptions)
        } catch (e: Exception) {
            Log.e("AttachmentUtils", "Error decoding bitmap: ${e.message}", e)
            null
        }
    }
}