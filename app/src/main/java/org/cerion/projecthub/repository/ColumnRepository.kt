package org.cerion.projecthub.repository

import GetColumnsByStatusQuery
import android.util.Log
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.coroutines.await
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.cerion.projecthub.TAG
import org.cerion.projecthub.USE_MOCK_DATA
import org.cerion.projecthub.model.Column
import type.ProjectV2SingleSelectFieldOptionColor


class ColumnRepository(private val apolloClient: ApolloClient) {

    suspend fun getColumnsForProject(projectId: String): List<Column> = withContext(Dispatchers.IO) {
        Log.d(TAG, "getColumnsForProject($projectId)")

        if (USE_MOCK_DATA && projectId == "MDc6UHJvamVjdDQ1ODIxODM=") {
            mockColumns
        }
        else {
            val query = GetColumnsByStatusQuery.builder().id(projectId).build();
            val response = apolloClient.query(query).await()

            val project = response.data?.node()?.fragments()?.projectFragment()
            val field = project?.field()?.fragments()?.projectField()!!
            val options = field.options()

            options.mapIndexed { index, it ->
                Column(index, field.id(), it.id(), it.name(), it.color())
            }
        }
    }
}

val mockColumns = listOf(
    Column(0, "", "MDEzOlByb2plY3RDb2x1bW45MzE5NTQ2", "New", ProjectV2SingleSelectFieldOptionColor.GRAY),
    Column(1, "","MDEzOlByb2plY3RDb2x1bW45MzE5NTQ3", "Done", ProjectV2SingleSelectFieldOptionColor.RED))