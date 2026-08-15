package org.cerion.projecthub.repository

import android.graphics.Color
import com.apollographql.apollo.ApolloClient
import org.cerion.projecthub.graphql.GetCurrentUserProjectsQuery
import org.cerion.projecthub.graphql.GetProjectQuery
import org.cerion.projecthub.graphql.GetProjectLabelsQuery
import org.cerion.projecthub.graphql.fragment.ProjectDetails
import org.cerion.projecthub.model.Label
import org.cerion.projecthub.model.Project
import org.cerion.projecthub.model.ProjectType


class ProjectRepository(private val apolloClient: ApolloClient) {

    suspend fun getProjectLabels(project: Project): Pair<String,List<Label>> {
        val query = GetProjectLabelsQuery(project.id)
        val result = apolloClient.query(query).execute()

        val repositories = result.data?.node?.projectLabels?.repositories?.nodes!!
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

    suspend fun getById(id: String): Project? {
        val query = GetProjectQuery(id)
        val response = apolloClient.query(query).execute()
        val project = response.data?.node?.projectDetails ?: return null

        return project.toProject()
    }

    suspend fun getUserProjects(): List<Project> {
        val query = GetCurrentUserProjectsQuery()
        val response = apolloClient.query(query).execute()
        val viewer = response.data?.viewer

        return viewer?.projectsV2?.nodes!!.map { project ->
            project!!.projectDetails.toProject()
        }
    }

    private fun ProjectDetails.toProject(): Project {
        val ownerLogin = owner.onUser?.login
            ?: owner.onOrganization?.login
            ?: error("Unsupported project owner type ${owner.__typename}")

        val projectType = if (owner.onOrganization != null) {
            ProjectType.Org
        } else {
            ProjectType.User
        }

        return Project(id, projectType, ownerLogin, name = title)
    }
}
