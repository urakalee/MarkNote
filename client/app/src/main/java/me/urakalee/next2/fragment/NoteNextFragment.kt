package me.urakalee.next2.fragment

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import kotlinx.android.synthetic.main.fragment_note_next.*
import me.shouheng.notepal.R
import me.urakalee.next2.base.fragment.BaseModelFragment
import me.urakalee.next2.model.Note

/**
 * @author Uraka.Lee
 */
class NoteNextFragment : BaseModelFragment<Note>() {

    private lateinit var adapter: NextAdapter

    override val layoutResId: Int
        get() = R.layout.fragment_note_next

    override fun afterViewCreated(savedInstanceState: Bundle?) {
        listView.layoutManager = LinearLayoutManager(context)

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
    }

    private class NextViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        fun bind(line: String) {
            (itemView as? TextView)?.text = line
        }
    }
}