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
import org.cerion.projecthub.R
import org.cerion.projecthub.databinding.ListItemLabelBinding
import org.cerion.projecthub.model.Label


class LabelsDialogFragment : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val labels = listOf(Label("Blue", Color.BLUE), Label("Red", Color.RED).apply { description = "Bugs" })

        val li = LayoutInflater.from(requireActivity())
        val view = li.inflate(R.layout.dialog_labels, null)

        val builder = AlertDialog.Builder(activity)
            .setView(view)
            .setTitle("Title here")
        //.setMultiChoiceItems(items, null) { _, i, _ -> Toast.makeText(activity, "item clicked at $i", Toast.LENGTH_SHORT).show() }

        val listView = view.findViewById(R.id.listView) as ListView
        val adapter = LabelListAdapter(requireContext(), labels)
        listView.setAdapter(adapter)
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE)
        //listView.setDivider(null)

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
        binding.name.text = item.name
        binding.description.text = item.description
        binding.color.setBackgroundColor(item.color)

        return binding.root
    }
}