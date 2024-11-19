package org.cerion.projecthub.model

import type.ProjectV2SingleSelectFieldOptionColor


data class Column(
    val index: Int,
    val fieldId: String,
    val optionId: String,
    val name: String,
    val color: ProjectV2SingleSelectFieldOptionColor
    )

data class Label(val id: String, val name: String, val color: Int) {
    var description: String = ""
    var included = false
}


