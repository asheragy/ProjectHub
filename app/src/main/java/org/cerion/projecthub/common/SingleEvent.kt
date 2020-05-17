package org.cerion.projecthub.common


open class SingleEventData<out T>(private val content: T) {
    var handled = false
        private set

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
    var handled = false
        private set

    fun getAndSetHandled(): Boolean {
        return if (handled)
            true
        else {
            handled = true
            false
        }
    }
}