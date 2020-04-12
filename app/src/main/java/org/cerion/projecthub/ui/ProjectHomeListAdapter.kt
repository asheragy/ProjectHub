package org.cerion.projecthub.ui

import android.content.res.ColorStateList
import android.content.res.Configuration
import android.view.*
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import org.cerion.projecthub.R
import org.cerion.projecthub.databinding.ListItemCardIssueBinding
import org.cerion.projecthub.databinding.ListItemCardNoteBinding
import org.cerion.projecthub.databinding.ListItemColumnBinding
import org.cerion.projecthub.model.Card
import org.cerion.projecthub.model.IssueCard
import org.cerion.projecthub.model.NoteCard


interface BoardListener {
    fun move(card: Card)
    fun onClick(card: Card)
}

class ProjectColumnListAdapter(private val lifecycleOwner: LifecycleOwner, private val listener: BoardListener) : RecyclerView.Adapter<ProjectColumnListAdapter.ViewHolder>() {

    private var items = emptyList<ColumnViewModel>()
    private var width = 0

    fun setItems(items: List<ColumnViewModel>) {
        this.items = items
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ListItemColumnBinding.inflate(layoutInflater, parent, false)

        if (width == 0) {
            val percent = if (parent.context.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) 0.7 else 0.3
            width = (parent.measuredWidth * percent).toInt()
        }

        binding.root.minimumWidth = width
        binding.recyclerView.adapter = ColumnCardListAdapter(listener)
        binding.recyclerView.addItemDecoration(DividerItemDecoration(parent.context, DividerItemDecoration.VERTICAL))

        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item)
    }

    inner class ViewHolder(private val binding: ListItemColumnBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ColumnViewModel) {
            binding.viewModel = item

            val adapter = binding.recyclerView.adapter as ColumnCardListAdapter

            item.cards.observe(lifecycleOwner, Observer {
                adapter.setItems(it)
            })

            binding.executePendingBindings()
        }
    }
}


class ColumnCardListAdapter(private val listener: BoardListener) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
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
            }
        }

        override fun onClick() {
            listener.onClick(binding.card!!)
        }
    }
}