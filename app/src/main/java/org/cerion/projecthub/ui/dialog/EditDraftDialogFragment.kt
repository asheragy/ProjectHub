package org.cerion.projecthub.ui.dialog

import EditDraftDialog
import EditDraftDialogState
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.DialogFragment
import org.cerion.projecthub.model.DraftIssueCard
import org.cerion.projecthub.ui.AppTheme
import org.cerion.projecthub.ui.project.ProjectHomeViewModel
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class EditDraftDialogFragment : DialogFragment() {

    private val projectViewModel: ProjectHomeViewModel by sharedViewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_FRAME, android.R.style.ThemeOverlay_Material_Dialog)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val args = EditDraftDialogFragmentArgs.fromBundle(requireArguments())
        val viewModel = projectViewModel.columns.value!![args.columnIndex]

        val isNew = args.cardId == ""
        var contentId = ""
        var initialState = EditDraftDialogState("", "")

        viewModel.cards.value!!.firstOrNull { it.itemId == args.cardId }?.let {
            it as DraftIssueCard
            initialState = EditDraftDialogState(title = it.title, body = it.body)
            contentId = it.id
        }

        return ComposeView(requireContext()).apply {
            setContent {
                AppTheme {
                    EditDraftDialog(
                        initialState,
                        onDismiss = { finalState ->
                            if (finalState != null) {
                                if (isNew) {
                                    val card = DraftIssueCard.create(finalState.title, finalState.body)
                                    viewModel.addDraft(card)
                                }
                                else if (finalState == initialState) {
                                    println("Draft unchanged")
                                }
                                else {
                                    val card = DraftIssueCard(args.cardId, contentId, finalState.title, finalState.body)
                                    viewModel.updateDraft(card)
                                }
                            }

                            dismiss()
                        }
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
    }
}