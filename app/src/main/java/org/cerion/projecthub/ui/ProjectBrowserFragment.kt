package org.cerion.projecthub.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import org.koin.androidx.viewmodel.ext.android.viewModel

class ProjectBrowserFragment : Fragment() {

    private val viewModel: ProjectBrowserViewModel by viewModel()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return ComposeView(requireContext()).apply {
            setContent {
                AppTheme {
                    val projects by viewModel.ownerRepositoryProjects.collectAsState()

                    ProjectList(
                        projects = projects,
                        onClick = {
                            if (it.saved)
                                viewModel.deleteProject(it)
                            else
                                viewModel.addProject(it)
                        },
                        onDelete = {},
                        browser = true
                    )
                }
            }
        }
    }

}