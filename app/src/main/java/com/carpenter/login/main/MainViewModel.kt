package com.carpenter.login.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.carpenter.login.login.AuthRepository
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch

class MainViewModel(private val app: Application) : AndroidViewModel(app) {

    private val authRepository = AuthRepository.getInstance()

    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String?>(null)
    val error: LiveData<String?> = _error

    private val _signedOut = MutableLiveData(false)
    val signedOut: LiveData<Boolean> = _signedOut

    fun removeError() {
        _error.value = null
    }

    fun signOug() = viewModelScope.launch {
        try {
            _loading.postValue(true)
            authRepository.signOut(app)
            _signedOut.postValue(true)
        } catch (e: Exception) {
            _error.postValue(e.message)
        } finally {
            _loading.postValue(false)
        }
    }

    fun isUserSignedInAndVerified(): Boolean {
        val user = Firebase.auth.currentUser
        return user != null && authRepository.isUserVerified()
    }
}