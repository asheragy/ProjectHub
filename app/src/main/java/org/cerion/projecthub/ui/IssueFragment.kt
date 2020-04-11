package org.cerion.projecthub.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import org.cerion.projecthub.databinding.FragmentIssueBinding
import org.cerion.projecthub.model.IssueCard

class IssueFragment : Fragment() {

    private val mainViewModel by lazy {
        ViewModelProviders.of(requireActivity()).get(ProjectHomeViewModel::class.java)
    }

    private lateinit var viewModel: IssueViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = FragmentIssueBinding.inflate(inflater, container, false)

        viewModel = ViewModelProviders.of(requireActivity()).get(IssueViewModel::class.java)
        binding.viewModel = viewModel

        val columnId = IssueFragmentArgs.fromBundle(requireArguments()).columnId

        viewModel.issue.observe(viewLifecycleOwner, Observer {
            mainViewModel.addIssueForColumn(columnId, it.title, it.body)

            // TODO is this correct?
            findNavController().navigateUp()
            //requireActivity().onBackPressed()
        })

        return binding.root
    }
}

class IssueViewModel : ViewModel() {

    val title = MutableLiveData<String>("")
    val comment = MutableLiveData<String>("")

    val issue = MutableLiveData<IssueCard>()

    fun submit() {
        issue.value = IssueCard(0, "").also {
            it.title = title.value!!
            it.body = comment.value!!
        }
    }
}