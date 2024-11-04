package org.cerion.projecthub.model



data class Column(
    val index: Int,
    val fieldId: String,
    val optionId: String,
    val name: String,
    // TODO change to enum before using
    val color: String
    )

data class Label(val id: String, val name: String, val color: Int) {
    var description: String = ""
    var included = false
}


