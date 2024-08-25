package org.cerion.projecthub.model



data class Column(
    @Deprecated("unused")
    val id: Int,
    // TODO rename to id
    val node_id: String,
    val name: String,
    // TODO change to enum before using
    val color: String
    )

data class Label(val name: String, val color: Int) {
    var description: String = ""
    var included = false
}


