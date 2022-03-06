package org.cerion.projecthub.ui.project

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.google.android.material.chip.Chip
import org.cerion.projecthub.R
import org.cerion.projecthub.databinding.FragmentIssueBinding
import org.cerion.projecthub.ui.dialog.LabelsViewModel
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class IssueFragment : Fragment() {

    private val projectViewModel: ProjectHomeViewModel by sharedViewModel()
    private val viewModel: IssueViewModel by viewModel()
    private val labelsViewModel: LabelsViewModel by sharedViewModel()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = FragmentIssueBinding.inflate(inflater, container, false)

        val args = IssueFragmentArgs.fromBundle(requireArguments())
        viewModel.load(args.columnId, args.repoOwner, args.repo, args.number)
        val columnViewModel = projectViewModel.findColumnById(args.columnId)!!

        viewModel.finished.observe(viewLifecycleOwner, Observer {
            if (it!!) {
                columnViewModel.refresh()
                // TODO need to handle keyboard
                findNavController().navigateUp()
                //requireActivity().onBackPressed()
            }
        })

        viewModel.issue.observe(viewLifecycleOwner, Observer { issue ->
            binding.body.setText(issue.body)
            binding.title.setText(issue.title)
            binding.labelChipGroup.removeAllViews()
            issue.labels.forEach {
                val chip = LayoutInflater.from(binding.root.context).inflate(R.layout.label_chip, binding.labelChipGroup, false) as Chip
                chip.text = it.name
                chip.chipBackgroundColor = ColorStateList.valueOf(it.color)
                chip.isCheckable = false
                binding.labelChipGroup.addView(chip)
            }
        })

        viewModel.message.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.run {
                Toast.makeText(requireContext(), this, Toast.LENGTH_SHORT).show()
            }
        })

        viewModel.busy.observe(viewLifecycleOwner, Observer { busy ->
            binding.busy.visibility = if (busy == true) View.VISIBLE else View.GONE
        })

        labelsViewModel.result.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                viewModel.setLabels(it)
                labelsViewModel.onRecieveResult()
            }
        })

        binding.labelLayout.setOnClickListener {
            editLabels()
        }
        binding.submit.setOnClickListener {
            viewModel.issue.value?.apply {
                body = binding.body.text.toString()
                title = binding.title.text.toString()
            }
            viewModel.submit()
        }


        requireActivity().title = viewModel.title

        return binding.root
    }

    private fun editLabels() {
        labelsViewModel.setLabels(viewModel.issue.value!!.labels.map { it.name })
        val action = IssueFragmentDirections.actionIssueFragmentToLabelsDialogFragment()
        findNavController().navigate(action)
    }
}
