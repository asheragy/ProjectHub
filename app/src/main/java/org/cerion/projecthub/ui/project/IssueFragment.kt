package org.cerion.projecthub.ui.project

import IssueEditScreen
import IssueEditState
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import kotlinx.coroutines.launch
import org.cerion.projecthub.model.IssueCard
import org.cerion.projecthub.ui.AppTheme
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class IssueFragment : Fragment() {

    private val projectViewModel: ProjectHomeViewModel by sharedViewModel()
    private val viewModel: IssueViewModel by viewModel()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val args = IssueFragmentArgs.fromBundle(requireArguments())
        val column = projectViewModel.columns.value!!.first { it.column.optionId == args.columnId }

        val issue = if (args.id.isEmpty()) {
            viewModel.load(projectViewModel.project.value!!, projectViewModel.repositoryId, column.column)
            IssueCard("", "")
        } else {
            val issue = column.findCardById(args.id)
            viewModel.load(issue as IssueCard)
            issue
        }

        return ComposeView(requireContext()).apply {
            setContent {
                val globalLabels by projectViewModel.labels.observeAsState(listOf())
                val tempState = IssueEditState(issue.title, issue.body, issue.labels)
                val scope = rememberCoroutineScope()

                AppTheme {
                    IssueEditScreen(
                        initialState = tempState,
                        labels =  globalLabels,
                        onClose = {
                            // TODO findNavController().navigateUp() instead?
                            parentFragmentManager.popBackStack()
                        },
                        onSave = {
                            issue.title = it.title
                            issue.body = it.body
                            if (issue.labels !== it.labels) {
                                issue.labels.clear()
                                issue.labels.addAll(it.labels)
                            }

                            // TODO add busy indicator, simulate long save to test
                            scope.launch {
                                try {
                                    viewModel.save(issue)
                                    projectViewModel.refresh()
                                    parentFragmentManager.popBackStack()
                                }
                                catch (e: IllegalArgumentException) {
                                    Toast.makeText(requireContext(), e.message, Toast.LENGTH_SHORT).show()
                                }
                            }
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
