package org.cerion.projecthub.ui.project

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.cerion.projecthub.github.*
import org.cerion.projecthub.model.Card
import org.cerion.projecthub.model.Label
import org.cerion.projecthub.model.Project
import org.cerion.projecthub.repository.CardRepository
import org.cerion.projecthub.repository.ColumnRepository
import org.cerion.projecthub.repository.LabelRepository
import org.cerion.projecthub.repository.ProjectRepository

// TODO pass in all objects that use context
class ProjectHomeViewModel(context: Context, private val projectRepo: ProjectRepository, private val labelsRepo: LabelRepository) : ViewModel() {

    private val _project = MutableLiveData<Project>()
    val project: LiveData<Project>
        get() = _project

    private var service: GitHubService = getService(context)
    private val graphQL = getGraphQLClient(context)
    private val columnRepo = ColumnRepository(service, graphQL)
    private val cardRepo = CardRepository(service, graphQL)

    private val _columns = MutableLiveData<List<ColumnViewModel>>()
    val columns: LiveData<List<ColumnViewModel>>
        get() = _columns

    private val _labels = MutableLiveData<List<Label>>()
    val labels: LiveData<List<Label>>
        get() = _labels

    fun load(projectId: Int) {
        val existingId = project.value?.id
        // When loading new project clear everything first since delay in load
        if (existingId != null) {
            if (existingId != projectId) {
                _columns.value = null
                _project.value = null
            }
            else {
                return // Already have this project loaded
            }
        }

        val vm = this
        viewModelScope.launch {
            _project.value = projectRepo.getById(projectId)
            val cols = columnRepo.getColumnsForProject(_project.value!!.nodeId)
            _columns.value = cols.map {
                ColumnViewModel(vm, cardRepo, service, it)
            }

            // TODO see if this can be lazy loaded somehow, but need to handle various cases of that
            _labels.value = labelsRepo.getAll(_project.value!!.owner, _project.value!!.repo)
        }
    }

    fun moveCard(card: Card, columnId: Int) {
        val oldColumn = _columns.value!!.first { it.containsCard(card) }
        val newColumn = _columns.value!!.first { it.id == columnId}

        viewModelScope.launch {
            try {
                oldColumn.removeCard(card)
                service.moveCard(card.id, MoveCardParams(columnId)).await()
                newColumn.addCard(card)
            }
            catch(e: Exception) {
                e.printStackTrace()

                // TODO may fail because no internet, can undo operation by moving back
                oldColumn.loadCards()
                newColumn.loadCards()
            }
        }
    }

    fun addIssueForColumn(columnId: Int, title: String, body: String) {
        viewModelScope.launch {
            val params = CreateIssueParams(title, body)
            val project = _project.value!!
            val issue = service.createIssue(project.owner, project.repo, params).await()
            service.createCard(columnId, CreateIssueCardParams(issue.id)).await()

            findColumnById(columnId)?.loadCards()
        }
    }

    fun findColumnById(id: Int) = _columns.value?.first { it.id == id}

}
