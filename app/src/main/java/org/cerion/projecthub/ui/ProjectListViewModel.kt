package org.cerion.projecthub.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.cerion.projecthub.model.Project
import org.cerion.projecthub.repository.ProjectRepository

class ProjectListViewModel(private val repo: ProjectRepository) : ViewModel() {

    private val _projects = MutableLiveData<List<Project>>()
    val projects: LiveData<List<Project>>
        get() = _projects

    init {
        _projects.value = repo.getAll()
    }

    fun deleteProject(project: Project) {
        viewModelScope.launch {
            repo.delete(project)
            _projects.value = repo.getAll()
        }
    }
}