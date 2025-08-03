package org.cerion.projecthub.repository

import GetCurrentUserProjectsQuery
import GetProjectLabelsQuery
import android.graphics.Color
import com.apollographql.apollo3.ApolloClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import org.cerion.projecthub.database.DbProject
import org.cerion.projecthub.database.ProjectDao
import org.cerion.projecthub.model.Label
import org.cerion.projecthub.model.Project
import org.cerion.projecthub.model.ProjectType


class ProjectRepository(private val dao: ProjectDao, private val apolloClient: ApolloClient) {

    val projects: Flow<List<Project>> = dao.getAllFlow().map { dbProjects ->
        dbProjects.map { it.toProject() }
    }

    val ownerRepositoryProjects: Flow<List<Project>> = combine(
        flow { emit(getUserProjects()) }, // emits once
        dao.getAllFlow()
    ) { remoteProjects, dbProjects ->
        remoteProjects.map { project ->
            project.copy(saved = dbProjects.any { it.id == project.id })
        }
    }

    suspend fun getProjectLabels(project: Project): Pair<String,List<Label>> {
        val query = GetProjectLabelsQuery(project.id)
        val result = apolloClient.query(query).execute()

        val repositories = result.data?.node?.fragments?.projectLabels?.repositories?.nodes!!
        if (repositories.size != 1)
            throw RuntimeException("Project must be linked to only 1 repository")

        val labels = repositories[0]!!.labels?.nodes!!.map { label ->
            val color = Color.parseColor("#${label!!.color}")
            Label(label.id, label.name, color).apply {
                description = label.description ?: ""
            }
        }

        return Pair(repositories[0]!!.id, labels)
    }

    fun getById(id: String): Project? = dao.getAll().map { it.toProject() }.firstOrNull { it.id == id }

    private suspend fun getUserProjects(): List<Project> {
        val query = GetCurrentUserProjectsQuery()
        val response = apolloClient.query(query).execute()
        val viewer = response.data?.viewer

        return viewer?.projectsV2?.nodes!!.map { project ->
            Project(project!!.id, ProjectType.User, viewer.login, "", name = project.title)
        }
    }

    suspend fun add(project: Project) {
        dao.insert(project.toDbProject())
    }

    suspend fun delete(project: Project) {
        dao.delete(project.toDbProject())
    }
}

fun DbProject.toProject(): Project = Project(id, ProjectType.values()[type], owner, repo, name = name, description = description, saved = true)
fun Project.toDbProject(): DbProject = DbProject(id, type.ordinal, owner, repo, name, description)