package com.githubuiviewer.ui.projectScreen.contributors

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.paging.PagingData
import androidx.recyclerview.widget.LinearLayoutManager
import com.githubuiviewer.App
import com.githubuiviewer.R
import com.githubuiviewer.databinding.ContributorsFragmentBinding
import com.githubuiviewer.datasource.api.DataLoadingException
import com.githubuiviewer.datasource.api.NetworkException
import com.githubuiviewer.datasource.api.UnauthorizedException
import com.githubuiviewer.datasource.model.UserResponse
import com.githubuiviewer.tools.FragmentArgsDelegate
import com.githubuiviewer.tools.State
import com.githubuiviewer.tools.USER_KEY
import com.githubuiviewer.tools.UserProfile
import com.githubuiviewer.ui.navigator.BaseFragment
import com.githubuiviewer.ui.projectScreen.UserAndRepoName
import com.githubuiviewer.ui.userScreen.adapter.UserAdapter
import kotlinx.coroutines.launch
import javax.inject.Inject

class ContributorsFragment : BaseFragment(R.layout.contributors_fragment) {

    override var parentContainer: ConstraintLayout? = null

    @Inject
    lateinit var viewModel: ContributorsViewModel

    private var userAndRepoName by FragmentArgsDelegate<UserAndRepoName>(USER_KEY)
    private lateinit var binding: ContributorsFragmentBinding

    private val userAdapter = UserAdapter {
        navigation.showUserScreen(UserProfile.PublicUser(it.name))
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = ContributorsFragmentBinding.inflate(inflater, container, false)
        parentContainer = binding.container
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupDi()
        setupRecyclerView()
        viewModel.getContributors(userAndRepoName)
        setupLiveDataListener()
    }

    private fun setupDi() {
        val app = requireActivity().application as App
        app.getComponent().inject(this)
    }

    private fun setupRecyclerView(){
        binding.apply {
            rvContributors.adapter = userAdapter
            rvContributors.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        }
    }

    private fun setupLiveDataListener() {
        viewModel.contributorsLiveData.observe(viewLifecycleOwner){
            when(it){
                is State.Loading -> showLoading()
                is State.Content ->  {
                    closeLoading()
                    updateContributorsRecyclerView(it.data)
                }
                is State.Error -> {
                    closeLoading()
                    when (it.error) {
                        is UnauthorizedException -> navigation.showLoginScreen()
                        is DataLoadingException -> showError(R.string.dataloading_error)
                        is NetworkException -> showError(R.string.netwotk_error)
                    }
                }
            }
        }
    }

    private fun updateContributorsRecyclerView(pagingData: PagingData<UserResponse>){
        viewModel.baseView.launch {
            userAdapter.submitData(pagingData)
        }
    }

    companion object {
        fun newInstance(userAndRepoName: UserAndRepoName) =
            ContributorsFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(USER_KEY, userAndRepoName)
                }
            }
    }
}