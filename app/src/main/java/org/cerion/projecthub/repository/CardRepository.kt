package org.cerion.projecthub.repository

import GetCardsForProjectQuery
import UpdateItemPositionMutation
import UpdateItemStatusMutation
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.coroutines.await
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.cerion.projecthub.github.ArchiveCardParams
import org.cerion.projecthub.github.CreateCardParams
import org.cerion.projecthub.github.GitHubService
import org.cerion.projecthub.github.UpdateCardParams
import org.cerion.projecthub.github.UpdateIssueState
import org.cerion.projecthub.model.Card
import org.cerion.projecthub.model.DraftIssueCard
import org.cerion.projecthub.model.Issue

class CardRepository(private val service: GitHubService, private val apolloClient: ApolloClient) {

    suspend fun getCardsForProject(projectId: String): Map<String, List<Card>> = withContext(Dispatchers.IO) {
        val query = GetCardsForProjectQuery.builder().id(projectId).build()
        val response = apolloClient.query(query).await()

        val project = response.data?.node()?.fragments()?.projectFragment_Cards()
        val items = project?.items()?.nodes()!!

        val result = mutableMapOf<String, MutableList<Card>>()

        items.forEach { item ->
            val statusOptionId = item.fieldValueByName()?.fragments()?.singleSelectValueFragment()?.optionId()!!

            val draft = item.content()?.fragments()?.draftIssueFragment()
            val card = if (draft != null) {
                DraftIssueCard(item.databaseId()!!, item.id(), draft.id(), draft.title(), draft.body()).apply {
                    // TODO add other fields
                }
            }
            else {
                throw RuntimeException("missing case")
            }

            val list = result.getOrElse(statusOptionId) { mutableListOf() }
            list.add(card)
            result[statusOptionId] = list
        }

        result
    }

    /*
    suspend fun getCardsForColumn(columnId: String): List<Card> = withContext(Dispatchers.IO) {
        Log.d(TAG, "getCardsForColumn($columnId)")

        if (USE_MOCK_DATA && columnId == "MDEzOlByb2plY3RDb2x1bW45MzE5NTQ2")
            mockColumn1
        else if (USE_MOCK_DATA && columnId == "MDEzOlByb2plY3RDb2x1bW45MzE5NTQ3")
            mockColumn2
        else {
            val query = GetCardsForColumnQuery.builder().id(columnId).build()
            val response = apolloClient.query(query).await()
            val nodes = response.data?.node()?.fragments()?.columnDetailFragment()?.cards()?.nodes()

            nodes!!.map {
                val id = it.databaseId()!!
                val node = it.id()

                val issue = it.content()?.fragments()?.issueFragment()
                // TODO add pull request type
                if (issue != null)
                    IssueCard(id, node).apply {
                        this.closed = issue.closed()
                        this.title = issue.title()
                        this.number = issue.number()
                        this.repository = issue.repository().name()
                        this.author = issue.author()?.login() ?: ""

                        issue.labels()?.nodes()?.forEach { label ->
                            this.labels.add(Label(label.name(), Color.parseColor("#" + label.color())))
                        }
                    }
                else
                    NoteCard(id, node).apply {
                        this.note = it.note() ?: ""
                        this.creator = it.creator()?.login() ?: ""
                    }
            }
        }
    }
     */

    suspend fun addNoteForColumn(columnId: Int, note: String) {
        val params = CreateCardParams(note)
        service.createCard(columnId, params).await()
    }

    suspend fun updateNote(id: Int, note: String) {
        val params = UpdateCardParams(note)
        service.updateCard(id, params).await()
    }

    suspend fun deleteCard(id: Int) {
        withContext(Dispatchers.IO) {
            service.deleteCard(id).execute() // For some reason the deferred/await was throwing exception
        }
    }

    suspend fun changeCardPosition(projectId: String, card: Card, afterCardId: String?) {
        val mutation = UpdateItemPositionMutation.builder()
            .projectId(projectId)
            .afterItemId(afterCardId)
            .itemId(card.itemId)

        apolloClient.mutate(mutation.build()).await()
    }

    suspend fun changeCardColumn(projectId: String, card: Card, fieldId: String, optionId: String) {
        val mutation = UpdateItemStatusMutation.builder()
            .projectId(projectId)
            .itemId(card.itemId)
            .statusFieldId(fieldId)
            .optionId(optionId)

        apolloClient.mutate(mutation.build()).await()
    }

    suspend fun archiveCard(id: Int, archived: Boolean) {
        service.archiveCard(id, ArchiveCardParams(archived)).await()
    }

    suspend fun setIssueState(issue: Issue, closed: Boolean) {
        val state = UpdateIssueState(if(closed) "closed" else "open")
        service.updateIssueState(issue.owner, issue.repo, issue.number, state).await()
    }
}

/*
val mockColumn1 = listOf(
    NoteCard("MDExOlByb2plY3RDYXJkMzg3ODM4NDk=").apply {
        note = "This is a note"
        creator = "asheragy"
    },
    IssueCard("MDU6SXNzdWU2MTk2MDI5MDQ=").apply {
        title = "test issue with labels"
        closed = false
        number = 24
        repository = "TestForIssueTracking"
        author = "asheragy"
        labels.add(Label("duplicate", Color.parseColor("#cfd3d7")))
        labels.add(Label("enhancement", Color.parseColor("#a2eeef")))
    },
    IssueCard("MDU6SXNzdWU2MTk2MDQ5OTk=").apply {
        title = "test issue with labels"
        closed = false
        number = 25
        repository = "TestForIssueTracking"
        author = "asheragy"
    }
)

val mockColumn2 = listOf(
    IssueCard("MDU6SXNzdWU1MTM2NzQ1OTU=").apply {
        title = "test issue"
        closed = true
        number = 1
        repository = "TestForIssueTracking"
        author = "asheragy"
        labels.add(Label("bug", Color.parseColor("#d73a4a")))
    }
)

 */