package com.example.zipmaster.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.zipmaster.data.AppDatabase
import com.example.zipmaster.data.HistoryItem
import com.example.zipmaster.data.HistoryRepository
import com.example.zipmaster.logic.ArchiveManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.io.File

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: HistoryRepository
    val allHistory: Flow<List<HistoryItem>>
    private val archiveManager: ArchiveManager

    init {
        val historyDao = AppDatabase.getDatabase(application).historyDao()
        repository = HistoryRepository(historyDao)
        allHistory = repository.allHistory
        archiveManager = ArchiveManager(application)
    }

    fun addHistory(fileName: String, type: String, success: Boolean, path: String? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insert(
                HistoryItem(
                    fileName = fileName,
                    operationType = type,
                    timestamp = System.currentTimeMillis(),
                    isSuccess = success,
                    filePath = path
                )
            )
        }
    }

    fun clearHistory() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.clearAll()
        }
    }

    fun extractArchive(filePath: String, destPath: String, password: String? = null, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val listener = object : ArchiveManager.ProgressListener {
                override fun onProgress(percentage: Int, currentFile: String) {}
                override fun onCompleted() {
                    addHistory(File(filePath).name, "EXTRACT", true, destPath)
                    onResult(true, null)
                }
                override fun onError(error: String) {
                    addHistory(File(filePath).name, "EXTRACT", false)
                    onResult(false, error)
                }
            }
            
            if (filePath.lowercase().endsWith(".zip")) {
                archiveManager.extractZip(filePath, destPath, password, listener)
            } else if (filePath.lowercase().endsWith(".rar")) {
                archiveManager.extractRar(filePath, destPath, password, listener)
            } else {
                onResult(false, "Unsupported format")
            }
        }
    }

    fun createZip(files: List<File>, destPath: String, password: String? = null, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val listener = object : ArchiveManager.ProgressListener {
                override fun onProgress(percentage: Int, currentFile: String) {}
                override fun onCompleted() {
                    addHistory(File(destPath).name, "COMPRESS", true, destPath)
                    onResult(true, null)
                }
                override fun onError(error: String) {
                    addHistory(File(destPath).name, "COMPRESS", false)
                    onResult(false, error)
                }
            }
            archiveManager.createZip(files, destPath, password, listener)
        }
    }
}
