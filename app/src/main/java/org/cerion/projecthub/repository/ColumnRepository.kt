package org.cerion.projecthub.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.cerion.projecthub.github.GitHubService
import org.cerion.projecthub.model.Column


class ColumnRepository(private val service: GitHubService) {

    fun getAll(): List<Column> {
        return listOf(Column(6954118, "To do"),
            Column(6954119, "In Progress"),
            Column(6954120, "Done"))
    }

    suspend fun getColumnsForProject(projectId: Int) = withContext(Dispatchers.IO) {
        val columns = service.getProjectColumns(projectId).await()

        columns.map {
            Column(it.id, it.name)
        }
    }
}