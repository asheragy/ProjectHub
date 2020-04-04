package org.cerion.projecthub.ui

import android.app.Application
import androidx.lifecycle.*
import kotlinx.coroutines.launch
import org.cerion.projecthub.github.*
import org.cerion.projecthub.model.Card
import org.cerion.projecthub.model.Column
import org.cerion.projecthub.model.NoteCard
import org.cerion.projecthub.repository.CardRepository
import org.cerion.projecthub.repository.ColumnRepository
import org.cerion.projecthub.repository.ProjectRepository

class ProjectHomeViewModel(application: Application) : AndroidViewModel(application) {

    val projectName = "My Project" // TODO load from database or web

    private val context = getApplication<Application>().applicationContext!!
    private var service: GitHubService = getService(context)
    private val graphQL = getGraphQLClient(context)
    private val projectRepo = ProjectRepository()
    private val columnRepo = ColumnRepository(service, graphQL)
    private val cardRepo = CardRepository(service, graphQL)
    var editCard: Card? = null

    private val _columns = MutableLiveData<List<ColumnViewModel>>()
    val columns: LiveData<List<ColumnViewModel>>
        get() = _columns

    fun load(projectId: Int) {
        viewModelScope.launch {
            val nodeId = projectRepo.getById(projectId)!!.nodeId
            val cols = columnRepo.getColumnsForProject(nodeId)
            _columns.value = cols.map { ColumnViewModel(cardRepo, it) }
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

    fun updateNote(card: NoteCard) {
        viewModelScope.launch {
            // TODO don't updated if not modified, maybe just take id+text as params
            val column = findColumnForCard(card)

            val params = UpdateCardParams(card.note)
            service.updateCard(card.id, params).await()
            column?.loadCards()
        }
    }

    override fun onCleared() {
        super.onCleared()
        // TODO look into how this can get called when using activity scope
    }

    private fun findColumnForCard(card: Card) = _columns.value?.first { it.containsCard(card) }

}

// TODO verify this gets destroyed + onCleared is called
class ColumnViewModel(private val repo: CardRepository, private val column: Column) : ViewModel() {

    val id = column.id
    val name = column.name

    private val _cards = MutableLiveData<List<Card>>()
    val cards: LiveData<List<Card>>
        get() = _cards

    init {
        viewModelScope.launch {
            loadCards()
        }
    }

    fun containsCard(card: Card): Boolean = _cards.value?.contains(card) ?: false

    fun removeCard(card: Card) {
        _cards.value = _cards.value?.filter { it.id != card.id }
    }

    fun addCard(card: Card) {
        _cards.value = cards.value?.plus(card)
    }

    suspend fun loadCards() {
        _cards.value = repo.getCardsForColumn(column.node_id)
    }
}