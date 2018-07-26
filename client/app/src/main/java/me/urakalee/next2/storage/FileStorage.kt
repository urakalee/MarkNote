@file:JvmName("FileStorage")

package me.urakalee.next2.storage

import android.os.Environment
import java.io.File

/**
 * @author Uraka.Lee
 */
//private const val ROOT_PATH = "NExT2"
private const val ROOT_PATH = "/Android/data/com.ryeeeeee.markdownx/files/notes/dayone"

fun sdCard() = Environment.getExternalStorageDirectory()

fun storageRoot() = File(sdCard(), ROOT_PATH)

fun listDirs(file: File, showHidden: Boolean = false): List<File> {
    val dir = if (file.isDirectory) file else return emptyList()
    val dirs = dir.listFiles { f: File ->
        f.isDirectory && (showHidden || !isHidden(f))
    }
    dirs.sortBy { it.name } // TODO: 手动顺序排序就不需要这个了
    return dirs.asList()
}

fun listFiles(file: File, showHidden: Boolean = false): List<File> {
    val dir = if (file.isDirectory) file else return emptyList()
    val files = dir.listFiles { f: File ->
        f.isFile && (showHidden || !isHidden(f))
    }
    files.sortByDescending { it.lastModified() } // XXX: 这个值和设备相关...
    return files.asList()
}

fun listFilesInSubDirs(file: File): Map<String, List<File>> {
    val dirs = listDirs(file)
    val filesInDirs = mutableMapOf<String, List<File>>()
    for (dir in dirs) {
        if (isDirEmpty(dir)) {
            continue
        }
        filesInDirs[dir.name] = listFiles(dir)
    }
    return filesInDirs
}

fun isDirEmpty(file: File): Boolean {
    val dir = if (file.isDirectory) file else return false
    return dir.list().isEmpty() || dir.listFiles().all {
        it.isDirectory && isDirEmpty(it)
    }
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

/**
 * @return -1 if not directory
 */
fun countFiles(file: File, ext: String? = null, includeHidden: Boolean = false): Int {
    val dir = if (file.isDirectory) file else return -1
    val files = dir.listFiles { f: File ->
        f.isFile && (ext == null || f.name.endsWith("$ext")) && (includeHidden || !isHidden(f))
    }
    return files.size
}

fun isHidden(file: File): Boolean {
    return file.name.startsWith('.')
}

fun getFile(name: String): File {
    return File(storageRoot(), name)
}

fun getFile(parent: String, child: String): File? {
    return getFile(getFile(parent), child)
}

fun getFile(parent: String, subDir: String, child: String): File? {
    val dir = getFile(parent, subDir) ?: return null
    return getFile(dir, child)
}

fun getFile(parent: File, child: String): File? {
    return parent.listFiles { pathname ->
        pathname?.name == child
    }.firstOrNull()
}
