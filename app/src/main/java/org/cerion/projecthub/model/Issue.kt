package org.cerion.projecthub.model

enum class IssueState {
    Open,
    Closed
}

data class Issue(val owner: String, val repo: String, val number: Int) {
    var title = ""
    var body = ""
    var state = IssueState.Open
    val labels = mutableListOf<Label>()
}