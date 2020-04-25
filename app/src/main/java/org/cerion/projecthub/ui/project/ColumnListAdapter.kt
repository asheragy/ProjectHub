package org.cerion.projecthub.ui.project

import android.content.res.ColorStateList
import android.view.*
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
    fun move(card: Card)
    fun onArchive(card: Card)
    fun onClick(card: Card)
}

class ColumnCardListAdapter(private val listener: CardListener) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    companion object {
        private const val TypeNote = 0
        private const val TypeIssue = 1
    }

    private var items = emptyList<Card>()

    fun setItems(items: List<Card>) {
        this.items = items
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
    }

    abstract inner class BaseViewHolder(binding: ViewDataBinding) : RecyclerView.ViewHolder(binding.root), View.OnClickListener {
        override fun onClick(p0: View?) {
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
    }

    inner class IssueViewHolder(private val binding: ListItemCardIssueBinding) : BaseViewHolder(binding), View.OnCreateContextMenuListener {

        init {
            binding.root.setOnCreateContextMenuListener(this)
        }

        fun bind(item: IssueCard) {
            binding.card = item
            // TODO add repository if this is a multi repo project (associated to user/org)
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
                add(Menu.NONE, view.id, Menu.NONE, "Move").setOnMenuItemClickListener {
                    listener.move(card)
                    true
                }
                add(Menu.NONE, view.id, Menu.NONE, "Archive").setOnMenuItemClickListener {
                    listener.onArchive(card)
                    true
                }
            }
        }

        override fun onClick() {
            listener.onClick(binding.card!!)
        }
    }
}