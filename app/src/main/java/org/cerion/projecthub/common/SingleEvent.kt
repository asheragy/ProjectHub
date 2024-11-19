package org.cerion.projecthub.common


open class SingleEventData<out T>(private val content: T) {
    private var handled = false

    fun getContentIfNotHandled(): T? {
        return if (handled) {
            null
        } else {
            handled = true
            content
        }
    }
}

open class SingleEvent {
    private var handled = false

    fun getAndSetHandled(): Boolean {
        return if (handled)
            true
        else {
            handled = true
            false
        }
    }
}