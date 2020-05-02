package org.cerion.projecthub.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.cerion.projecthub.model.Project
import org.cerion.projecthub.repository.ProjectRepository

class ProjectBrowserViewModel(val repo: ProjectRepository) : ViewModel() {

    private val _projects = MutableLiveData<List<Project>>()
    val projects: LiveData<List<Project>>
        get() = _projects

    init {
        viewModelScope.launch {
            loadProjects()
        }
    }

    private suspend fun loadProjects() {
        val dbProjects = repo.getAll()
        val ownerProjects = repo.getRepositoryProjectsByOwner("asheragy")

        // should repo do this?
        ownerProjects.forEach {
            if (dbProjects.any { dbProject -> it.id == dbProject.id })
                it.saved = true
        }

        _projects.value = ownerProjects
    }

    fun addProject(project: Project) {
        viewModelScope.launch {
            if (!project.saved) {
                repo.add(project)
                // TODO use mediatorlivedata to combine 2 sources, db version updates 1+ times and web only once.
                loadProjects()
            }
        }
    }
}