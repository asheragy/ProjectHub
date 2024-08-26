package org.cerion.projecthub.ui.project

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.cerion.projecthub.model.Label
import org.cerion.projecthub.model.Project
import org.cerion.projecthub.repository.CardRepository
import org.cerion.projecthub.repository.ColumnRepository
import org.cerion.projecthub.repository.LabelRepository
import org.cerion.projecthub.repository.ProjectRepository

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
            val items = cardRepo.getCardsForProject(_project.value!!.nodeId)
            val cols = columnRepo.getColumnsForProject(_project.value!!.nodeId)
            _columns.value = cols.map {
                val cards = items[it.node_id]
                ColumnViewModel(vm, cardRepo, it).apply {
                    setCards(cards ?: arrayListOf())
                }
            }

            // TODO see if this can be lazy loaded somehow, but need to handle various cases of that
            // _labels.value = labelsRepo.getAll(_project.value!!.owner, _project.value!!.repo)
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
                cardRepo.changeCardColumn(_project.value!!.nodeId, card, column.column.fieldId, column.column.node_id)
            }
            catch(e: Exception) {
                e.printStackTrace()
            }
            finally {
                refresh()
            }
        }
    }
}
