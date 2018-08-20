package me.urakalee.next2.fragment

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import kotlinx.android.synthetic.main.fragment_note_next.*
import me.shouheng.notepal.R
import me.urakalee.next2.base.fragment.BaseModelFragment
import me.urakalee.next2.model.Note
import java.util.*

/**
 * @author Uraka.Lee
 */
class NoteNextFragment : BaseModelFragment<Note>() {

    private lateinit var adapter: NextAdapter

    override val layoutResId: Int
        get() = R.layout.fragment_note_next

    override fun afterViewCreated(savedInstanceState: Bundle?) {
        listView.layoutManager = LinearLayoutManager(context)
        val helper = ItemTouchHelper(touchCallback)
        helper.attachToRecyclerView(listView)

        val lines = delegate.getNote().content?.lines() ?: listOf()
        adapter = NextAdapter(lines)
        adapter.delegate = adapterDelegate

        listView.adapter = adapter
        adapter.notifyDataSetChanged()
    }

    fun refreshData() {
        val lines = delegate.getNote().content?.lines() ?: listOf()
        adapter.lines = lines
        adapter.notifyDataSetChanged()
    }

    //region delegate

    lateinit var delegate: NoteEditFragment.NoteEditFragmentDelegate

    private val adapterDelegate = object : NextAdapter.NextAdapterDelegate {

        override fun onMove() {
            delegate.getNote().content = adapter.lines.joinToString("\n")
            delegate.setContentChanged(true)
        }
    }

    //endregion
    //region adapter

    private class NextAdapter(var lines: List<String>) : RecyclerView.Adapter<NextViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NextViewHolder {
            val root = LayoutInflater.from(parent.context).inflate(R.layout.note_item_next, null, false)
            val layoutParameter = RecyclerView.LayoutParams(
                    RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT)
            root.layoutParams = layoutParameter
            return NextViewHolder(root)
        }

        override fun getItemCount(): Int {
            return lines.size
        }

        override fun onBindViewHolder(holder: NextViewHolder, position: Int) {
            val line = lines[position]
            holder.bind(line)
        }

        fun onMove(fromPosition: Int, toPosition: Int) {
            Collections.swap(lines, fromPosition, toPosition)
            //通知数据移动
            notifyItemMoved(fromPosition, toPosition)
            delegate.onMove()
        }

        lateinit var delegate: NextAdapterDelegate

        interface NextAdapterDelegate {

            fun onMove()
        }
    }

    private class NextViewHolder(val root: View) : RecyclerView.ViewHolder(root) {

        private var lineView: TextView = root.findViewById(R.id.line)

        fun bind(line: String) {
            lineView.text = line
            val blankLine = line.isBlank()
            root.setBackgroundResource(
                    if (blankLine) R.color.note_next_bg_empty else android.R.color.transparent)
            lineView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, if (blankLine) 0f else 16f)
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
}