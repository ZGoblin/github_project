package com.githubuiviewer.ui.userScreen

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.githubuiviewer.data.repository.GitHubRepository
import com.githubuiviewer.datasource.api.DataLoadingException
import com.githubuiviewer.datasource.api.GitHubService
import com.githubuiviewer.datasource.api.NetworkException
import com.githubuiviewer.datasource.api.UnauthorizedException
import com.githubuiviewer.datasource.model.ReposResponse
import com.githubuiviewer.datasource.model.UserResponse
import com.githubuiviewer.tools.PER_PAGE
import com.githubuiviewer.tools.State
import com.githubuiviewer.tools.UserProfile
import com.githubuiviewer.ui.BaseViewModel
import com.githubuiviewer.tools.PagingDataSource
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.lang.Exception
import javax.inject.Inject

class UserViewModel @Inject constructor(
    private val gitHubRepository: GitHubRepository,
    private val gitHubService: GitHubService
) : BaseViewModel() {
    lateinit var userProfile: UserProfile

    private val _userInfoLiveData = MutableLiveData<State<UserResponse, Exception>>()
    val userInfoLiveData: LiveData<State<UserResponse, Exception>> = _userInfoLiveData

    private val _reposLiveData = MutableLiveData<PagingData<ReposResponse>>()
    val reposLiveData: LiveData<PagingData<ReposResponse>> = _reposLiveData

    private val _reposLiveDataCustom = MutableLiveData<List<ReposResponse>>()
    val reposLiveDataCustom: LiveData<List<ReposResponse>> = _reposLiveDataCustom

    private val _searchLiveData = MutableLiveData<PagingData<UserResponse>>()
    val searchLiveData: LiveData<PagingData<UserResponse>> = _searchLiveData

    val baseScope = baseViewModelScope
    lateinit var currentUserName: String

    private var curPage = 1

    fun getContent() {
        _userInfoLiveData.value = State.Loading
        viewModelScope.launch {
//            while (true) {
//                delay(1000)
                Log.d("ErrorFlow", "error.msg")
//            }
        }
        baseViewModelScope.launch {
            launch {
                while (true) {
                    delay(1000)
                Log.d("ErrorFlow", "error.msg")
            }
            }
            val user = gitHubRepository.getUser(userProfile)
            currentUserName = user.name
            _userInfoLiveData.postValue(State.Content(user))
            loadMoreRepos()
//            reposFlow().collectLatest { pagedData ->
//                _reposLiveData.postValue(pagedData)
//            }
        }
    }

    private suspend fun reposFlow(): Flow<PagingData<ReposResponse>> {
        return Pager(PagingConfig(PER_PAGE)) {
            PagingDataSource(baseViewModelScope) { currentPage ->
                gitHubRepository.getRepos(userProfile, currentPage)
            }
        }.flow.cachedIn(baseViewModelScope)
    }

    fun getSearchable(query: String) {
        baseViewModelScope.launch {
            searchFlow(query).collectLatest { pagedData ->
                _searchLiveData.postValue(pagedData)
            }
        }
    }

    private suspend fun searchFlow(query: String): Flow<PagingData<UserResponse>> {
        return Pager(PagingConfig(PER_PAGE)) {
            PagingDataSource(baseViewModelScope) { currentPage ->
                gitHubService.getSearcher(query, PER_PAGE, currentPage).items
            }
        }.flow.cachedIn(baseViewModelScope)
    }

    override fun unauthorizedException() {
        super.unauthorizedException()
        _userInfoLiveData.postValue(State.Error(UnauthorizedException()))
    }

    override fun networkException() {
        super.networkException()
        _userInfoLiveData.postValue(State.Error(NetworkException()))
    }

    override fun dataLoadingException() {
        super.dataLoadingException()
        _userInfoLiveData.postValue(State.Error(DataLoadingException()))
    }

    fun loadMoreRepos() {
        baseViewModelScope.launch {
            val additionalList = gitHubRepository.getRepos(userProfile, curPage++)
            val currList = _reposLiveDataCustom.value
            currList?.let {
                _reposLiveDataCustom.postValue(currList + additionalList)
                return@launch
            }
            _reposLiveDataCustom.postValue(additionalList)
        }
    }

    override fun onCleared() {
        super.onCleared()
        Log.d("ErrorFlow", "canceled")
    }
}