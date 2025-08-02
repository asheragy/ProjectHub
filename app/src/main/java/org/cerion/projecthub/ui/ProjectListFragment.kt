package org.cerion.projecthub.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import org.cerion.projecthub.R
import org.cerion.projecthub.logout
import org.koin.androidx.viewmodel.ext.android.viewModel


class ProjectListFragment : Fragment() {

    private val viewModel: ProjectListViewModel by viewModel()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)

        return ComposeView(requireContext()).apply {
            setContent {
                AppTheme {
                    val projects by viewModel.projects.observeAsState(initial = emptyList())
                    ProjectList(
                        projects,
                        onClick = {
                            val action = ProjectListFragmentDirections.actionProjectListFragmentToProjectHomeFragment(it.id)
                            findNavController().navigate(action)
                        },
                        onDelete = {
                            viewModel.deleteProject(it)
                        }
                    )
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.project_list, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.action_add -> onBrowseProjects()
            R.id.action_logout -> logout()
        }

        return super.onOptionsItemSelected(item)
    }

    private fun onBrowseProjects() {
        val action = ProjectListFragmentDirections.actionProjectListFragmentToProjectBrowserFragment()
        findNavController().navigate(action)
    }
}
