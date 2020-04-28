package org.cerion.projecthub.repository

import GetRepositoryProjectsByOwnerQuery
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.coroutines.toDeferred
import org.cerion.projecthub.model.Project
import org.cerion.projecthub.model.ProjectType


class ProjectRepository(private val apolloClient: ApolloClient) {

    // TODO store project as combo of type / owner / repo?
    // Org/user projects can have multiple repos
    //GET /repos/:owner/:repo/projects
    //GET /orgs/:org/projects
    //GET /users/:username/projects

    fun getAll(): List<Project> {
        val p1 = Project(1481924, "MDc6UHJvamVjdDE0ODE5MjQ=", ProjectType.Repository,"PhilJay" , "MPAndroidChart").apply {
            name = "Support"
            description = ":fire: Automated issue tracking :fire:\\r\\n\\r\\n*Never-ending*"
        }

        val p2 = Project(3436611, "MDc6UHJvamVjdDQwMDUxNjY=", ProjectType.Repository,"asheragy","TestForIssueTracking").apply {
            name = "Project 3"
        }

        return listOf(p1, p2)
    }

    fun getById(id: Int): Project? {
        return getAll().firstOrNull { it.id == id }
    }

    // TODO change this query to only get repo project for current user
    suspend fun getRepositoryProjectsByOwner(owner: String): List<Project> {
        val query = GetRepositoryProjectsByOwnerQuery.builder().owner(owner).build()
        val response = apolloClient.query(query).toDeferred().await()

        val result = mutableListOf<Project>()

        response.data()?.repositoryOwner()?.repositories()?.nodes()?.forEach { repo ->
            repo.projects().nodes()?.forEach { project ->
                result.add(Project(project.databaseId()!!, project.id(), ProjectType.Repository, owner, repo.name()))
            }
        }

        return result
    }

    //GET /repos/:owner/:repo/projects
    //GET /orgs/:org/projects
    //GET /users/:username/projects
}