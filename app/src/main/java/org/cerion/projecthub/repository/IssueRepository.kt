package org.cerion.projecthub.repository

import org.cerion.projecthub.github.CreateIssueCardParams
import org.cerion.projecthub.github.CreateIssueParams
import org.cerion.projecthub.github.GitHubService
import org.cerion.projecthub.model.Issue

class IssueRepository(private val service: GitHubService) {

    suspend fun add(issue: Issue, columnId: Int): Int {
        issue.run {
            val params = CreateIssueParams(title, body)
            val result = service.createIssue(owner, repo, params).await()
            if (labels.size > 0)
                service.updateIssueLabels(owner, repo, result.number, labels.map { it.name }).await()

            service.createCard(columnId, CreateIssueCardParams(result.id)).await()

            return result.id
        }
    }
}