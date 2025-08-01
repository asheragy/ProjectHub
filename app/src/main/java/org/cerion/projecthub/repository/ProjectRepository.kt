package org.cerion.projecthub.repository

import GetCurrentUserProjectsQuery
import GetProjectLabelsQuery
import android.graphics.Color
import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.map
import com.apollographql.apollo3.ApolloClient
import org.cerion.projecthub.database.DbProject
import org.cerion.projecthub.database.ProjectDao
import org.cerion.projecthub.model.Label
import org.cerion.projecthub.model.Project
import org.cerion.projecthub.model.ProjectType



class ProjectRepository(private val dao: ProjectDao, private val apolloClient: ApolloClient) {

    val projects = dao.getAllAsync().map { projects ->
        projects.map { it.toProject() }
    }

    val ownerRepositoryProjects: LiveData<List<Project>> = liveData {
        val db = dao.getAllAsync()
        val remoteProjects = getUserProjects()

        // Database values are the only ones that will change
        val merged: LiveData<List<Project>> = db.map { dbProjects ->
            remoteProjects.forEach {
                it.saved = dbProjects.any { dbProject -> it.id == dbProject.id }
            }

            remoteProjects
        }

        emitSource(merged)
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
            Project(project!!.id, ProjectType.User, viewer.login, "").apply {
                name = project.title
            }
        }
    }

    suspend fun add(project: Project) {
        dao.insert(project.toDbProject())
    }

    suspend fun delete(project: Project) {
        dao.delete(project.toDbProject())
    }
}

fun DbProject.toProject(): Project = Project(id, ProjectType.values()[type], owner, repo).also {
    it.name = name
    it.description = this.description
    it.saved = true
}

fun Project.toDbProject(): DbProject = DbProject(id, type.ordinal, owner, repo, name, description)