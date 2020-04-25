package org.cerion.projecthub.model



data class Column(val id: Int, val node_id: String, val name: String)

data class Label(val name: String, val color: Int)


enum class IssueState {
    Open,
    Closed
}

data class Issue(val owner: String, val repo: String, val number: Int) {
    var title = ""
    var body = ""
    var state = IssueState.Open
}