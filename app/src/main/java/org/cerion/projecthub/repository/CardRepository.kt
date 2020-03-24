package org.cerion.projecthub.repository

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.cerion.projecthub.github.GitHubIssue
import org.cerion.projecthub.github.GitHubService
import org.cerion.projecthub.model.Card
import org.cerion.projecthub.model.IssueCard
import org.cerion.projecthub.model.NoteCard


class CardRepository(private val service: GitHubService) {

    private val lock = Mutex()
    private val map = mutableMapOf<String, List<GitHubIssue>>()

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
}