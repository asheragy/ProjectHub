package org.cerion.projecthub.ui

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import org.cerion.projecthub.databinding.FragmentProjectHomeBinding
import org.cerion.projecthub.model.Card
import org.cerion.projecthub.model.IssueCard
import org.cerion.projecthub.model.NoteCard

// TODO https://issuetracker.google.com/issues/111614463

class ProjectHomeFragment : Fragment() {

    private val args: ProjectHomeFragmentArgs by navArgs()
    private lateinit var viewModel: ProjectHomeViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = FragmentProjectHomeBinding.inflate(inflater, container, false)


        viewModel = ViewModelProviders.of(requireActivity()).get(ProjectHomeViewModel::class.java)
        //binding.viewModel = viewModel
        binding.lifecycleOwner = this

        requireActivity().title = viewModel.projectName

        val adapter = ProjectColumnListAdapter(viewLifecycleOwner, object : BoardListener {
            override fun move(card: Card) {
                val items = viewModel.columns.value!!.map { it.name }.toTypedArray()

                val builder = AlertDialog.Builder(requireContext())
                builder.setTitle("Move to")
                builder.setItems(items) { _, which ->
                    val column = viewModel.columns.value!!.firstOrNull { it.name == items[which] }!!
                    viewModel.moveCard(card, column.id)
                }

                builder.show()
            }

            override fun onClick(card: Card) {
                when (card) {
                    is NoteCard -> onEditNote(card)
                    is IssueCard -> onEditIssue(card)
                }

            }
        })

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.recyclerView.adapter = adapter
        binding.recyclerView.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.HORIZONTAL))

        viewModel.columns.observe(viewLifecycleOwner, Observer {
            adapter.setItems(it)
        })

        viewModel.addNote.observe(viewLifecycleOwner, Observer { event ->
            event.getContentIfNotHandled()?.let { onAddNote(it.id) }
        })

        viewModel.addIssue.observe(viewLifecycleOwner, Observer { event ->
            event.getContentIfNotHandled()?.let { onAddIssue(it.id) }
        })

        // TODO should not reload every time
        viewModel.load(args.projectId)

        return binding.root
    }

    private fun onAddNote(columnId: Int) {
        val action = ProjectHomeFragmentDirections.actionProjectHomeFragmentToEditNoteDialogFragment(columnId, null)
        findNavController().navigate(action)
    }

    private fun onEditNote(card: NoteCard) {
        val action = ProjectHomeFragmentDirections.actionProjectHomeFragmentToEditNoteDialogFragment(card.id, card.note)
        findNavController().navigate(action)
    }

    private fun onAddIssue(columnId: Int) {
        val action = ProjectHomeFragmentDirections.actionProjectHomeFragmentToIssueFragment(columnId, null, null, 0)
        findNavController().navigate(action)
    }

    private fun onEditIssue(issue: IssueCard) {
        val action = ProjectHomeFragmentDirections.actionProjectHomeFragmentToIssueFragment(issue.id, viewModel.project.owner, issue.repository, issue.number)
        findNavController().navigate(action)
    }


}
