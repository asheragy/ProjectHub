package org.cerion.projecthub.model

sealed class Card {
    abstract val id: Int
}

data class NoteCard(override val id: Int, val nodeId: String) : Card() {
    var note = ""
    var creator = ""
}

data class IssueCard(override val id: Int, val nodeId: String) : Card() {
    var number = 0
    var author = ""
    var repository = ""
    var title: String = ""
    var body: String = ""
    var closed = false

    val labels = mutableListOf<Label>()
}