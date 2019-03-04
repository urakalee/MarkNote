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
import me.urakalee.markdown.Indent
import me.urakalee.markdown.Mark
import me.urakalee.markdown.action.DayOneStrategy
import me.urakalee.next2.base.fragment.BaseModelFragment
import me.urakalee.next2.model.Note
import me.urakalee.ranger.extension.isInvisible
import me.urakalee.ranger.extension.removeRange
import java.util.*

/**
 * @author Uraka.Lee
 */
class NoteNextFragment : BaseModelFragment<Note>() {

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
            // 尽可能保持折叠状态
            val sections = mutableListOf<Section>()
            sections.addAll(Section.lines2Sections(lines))
            val oldSections = adapter.sections
            // 对比找到第一个 fold 不同的 section
            for (index in 0 until oldSections.size) {
                val oldSection = oldSections[index]
                if (oldSection.canExpand()) {
                    Section.fold(sections, index)
                    if (oldSection != sections[index]) {
                        // 操作数据之前先恢复之前的 fold
                        Section.expand(sections, index)
                        oldSections.removeRange(index..oldSections.size)
                        oldSections.addAll(sections.subList(index, sections.size))
                        break
                    }
                }
            }
            adapter.notifyDataSetChanged()
        }
    }

    //region menu

    private fun popNextItemMenu(view: View, position: Int) {
        val contextNonNull = context ?: return
        val popupMenu = PopupMenu(contextNonNull, view)
        popupMenu.inflate(R.menu.note_menu_next_item)
        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_fold -> {
                }
                R.id.action_delete -> {

                }
            }
            true
        }
        popupMenu.show()
    }

    private fun configPopMenu(popupMenu: PopupMenu) {
        popupMenu.menu.findItem(R.id.action_fold).isVisible = true
        popupMenu.menu.findItem(R.id.action_delete).isVisible = true
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

        override fun onItemLongClicked(itemView: View, position: Int) {
//            popNextItemMenu(itemView, position)
        }

        override fun onItemMoved() {
            delegate.getNote().content = Section.joinSections(adapter.sections)
            delegate.setContentChanged(true)
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
            //通知数据移动
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

            override fun onLongClicked(itemView: View, position: Int) {
                delegate.onItemLongClicked(itemView, position)
            }
        }

        lateinit var delegate: NextAdapterDelegate

        interface NextAdapterDelegate {

            fun onItemClicked(itemView: View, position: Int)

            fun onItemDoubleClicked(itemView: View, position: Int)

            fun onItemLongClicked(itemView: View, position: Int)

            fun onItemMoved()
        }
    }

    private class NextViewHolder(val root: View) : RecyclerView.ViewHolder(root) {

        private var folded: View = root.findViewById(R.id.folded)
        private var lineView: TextView = root.findViewById(R.id.line)

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
            root.setOnLongClickListener {
                delegate.onLongClicked(it, position)
                true
            }

            folded.isInvisible = !section.canExpand()
            lineView.text = section.toString()

            // decorate blank-line
            val blankLine = section.isBlank()
            root.setBackgroundResource(
                    if (blankLine) R.color.note_next_bg_empty else android.R.color.transparent)
            lineView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, if (blankLine) 0f else 16f)
        }

        lateinit var delegate: NextViewHolderDelegate

        interface NextViewHolderDelegate {

            fun onClicked(itemView: View, position: Int)

            fun onDoubleClicked(itemView: View, position: Int)

            fun onLongClicked(itemView: View, position: Int)
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

        var sections: MutableList<Section> = mutableListOf()

        constructor(sections: List<Section>) : this(null) {
            this.sections.addAll(sections)
        }

        fun isLine(): Boolean = line != null && sections.isEmpty()

        fun isBlank(): Boolean = isLine() && line!!.isBlank()

        fun canFold(): Boolean = isLine() && !isBlank()

        fun canExpand(): Boolean = !isLine()

        override fun toString(): String {
            return when {
                line != null -> line!!
                sections.isNotEmpty() -> sections[0].toString()
                else -> throw IllegalArgumentException("Invalid section")
            }
        }

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
                    if (it.isLine()) it.toString() else joinSections(it.sections)
                })
            }

            /**
             * @return true if folded
             */
            fun fold(sections: MutableList<Section>, index: Int): Boolean {
                val section = sections[index]
                if (!section.canFold()) return false
                // 找到和 section 同级别的 section, 折叠
                val (precedingMarkSource, indent, _) = DayOneStrategy.detectPrecedingMark(section.line!!)
                val mark = Mark.fromString(precedingMarkSource)
                var nextSectionStartIndex = index + 1
                for (i in nextSectionStartIndex until sections.size) {
                    val aSection = sections[i]
                    if (aSection.isBlank()) {
                        // pass
                    } else if (aSection.isLine()) {
                        if (findNextSection(precedingMarkSource, indent, mark, aSection)) {
                            nextSectionStartIndex = i
                            break
                        }
                    } else {
                        var firstSection = aSection.sections[0]
                        while (firstSection.canExpand()) {
                            firstSection = firstSection.sections[0]
                        }
                        if (findNextSection(precedingMarkSource, indent, mark, firstSection)) {
                            nextSectionStartIndex = i
                            break
                        }
                    }
                }
                if (nextSectionStartIndex == index + 1) return false
                // 将折叠的元素新建 section, 放到 sections 里, index 对应的位置
                val folded = sections.removeRange(index..nextSectionStartIndex)
                sections.add(index, Section(folded))
                return true
            }

            private fun findNextSection(precedingMarkSource: String, indent: Indent, mark: Mark, aSection: Section): Boolean {
                val (aPrecedingMarkSource, aIndent, _)
                        = DayOneStrategy.detectPrecedingMark(aSection.line!!)
                val aMark = Mark.fromString(aPrecedingMarkSource)
                if (mark == Mark.H) {
                    // mark 为 H 标签, 只在 H 标签之间折叠, 不考虑缩进
                    if (aMark == Mark.H) {
                        if (precedingMarkSource.length >= aPrecedingMarkSource.length) {
                            return true
                        }
                    }
                } else if (mark == Mark.NONE) {
                    // mark 为无标签, 可以折叠同缩进的 list 或 TD, 或任何缩进标签
                    if (aMark.isList() || aMark == Mark.TD || aMark == Mark.QT) {
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
        }
    }

    //endregion
}

