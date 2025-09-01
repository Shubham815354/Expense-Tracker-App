package com.example.expenso

import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.expenso.ApiUsage.API
import com.example.expenso.ApiUsage.Constant
import com.example.expenso.ApiUsage.obj
import com.example.expenso.Data_Class.RecievePasswordUpdateRequest
import com.example.expenso.Data_Class.UpdatePassword
import com.google.android.material.bottomsheet.BottomSheetDialog
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class Create_New_Password : AppCompatActivity() {
    lateinit var pass: EditText
    lateinit var rePass: EditText
    lateinit var eye: ImageView
    lateinit var save: Button
    lateinit var reEye: ImageView
     var isPasswordVisible=false
    private var apiCall: Call<RecievePasswordUpdateRequest>? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_new_password)
        createpopup()

    }
    fun createpopup(){
        val dialog = BottomSheetDialog(this)
        val view = LayoutInflater.from(this).inflate(R.layout.create_new_password_popup,null)
        dialog.setContentView(view)
        dialog.show()
        dialog.setCanceledOnTouchOutside(false)
        dialog.setCancelable(false)
        pass = view.findViewById(R.id.EditText_Password)
        rePass = view.findViewById(R.id.EditText_Password_match)
        eye = view.findViewById(R.id.eyeImageView)
        save = view.findViewById(R.id.save_popup)
        reEye = view.findViewById(R.id.eyeImageView_match)

        eye.setOnClickListener {
            isPasswordVisible = !isPasswordVisible
            if (isPasswordVisible) {
                pass.inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                eye.setImageResource(R.drawable.ic_eye_open)
            } else {
                pass.inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
                eye.setImageResource(R.drawable.ic_eye_closed)
            }
            pass.setSelection(pass.text.length)
        }
        reEye.setOnClickListener {
            isPasswordVisible = !isPasswordVisible
            if (isPasswordVisible) {
                rePass.inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                reEye.setImageResource(R.drawable.ic_eye_open)
            } else {
                rePass.inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
                reEye.setImageResource(R.drawable.ic_eye_closed)
            }
            rePass.setSelection(rePass.text.length)
        }
        setupPasswordValidation()
        setupConfirmPasswordValidation()
        save.setOnClickListener {
            val newPassword = pass.text.toString()
            val confirmPassword = rePass.text.toString()
            val token = Constant.getSavedToken(this)
            Log.d("Token", "Retrieved Token: $token")

            if (!validatePasswords(newPassword, confirmPassword)) {
                return@setOnClickListener
            }

            if (token.isNullOrEmpty()) {
                Toast.makeText(this@Create_New_Password,"Session expired. Please log in again.",Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            changePassword(token, newPassword)
        }
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
                        Toast.makeText(this@Create_New_Password,"Password updated successfully",Toast.LENGTH_SHORT).show()
                    }
                    response.code() == 401 -> {
                        Toast.makeText(this@Create_New_Password,"Session expired. Please login again.",Toast.LENGTH_SHORT).show()
                    }
                    else -> {
                        Toast.makeText(this@Create_New_Password, "Error: ${response.code()} - ${response.message()}",Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onFailure(call: Call<RecievePasswordUpdateRequest>, t: Throwable) {
                Toast.makeText(this@Create_New_Password, "Failed to connect to server",Toast.LENGTH_SHORT).show()
            }
        })
    }


    override fun onDestroy() {
        super.onDestroy()
        apiCall?.cancel() // Cancel ongoing API call if activity is destroyed
    }
    private fun validatePasswords(newPassword: String, confirmPassword: String): Boolean {
        if (newPassword != confirmPassword) {
            Toast.makeText(this@Create_New_Password,"Password Mismatch",Toast.LENGTH_SHORT).show()
            return false
        }

        if (newPassword.length < 6) {
            Toast.makeText(this@Create_New_Password, "Password must be at least 6 characters",Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }
    private fun setupPasswordValidation() {
        pass.addTextChangedListener(object : TextWatcher {
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
         if (errors.isNotEmpty()) {
             Toast.makeText(
                 this@Create_New_Password,
                 "Include: ${errors.joinToString(", ")}",
                 Toast.LENGTH_SHORT
             ).show()

         }
    }
    private fun setupConfirmPasswordValidation() {
        rePass.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus && pass.text.toString().trim().isEmpty()) {
                Toast.makeText(this@Create_New_Password,"Enter password first",Toast.LENGTH_SHORT).show()
                pass.requestFocus()
            }
        }

        rePass.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                val newPassword = pass.text.toString().trim()
                val confirmPassword = rePass.text.toString().trim()

                if (confirmPassword.isNotEmpty()) {
                    if (newPassword != confirmPassword) {
                        Toast.makeText(this@Create_New_Password,"Password Mismatch",Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@Create_New_Password,"Passwords match",Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun afterTextChanged(p0: Editable?) {}
        })
    }
}