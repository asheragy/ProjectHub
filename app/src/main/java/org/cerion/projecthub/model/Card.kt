package org.cerion.projecthub.model

import org.cerion.projecthub.github.GitHubCard
import org.cerion.projecthub.github.GitHubIssue

abstract class Card

data class NoteCard(val note: String) : Card()

data class IssueCard(private val card: GitHubCard, private val issue: GitHubIssue) : Card() {
    val title = issue.title
    val body = ""
    val closed = issue.state != "open"
}