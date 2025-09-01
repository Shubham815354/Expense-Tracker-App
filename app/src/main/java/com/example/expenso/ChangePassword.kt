package com.example.expenso

import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.expenso.ApiUsage.API
import com.example.expenso.ApiUsage.Constant
import com.example.expenso.ApiUsage.obj
import com.example.expenso.Data_Class.RecievePasswordUpdateRequest
import com.example.expenso.Data_Class.UpdatePassword
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ChangePassword : AppCompatActivity() {
    private lateinit var textView: TextView
    private lateinit var newPasswordEditText: EditText
    private lateinit var confirmPasswordEditText: EditText
    private lateinit var changePasswordButton: Button
    private var apiCall: Call<RecievePasswordUpdateRequest>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_password)

        initializeViews()
        setupPasswordValidation()
        setupConfirmPasswordValidation()
        setupButtonClickListener()
    }

    private fun initializeViews() {
        textView = findViewById(R.id.textView)
        newPasswordEditText = findViewById(R.id.editTextText2)
        confirmPasswordEditText = findViewById(R.id.editTextText3)
        changePasswordButton = findViewById(R.id.button)
    }

    private fun setupButtonClickListener() {
        changePasswordButton.setOnClickListener {
            val newPassword = newPasswordEditText.text.toString()
            val confirmPassword = confirmPasswordEditText.text.toString()
            val token = Constant.getSavedToken(this)
            Log.d("Token", "Retrieved Token: $token")

            if (!validatePasswords(newPassword, confirmPassword)) {
                return@setOnClickListener
            }

            if (token.isNullOrEmpty()) {
                showToast("Session expired. Please log in againn.")
                return@setOnClickListener
            }

            changePassword(token, newPassword)
        }
    }

    private fun validatePasswords(newPassword: String, confirmPassword: String): Boolean {
        if (newPassword != confirmPassword) {
            textView.setTextColor(Color.RED)
            textView.text = "Password Mismatch"
            return false
        }

        if (newPassword.length < 6) {
            textView.setTextColor(Color.RED)
            textView.text = "Password must be at least 6 characters"
            return false
        }

        return true
    }

    private fun changePassword(token: String, newPassword: String) {
        Log.d("API", "Authorization Header: Bearer $token")
        val passwordUpdate = UpdatePassword(newPassword)
        val retrofit = obj.createService(API::class.java)

        apiCall = retrofit.update_password(token, passwordUpdate)
        apiCall?.enqueue(object : Callback<RecievePasswordUpdateRequest> {
            override fun onResponse(
                call: Call<RecievePasswordUpdateRequest>,
                response: Response<RecievePasswordUpdateRequest>
            ) {
                when {
                    response.isSuccessful -> {
                        Log.d("API Response", "Code: ${response.code()}, Message: ${response.message()}")
                        textView.setTextColor(Color.GREEN)
                        textView.text = "Password Changed Successfully"
                        showToast("Password updated successfully")
                    }
                    response.code() == 401 -> {
                        textView.setTextColor(Color.RED)
                        textView.text = "Session expired. Please login again."
                    }
                    else -> {
                        textView.setTextColor(Color.RED)
                        textView.text = "Error: ${response.code()} - ${response.message()}"
                    }
                }
            }

            override fun onFailure(call: Call<RecievePasswordUpdateRequest>, t: Throwable) {
                textView.setTextColor(Color.RED)
                textView.text = "Network Error: ${t.localizedMessage}"
                showToast("Failed to connect to server")
            }
        })
    }


    override fun onDestroy() {
        super.onDestroy()
        apiCall?.cancel() // Cancel ongoing API call if activity is destroyed
    }

    private fun setupPasswordValidation() {
        newPasswordEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val password = s?.toString() ?: ""
                validatePasswordStrength(password)
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun validatePasswordStrength(password: String) {
        val hasUpper = password.any { it.isUpperCase() }
        val hasLower = password.any { it.isLowerCase() }
        val hasDigit = password.any { it.isDigit() }
        val hasSpecial = password.any { !it.isLetterOrDigit() }
        val isLongEnough = password.length >= 6

        val errors = mutableListOf<String>().apply {
            if (!hasUpper) add("Uppercase")
            if (!hasLower) add("Lowercase")
            if (!hasDigit) add("Digit")
            if (!hasSpecial) add("Special Char")
            if (!isLongEnough) add("Min 6 chars")
        }

        textView.setTextColor(if (errors.isEmpty()) Color.GREEN else Color.RED)
        textView.text = if (errors.isNotEmpty()) {
            "Include: ${errors.joinToString(", ")}"
        } else {
            "Strong password"
        }
    }

    private fun setupConfirmPasswordValidation() {
        confirmPasswordEditText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus && newPasswordEditText.text.toString().trim().isEmpty()) {
                showToast("Enter password first")
                newPasswordEditText.requestFocus()
            }
        }

        confirmPasswordEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                val newPassword = newPasswordEditText.text.toString().trim()
                val confirmPassword = confirmPasswordEditText.text.toString().trim()

                if (confirmPassword.isNotEmpty()) {
                    if (newPassword != confirmPassword) {
                        textView.setTextColor(Color.RED)
                        textView.text = "Password Mismatch"
                    } else {
                        textView.setTextColor(Color.GREEN)
                        textView.text = "Passwords match"
                    }
                }
            }

            override fun afterTextChanged(p0: Editable?) {}
        })
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}