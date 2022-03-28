package org.cerion.projecthub.repository

import GetCardsForColumnQuery
import android.graphics.Color
import android.util.Log
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.coroutines.await
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.cerion.projecthub.TAG
import org.cerion.projecthub.USE_MOCK_DATA
import org.cerion.projecthub.github.*
import org.cerion.projecthub.model.*

enum class CardPosition {
    TOP,
    BOTTOM,
    AFTER
}

class CardRepository(private val service: GitHubService, private val apolloClient: ApolloClient) {

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

    /**
     * @param card              Card to move
     * @param columnId          New or existing column to move card to
     * @param position          New position
     * @param relativeCardId    Card id that this card goes AFTER
     */
    suspend fun move(card: Card, columnId: Int, position: CardPosition, relativeCardId: Int = 0) {
        val pos =
            when (position) {
                CardPosition.TOP -> "top"
                CardPosition.BOTTOM -> "bottom"
                CardPosition.AFTER -> "after:$relativeCardId"
            }

        val params = MoveCardParams(columnId, pos)
        service.moveCard(card.id, params).await()
    }

    suspend fun archiveCard(id: Int, archived: Boolean) {
        service.archiveCard(id, ArchiveCardParams(archived)).await()
    }

    suspend fun setIssueState(issue: Issue, closed: Boolean) {
        val state = UpdateIssueState(if(closed) "closed" else "open")
        service.updateIssueState(issue.owner, issue.repo, issue.number, state).await()
    }
}

val mockColumn1 = listOf(
    NoteCard(38783849, "MDExOlByb2plY3RDYXJkMzg3ODM4NDk=").apply {
        note = "This is a note"
        creator = "asheragy"
    },
    IssueCard(619602904, "MDU6SXNzdWU2MTk2MDI5MDQ=").apply {
        title = "test issue with labels"
        closed = false
        number = 24
        repository = "TestForIssueTracking"
        author = "asheragy"
        labels.add(Label("duplicate", Color.parseColor("#cfd3d7")))
        labels.add(Label("enhancement", Color.parseColor("#a2eeef")))
    },
    IssueCard(619604999, "MDU6SXNzdWU2MTk2MDQ5OTk=").apply {
        title = "test issue with labels"
        closed = false
        number = 25
        repository = "TestForIssueTracking"
        author = "asheragy"
    }
)

val mockColumn2 = listOf(
    IssueCard(513674595, "MDU6SXNzdWU1MTM2NzQ1OTU=").apply {
        title = "test issue"
        closed = true
        number = 1
        repository = "TestForIssueTracking"
        author = "asheragy"
        labels.add(Label("bug", Color.parseColor("#d73a4a")))
    }
)