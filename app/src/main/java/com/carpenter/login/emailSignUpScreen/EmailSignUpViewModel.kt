package com.carpenter.login.emailSignUpScreen

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.carpenter.login.utils.connectedOrThrow
import com.carpenter.login.login.*
import kotlinx.coroutines.launch

class EmailSignUpViewModel(private val app: Application) : AndroidViewModel(app) {

    private val authRepo = AuthRepository.getInstance()

    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> = _loading

    private val _signedIn = MutableLiveData(false)
    val signedIn: LiveData<Boolean> = _signedIn

    private val _emailError = MutableLiveData<String?>(null)
    val emailError: LiveData<String?> = _emailError

    private val _passwordError = MutableLiveData<String?>(null)
    val passwordError: LiveData<String?> = _passwordError

    private val _confirmedPasswordError = MutableLiveData<String?>(null)
    val confirmedPasswordError: LiveData<String?> = _confirmedPasswordError

    private val _error = MutableLiveData<String?>(null)
    val error: LiveData<String?> = _error

    fun removeEmailError() {
        _emailError.value = null
    }

    fun removePasswordError() {
        _passwordError.value = null
    }

    fun removeConfirmedPasswordError() {
        _confirmedPasswordError.value = null
    }

    fun removeError() {
        _error.value = null
    }

    fun signUp(email: String, password: String, confirmedPassword: String) = viewModelScope.launch {
        try {
            _loading.postValue(true)
            app.connectedOrThrow()
            app.validEmailOrThrow(email)
            app.validPasswordOrThrow(password)
            app.validConfirmedPasswordOrThrow(password, confirmedPassword)
            authRepo.signUpWithEmail(email, password)
            _signedIn.postValue(true)
        } catch (e: EmailException) {
            _emailError.postValue(e.message)
        } catch (e: PasswordException) {
            _passwordError.postValue(e.message)
        } catch (e: ConfirmedPasswordException) {
            _confirmedPasswordError.postValue(e.message)
        } catch (e: Exception) {
            _error.postValue(e.message)
        } finally {
            _loading.postValue(false)
        }
    }

    fun isUserVerified(): Boolean = authRepo.isUserVerified()
}