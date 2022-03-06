package org.cerion.projecthub.ui.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import org.cerion.projecthub.R
import org.cerion.projecthub.databinding.ListItemLabelBinding
import org.cerion.projecthub.model.Label
import org.cerion.projecthub.ui.project.ProjectHomeViewModel
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class LabelsDialogFragment : DialogFragment() {

    private val projectViewModel: ProjectHomeViewModel by sharedViewModel()
    private val viewModel: LabelsViewModel by sharedViewModel()

    // TODO copy format used for edit note dialog that uses onCreateView
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val li = LayoutInflater.from(requireActivity())
        val view = li.inflate(R.layout.dialog_labels, null)

        val labels = mutableListOf<Label>()
        val listView = view.findViewById(R.id.listView) as ListView

        val builder = AlertDialog.Builder(activity)
            .setView(view)
            .setTitle("Labels")
            .setPositiveButton("Save") { _, _ ->
                val result = labels.filter { it.included }
                viewModel.setResult(result)
            }

        projectViewModel.labels.observe(this, Observer { it ->
            labels.addAll(it)
            labels.forEach { label ->
                label.included = (viewModel.selectedLabels.any { it == label.name })
            }

            val adapter = LabelListAdapter(requireContext(), labels)
            listView.adapter = adapter
        })

        return builder.create()
    }
}

class LabelsViewModel : ViewModel() {
    private var _selectedLabels = emptyList<String>()
    val selectedLabels: List<String>
        get() = _selectedLabels

    private val _result = MutableLiveData<List<Label>>()
    val result: LiveData<List<Label>>
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

private class LabelListAdapter(context: Context, private val items: List<Label>) : ArrayAdapter<Label>(context, 0, items) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val binding =
            if (convertView?.tag != null)
                convertView.tag as ListItemLabelBinding
            else
                ListItemLabelBinding.inflate(LayoutInflater.from(context), parent, false)

        val item = items[position]
        binding.name.text = item.name
        binding.description.text = item.description
        binding.checked.visibility = if(item.included) View.VISIBLE else View.INVISIBLE
        binding.color.setBackgroundColor(item.color)
        binding.root.setOnClickListener {
            item.included = !item.included
            binding.checked.visibility = if(item.included) View.VISIBLE else View.INVISIBLE
        }

        return binding.root
    }
}