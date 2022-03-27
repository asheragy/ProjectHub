package org.cerion.projecthub.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import org.cerion.projecthub.databinding.FragmentProjectBrowserBinding
import org.cerion.projecthub.model.Project
import org.koin.androidx.viewmodel.ext.android.viewModel

class ProjectBrowserFragment : Fragment() {

    private val viewModel: ProjectBrowserViewModel by viewModel()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val binding = FragmentProjectBrowserBinding.inflate(inflater, container, false)

        val adapter = ProjectListAdapter(object : ProjectListener {
            override fun onDelete(project: Project) {}
            override fun onClick(project: Project) {
                viewModel.addProject(project)
            }
        }, true)

        binding.recyclerView.adapter = adapter
        binding.recyclerView.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))

        viewModel.projects.observe(viewLifecycleOwner) { items ->
            if (items != null)
                adapter.setItems(items)
            else
                adapter.setItems(emptyList())
        }

        return binding.root
    }

}