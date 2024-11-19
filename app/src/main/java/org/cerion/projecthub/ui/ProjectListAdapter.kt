package org.cerion.projecthub.ui

import android.annotation.SuppressLint
import android.view.*
import androidx.recyclerview.widget.RecyclerView
import org.cerion.projecthub.R
import org.cerion.projecthub.databinding.ListItemProjectBinding
import org.cerion.projecthub.model.Project
import org.cerion.projecthub.model.ProjectType


interface ProjectListener {
    fun onDelete(project: Project)
    fun onClick(project: Project)
}

class ProjectListAdapter(private val listener: ProjectListener, private val browser: Boolean = false) : RecyclerView.Adapter<ProjectListAdapter.ViewHolder>() {

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
        @SuppressLint("SetTextI18n")
        fun bind(item: Project) {
            binding.repo.text = item.owner + '/' + item.repo
            binding.name.text = item.name
            val icon = when (item.type) {
                //ProjectType.Repository -> R.drawable.type_repo
                ProjectType.User -> R.drawable.type_account
                ProjectType.Org -> R.drawable.type_org
            }
            binding.type.setImageResource(icon)
            binding.root.setOnClickListener {
                listener.onClick(item)
            }

            if (browser)
                binding.saved.visibility = if (item.saved) View.VISIBLE else View.GONE
            else
                binding.root.setOnCreateContextMenuListener(this)
        }

        override fun onCreateContextMenu(menu: ContextMenu?, view: View, contextMenuInfo: ContextMenu.ContextMenuInfo?) {
            menu?.apply {
                add(Menu.NONE, view.id, adapterPosition, "Delete").setOnMenuItemClickListener {
                    listener.onDelete(items[it.order])
                    true
                }
            }
        }
    }
}