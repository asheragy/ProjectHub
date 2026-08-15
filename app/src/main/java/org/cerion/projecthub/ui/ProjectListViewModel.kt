package org.cerion.projecthub.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.cerion.projecthub.model.Project
import org.cerion.projecthub.repository.ProjectRepository

class ProjectListViewModel(private val repo: ProjectRepository) : ViewModel() {

    private val _projects = MutableStateFlow<List<Project>>(emptyList())
    val projects = _projects.asStateFlow()

    init {
        viewModelScope.launch {
            _projects.value = repo.getUserProjects()
        }
    }
}
