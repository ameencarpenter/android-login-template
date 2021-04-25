package com.carpenter.login.phoneLoginScreen

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.carpenter.login.*
import com.carpenter.login.databinding.FragmentPhoneLoginBinding
import com.carpenter.login.login.addCountryCode
import com.carpenter.login.login.onTextChange
import com.carpenter.login.login.value
import com.carpenter.login.main.MainActivity
import com.carpenter.login.utils.*
import com.google.firebase.FirebaseException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.TimeUnit

@AndroidEntryPoint
class PhoneLoginFragment : Fragment(), View.OnClickListener {

    private var _binding: FragmentPhoneLoginBinding? = null
    private val binding get() = _binding!!
    private val model by viewModels<PhoneLoginViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPhoneLoginBinding.inflate(layoutInflater, container, false)

        onBackClicked {
            if (model.loading.value!! || model.page.value!! == PhoneLoginPage.VERIFY) {
                showCancelConfirmationDialog()
            } else goBack()
        }

        binding.phoneNumber.onTextChange { model.removePhoneNumberError() }
        binding.verificationCode.onTextChange { model.removeVerificationCodeError() }

        binding.phoneNumber.setOnDoneClick { sendCode() }
        binding.verificationCode.setOnDoneClick { model.verify(binding.verificationCode.value) }

        getResult<Boolean>(IS_CANCELED) {
            if (!it) return@getResult
            model.onLoginCanceled()
            //if user is in verification page, clear phone number field.
            binding.phoneNumber.setText("")
        }

        getResult<Boolean>(SEND_CODE) { if (it) sendCodeAgain() }

        observe(model.signedIn) { if (it) openActivity(MainActivity::class.java) }

        observe(model.loading) {
            binding.root.enableViews(!it)
            binding.loginProgress.isVisible = it
            binding.verificationProgress.isVisible = it
        }

        observe(model.error) {
            showSnackBar(it ?: return@observe) {
                when (model.page.value) {
                    PhoneLoginPage.LOGIN -> sendCode()
                    PhoneLoginPage.VERIFY -> model.verify(binding.verificationCode.value)
                }
            }
            model.removeError()
        }

        observe(model.phoneNumberError) { binding.phoneNumberContainer.error = it }

        observe(model.verificationCodeError) { binding.verificationCodeContainer.error = it }

        observe(model.page) {
            val shownPage =
                if (binding.login.isVisible) PhoneLoginPage.LOGIN else PhoneLoginPage.VERIFY

            if (it == shownPage) return@observe

            when (it ?: return@observe) {
                PhoneLoginPage.LOGIN -> crossFade(binding.login, binding.verification) {
                    openKeyboard(binding.phoneNumber)
                }
                PhoneLoginPage.VERIFY -> crossFade(binding.verification, binding.login) {
                    openKeyboard(binding.verificationCode)
                }
            }
        }

        observe(model.phoneNumber) {
            val text = getString(R.string.code_sent_to_number, it ?: return@observe)
            binding.toVerifyNumber.text = text
        }

        binding.loginSubmit.setOnClickListener(this)
        binding.verificationSubmit.setOnClickListener(this)
        binding.sendAgain.setOnClickListener(this)

        return binding.root
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            binding.loginSubmit.id -> sendCode()
            binding.verificationSubmit.id -> model.verify(binding.verificationCode.value)
            binding.sendAgain.id -> showSendAgainConfirmationDialog()
        }
    }

    private fun showCancelConfirmationDialog() {
        goTo(
            PhoneLoginFragmentDirections.actionGlobalConfirmationDialog(
                resultKey = IS_CANCELED,
                title = getString(R.string.cancel_phone_login),
                content = null,
                positiveLabel = getString(R.string.cancel),
                negativeLabel = getString(R.string.dismiss)
            )
        )
    }

    private fun showSendAgainConfirmationDialog() {
        goTo(
            PhoneLoginFragmentDirections.actionGlobalConfirmationDialog(
                resultKey = SEND_CODE,
                title = getString(R.string.send_code_again),
                content = null,
                positiveLabel = getString(R.string.send),
                negativeLabel = getString(R.string.cancel)
            )
        )
    }

    private fun sendCode() {
        val phoneNumber = binding.phoneNumber.value
        val numberWithCode = requireContext().addCountryCode(phoneNumber)
        model.onSendCode(numberWithCode) {
            val options = PhoneAuthOptions.newBuilder(Firebase.auth)
                .setPhoneNumber(numberWithCode)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(requireActivity())
                .setCallbacks(callbacks)
                .build()
            PhoneAuthProvider.verifyPhoneNumber(options)
        }
    }

    private fun sendCodeAgain() {
        model.onSendCodeAgain {
            val options = PhoneAuthOptions.newBuilder(Firebase.auth)
                .setPhoneNumber(model.phoneNumber.value!!)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(requireActivity())
                .setCallbacks(callbacks)
                .build()
            PhoneAuthProvider.verifyPhoneNumber(options)
        }
    }

    private val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            model.onVerificationCompleted(credential)
        }

        override fun onVerificationFailed(e: FirebaseException) {
            model.onVerificationFailed(e)
        }

        override fun onCodeSent(
            verificationId: String,
            token: PhoneAuthProvider.ForceResendingToken
        ) {
            model.onCodeSent(verificationId)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    companion object {
        private const val IS_CANCELED = "IS_CANCELED"
        private const val SEND_CODE = "SEND_CODE"
    }

}