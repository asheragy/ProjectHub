package org.cerion.projecthub.repository

import android.graphics.Color
import org.cerion.projecthub.USE_MOCK_DATA
import org.cerion.projecthub.github.GitHubLabel
import org.cerion.projecthub.github.GitHubService
import org.cerion.projecthub.model.Label

class LabelRepository(private val service: GitHubService) {

    suspend fun getAll(owner: String, repo: String): List<Label> {

        val labels =
            if (USE_MOCK_DATA) {
                listOf(
                    GitHubLabel(id=1644206713, name="bug", description="Something isn't working", color="d73a4a"),
                    GitHubLabel(id=1644206715, name="duplicate", description="This issue or pull request already exists", color="cfd3d7"),
                    GitHubLabel(id=1644206716, name="enhancement", description="New feature or request", color="a2eeef"))
            }
            else service.getLabelsAsync(owner, repo).await()

        return labels.map {
            it.toLabel()
        }
    }

}

fun GitHubLabel.toLabel(): Label = Label(name, Color.parseColor("#$color")).also {
    it.description = description ?: ""
}