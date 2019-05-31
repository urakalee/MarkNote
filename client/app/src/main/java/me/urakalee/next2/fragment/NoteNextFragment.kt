package me.urakalee.next2.fragment

import android.os.Bundle
import android.os.Handler
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.PopupMenu
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewConfiguration
import android.view.ViewGroup
import android.widget.TextView
import kotlinx.android.synthetic.main.note_fragment_next.*
import me.shouheng.notepal.R
import me.shouheng.notepal.util.MD5Util
import me.urakalee.markdown.Indent
import me.urakalee.markdown.Mark
import me.urakalee.markdown.action.DayOneStrategy
import me.urakalee.next2.activity.NoteParagraphActivity
import me.urakalee.next2.base.fragment.BaseModelFragment
import me.urakalee.next2.model.Note
import me.urakalee.ranger.extension.isInvisible
import me.urakalee.ranger.extension.removeRange
import java.util.*
import kotlin.collections.HashMap

/**
 * @author Uraka.Lee
 */
class NoteNextFragment : BaseModelFragment<Note>() {

    companion object {
        const val REQUEST_EDIT_PARAGRAPH = 101
    }

    private lateinit var adapter: NextAdapter

    override val layoutResId: Int
        get() = R.layout.note_fragment_next

    override fun afterViewCreated(savedInstanceState: Bundle?) {
        listView.layoutManager = LinearLayoutManager(context)
        val helper = ItemTouchHelper(touchCallback)
        helper.attachToRecyclerView(listView)

        adapter = NextAdapter()
        adapter.delegate = adapterDelegate
        val lines = delegate.getNote().content?.lines() ?: listOf()
        adapter.setData(Section.lines2Sections(lines))

        listView.adapter = adapter
        adapter.notifyDataSetChanged()
    }

    fun refreshData() {
        // 避免在 next 模式下操作之后, 回到 next 模式时, 导致多余的 setData
        if (Section.joinSections(adapter.sections) != delegate.getNote().content) {
            val lines = delegate.getNote().content?.lines() ?: listOf()
            //region 尽可能保持折叠状态
            val sections = mutableListOf<Section>()
            sections.addAll(Section.lines2Sections(lines))
            val oldSections = adapter.sections
            val md5Map = HashMap<String, Pair<String, String?>>() // line -> (lineMd5, sectionMd5), 如果是 line, sectionMd5 为 null
            traverse(oldSections, false) { list, index ->
                val section = list[index]
                val line = section.text
                val lineMd5 = MD5Util.MD5(line)
                val sectionMd5 = if (section.canExpand()) {
                    MD5Util.MD5(Section.joinSections(section.sections))
                } else {
                    null
                }
                // 这个算法有一个问题但不大: 对于内容完全相同的行, 其 md5 值会相互覆盖
                // 对于有文字的话, 完全相同的行可以认为不存在; 对于无文字的行, 也不会 fold
                md5Map[line] = Pair(lineMd5, sectionMd5)
            }
            traverse(sections, false) { list, index ->
                val section = list[index]
                val line = section.text
                // 新的一行, 可能是改/新增, 不处理
                val pair = md5Map[line] ?: return@traverse
                // 旧的一行, 但没有折叠, 不处理
                if (pair.second == null) {
                    return@traverse
                }
                // 旧的一行, 折叠了, 尝试进行折叠
                if (Section.fold(list, index)) {
                    // 折叠成功, 比较折叠的结果
                    if (pair.second == MD5Util.MD5(Section.joinSections(list[index].sections))) {
                        // 如果相同, 保持这个折叠
                    } else {
                        // 否则, 取消折叠
                        Section.expand(list, index)
                    }
                }
            }
            //endregion
            adapter.setData(sections)
            adapter.notifyDataSetChanged()
        }
    }

