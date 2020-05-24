package org.cerion.projecthub.ui.project

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchHelper.*
import androidx.recyclerview.widget.RecyclerView
import org.cerion.projecthub.databinding.FragmentColumnBinding
import org.cerion.projecthub.model.Card
import org.cerion.projecthub.model.IssueCard
import org.cerion.projecthub.model.NoteCard
import org.koin.androidx.viewmodel.ext.android.sharedViewModel


class ColumnFragment : Fragment() {
    companion object {
        private const val COLUMN_ID = "columnId"

        fun getInstance(columnId: Int): ColumnFragment {
            val fragment = ColumnFragment()
            val bundle = Bundle()
            bundle.putInt(COLUMN_ID, columnId)
            fragment.arguments = bundle
            return fragment
        }
    }

    private val parentViewModel: ProjectHomeViewModel by sharedViewModel()
    private lateinit var viewModel: ColumnViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = FragmentColumnBinding.inflate(layoutInflater, container, false)

        val columnId = arguments!!.getInt(COLUMN_ID)
        viewModel = parentViewModel.columns.value!!.first { it.id == columnId }

        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        val adapter =
            ColumnCardListAdapter(object :
                CardListener {
                override fun move(card: Card) {

                    val items = parentViewModel.columns.value!!.map { it.name }.toTypedArray()
                    val builder = AlertDialog.Builder(requireContext())
                    builder.setTitle("Move to")
                    builder.setItems(items) { _, which ->
                        val column = parentViewModel.columns.value!!.firstOrNull { it.name == items[which] }!!
                        parentViewModel.moveCard(card, column.id)
                    }

                    builder.show()
                }

                override fun onClick(card: Card) {
                    when (card) {
                        is NoteCard -> navigateToNote(card.id)
                        is IssueCard -> navigateToIssue(card.number)
                    }
                }

                override fun onArchive(card: Card) {
                    viewModel.archiveCard(card, true)
                }

                override fun onDelete(note: NoteCard) {
                    viewModel.deleteCard(note)
                }

                override fun onCloseOrOpen(issue: IssueCard) {
                    viewModel.toggleIssueState(issue)
                }
            })

        binding.recyclerView.adapter = adapter
        itemTouchHelperCallback.attachToRecyclerView(binding.recyclerView)
        //binding.recyclerView.addItemDecoration(DividerItemDecoration(parent.context, DividerItemDecoration.VERTICAL))

        viewModel.cards.observe(viewLifecycleOwner, Observer {
            adapter.setItems(it)
        })

        viewModel.eventAddIssue.observe(viewLifecycleOwner, Observer {
            if (it != null && !it.getAndSetHandled()) {
                navigateToIssue(0)
            }
        })

        viewModel.eventAddNote.observe(viewLifecycleOwner, Observer {
            if (it?.getAndSetHandled() == false) {
                navigateToNote(0)
            }
        })

        return binding.root
    }

    private fun navigateToNote(cardId: Int) {
        val action = ProjectHomeFragmentDirections.actionProjectHomeFragmentToEditNoteDialogFragment(viewModel.id, cardId)
        findNavController().navigate(action)
    }

    private fun navigateToIssue(number: Int = 0) {
        val project = parentViewModel.project.value!!
        val action = ProjectHomeFragmentDirections.actionProjectHomeFragmentToIssueFragment(viewModel.id, project.owner, project.repo, number)
        findNavController().navigate(action)
    }

    private val itemTouchHelperCallback = ItemTouchHelper(
        object : ItemTouchHelper.Callback() {

            private var from = -1
            private var to = -1

            override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
                val dragFlags = UP or DOWN or START or END
                return makeMovementFlags(dragFlags, 0)
            }

            override fun isLongPressDragEnabled() = true

            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                val adapter = recyclerView.adapter as ColumnCardListAdapter

                if (from == -1)
                    from = viewHolder.adapterPosition
                to = target.adapterPosition

                adapter.moveItem(viewHolder.adapterPosition, to)
                return true
            }

            override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
                super.clearView(recyclerView, viewHolder)

                viewModel.move(from, to)
                from = -1
                to = -1
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {}
        })
}

