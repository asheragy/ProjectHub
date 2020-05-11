package org.cerion.projecthub.ui

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import org.cerion.projecthub.R
import org.cerion.projecthub.databinding.FragmentProjectListBinding
import org.cerion.projecthub.model.Project
import org.koin.androidx.viewmodel.ext.android.viewModel


class ProjectListFragment : Fragment() {

    private val viewModel: ProjectListViewModel by viewModel()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = FragmentProjectListBinding.inflate(inflater, container, false)

        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        val adapter = ProjectListAdapter(object : ProjectListener {
            override fun onDelete(project: Project) {
                viewModel.deleteProject(project)
            }

            override fun onClick(project: Project) {
                val action = ProjectListFragmentDirections.actionProjectListFragmentToProjectHomeFragment(project.id)
                findNavController().navigate(action)
            }
        })

        binding.recyclerView.adapter = adapter
        binding.recyclerView.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))

        viewModel.projects.observe(viewLifecycleOwner, Observer {
            adapter.setItems(it)
        })

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
