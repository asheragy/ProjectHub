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
import org.cerion.projecthub.repository.CardRepository


// TODO verify this gets destroyed + onCleared is called
class ColumnViewModel(private val parent: ProjectHomeViewModel, private val cardRepository: CardRepository, column: Column) : ViewModel() {

    val id = column.id
    val name = column.name

    val eventAddIssue = MutableLiveData<SingleEvent>()
    val eventAddDraft = MutableLiveData<SingleEvent>()

    private val _cards = MutableLiveData<List<Card>>()
    val cards: LiveData<List<Card>>
        get() = _cards

    private val _busy = MutableLiveData(false)
    val busy: LiveData<Boolean>
        get() = _busy

    @Deprecated("use id version")
    fun containsCard(card: Card): Boolean = _cards.value?.contains(card) ?: false
    fun containsCard(cardId: Int): Boolean = _cards.value?.any { it.id == cardId} ?: false

    fun removeCard(card: Card) {
        _cards.value = _cards.value?.filter { it.id != card.id }
    }


    internal fun addCard(card: Card) {
        _cards.value = cards.value?.plus(card)
    }

    fun setCards(cards: List<Card>) {
        _cards.value = cards
    }

    fun addIssue() {
        eventAddIssue.value = SingleEvent()
    }

    fun addDraft() {
        eventAddDraft.value = SingleEvent()
    }

    fun addNote(note: String) {
        launchBusy {
            cardRepository.addNoteForColumn(id, note)

            // TODO refresh all cards
        }
    }

    fun updateNote(cardId: Int, note: String) {
        launchBusy {
            cardRepository.updateNote(cardId, note)
            // TODO refresh all cards
        }
    }

    fun archiveCard(card: Card, archived: Boolean) {
        launchBusy {
            cardRepository.archiveCard(card.id, archived)
            // TODO refresh all cards
        }
    }

    fun deleteCard(card: Card) {
        if (card is IssueCard)
            throw NotImplementedError() // TODO deleting issue is different than deleting note

        launchBusy {
            cardRepository.deleteCard(card.id)
            // TODO refresh all cards
        }
    }

    fun toggleIssueState(card: IssueCard) {
        launchBusy {
            val issue = Issue(parent.project.value!!.owner, card.repository, card.number)
            cardRepository.setIssueState(issue, !card.closed)
            // TODO refresh all cards
        }
    }

    fun move(oldPosition: Int, newPosition: Int) {
        if (oldPosition == newPosition)
            return

        val projectId = parent.project.value!!.nodeId
        val cards = cards.value!!.toMutableList()
        val movedCard = cards[oldPosition]

        cards.removeAt(oldPosition)
        cards.add(newPosition, movedCard)
        _cards.value = cards

        val afterCardId = if (newPosition > 0) cards[newPosition - 1].itemId else null
        if (movedCard.itemId == afterCardId)
            throw RuntimeException("This should never happen")

        viewModelScope.launch {
            // If move fails it won't have any major side effects and get refreshed on load or other operations
            cardRepository.changeCardPosition(projectId, movedCard, afterCardId)
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