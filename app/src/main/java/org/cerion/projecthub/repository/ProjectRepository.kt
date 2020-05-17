package org.cerion.projecthub.repository

import GetRepositoryProjectsQuery
import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.map
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.coroutines.toDeferred
import org.cerion.projecthub.database.DbProject
import org.cerion.projecthub.database.ProjectDao
import org.cerion.projecthub.model.Project
import org.cerion.projecthub.model.ProjectType


class ProjectRepository(private val dao: ProjectDao, private val apolloClient: ApolloClient) {

    // FEATURE store project as combo of type / owner / repo?
    // Org/user projects can have multiple repos
    //GET /repos/:owner/:repo/projects
    //GET /orgs/:org/projects
    //GET /users/:username/projects

    val projects = dao.getAllAsync().map { projects ->
        // Temp for testing extra project
        val mpa = Project(1481924, "MDc6UHJvamVjdDE0ODE5MjQ=", ProjectType.Repository,"PhilJay" , "MPAndroidChart").apply {
            name = "Support"
            description = ":fire: Automated issue tracking :fire:\\r\\n\\r\\n*Never-ending*"
        }

        projects.map { it.toProject() }.plus(mpa)
    }

    val ownerRepositoryProjects: LiveData<List<Project>> = liveData {
        val db = dao.getAllAsync()
        val remoteProjects = getRepositoryProjects()

        // Database values are the only ones that will change
        val merged: LiveData<List<Project>> = db.map { dbProjects ->
            remoteProjects.forEach {
                it.saved = dbProjects.any { dbProject -> it.id == dbProject.id }
            }

            remoteProjects
        }

        emitSource(merged)
    }

    // TODO remove and add single function to get by id
    private fun getAll(): List<Project> = dao.getAll().map { it.toProject() }

    fun getById(id: Int): Project? = getAll().firstOrNull { it.id == id }

    private suspend fun getRepositoryProjects(): List<Project> {
        val query = GetRepositoryProjectsQuery.builder().build()
        val response = apolloClient.query(query).toDeferred().await()
        val viewer=  response.data()?.viewer()

        return viewer?.repositories()?.nodes()!!.flatMap { repo ->
            repo.projects().nodes()!!.map { project ->
                Project(project.databaseId()!!, project.id(), ProjectType.Repository, viewer.login(), repo.name()).apply {
                    name = project.name()
                }
            }
        }
    }

    suspend fun add(project: Project) {
        dao.insert(project.toDbProject())
    }

    suspend fun delete(project: Project) {
        dao.delete(project.toDbProject())
    }

    //GET /repos/:owner/:repo/projects
    //GET /orgs/:org/projects
    //GET /users/:username/projects
}

fun DbProject.toProject(): Project = Project(id, nodeId, ProjectType.values()[type], owner, repo).also {
    it.name = name
    it.description = this.description
    it.saved = true
}

fun Project.toDbProject(): DbProject = DbProject(id, nodeId, type.ordinal, owner, repo, name, description)