package com.githubuiviewer.ui.userScreen

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.githubuiviewer.data.repository.ProfileRepository
import com.githubuiviewer.datasource.api.GitHubService
import com.githubuiviewer.datasource.model.ReposResponse
import com.githubuiviewer.datasource.model.SearchResponse
import com.githubuiviewer.datasource.model.UserResponse
import com.githubuiviewer.tools.PER_PAGE
import com.githubuiviewer.tools.State
import com.githubuiviewer.tools.UserProfile
import com.githubuiviewer.ui.BaseViewModel
import com.githubuiviewer.ui.userScreen.adapter.PagingDataSource
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

class UserFragmentViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val gitHubService: GitHubService
) : BaseViewModel() {
    lateinit var userProfile: UserProfile

    private val _userInfoLiveData = MutableLiveData<State<UserResponse, Int>>()
    val userInfoLiveData: LiveData<State<UserResponse, Int>> = _userInfoLiveData

    private val _reposLiveData = MutableLiveData<PagingData<ReposResponse>>()
    val reposLiveData: LiveData<PagingData<ReposResponse>> = _reposLiveData

    private val _searchLiveData = MutableLiveData<SearchResponse>()
    val searchLiveData: LiveData<SearchResponse> = _searchLiveData

    val baseScope = baseViewModelScope

    private val repos = Pager(PagingConfig(PER_PAGE)) {
        PagingDataSource(baseViewModelScope) { currentPage ->
            profileRepository.getRepos(userProfile, currentPage)
        }
    }.flow.cachedIn(baseViewModelScope)

    fun getContent() {
        _userInfoLiveData.value = State.Loading
        baseViewModelScope.launch {
            _userInfoLiveData.postValue(State.Content(profileRepository.getUser(userProfile)))
            repos.collectLatest { pagedData ->
                _reposLiveData.postValue(pagedData)
            }
        }
    }

    fun getSearchable(query: String) {
        baseViewModelScope.launch {
            _searchLiveData.postValue(gitHubService.getSearcher(query))
        }
    }

    override fun unauthorizedException() {
        _userInfoLiveData.postValue(State.Unauthorized)
    }
}