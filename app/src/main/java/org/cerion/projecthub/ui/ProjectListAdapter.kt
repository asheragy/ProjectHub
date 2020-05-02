package org.cerion.projecthub.ui

import android.view.*
import androidx.recyclerview.widget.RecyclerView
import org.cerion.projecthub.databinding.ListItemProjectBinding
import org.cerion.projecthub.model.Project


interface ProjectListener {
    fun onDelete(project: Project)
    fun onClick(project: Project)
}

class ProjectListAdapter(private val listener: ProjectListener) : RecyclerView.Adapter<ProjectListAdapter.ViewHolder>() {

    private var items = emptyList<Project>()

    fun setItems(items: List<Project>) {
        this.items = items
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ListItemProjectBinding.inflate(layoutInflater, parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val task = items[position]
        holder.bind(task)
    }

    inner class ViewHolder(private val binding: ListItemProjectBinding) : RecyclerView.ViewHolder(binding.root), View.OnCreateContextMenuListener {
        fun bind(item: Project) {
            binding.project = item
            binding.type.text = item.type.toString()
            binding.root.setOnClickListener {
                listener.onClick(item)
            }
            binding.root.setOnCreateContextMenuListener(this)
            binding.executePendingBindings()
        }

        override fun onCreateContextMenu(menu: ContextMenu?, view: View, contextMenuInfo: ContextMenu.ContextMenuInfo?) {
            val project = binding.project!!

            menu?.apply {
                add(Menu.NONE, view.id, Menu.NONE, "Delete").setOnMenuItemClickListener {
                    listener.onDelete(project)
                    true
                }
            }
        }
    }
}