package com.carpenter.login.main

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.carpenter.login.login.UserRepository
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val app: Application,
    private val userRepo: UserRepository
) : ViewModel() {

    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String?>(null)
    val error: LiveData<String?> = _error

    private val _signedOut = MutableLiveData(false)
    val signedOut: LiveData<Boolean> = _signedOut

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

    fun isUserSignedInAndVerified(): Boolean {
        val user = Firebase.auth.currentUser
        return user != null && userRepo.isUserVerified()
    }
}