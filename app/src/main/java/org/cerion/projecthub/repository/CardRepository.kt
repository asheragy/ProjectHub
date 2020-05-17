package org.cerion.projecthub.repository

import GetCardsForColumnQuery
import android.graphics.Color
import android.util.Log
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.coroutines.toDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.withContext
import org.cerion.projecthub.TAG
import org.cerion.projecthub.github.*
import org.cerion.projecthub.model.*


class CardRepository(private val service: GitHubService, private val apolloClient: ApolloClient) {

    private val lock = Mutex()
    private val map = mutableMapOf<String, List<GitHubIssue>>()

    suspend fun getCardsForColumn(columnId: String): List<Card> = withContext(Dispatchers.IO) {
        Log.d(TAG, "getCardsForColumn($columnId)")

        val query = GetCardsForColumnQuery.builder().id(columnId).build()
        val response = apolloClient.query(query).toDeferred().await()
        val nodes = response.data()?.node()?.fragments()?.columnDetailFragment()?.cards()?.nodes()

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

    suspend fun setIssueState(issue: Issue, closed: Boolean) {
        val state = UpdateIssueState(if(closed) "closed" else "open")
        service.updateIssueState(issue.owner, issue.repo, issue.number, state).await()
    }

    /*
    suspend fun getCardsForColumn(id: Int): List<Card> = withContext(Dispatchers.IO) {
        val cards = service.getCardsForColumn(id).await()

        val issues = mutableListOf<GitHubIssue>()
        // TODO a project can be linked to multiple repos, may need to loop and find unique here
        val url = cards.firstOrNull { it.content_url != null }?.content_url

        if (url != null) {
            val parts = url.split("/")
            val repoIssues = getIssuesForRepo(parts[4], parts[5])
            issues.addAll(repoIssues)
        }

        cards.map { card ->
            if (!card.content_url.isNullOrEmpty()) {
                val issue = issues.firstOrNull { issue -> issue.url == card.content_url }
                if (issue == null)
                    IssueCard(card, GitHubIssue(123, "??", "open", ""))
                else
                    IssueCard(card, issue)
            }
            else {
                NoteCard(card.note ?: "")
            }

        }
    }

    private suspend fun getIssuesForRepo(user: String, repo: String): List<GitHubIssue> {
        val key = "repo:$user:$repo"

        lock.withLock {
            if (!map.containsKey(key)) {
                Log.e("TAG", "Getting issues $key")
                val issues = service.getIssuesForRepo(user, repo).await()
                map[key] = issues
                return issues
            }

            Log.e("TAG", "Skipping issues")
            return map[key]!!
        }
    }

     */
}