package org.cerion.projecthub.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.cerion.projecthub.model.Project
import org.cerion.projecthub.repository.ProjectRepository

class ProjectListViewModel(private val repo: ProjectRepository) : ViewModel() {

    val projects = repo.projects

    fun deleteProject(project: Project) {
        viewModelScope.launch {
            repo.delete(project)
        }
    }
}