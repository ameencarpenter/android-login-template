package com.carpenter.login.accountRecoveryScreen

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.carpenter.login.utils.connectedOrThrow
import com.carpenter.login.login.AuthRepository
import com.carpenter.login.login.EmailException
import com.carpenter.login.login.validEmailOrThrow
import kotlinx.coroutines.launch

class AccountRecoveryViewModel(private val app: Application) : AndroidViewModel(app) {

    private val authRepo = AuthRepository.getInstance()

    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> = _loading

    private val _emailSent = MutableLiveData(false)
    val emailSent: LiveData<Boolean> = _emailSent

    private val _emailError = MutableLiveData<String?>(null)
    val emailError: LiveData<String?> = _emailError

    private val _error = MutableLiveData<String?>(null)
    val error: LiveData<String?> = _error

    fun removeError() {
        _error.value = null
    }

    fun removeEmailError() {
        _emailError.value = null
    }

    fun sendPasswordResetEmail(email: String) = viewModelScope.launch {
        try {
            _loading.postValue(true)
            app.connectedOrThrow()
            app.validEmailOrThrow(email)
            authRepo.sendPasswordResetEmail(email)
            _emailSent.postValue(true)
        } catch (e: EmailException) {
            _emailError.postValue(e.message)
        } catch (e: Exception) {
            _error.postValue(e.message)
        } finally {
            _loading.postValue(false)
        }
    }
}