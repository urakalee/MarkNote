package me.urakalee.next2.fragment

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.PopupMenu
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import kotlinx.android.synthetic.main.note_fragment_next.*
import me.shouheng.notepal.R
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
            adapter.setData(Section.lines2Sections(lines))
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
                    val section = adapter.sections[position]
                    if (section.isFolded()) {
                        Section.expand(adapter.sections, position)
                        adapter.notifyDataSetChanged()
                    } else if (Section.fold(adapter.sections, position)) {
                        adapter.notifyDataSetChanged()
                    }
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
            popNextItemMenu(itemView, position)
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
        }

        lateinit var delegate: NextAdapterDelegate

        interface NextAdapterDelegate {

            fun onItemClicked(itemView: View, position: Int)

            fun onItemMoved()
        }
    }

    private class NextViewHolder(val root: View) : RecyclerView.ViewHolder(root) {

        private var folded: View = root.findViewById(R.id.folded)
        private var lineView: TextView = root.findViewById(R.id.line)

        fun bind(position: Int, section: Section) {
            root.setOnClickListener {
                delegate.onClicked(it, position)
            }

            folded.isInvisible = !section.isFolded()
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

        fun isFolded(): Boolean = !isLine()

        fun isBlank(): Boolean = isLine() && line!!.isBlank()

        override fun toString(): String {
            return when {
                line != null -> line!!
                sections.isNotEmpty() -> sections[0].toString()
                else -> throw IllegalArgumentException("Invalid section")
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
                if (section.isFolded()) return false
                // 找到和 section 同级别的 section, 折叠
                val (precedingMarkSource, indent, _) = DayOneStrategy.detectPrecedingMark(section.line!!)
                val mark = Mark.fromString(precedingMarkSource)
                var nextSectionStartIndex = index + 1
                for (i in nextSectionStartIndex until sections.size) {
                    val aSection = sections[i]
                    if (aSection.isBlank()) {
                        // pass
                    } else if (aSection.isLine()) {
                        val (aPrecedingMarkSource, aIndent, _)
                                = DayOneStrategy.detectPrecedingMark(aSection.line!!)
                        val aMark = Mark.fromString(aPrecedingMarkSource)
                        if (mark == Mark.H && aMark == Mark.H) {
                            // mark 为 H 标签, 找到了同级或上级 H 标签, 则结束
                            if (precedingMarkSource.length >= aPrecedingMarkSource.length) {
                                nextSectionStartIndex = i
                                break
                            }
                        } else if (aMark == Mark.H) {
                            // mark 为无标签或其它标签, 找到了 H 标签, 则结束
                            nextSectionStartIndex = i
                            break
                        } else if (mark == aMark) {
                            // mark 为无标签或其它标签, 找到了同缩进或上级缩进的无标签或其它标签, 则结束
                            if (indent.length >= aIndent.length) {
                                nextSectionStartIndex = i
                                break
                            }
                        }
                    }
                    nextSectionStartIndex += 1
                }
                if (nextSectionStartIndex == index + 1) return false
                // 往回退掉所有的空行(空行不参与折叠)
                /*
                for (i in nextSectionStartIndex - 1 downTo index + 1) {
                    val aSection = sections[i]
                    if (aSection.isBlank()) {
                        nextSectionStartIndex -= 1
                    } else {
                        break
                    }
                }
                if (nextSectionStartIndex == index + 1) return false
                */
                // 将折叠的元素新建 section, 放到 sections 里, index 对应的位置
                val folded = sections.removeRange(index..nextSectionStartIndex)
                sections.add(index, Section(folded))
                return true
            }

            /**
             * @return true if expanded
             */
            fun expand(sections: MutableList<Section>, index: Int): Boolean {
                val section = sections[index]
                if (!section.isFolded()) return false
                sections.removeAt(index)
                sections.addAll(index, section.sections)
                return true
            }
        }
    }

    //endregion
}

