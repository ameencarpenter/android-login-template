package com.carpenter.login.confirmationDialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.navArgs
import com.carpenter.login.databinding.DialogConfirmationBinding
import com.carpenter.login.utils.sendResult

class ConfirmationDialog : DialogFragment(), View.OnClickListener {

    private val args by navArgs<ConfirmationDialogArgs>()
    private var _binding: DialogConfirmationBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogConfirmationBinding.inflate(layoutInflater, container, false)

        bindText()

        binding.positiveAction.setOnClickListener(this)
        binding.negativeAction.setOnClickListener(this)

        return binding.root
    }

    private fun bindText() {
        binding.title.text = args.title
        binding.content.isVisible = args.content != null
        binding.content.text = args.content
        binding.positiveAction.text = args.positiveLabel
        binding.negativeAction.text = args.negativeLabel
    }

    override fun onClick(v: View?) = sendResult(
        key = args.resultKey,
        v?.id == binding.positiveAction.id
    )

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}