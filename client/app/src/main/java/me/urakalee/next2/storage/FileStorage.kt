@file:JvmName("FileStorage")

package me.urakalee.next2.storage

import android.os.Environment
import java.io.File

/**
 * @author Uraka.Lee
 */
fun sdCard() = Environment.getExternalStorageDirectory()

fun storageRoot() = File(sdCard(), "/Android/data/com.ryeeeeee.markdownx/files/notes/YfdClientWiki")

fun listDirs(file: File, showHidden: Boolean = false): List<File> {
    val dir = if (file.isDirectory) file else return listOf()
    val dirs = dir.listFiles { pathname: File? ->
        pathname?.isDirectory ?: false
    }
    dirs.sortBy { it.name } // TODO: 先按名称排序
    return if (showHidden) dirs.asList() else dirs.filterNot { it.name.startsWith('.') }
}

fun listFiles(file: File): List<File> {
    val dir = if (file.isDirectory) file else return listOf()
    val files = dir.listFiles { pathname: File? ->
        pathname?.isFile ?: false
    }
    files.sortByDescending { it.lastModified() } // XXX: 这个值和设备相关...
    return files.asList()
}

fun isDirEmpty(file: File): Boolean {
    val dir = if (file.isDirectory) file else return false
    return dir.list().isEmpty() || dir.listFiles().all {
        it.isDirectory && isDirEmpty(it)
    }
}

fun getFile(name: String): File {
    return File(storageRoot(), name)
}

fun ensureDir(name: String) {
    ensureDir(getFile(name))
}

fun ensureDir(file: File) {
    if (file.isDirectory) {
        return
    }
    if (file.exists()) {
        throw RuntimeException("Target is a file")
    } else {
        file.mkdir()
    }
}

fun ensureMoveDir(source: String, target: String) {
    val targetFile = getFile(target)
    if (targetFile.exists()) {
        throw RuntimeException("Target exists")
    } else {
        getFile(source).renameTo(targetFile)
    }
}

fun getFile(parent: String, child: String): File? {
    return File(storageRoot(), parent).listFiles { pathname ->
        pathname?.name == child
    }.firstOrNull()
}