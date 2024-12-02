package com.dicoding.storyapp.view.auth.login

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.dicoding.storyapp.R
import com.dicoding.storyapp.databinding.ActivityLoginBinding
import com.dicoding.storyapp.model.AuthModel
import com.dicoding.storyapp.utils.ViewModelFactory
import com.dicoding.storyapp.view.home.MainActivity

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var factory: ViewModelFactory
    private val loginViewModel: LoginViewModel by viewModels { factory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupView()
        setupViewModel()
        startAnimation()
        setupAction()
        observeLoginResponse() // Observasi konsolidasi di satu tempat
    }

    private fun startAnimation() {
        ObjectAnimator.ofFloat(binding.ivLogin, View.TRANSLATION_X, -30f, 30f).apply {
            duration = 6000
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.REVERSE
        }.start()

        val title = ObjectAnimator.ofFloat(binding.tvTitle, View.ALPHA, 1f).setDuration(500)
        val message = ObjectAnimator.ofFloat(binding.tvMessage, View.ALPHA, 1f).setDuration(500)
        val email = ObjectAnimator.ofFloat(binding.tvEmail, View.ALPHA, 1f).setDuration(500)
        val emailEdit = ObjectAnimator.ofFloat(binding.tlEmail, View.ALPHA, 1f).setDuration(500)
        val password = ObjectAnimator.ofFloat(binding.tvPassword, View.ALPHA, 1f).setDuration(500)
        val passwordEdit = ObjectAnimator.ofFloat(binding.tlPassword, View.ALPHA, 1f).setDuration(500)
        val login = ObjectAnimator.ofFloat(binding.btnLogin, View.ALPHA, 1f).setDuration(500)

        AnimatorSet().apply {
            playSequentially(title, message, email, emailEdit, password, passwordEdit, login)
            startDelay = 500
        }.start()
    }

    private fun setupAction() {
        binding.btnLogin.setOnClickListener {
            val email = binding.edtEmail.text.toString()
            val password = binding.edtPassword.text.toString()

            if (email.isEmpty()) {
                binding.edtEmail.error = getString(R.string.required_field)
                return@setOnClickListener
            }

            if (password.isEmpty()) {
                binding.edtPassword.error = getString(R.string.required_field)
                return@setOnClickListener
            }

            showLoading()
            loginViewModel.postLogin(email, password) // Kirim permintaan login
        }
    }

    private fun setupViewModel() {
        factory = ViewModelFactory.getInstance(this)
    }

    private fun setupView() {
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.apply {
            title = getString(R.string.title_login)
            setDisplayHomeAsUpEnabled(true)
        }
    }

    private fun observeLoginResponse() {
        loginViewModel.loginResponse.observe(this) { response ->
            hideLoading()

            if (response.error == false) {
                // Login berhasil: simpan sesi dan navigasi ke halaman utama
                saveSession(
                    AuthModel(
                        response.loginResult?.name.toString(),
                        AUTH_KEY + response.loginResult?.token.toString(),
                        true
                    )
                )
                navigateToMain()
            } else {
                // Login gagal: tampilkan pesan error
                Toast.makeText(this, response.message ?: getString(R.string.login_failed), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun navigateToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun showLoading() {
        binding.pbLogin.visibility = View.VISIBLE
    }

    private fun hideLoading() {
        binding.pbLogin.visibility = View.GONE
    }

    private fun saveSession(session: AuthModel) {
        loginViewModel.saveSession(session)
    }

    companion object {
        private const val AUTH_KEY = "Bearer "
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return super.onSupportNavigateUp()
    }
}
