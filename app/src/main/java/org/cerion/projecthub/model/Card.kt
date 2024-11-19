package org.cerion.projecthub.model


sealed class Card {
    abstract val id: String
    abstract val itemId: String // ID for this node in context of project
}

data class DraftIssueCard(
    override val itemId: String,
    override val id: String,
    val title: String,
    val body: String
) : Card() {

    companion object {
        fun create(title: String, body: String): DraftIssueCard {
            return DraftIssueCard("", "", title, body)
        }
    }
}

data class IssueCard(
    override val itemId: String,
    override val id: String) : Card() {
        var number = 0
        var author = ""
        var repository = ""
        var title: String = ""
        var body: String = ""
        var closed = false

        val labels = mutableListOf<Label>()
}