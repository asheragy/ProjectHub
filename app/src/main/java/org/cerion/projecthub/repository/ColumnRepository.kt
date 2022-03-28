package org.cerion.projecthub.repository

import GetColumnsForProjectQuery
import android.util.Log
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.coroutines.await
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.cerion.projecthub.TAG
import org.cerion.projecthub.USE_MOCK_DATA
import org.cerion.projecthub.model.Column


class ColumnRepository(private val apolloClient: ApolloClient) {

    suspend fun getColumnsForProject(projectId: String): List<Column> = withContext(Dispatchers.IO) {
        Log.d(TAG, "getColumnsForProject($projectId)")

        if (USE_MOCK_DATA && projectId == "MDc6UHJvamVjdDQ1ODIxODM=") {
            mockColumns
        }
        else {
            val query = GetColumnsForProjectQuery.builder().id(projectId).build()
            val response = apolloClient.query(query).await()

            val nodes =
                response.data?.node()?.fragments()?.projectFragment()?.columns()?.nodes()!!

            nodes.map {
                val col = it.fragments().columnFragment()
                Column(col.databaseId()!!, col.id(), col.name())
            }
        }
    }
}

val mockColumns = listOf(
    Column(9319546, "MDEzOlByb2plY3RDb2x1bW45MzE5NTQ2", "New"),
    Column(9319547, "MDEzOlByb2plY3RDb2x1bW45MzE5NTQ3", "Done"))