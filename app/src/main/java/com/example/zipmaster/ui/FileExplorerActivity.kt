package com.example.zipmaster.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.zipmaster.databinding.ActivityExplorerBinding
import java.io.File

class FileExplorerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityExplorerBinding
    private var currentDir = Environment.getExternalStorageDirectory()
    private val selectedFiles = mutableSetOf<File>()
    private var isSelectionMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityExplorerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        isSelectionMode = intent.getBooleanExtra("selection_mode", false)
        binding.fabDone.visibility = if (isSelectionMode) View.VISIBLE else View.GONE

        setupRecyclerView()
        loadDirectory(currentDir)

        binding.fabDone.setOnClickListener {
            val resultIntent = Intent().apply {
                putExtra("selected_paths", selectedFiles.map { it.absolutePath }.toTypedArray())
            }
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }
    }

    private fun setupRecyclerView() {
        binding.rvFiles.layoutManager = LinearLayoutManager(this)
    }

    private fun loadDirectory(dir: File) {
        currentDir = dir
        binding.tvPath.text = dir.absolutePath
        val files = dir.listFiles()?.sortedWith(compareBy({ !it.isDirectory }, { it.name.lowercase() })) ?: emptyList()
        
        binding.rvFiles.adapter = FileAdapter(files, isSelectionMode, selectedFiles) { file ->
            if (file.isDirectory) {
                loadDirectory(file)
            } else if (!isSelectionMode) {
                val resultIntent = Intent().apply {
                    putExtra("selected_path", file.absolutePath)
                }
                setResult(Activity.RESULT_OK, resultIntent)
                finish()
            }
        }
    }

    override fun onBackPressed() {
        if (currentDir.absolutePath != Environment.getExternalStorageDirectory().absolutePath) {
            loadDirectory(currentDir.parentFile ?: Environment.getExternalStorageDirectory())
        } else {
            super.onBackPressed()
        }
    }
}
