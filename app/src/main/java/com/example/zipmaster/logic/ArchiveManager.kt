package com.example.zipmaster.logic

import android.content.Context
import net.lingala.zip4j.ZipFile
import net.lingala.zip4j.model.ZipParameters
import net.lingala.zip4j.model.enums.EncryptionMethod
import com.github.junrar.Archive
import com.github.junrar.rarfile.FileHeader
import java.io.File
import java.io.FileOutputStream

class ArchiveManager(private val context: Context) {

    interface ProgressListener {
        fun onProgress(percentage: Int, currentFile: String)
        fun onCompleted()
        fun onError(error: String)
    }

    fun extractZip(zipFilePath: String, destPath: String, password: String? = null, listener: ProgressListener) {
        try {
            val zipFile = ZipFile(zipFilePath)
            if (zipFile.isEncrypted) {
                if (password == null) {
                    listener.onError("PASSWORD_REQUIRED")
                    return
                }
                zipFile.setPassword(password.toCharArray())
            }

            if (!zipFile.isValidZipFile) {
                listener.onError("INVALID_ZIP")
                return
            }

            val fileHeaders = zipFile.fileHeaders
            val totalFiles = fileHeaders.size
            var processedFiles = 0

            zipFile.isRunInThread = true
            
            // Note: Zip4j progress monitoring can be complex in async. 
            // For simplicity, we use the synchronous call with manual progress if needed, 
            // but here we'll use its built-in listener if possible or just wrap it.
            
            zipFile.extractAll(destPath)
            listener.onCompleted()
        } catch (e: Exception) {
            listener.onError(e.message ?: "Unknown error")
        }
    }

    fun extractRar(rarFilePath: String, destPath: String, password: String? = null, listener: ProgressListener) {
        try {
            val rarFile = File(rarFilePath)
            val archive = Archive(rarFile, password)
            
            if (archive.isEncrypted) {
                 if (password == null) {
                    listener.onError("PASSWORD_REQUIRED")
                    return
                }
            }

            var header: FileHeader? = archive.nextFileHeader()
            while (header != null) {
                if (!header.isDirectory) {
                    val out = File(destPath, header.fileNameString)
                    out.parentFile?.mkdirs()
                    val fos = FileOutputStream(out)
                    archive.extractFile(header, fos)
                    fos.close()
                }
                header = archive.nextFileHeader()
            }
            archive.close()
            listener.onCompleted()
        } catch (e: Exception) {
            listener.onError(e.message ?: "Unknown error")
        }
    }

    fun createZip(files: List<File>, destZipPath: String, password: String? = null, listener: ProgressListener) {
        try {
            val zipFile = ZipFile(destZipPath)
            val parameters = ZipParameters()
            if (password != null) {
                parameters.isEncryptFiles = true
                parameters.encryptionMethod = EncryptionMethod.AES
                zipFile.setPassword(password.toCharArray())
            }

            files.forEach { file ->
                if (file.isDirectory) {
                    zipFile.addFolder(file, parameters)
                } else {
                    zipFile.addFile(file, parameters)
                }
            }
            listener.onCompleted()
        } catch (e: Exception) {
            listener.onError(e.message ?: "Unknown error")
        }
    }
}
