package com.carpenter.login.utils

import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import com.carpenter.login.R
import com.google.android.material.snackbar.Snackbar

fun <T> Fragment.sendResult(key: String, value: T) {
    findNavController().previousBackStackEntry?.savedStateHandle?.set(key, value)
    findNavController().navigateUp()
}

fun <T> Fragment.getResult(key: String, observer: Observer<T>) {
    val savedStateHandle = findNavController().currentBackStackEntry?.savedStateHandle ?: return
    savedStateHandle.getLiveData<T>(key).observe(viewLifecycleOwner, observer)
}

fun <T> Fragment.observe(observable: LiveData<T>, observer: Observer<T>) {
    observable.observe(viewLifecycleOwner, observer)
}

fun Fragment.goTo(action: NavDirections) = findNavController().navigate(action)

fun Fragment.goBack() = findNavController().navigateUp()

fun Fragment.showSnackBar(
    message: String,
    action: String? = getString(R.string.retry),
    onAction: () -> Unit
) {
    Snackbar.make(requireView(), message, Snackbar.LENGTH_LONG).setAction(action) {
        onAction()
    }.show()
}

fun Fragment.showSnackBar(message: String) {
    Snackbar.make(requireView(), message, Snackbar.LENGTH_LONG).show()
}

fun <T> Fragment.openActivity(
    activity: Class<T>,
    finishCurrent: Boolean = true,
    enterFromLeft: Boolean = false
) = requireActivity().openActivity(activity, finishCurrent, enterFromLeft)

fun Fragment.openKeyboard(view: View) {
    if (view.requestFocus()) {
        val imm =
            requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
    }
}

fun Fragment.onBackClicked(onClick: () -> Unit) {
    requireActivity().onBackPressedDispatcher.addCallback(this) {
        onClick()
    }
}