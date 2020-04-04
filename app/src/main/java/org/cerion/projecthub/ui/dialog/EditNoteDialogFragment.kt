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
import org.cerion.projecthub.model.NoteCard
import org.cerion.projecthub.ui.ProjectHomeViewModel

class EditNoteDialogFragment : DialogFragment() {

    private val viewModel: ProjectHomeViewModel by lazy {
        ViewModelProviders.of(requireActivity()).get(ProjectHomeViewModel::class.java)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return EditNoteDialog(requireContext(), viewModel)
    }
}

private class EditNoteDialog(context: Context, private val viewModel: ProjectHomeViewModel) : Dialog(context) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.dialog_edit_note)
        window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)

        val note = viewModel.editCard!! as NoteCard
        val editText = findViewById<EditText>(R.id.text)
        editText.setText(note.note)

        findViewById<Button>(R.id.save).setOnClickListener {
            note.note = editText.text.toString()
            viewModel.updateNote(note)
            dismiss()
        }
    }
}