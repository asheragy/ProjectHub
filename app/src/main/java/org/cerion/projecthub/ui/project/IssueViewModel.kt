package org.cerion.projecthub.ui.project

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.cerion.projecthub.common.SingleEventData
import org.cerion.projecthub.model.Column
import org.cerion.projecthub.model.IssueCard
import org.cerion.projecthub.model.Label
import org.cerion.projecthub.model.Project
import org.cerion.projecthub.repository.CardRepository


class IssueViewModel(private val cardRepo: CardRepository) : ViewModel() {

    val issue = MutableLiveData<IssueCard>()
    val finished = MutableLiveData(false)
    val message = MutableLiveData<SingleEventData<String>>()
    val labelsChanged = MutableLiveData<SingleEventData<List<Label>>>()

    private var ownerName: String = ""
    private var repoName: String = ""
    private var number: Int = 0
    private var columnId: Int = 0

    private val _busy = MutableLiveData(false)
    val busy: LiveData<Boolean>
        get() = _busy

    private val isNew: Boolean
        get() = number == 0

    val title: String
        get() = if (isNew) "New Issue" else "Issue $number"

    fun load(issueCard: IssueCard) {
        //this.columnId = columnId
        //this.ownerName = issue.author
        this.repoName = issueCard.repository
        this.number = issueCard.number

        if (isNew) {
            //issue.value = Issue(owner, repo, 0)
        }
        else {

            issue.value = issueCard
            //launchBusy {
            //    issue.value = issueRepo.getByNumber(owner, repo, number)
            //}
        }
    }

    private var column: Column? = null
    private var repositoryId: String? = null
    private var project: Project? = null

    fun load(project: Project, repositoryId: String, column: Column) {
        this.project = project
        this.repositoryId = repositoryId
        this.column = column
        issue.value = IssueCard("", "")
    }

    fun setLabels(labels: List<Label>) {
        issue.value!!.labels.apply {
            if (this != labels) {
                clear()
                addAll(labels)
            }
        }

        labelsChanged.value = SingleEventData(labels)
    }

    suspend fun save(issue: IssueCard) {
        if (issue.title.isEmpty()) {
            throw IllegalArgumentException("Title must not be blank")
        }

        if (isNew) {
            cardRepo.addIssue(project!!, repositoryId!!, column!!, issue)
        }
        else
            cardRepo.updateIssue(issue)
    }

    fun submit() {
        // TODO failure to update indicator
        issue.value?.let {

            if (it.title.isEmpty()) {
                message.value = SingleEventData("Title must not be blank")
                return
            }

            launchBusy {
                if (isNew) {
                    cardRepo.addIssue(project!!, repositoryId!!, column!!, it)
                }
                else
                    cardRepo.updateIssue(it)

                finished.value = true
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