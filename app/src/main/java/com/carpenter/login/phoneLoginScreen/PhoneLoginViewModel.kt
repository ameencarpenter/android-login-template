package com.carpenter.login.phoneLoginScreen

import android.app.Application
import androidx.lifecycle.*
import com.carpenter.login.utils.connectedOrThrow
import com.carpenter.login.login.*
import com.google.firebase.FirebaseException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PhoneLoginViewModel @Inject constructor(
    private val app: Application,
    private val userRepo: UserRepository
) : ViewModel() {

    //note: You have to enable SafetyNet for phone login to work correctly:
    //https://firebase.google.com/docs/auth/android/phone-auth#enable-app-verification

    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> = _loading

    private val _signedIn = MutableLiveData(false)
    val signedIn: LiveData<Boolean> = _signedIn

    private val _error = MutableLiveData<String?>(null)
    val error: LiveData<String?> = _error

    private val _phoneNumberError = MutableLiveData<String?>(null)
    val phoneNumberError: LiveData<String?> = _phoneNumberError

    private val _verificationCodeError = MutableLiveData<String?>(null)
    val verificationCodeError: LiveData<String?> = _verificationCodeError

    private val _page = MutableLiveData(PhoneLoginPage.LOGIN)
    val page: LiveData<PhoneLoginPage> = _page

    private val _phoneNumber = MutableLiveData<String?>(null)
    val phoneNumber: LiveData<String?> = _phoneNumber

    private var verificationId: String? = null

    fun removeError() {
        _error.value = null
    }

    fun removePhoneNumberError() {
        _phoneNumberError.value = null
    }

    fun removeVerificationCodeError() {
        _verificationCodeError.value = null
    }

    fun onSendCode(number: String, send: () -> Unit) {
        try {
            _loading.postValue(true)
            app.connectedOrThrow()
            app.validPhoneNumberOrThrow(number)
            _phoneNumber.postValue(number)
            send()
        } catch (e: PhoneNumberException) {
            _loading.postValue(false)
            _phoneNumberError.postValue(e.message)
        } catch (e: Exception) {
            _loading.postValue(false)
            _error.postValue(e.message)
        }
    }

    fun onSendCodeAgain(send: () -> Unit) {
        try {
            app.connectedOrThrow()
            send()
        } catch (e: Exception) {
            _error.postValue(e.message)
        }
    }

    fun onVerificationCompleted(credential: PhoneAuthCredential) {
        login(credential)
    }

    fun onVerificationFailed(e: FirebaseException) {
        _error.postValue(e.message)
        _loading.postValue(false)
    }

    fun onCodeSent(verificationId: String) {
        this@PhoneLoginViewModel.verificationId = verificationId
        _loading.postValue(false)
        _page.postValue(PhoneLoginPage.VERIFY)
    }

    fun verify(code: String) {
        try {
            app.connectedOrThrow()
            app.validVerificationCodeOrThrow(code)
            val credential = PhoneAuthProvider.getCredential(verificationId!!, code)
            login(credential)
        } catch (e: VerificationCodeException) {
            _verificationCodeError.postValue(e.message)
        } catch (e: Exception) {
            _error.postValue(e.localizedMessage)
        }
    }

    fun onLoginCanceled() {
        _phoneNumber.value = null
        verificationId = null
        _page.value = PhoneLoginPage.LOGIN
    }

    private fun login(credential: PhoneAuthCredential) = viewModelScope.launch {
        try {
            _loading.postValue(true)
            userRepo.signInWithCredential(credential)
            _signedIn.postValue(true)
        } catch (e: Exception) {
            _error.postValue(e.localizedMessage)
        } finally {
            _loading.postValue(false)
        }
    }

}

enum class PhoneLoginPage { LOGIN, VERIFY }