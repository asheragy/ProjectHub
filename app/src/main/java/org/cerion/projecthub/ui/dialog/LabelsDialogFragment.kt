package org.cerion.projecthub.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.cerion.projecthub.model.Label
import org.cerion.projecthub.ui.AppTheme
import org.cerion.projecthub.ui.project.ProjectHomeViewModel
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class LabelsDialogFragment : DialogFragment() {

    private val projectViewModel: ProjectHomeViewModel by sharedViewModel()
    private val viewModel: LabelsViewModel by sharedViewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_FRAME, android.R.style.ThemeOverlay_Material_Dialog)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return ComposeView(requireContext()).apply {
            setContent {
                AppTheme {
                    val globalLabels by projectViewModel.labels.observeAsState(listOf())
                    val labels = remember(globalLabels) {
                        mutableStateListOf<LabelSelection>().apply {
                            addAll(
                                globalLabels.map { label ->
                                    LabelSelection(label, viewModel.selectedLabels.any { it == label.name })
                                }
                            )
                        }
                    }

                    LabelsDialog(
                        labels,
                        onSelect = { selection ->
                            val index = labels.indexOfFirst { it.label.name == selection.label.name }
                            if (index != -1) {
                                val updated = selection.copy(selected = !selection.selected)
                                labels[index] = updated
                            }
                        }, onClose = {
                            dismiss()
                        }, onSave = {
                            val selected = labels.filter { it.selected }.map { it.label }
                            viewModel.setResult(selected)
                            dismiss()
                        }
                    )
                }
            }
        }
    }
}

class LabelsViewModel : ViewModel() {
    private var _selectedLabels = emptyList<String>()
    val selectedLabels: List<String>
        get() = _selectedLabels

    private val _result = MutableLiveData<List<Label>?>()
    val result: LiveData<List<Label>?>
        get() = _result

    fun setLabels(labels: List<String>) {
        _selectedLabels = labels
        _result.value = null
    }

    fun setResult(labels: List<Label>) {
        _result.value = labels
    }

    fun onRecieveResult() {
        _result.value = null
    }
}