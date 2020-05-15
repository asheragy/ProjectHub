package org.cerion.projecthub.model

import androidx.databinding.ObservableArrayList
import androidx.databinding.ObservableList


enum class IssueState {
    Open,
    Closed
}

data class Issue(val owner: String, val repo: String, val number: Int) {
    var title = ""
    var body = ""
    var state = IssueState.Open

    private val _labels = ObservableArrayList<Label>()
    val labels: MutableList<Label>
        get() = _labels

    private var _labelsModified = false
    val labelsModified: Boolean
        get() = _labelsModified

    fun init(block: Issue.() -> Unit): Issue {
        return this.apply {
            block()
            _labelsModified = false
        }
    }

    init {
        _labels.addOnListChangedCallback(object : ObservableList.OnListChangedCallback<ObservableArrayList<Label>>() {
            override fun onChanged(sender: ObservableArrayList<Label>?) {
                _labelsModified = true
            }

            override fun onItemRangeRemoved(sender: ObservableArrayList<Label>?, positionStart: Int, itemCount: Int) {
                _labelsModified = true
            }

            override fun onItemRangeMoved(sender: ObservableArrayList<Label>?, fromPosition: Int, toPosition: Int, itemCount: Int) {
                _labelsModified = true
            }

            override fun onItemRangeInserted(sender: ObservableArrayList<Label>?, positionStart: Int, itemCount: Int) {
                _labelsModified = true
            }

            override fun onItemRangeChanged(sender: ObservableArrayList<Label>?, positionStart: Int, itemCount: Int) {
                _labelsModified = true
            }
        })

        _labelsModified = false
    }


}