package org.cerion.projecthub.model


sealed class Card {
    @Deprecated("unused") val id: Int = 0
    // ID for this node in context of project
    abstract val itemId: String
    // ID for underlying object which lives outside project
    abstract val contentId: String
}

data class DraftIssueCard(override val itemId: String, override val contentId: String, val title: String) : Card()

data class IssueCard(override val itemId: String, override val contentId: String) : Card() {
    var number = 0
    var author = ""
    var repository = ""
    var title: String = ""
    var body: String = ""
    var closed = false

    val labels = mutableListOf<Label>()
}