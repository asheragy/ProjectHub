package org.cerion.projecthub.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import org.cerion.projecthub.R
import org.cerion.projecthub.databinding.DialogEditDraftBinding
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
        val binding = DialogEditDraftBinding.inflate(layoutInflater, container, false)

        val args = EditDraftDialogFragmentArgs.fromBundle(requireArguments())
        val viewModel = projectViewModel.columns.value!![args.columnIndex]

        val isNew = args.cardId == ""
        var contentId = ""

        viewModel.cards.value!!.firstOrNull { it.itemId == args.cardId }?.let {
            it as DraftIssueCard
            binding.title.setText(it.title)
            binding.body.setText(it.body)
            contentId = it.contentId
        }

        binding.save.setOnClickListener {
            val title = binding.title.text.toString()
            val body = binding.body.text.toString()

            if (isNew) {
                val card = DraftIssueCard.create(title, body)
                viewModel.addDraft(card)
            }
            // TODO check if data changed
            else {
                val card = DraftIssueCard(args.cardId, contentId, title, body)
                viewModel.updateDraft(card)
            }

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