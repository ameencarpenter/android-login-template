package com.carpenter.login.utils

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.carpenter.login.R
import com.carpenter.login.customViews.CustomScrollView
import com.google.android.gms.tasks.Task
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.*
import kotlin.coroutines.resumeWithException

suspend fun <T> Task<T>.await(): T {
    if (isComplete) {
        val e = exception
        return if (e == null) {
            @Suppress("UNCHECKED_CAST")
            if (isCanceled) throw CancellationException("Task $this was cancelled normally.")
            else result as T
        } else {
            throw e
        }
    }

    return suspendCancellableCoroutine { cont ->
        addOnCompleteListener {
            val e = exception
            if (e == null) {
                @Suppress("UNCHECKED_CAST", "EXPERIMENTAL_API_USAGE")
                if (isCanceled) cont.cancel() else cont.resume(result as T) {}
            } else {
                cont.resumeWithException(e)
            }
        }
    }
}

private fun ConnectivityManager.isConnected(): Boolean {
    val capabilities = getNetworkCapabilities(activeNetwork) ?: return false
    val wifiConnected = capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
    val mobileDataActive = capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
    val ethernetConnected = capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
    return wifiConnected || mobileDataActive || ethernetConnected
}

private fun Context.isConnected(): Boolean {
    return (this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager).isConnected()
}

fun Context.connectedOrThrow() {
    if (!isConnected()) throw Exception(getString(R.string.you_are_offline))
}

fun ViewGroup.enableViews(enabled: Boolean) {
    //this view group
    isEnabled = enabled

    //children
    for (childIndex in 0 until childCount) {
        getChildAt(childIndex).apply {
            isEnabled = enabled
//            custom scrolling view to disable and enable scrolling while in loading state.
            if (this is CustomScrollView) isScrollable = enabled
            if (this is ViewGroup) enableViews(enabled)
        }
    }
}

fun crossFade(show: View, hide: View, duration: Long = 500, onEnd: (() -> Unit)? = null) {
    show.apply {
        // Set the content view to 0% opacity but visible, so that it is visible
        // (but fully transparent) during the animation.
        alpha = 0f
        isVisible = true

        // Animate the content view to 100% opacity, and clear any animation
        // listener set on the view.
        animate().alpha(1f).setDuration(duration).setListener(null)
    }
    // Animate the loading view to 0% opacity. After the animation ends,
    // set its visibility to GONE as an optimization step (it won't
    // participate in layout passes, etc.)
    hide.animate().alpha(0f).setDuration(duration).setListener(object : AnimatorListenerAdapter() {
        override fun onAnimationEnd(animation: Animator) {
            hide.isVisible = false
            onEnd?.let { it() }
        }
    })
}

fun <T> AppCompatActivity.observe(observable: LiveData<T>, observer: Observer<T>) {
    observable.observe(this, observer)
}

fun Activity.showSnackBar(
    view: View,
    message: String,
    action: String? = getString(R.string.retry),
    onAction: () -> Unit
) {
    Snackbar.make(view, message, Snackbar.LENGTH_LONG).setAction(action) {
        onAction()
    }.show()
}

fun EditText.setOnDoneClick(onDone: () -> Unit) {
    setOnEditorActionListener { _, actionId, _ ->
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            onDone()
            return@setOnEditorActionListener true
        }

        return@setOnEditorActionListener false
    }
}

fun <T> Activity.openActivity(
    activity: Class<T>,
    finishCurrent: Boolean = true,
    enterFromLeft: Boolean = false
) {
    startActivity(Intent(this, activity))
    if (enterFromLeft) {
        overridePendingTransition(R.anim.from_left_to_center, R.anim.from_center_to_right)
    } else {
        overridePendingTransition(R.anim.from_right_to_center, R.anim.from_center_to_left)
    }
    if (finishCurrent) finish()
}