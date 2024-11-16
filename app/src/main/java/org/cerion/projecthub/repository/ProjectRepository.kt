package org.cerion.projecthub.repository

import GetCurrentUserProjectsQuery
import GetProjectLabelsQuery
import android.graphics.Color
import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.map
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.coroutines.await
import org.cerion.projecthub.database.DbProject
import org.cerion.projecthub.database.ProjectDao
import org.cerion.projecthub.model.Label
import org.cerion.projecthub.model.Project
import org.cerion.projecthub.model.ProjectType


// Temp for testing larger project than my own
val mpAndroidChart = Project(1481924, "MDc6UHJvamVjdDE0ODE5MjQ=", ProjectType.Repository,"PhilJay" , "MPAndroidChart").apply {
    name = "Support"
    description = ":fire: Automated issue tracking :fire:\\r\\n\\r\\n*Never-ending*"
}

class ProjectRepository(private val dao: ProjectDao, private val apolloClient: ApolloClient) {

    // FEATURE store project as combo of type / owner / repo?
    // Org/user projects can have multiple repos
    //GET /repos/:owner/:repo/projects
    //GET /orgs/:org/projects
    //GET /users/:username/projects

    val projects = dao.getAllAsync().map { projects ->
        projects.map { it.toProject() }.plus(mpAndroidChart)
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
        val query = GetProjectLabelsQuery.builder().projectId(project.nodeId).build()
        val result = apolloClient.query(query).await()

        val repositories = result.data?.node()?.fragments()?.projectLabels()?.repositories()?.nodes()!!
        if (repositories.size != 1)
            throw RuntimeException("Project must be linked to only 1 repository")

        val labels = repositories[0].labels()?.nodes()!!.map { label ->
            val color = Color.parseColor("#${label.color()}")
            Label(label.id(), label.name(), color).apply {
                description = label.description() ?: ""
            }
        }

        return Pair(repositories[0].id(), labels)
    }

    // TODO remove and add single function to get by id
    private fun getAll(): List<Project> = dao.getAll().map { it.toProject() }.plus(mpAndroidChart)

    fun getById(id: Int): Project? = getAll().firstOrNull { it.id == id }

    private suspend fun getUserProjects(): List<Project> {
        val query = GetCurrentUserProjectsQuery.builder().build()
        val response = apolloClient.query(query).await()
        val viewer = response.data?.viewer()

        return viewer?.projectsV2()?.nodes()!!.map { project ->
            Project(project.databaseId()!!, project.id(), ProjectType.User, viewer.login(), "").apply {
                name = project.title()
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