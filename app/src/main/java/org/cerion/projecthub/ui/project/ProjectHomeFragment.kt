package org.cerion.projecthub.ui.project

import android.graphics.Color
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.woxthebox.draglistview.BoardView
import com.woxthebox.draglistview.BoardView.BoardCallback
import com.woxthebox.draglistview.BoardView.BoardListener
import com.woxthebox.draglistview.ColumnProperties
import org.cerion.projecthub.R
import org.cerion.projecthub.databinding.ColumnFooterBinding
import org.cerion.projecthub.databinding.ColumnHeaderBinding
import org.cerion.projecthub.databinding.FragmentProjectHomeBinding
import org.cerion.projecthub.model.Card
import org.cerion.projecthub.model.IssueCard
import org.cerion.projecthub.model.NoteCard
import org.koin.androidx.viewmodel.ext.android.sharedViewModel


// TODO https://issuetracker.google.com/issues/111614463

class ProjectHomeFragment : Fragment() {

    private val args: ProjectHomeFragmentArgs by navArgs()
    private val viewModel: ProjectHomeViewModel by sharedViewModel()
    private lateinit var binding: FragmentProjectHomeBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentProjectHomeBinding.inflate(inflater, container, false)
        viewModel.load(args.projectId)
        //binding.viewModel = viewModel
        //binding.lifecycleOwner = this

        viewModel.project.observe(viewLifecycleOwner, Observer {
            (requireActivity() as AppCompatActivity).supportActionBar?.title = it?.name ?: ""
        })

        viewModel.columns.observe(viewLifecycleOwner, Observer { columns ->
            binding.board.clearBoard()
            columns.forEach { addColumn(it, inflater) }
        })

        setHasOptionsMenu(true)
        initBoard()

        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.project_home, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_refresh -> {
                viewModel.refresh()
                return true
            }
        }

        return false
    }

    private fun addColumn(columnViewModel: ColumnViewModel, inflater: LayoutInflater) {
        val adapter = CardListAdapter(getListenerForColumn(columnViewModel))
        val header = ColumnHeaderBinding.inflate(inflater)
        val footer = ColumnFooterBinding.inflate(inflater)

        //val backgroundColor = ContextCompat.getColor(context, R.color.column_background)
        val columnProperties = ColumnProperties.Builder.newBuilder(adapter)
            .setLayoutManager(LinearLayoutManager(context))
            .setHasFixedItemSize(false)
            .setColumnBackgroundColor(Color.TRANSPARENT)
            //.setItemsSectionBackgroundColor(backgroundColor)
            .setHeader(header.root)
            .setFooter(footer.root)
            //.setColumnDragView(header)
            .build()

        binding.board.addColumn(columnProperties)

        // Observe fields
        columnViewModel.cards.observe(viewLifecycleOwner, Observer {
            adapter.itemList = it
        })

        columnViewModel.eventAddIssue.observe(viewLifecycleOwner, Observer {
            if (it != null && !it.getAndSetHandled())
                navigateToIssue(columnViewModel.id)
        })

        columnViewModel.eventAddNote.observe(viewLifecycleOwner, Observer {
            if (it?.getAndSetHandled() == false)
                navigateToNote(columnViewModel.id)
        })

        header.name.text = columnViewModel.name
        footer.addIssue.setOnClickListener { columnViewModel.addIssue() }
        footer.addNote.setOnClickListener { columnViewModel.addNote() }
    }

    private fun navigateToNote(columnId: Int, cardId: Int = 0) {
        val action = ProjectHomeFragmentDirections.actionProjectHomeFragmentToEditNoteDialogFragment(columnId, cardId)
        findNavController().navigate(action)
    }

    private fun navigateToIssue(columnId: Int, number: Int = 0) {
        val project = viewModel.project.value!!
        val action = ProjectHomeFragmentDirections.actionProjectHomeFragmentToIssueFragment(columnId, project.owner, project.repo, number)
        findNavController().navigate(action)
    }

    private fun initBoard() {
        binding.board.apply {
            setSnapToColumnsWhenScrolling(true)
            setSnapToColumnWhenDragging(true)
            setSnapDragItemToTouch(true)
            setSnapToColumnInLandscape(false)
            setColumnSnapPosition(BoardView.ColumnSnapPosition.CENTER)

            setBoardListener(object : BoardListener {
                override fun onItemDragStarted(column: Int, row: Int) {}

                override fun onItemDragEnded(fromColumn: Int, fromRow: Int, toColumn: Int, toRow: Int) {
                    if (fromColumn != toColumn)
                        viewModel.moveCard(toColumn, toRow)
                    else if (fromRow != toRow)
                        viewModel.columns.value!![fromColumn].move(fromRow, toRow)
                }

                override fun onItemChangedPosition(oldColumn: Int, oldRow: Int, newColumn: Int, newRow: Int) {}
                override fun onItemChangedColumn(oldColumn: Int, newColumn: Int) {}
                override fun onFocusedColumnChanged(oldColumn: Int, newColumn: Int) {}
                override fun onColumnDragStarted(position: Int) {}
                override fun onColumnDragChangedPosition(oldPosition: Int, newPosition: Int) {}
                override fun onColumnDragEnded(position: Int) {}
            })

            setBoardCallback(object : BoardCallback {
                override fun canDragItemAtPosition(column: Int, dragPosition: Int): Boolean = true
                override fun canDropItemAtPosition(oldColumn: Int, oldRow: Int, newColumn: Int, newRow: Int): Boolean = true
            })
        }
    }

    private fun getListenerForColumn(viewModel: ColumnViewModel): CardListener {
        return object : CardListener {
            override fun onClick(card: Card) {
                when (card) {
                    is NoteCard -> navigateToNote(viewModel.id, card.id)
                    is IssueCard -> navigateToIssue(viewModel.id, card.number)
                }
            }

            override fun onArchive(card: Card) {
                viewModel.archiveCard(card, true)
            }

            override fun onDelete(card: Card) {
                if (card is IssueCard)
                    Toast.makeText(requireContext(), "Not Implemented", Toast.LENGTH_SHORT).show() // TODO
                else
                    viewModel.deleteCard(card)
            }

            override fun onConvertToIssue(note: NoteCard) {
                Toast.makeText(requireContext(), "Not Implemented", Toast.LENGTH_SHORT).show() // TODO
            }

            override fun onCloseOrOpen(issue: IssueCard) {
                viewModel.toggleIssueState(issue)
            }
        }
    }
}