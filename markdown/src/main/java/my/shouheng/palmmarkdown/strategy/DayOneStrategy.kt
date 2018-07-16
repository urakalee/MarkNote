package my.shouheng.palmmarkdown.strategy

import android.widget.EditText

/**
 * @author Uraka.Lee
 */
class DayOneStrategy : DefaultStrategy() {

    // XXX: \n 和 \r\n 对于 selectionStart 的影响相同吗?
    override fun h1(source: String?, selectionStart: Int, selectionEnd: Int, selection: String?, editor: EditText?) {
        val lines = source?.lines()?.toMutableList() ?: return
        // 如果选中多行, 目前只处理第一行
        // 选择一行文字
        var charPassed = 0
        var targetLineIndex = 0
        while (charPassed < selectionStart && targetLineIndex < lines.size) {
            if (selectionStart <= charPassed + lines[targetLineIndex].length) {
                // 找到了
                break
            }
            charPassed += lines[targetLineIndex].length
            targetLineIndex += 1
            if (targetLineIndex < lines.size) {
                charPassed += 1 // \n
            }
        }
        var targetLine = lines.getOrNull(targetLineIndex) ?: return
        // parse 出前面格式(header, list, taskt, quote)之外的文字
        // TODO: 处理 indent
        val indent = false
        val firstBlank = targetLine.indexOf(' ')
        if (firstBlank == -1) {
            targetLine = "# $targetLine"
        } else {
            val mark = targetLine.substring(0 until firstBlank)
            val newMark = TRANSFORM_MATRIX.filter {
                it.first == mark && it.second == indent
            }.firstOrNull()?.third
            newMark?.let {
                targetLine = targetLine.replaceFirst(mark, newMark)
            }
        }
        lines[targetLineIndex] = targetLine
        // 为这一行增加/替换 header

        editor?.setText(lines.joinToString("\n"))
        editor?.setSelection(charPassed + targetLine.length) // 简单处理, 光标放在行尾
    }

    private companion object {
        val TRANSFORM_MATRIX = listOf(
                Triple("#", false, "##"),
                Triple("##", false, "###"),
                Triple("###", false, "####"),
                Triple("####", false, "#####"),
                Triple("#####", false, "######"),
                Triple("######", false, "#")
        )
    }
}