package com.example.zipmaster

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.zipmaster.databinding.ActivityMainBinding
import com.example.zipmaster.ui.HistoryAdapter
import com.example.zipmaster.ui.MainViewModel
import com.example.zipmaster.util.FileUtil
import kotlinx.coroutines.launch
import java.io.File

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()
    private val adapter = HistoryAdapter()
    private var progressDialog: androidx.appcompat.app.AlertDialog? = null

    private val openArchiveLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val path = result.data?.getStringExtra("selected_path")
            if (path != null) {
                handleArchiveSelection(path)
            }
        }
    }

    private val createZipLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val paths = result.data?.getStringArrayExtra("selected_paths")
            if (paths != null && paths.isNotEmpty()) {
                handleCreateZip(paths.toList())
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        observeViewModel()
    }

    private fun setupUI() {
        binding.rvHistory.layoutManager = LinearLayoutManager(this)
        binding.rvHistory.adapter = adapter

        binding.cardOpen.setOnClickListener {
            val intent = Intent(this, com.example.zipmaster.ui.FileExplorerActivity::class.java).apply {
                putExtra("selection_mode", false)
            }
            openArchiveLauncher.launch(intent)
        }

        binding.cardCreate.setOnClickListener {
            val intent = Intent(this, com.example.zipmaster.ui.FileExplorerActivity::class.java).apply {
                putExtra("selection_mode", true)
            }
            createZipLauncher.launch(intent)
        }

        binding.btnSettings.setOnClickListener {
            startActivity(Intent(this, com.example.zipmaster.ui.SettingsActivity::class.java))
        }
    }

    private fun showPasswordDialog(onPasswordEntered: (String) -> Unit) {
        val editText = android.widget.EditText(this)
        editText.inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
        
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle(R.string.password)
            .setView(editText)
            .setPositiveButton(R.string.extract) { _, _ ->
                onPasswordEntered(editText.text.toString())
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.allHistory.collect { history ->
                adapter.submitList(history)
            }
        }
    }

    private fun showProgress(message: String) {
        val padding = 64
        val layout = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.HORIZONTAL
            setPadding(padding, padding, padding, padding)
            gravity = android.view.Gravity.CENTER_VERTICAL
        }
        val progressBar = android.widget.ProgressBar(this).apply {
            isIndeterminate = true
        }
        val textView = android.widget.TextView(this).apply {
            text = message
            setPadding(padding, 0, 0, 0)
        }
        layout.addView(progressBar)
        layout.addView(textView)

        progressDialog = androidx.appcompat.app.AlertDialog.Builder(this)
            .setView(layout)
            .setCancelable(false)
            .show()
    }

    private fun hideProgress() {
        progressDialog?.dismiss()
    }

    private fun handleArchiveSelection(path: String, password: String? = null) {
        showProgress(getString(R.string.extracting))
        val archiveFile = File(path)
        val destDir = File(archiveFile.parentFile, "extracted_${archiveFile.nameWithoutExtension}")
        destDir.mkdirs()

        viewModel.extractArchive(path, destDir.absolutePath, password) { success, error ->
            runOnUiThread {
                hideProgress()
                if (success) {
                    Toast.makeText(this, getString(R.string.operation_success), Toast.LENGTH_SHORT).show()
                } else if (error == "PASSWORD_REQUIRED") {
                    showPasswordDialog { pwd ->
                        handleArchiveSelection(path, pwd)
                    }
                } else {
                    Toast.makeText(this, error ?: getString(R.string.operation_failed), Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun handleCreateZip(paths: List<String>) {
        showProgress(getString(R.string.compressing))
        val files = paths.map { File(it) }
        val destFile = File(files[0].parentFile, "archive_${System.currentTimeMillis()}.zip")

        viewModel.createZip(files, destFile.absolutePath) { success, error ->
            runOnUiThread {
                hideProgress()
                if (success) {
                    Toast.makeText(this, getString(R.string.operation_success), Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, error ?: getString(R.string.operation_failed), Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}
