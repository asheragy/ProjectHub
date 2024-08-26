package org.cerion.projecthub.model


sealed class Card {
    @Deprecated("unused") val id: Int = 0
    abstract val databaseId: Int   // Only for DragItemAdapter uniqueness
    abstract val itemId: String    // ID for this node in context of project
    abstract val contentId: String // ID for underlying object which lives outside project
}

data class DraftIssueCard(
    override val databaseId: Int,
    override val itemId: String,
    override val contentId: String,
    val title: String,
    val body: String
) : Card()

data class IssueCard(
    override val databaseId: Int,
    override val itemId: String,
    override val contentId: String) : Card() {
        var number = 0
        var author = ""
        var repository = ""
        var title: String = ""
        var body: String = ""
        var closed = false

        val labels = mutableListOf<Label>()
}