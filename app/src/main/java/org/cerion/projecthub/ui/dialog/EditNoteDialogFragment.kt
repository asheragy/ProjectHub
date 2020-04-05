package org.cerion.projecthub.ui.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProviders
import org.cerion.projecthub.R
import org.cerion.projecthub.ui.ProjectHomeViewModel

class EditNoteDialogFragment : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val args = EditNoteDialogFragmentArgs.fromBundle(requireArguments())
        val viewModel = ViewModelProviders.of(requireActivity()).get(ProjectHomeViewModel::class.java)

        return EditNoteDialog(requireContext(), viewModel, args)
    }
}

private class EditNoteDialog(context: Context, private val viewModel: ProjectHomeViewModel, private val args: EditNoteDialogFragmentArgs) : Dialog(context) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.dialog_edit_note)
        window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)

        val isNew = args.note == null
        val note = args.note ?: ""

        val editText = findViewById<EditText>(R.id.text)
        editText.setText(note)

        findViewById<Button>(R.id.save).setOnClickListener {
            val newNote = editText.text.toString()
            if (isNew)
                viewModel.addNoteForColumn(args.columnOrCardId, newNote)
            else
                viewModel.updateNote(args.columnOrCardId, newNote)

            dismiss()
        }
    }
}