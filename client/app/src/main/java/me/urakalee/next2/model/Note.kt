package me.urakalee.next2.model

import android.net.Uri
import me.shouheng.notepal.model.Model
import me.shouheng.notepal.model.Notebook
import me.shouheng.notepal.provider.annotation.Column
import me.shouheng.notepal.provider.annotation.Table
import me.shouheng.notepal.provider.schema.NoteSchema
import me.urakalee.next2.config.TimeConfig
import me.urakalee.next2.storage.getFile
import org.joda.time.LocalDate
import java.io.File

/**
 * @author Uraka.Lee
 */
@Table(name = NoteSchema.TABLE_NAME)
class Note : Model() {

    @Column(name = NoteSchema.TREE_PATH)
    var treePath: String? = null

    @Column(name = NoteSchema.TITLE)
    var title: String? = null

    @Column(name = NoteSchema.CONTENT_CODE)
    var attachmentCode: Long = 0

    @Column(name = NoteSchema.TAGS)
    var tags: String? = null

    @Column(name = NoteSchema.PREVIEW_IMAGE)
    var previewImage: Uri? = null

    @Column(name = NoteSchema.PREVIEW_CONTENT)
    var previewContent: String? = null

    // region Android端字段，不计入数据库

    var notebook: Notebook? = null
    var content: String? = null
    var tagsName: String? = null

    var timePath: String? = null
    var dayPrefix: Int = 0
    var originTitle: String? = null
    var originFile: File? = null

    //endregion

    val fileName: String?
        get() {
            val titleNonNull = title ?: return null
            return getFileNameWithDayPrefix(
                    if (titleNonNull.endsWith(DEFAULT_SUFFIX)) {
                        titleNonNull
                    } else {
                        titleNonNull + DEFAULT_SUFFIX
                    }
            )
        }

    val file: File?
        get() {
            val notebookNonNull = notebook ?: return null
            val timePathNonNull = timePath ?: return null
            val fileNameNonNull = fileName ?: return null
            return getFile(notebookNonNull.title, timePathNonNull, fileNameNonNull)
        }

    val createTimeStr: String
        get() {
            // 列表展示时使用, timePath 一定不空
            val timePathNonNull = timePath!!
            // 验证 dayPrefix 的有效性
            return if (dayPrefix > 0) {
                val firstDay = LocalDate.parse(String.format("%s-01", timePath))
                val month = firstDay.monthOfYear
                val createDay = firstDay.plusDays(dayPrefix - 1)
                return if (createDay.monthOfYear == month) {
                    createDay.toString("yyyy-MM-dd")
                } else {
                    timePathNonNull
                }
            } else {
                timePathNonNull
            }
        }

    //region load

    fun setTitleByFileName(name: String) {
        val result = if (name.endsWith(DEFAULT_SUFFIX)) {
            name.substring(0, name.length - DEFAULT_SUFFIX.length)
        } else {
            name
        }
        val parts = result.split(DAY_SEPARATOR, limit = 2)
        dayPrefix = if (parts.size == 2) {
            parts[0].toIntOrNull() ?: 0
        } else {
            0
        }
        title = if (dayPrefix > 0) parts[1] else result
    }

    //endregion
    //region save

    val isNewNote: Boolean
        get() = originTitle == null

    fun finishNew() {
        originTitle = title
    }

    fun needRename(): Boolean {
        return originTitle != null && title != originTitle
    }

    val originFileName: String
        get() {
            // 改名时使用, originTitle 一定不空
            val originTitleNonNull = originTitle!!
            return getFileNameWithDayPrefix(
                    if (originTitleNonNull.endsWith(DEFAULT_SUFFIX)) {
                        originTitleNonNull
                    } else {
                        originTitleNonNull + DEFAULT_SUFFIX
                    }
            )
        }

    fun finishRename() {
        originTitle = title
        originFile = null
    }

    fun generateTimePath() {
        val today = LocalDate.now()
        timePath = today.toString(TimeConfig.MONTH_FORMAT)
        dayPrefix = today.dayOfMonth().get()
    }

    //endregion
    //region private

    private fun getFileNameWithDayPrefix(name: String): String {
        return if (dayPrefix > 0) {
            String.format("%02d%s%s", dayPrefix, DAY_SEPARATOR, name)
        } else {
            name
        }
    }

    //endregion

    override fun toString(): String {
        return "Note{" +
                "treePath='" + treePath + '\''.toString() +
                ", title='" + title + '\''.toString() +
                ", attachmentCode=" + attachmentCode +
                ", tags='" + tags + '\''.toString() +
                ", previewImage=" + previewImage +
                ", previewContent='" + previewContent + '\''.toString() +
                ", content='" + content + '\''.toString() +
                ", tagsName='" + tagsName + '\''.toString() +
                "} " + super.toString()
    }

    companion object {

        const val DEFAULT_SUFFIX = ".md"

        private const val DAY_SEPARATOR = "_"
    }
}
