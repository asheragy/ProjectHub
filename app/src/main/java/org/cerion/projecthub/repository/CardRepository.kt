package org.cerion.projecthub.repository

import AddDraftIssueMutation
import ArchiveItemMutation
import GetCardsForProjectQuery
import UpdateItemPositionMutation
import UpdateItemStatusMutation
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.coroutines.await
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.cerion.projecthub.github.GitHubService
import org.cerion.projecthub.github.UpdateIssueState
import org.cerion.projecthub.model.Card
import org.cerion.projecthub.model.Column
import org.cerion.projecthub.model.DraftIssueCard
import org.cerion.projecthub.model.Issue
import org.cerion.projecthub.model.Project

class CardRepository(private val service: GitHubService, private val apolloClient: ApolloClient) {

    suspend fun getCardsForProject(projectId: String): Map<String, List<Card>> = withContext(Dispatchers.IO) {
        val query = GetCardsForProjectQuery.builder().id(projectId).build()
        val response = apolloClient.query(query).await()

        val project = response.data?.node()?.fragments()?.projectFragment_Cards()
        val items = project?.items()?.nodes()!!

        val result = mutableMapOf<String, MutableList<Card>>()

        items.forEach { item ->
            // This happens for uncategorized which are not part of the project
            if (item.fieldValueByName() == null)
                return@forEach

            val statusOptionId = item.fieldValueByName()?.fragments()?.singleSelectValueFragment()?.optionId()!!

            val draft = item.content()?.fragments()?.draftIssueFragment()
            val card = if (draft != null) {
                DraftIssueCard(item.id(), draft.id(), draft.title(), draft.body()).apply {
                    // TODO add other fields
                }
            } else {
                throw RuntimeException("missing case")
            }

            val list = result.getOrElse(statusOptionId) { mutableListOf() }
            list.add(card)
            result[statusOptionId] = list

        }

        result
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

    suspend fun archiveCard(project: Project, card: Card) {
        val mutation = ArchiveItemMutation.builder()
            .projectId(project.nodeId)
            .itemId(card.itemId)

        apolloClient.mutate(mutation.build()).await()
    }

    suspend fun addDraftIssue(project: Project, column: Column, card: DraftIssueCard) {
        val addMutation = AddDraftIssueMutation.builder()
            .projectId(project.nodeId)
            .title(card.title)
            .body(card.body)

        val result = apolloClient.mutate(addMutation.build()).await()
        val cardId = result.data?.addProjectV2DraftIssue()?.projectItem()?.id()!!

        // Set column
        val columnMutation = UpdateItemStatusMutation.builder()
            .projectId(project.nodeId)
            .itemId(cardId)
            .statusFieldId(column.fieldId)
            .optionId(column.optionId)

        apolloClient.mutate(columnMutation.build()).await()
    }

    suspend fun updateDraftIssue(card: DraftIssueCard) {
        val mutation = UpdateDraftIssueMutation.builder()
            .id(card.contentId)
            .title(card.title)
            .body(card.body)

        val result = apolloClient.mutate(mutation.build()).await()
        println(result)
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