package org.cerion.projecthub.ui

import android.app.Application
import androidx.lifecycle.*
import kotlinx.coroutines.launch
import org.cerion.projecthub.common.SingleEvent
import org.cerion.projecthub.github.*
import org.cerion.projecthub.model.Card
import org.cerion.projecthub.model.Column
import org.cerion.projecthub.repository.CardRepository
import org.cerion.projecthub.repository.ColumnRepository
import org.cerion.projecthub.repository.Project
import org.cerion.projecthub.repository.ProjectRepository


class ProjectHomeViewModel(application: Application) : AndroidViewModel(application) {

    lateinit var project: Project

    val projectName = "My Project" // TODO load from database or web

    private val context = getApplication<Application>().applicationContext!!
    private var service: GitHubService = getService(context)
    private val graphQL = getGraphQLClient(context)
    private val projectRepo = ProjectRepository()
    private val columnRepo = ColumnRepository(service, graphQL)
    private val cardRepo = CardRepository(service, graphQL)

    val addNote = MutableLiveData<SingleEvent<Column>>()
    val addIssue = MutableLiveData<SingleEvent<Column>>()

    private val _columns = MutableLiveData<List<ColumnViewModel>>()
    val columns: LiveData<List<ColumnViewModel>>
        get() = _columns

    fun load(projectId: Int) {
        val vm = this
        viewModelScope.launch {
            project = projectRepo.getById(projectId)!!
            val cols = columnRepo.getColumnsForProject(project.nodeId)
            _columns.value = cols.map { ColumnViewModel(vm, cardRepo, it) }
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

    fun updateNote(cardId: Int, note: String) {
        viewModelScope.launch {
            // TODO don't updated if not modified, maybe just take id+text as params
            val column = findColumnForCard(cardId)

            val params = UpdateCardParams(note)
            service.updateCard(cardId, params).await()
            column?.loadCards()
        }
    }

    fun addNoteForColumn(columnId: Int, note: String) {
        viewModelScope.launch {
            val params = CreateCardParams(note)
            val result = service.createCard(columnId, params).await()
            findColumnById(columnId)?.loadCards()
        }
    }

    fun addIssueForColumn(columnId: Int, title: String, body: String) {
        viewModelScope.launch {
            val params = CreateIssueParams(title, body)
            val issue = service.createIssue(project.owner, project.repo, params).await()
            service.createCard(columnId, CreateIssueCardParams(issue.id)).await()

            findColumnById(columnId)?.loadCards()
        }
    }

    override fun onCleared() {
        super.onCleared()
        // TODO look into how this can get called when using activity scope
    }

    private fun findColumnById(id: Int) = _columns.value?.first { it.id == id}
    private fun findColumnForCard(id: Int) = _columns.value?.first { it.containsCard(id) }

}

// TODO verify this gets destroyed + onCleared is called
class ColumnViewModel(private val parent: ProjectHomeViewModel, private val repo: CardRepository, private val column: Column) : ViewModel() {

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

    @Deprecated("use id version")
    fun containsCard(card: Card): Boolean = _cards.value?.contains(card) ?: false
    fun containsCard(cardId: Int): Boolean = _cards.value?.any { it.id == cardId} ?: false

    fun removeCard(card: Card) {
        _cards.value = _cards.value?.filter { it.id != card.id }
    }

    internal fun addCard(card: Card) {
        _cards.value = cards.value?.plus(card)
    }

    fun addNote() {
        parent.addNote.value = SingleEvent(column)
    }

    fun addIssue() {
        parent.addIssue.value = SingleEvent(column)
    }

    suspend fun loadCards() {
        _cards.value = repo.getCardsForColumn(column.node_id)
    }
}