package org.cerion.projecthub.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.cerion.projecthub.model.Project
import org.cerion.projecthub.repository.ProjectRepository

class ProjectBrowserViewModel(val repo: ProjectRepository) : ViewModel() {

    val ownerRepositoryProjects = repo.ownerRepositoryProjects.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun addProject(project: Project) {
        viewModelScope.launch {
            if (!project.saved) {
                repo.add(project)
            }
        }
    }

    fun deleteProject(project: Project) {
        viewModelScope.launch {
            repo.delete(project)
        }
    }
}