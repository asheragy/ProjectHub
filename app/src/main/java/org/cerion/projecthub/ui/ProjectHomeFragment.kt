package org.cerion.projecthub.ui

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.cerion.projecthub.databinding.FragmentProjectHomeBinding
import org.cerion.projecthub.databinding.ListItemCardIssueBinding
import org.cerion.projecthub.databinding.ListItemCardNoteBinding
import org.cerion.projecthub.databinding.ListItemColumnBinding
import org.cerion.projecthub.github.GitHubService
import org.cerion.projecthub.github.getService
import org.cerion.projecthub.model.Card
import org.cerion.projecthub.model.Column
import org.cerion.projecthub.model.IssueCard
import org.cerion.projecthub.model.NoteCard
import org.cerion.projecthub.repository.CardRepository


class ProjectHomeFragment : Fragment() {

    private val args: ProjectHomeFragmentArgs by navArgs()
    private lateinit var viewModel: ProjectHomeViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = FragmentProjectHomeBinding.inflate(inflater, container, false)

        viewModel = ViewModelProviders.of(this).get(ProjectHomeViewModel::class.java)
        //binding.viewModel = viewModel
        binding.lifecycleOwner = this

        requireActivity().title = viewModel.projectName

        val adapter = ProjectColumnListAdapter(requireContext(), viewLifecycleOwner)
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.recyclerView.adapter = adapter
        binding.recyclerView.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.HORIZONTAL))

        viewModel.columns.observe(viewLifecycleOwner, Observer {
            adapter.setItems(it)
        })

        viewModel.load(args.projectId)

        return binding.root
    }
}

class ProjectColumnListAdapter(context: Context, private val lifecycleOwner: LifecycleOwner) : RecyclerView.Adapter<ProjectColumnListAdapter.ViewHolder>() {

    private val service: GitHubService = getService(context)
    private val repo = CardRepository(service)

    private var items = emptyList<ColumnViewModel>()
    private var width = 0

    fun setItems(items: List<Column>) {
        this.items = items.map { ColumnViewModel(repo, it) }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ListItemColumnBinding.inflate(layoutInflater, parent, false)

        if (width == 0) {
            val percent = if (parent.context.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) 0.7 else 0.3
            width = (parent.measuredWidth * percent).toInt()
        }

        binding.root.minimumWidth = width
        binding.recyclerView.adapter = ColumnCardListAdapter()
        binding.recyclerView.addItemDecoration(DividerItemDecoration(parent.context, DividerItemDecoration.VERTICAL))

        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item)
    }

    inner class ViewHolder(private val binding: ListItemColumnBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ColumnViewModel) {
            binding.viewModel = item

            val adapter = binding.recyclerView.adapter as ColumnCardListAdapter

            item.cards.observe(lifecycleOwner, Observer {
                adapter.setItems(it)
            })

            binding.executePendingBindings()
        }
    }
}


class ColumnCardListAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    companion object {
        private const val TypeNote = 0
        private const val TypeIssue = 1
    }

    private var items = emptyList<Card>()

    fun setItems(items: List<Card>) {
        this.items = items
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)

        return if (viewType == TypeNote)
            NoteViewHolder(ListItemCardNoteBinding.inflate(layoutInflater, parent, false))
        else
            IssueViewHolder(ListItemCardIssueBinding.inflate(layoutInflater, parent, false))
    }

    override fun getItemViewType(position: Int): Int = if (items[position] is NoteCard) TypeNote else TypeIssue
    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = items[position]
        if (item is NoteCard)
            (holder as NoteViewHolder).bind(item)
        else
            (holder as IssueViewHolder).bind(item as IssueCard)
    }

    inner class NoteViewHolder(private val binding: ListItemCardNoteBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: NoteCard) {
            binding.card = item
            binding.executePendingBindings()
        }
    }

    inner class IssueViewHolder(private val binding: ListItemCardIssueBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: IssueCard) {
            binding.card = item
            binding.executePendingBindings()
        }
    }
}