package org.cerion.projecthub.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import org.cerion.projecthub.R
import org.cerion.projecthub.logout
import org.koin.androidx.viewmodel.ext.android.viewModel


class ProjectListFragment : Fragment() {
    private val viewModel: ProjectListViewModel by viewModel()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return ComposeView(requireContext()).apply {
            setContent {
                AppTheme {
                    val projects by viewModel.projects.collectAsState()

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.project_list, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.action_add -> {
                        val action = ProjectListFragmentDirections.actionProjectListFragmentToProjectBrowserFragment()
                        findNavController().navigate(action)
                        true
                    }
                    R.id.action_logout -> {
                        logout()
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }
}
