package com.example.zipmaster.utils

import com.github.junrar.Archive
import java.io.File
import java.io.FileOutputStream

object RarUtils {

    fun extract(rarFilePath: String, destinationPath: String) {
        val archive = Archive(File(rarFilePath))
        var fileHeader = archive.nextFileHeader()
        while (fileHeader != null) {
            val out = File(destinationPath, fileHeader.fileNameString.trim())
            if (fileHeader.isDirectory) {
                out.mkdirs()
            } else {
                out.parentFile?.mkdirs()
                FileOutputStream(out).use { fos ->
                    archive.extractFile(fileHeader, fos)
                }
            }
            fileHeader = archive.nextFileHeader()
        }
    }
}
