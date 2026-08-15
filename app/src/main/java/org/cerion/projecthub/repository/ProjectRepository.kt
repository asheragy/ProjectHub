package org.cerion.projecthub.repository

import android.graphics.Color
import com.apollographql.apollo.ApolloClient
import org.cerion.projecthub.graphql.GetCurrentUserProjectsQuery
import org.cerion.projecthub.graphql.GetProjectLabelsQuery
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

    // TODO fetch 1 not all or get the project from the previous screen
    suspend fun getById(id: String) = getUserProjects().find { it.id == id }

    suspend fun getUserProjects(): List<Project> {
        val query = GetCurrentUserProjectsQuery()
        val response = apolloClient.query(query).execute()
        val viewer = response.data?.viewer

        return viewer?.projectsV2?.nodes!!.map { project ->
            Project(project!!.id, ProjectType.User, viewer.login, "", name = project.title)
        }
    }
}
