package org.cerion.projecthub.ui.project

import IssueEditScreen
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.launch
import org.cerion.projecthub.model.IssueCard
import org.cerion.projecthub.ui.AppTheme
import org.cerion.projecthub.ui.dialog.BusyDialog
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class IssueFragment : Fragment() {

    private val projectViewModel: ProjectHomeViewModel by sharedViewModel()
    private val viewModel: IssueViewModel by viewModel()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val args = IssueFragmentArgs.fromBundle(requireArguments())
        val column = projectViewModel.columns.value!!.first { it.column.optionId == args.columnId }

        if (args.id.isEmpty()) {
            viewModel.loadNew(projectViewModel.project.value!!, projectViewModel.repositoryId, column.column)
        } else {
            val issue = column.findCardById(args.id)
            viewModel.load(issue as IssueCard)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.effects.collect { effect ->
                    when (effect) {
                        IssueUiEffect.Saved -> {
                            projectViewModel.refresh()
                            parentFragmentManager.popBackStack()
                        }
                        is IssueUiEffect.ShowMessage -> Toast.makeText(requireContext(), effect.text, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        return ComposeView(requireContext()).apply {
            setContent {
                val globalLabels by projectViewModel.labels.observeAsState(listOf())
                val state by viewModel.ui.collectAsState()
                val busy by viewModel.busy.collectAsState()

                AppTheme {
                    BusyDialog(busy)
                    IssueEditScreen(
                        state = state,
                        onEvent = {
                            viewModel.onEvent(it)
                        },
                        labels =  globalLabels,
                        onClose = {
                            // TODO findNavController().navigateUp() instead?
                            parentFragmentManager.popBackStack()
                        },
                        onSave = {
                            viewModel.submit()
                        })
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        (requireActivity() as AppCompatActivity).supportActionBar?.hide()
    }

    override fun onStop() {
        (requireActivity() as AppCompatActivity).supportActionBar?.show()
        super.onStop()
    }
}
