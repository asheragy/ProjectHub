package org.cerion.projecthub.ui.project

import android.content.res.ColorStateList
import android.view.*
import com.google.android.material.chip.Chip
import com.woxthebox.draglistview.DragItemAdapter
import org.cerion.projecthub.R
import org.cerion.projecthub.databinding.ListItemCardIssueBinding
import org.cerion.projecthub.databinding.ListItemCardNoteBinding
import org.cerion.projecthub.model.Card
import org.cerion.projecthub.model.IssueCard
import org.cerion.projecthub.model.NoteCard
import org.cerion.projecthub.ui.setCardImage
import org.cerion.projecthub.ui.setFormattedText


interface CardListener {
    // All types
    fun onClick(card: Card)
    fun onArchive(card: Card)
    fun onDelete(card: Card)

    // Issues
    fun onCloseOrOpen(issue: IssueCard)
    // onRemoveFromProject
    // onChangeLabels

    // Notes
    fun onConvertToIssue(note: NoteCard)
}

internal class CardListAdapter(private val listener: CardListener) : DragItemAdapter<Card?, DragItemAdapter.ViewHolder>() {

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
            is IssueCard -> (holder as IssueViewHolder).bind(item)
        }
    }

    override fun getUniqueItemId(position: Int): Long {
        return mItemList[position]!!.id.toLong()
    }

    inner class NoteViewHolder(val binding: ListItemCardNoteBinding) : DragItemAdapter.ViewHolder(binding.root, mGrabHandleId, true), View.OnCreateContextMenuListener {

        private var note: NoteCard? = null

        fun bind(item: NoteCard) {
            note = item
            binding.createdBy.text = "Added by ${item.creator}"
            setFormattedText(binding.title, item.note)
            binding.root.setOnCreateContextMenuListener(this)
            binding.menu.setOnClickListener {
                it.showContextMenu()
            }
        }

        override fun onItemClicked(view: View) {
            listener.onClick(note!!)
        }

        override fun onCreateContextMenu(menu: ContextMenu?, view: View, menuInfo: ContextMenu.ContextMenuInfo?) {
            val card = note!!

            menu?.apply {
                add(Menu.NONE, view.id, Menu.NONE, "Archive").setOnMenuItemClickListener {
                    listener.onArchive(card)
                    true
                }
                add(Menu.NONE, view.id, Menu.NONE, "Convert to issue").setOnMenuItemClickListener {
                    listener.onConvertToIssue(card)
                    true
                }
                add(Menu.NONE, view.id, Menu.NONE, "Delete").setOnMenuItemClickListener {
                    listener.onDelete(card)
                    true
                }
            }
        }
    }

    inner class IssueViewHolder(private val binding: ListItemCardIssueBinding) : DragItemAdapter.ViewHolder(binding.root, mGrabHandleId, true), View.OnCreateContextMenuListener {
        private var issue: IssueCard? = null

        fun bind(item: IssueCard) {
            issue = item
            // FEATURE add repository if this is a multi repo project (associated to user/org)
            binding.openedBy.text = "#${item.number} opened by ${item.author}"
            binding.icon.setCardImage(item)

            binding.labels.removeAllViews()
            item.labels.forEach {
                val chip = LayoutInflater.from(binding.root.context).inflate(R.layout.label_chip, binding.labels, false) as Chip
                chip.text = it.name
                chip.chipBackgroundColor = ColorStateList.valueOf(it.color)
                binding.labels.addView(chip)
            }

            binding.root.setOnCreateContextMenuListener(this)
            binding.menu.setOnClickListener {
                it.showContextMenu()
            }
        }

        override fun onItemClicked(view: View) {
            listener.onClick(issue!!)
        }

        override fun onCreateContextMenu(menu: ContextMenu?, view: View, menuInfo: ContextMenu.ContextMenuInfo?) {
            val card = issue!!

            menu?.apply {
                add(Menu.NONE, view.id, Menu.NONE, "Archive").setOnMenuItemClickListener {
                    listener.onArchive(card)
                    true
                }
                add(Menu.NONE, view.id, Menu.NONE, if(card.closed) "Open" else "Close").setOnMenuItemClickListener {
                    listener.onCloseOrOpen(card)
                    true
                }
                add(Menu.NONE, view.id, Menu.NONE, "Delete").setOnMenuItemClickListener {
                    listener.onDelete(card)
                    true
                }
            }
        }
    }

    companion object {
        private const val TypeNote = 0
        private const val TypeIssue = 1
    }
}