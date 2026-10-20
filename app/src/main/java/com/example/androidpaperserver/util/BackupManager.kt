package com.example.androidpaperserver.util

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

object BackupManager {
    fun create(source: File, destination: File) {
        destination.parentFile?.mkdirs()
        ZipOutputStream(FileOutputStream(destination)).use { zip ->
            source.walkTopDown().filter { it.isFile && it.canonicalPath != destination.canonicalPath }.forEach { file ->
                val rel = file.relativeTo(source).path.replace(File.separatorChar, '/')
                zip.putNextEntry(ZipEntry(rel))
                FileInputStream(file).use { it.copyTo(zip) }
                zip.closeEntry()
            }
        }
    }

    fun restore(archive: File, destination: File) {
        ZipInputStream(archive.inputStream()).use { zip ->
            var entry = zip.nextEntry
            while (entry != null) {
                require(!entry.name.startsWith("/") && !entry.name.contains("../") && !entry.name.contains("..\\")) { "Unsafe backup entry" }
                val target = File(destination, entry.name).canonicalFile
                require(target.path.startsWith(destination.canonicalPath + File.separator)) { "Unsafe backup entry" }
                if (entry.isDirectory) target.mkdirs() else {
                    target.parentFile?.mkdirs(); target.outputStream().use { zip.copyTo(it) }
                }
                zip.closeEntry(); entry = zip.nextEntry
            }
        }
    }
}
