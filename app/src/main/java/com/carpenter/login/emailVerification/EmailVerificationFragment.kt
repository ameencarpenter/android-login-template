package com.carpenter.login.emailVerification

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.carpenter.login.*
import com.carpenter.login.databinding.FragmentEmailVerificationBinding
import com.carpenter.login.main.MainActivity
import com.carpenter.login.utils.*

class EmailVerificationFragment : Fragment(), View.OnClickListener {

    private var _binding: FragmentEmailVerificationBinding? = null
    private val binding get() = _binding!!
    private val model by viewModels<EmailVerificationViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEmailVerificationBinding.inflate(layoutInflater, container, false)

        observe(model.email) { binding.email.text = it }

        observe(model.error) {
            showSnackBar(it ?: return@observe) { model.verify() }
            model.removeError()
        }

        observe(model.loading) {
            binding.root.enableViews(!it)
            binding.progress.isVisible = it
        }

        observe(model.verified) { if (it) openActivity(MainActivity::class.java) }

        observe(model.signedOut) {
            if (it) goTo(EmailVerificationFragmentDirections.actionEmailVerificationFragmentToLoginFragment())
        }

        observe(model.emailSentAgain) {
            if (!it) return@observe
            showSnackBar(getString(R.string.verification_email_sent_successfully))
            model.onEmailSentHandled()
        }

        getResult<Boolean>(IS_CANCELED) { if (it) model.signOut() }

        getResult<Boolean>(SEND_AGAIN) { if (it) model.sendAgain() }

        binding.verify.setOnClickListener(this)
        binding.sendAgain.setOnClickListener(this)
        binding.cancel.setOnClickListener(this)

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        model.verify()
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            binding.verify.id -> model.verify()
            binding.sendAgain.id -> {
                goTo(
                    EmailVerificationFragmentDirections.actionGlobalConfirmationDialog(
                        getString(R.string.send_email_again),
                        content = getString(R.string.too_many_requests_may_result_in_your_account_being_unable_to_send_requests_for_a_while),
                        positiveLabel = getString(R.string.send),
                        negativeLabel = getString(R.string.cancel),
                        resultKey = SEND_AGAIN
                    )
                )
            }
            binding.cancel.id -> {
                goTo(
                    EmailVerificationFragmentDirections.actionGlobalConfirmationDialog(
                        getString(R.string.cancel_login),
                        content = null,
                        positiveLabel = getString(R.string.cancel),
                        negativeLabel = getString(R.string.dismiss),
                        resultKey = IS_CANCELED
                    )
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
        private const val SEND_AGAIN = "SEND_AGAIN"
    }
}