package org.cerion.projecthub.model


enum class IssueState {
    Open,
    Closed
}

data class Issue(val owner: String, val repo: String, val number: Int) {

    private lateinit var _originalTitle: String
    lateinit var title: String

    private lateinit var _originalBody: String
    lateinit var body: String

    private lateinit var _originalState: IssueState
    lateinit var state: IssueState

    private lateinit var _originalLabels: List<Label>
    lateinit var labels: MutableList<Label>

    val fieldsModified: Boolean
        get() = _originalTitle != title || _originalBody != body || _originalState != state

    val labelsModified: Boolean
        get() = _originalLabels != labels

    fun init(title: String, body: String, state: IssueState, labels: List<Label>): Issue {
        return this.apply {
            this._originalTitle = title
            this.title = title

            this._originalBody = body
            this.body = body

            this._originalState = state
            this.state = state

            this._originalLabels = labels
            this.labels = labels.toMutableList()
        }
    }
}