package com.dicoding.storyapp.view.splash

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.lifecycleScope
import com.dicoding.storyapp.databinding.ActivityWelcomeBinding
import com.dicoding.storyapp.utils.AuthPreferences
import com.dicoding.storyapp.view.auth.login.LoginActivity
import com.dicoding.storyapp.view.auth.register.RegisterActivity


class WelcomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityWelcomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupView()
        startAnimation()
        setupAction()
    }


    private fun startAnimation() {
        ObjectAnimator.ofFloat(binding.ivWelcome, View.TRANSLATION_X, -30f, 30f).apply {
            duration = 6000
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.REVERSE
        }.start()

        val login = ObjectAnimator.ofFloat(binding.btnLoginWelcome, View.ALPHA, 1f).setDuration(500)
        val signup = ObjectAnimator.ofFloat(binding.btnRegisterWelcome, View.ALPHA, 1f).setDuration(500)
        val title = ObjectAnimator.ofFloat(binding.tvTitle, View.ALPHA, 1f).setDuration(500)
        val desc = ObjectAnimator.ofFloat(binding.tvDesc, View.ALPHA, 1f).setDuration(500)

        val together = AnimatorSet().apply {
            playTogether(login, signup)
        }

        AnimatorSet().apply {
            playSequentially(title, desc, together)
            start()
        }
    }

    private fun setupAction() {
        binding.apply {
            btnLoginWelcome.setOnClickListener {
                startActivity(Intent(this@WelcomeActivity, LoginActivity::class.java))
            }
            btnRegisterWelcome.setOnClickListener {
                startActivity(Intent(this@WelcomeActivity, RegisterActivity::class.java))
            }
        }
    }

    private fun setupView() {
        binding = ActivityWelcomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}
