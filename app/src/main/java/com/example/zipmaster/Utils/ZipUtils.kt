package com.example.zipmaster.utils

import net.lingala.zip4j.ZipFile
import net.lingala.zip4j.model.ZipParameters
import net.lingala.zip4j.model.enums.CompressionLevel
import net.lingala.zip4j.model.enums.EncryptionMethod
import java.io.File

object ZipUtils {

    fun extract(zipFilePath: String, destinationPath: String, password: String? = null) {
        val zipFile = ZipFile(zipFilePath)
        if (zipFile.isEncrypted && password != null) {
            zipFile.setPassword(password.toCharArray())
        }
        zipFile.extractAll(destinationPath)
    }

    fun compress(filesToCompress: List<File>, destinationZipPath: String, password: String? = null) {
        val zipFile = ZipFile(destinationZipPath)
        val parameters = ZipParameters().apply {
            compressionLevel = CompressionLevel.NORMAL
            if (password != null) {
                isEncryptFiles = true
                encryptionMethod = EncryptionMethod.AES
            }
        }
        if (password != null) {
            zipFile.setPassword(password.toCharArray())
        }
        zipFile.addFiles(filesToCompress, parameters)
    }
}
