package org.cerion.projecthub.model

import androidx.databinding.ObservableArrayList
import androidx.databinding.ObservableField
import androidx.databinding.ObservableList


enum class IssueState {
    Open,
    Closed
}

data class Issue(val owner: String, val repo: String, val number: Int) {

    private val _title = ObservableField<String>("")
    var title: String
        get() = _title.get()!!
        set(value) {
            if (value != _title.get()) { // TODO data binding is setting this to existing value, check if viewbinding is different or anyway to prevent it
                _title.set(value)
                _fieldsModified = true
            }
        }

    private val _body = ObservableField("")
    var body: String
        get() = _body.get()!!
        set(value) {
            if (value != _body.get()) {
                _body.set(value)
                _fieldsModified = true
            }
        }

    private var _state = ObservableField(IssueState.Open)
    var state: IssueState
        get() = _state.get()!!
        set(value) {
            if (value != _state.get()) {
                _state.set(value)
                _fieldsModified = true
            }
        }

    private val _labels = ObservableArrayList<Label>()
    val labels: MutableList<Label>
        get() = _labels

    private var _fieldsModified = false
    val fieldsModified: Boolean
        get() = _fieldsModified

    private var _labelsModified = false
    val labelsModified: Boolean
        get() = _labelsModified

    fun init(block: Issue.() -> Unit): Issue {
        return this.apply {
            block()
            _labelsModified = false
            _fieldsModified = false
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
    }


}