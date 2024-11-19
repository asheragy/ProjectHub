package org.cerion.projecthub.model

enum class ProjectType {
    User,
    Org
}

data class Project(val id: String, val type: ProjectType, val owner: String, val repo: String) {
    var name: String = ""
    var description: String = ""
    var saved = false
}