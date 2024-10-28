package org.cerion.projecthub.ui.project

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.cerion.projecthub.common.SingleEvent
import org.cerion.projecthub.model.Card
import org.cerion.projecthub.model.Column
import org.cerion.projecthub.model.DraftIssueCard
import org.cerion.projecthub.model.Issue
import org.cerion.projecthub.model.IssueCard
import org.cerion.projecthub.repository.CardRepository


// TODO verify this gets destroyed + onCleared is called
class ColumnViewModel(private val parent: ProjectHomeViewModel, private val cardRepository: CardRepository, val column: Column) : ViewModel() {

    val index = column.index
    val name = column.name

    val eventAddIssue = MutableLiveData<SingleEvent>()
    val eventAddDraft = MutableLiveData<SingleEvent>()

    private val _cards = MutableLiveData<List<Card>>()
    val cards: LiveData<List<Card>>
        get() = _cards

    private val _busy = MutableLiveData(false)
    val busy: LiveData<Boolean>
        get() = _busy

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

    fun archiveCard(card: Card, archived: Boolean) {
        launchBusy {
            cardRepository.archiveCard(parent.project.value!!, card)
            parent.refresh()
        }
    }

    fun deleteCard(card: Card) {
        launchBusy {
            cardRepository.deleteCard(parent.project.value!!, card)
            parent.refresh()
        }
    }

    fun toggleIssueState(card: IssueCard) {
        launchBusy {
            val issue = Issue(parent.project.value!!.owner, card.repository, card.number)
            cardRepository.setIssueState(issue, !card.closed)
            parent.refresh()
        }
    }

    fun addDraft(card: DraftIssueCard) {
        launchBusy {
            cardRepository.addDraftIssue(parent.project.value!!, column, card)
            parent.refresh()
        }
    }

    fun updateDraft(card: DraftIssueCard) {
        launchBusy {
            cardRepository.updateDraftIssue(card)
            parent.refresh()
        }
    }

    fun move(oldPosition: Int, newPosition: Int) {
        if (oldPosition == newPosition)
            return

        val projectId = parent.project.value!!.nodeId
        val cards = cards.value!!.toMutableList()
        val movedCard = cards[newPosition]

        //cards.removeAt(oldPosition)
        //cards.add(newPosition, movedCard)
        //_cards.value = cards

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