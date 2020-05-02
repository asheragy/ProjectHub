package org.cerion.projecthub.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.cerion.projecthub.model.Project
import org.cerion.projecthub.repository.ProjectRepository

class ProjectBrowserViewModel(val repo: ProjectRepository) : ViewModel() {

    val projects = repo.ownerRepositoryProjects

    fun addProject(project: Project) {
        viewModelScope.launch {
            if (!project.saved) {
                repo.add(project)
            }
        }
    }
}