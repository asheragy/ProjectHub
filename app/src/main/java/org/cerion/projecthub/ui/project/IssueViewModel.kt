package org.cerion.projecthub.ui.project

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.cerion.projecthub.model.Column
import org.cerion.projecthub.model.IssueCard
import org.cerion.projecthub.model.Label
import org.cerion.projecthub.model.Project
import org.cerion.projecthub.repository.CardRepository


data class IssueUiState(
    val new: Boolean = false,
    val title: String = "",
    val body: String = "",
    val labels: List<Label> = listOf()
)

sealed interface IssueUiEffect {
    data object Saved : IssueUiEffect
    data class ShowMessage(val text: String) : IssueUiEffect
}

sealed interface IssueEvent {
    data class TitleChanged(val value: String) : IssueEvent
    data class BodyChanged(val value: String) : IssueEvent
    data class LabelsChanged(val value: List<Label>) : IssueEvent
    //data object SaveClicked : IssueEvent
    //data object DeleteClicked : IssueEvent
}

class IssueViewModel(private val cardRepo: CardRepository) : ViewModel() {
    private val _ui = MutableStateFlow(IssueUiState())
    val ui: StateFlow<IssueUiState> = _ui.asStateFlow()

    private val _busy = MutableStateFlow(false)
    val busy = _busy.asStateFlow()

    private lateinit var issue: IssueCard
    private var repoName: String = ""
    private var number: Int = 0

    fun onEvent(action: IssueEvent) {
        when(action) {
            is IssueEvent.TitleChanged -> _ui.value = _ui.value.copy(title = action.value)
            is IssueEvent.BodyChanged -> _ui.value = _ui.value.copy(body = action.value)
            is IssueEvent.LabelsChanged -> _ui.value = _ui.value.copy(labels = action.value)
        }
    }

    private val _effects = MutableSharedFlow<IssueUiEffect>(
        replay = 0,                     // don't re-emit on new collectors
        extraBufferCapacity = 1,        // allow tryEmit from UI thread
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val effects: Flow<IssueUiEffect> = _effects

    fun load(issue: IssueCard) {
        _ui.value = IssueUiState(false, issue.title, issue.body, issue.labels)
        this.repoName = issue.repository
        this.number = issue.number

        this.issue = issue
    }

    private var column: Column? = null
    private var repositoryId: String? = null
    private var project: Project? = null

    fun loadNew(project: Project, repositoryId: String, column: Column) {
        this.project = project
        this.repositoryId = repositoryId
        this.column = column
        issue = IssueCard("", "")

        _ui.value = IssueUiState(new = true)
    }

    fun submit() {
        launchBusy {
            if (ui.value.title.isEmpty()) {
                _effects.emit(IssueUiEffect.ShowMessage("Title must not be blank"))
            }
            else {
                issue.apply {
                    title = ui.value.title
                    body = ui.value.body
                    if (issue.labels !== ui.value.labels) {
                        issue.labels.clear()
                        issue.labels.addAll(ui.value.labels)
                    }
                }
                if (ui.value.new) {
                    cardRepo.addIssue(project!!, repositoryId!!, column!!, issue)
                }
                else
                    cardRepo.updateIssue(issue)

                _effects.emit(IssueUiEffect.Saved)
            }
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