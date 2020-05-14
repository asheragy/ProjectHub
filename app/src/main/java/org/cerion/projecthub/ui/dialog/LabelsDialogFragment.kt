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
import org.cerion.projecthub.R
import org.cerion.projecthub.databinding.ListItemLabelBinding
import org.cerion.projecthub.model.Label
import org.cerion.projecthub.ui.project.ProjectHomeViewModel
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class LabelsDialogFragment : DialogFragment() {

    private val projectViewModel: ProjectHomeViewModel by sharedViewModel()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val li = LayoutInflater.from(requireActivity())
        val view = li.inflate(R.layout.dialog_labels, null)

        val labels = mutableListOf<Label>()
        val listView = view.findViewById(R.id.listView) as ListView
        val currentLabels = LabelsDialogFragmentArgs.fromBundle(requireArguments()).currentLabels.toList()

        val builder = AlertDialog.Builder(activity)
            .setView(view)
            .setTitle("Labels")
            .setPositiveButton("Save") { _, _ ->

                // TODO pass back to sending fragment
                labels.filter { it.included }.forEach {
                    println(it)
                }
            }

        projectViewModel.labels.observe(this, Observer { it ->
            labels.addAll(it)
            labels.forEach { label ->
                label.included = (currentLabels.any { it == label.name })
            }

            val adapter = LabelListAdapter(requireContext(), labels)
            listView.adapter = adapter
        })

        return builder.create()
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
        binding.label = item
        binding.color.setBackgroundColor(item.color)
        binding.root.setOnClickListener {
            item.included = !item.included
            binding.invalidateAll()
        }

        binding.executePendingBindings()

        return binding.root
    }
}