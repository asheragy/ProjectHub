package org.cerion.projecthub.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import org.cerion.projecthub.R
import org.cerion.projecthub.databinding.DialogEditNoteBinding
import org.cerion.projecthub.model.DraftIssueCard
import org.cerion.projecthub.ui.project.ProjectHomeViewModel
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class EditDraftDialogFragment : DialogFragment() {

    private val projectViewModel: ProjectHomeViewModel by sharedViewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_FRAME, android.R.style.ThemeOverlay_Material_Dialog)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val binding = DialogEditNoteBinding.inflate(layoutInflater, container, false)

        val args = EditDraftDialogFragmentArgs.fromBundle(requireArguments())
        val viewModel = projectViewModel.columns.value!![args.columnIndex]

        val isNew = args.cardId == ""

        viewModel.cards.value!!.firstOrNull { it.itemId == args.cardId }.let {
            it as DraftIssueCard
            binding.title.setText(it.title)
            binding.body.setText(it.body)
        }

        binding.save.setOnClickListener {
            //val newNote = binding.text.text.toString()
            /* TODO save
            if (isNew)
                viewModel.addNote(newNote)
            else if(note != newNote)
                viewModel.updateNote(args.cardId, newNote)

             */

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