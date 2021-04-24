package com.carpenter.login.loginScreen

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.launch
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.carpenter.login.*
import com.carpenter.login.databinding.FragmentLoginBinding
import com.carpenter.login.login.LoginWithGoogle
import com.carpenter.login.login.onTextChange
import com.carpenter.login.login.value
import com.carpenter.login.main.MainActivity
import com.carpenter.login.utils.*
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginResult
import com.google.firebase.auth.OAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase


class LoginFragment : Fragment(), View.OnClickListener {

    //todo: bug fix; confirmation dialog doesn't return result on rotation, but after rotating back.
    //todo: handle firebase exceptions

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private val model by viewModels<LoginViewModel>()
    private val googleLoginLauncher = registerForActivityResult(LoginWithGoogle()) {
        model.loginWithGoogle(it ?: return@registerForActivityResult)
    }
    private lateinit var callbackManager: CallbackManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(layoutInflater, container, false)

        callbackManager = CallbackManager.Factory.create()
        setFacebookButton()

        binding.email.onTextChange { model.removeEmailError() }
        binding.password.onTextChange { model.removePasswordError() }
        binding.password.setOnDoneClick {
            model.signInWithEmail(binding.email.value, binding.password.value)
        }

        binding.google.setOnClickListener(this)
        binding.phone.setOnClickListener(this)
        binding.fakeFacebook.setOnClickListener(this)
        binding.facebook.setOnClickListener(this)
        binding.forgotPassword.setOnClickListener(this)
        binding.register.setOnClickListener(this)
        binding.signIn.setOnClickListener(this)
        binding.twitter.setOnClickListener(this)

        observe(model.signedIn) {
            if (!it) return@observe
            if (model.isUserVerified()) openActivity(MainActivity::class.java)
            else goTo(LoginFragmentDirections.actionLoginFragmentToEmailVerificationFragment())
        }

        observe(model.emailError) { binding.emailContainer.error = it }

        observe(model.passwordError) { binding.passwordContainer.error = it }

        observe(model.loading) {
            if (it) binding.progress.show() else binding.progress.hide()
            binding.root.enableViews(!it)
        }

        observe(model.error) {
            showSnackBar(it ?: return@observe)
            model.removeError()
        }

        return binding.root
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            binding.forgotPassword.id -> goTo(LoginFragmentDirections.actionLoginFragmentToAccountRecoveryFragment())
            binding.signIn.id -> model.signInWithEmail(binding.email.value, binding.password.value)
            binding.google.id -> googleLoginLauncher.launch()
            binding.phone.id -> goTo(LoginFragmentDirections.actionLoginFragmentToPhoneLoginFragment())
            binding.fakeFacebook.id -> {
                binding.facebook.callOnClick()
                model.onLoginWithFacebookClicked()
            }
            binding.register.id -> goTo(LoginFragmentDirections.actionLoginFragmentToEmailSignUpFragment())
            binding.twitter.id -> signInWithTwitter()
        }
    }

    private fun setFacebookButton() {
        binding.facebook.setPermissions("email", "public_profile")
        binding.facebook.registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
            override fun onSuccess(loginResult: LoginResult) {
                model.loginWithFacebook(loginResult.accessToken)
            }

            override fun onCancel() = model.onLoginWithFacebookCanceled()

            override fun onError(error: FacebookException) {
                model.onLoginWithFacebookFailed(
                    error.message ?: getString(R.string.something_went_wrong)
                )
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        @Suppress("DEPRECATION")
        super.onActivityResult(requestCode, resultCode, data)
        callbackManager.onActivityResult(requestCode, resultCode, data)
    }

    private fun signInWithTwitter() {
        val pendingResultTask = Firebase.auth.pendingAuthResult
        if (pendingResultTask != null) {
            pendingResultTask.addOnSuccessListener {
                model.onTwitterLoginSucceeded()
            }.addOnFailureListener {
                model.onLoginWithTwitterFailed(it)
            }
        } else startTwitterSignInFlow()
    }

    private fun startTwitterSignInFlow() {
        model.onLoginWithTwitterClicked()
        val provider = OAuthProvider.newBuilder("twitter.com")
        Firebase.auth.startActivityForSignInWithProvider(requireActivity(), provider.build())
            .addOnSuccessListener {
                model.onTwitterLoginSucceeded()
            }.addOnFailureListener {
                model.onLoginWithTwitterFailed(it)
            }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}