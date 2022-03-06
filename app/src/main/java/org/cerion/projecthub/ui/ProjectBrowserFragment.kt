package org.cerion.projecthub.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import org.cerion.projecthub.databinding.FragmentProjectBrowserBinding
import org.cerion.projecthub.databinding.ListItemProjectBinding
import org.cerion.projecthub.model.Project
import org.koin.androidx.viewmodel.ext.android.viewModel

class ProjectBrowserFragment : Fragment() {

    private val viewModel: ProjectBrowserViewModel by viewModel()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = FragmentProjectBrowserBinding.inflate(inflater, container, false)

        val adapter = ProjectBrowserListAdapter { project ->
            viewModel.addProject(project)
        }

        binding.recyclerView.adapter = adapter
        binding.recyclerView.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))

        viewModel.projects.observe(viewLifecycleOwner, Observer { items ->
            if (items != null)
                adapter.setItems(items)
            else
                adapter.setItems(emptyList())
        })

        return binding.root
    }

}


class ProjectBrowserListAdapter(val onClick: (project: Project) -> Unit) : RecyclerView.Adapter<ProjectBrowserListAdapter.ViewHolder>() {

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

    inner class ViewHolder(private val binding: ListItemProjectBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Project) {
            binding.repo.text = item.owner + '/' + item.repo
            binding.name.text = item.name
            binding.type.text = item.type.toString()
            binding.root.setOnClickListener {
                onClick(item)
            }
            binding.saved.visibility = if (item.saved) View.VISIBLE else View.GONE
        }
    }
}