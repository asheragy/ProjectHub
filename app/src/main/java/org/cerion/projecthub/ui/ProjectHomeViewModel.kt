package org.cerion.projecthub.ui

import android.app.Application
import androidx.lifecycle.*
import kotlinx.coroutines.launch
import org.cerion.projecthub.github.GitHubService
import org.cerion.projecthub.github.getService
import org.cerion.projecthub.model.Card
import org.cerion.projecthub.model.Column
import org.cerion.projecthub.repository.CardRepository
import org.cerion.projecthub.repository.ColumnRepository

class ProjectHomeViewModel(application: Application) : AndroidViewModel(application) {

    val projectName = "My Project" // TODO load from database or web

    private var service: GitHubService = getService(this.getApplication<Application>().applicationContext!!)
    private val repo = ColumnRepository(service)

    private val _columns = MutableLiveData<List<Column>>()
    val columns: LiveData<List<Column>>
        get() = _columns

    fun load(projectId: Int) {
        viewModelScope.launch {
            _columns.value = repo.getColumnsForProject(projectId)
        }
    }
}

// TODO verify this gets destroyed + onCleared is called
class ColumnViewModel(private val repo: CardRepository, private val column: Column) : ViewModel() {

    val id = column.id
    val name = column.name

    private val _cards = MutableLiveData<List<Card>>()
    val cards: LiveData<List<Card>>
        get() = _cards

    init {
        viewModelScope.launch {
            _cards.value = repo.getCardsForColumn(column.id)
        }
    }
}