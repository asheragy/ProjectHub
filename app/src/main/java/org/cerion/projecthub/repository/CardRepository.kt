package org.cerion.projecthub.repository

import AddDraftIssueMutation
import AddProjectItemMutation
import ArchiveItemMutation
import ConvertDraftToIssueMutation
import CreateIssueMutation
import DeleteItemMutation
import GetCardsForProjectQuery
import UpdateDraftIssueMutation
import UpdateIssueMutation
import UpdateIssueStateMutation
import UpdateItemPositionMutation
import UpdateItemStatusMutation
import android.graphics.Color
import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.api.Optional
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.cerion.projecthub.model.Card
import org.cerion.projecthub.model.Column
import org.cerion.projecthub.model.DraftIssueCard
import org.cerion.projecthub.model.IssueCard
import org.cerion.projecthub.model.Label
import org.cerion.projecthub.model.Project
import type.IssueState
import java.util.*

class CardRepository(private val apolloClient: ApolloClient) {

    suspend fun getCardsForProject(projectId: String): Map<String, List<Card>> = withContext(Dispatchers.IO) {
        val query = GetCardsForProjectQuery(projectId)
        val response = apolloClient.query(query).execute()

        val project = response.data?.node?.fragments?.projectFragment_Cards
        val items = project?.items?.nodes!!

        val result = mutableMapOf<String, MutableList<Card>>()

        items.forEach { item ->
            // This happens for uncategorized which are not part of the project
            if (item?.fieldValueByName == null)
                return@forEach

            val statusOptionId = item.fieldValueByName.fragments.singleSelectValueFragment?.optionId!!

            val draft = item.content?.fragments?.draftIssueFragment
            val issue = item.content?.fragments?.issueFragment

            val card = if (draft != null) {
                DraftIssueCard(item.id, draft.id, draft.title, draft.body)
            } else if (issue != null) {
                IssueCard(item.id, issue.id).apply {
                    author = issue.author?.login ?: ""
                    repository = issue.repository.name
                    title = issue.title
                    body = issue.body
                    closed = issue.closed
                    number = issue.number

                    labels.addAll(issue.labels?.nodes!!.map { node ->
                        val color = Color.parseColor("#${node!!.color}")
                        Label(node.id, node.name, color).apply {
                            description = node.description ?: ""
                        }
                    })
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

    suspend fun deleteCard(project: Project, card: Card) {
        if (card is DraftIssueCard) {
            val mutation = DeleteItemMutation(project.id, card.itemId)
            apolloClient.mutation(mutation).execute()
        }
        else {
            // TODO for issue
            throw NotImplementedError()
        }
    }

    suspend fun changeCardPosition(projectId: String, card: Card, afterCardId: String?) {
        val mutation = UpdateItemPositionMutation(
            projectId = projectId,
            itemId = card.itemId,
            afterItemId = Optional.presentIfNotNull(afterCardId)
        )

        apolloClient.mutation(mutation).execute()
    }

    suspend fun changeCardColumn(projectId: String, card: Card, column: Column) {
        val mutation = UpdateItemStatusMutation(
            projectId = projectId,
            itemId = card.itemId,
            statusFieldId = column.fieldId,
            optionId = column.optionId
        )

        apolloClient.mutation(mutation).execute()
    }

    suspend fun archiveCard(project: Project, card: Card) {
        val mutation = ArchiveItemMutation(
            projectId = project.id,
            itemId = card.itemId
        )

        apolloClient.mutation(mutation).execute()
    }

    suspend fun addDraftIssue(project: Project, column: Column, card: DraftIssueCard) {
        val addMutation = AddDraftIssueMutation(
            projectId = project.id,
            title = card.title,
            body = Optional.presentIfNotNull(card.body)
        )

        val result = apolloClient.mutation(addMutation).execute()
        val cardId = result.data?.addProjectV2DraftIssue?.projectItem?.id
            ?: error("Failed to get card ID from mutation result")

        // Set column
        val columnMutation = UpdateItemStatusMutation(
            projectId = project.id,
            itemId = cardId,
            statusFieldId = column.fieldId,
            optionId = column.optionId
        )

        apolloClient.mutation(columnMutation).execute()
    }


    suspend fun updateDraftIssue(card: DraftIssueCard) {
        val mutation = UpdateDraftIssueMutation(
            id = card.id,
            title = card.title,
            body = Optional.presentIfNotNull(card.body)
        )

        val result = apolloClient.mutation(mutation).execute()
        println(result)
    }

    suspend fun updateIssue(card: IssueCard) {
        val mutation = UpdateIssueMutation(
            id = card.id,
            title = card.title,
            body = Optional.presentIfNotNull(card.body),
            labelIds = Optional.Present(card.labels.map { it.id })
        )

        val result = apolloClient.mutation(mutation).execute()
        println(result)
    }


    suspend fun addIssue(project: Project, repositoryId: String, column: Column, issue: IssueCard) {
        val addMutation = CreateIssueMutation(
            repositoryId = repositoryId,
            title = issue.title,
            body = Optional.presentIfNotNull(issue.body),
            labelIds = Optional.Present(issue.labels.map { it.id })
        )

        val createResult = apolloClient.mutation(addMutation).execute()
        val id = createResult.data?.createIssue?.issue?.id
            ?: error("Failed to get issue ID from createIssue mutation result")


        // Add to project
        // TODO for some reason this was not working as part of the add mutation
        /*
mutation {
	createIssue(input: {
	repositoryId: "MDEwOlJlcG9zaXRvcnkzODU0ODg5NDM="
    projectIds: ["PVT_kwHOAMMyYM4Aml3M"]
		title: "test"
	}) {
		clientMutationId
		issue {
			id
		}
	}
}
         */
        val addToProjectMutation = AddProjectItemMutation(
            projectId = project.id,
            itemId = id
        )

        val result = apolloClient.mutation(addToProjectMutation).execute()
        val itemId = result.data?.addProjectV2ItemById?.item?.id
            ?: error("Failed to get item ID from AddProjectItemMutation")

        // Set column
        val columnMutation = UpdateItemStatusMutation(
            projectId = project.id,
            itemId = itemId,
            statusFieldId = column.fieldId,
            optionId = column.optionId
        )

        apolloClient.mutation(columnMutation).execute()
    }

    suspend fun convertToIssue(card: DraftIssueCard, repositoryId: String) {
        val mutation = ConvertDraftToIssueMutation(
            itemId = card.itemId,
            repositoryId = repositoryId
        )

        apolloClient.mutation(mutation).execute()
    }

    suspend fun updateIssueState(card: IssueCard, closed: Boolean) {
        val mutation = UpdateIssueStateMutation(
            id = card.id,
            state = if (closed) IssueState.CLOSED else IssueState.OPEN
        )

        apolloClient.mutation(mutation).execute()
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