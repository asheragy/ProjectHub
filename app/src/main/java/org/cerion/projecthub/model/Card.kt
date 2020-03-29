package org.cerion.projecthub.model

abstract class Card

data class NoteCard(val id: Int, val nodeId: String) : Card() {
    var note = ""
    var creator = ""
}

data class IssueCard(val id: Int, val nodeId: String) : Card() {
    var number = 0
    var author = ""
    var repository = ""
    var title: String = ""
    var body: String = ""
    var closed = false

    val labels = mutableListOf<Label>()
}