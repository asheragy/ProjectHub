package org.cerion.projecthub.model


enum class IssueState {
    Open,
    Closed
}

data class Issue(val owner: String, val repo: String, val number: Int) {

    private var _originalTitle = ""
    var title = _originalTitle

    private var _originalBody = ""
    var body = _originalBody

    private var _originalState = IssueState.Open
    var state = _originalState

    private var _originalLabels = listOf<Label>()
    var labels = _originalLabels.toMutableList()

    val fieldsModified: Boolean
        get() = _originalTitle != title || _originalBody != body || _originalState != state

    val labelsModified: Boolean
        get() = _originalLabels != labels

    fun init(title: String, body: String?, state: IssueState, labels: List<Label>): Issue {
        return this.apply {
            this._originalTitle = title
            this.title = title

            this._originalBody = body ?: ""
            this.body = body ?: ""

            this._originalState = state
            this.state = state

            this._originalLabels = labels
            this.labels = labels.toMutableList()
        }
    }
}