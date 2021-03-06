package com.githubuiviewer.ui.navigator

import androidx.constraintlayout.widget.ConstraintSet.*
import android.view.View
import android.widget.ProgressBar
import androidx.annotation.StringRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.fragment.app.Fragment
import com.afollestad.materialdialogs.MaterialDialog
import com.githubuiviewer.R
import com.githubuiviewer.tools.PROGRESS_BAR_MARGIN
import com.githubuiviewer.ui.mainActivity.NavigationActivity

abstract class BaseFragment(layoutId: Int) : Fragment(layoutId) {

    protected val navigation: Navigator by lazy {
        (requireActivity() as NavigationActivity).navigator
    }

    protected fun showError(@StringRes error: Int) {
        MaterialDialog(requireContext()).show {
            title(R.string.error_title)
            message(error)
        }
    }

    abstract var parentContainer: ConstraintLayout?

    private val progressBarId = View.generateViewId()
    private val backgroundViewId = View.generateViewId()

    fun showLoading() {
        val progressBar = ProgressBar(requireContext()).apply {
            id = progressBarId
        }

        val view = View(requireContext()).apply {
            layoutParams = ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.MATCH_PARENT,
                ConstraintLayout.LayoutParams.MATCH_PARENT
            )
            setBackgroundColor(context.getColor(R.color.purple_500))
            id = backgroundViewId
            isClickable = true
            isFocusable = true
        }


        parentContainer?.addView(view)
        parentContainer?.addView(progressBar)

        val constraintSet = ConstraintSet()
        constraintSet.connect(progressBarId, TOP, PARENT_ID, TOP, PROGRESS_BAR_MARGIN)
        constraintSet.connect(progressBarId, BOTTOM, PARENT_ID, BOTTOM, PROGRESS_BAR_MARGIN)
        constraintSet.connect(progressBarId, START, PARENT_ID, START, PROGRESS_BAR_MARGIN)
        constraintSet.connect(progressBarId, END, PARENT_ID, END, PROGRESS_BAR_MARGIN)
        constraintSet.applyTo(parentContainer)
    }

    fun closeLoading() {
        parentContainer?.getViewById(progressBarId)?.visibility = View.GONE
        parentContainer?.getViewById(backgroundViewId)?.visibility = View.GONE
    }
}