package org.cerion.projecthub.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import org.cerion.projecthub.R
import org.cerion.projecthub.databinding.FragmentProjectHomeBinding
import org.cerion.projecthub.model.IssueCard
import org.cerion.projecthub.model.NoteCard


// TODO https://issuetracker.google.com/issues/111614463

class ColumnPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    private var pages = listOf<Int>()

    override fun getItemCount(): Int = pages.size
    override fun createFragment(position: Int): Fragment = ColumnFragment(pages[position])

    fun setPages(pages: List<Int>) {
        this.pages = pages
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
    }
}


class ProjectHomeFragment : Fragment() {

    private val args: ProjectHomeFragmentArgs by navArgs()
    private lateinit var viewModel: ProjectHomeViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = FragmentProjectHomeBinding.inflate(inflater, container, false)

        viewModel = ViewModelProviders.of(requireActivity()).get(ProjectHomeViewModel::class.java)
        //binding.viewModel = viewModel
        binding.lifecycleOwner = this

        val pagerAdapter = ColumnPagerAdapter(this)
        binding.viewPager.adapter = pagerAdapter
        binding.viewPager.setShowSideItems()

        binding.tabLayout.setTabGravity(TabLayout.GRAVITY_CENTER);
        binding.tabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = viewModel.columns.value!![position % 3].name
        }.attach()


        requireActivity().title = viewModel.projectName

        viewModel.columns.observe(viewLifecycleOwner, Observer { columns ->
            val ids = columns.map { it.id }
            pagerAdapter.setPages(ids)
        })

        /*
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

        viewModel.addNote.observe(viewLifecycleOwner, Observer { event ->
            event.getContentIfNotHandled()?.let { onAddNote(it.id) }
        })

        viewModel.addIssue.observe(viewLifecycleOwner, Observer { event ->
            event.getContentIfNotHandled()?.let { onAddIssue(it.id) }
        })

         */

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
