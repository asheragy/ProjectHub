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

        return Issue(owner, repo, number).init {
            state = if(issue.state == "open") IssueState.Open else IssueState.Closed
            body = issue.body
            title = issue.title
            labels.addAll(issue.labels.map {
                it.toLabel()
            })
        }
    }

    suspend fun add(issue: Issue, columnId: Int): Int {
        val params = CreateIssueParams(issue.title, issue.body)
        val result = service.createIssue(issue.owner, issue.repo, params).await()

        service.createCard(columnId, CreateIssueCardParams(result.id)).await()
        return result.id
    }

    suspend fun update(issue: Issue) {
        val params = UpdateIssueParams(issue.title, issue.body)
        service.updateIssue(issue.owner, issue.repo, issue.number, params).await()
    }
}