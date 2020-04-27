package org.cerion.projecthub.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import org.cerion.projecthub.databinding.FragmentProjectBrowserBinding
import org.cerion.projecthub.databinding.ListItemProjectBinding
import org.cerion.projecthub.model.Project

class ProjectBrowserFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = FragmentProjectBrowserBinding.inflate(inflater, container, false)

        val adapter = ProjectBrowserListAdapter { project ->
            Toast.makeText(requireContext(), "Clicked project ${project.name}", Toast.LENGTH_SHORT).show()
        }

        binding.recyclerView.adapter = adapter
        binding.recyclerView.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))

        val viewModel = ProjectBrowserViewModel()
        viewModel.projects.observe(viewLifecycleOwner, Observer { items ->
            adapter.setItems(items)
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
            binding.project = item
            binding.type.text = item.type.toString()
            binding.root.setOnClickListener {
                onClick(item)
            }
            binding.executePendingBindings()
        }
    }
}