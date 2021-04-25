package com.carpenter.login.loginScreen

import android.app.Application
import androidx.lifecycle.*
import com.carpenter.login.login.*
import com.carpenter.login.utils.connectedOrThrow
import com.facebook.AccessToken
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@Suppress("SpellCheckingInspection")
@HiltViewModel
class LoginViewModel @Inject constructor(
    private val app: Application,
    private val userRepo: UserRepository
) : ViewModel() {

    //note: You have to add SHA1 for google login to work.
    //To get SHA1 for debug mode, run this: keytool -list -v -keystore C:\Users\sheri\.android\debug.keystore -alias androiddebugkey -storepass android -keypass android
    //debug keystore path example: C:\Users\sheri\.android\debug.keystore (replace "\Users\sheri\" with your username on your computer.
    //to get SHA1 for release mode, run this: keytool -list -v -alias keytstore -keystore F:\Ameen\upload-keystore.jks -alias upload
    //keystore path example: F:\Ameen\upload-keystore.jks
    //keystore alias name example: upload

    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> = _loading

    private val _signedIn = MutableLiveData(Firebase.auth.currentUser != null)
    val signedIn: LiveData<Boolean> = _signedIn

    private val _error = MutableLiveData<String?>(null)
    val error: LiveData<String?> = _error

    private val _emailError = MutableLiveData<String?>(null)
    val emailError: LiveData<String?> = _emailError

    private val _passwordError = MutableLiveData<String?>(null)
    val passwordError: LiveData<String?> = _passwordError

    fun removeEmailError() {
        _emailError.value = null
    }

    fun removePasswordError() {
        _passwordError.value = null
    }

    fun removeError() {
        _error.value = null
    }

    fun loginWithGoogle(idToken: String) = viewModelScope.launch {
        try {
            app.connectedOrThrow()
            _loading.postValue(true)
            userRepo.loginWithGoogle(idToken)
            _signedIn.postValue(true)
        } catch (e: Exception) {
            _error.postValue(e.message)
        } finally {
            _loading.postValue(false)
        }
    }

    fun onLoginWithFacebookClicked() = _loading.postValue(true)

    fun onLoginWithFacebookCanceled() = _loading.postValue(false)

    fun onLoginWithFacebookFailed(error: String) {
        _loading.postValue(false)
        _error.postValue(error)
    }

    fun loginWithFacebook(token: AccessToken) = viewModelScope.launch {
        try {
            app.connectedOrThrow()
            _loading.postValue(true)
            val credential = FacebookAuthProvider.getCredential(token.token)
            userRepo.signInWithCredential(credential)
            _signedIn.postValue(true)
        } catch (e: Exception) {
            //if the error from firebase, log out from facebook.
            userRepo.signOutFromFacebook()
            _error.postValue(e.message)
        } finally {
            _loading.postValue(false)
        }

    }

    fun signInWithEmail(email: String, password: String) = viewModelScope.launch {
        try {
            _loading.postValue(true)
            app.connectedOrThrow()
            app.validEmailOrThrow(email)
            app.validPasswordOrThrow(password)
            userRepo.signInWithEmail(email, password)
            _signedIn.postValue(true)
        } catch (e: EmailException) {
            _emailError.postValue(e.message)
        } catch (e: PasswordException) {
            _passwordError.postValue(e.message)
        } catch (e: Exception) {
            _error.postValue(e.message)
        } finally {
            _loading.postValue(false)
        }
    }

    fun onLoginWithTwitterClicked() = _loading.postValue(true)

    fun onLoginWithTwitterFailed(e: Exception) {
        _loading.postValue(false)
        _error.postValue(e.message)
    }

    fun onTwitterLoginSucceeded() {
        _signedIn.postValue(true)
        _loading.postValue(false)
    }

    fun isUserVerified() = userRepo.isUserVerified()

}