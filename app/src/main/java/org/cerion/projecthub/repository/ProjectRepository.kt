package org.cerion.projecthub.repository

import GetRepositoryProjectsByOwnerQuery
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.coroutines.toDeferred
import org.cerion.projecthub.database.DbProject
import org.cerion.projecthub.database.ProjectDao
import org.cerion.projecthub.model.Project
import org.cerion.projecthub.model.ProjectType


class ProjectRepository(private val projects: ProjectDao, private val apolloClient: ApolloClient) {

    // TODO store project as combo of type / owner / repo?
    // Org/user projects can have multiple repos
    //GET /repos/:owner/:repo/projects
    //GET /orgs/:org/projects
    //GET /users/:username/projects

    fun getAll(): List<Project> {

        val mpa = Project(1481924, "MDc6UHJvamVjdDE0ODE5MjQ=", ProjectType.Repository,"PhilJay" , "MPAndroidChart").apply {
            name = "Support"
            description = ":fire: Automated issue tracking :fire:\\r\\n\\r\\n*Never-ending*"
        }

        val projects = projects.getAll().map { it.toProject() }

        return projects.plus(mpa)
    }

    fun getById(id: Int): Project? {
        return getAll().firstOrNull { it.id == id }
    }

    // TODO change this query to only get repo project for current user
    suspend fun getRepositoryProjectsByOwner(owner: String): List<Project> {
        val query = GetRepositoryProjectsByOwnerQuery.builder().owner(owner).build()
        val response = apolloClient.query(query).toDeferred().await()

        return response.data()?.repositoryOwner()?.repositories()?.nodes()!!.flatMap { repo ->
            repo.projects().nodes()!!.map { project ->
                Project(project.databaseId()!!, project.id(), ProjectType.Repository, owner, repo.name()).apply {
                    name = project.name()
                }
            }
        }
    }

    suspend fun add(project: Project) {
        projects.insert(project.toDbProject())
    }

    suspend fun delete(project: Project) {
        projects.delete(project.toDbProject())
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