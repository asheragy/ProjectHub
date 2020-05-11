package org.cerion.projecthub.ui.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.cerion.projecthub.R
import org.cerion.projecthub.databinding.ListItemLabelBinding
import org.cerion.projecthub.model.Label
import org.cerion.projecthub.repository.LabelRepository
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class LabelsDialogFragment : DialogFragment() {

    private val viewModel: LabelsViewModel by sharedViewModel()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val li = LayoutInflater.from(requireActivity())
        val view = li.inflate(R.layout.dialog_labels, null)

        val builder = AlertDialog.Builder(activity)
            .setView(view)
            .setTitle("Labels")
            .setPositiveButton("Save", { _, _ ->

            })

        val listView = view.findViewById(R.id.listView) as ListView

        // TODO labels should be observed, may not be loaded yet although to get to this screen they probably are
        val adapter = LabelListAdapter(requireContext(), viewModel.labels.value!!, viewModel.currentLabels, viewModel)
        listView.adapter = adapter

        return builder.create()
    }
}

class LabelsViewModel(private val labelRepo: LabelRepository) : ViewModel() {

    val labels = MutableLiveData<List<Label>>()
    val currentLabels = mutableListOf<Label>()

    // TODO get initial list from project viewmodel this will only be used for view screen checkbox changes
    fun initialize(owner: String, repo: String) {
        viewModelScope.launch {

            labels.value = labelRepo.getAll(owner, repo)
        }
        labels.value = listOf(Label("Blue", Color.BLUE), Label("Red", Color.RED).apply { description = "Bugs" })
    }

    fun setLabels(labels: List<Label>) {
        currentLabels.clear()
        currentLabels.addAll(labels)
    }
}

private class LabelListAdapter(context: Context, private val items: List<Label>, private val checked: List<Label>, private val viewModel: LabelsViewModel) : ArrayAdapter<Label>(context, 0, items) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

        val binding =
            if (convertView?.tag != null)
                convertView.tag as ListItemLabelBinding
            else
                ListItemLabelBinding.inflate(LayoutInflater.from(context), parent, false)

        val item = items[position]
        binding.name.text = item.name
        binding.description.text = item.description
        binding.color.setBackgroundColor(item.color)
        binding.checkbox.isChecked = checked.any { it.name == item.name }
        binding.checkbox.setOnCheckedChangeListener { compoundButton, b ->
            if (!b)
                viewModel.currentLabels.remove(item)
            else
                viewModel.currentLabels.add(item)
        }

        return binding.root
    }
}