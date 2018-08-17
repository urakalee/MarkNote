package me.urakalee.next2.fragment

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
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

        listView.adapter = adapter
        adapter.notifyDataSetChanged()
    }

    lateinit var delegate: NoteNextFragmentDelegate

    interface NoteNextFragmentDelegate {

        fun getNote(): Note
    }

    private class NextAdapter(val lines: List<String>) : RecyclerView.Adapter<NextViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NextViewHolder {
            return NextViewHolder(TextView(parent.context))
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
            notifyItemMoved(fromPosition, toPosition);
        }
    }

    private class NextViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        fun bind(line: String) {
            (itemView as? TextView)?.text = line
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

        override fun onMove(recyclerView: RecyclerView?, viewHolder: RecyclerView.ViewHolder?, target: RecyclerView.ViewHolder?): Boolean {
            val vh = viewHolder ?: return false
            val tgt = target ?: return false
            adapter.onMove(vh.adapterPosition, tgt.adapterPosition)
            return true
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder?, direction: Int) {
        }

        override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
            if (actionState != 0) {
                viewHolder?.itemView?.alpha = 0.9f
            }
            super.onSelectedChanged(viewHolder, actionState)
        }

        override fun clearView(recyclerView: RecyclerView?, viewHolder: RecyclerView.ViewHolder?) {
            super.clearView(recyclerView, viewHolder);
            viewHolder?.itemView?.alpha = 1.0f
            adapter.notifyDataSetChanged()
        }
    }
}