package org.cerion.projecthub.repository

import org.cerion.projecthub.github.CreateIssueCardParams
import org.cerion.projecthub.github.CreateIssueParams
import org.cerion.projecthub.github.GitHubService
import org.cerion.projecthub.github.UpdateIssueParams
import org.cerion.projecthub.model.Issue
import org.cerion.projecthub.model.IssueState

class IssueRepository(private val service: GitHubService) {

    suspend fun getByNumber(owner: String, repo: String, number: Int): Issue {
        val issue = service.getIssue(owner, repo, number).await()
        val state = if(issue.state == "open") IssueState.Open else IssueState.Closed
        val labels = issue.labels.map { it.toLabel() }

        return Issue(owner, repo, number)
            .init(issue.title, issue.body, state, labels)
    }

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

    suspend fun update(issue: Issue) {
        issue.run {
            if (labelsModified) {
                service.updateIssueLabels(owner, repo, number, labels.map { it.name }).await()
            }

            if (fieldsModified) {
                val params = UpdateIssueParams(issue.title, issue.body)
                service.updateIssue(issue.owner, issue.repo, issue.number, params).await()
            }
        }
    }
}