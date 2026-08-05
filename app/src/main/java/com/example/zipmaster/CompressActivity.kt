package com.example.zipmaster

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.OpenableColumns
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.zipmaster.databinding.ActivityCompressBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.lingala.zip4j.ZipFile
import net.lingala.zip4j.model.ZipParameters
import net.lingala.zip4j.model.enums.CompressionLevel
import net.lingala.zip4j.model.enums.EncryptionMethod
import java.io.File
import java.io.FileOutputStream

class CompressActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCompressBinding
    private var selectedFiles = mutableListOf<Uri>()
    private val PICK_FILES = 200

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCompressBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnSelectFiles.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "*/*"
                putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            }
            startActivityForResult(intent, PICK_FILES)
        }

        binding.btnCompress.setOnClickListener {
            compressFiles()
        }

        binding.etFileName.setOnFocusChangeListener { _, _ -> updateButton() }
    }

    private fun updateButton() {
        val enabled = selectedFiles.isNotEmpty() && binding.etFileName.text.toString().isNotEmpty()
        binding.btnCompress.isEnabled = enabled
        binding.btnCompress.alpha = if (enabled) 1.0f else 0.5f
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && data != null) {
            when (requestCode) {
                PICK_FILES -> {
                    selectedFiles.clear()
                    data.clipData?.let { clip ->
                        for (i in 0 until clip.itemCount) {
                            selectedFiles.add(clip.getItemAt(i).uri)
                        }
                    } ?: data.data?.let { selectedFiles.add(it) }

                    binding.tvFileCount.text = "${selectedFiles.size} فایل"
                    binding.tvFileCount.visibility = View.VISIBLE
                    updateButton()
                }
            }
        }
    }

    private fun compressFiles() {
        val fileName = binding.etFileName.text.toString()
        val password = binding.etPassword.text.toString()
        val confirm = binding.etConfirmPassword.text.toString()

        if (password.isNotEmpty() && password != confirm) {
            showResult("❌ رمز و تکرار یکسان نیست!", false)
            return
        }

        val level = when (binding.sliderLevel.value.toInt()) {
            in 0..2 -> CompressionLevel.FASTEST
            in 3..5 -> CompressionLevel.NORMAL
            in 6..7 -> CompressionLevel.HIGHER
            else -> CompressionLevel.ULTRA
        }

        binding.progressBar.visibility = View.VISIBLE
        binding.btnCompress.isEnabled = false

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val outDir = File(Environment.getExternalStorageDirectory(), "Download/ZipMaster/Compressed")
                outDir.mkdirs()

                val zipPath = "${outDir.absolutePath}/$fileName.zip"
                val zipFile = ZipFile(zipPath)

                if (password.isNotEmpty()) {
                    zipFile.setPassword(password.toCharArray())
                }

                val params = ZipParameters().apply {
                    compressionLevel = level
                    if (password.isNotEmpty()) encryptionMethod = EncryptionMethod.AES
                }

                for (uri in selectedFiles) {
                    val temp = File(cacheDir, "temp_${System.currentTimeMillis()}")
                    contentResolver.openInputStream(uri)?.use { input ->
                        FileOutputStream(temp).use { output -> input.copyTo(output) }
                    }
                    zipFile.addFile(temp, params)
                    temp.delete()
                }

                withContext(Dispatchers.Main) {
                    showResult("✅ ساخته شد!\n📦 $zipPath", true)
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
        binding.btnCompress.isEnabled = true
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
