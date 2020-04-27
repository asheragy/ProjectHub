package org.cerion.projecthub.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.cerion.projecthub.model.Project
import org.cerion.projecthub.model.ProjectType

class ProjectBrowserViewModel : ViewModel() {

    private val _projects = MutableLiveData<List<Project>>()
    val projects: LiveData<List<Project>>
        get() = _projects

    init {
        val p1 = Project(1, "", ProjectType.Repository, "", "").apply {
            name = "Fake name"
        }

        _projects.value = listOf(p1)
    }
}