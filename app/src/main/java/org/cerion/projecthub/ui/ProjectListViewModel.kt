package org.cerion.projecthub.ui

import androidx.lifecycle.ViewModel
import org.cerion.projecthub.repository.ProjectRepository

class ProjectListViewModel(private val repo: ProjectRepository) : ViewModel() {

    val projects = repo.getAll()


}