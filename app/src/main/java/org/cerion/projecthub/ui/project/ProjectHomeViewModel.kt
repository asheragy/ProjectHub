package org.cerion.projecthub.ui.project

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.cerion.projecthub.model.Label
import org.cerion.projecthub.model.Project
import org.cerion.projecthub.repository.*

class ProjectHomeViewModel(private val projectRepo: ProjectRepository, private val labelsRepo: LabelRepository, private val cardRepo: CardRepository, private val columnRepo: ColumnRepository) : ViewModel() {

    private val _project = MutableLiveData<Project?>()
    val project: LiveData<Project?>
        get() = _project

    private val _columns = MutableLiveData<List<ColumnViewModel>?>()
    val columns: LiveData<List<ColumnViewModel>?>
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
                ColumnViewModel(vm, cardRepo, it)
            }

            // TODO see if this can be lazy loaded somehow, but need to handle various cases of that
            _labels.value = labelsRepo.getAll(_project.value!!.owner, _project.value!!.repo)
        }
    }

    fun refresh() {
        val id = project.value!!.id
        _project.value = null
        load(id)
    }

    fun moveCard(newColumnPosition: Int, newRowPosition: Int) {
        val column = _columns.value!![newColumnPosition]
        val card = column.cards.value!![newRowPosition]

        viewModelScope.launch {
            try {
                val position = when(newRowPosition) {
                    0 -> CardPosition.TOP
                    column.cards.value!!.size - 1 -> CardPosition.BOTTOM
                    else -> CardPosition.AFTER
                }

                val relativeCardId = if (position == CardPosition.AFTER) column.cards.value!![newRowPosition - 1].id else 0
                cardRepo.move(card, column.id, position, relativeCardId)
            }
            catch(e: Exception) {
                e.printStackTrace()

                // TODO may fail because no internet, can undo operation by moving back
                //oldColumn.loadCards()
                //newColumn.loadCards()
            }
        }
    }

    fun findColumnById(id: Int) = _columns.value?.first { it.id == id}
}
