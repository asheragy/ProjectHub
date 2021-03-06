package org.cerion.projecthub.ui.project

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.cerion.projecthub.common.SingleEvent
import org.cerion.projecthub.model.Card
import org.cerion.projecthub.model.Column
import org.cerion.projecthub.model.Issue
import org.cerion.projecthub.model.IssueCard
import org.cerion.projecthub.repository.CardPosition.*
import org.cerion.projecthub.repository.CardRepository


// TODO verify this gets destroyed + onCleared is called
class ColumnViewModel(private val parent: ProjectHomeViewModel, private val cardRepository: CardRepository, private val column: Column) : ViewModel() {

    val id = column.id
    val name = column.name

    val eventAddIssue = MutableLiveData<SingleEvent>()
    val eventAddNote = MutableLiveData<SingleEvent>()

    private val _cards = MutableLiveData<List<Card>>()
    val cards: LiveData<List<Card>>
        get() = _cards

    private val _busy = MutableLiveData(false)
    val busy: LiveData<Boolean>
        get() = _busy

    init {
        refresh()
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

    fun refresh() {
        launchBusy {
            loadCards()
        }
    }

    suspend fun loadCards() {
        _cards.value = cardRepository.getCardsForColumn(column.node_id)
    }

    fun addIssue() {
        eventAddIssue.value = SingleEvent()
    }

    fun addNote() {
        eventAddNote.value = SingleEvent()
    }

    fun addNote(note: String) {
        launchBusy {
            cardRepository.addNoteForColumn(id, note)
            loadCards() // TODO need fields for order but can update card manually without this request
        }
    }

    fun updateNote(cardId: Int, note: String) {
        launchBusy {
            cardRepository.updateNote(cardId, note)
            loadCards()
        }
    }

    fun archiveCard(card: Card, archived: Boolean) {
        launchBusy {
            cardRepository.archiveCard(card.id, archived)
            loadCards()
        }
    }

    fun deleteCard(card: Card) {
        if (card is IssueCard)
            throw NotImplementedError() // TODO deleting issue is different than deleting note

        launchBusy {
            cardRepository.deleteCard(card.id)
            loadCards()
        }
    }

    fun toggleIssueState(card: IssueCard) {
        launchBusy {
            val issue = Issue(parent.project.value!!.owner, card.repository, card.number)
            cardRepository.setIssueState(issue, !card.closed)
            loadCards()
        }
    }

    fun move(oldPosition: Int, newPosition: Int) {
        if (oldPosition == newPosition)
            return

        val cards = cards.value!!.toMutableList()
        val movedCard = cards[newPosition]

        // Get position AND Update internal list order, adapter will get updates but should already have the new order
        val position =
            when (newPosition) {
                0 -> {
                    //cards.removeAt(oldPosition)
                    //cards.add(0, movedCard)
                    TOP
                }
                cards.size - 1 -> {
                    //cards.removeAt(oldPosition)
                    //cards.add(movedCard)
                    BOTTOM
                }
                else -> AFTER
            }

        val relativeCardId = if (position == AFTER) cards[newPosition - 1].id else 0

        _cards.value = cards

        viewModelScope.launch {
            // If move fails it won't have any major side effects and get refreshed on load or other operations
            cardRepository.move(movedCard, column.id, position, relativeCardId)
        }
    }

    private fun launchBusy(action: suspend () -> Unit) {
        viewModelScope.launch {
            try {
                _busy.value = true
                action()
            }
            finally {
                _busy.value = false
            }
        }
    }
}