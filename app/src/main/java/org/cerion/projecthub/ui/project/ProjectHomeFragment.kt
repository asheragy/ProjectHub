package org.cerion.projecthub.ui.project

import android.graphics.Color
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.util.Pair
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.woxthebox.draglistview.BoardView
import com.woxthebox.draglistview.BoardView.BoardCallback
import com.woxthebox.draglistview.BoardView.BoardListener
import com.woxthebox.draglistview.ColumnProperties
import org.cerion.projecthub.R
import org.cerion.projecthub.databinding.FragmentProjectHomeBinding
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import java.util.*


// TODO https://issuetracker.google.com/issues/111614463

class ProjectHomeFragment : Fragment() {

    private val args: ProjectHomeFragmentArgs by navArgs()
    private val viewModel: ProjectHomeViewModel by sharedViewModel()

    private lateinit var binding: FragmentProjectHomeBinding

    private val currentColumn: ColumnViewModel?
        get() {
            val index = binding.viewPager.currentItem
            return viewModel.columns.value?.get(index)
        }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentProjectHomeBinding.inflate(inflater, container, false)

        viewModel.load(args.projectId)

        //binding.viewModel = viewModel
        //binding.lifecycleOwner = this

        viewModel.project.observe(viewLifecycleOwner, Observer {
            requireActivity().title = it.name
        })

        viewModel.columns.observe(viewLifecycleOwner, Observer { columns ->
            val ids = columns?.map { it.id } ?: emptyList()
            setupPagerWithColumns(ids)

            columns.forEach {
                addColumn(it)
            }
        })

        binding.fabGroup.add("Note") {
            currentColumn?.addNote()
        }

        binding.fabGroup.add("Issue") {
            currentColumn?.addIssue()
        }

        initBoard()

        return binding.root
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

    private fun addColumn(column: ColumnViewModel) {
        /*
        val mItemArray = ArrayList<Pair<Long, String>>()
        val addItems = 15
        for (i in 0 until addItems) {
            val id: Long = i.toLong()
            mItemArray.add(Pair(id, "Item $id"))
        }
         */

        val view = ColumnHeaderView(requireContext(), column)

        /*
        val listAdapter = ItemAdapter(mItemArray, R.layout.list_item_card_issue, R.id.root, true)
        val layoutManager = LinearLayoutManager(context)
        //val backgroundColor = ContextCompat.getColor(context, R.color.column_background)

        val columnProperties: ColumnProperties = ColumnProperties.Builder.newBuilder(listAdapter)
            .setLayoutManager(layoutManager)
            .setHasFixedItemSize(false)
            .setColumnBackgroundColor(Color.TRANSPARENT)
            //.setItemsSectionBackgroundColor(backgroundColor)
            .setHeader(header)
            .setColumnDragView(header)
            .build()

         */

        binding.board.addColumn(view.getColumnProperties())
    }

    private fun setupPagerWithColumns(ids: List<Int>) {
        val pagerAdapter = ColumnPagerAdapter(this, ids)
        binding.viewPager.adapter = pagerAdapter
        binding.viewPager.setShowSideItems()

        binding.tabLayout.tabGravity = TabLayout.GRAVITY_CENTER;
        binding.tabLayout.tabMode = TabLayout.MODE_SCROLLABLE;
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = viewModel.columns.value!![position % 3].name
        }.attach()
    }
}


class ColumnPagerAdapter(fragment: Fragment, private val columnIds: List<Int>) : FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int = columnIds.size
    override fun createFragment(position: Int): Fragment = ColumnFragment.getInstance(columnIds[position])
}


fun Int.dpToPx(displayMetrics: DisplayMetrics): Int = (this * displayMetrics.density).toInt()
fun Int.pxToDp(displayMetrics: DisplayMetrics): Int = (this / displayMetrics.density).toInt()

fun ViewPager2.setShowSideItems() {
    clipToPadding = false   // allow full width shown with padding
    clipChildren = false    // allow left/right item is not clipped
    offscreenPageLimit = 2  // make sure left/right item is rendered

    // TODO this seems to work in place of custom page transformer but need first/last page to behave differently
    //val offsetPx = 50.dpToPx(resources.displayMetrics)
    //setPadding(offsetPx, 0, offsetPx, 0)

    //val pageMarginPx =  0.dpToPx(resources.displayMetrics)
    //val marginTransformer = MarginPageTransformer(pageMarginPx)
    //setPageTransformer(marginTransformer)

    /*
    val pageMarginPx = resources.getDimensionPixelOffset(R.dimen.pageMargin)
    val offsetPx = resources.getDimensionPixelOffset(R.dimen.pagerOffset)

    setPageTransformer { page, position ->
        val viewPager = page.parent.parent as ViewPager2
        val offset = if (position > 1)
                position * -(2 * pageMarginPx)
            else
                position * -(2 * offsetPx + pageMarginPx)

        if (ViewCompat.getLayoutDirection(viewPager) == ViewCompat.LAYOUT_DIRECTION_RTL)
            page.translationX = -offset
        else
            page.translationX = offset

        //Log.d(TAG, "$position ${page.translationX}")
    }

     */
}