    /**
     * @param ignoreIndexZero 由于已经对第一行做了处理, 继续遍历时应忽略; 只在入口时为 false
     */
    private fun traverse(sections: MutableList<Section>, ignoreIndexZero: Boolean, processor: (MutableList<Section>, Int) -> Unit) {
        var index = if (ignoreIndexZero) 1 else 0
        while (index < sections.size) {
            processor.invoke(sections, index)
            val section = sections[index]
            if (section.canExpand()) {
                traverse(section.sections, true, processor)
            }
            index += 1
        }
    }

    //region menu

    /**
     * 支持的整理方式(基于段落 #):
     * 1. TODO: 将想要收集的段落集中到本段开头
     * 1. 新建一下一级, 将想要收集的段落集中到该级的上一级末尾, 剩下的可以折叠起来
     */
    private fun popNextItemMenu(view: View, position: Int) {
        val contextNonNull = context ?: return
        val popupMenu = PopupMenu(contextNonNull, view)
        popupMenu.inflate(R.menu.note_menu_next_item)
        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_edit_paragraph -> {
                    editParagraph(position)
                }
                R.id.action_new_sub_paragraph -> {
                    newSubParagraph(position)
                }
                R.id.action_pop_and_append -> {
                    popAndAppend(position)
                }
            }
            true
        }
        configPopMenu(popupMenu, position)
        popupMenu.show()
    }

    private fun configPopMenu(popupMenu: PopupMenu, position: Int) {
        val section = adapter.sections[position]
        val (mark, _, _) = DayOneStrategy.detectPrecedingMark(section.text)
        popupMenu.menu.findItem(R.id.action_edit_paragraph).isVisible =
            delegate.supportEditParagraph && Mark.fromString(mark) == Mark.H
        popupMenu.menu.findItem(R.id.action_new_sub_paragraph).isVisible = Mark.fromString(mark) != Mark.H
        popupMenu.menu.findItem(R.id.action_pop_and_append).isVisible = true
    }

    private fun editParagraph(position: Int) {
        // 保存 note
        delegate.saveIfNeed {
            // 拷贝一份 note, 不带 content
            val note = delegate.getNote()
            val noteContent = note.content
            note.content = null
            // 打开 NoteParagraphActivity, 带起止行信息
            val sections = adapter.sections
            val section = sections[position]
            var doFold = false
            // 如果能折叠, 先折叠一下再算起止行信息
            if (section.canFold()) {
                Section.fold(sections, position)
                doFold = true
            }
            val paragraph = Section.paragraph(sections, position)
            NoteParagraphActivity.editNote(activity, note, paragraph.first, paragraph.second, REQUEST_EDIT_PARAGRAPH)
            // 如果折叠了, 展开(恢复原状)
            if (doFold) {
                Section.expand(sections, position)
            }
            note.content = noteContent
        }
    }

    private fun newSubParagraph(position: Int) {
        var parentHeaderMark: String? = null
        val sections = adapter.sections
        var curr = position - 1
        while (curr >= 0) {
            val section = sections[curr]
            val (markSource, _, _) = DayOneStrategy.detectPrecedingMark(section.text)
            val testMark = Mark.fromString(markSource)
            if (testMark == Mark.H) {
                parentHeaderMark = markSource
                break
            }
            curr -= 1
        }
        val subHeaderLine = if (parentHeaderMark == null) {
            Mark.H.defaultMark
        } else {
            "$parentHeaderMark#"
        }
        sections.add(position, Section("$subHeaderLine 新段落"))
        adapter.notifyDataSetChanged()
        delegate.getNote().content = Section.joinSections(sections)
        delegate.setContentChanged(true)
    }

    private fun popAndAppend(position: Int) {
        // 找到上一级
        // 理论上上一级会有多个子段落和当前 section 所在段落平级, 但目前的整理方案要求新建下一级, 所以暂时用简单的处理方式:
        // 因为当前 section 所在段落可见, 所以上一级段落一定没有折叠
        // 找到当前段落的开始, 将当前 section 移动到当前段落的上面
        val sections = adapter.sections
        var curr = position - 1
        while (curr >= 0) {
            val section = sections[curr]
            val (testMarkSource, _, _) = DayOneStrategy.detectPrecedingMark(section.text)
            val testMark = Mark.fromString(testMarkSource)
            if (testMark == Mark.H) {
                break
            }
            curr -= 1
        }
        // 追加到最后一个
        if (curr >= 0) {
            val section = sections.removeAt(position)
            sections.add(curr, section)
            // 如果刚刚移动的是一行, 且下一行是空行, 则一起移动
            val nextLinePosition = position + 1
            if (!section.canExpand() && nextLinePosition < sections.size && sections[nextLinePosition].isBlank()) {
                val blankLine = sections.removeAt(nextLinePosition)
                sections.add(curr + 1, blankLine)
            }
            adapter.notifyDataSetChanged()
            delegate.getNote().content = Section.joinSections(sections)
            delegate.setContentChanged(true)
        }
    }

    //endregion
    //region delegate

    lateinit var delegate: NoteEditFragment.NoteEditFragmentDelegate

    private val adapterDelegate = object : NextAdapter.NextAdapterDelegate {

        override fun onItemClicked(itemView: View, position: Int) {
        }

        override fun onItemDoubleClicked(itemView: View, position: Int) {
            val section = adapter.sections[position]
            if (section.canFold()) {
                if (Section.fold(adapter.sections, position)) {
                    adapter.notifyDataSetChanged()
                }
            } else if (section.canExpand()) {
                if (Section.expand(adapter.sections, position)) {
                    adapter.notifyDataSetChanged()
                }
            }
        }

        override fun onItemMoved() {
            delegate.getNote().content = Section.joinSections(adapter.sections)
            delegate.setContentChanged(true)
        }

        override fun onItemMoreClicked(itemView: View, position: Int) {
            popNextItemMenu(itemView, position)
        }
    }

    //endregion
    //region adapter

    private class NextAdapter : RecyclerView.Adapter<NextViewHolder>() {

        val sections: MutableList<Section> = mutableListOf()

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NextViewHolder {
            val root = LayoutInflater.from(parent.context).inflate(R.layout.note_item_next, null, false)
            val layoutParameter = RecyclerView.LayoutParams(
                RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT)
            root.layoutParams = layoutParameter
            return NextViewHolder(root)
        }

        override fun getItemCount(): Int {
            return sections.size
        }

        override fun onBindViewHolder(holder: NextViewHolder, position: Int) {
            holder.delegate = viewHolderDelegate

            val section = sections[position]
            holder.bind(position, section)
        }

        fun setData(sections: List<Section>) {
            this.sections.clear()
            this.sections.addAll(sections)
        }

        fun onMove(fromPosition: Int, toPosition: Int) {
            Collections.swap(sections, fromPosition, toPosition)
            // 通知数据移动
            notifyItemMoved(fromPosition, toPosition)
            delegate.onItemMoved()
        }

        private var viewHolderDelegate = object : NextViewHolder.NextViewHolderDelegate {

            override fun onClicked(itemView: View, position: Int) {
                delegate.onItemClicked(itemView, position)
            }

            override fun onDoubleClicked(itemView: View, position: Int) {
                delegate.onItemDoubleClicked(itemView, position)
            }

            override fun onMoreClicked(itemView: View, position: Int) {
                delegate.onItemMoreClicked(itemView, position)
            }
        }

        lateinit var delegate: NextAdapterDelegate

        interface NextAdapterDelegate {

            fun onItemClicked(itemView: View, position: Int)

            fun onItemDoubleClicked(itemView: View, position: Int)

            fun onItemMoved()

            fun onItemMoreClicked(itemView: View, position: Int)
        }
    }

    private class NextViewHolder(val root: View) : RecyclerView.ViewHolder(root) {

        private var folded: View = root.findViewById(R.id.folded)
        private var lineView: TextView = root.findViewById(R.id.line)
        private var btnMore: View = root.findViewById(R.id.btnMore)

        private var firstClickTime: Long = 0
        private var doubleClickTimeout: Long = ViewConfiguration.getDoubleTapTimeout().toLong()
        private var handler = Handler()

        fun bind(position: Int, section: Section) {
            root.setOnClickListener {
                val now = System.currentTimeMillis()
                if (now - firstClickTime < doubleClickTimeout) {
                    handler.removeCallbacksAndMessages(null)
                    firstClickTime = 0
                    delegate.onDoubleClicked(it, position)
                } else {
                    firstClickTime = now
                    handler.postDelayed({
                        delegate.onClicked(it, position)
                        firstClickTime = 0
                    }, doubleClickTimeout)
                }
            }

            folded.isInvisible = !section.canExpand()
            lineView.text = section.text

            btnMore.setOnClickListener {
                delegate.onMoreClicked(it, position)
            }

            // decorate blank-line
            val blankLine = section.isBlank()
            root.setBackgroundResource(
                if (blankLine) R.color.note_next_bg_empty else android.R.color.transparent)
            lineView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, if (blankLine) 0f else 16f)
            btnMore.visibility = if (blankLine) View.GONE else View.VISIBLE
        }

        lateinit var delegate: NextViewHolderDelegate

        interface NextViewHolderDelegate {

            fun onClicked(itemView: View, position: Int)

            fun onDoubleClicked(itemView: View, position: Int)

            fun onMoreClicked(itemView: View, position: Int)
        }
    }

    private val touchCallback = object : ItemTouchHelper.Callback() {

        override fun isLongPressDragEnabled(): Boolean {
            return true
        }

        override fun getMovementFlags(recyclerView: RecyclerView?, viewHolder: RecyclerView.ViewHolder?): Int {
            //首先回调的方法,返回int表示是否监听该方向
            val dragFlag = ItemTouchHelper.DOWN or ItemTouchHelper.UP //拖拽
            val swipeFlag = 0 //侧滑删除
            return makeMovementFlags(dragFlag, swipeFlag);
        }

        override fun onMove(recyclerView: RecyclerView?,
                            viewHolder: RecyclerView.ViewHolder?, target: RecyclerView.ViewHolder?): Boolean {
            val vh = viewHolder ?: return false
            val tgt = target ?: return false
            adapter.onMove(vh.adapterPosition, tgt.adapterPosition)
            return true
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder?, direction: Int) {
        }

        override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
            if (actionState != 0) {
                viewHolder?.itemView?.alpha = 0.8f
            }
            super.onSelectedChanged(viewHolder, actionState)
        }

        override fun clearView(recyclerView: RecyclerView?, viewHolder: RecyclerView.ViewHolder?) {
            super.clearView(recyclerView, viewHolder);
            viewHolder?.itemView?.alpha = 1.0f
            adapter.notifyDataSetChanged()
        }
    }

    //endregion
    //region Section

    /**
     * section 是一个嵌套的数据结构
     */
    private class Section(var line: String?) {

        val text: String
            get() = when {
                isLine() -> line!!
                sections.isNotEmpty() -> sections[0].text
                else -> throw IllegalArgumentException("Invalid section")
            }

        val lines: Int
            get() = when {
                isLine() -> 1
                sections.isNotEmpty() -> sections.sumBy { it.lines }
                else -> throw IllegalArgumentException("Invalid section")
            }

        var sections: MutableList<Section> = mutableListOf()

        constructor(sections: List<Section>) : this(null) {
            this.sections.addAll(sections)
        }

        fun isLine(): Boolean = line != null && sections.isEmpty()

        fun isBlank(): Boolean = isLine() && line!!.isBlank()

        fun canFold(): Boolean = isLine() && !isBlank()

        fun canExpand(): Boolean = !isLine()

        override fun equals(other: Any?): Boolean {
            if (other !is Section) {
                return false
            }
            if (isLine()) {
                if (!other.isLine()) {
                    return false
                }
                return line == other.line
            } else {
                if (other.isLine()) {
                    return false
                }
                if (sections.size != other.sections.size) {
                    return false
                }
                for (index in 0 until sections.size) {
                    if (sections[index] != other.sections[index]) {
                        return false
                    }
                }
                return true
            }
        }

        companion object {

            fun lines2Sections(lines: List<String>) = lines.map { Section(it) }

            fun joinSections(sections: List<Section>): String {
                return sections.joinToString("\n", transform = {
                    if (it.isLine()) it.text else joinSections(it.sections)
                })
            }

            /**
             * @return true if folded
             */
            fun fold(sections: MutableList<Section>, index: Int): Boolean {
                val section = sections[index]
                if (!section.canFold()) return false
                // 找到和 section 同级别的 section, 折叠
                val (testMarkSource, indent, _) = DayOneStrategy.detectPrecedingMark(section.line!!)
                val testMark = Mark.fromString(testMarkSource)
                var nextSectionStartIndex = index + 1
                var untilEnd = false
                for (i in nextSectionStartIndex until sections.size) {
                    val aSection = sections[i]
                    if (aSection.isBlank()) {
                        // pass
                    } else if (aSection.isLine()) {
                        // 找到一行, 属于同级
                        if (findNextSection(testMarkSource, indent, testMark, aSection)) {
                            nextSectionStartIndex = i
                            break
                        }
                    } else {
                        // 找到一个 section, 该 section 的第一行属于同级
                        var firstSection = aSection.sections[0]
                        while (firstSection.canExpand()) {
                            firstSection = firstSection.sections[0]
                        }
                        if (findNextSection(testMarkSource, indent, testMark, firstSection)) {
                            nextSectionStartIndex = i
                            break
                        }
                    }
                    if (i == sections.size - 1) {
                        untilEnd = true
                    }
                }
                // 没找到, 但并不代表不能折叠, 但这个处理比较复杂
                if (nextSectionStartIndex == index + 1) {
                    if (testMark == Mark.H && untilEnd) {
                        // 当 testMark 为 H 标签时, 找到了结尾认为可以折叠
                        nextSectionStartIndex = sections.size
                    } else {
                        // TODO: Mark.L 系列标签, 如果找到了结尾, 且一直都是缩进的, 也认为可以折叠
                        return false
                    }
                }
                // 将折叠的元素新建 section, 放到 sections 里, index 对应的位置
                val folded = sections.removeRange(index..nextSectionStartIndex)
                sections.add(index, Section(folded))
                return true
            }

            private fun findNextSection(markSource: String, indent: Indent, mark: Mark, aSection: Section): Boolean {
                val (targetMarkSource, aIndent, _) = DayOneStrategy.detectPrecedingMark(aSection.line!!)
                val targetMark = Mark.fromString(targetMarkSource)
                if (mark == Mark.H) {
                    // mark 为 H 标签, 只在 H 标签之间折叠, 不考虑缩进
                    if (targetMark == Mark.H) {
                        if (markSource.length >= targetMarkSource.length) {
                            return true
                        }
                    }
                } else if (mark == Mark.NONE) {
                    // mark 为无标签, 可以折叠同缩进的 list 或 TD, 或任何缩进标签
                    if (targetMark.isList() || targetMark == Mark.TD || targetMark == Mark.QT) {
                        if (indent.length > aIndent.length) {
                            return true
                        }
                    } else {
                        if (indent.length >= aIndent.length) {
                            return true
                        }
                    }
                } else {
                    // mark 为其他标签, 只能折叠缩进标签
                    if (indent.length >= aIndent.length) {
                        return true
                    }
                }
                return false
            }

            /**
             * @return true if expanded
             */
            fun expand(sections: MutableList<Section>, index: Int): Boolean {
                val section = sections[index]
                if (!section.canExpand()) return false
                sections.removeAt(index)
                sections.addAll(index, section.sections)
                return true
            }

            fun paragraph(sections: List<Section>, index: Int): Pair<Int, Int> {
                val lineStartIndex = sections.subList(0, index).sumBy { it.lines }
                val paragraphSize = sections[index].lines
                return Pair(lineStartIndex, paragraphSize)
            }
        }
    }

    //endregion
}

