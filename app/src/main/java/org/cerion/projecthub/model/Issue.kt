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

    val _labels = ObservableArrayList<Label>()
    val labels: MutableList<Label>
        get() = _labels

    var _modified = false
    val modified: Boolean
        get() = _modified

    fun init(block: Issue.() -> Unit): Issue {
        return this.apply {
            block()
            _modified = false
        }
    }

    init {
        _labels.addOnListChangedCallback(object : ObservableList.OnListChangedCallback<ObservableArrayList<Label>>() {
            override fun onChanged(sender: ObservableArrayList<Label>?) {
                _modified = true
            }

            override fun onItemRangeRemoved(sender: ObservableArrayList<Label>?, positionStart: Int, itemCount: Int) {
                _modified = true
            }

            override fun onItemRangeMoved(sender: ObservableArrayList<Label>?, fromPosition: Int, toPosition: Int, itemCount: Int) {
                _modified = true
            }

            override fun onItemRangeInserted(sender: ObservableArrayList<Label>?, positionStart: Int, itemCount: Int) {
                _modified = true
            }

            override fun onItemRangeChanged(sender: ObservableArrayList<Label>?, positionStart: Int, itemCount: Int) {
                _modified = true
            }
        })

        _modified = false
    }


}