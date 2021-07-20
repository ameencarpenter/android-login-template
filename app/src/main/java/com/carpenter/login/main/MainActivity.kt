package com.carpenter.login.main

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.carpenter.login.databinding.ActivityMainBinding
import com.carpenter.login.login.LoginActivity
import com.carpenter.login.utils.observe
import com.carpenter.login.utils.openActivity
import com.carpenter.login.utils.showSnackBar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val model by viewModels<MainViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (!model.isUserSignedInAndVerified()) {
            openActivity(LoginActivity::class.java, enterFromLeft = true)
        }

        binding.logOut.setOnClickListener { model.signOut() }

        observe(model.error) {
            showSnackBar(binding.logOut, it ?: return@observe) {
                model.signOut()
            }
        }

        observe(model.loading) {
            binding.progress.isVisible = it
        }

        observe(model.signedOut) {
            if (it) openActivity(LoginActivity::class.java, enterFromLeft = true)
        }
    }
}