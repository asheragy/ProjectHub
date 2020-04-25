package org.cerion.projecthub.ui.project

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.navArgs
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import org.cerion.projecthub.R
import org.cerion.projecthub.databinding.FragmentProjectHomeBinding


// TODO https://issuetracker.google.com/issues/111614463

class ColumnPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
    private var pages = listOf<Int>()
    private var fragments = arrayOfNulls<ColumnFragment>(0)
    var currentPosition = -1

    // TODO maybe better way of doing this with fragment manager + tags
    val currentFragment: ColumnFragment
        get() = fragments[currentPosition]!!

    override fun getItemCount(): Int = pages.size
    override fun createFragment(position: Int): Fragment {
        fragments[position] = ColumnFragment(pages[position])
        return fragments[position]!!
    }

    fun setPages(pages: List<Int>) {
        this.pages = pages
        this.fragments = arrayOfNulls(pages.size)
        this.notifyDataSetChanged()
    }
}

fun ViewPager2.setShowSideItems() {
    clipToPadding = false
    clipChildren = false
    offscreenPageLimit = 3

    val pageMarginPx = resources.getDimensionPixelOffset(R.dimen.pageMargin)
    val offsetPx = resources.getDimensionPixelOffset(R.dimen.pagerOffset)

    setPageTransformer { page, position ->
        val viewPager = page.parent.parent as ViewPager2
        val offset = position * -(2 * offsetPx + pageMarginPx)
        if (ViewCompat.getLayoutDirection(viewPager) == ViewCompat.LAYOUT_DIRECTION_RTL)
            page.translationX = -offset
        else
            page.translationX = offset

        //Log.d(TAG, "$position ${page.translationX}")
    }

}


class ProjectHomeFragment : Fragment() {

    private val args: ProjectHomeFragmentArgs by navArgs()
    private lateinit var viewModel: ProjectHomeViewModel
    private lateinit var binding: FragmentProjectHomeBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = FragmentProjectHomeBinding.inflate(inflater, container, false)

        viewModel = ViewModelProviders.of(requireActivity()).get(ProjectHomeViewModel::class.java)
        viewModel.load(args.projectId)

        //binding.viewModel = viewModel
        binding.lifecycleOwner = this

        val pagerAdapter = ColumnPagerAdapter(this)

        binding.viewPager.adapter = pagerAdapter
        binding.viewPager.setShowSideItems()
        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                pagerAdapter.currentPosition = position
            }
        })

        binding.tabLayout.tabGravity = TabLayout.GRAVITY_CENTER;
        binding.tabLayout.tabMode = TabLayout.MODE_SCROLLABLE;
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = viewModel.columns.value!![position % 3].name
        }.attach()

        viewModel.project.observe(viewLifecycleOwner, Observer {
            requireActivity().title = it.name
        })

        viewModel.columns.observe(viewLifecycleOwner, Observer { columns ->
            val ids = columns?.map { it.id } ?: emptyList()
            pagerAdapter.setPages(ids)
        })

        /*
        viewModel.addNote.observe(viewLifecycleOwner, Observer { event ->
            event.getContentIfNotHandled()?.let { onAddNote(it.id) }
        })

        viewModel.addIssue.observe(viewLifecycleOwner, Observer { event ->
            event.getContentIfNotHandled()?.let { onAddIssue(it.id) }
        })
         */

        binding.fabGroup.add("Note") { pagerAdapter.currentFragment.onAddNote() }
        binding.fabGroup.add("Issue") { pagerAdapter.currentFragment.onAddIssue() }

        return binding.root
    }


}
