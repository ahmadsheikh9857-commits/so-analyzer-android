package com.example.soanalyzer

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.example.soanalyzer.utils.FileItem
import com.example.soanalyzer.utils.FileSystemManager
import com.google.android.material.appbarcompat.AppBarLayout
import com.google.android.material.button.MaterialButton
import com.google.android.material.textview.MaterialTextView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private lateinit var fileSystemManager: FileSystemManager
    private lateinit var recyclerView: RecyclerView
    private lateinit var fileAdapter: FileAdapter
    private val pathStack = mutableListOf<String>()
    private var currentPath = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        fileSystemManager = FileSystemManager(this)
        setupUI()
        checkStoragePermission()
    }

    private fun setupUI() {
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        fileAdapter = FileAdapter { item -> onFileItemClicked(item) }
        recyclerView.adapter = fileAdapter

        val startPath = Environment.getExternalStorageDirectory().absolutePath
        pathStack.add(startPath)
        currentPath = startPath
        loadDirectory(currentPath)
    }

    private fun checkStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                showPermissionDialog()
            }
        } else {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    PERMISSION_REQUEST_CODE
                )
            }
        }
    }

    private fun showPermissionDialog() {
        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
        builder.setTitle(R.string.permission_required)
        builder.setMessage(R.string.permission_message)
        builder.setPositiveButton(R.string.open_settings) { _, _ ->
            val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
            startActivity(intent)
        }
        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
        builder.show()
    }

    private fun loadDirectory(path: String) {
        GlobalScope.launch {
            val items = withContext(Dispatchers.IO) {
                fileSystemManager.listDirectory(path)
            }
            withContext(Dispatchers.Main) {
                fileAdapter.submitList(items)
            }
        }
    }

    private fun onFileItemClicked(item: FileItem) {
        if (item.isDirectory) {
            pathStack.add(item.path)
            currentPath = item.path
            loadDirectory(currentPath)
        } else if (fileSystemManager.isSOFile(item.path)) {
            val intent = Intent(this, AnalyzerActivity::class.java)
            intent.putExtra("FILE_PATH", item.path)
            intent.putExtra("FILE_NAME", item.name)
            startActivity(intent)
        } else {
            Toast.makeText(this, "File type not supported", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onBackPressed() {
        if (pathStack.size > 1) {
            pathStack.removeAt(pathStack.size - 1)
            currentPath = pathStack.last()
            loadDirectory(currentPath)
        } else {
            super.onBackPressed()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                loadDirectory(currentPath)
            }
        }
    }

    companion object {
        private const val PERMISSION_REQUEST_CODE = 100
    }
}

class FileAdapter(private val onItemClick: (FileItem) -> Unit) :
    RecyclerView.Adapter<FileAdapter.ViewHolder>() {

    private var items = listOf<FileItem>()

    fun submitList(newItems: List<FileItem>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_file, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size

    inner class ViewHolder(itemView: android.view.View) :
        RecyclerView.ViewHolder(itemView) {
        private val nameView: MaterialTextView = itemView.findViewById(R.id.fileName)
        private val detailView: MaterialTextView = itemView.findViewById(R.id.fileDetail)
        private val iconView: MaterialTextView = itemView.findViewById(R.id.fileIcon)

        fun bind(item: FileItem) {
            nameView.text = item.name
            iconView.text = getFileIcon(item.extension)

            val fileSystemManager = FileSystemManager(itemView.context)
            val detail = if (item.isDirectory) {
                "Folder"
            } else {
                "${fileSystemManager.getFileSize(item.size)} • ${fileSystemManager.getFormattedDate(item.modifiedDate)}"
            }
            detailView.text = detail

            itemView.setOnClickListener {
                onItemClick(item)
            }
        }

        private fun getFileIcon(extension: String): String {
            return when (extension) {
                "so" -> "📦"
                "apk" -> "📱"
                "txt" -> "📄"
                "pdf" -> "📕"
                "jpg", "png" -> "🖼️"
                "zip" -> "🗜️"
                "jar" -> "🏺"
                else -> "📄"
            }
        }
    }
}
