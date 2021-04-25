package com.carpenter.login.emailVerification

import android.app.Application
import androidx.lifecycle.*
import com.carpenter.login.R
import com.carpenter.login.login.UserRepository
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EmailVerificationViewModel @Inject constructor(
    private val app: Application,
    private val userRepo: UserRepository
) : ViewModel() {

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

    fun onEmailSentHandled() {
        _emailSentAgain.value = false
    }

    fun verify() = viewModelScope.launch {
        try {
            _loading.postValue(true)
            userRepo.reloadUser()
            if (userRepo.isUserVerified()) {
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
            userRepo.sendEmailVerification()
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
            userRepo.signOut(app)
            _signedOut.postValue(true)
        } catch (e: Exception) {
            _error.postValue(e.message)
        } finally {
            _loading.postValue(false)
        }
    }

}