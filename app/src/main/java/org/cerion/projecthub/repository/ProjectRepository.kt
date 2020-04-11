package org.cerion.projecthub.repository


enum class ProjectType {
    User,
    Repository,
    Org
}

data class Project(val id: Int, val nodeId: String, val type: ProjectType, val owner: String, val repo: String) {
    var name: String = ""
    var description: String = ""

    /* TODO to support user/org projects repo is not required
    init {
        if (type == ProjectType.Repository && repo.isNullOrEmpty())
            throw IllegalArgumentException("repository name must be specified")
    }
     */
}

class ProjectRepository {

    // TODO store project as combo of type / owner / repo?
    // Org/user projects can have multiple repos
    //GET /repos/:owner/:repo/projects
    //GET /orgs/:org/projects
    //GET /users/:username/projects

    fun getAll(): List<Project> {
        val p1 = Project(1481924, "MDc6UHJvamVjdDE0ODE5MjQ=", ProjectType.Repository,"PhilJay" , "MPAndroidChart").apply {
            name = "Support"
            description = ":fire: Automated issue tracking :fire:\\r\\n\\r\\n*Never-ending*"
        }

        val p2 = Project(3436611, "MDc6UHJvamVjdDQwMDUxNjY=", ProjectType.Repository,"asheragy","TestForIssueTracking").apply {
            name = "Project 3"
        }

        return listOf(p1, p2)
    }

    fun getById(id: Int): Project? {
        return getAll().firstOrNull { it.id == id }
    }

    //GET /repos/:owner/:repo/projects
    //GET /orgs/:org/projects
    //GET /users/:username/projects
}