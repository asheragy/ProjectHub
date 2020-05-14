package org.cerion.projecthub.ui.project

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.*
import androidx.navigation.fragment.findNavController
import com.google.android.material.chip.Chip
import kotlinx.coroutines.launch
import org.cerion.projecthub.R
import org.cerion.projecthub.databinding.FragmentIssueBinding
import org.cerion.projecthub.model.Issue
import org.cerion.projecthub.model.Label
import org.cerion.projecthub.repository.IssueRepository
import org.cerion.projecthub.ui.dialog.LabelsViewModel
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class IssueFragment : Fragment() {

    private val mainViewModel by lazy {
        ViewModelProviders.of(requireActivity()).get(ProjectHomeViewModel::class.java)
    }

    private val viewModel: IssueViewModel by viewModel()
    private val labelsViewModel: LabelsViewModel by sharedViewModel()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = FragmentIssueBinding.inflate(inflater, container, false)

        //viewModel = ViewModelProviders.of(this).get(IssueViewModel::class.java)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        val args = IssueFragmentArgs.fromBundle(requireArguments())
        viewModel.load(args.columnId, args.repoOwner, args.repo, args.number)
        val columnViewModel = mainViewModel.findColumnById(args.columnId)!!

        viewModel.finished.observe(viewLifecycleOwner, Observer {
            if (it!!) {
                columnViewModel.refresh()
                // TODO need to handle keyboard
                findNavController().navigateUp()
                //requireActivity().onBackPressed()
            }
        })

        viewModel.issue.observe(viewLifecycleOwner, Observer {
            binding.labelChipGroup.removeAllViews()
            it.labels.forEach {
                val chip = LayoutInflater.from(binding.root.context).inflate(R.layout.label_chip, binding.labelChipGroup, false) as Chip
                chip.text = it.name
                chip.chipBackgroundColor = ColorStateList.valueOf(it.color)
                binding.labelChipGroup.addView(chip)
            }
        })

        labelsViewModel.result.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                viewModel.setLabels(it)
            }
        })

        binding.labelLayout.setOnClickListener {
            editLabels()
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

class IssueViewModel(private val issueRepo: IssueRepository) : ViewModel() {

    val issue = MutableLiveData<Issue>()
    val finished = MutableLiveData<Boolean>(false)

    private var ownerName: String = ""
    private var repoName: String = ""
    private var number: Int = 0
    private var columnId: Int = 0

    private val _busy = MutableLiveData(false)
    val busy: LiveData<Boolean>
        get() = _busy

    private val isNew: Boolean
        get() = number == 0

    val title: String
        get() = if (isNew) "New Issue" else "Issue $number"

    fun load(columnId: Int, owner: String, repo: String, number: Int) {
        this.columnId = columnId
        this.ownerName = owner
        this.repoName = repo
        this.number = number

        if (isNew) {
            issue.value = Issue(owner, repo, 0)
        }
        else {
            launchBusy {
                issue.value = issueRepo.getByNumber(owner, repo, number)
            }
        }
    }

    fun setLabels(labels: List<Label>) {
        issue.value!!.labels.apply {
            clear()
            addAll(labels)
        }

        // TODO different way of doing this?  Need to let things know this was updated
        issue.postValue(issue.value)
    }

    fun submit() {
        // TODO check if any changes or blanks if new record
        // TODO failure to update indicator

        launchBusy {
            if (isNew) {
                issueRepo.add(issue.value!!, columnId)
                finished.value = true
            }
            else {
                issueRepo.update(issue.value!!)
                finished.value = true
            }
        }
    }

    private fun launchBusy(action: suspend () -> Unit) {
        viewModelScope.launch {
            try {
                _busy.value = true
                action()
            }
            finally {
                _busy.value = false
            }
        }
    }
}