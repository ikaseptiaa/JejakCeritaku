package com.example.jejakceritaku.view.register

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import com.example.jejakceritaku.databinding.ActivitySignupBinding
import com.example.jejakceritaku.view.ViewModelFactory
import com.example.jejakceritaku.view.login.LoginActivity
import com.example.jejakceritaku.viewmodel.RegisterViewModel
import com.example.jejakceritaku.data.pref.Result

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignupBinding
    private lateinit var myButton: Button
    private lateinit var passwordEditText: EditText
    private lateinit var emailEditText: EditText
    private val viewModel: RegisterViewModel by viewModels { ViewModelFactory.getInstance(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        emailEditText = binding.emailEditText
        passwordEditText = binding.passwordEditText
        myButton = binding.signupButton

        emailEditText.addTextChangedListener(createTextWatcher())
        passwordEditText.addTextChangedListener(createTextWatcher())

        setupView()
        playAnimation()
        myButton.setOnClickListener { register() }
        setButtons()
    }

    private fun register() {
        val name = binding.nameEditText.text.toString()
        val email = binding.emailEditText.text.toString()
        val password = binding.passwordEditText.text.toString()

        viewModel.register(name, email, password).observe(this) { result ->
            when (result) {
                is Result.Loading -> showLoading(true)
                is Result.Success -> {
                    showToast(result.data)
                    showLoading(false)
                    setupAction(email)
                }
                is Result.Error -> {
                    showToast(result.error)
                    showLoading(false)
                }
            }
        }
    }

    private fun createTextWatcher() = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            setButtons()
        }
        override fun afterTextChanged(s: Editable?) {}
    }

    private fun setupView() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            @Suppress("DEPRECATION")
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
        supportActionBar?.hide()
        passwordEditText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
    }


    private fun setupAction(email: String) {
        AlertDialog.Builder(this).apply {
            setTitle("Congratulations!!")
            setMessage("Your account is registered.")
            setPositiveButton("Login") { _, _ ->
                val intent = Intent(this@RegisterActivity, LoginActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                }
                startActivity(intent)
                finish()
            }
            create()
            show()
        }
    }

    private fun playAnimation() {
        ObjectAnimator.ofFloat(binding.imageView, View.TRANSLATION_X, -30f, 30f).apply {
            duration = 6000
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.REVERSE
        }.start()

        AnimatorSet().apply {
            playSequentially(
                ObjectAnimator.ofFloat(binding.titleTextView, View.ALPHA, 1f).setDuration(100),
                ObjectAnimator.ofFloat(binding.nameTextView, View.ALPHA, 1f).setDuration(100),
                ObjectAnimator.ofFloat(binding.nameEditTextLayout, View.ALPHA, 1f).setDuration(100),
                ObjectAnimator.ofFloat(binding.emailTextView, View.ALPHA, 1f).setDuration(100),
                ObjectAnimator.ofFloat(binding.emailEditTextLayout, View.ALPHA, 1f).setDuration(100),
                ObjectAnimator.ofFloat(binding.passwordTextView, View.ALPHA, 1f).setDuration(100),
                ObjectAnimator.ofFloat(binding.passwordEditTextLayout, View.ALPHA, 1f).setDuration(100),
                ObjectAnimator.ofFloat(binding.signupButton, View.ALPHA, 1f).setDuration(100)
            )
            startDelay = 100
        }.start()
    }

    private fun setButtons() {
        val emailValid = emailEditText.text.toString().isNotEmpty() && emailEditText.error == null
        val passwordValid = passwordEditText.text.toString().isNotEmpty() && passwordEditText.error == null

        myButton.isEnabled = emailValid && passwordValid
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
