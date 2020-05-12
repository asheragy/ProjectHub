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
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import org.cerion.projecthub.R
import org.cerion.projecthub.databinding.ListItemLabelBinding
import org.cerion.projecthub.model.Label
import org.cerion.projecthub.repository.LabelRepository
import org.cerion.projecthub.ui.project.ProjectHomeViewModel
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class LabelsDialogFragment : DialogFragment() {

    private val viewModel: LabelsViewModel by sharedViewModel()
    private val projectViewModel: ProjectHomeViewModel by sharedViewModel()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val li = LayoutInflater.from(requireActivity())
        val view = li.inflate(R.layout.dialog_labels, null)

        val builder = AlertDialog.Builder(activity)
            .setView(view)
            .setTitle("Labels")
            .setPositiveButton("Save", { _, _ ->

            })

        val listView = view.findViewById(R.id.listView) as ListView

        projectViewModel.labels.observe(this, Observer {
            val adapter = LabelListAdapter(requireContext(), projectViewModel.labels.value!!, viewModel.currentLabels, viewModel)
            listView.adapter = adapter
        })

        return builder.create()
    }
}


class LabelsViewModel(private val labelRepo: LabelRepository) : ViewModel() {

    val currentLabels = mutableListOf<Label>()

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