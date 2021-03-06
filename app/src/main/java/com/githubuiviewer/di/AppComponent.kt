package com.githubuiviewer.di

import android.content.Context
import com.githubuiviewer.ui.navigator.BaseFragment
import com.githubuiviewer.ui.issueScreen.IssueFragment
import com.githubuiviewer.ui.projectScreen.contributors.ContributorsFragment
import com.githubuiviewer.ui.projectScreen.issues.BriefInfoIssuesFragment
import com.githubuiviewer.ui.projectScreen.readme.ReadMeFragment
import com.githubuiviewer.ui.updateTokenFragment.UpdateTokenFragment
import com.githubuiviewer.ui.userScreen.UserFragment
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(dependencies = [], modules = [AppModule::class])
interface AppComponent {

    fun inject(fragment: UserFragment)

    fun inject(fragment: UpdateTokenFragment)

    fun inject(fragment: BaseFragment)

    fun inject(fragment: IssueFragment)

    fun inject(fragment: ReadMeFragment)
  
    fun inject(fragment: BriefInfoIssuesFragment)
  
    fun inject(fragment: ContributorsFragment)
  
}