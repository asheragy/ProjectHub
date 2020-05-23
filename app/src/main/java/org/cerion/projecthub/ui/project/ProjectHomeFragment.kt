package org.cerion.projecthub.ui.project

import android.os.Bundle
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.marginLeft
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.navArgs
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import org.cerion.projecthub.R
import org.cerion.projecthub.databinding.FragmentProjectHomeBinding
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

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
        })

        binding.fabGroup.add("Note") {
            currentColumn?.addNote()
        }

        binding.fabGroup.add("Issue") {
            currentColumn?.addIssue()
        }

        return binding.root
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
