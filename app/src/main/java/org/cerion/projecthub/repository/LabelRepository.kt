package org.cerion.projecthub.repository

import android.graphics.Color
import org.cerion.projecthub.github.GitHubLabel
import org.cerion.projecthub.github.GitHubService
import org.cerion.projecthub.model.Label

class LabelRepository(private val service: GitHubService) {

    suspend fun getAll(owner: String, repo: String): List<Label> {
        val labels = service.getLabelsAsync(owner, repo).await()

        return labels.map {
            it.toLabel()
        }
    }

}

fun GitHubLabel.toLabel(): Label = Label(name, Color.parseColor("#$color"))