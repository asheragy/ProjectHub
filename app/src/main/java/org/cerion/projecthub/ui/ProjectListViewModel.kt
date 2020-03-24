package org.cerion.projecthub.ui

import androidx.lifecycle.ViewModel
import org.cerion.projecthub.repository.ProjectRepository

class ProjectListViewModel : ViewModel() {

    private val repo = ProjectRepository()

    val projects = repo.getAll()


}