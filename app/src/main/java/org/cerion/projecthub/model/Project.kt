package org.cerion.projecthub.model

enum class ProjectType {
    User,
    Org
}

data class Project(
    val id: String,
    val type: ProjectType,
    val owner: String,
    val repo: String,
    val name: String = "",
    val description: String = "",
    val saved: Boolean = false)