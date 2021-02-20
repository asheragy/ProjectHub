package org.cerion.projecthub.ui.project

import android.content.ClipData
import android.content.res.ColorStateList
import android.graphics.Point
import android.view.*
import android.view.View.DragShadowBuilder
import android.view.View.OnLongClickListener
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import org.cerion.projecthub.R
import org.cerion.projecthub.databinding.ListItemCardIssueBinding
import org.cerion.projecthub.databinding.ListItemCardNoteBinding
import org.cerion.projecthub.model.Card
import org.cerion.projecthub.model.IssueCard
import org.cerion.projecthub.model.NoteCard

interface CardListener {
    fun onArchive(card: Card)
    fun onClick(card: Card)
    fun onDelete(note: NoteCard)
    fun onCloseOrOpen(issue: IssueCard)
}

class ColumnCardListAdapter(private val listener: CardListener) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    companion object {
        private const val TypeNote = 0
        private const val TypeIssue = 1
    }

    private var items = mutableListOf<Card>()

    fun setItems(items: List<Card>) {
        this.items = items.toMutableList() // Mutable so re-ordering works
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)

        return if (viewType == TypeNote)
            NoteViewHolder(ListItemCardNoteBinding.inflate(layoutInflater, parent, false))
        else
            IssueViewHolder(ListItemCardIssueBinding.inflate(layoutInflater, parent, false))
    }

    override fun getItemViewType(position: Int): Int = if (items[position] is NoteCard) TypeNote else TypeIssue
    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = items[position]
        if (item is NoteCard)
            (holder as NoteViewHolder).bind(item)
        else
            (holder as IssueViewHolder).bind(item as IssueCard)

        holder.itemView.tag = position // to get original item position for DragListener
    }

    fun moveItem(from: Int, to: Int) {
        // This might be wrong if the jump is more than 1 item (which is hard to reproduce), VM will fix on final move though
        items[from] = items[to].also { items[to] = items[from] }
        notifyItemMoved(from, to)
    }

    private val onLongClickListener = OnLongClickListener { view ->
        val data = ClipData.newPlainText("", "")
        val shadowBuilder = DragShadowBuilder(view)
        view.startDragAndDrop(data, shadowBuilder, view, 0)
        true
    }

    // TODO remove base class its not that useful anymore
    abstract inner class BaseViewHolder(binding: ViewDataBinding) : RecyclerView.ViewHolder(binding.root), View.OnClickListener, View.OnCreateContextMenuListener {

        init {
            // TODO need to replace context menu stuff if using touch listener to move
            //binding.root.setOnCreateContextMenuListener(this)
            binding.root.setOnLongClickListener(onLongClickListener)
        }

        override fun onClick(view: View) {
            onClick()
        }

        abstract fun onClick()
    }

    inner class NoteViewHolder(val binding: ListItemCardNoteBinding) : BaseViewHolder(binding) {
        fun bind(item: NoteCard) {
            binding.card = item
            binding.createdBy.text = "Added by ${item.creator}"
            binding.root.setOnClickListener(this)
            binding.executePendingBindings()
        }

        override fun onClick() {
            listener.onClick(binding.card!!)
        }

        override fun onCreateContextMenu(menu: ContextMenu?, view: View, menuInfo: ContextMenu.ContextMenuInfo?) {
            val card = binding.card!!

            menu?.apply {
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
    }

    inner class IssueViewHolder(private val binding: ListItemCardIssueBinding) : BaseViewHolder(binding) {

        fun bind(item: IssueCard) {
            binding.card = item
            // FEATURE add repository if this is a multi repo project (associated to user/org)
            binding.openedBy.text = "#${item.number} opened by ${item.author}"

            binding.labels.removeAllViews()
            item.labels.forEach {
                val chip = LayoutInflater.from(binding.root.context).inflate(R.layout.label_chip, binding.labels, false) as Chip
                chip.text = it.name
                chip.chipBackgroundColor = ColorStateList.valueOf(it.color)
                binding.labels.addView(chip)
            }

            binding.root.setOnClickListener(this)
            binding.executePendingBindings()
        }

        override fun onCreateContextMenu(menu: ContextMenu?, view: View, menuInfo: ContextMenu.ContextMenuInfo?) {
            val card = binding.card!!

            menu?.apply {
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
    }
}
