package org.cerion.projecthub.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import org.cerion.projecthub.R
import org.cerion.projecthub.databinding.DialogEditNoteBinding
import org.cerion.projecthub.model.NoteCard
import org.cerion.projecthub.ui.project.ProjectHomeViewModel
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class EditNoteDialogFragment : DialogFragment() {

    private val projectViewModel: ProjectHomeViewModel by sharedViewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_FRAME, android.R.style.ThemeOverlay_Material_Dialog)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = DialogEditNoteBinding.inflate(layoutInflater, container, false)

        val args = EditNoteDialogFragmentArgs.fromBundle(requireArguments())
        val viewModel = projectViewModel.findColumnById(args.columnId)!!

        val isNew = args.cardId == 0
        val note = if (isNew) "" else {
            val card = viewModel.cards.value!!.first { it.id == args.cardId } as NoteCard
            card.note
        }

        binding.text.setText(note)
        binding.save.setOnClickListener {
            val newNote = binding.text.text.toString()
            if (isNew)
                viewModel.addNote(newNote)
            else if(note != newNote)
                viewModel.updateNote(args.cardId, newNote)

            dismiss()
        }

        binding.toolbar.inflateMenu(R.menu.edit_note)
        binding.toolbar.menu.getItem(0).setOnMenuItemClickListener {
            dismiss()
            true
        }

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
    }
}