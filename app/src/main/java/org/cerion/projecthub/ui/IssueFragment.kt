package org.cerion.projecthub.ui

import android.app.Application
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.*
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.launch
import org.cerion.projecthub.databinding.FragmentIssueBinding
import org.cerion.projecthub.github.*
import org.cerion.projecthub.model.IssueCard
import org.cerion.projecthub.ui.project.ProjectHomeViewModel

class IssueFragment : Fragment() {

    private val mainViewModel by lazy {
        ViewModelProviders.of(requireActivity()).get(ProjectHomeViewModel::class.java)
    }

    private lateinit var viewModel: IssueViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = FragmentIssueBinding.inflate(inflater, container, false)

        viewModel = ViewModelProviders.of(this).get(IssueViewModel::class.java)
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

        return binding.root
    }
}

class IssueViewModel(application: Application) : AndroidViewModel(application) {

    private val context = getApplication<Application>().applicationContext!!
    private var service: GitHubService = getService(context)

    val issue = MutableLiveData<IssueCard>()
    val finished = MutableLiveData<Boolean>(false)

    private var owner: String = ""
    private var repo: String = ""
    private var number: Int = 0
    private var columnId: Int = 0

    private val _busy = MutableLiveData(false)
    val busy: LiveData<Boolean>
        get() = _busy

    private val isNew: Boolean
        get() = number == 0

    fun load(columnId: Int, owner: String, repo: String, number: Int) {
        this.columnId = columnId
        this.owner = owner
        this.repo = repo
        this.number = number

        if (isNew) {
            issue.value = IssueCard(0, "")
        }
        else {
            // TODO should use repository here and get full issue object (not a simplified card)
            viewModelScope.launch {
                _busy.value = true
                service.getIssue(owner, repo, number).await().let {
                    issue.value = IssueCard(it.id, "").apply {
                        this.title = it.title
                        this.body = it.body
                    }
                }
                _busy.value = false
            }
        }
    }

    fun submit() {
        // TODO check if any changes or blanks if new record
        // TODO failure to update indicator

        val title = issue.value!!.title
        val comment = issue.value!!.body
        _busy.value = true

        viewModelScope.launch {
            try {
                if (isNew) {
                    val params = CreateIssueParams(title, comment)
                    val issue = service.createIssue(owner, repo, params).await()
                    service.createCard(columnId, CreateIssueCardParams(issue.id)).await()
                    finished.value = true
                }
                else {
                    val params = UpdateIssueParams(title, comment)
                    service.updateIssue(owner, repo, number, params).await()
                    finished.value = true
                }
            }
            finally {
                _busy.value = false
            }
        }
    }
}