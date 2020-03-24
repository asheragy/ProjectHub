package org.cerion.projecthub.repository


enum class ProjectType {
    User,
    Repository,
    Org
}

data class Project(val id: Int, val ownerUrl: String, val name: String, val description: String) {

    private val urlParts = ownerUrl.split("/")

    val type: ProjectType = when (urlParts[3]) {
            "users" -> ProjectType.User
            "repos" -> ProjectType.Repository
            "orgs" -> ProjectType.Org
            else -> throw NotImplementedError()
        }

    val user = when (type) {
            ProjectType.User -> urlParts[4]
            ProjectType.Repository -> urlParts[4]
            ProjectType.Org -> urlParts[5]
        }

    val repo = if (type == ProjectType.Repository) urlParts[5] else null
}

class ProjectRepository {

    fun getAll(): List<Project> {
        return listOf(
            Project(1481924, "https://api.github.com/repos/PhilJay/MPAndroidChart", "Support", ":fire: Automated issue tracking :fire:\\r\\n\\r\\n*Never-ending*"),
            Project(3436425, "https://api.github.com/users/asheragy","Projects", ""))
    }
}