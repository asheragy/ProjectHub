package org.cerion.projecthub.ui.project

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.woxthebox.draglistview.BoardView
import com.woxthebox.draglistview.BoardView.BoardCallback
import com.woxthebox.draglistview.BoardView.BoardListener
import org.cerion.projecthub.databinding.FragmentProjectHomeBinding
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
            requireActivity().title = it.name
        })

        viewModel.columns.observe(viewLifecycleOwner, Observer { columns ->
            columns.forEach { columnViewModel ->
                val view = ColumnHeaderView(requireContext(), columnViewModel)
                binding.board.addColumn(view.getColumnProperties())

                // Observe fields
                columnViewModel.eventAddIssue.observe(viewLifecycleOwner, Observer {
                    if (it != null && !it.getAndSetHandled())
                        navigateToIssue(columnViewModel.id)
                })

                columnViewModel.eventAddNote.observe(viewLifecycleOwner, Observer {
                    if (it?.getAndSetHandled() == false)
                        navigateToNote(columnViewModel.id)
                })
            }
        })

        initBoard()

        return binding.root
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
                override fun onItemDragStarted(column: Int, row: Int) {
                    //Toast.makeText(activity, "Start - column: $column row: $row", Toast.LENGTH_SHORT).show()
                }

                override fun onItemDragEnded(fromColumn: Int, fromRow: Int, toColumn: Int, toRow: Int) {
                    //if (fromColumn != toColumn || fromRow != toRow)
                    //    Toast.makeText(activity, "End - column: $toColumn row: $toRow", Toast.LENGTH_SHORT).show()
                }

                override fun onItemChangedPosition(oldColumn: Int, oldRow: Int, newColumn: Int, newRow: Int) {
                    Toast.makeText(context, "Position changed - column: $newColumn row: $newRow", Toast.LENGTH_SHORT).show()
                }

                override fun onItemChangedColumn(oldColumn: Int, newColumn: Int) {
                    //getHeaderView(oldColumn).findViewById(R.id.item_count).text = "" + getAdapter(oldColumn).getItemCount()
                    //getHeaderView(newColumn).findViewById(R.id.item_count).text = "" + getAdapter(newColumn).getItemCount()
                }

                override fun onFocusedColumnChanged(oldColumn: Int, newColumn: Int) {
                    Toast.makeText(context, "Focused column changed from $oldColumn to $newColumn", Toast.LENGTH_SHORT).show()
                }

                override fun onColumnDragStarted(position: Int) {
                    Toast.makeText(context, "Column drag started from $position", Toast.LENGTH_SHORT).show()
                }

                override fun onColumnDragChangedPosition(oldPosition: Int, newPosition: Int) {
                    Toast.makeText(context, "Column changed from $oldPosition to $newPosition", Toast.LENGTH_SHORT).show()
                }

                override fun onColumnDragEnded(position: Int) {
                    Toast.makeText(context, "Column drag ended at $position", Toast.LENGTH_SHORT).show()
                }
            })

            setBoardCallback(object : BoardCallback {
                override fun canDragItemAtPosition(column: Int, dragPosition: Int): Boolean {
                    // Add logic here to prevent an item to be dragged
                    return true
                }

                override fun canDropItemAtPosition(oldColumn: Int, oldRow: Int, newColumn: Int, newRow: Int): Boolean {
                    // Add logic here to prevent an item to be dropped
                    return true
                }
            })
        }
    }
}