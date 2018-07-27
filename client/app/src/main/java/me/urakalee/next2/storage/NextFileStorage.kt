package me.urakalee.next2.storage

import me.urakalee.next2.model.Note
import me.shouheng.notepal.model.Notebook
import me.urakalee.next2.config.TimeConfig.MONTH_FORMAT
import org.joda.time.LocalDate
import java.io.File
import java.util.*

/**
 * @author Uraka.Lee
 */
fun listNotebook(): List<Notebook> {
    val notebookRoot = storageRoot()
    val notebooks = LinkedList<Notebook>()
    for (file in listDirs(notebookRoot)) {
        val notebook = Notebook()
        notebook.title = file.name
        notebooks.add(notebook)
        // 计算最近 2 个月的文章数
        val thisMonth = LocalDate.now().toString(MONTH_FORMAT)
        val prevMonth = LocalDate.now().withDayOfMonth(1).minusMonths(1).toString(MONTH_FORMAT)
        for (dir in listDirs(file)) {
            if (dir.name == thisMonth || dir.name == prevMonth) {
                notebook.count += countFiles(dir, Note.DEFAULT_SUFFIX)
            }
        }
    }
    return notebooks
}

fun listNote(notebook: Notebook): List<Note> {
    val noteRoot = File(storageRoot(), notebook.title)
    val notes = LinkedList<Note>()
    val filesInDirs = listFilesInSubDirs(noteRoot)
    for (dirName in filesInDirs.keys.sortedByDescending { it }) {
        val files = filesInDirs[dirName] ?: continue
        for (file in files) {
            val note = Note()
            note.setTitleByFileName(file.name)
            note.notebook = notebook
            note.timePath = dirName
            notes.add(note)
        }
        notes.sortWith(kotlin.Comparator { o1, o2 ->
            o2.dayPrefix - o1.dayPrefix
        })
    }
    return notes
}
