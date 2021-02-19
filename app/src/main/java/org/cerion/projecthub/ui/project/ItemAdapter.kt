package org.cerion.projecthub.ui.project

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.android.material.chip.Chip
import com.woxthebox.draglistview.DragItemAdapter
import org.cerion.projecthub.R
import org.cerion.projecthub.databinding.ListItemCardIssueBinding
import org.cerion.projecthub.databinding.ListItemCardNoteBinding
import org.cerion.projecthub.model.Card
import org.cerion.projecthub.model.IssueCard
import org.cerion.projecthub.model.NoteCard

internal class ItemAdapter : DragItemAdapter<Card?, DragItemAdapter.ViewHolder>() {

    private val mGrabHandleId = R.id.root

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)

        return when(viewType) {
            TypeNote -> NoteViewHolder(ListItemCardNoteBinding.inflate(layoutInflater, parent, false))
            TypeIssue -> IssueViewHolder(ListItemCardIssueBinding.inflate(layoutInflater, parent, false))
            else -> throw NotImplementedError()
        }
    }

    override fun getItemViewType(position: Int): Int = if (mItemList[position] is IssueCard) TypeIssue else TypeNote

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)

        when (val item = itemList[position]) {
            is NoteCard -> (holder as NoteViewHolder).bind(item)
            is IssueCard -> (holder as IssueViewHolder).bind(item as IssueCard)
        }
    }

    override fun getUniqueItemId(position: Int): Long {
        return mItemList[position]!!.id.toLong()
    }

    /*
    internal inner class NoteViewHolder(itemView: View) : DragItemAdapter.ViewHolder(itemView, mGrabHandleId, false) {
        //var mText: TextView

        init {
            //mText = itemView.findViewById<View>(R.id.text) as TextView
        }
        override fun onItemClicked(view: View) {
            Toast.makeText(view.context, "Item clicked", Toast.LENGTH_SHORT).show()
        }

        override fun onItemLongClicked(view: View): Boolean {
            Toast.makeText(view.context, "Item long clicked", Toast.LENGTH_SHORT).show()
            return true
        }
    }

    internal inner class IssueViewHolder(itemView: View) : DragItemAdapter.ViewHolder(itemView, mGrabHandleId, false) {
        //var mText: TextView

        init {
            //mText = itemView.findViewById<View>(R.id.text) as TextView
        }
        override fun onItemClicked(view: View) {
            Toast.makeText(view.context, "Item clicked", Toast.LENGTH_SHORT).show()
        }

        override fun onItemLongClicked(view: View): Boolean {
            Toast.makeText(view.context, "Item long clicked", Toast.LENGTH_SHORT).show()
            return true
        }
    }
     */

    inner class NoteViewHolder(val binding: ListItemCardNoteBinding) : DragItemAdapter.ViewHolder(binding.root, mGrabHandleId, true) {

        fun bind(item: NoteCard) {
            binding.card = item
            binding.createdBy.text = "Added by ${item.creator}"
            binding.executePendingBindings()
        }

        override fun onItemClicked(view: View) {
            Toast.makeText(view.context, "Item clicked", Toast.LENGTH_SHORT).show()
        }

        /*
        override fun onClick() {
            listener.onClick(binding.card!!)
        }

        override fun onCreateContextMenu(menu: ContextMenu?, view: View, menuInfo: ContextMenu.ContextMenuInfo?) {
            val card = binding.card!!

            menu?.apply {
                add(Menu.NONE, view.id, Menu.NONE, "Move").setOnMenuItemClickListener {
                    listener.move(card)
                    true
                }
                /* TODO not sure if this works yet
                add(Menu.NONE, view.id, Menu.NONE, "Archive").setOnMenuItemClickListener {
                    listener.onArchive(card)
                    true
                }
                 */
                add(Menu.NONE, view.id, Menu.NONE, "Delete").setOnMenuItemClickListener {
                    listener.onDelete(card)
                    true
                }
            }
        }
         */
    }

    inner class IssueViewHolder(private val binding: ListItemCardIssueBinding) : DragItemAdapter.ViewHolder(binding.root, mGrabHandleId, true) {

        fun bind(item: IssueCard) {
            binding.card = item
            // FEATURE add repository if this is a multi repo project (associated to user/org)
            binding.openedBy.text = "#${item.number} opened by ${item.author}"

            binding.labels.removeAllViews()
            item.labels.forEach {
                val chip = LayoutInflater.from(binding.root.context).inflate(
                    R.layout.label_chip,
                    binding.labels,
                    false
                ) as Chip
                chip.text = it.name
                chip.chipBackgroundColor = ColorStateList.valueOf(it.color)
                binding.labels.addView(chip)
            }

            //binding.root.setOnClickListener(this)
            binding.executePendingBindings()
        }

        override fun onItemClicked(view: View) {
            Toast.makeText(view.context, "Item clicked", Toast.LENGTH_SHORT).show()
        }

        /*
        override fun onCreateContextMenu(menu: ContextMenu?, view: View, menuInfo: ContextMenu.ContextMenuInfo?) {
            val card = binding.card!!

            menu?.apply {
                add(Menu.NONE, view.id, Menu.NONE, "Move").setOnMenuItemClickListener {
                    listener.move(card)
                    true
                }
                add(Menu.NONE, view.id, Menu.NONE, "Archive").setOnMenuItemClickListener {
                    listener.onArchive(card)
                    true
                }
                add(Menu.NONE, view.id, Menu.NONE, if(card.closed) "Open" else "Close").setOnMenuItemClickListener {
                    listener.onCloseOrOpen(card)
                    true
                }
            }
        }

        override fun onClick() {
            listener.onClick(binding.card!!)
        }
         */
    }

    companion object {
        private const val TypeNote = 0
        private const val TypeIssue = 1
    }
}