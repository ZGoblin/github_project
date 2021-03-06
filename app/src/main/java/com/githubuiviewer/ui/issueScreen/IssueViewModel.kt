package com.githubuiviewer.ui.issueScreen

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.githubuiviewer.data.repository.GitHubRepository
import com.githubuiviewer.datasource.api.DataLoadingException
import com.githubuiviewer.datasource.api.GitHubService
import com.githubuiviewer.datasource.api.NetworkException
import com.githubuiviewer.datasource.api.UnauthorizedException
import com.githubuiviewer.datasource.model.IssueCommentRepos
import com.githubuiviewer.datasource.model.ReactionContent
import com.githubuiviewer.tools.Emoji
import com.githubuiviewer.tools.PER_PAGE
import com.githubuiviewer.tools.State
import com.githubuiviewer.ui.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import com.githubuiviewer.tools.PagingDataSource
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.IOException
import javax.inject.Inject

class IssueViewModel @Inject constructor(
    private val gitHubService: GitHubService,
    private val gitHubRepository: GitHubRepository
) : BaseViewModel() {

    val baseScope = baseViewModelScope
    lateinit var issuesDetailsParameter: IssuesDetailsParameter

    private val _commentLiveData = MutableLiveData<State<PagingData<IssueCommentRepos>, IOException>>()
    val commentLiveData
        get() = _commentLiveData as LiveData<State<PagingData<IssueCommentRepos>, IOException>>

    fun getContent() {
        baseViewModelScope.launch {
            commentsFlow().collectLatest { pagedData ->
                _commentLiveData.postValue(State.Content(pagedData))
            }
        }
    }

    fun createReaction(reaction: Emoji, issueCommentRepos: IssueCommentRepos) {
        baseViewModelScope.launch(Dispatchers.IO) {
            if (issueCommentRepos.id == issuesDetailsParameter.issue_number) {
                gitHubService.createReactionForIssue(
                    owner = issuesDetailsParameter.owner,
                    repo = issuesDetailsParameter.repo,
                    issue_number = issuesDetailsParameter.issue_number,
                    content = ReactionContent(reaction.githubReaction)
                )
            } else {
                gitHubService.createReactionForIssueComment(
                    owner = issuesDetailsParameter.owner,
                    repo = issuesDetailsParameter.repo,
                    comment_id = issueCommentRepos.id,
                    content = ReactionContent(reaction.githubReaction)
                )
            }
            getContent()
        }
    }

    private suspend fun commentsFlow(): Flow<PagingData<IssueCommentRepos>> {
        return Pager(PagingConfig(PER_PAGE)) {
            PagingDataSource(baseViewModelScope) { currentPage ->
                gitHubRepository.getIssueComment(issuesDetailsParameter, currentPage)
            }
        }.flow.cachedIn(baseViewModelScope)
    }

    override fun unauthorizedException() {
        super.unauthorizedException()
        _commentLiveData.postValue(State.Error(UnauthorizedException()))
    }

    override fun networkException() {
        super.networkException()
        _commentLiveData.postValue(State.Error(NetworkException()))
    }

    override fun dataLoadingException() {
        super.dataLoadingException()
        _commentLiveData.postValue(State.Error(DataLoadingException()))
    }
}