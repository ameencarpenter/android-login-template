package com.carpenter.login.accountRecoveryScreen

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.carpenter.login.*
import com.carpenter.login.databinding.FragmentAccountRecoveryBinding
import com.carpenter.login.login.onTextChange
import com.carpenter.login.login.value
import com.carpenter.login.utils.*

class AccountRecoveryFragment : Fragment(), View.OnClickListener {

    private var _binding: FragmentAccountRecoveryBinding? = null
    private val binding get() = _binding!!
    private val model by viewModels<AccountRecoveryViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAccountRecoveryBinding.inflate(layoutInflater, container, false)

        onBackClicked {
            if (model.loading.value!!) showCancelConfirmationDialog()
            else goBack()
        }

        getResult<Boolean>(IS_CANCELED) { if (it) goBack() }

        openKeyboard(binding.email)

        binding.email.onTextChange { model.removeEmailError() }

        binding.email.setOnDoneClick { model.sendPasswordResetEmail(binding.email.value) }

        observe(model.error) {
            showSnackBar(it ?: return@observe) {
                model.sendPasswordResetEmail(binding.email.value)
            }
            model.removeError()
        }

        observe(model.loading) {
            binding.root.enableViews(!it)
            binding.progress.isVisible = it
        }

        observe(model.emailSent) {
            if (!it) return@observe
            showSnackBar(getString(R.string.email_sent_successfully))
            goBack()
        }

        observe(model.emailError) { binding.emailContainer.error = it }

        binding.submit.setOnClickListener(this)

        return binding.root
    }

    private fun showCancelConfirmationDialog() {
        goTo(
            AccountRecoveryFragmentDirections.actionGlobalConfirmationDialog(
                resultKey = IS_CANCELED,
                title = getString(R.string.cancel_sending_email),
                content = null,
                positiveLabel = getString(R.string.cancel),
                negativeLabel = getString(R.string.dismiss)
            )
        )
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            binding.submit.id -> model.sendPasswordResetEmail(binding.email.value)
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