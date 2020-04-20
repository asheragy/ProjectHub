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
import org.cerion.projecthub.github.GitHubService
import org.cerion.projecthub.github.UpdateIssueParams
import org.cerion.projecthub.github.getService
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
        val isNew = args.number == 0

        if (!isNew) {
            viewModel.load(args.repoOwner!!, args.repo!!, args.number)
        }

        viewModel.finished.observe(viewLifecycleOwner, Observer {
            if (it!!) {
                // TODO need to handle keyboard
                findNavController().navigateUp()
                //requireActivity().onBackPressed()
            }
        })

        viewModel.issue.observe(viewLifecycleOwner, Observer {
            if (isNew) {
                val columnId = IssueFragmentArgs.fromBundle(requireArguments()).columnId
                mainViewModel.addIssueForColumn(columnId, it.title, it.body)
                // TODO need to handle keyboard
                findNavController().navigateUp()

            }
            else {
                viewModel.save()
            }
        })

        return binding.root
    }
}

class IssueViewModel(application: Application) : AndroidViewModel(application) {

    private val context = getApplication<Application>().applicationContext!!
    private var service: GitHubService = getService(context)

    val title = MutableLiveData<String>("")
    val comment = MutableLiveData<String>("")

    val issue = MutableLiveData<IssueCard>()
    val finished = MutableLiveData<Boolean>(false)

    private var owner: String? = null
    private var repo: String? = null
    private var number: Int? = null

    fun load(owner: String, repo: String, number: Int) {
        this.owner = owner
        this.repo = repo
        this.number = number

        viewModelScope.launch {
            val issue = service.getIssue(owner, repo, number).await()
            title.value = issue.title
            comment.value = issue.body
        }
    }

    fun save() {
        val params = UpdateIssueParams(title.value!!, comment.value!!)
        viewModelScope.launch {
            service.updateIssue(owner!!, repo!!, number!!, params).await()
            finished.value = true
        }
    }

    fun submit() {
        issue.value = IssueCard(0, "").also {
            it.title = title.value!!
            it.body = comment.value!!
        }
    }
}