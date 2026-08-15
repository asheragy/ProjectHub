package org.cerion.projecthub.model

enum class ProjectType {
    User,
    Org
}

data class Project(
    val id: String,
    val type: ProjectType,
    val owner: String,
    val name: String)