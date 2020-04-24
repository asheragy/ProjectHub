package org.cerion.projecthub.ui.project

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.cerion.projecthub.github.CreateCardParams
import org.cerion.projecthub.github.GitHubService
import org.cerion.projecthub.github.UpdateCardParams
import org.cerion.projecthub.model.Card
import org.cerion.projecthub.model.Column
import org.cerion.projecthub.repository.CardRepository


// TODO verify this gets destroyed + onCleared is called
class ColumnViewModel(private val parent: ProjectHomeViewModel, private val repo: CardRepository, private val service: GitHubService, private val column: Column) : ViewModel() {

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

    /*
    fun addNote() {
        parent.addNote.value = SingleEvent(column)
    }

    fun addIssue() {
        parent.addIssue.value = SingleEvent(column)
    }
     */

    suspend fun loadCards() {
        _cards.value = repo.getCardsForColumn(column.node_id)
    }

    fun updateNote(cardId: Int, note: String) {
        viewModelScope.launch {
            // TODO don't updated if not modified, maybe just take id+text as params

            val params = UpdateCardParams(note)
            service.updateCard(cardId, params).await()
            loadCards()
        }
    }

    fun addNote(note: String) {
        viewModelScope.launch {
            val params = CreateCardParams(note)
            val result =  service.createCard(id, params).await()
            loadCards()
        }
    }

}