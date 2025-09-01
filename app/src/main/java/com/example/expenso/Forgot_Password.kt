package com.example.expenso

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.expenso.ApiUsage.API
import com.example.expenso.ApiUsage.obj
import com.example.expenso.Data_Class.ForgotPassword
import com.google.android.material.bottomsheet.BottomSheetDialog
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

private val delayMillis: Long = 5000 // 5 second delay

class Forgot_Password : AppCompatActivity() {

    lateinit var Enter: TextView
    lateinit var EditText_Forgot_Email: EditText
    lateinit var send: Button
    lateinit var animation: ImageView
    private var dialog: BottomSheetDialog? = null // <- dialog reference for cleanup

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)
        forgotpasswordpopup()
    }

    override fun onDestroy() {
        dialog?.dismiss() // <- dismiss dialog to avoid memory leak
        super.onDestroy()
    }

    fun forgotpasswordpopup() {
        if (isFinishing || isDestroyed) return  // Prevent showing if activity is finishing or destroyed

        dialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.forgotpopup, null)
        dialog?.setContentView(view)

        dialog?.setCanceledOnTouchOutside(false)
        dialog?.setCancelable(false)

        // Safe show
        if (!isFinishing && !isDestroyed) {
            dialog?.show()
        }

        Enter = view.findViewById(R.id.enter_forgot)
        EditText_Forgot_Email = view.findViewById(R.id.EditText_Forgot_Email)
        send = view.findViewById(R.id.send)
        animation = view.findViewById(R.id.image_forgot)

        Glide.with(this)
            .asGif()
            .load(R.drawable.forgot_animation)
            .into(animation)

        setupEmailValidation()

        send.setOnClickListener {
            val email = EditText_Forgot_Email.text.toString().trim()
            val retrofit = obj.createService(API::class.java)

            retrofit.forgot_passwod(email).enqueue(object : Callback<ForgotPassword> {
                override fun onResponse(call: Call<ForgotPassword?>, response: Response<ForgotPassword?>) {
                    when {
                        response.isSuccessful -> {
                            Enter.setTextColor(Color.GREEN)
                            Enter.text = "Reset Password Link Has been sent to Email"

                            Handler(Looper.getMainLooper()).postDelayed({
                                val intent = Intent(this@Forgot_Password, LogIn::class.java)
                                startActivity(intent)
                                finish()
                            }, delayMillis)
                        }

                        response.code() == 404 -> {
                            Enter.setTextColor(Color.RED)
                            Enter.text = "Invalid User"
                        }

                        else -> {
                            Enter.setTextColor(Color.RED)
                            Enter.text = "Error From Server"
                        }
                    }
                }

                override fun onFailure(call: Call<ForgotPassword?>, t: Throwable) {
                    Toast.makeText(this@Forgot_Password, t.localizedMessage, Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    fun setupEmailValidation() {
        EditText_Forgot_Email.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val emailInput = s.toString().trim()
                Enter.text = when {
                    emailInput.isEmpty() -> ""
                    !Patterns.EMAIL_ADDRESS.matcher(emailInput).matches() -> "Enter a valid email"
                    else -> ""
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }
}
