package org.cerion.projecthub.repository

import android.graphics.Color
import org.cerion.projecthub.github.CreateIssueCardParams
import org.cerion.projecthub.github.CreateIssueParams
import org.cerion.projecthub.github.GitHubService
import org.cerion.projecthub.github.UpdateIssueParams
import org.cerion.projecthub.model.Issue
import org.cerion.projecthub.model.IssueState
import org.cerion.projecthub.model.Label

class IssueRepository(private val service: GitHubService) {

    suspend fun getByNumber(owner: String, repo: String, number: Int): Issue {
        val issue = service.getIssue(owner, repo, number).await()

        return Issue(owner, repo, number).apply {
            state = if(issue.state == "open") IssueState.Open else IssueState.Closed
            body = issue.body
            title = issue.title
            labels.addAll(issue.labels.map {
                Label(it.name, Color.parseColor("#" + it.color))
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