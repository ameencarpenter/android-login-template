package com.carpenter.login.emailSignUpScreen

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.carpenter.login.*
import com.carpenter.login.databinding.FragmentEmailSignUpBinding
import com.carpenter.login.login.onTextChange
import com.carpenter.login.login.value
import com.carpenter.login.main.MainActivity
import com.carpenter.login.utils.*

class EmailSignUpFragment : Fragment(), View.OnClickListener {

    private var _binding: FragmentEmailSignUpBinding? = null
    private val binding get() = _binding!!
    private val model by viewModels<EmailSignUpViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEmailSignUpBinding.inflate(layoutInflater, container, false)

        onBackClicked {
            if (model.loading.value!!) showCancelConfirmationDialog()
            else goBack()
        }

        getResult<Boolean>(IS_CANCELED) { if (it) goBack() }

        openKeyboard(binding.email)

        binding.email.onTextChange { model.removeEmailError() }
        binding.password.onTextChange { model.removePasswordError() }
        binding.confirmedPassword.onTextChange { model.removeConfirmedPasswordError() }

        binding.confirmedPassword.setOnDoneClick {
            model.signUp(
                binding.email.value,
                binding.password.value,
                binding.confirmedPassword.value
            )
        }

        observe(model.loading) {
            binding.root.enableViews(!it)
            binding.progress.isVisible = it
        }

        observe(model.signedIn) {
            if (!it) return@observe

            if (model.isUserVerified()) openActivity(MainActivity::class.java)
            else goTo(EmailSignUpFragmentDirections.actionEmailSignUpFragmentToEmailVerificationFragment())
        }

        observe(model.emailError) { binding.emailContainer.error = it }

        observe(model.passwordError) { binding.passwordContainer.error = it }

        observe(model.confirmedPasswordError) { binding.confirmedPasswordContainer.error = it }

        observe(model.error) {
            showSnackBar(it ?: return@observe) {
                model.signUp(
                    binding.email.value,
                    binding.password.value,
                    binding.confirmedPassword.value
                )
            }
            model.removeError()
        }

        binding.submit.setOnClickListener(this)

        return binding.root
    }

    private fun showCancelConfirmationDialog() {
        goTo(
            EmailSignUpFragmentDirections.actionGlobalConfirmationDialog(
                resultKey = IS_CANCELED,
                title = getString(R.string.cancel_signing_up),
                content = null,
                positiveLabel = getString(R.string.cancel),
                negativeLabel = getString(R.string.dismiss)
            )
        )
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            binding.submit.id -> {
                model.signUp(
                    binding.email.value,
                    binding.password.value,
                    binding.confirmedPassword.value
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    companion object {
        private const val IS_CANCELED = "IS_CANCELED"
    }
}