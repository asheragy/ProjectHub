package org.cerion.projecthub.ui.project

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import org.cerion.projecthub.databinding.FragmentColumnBinding
import org.cerion.projecthub.model.Card
import org.cerion.projecthub.model.IssueCard
import org.cerion.projecthub.model.NoteCard

interface CardListener {
    fun move(card: Card)
    fun onClick(card: Card)
}

class ColumnFragment(columnId: Int) : Fragment() {
    companion object {
        private const val COLUMN_ID = "columnId"
    }

    init {
        val bundle = Bundle()
        bundle.putInt(COLUMN_ID, columnId)
        arguments = bundle
    }

    private lateinit var parentViewModel: ProjectHomeViewModel
    private lateinit var viewModel: ColumnViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = FragmentColumnBinding.inflate(layoutInflater, container, false)

        parentViewModel = ViewModelProviders.of(requireActivity()).get(ProjectHomeViewModel::class.java)
        val columnId = arguments!!.getInt(COLUMN_ID)
        viewModel = parentViewModel.columns.value!!.first { it.id == columnId }

        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        val adapter =
            ColumnCardListAdapter(object :
                CardListener {
                override fun move(card: Card) {
                    /*
                    val items = viewModel.columns.value!!.map { it.name }.toTypedArray()

                    val builder = AlertDialog.Builder(requireContext())
                    builder.setTitle("Move to")
                    builder.setItems(items) { _, which ->
                        val column = viewModel.columns.value!!.firstOrNull { it.name == items[which] }!!
                        viewModel.moveCard(card, column.id)
                    }

                    builder.show()

                     */
                }

                override fun onClick(card: Card) {
                    when (card) {
                        is NoteCard -> onEditNote(card)
                        is IssueCard -> onEditIssue(card)
                    }

                }
            })

        binding.recyclerView.adapter = adapter
        //binding.recyclerView.addItemDecoration(DividerItemDecoration(parent.context, DividerItemDecoration.VERTICAL))

        viewModel.cards.observe(viewLifecycleOwner, Observer {
            adapter.setItems(it)
        })

        return binding.root
    }

    fun onAddNote() {
        val action = ProjectHomeFragmentDirections.actionProjectHomeFragmentToEditNoteDialogFragment(viewModel.id, 0)
        findNavController().navigate(action)
    }

    fun onAddIssue() {
        val action = ProjectHomeFragmentDirections.actionProjectHomeFragmentToIssueFragment(viewModel.id, null, null, 0)
        findNavController().navigate(action)
    }

    private fun onEditNote(card: NoteCard) {
        val action = ProjectHomeFragmentDirections.actionProjectHomeFragmentToEditNoteDialogFragment(viewModel.id, card.id)
        findNavController().navigate(action)
    }

    private fun onEditIssue(issue: IssueCard) {
        val action =
            ProjectHomeFragmentDirections.actionProjectHomeFragmentToIssueFragment(
                issue.id,
                parentViewModel.project.value!!.owner,
                issue.repository,
                issue.number
            )
        findNavController().navigate(action)
    }
}

