package org.cerion.projecthub.repository

import GetColumnsForProjectQuery
import android.util.Log
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.coroutines.toDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.cerion.projecthub.TAG
import org.cerion.projecthub.github.GitHubService
import org.cerion.projecthub.model.Column


class ColumnRepository(private val service: GitHubService, private val apolloClient: ApolloClient) {

    suspend fun getColumnsForProject(projectId: String): List<Column> = withContext(Dispatchers.IO) {
        Log.d(TAG, "getColumnsForProject($projectId)")

        val query = GetColumnsForProjectQuery.builder().id(projectId).build()
        val response = apolloClient.query(query).toDeferred().await()

        val nodes = response.data()?.node()?.fragments()?.projectFragment()?.columns()?.nodes()!!

        nodes.map {
            val col = it.fragments().columnFragment()
            Column(col.databaseId()!!, col.id(), col.name())
        }
    }
}