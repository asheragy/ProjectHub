package org.cerion.projecthub.model

enum class ProjectType {
    User,
    Repository,
    Org
}

// TODO nodeId should be id or projectId, see if integer id is used anymore
data class Project(val id: Int, val nodeId: String, val type: ProjectType, val owner: String, val repo: String) {
    var name: String = ""
    var description: String = ""
    var saved = false

    /* FEATURE to support user/org projects repo is not required
    init {
        if (type == ProjectType.Repository && repo.isNullOrEmpty())
            throw IllegalArgumentException("repository name must be specified")
    }
     */
}