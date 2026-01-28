package com.example.soanalyzer.utils

import android.content.Context
import android.os.Build
import android.os.Environment
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

data class FileItem(
    val name: String,
    val path: String,
    val isDirectory: Boolean,
    val size: Long,
    val modifiedDate: Long,
    val extension: String = ""
)

class FileSystemManager(private val context: Context) {

    fun hasFullStorageAccess(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else {
            true
        }
    }

    fun listDirectory(path: String): List<FileItem> {
        val directory = File(path)
        if (!directory.exists() || !directory.isDirectory) {
            return emptyList()
        }

        return try {
            directory.listFiles()?.map { file ->
                FileItem(
                    name = file.name,
                    path = file.absolutePath,
                    isDirectory = file.isDirectory,
                    size = file.length(),
                    modifiedDate = file.lastModified(),
                    extension = if (file.isFile) {
                        file.extension.lowercase()
                    } else {
                        ""
                    }
                )
            }?.sortedWith(compareBy<FileItem> { !it.isDirectory }.thenBy { it.name })
                ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun getFileSize(bytes: Long): String {
        return when {
            bytes <= 0 -> "0 B"
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> String.format("%.2f KB", bytes / 1024.0)
            bytes < 1024 * 1024 * 1024 -> String.format("%.2f MB", bytes / (1024.0 * 1024))
            else -> String.format("%.2f GB", bytes / (1024.0 * 1024 * 1024))
        }
    }

    fun getFormattedDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    fun readFileAsBytes(path: String): ByteArray? {
        return try {
            File(path).readBytes()
        } catch (e: Exception) {
            null
        }
    }

    fun isSOFile(path: String): Boolean {
        return path.endsWith(".so")
    }

    fun getStorageRoots(): List<String> {
        val roots = mutableListOf<String>()
        
        // Primary external storage
        if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) {
            roots.add(Environment.getExternalStorageDirectory().absolutePath)
        }

        // Additional storage paths
        roots.add("/storage/emulated/0")
        roots.add("/storage")

        return roots.filter { File(it).exists() }
    }
}
