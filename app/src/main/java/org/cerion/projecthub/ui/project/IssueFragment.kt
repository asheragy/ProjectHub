package org.cerion.projecthub.ui.project

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.chip.Chip
import org.cerion.projecthub.R
import org.cerion.projecthub.databinding.FragmentIssueBinding
import org.cerion.projecthub.model.Label
import org.cerion.projecthub.ui.dialog.LabelsViewModel
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class IssueFragment : Fragment() {

    private val projectViewModel: ProjectHomeViewModel by sharedViewModel()
    private val viewModel: IssueViewModel by viewModel()
    private val labelsViewModel: LabelsViewModel by sharedViewModel()
    private lateinit var binding: FragmentIssueBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentIssueBinding.inflate(inflater, container, false)

        val args = IssueFragmentArgs.fromBundle(requireArguments())
        viewModel.load(args.columnId, args.repoOwner, args.repo, args.number)
        val columnViewModel = projectViewModel.findColumnById(args.columnId)!!

        viewModel.finished.observe(viewLifecycleOwner) {
            if (it!!) {
                // TODO this needs to refresh ALL cards in project
                //columnViewModel.refresh()

                // TODO need to handle keyboard
                findNavController().navigateUp()
                //requireActivity().onBackPressed()
            }
        }

        viewModel.issue.observe(viewLifecycleOwner) { issue ->
            binding.body.setText(issue.body)
            binding.title.setText(issue.title)
            setLabels(issue.labels)
        }

        viewModel.labelsChanged.observe(viewLifecycleOwner) {
            it?.getContentIfNotHandled()?.run {
                setLabels(this)
            }
        }

        viewModel.message.observe(viewLifecycleOwner) {
            it?.getContentIfNotHandled()?.run {
                Toast.makeText(requireContext(), this, Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.busy.observe(viewLifecycleOwner) { busy ->
            binding.busy.visibility = if (busy == true) View.VISIBLE else View.GONE
        }

        labelsViewModel.result.observe(viewLifecycleOwner) {
            if (it != null) {
                viewModel.setLabels(it)
                labelsViewModel.onRecieveResult()
            }
        }

        binding.labelLayout.setOnClickListener {
            editLabels()
        }

        requireActivity().title = viewModel.title
        setHasOptionsMenu(true)

        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.edit_issue, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.save -> {
                viewModel.issue.value?.apply {
                    body = binding.body.text.toString()
                    title = binding.title.text.toString()
                }
                viewModel.submit()
            }
        }

        return super.onOptionsItemSelected(item)
    }

    private fun setLabels(labels: List<Label>) {
        binding.labelChipGroup.removeAllViews()
        labels.forEach {
            val chip = LayoutInflater.from(binding.root.context).inflate(R.layout.label_chip, binding.labelChipGroup, false) as Chip
            chip.text = it.name
            chip.chipBackgroundColor = ColorStateList.valueOf(it.color)
            chip.isCheckable = false
            binding.labelChipGroup.addView(chip)
        }
    }

    private fun editLabels() {
        labelsViewModel.setLabels(viewModel.issue.value!!.labels.map { it.name })
        val action = IssueFragmentDirections.actionIssueFragmentToLabelsDialogFragment()
        findNavController().navigate(action)
    }
}
