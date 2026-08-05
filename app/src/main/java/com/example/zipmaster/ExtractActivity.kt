package com.example.zipmaster

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.DocumentsContract
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.zipmaster.databinding.ActivityExtractBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.lingala.zip4j.ZipFile
import net.lingala.zip4j.exception.ZipException
import com.github.junrar.Junrar
import java.io.File
import java.io.FileOutputStream

class ExtractActivity : AppCompatActivity() {

    private lateinit var binding: ActivityExtractBinding
    private var selectedFileUri: Uri? = null
    private var selectedFileName: String? = null
    private var destUri: Uri? = null
    private val PICK_FILE = 100
    private val PICK_DEST = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityExtractBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnSelectFile.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "*/*"
                putExtra(Intent.EXTRA_MIME_TYPES, arrayOf(
                    "application/zip",
                    "application/x-rar-compressed",
                    "application/vnd.rar"
                ))
            }
            startActivityForResult(intent, PICK_FILE)
        }

        binding.btnSelectDest.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
            startActivityForResult(intent, PICK_DEST)
        }

        binding.btnExtract.setOnClickListener {
            extractFile()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && data != null) {
            when (requestCode) {
                PICK_FILE -> {
                    selectedFileUri = data.data
                    selectedFileName = getFileName(selectedFileUri!!)
                    binding.tvFileName.text = selectedFileName
                    binding.tvFileName.visibility = View.VISIBLE
                    binding.btnExtract.isEnabled = true
                    binding.btnExtract.alpha = 1.0f
                }
                PICK_DEST -> {
                    destUri = data.data
                    binding.tvDestPath.text = destUri.toString()
                }
            }
        }
    }

    private fun getFileName(uri: Uri): String {
        var result = "unknown"
        contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val idx = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                if (idx >= 0) result = cursor.getString(idx)
            }
        }
        return result
    }

    private fun extractFile() {
        val uri = selectedFileUri ?: return
        val password = binding.etPassword.text.toString().ifEmpty { null }

        binding.progressBar.visibility = View.VISIBLE
        binding.btnExtract.isEnabled = false

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val tempFile = File(cacheDir, "temp_archive_${System.currentTimeMillis()}")
                contentResolver.openInputStream(uri)?.use { input ->
                    FileOutputStream(tempFile).use { output ->
                        input.copyTo(output)
                    }
                }

                val destDir = if (destUri != null) {
                    File(cacheDir, "extracted")
                } else {
                    File(Environment.getExternalStorageDirectory(), "Download/ZipMaster/Extracted").apply { mkdirs() }
                }

                when {
                    selectedFileName?.endsWith(".zip", true) == true -> {
                        val zipFile = ZipFile(tempFile)
                        if (password != null) zipFile.setPassword(password.toCharArray())
                        zipFile.extractAll(destDir.absolutePath)
                    }
                    selectedFileName?.endsWith(".rar", true) == true -> {
                        Junrar.extract(tempFile, destDir)
                    }
                    else -> throw Exception("فرمت پشتیبانی نمی‌شود")
                }

                tempFile.delete()

                withContext(Dispatchers.Main) {
                    showResult("✅ استخراج شد!\n📂 ${destDir.absolutePath}", true)
                }

            } catch (e: ZipException) {
                withContext(Dispatchers.Main) {
                    showResult("❌ رمز اشتباه یا فایل خراب", false)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showResult("❌ ${e.message}", false)
                }
            }
        }
    }

    private fun showResult(msg: String, success: Boolean) {
        binding.progressBar.visibility = View.GONE
        binding.btnExtract.isEnabled = true
        binding.tvResult.text = msg
        binding.tvResult.setBackgroundColor(
            if (success) 0xFF1B4332.toInt() else 0xFF450A0A.toInt()
        )
        binding.tvResult.setTextColor(
            if (success) 0xFF6EE7B7.toInt() else 0xFFFCA5A5.toInt()
        )
        binding.tvResult.visibility = View.VISIBLE
    }
}
