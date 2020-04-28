package org.cerion.projecthub.ui

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import org.cerion.projecthub.R
import org.cerion.projecthub.databinding.FragmentProjectListBinding
import org.cerion.projecthub.databinding.ListItemProjectBinding
import org.cerion.projecthub.github.getGraphQLClient
import org.cerion.projecthub.model.Project
import org.cerion.projecthub.repository.ProjectRepository

class ProjectListFragment : Fragment() {

    private lateinit var viewModel: ProjectListViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = FragmentProjectListBinding.inflate(inflater, container, false)

        viewModel = ProjectListViewModel(ProjectRepository(getGraphQLClient(requireContext())))

        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        val adapter = ProjectListAdapter { project ->
            val action = ProjectListFragmentDirections.actionProjectListFragmentToProjectHomeFragment(project.id)
            findNavController().navigate(action)
        }

        binding.recyclerView.adapter = adapter
        binding.recyclerView.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        adapter.setItems(viewModel.projects)

        setHasOptionsMenu(true)

        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.project_list, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.action_add -> onBrowseProjects()
        }

        return super.onOptionsItemSelected(item)
    }

    private fun onBrowseProjects() {
        val action = ProjectListFragmentDirections.actionProjectListFragmentToProjectBrowserFragment()
        findNavController().navigate(action)
    }
}


class ProjectListAdapter(val onClick: (project: Project) -> Unit) : RecyclerView.Adapter<ProjectListAdapter.ViewHolder>() {

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