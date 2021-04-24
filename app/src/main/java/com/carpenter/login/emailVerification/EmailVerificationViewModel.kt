package com.carpenter.login.emailVerification

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.carpenter.login.R
import com.carpenter.login.login.AuthRepository
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch

class EmailVerificationViewModel(private val app: Application) : AndroidViewModel(app) {

    private val authRepo = AuthRepository.getInstance()

    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String?>(null)
    val error: LiveData<String?> = _error

    private val _email = MutableLiveData(Firebase.auth.currentUser?.email!!)
    val email: LiveData<String> = _email

    private val _verified = MutableLiveData(false)
    val verified: LiveData<Boolean> = _verified

    private val _signedOut = MutableLiveData(false)
    val signedOut: LiveData<Boolean> = _signedOut

    private val _emailSentAgain = MutableLiveData(false)
    val emailSentAgain: LiveData<Boolean> = _emailSentAgain

    fun removeError() {
        _error.value = null
    }

    fun onEmailSentHandled(){
        _emailSentAgain.value = false
    }

    fun verify() = viewModelScope.launch {
        try {
            _loading.postValue(true)
            authRepo.reloadUser()
            if (authRepo.isUserVerified()) {
                _verified.postValue(true)
            } else {
                _error.postValue(app.getString(R.string.not_verified_yet_check_the_link_sent_to_your_email))
            }
        } catch (e: Exception) {
            _error.postValue(e.message)
        } finally {
            _loading.postValue(false)
        }
    }

    fun sendAgain() = viewModelScope.launch {
        try {
            _loading.postValue(true)
            authRepo.sendEmailVerification()
            _emailSentAgain.postValue(true)
        } catch (e: Exception) {
            _error.postValue(e.message)
        } finally {
            _loading.postValue(false)
        }
    }

    fun signOut() = viewModelScope.launch {
        try {
            _loading.postValue(true)
            authRepo.signOut(app)
            _signedOut.postValue(true)
        } catch (e: Exception) {
            _error.postValue(e.message)
        } finally {
            _loading.postValue(false)
        }
    }

}