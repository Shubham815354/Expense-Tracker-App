package com.example.expenso

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.text.method.DigitsKeyListener
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.expenso.ApiUsage.API
import com.example.expenso.ApiUsage.obj
import com.example.expenso.Data_Class.Resend_Otp
import com.example.expenso.Data_Class.VerifyOtp
import com.example.expenso.LogIn
import com.google.android.material.bottomsheet.BottomSheetDialog
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class Otp_Verification : AppCompatActivity() {
    lateinit var invalid_textview: TextView
    lateinit var mail_text_view: TextView
    lateinit var otp1: EditText
    lateinit var otp2: EditText
    lateinit var otp3: EditText
    lateinit var otp4: EditText
    lateinit var verify_button: Button
    lateinit var invalid_Otp_view: TextView
    lateinit var resend_code: TextView
    lateinit var gifImageView: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_otp_verification)


        show_optp_popup()
    }

    fun show_optp_popup(){
        val dialog = BottomSheetDialog(this)
        val view = LayoutInflater.from(this).inflate(R.layout.otp_popup, null)
        dialog.setContentView(view)
        dialog.show()
        dialog.setCanceledOnTouchOutside(false)

        // Prevent dismiss on back press
        dialog.setCancelable(false)
        invalid_textview = view.findViewById(R.id.invalid_Otp_view)
        mail_text_view = view.findViewById(R.id.mail_text_view)
        otp1 = view.findViewById(R.id.otp1)
        otp2 = view.findViewById(R.id.otp2)
        otp3 = view.findViewById(R.id.otp3)
        otp4 = view.findViewById(R.id.otp4)
        verify_button = view.findViewById(R.id.verify_button)
        invalid_Otp_view = view.findViewById(R.id.invalid_Otp_view)
        resend_code = view.findViewById(R.id.resend_code)
        gifImageView = view.findViewById(R.id.gifImageView)
        Glide.with(this)
            .asGif()
            .load(R.drawable.email_success)
            .into(gifImageView)

        invalid_textview.text = "Please enter the code we just sent to email"
        val userEmail = intent.getStringExtra("mail_ID")
        mail_text_view.text = "$userEmail"

        resend_code.setOnClickListener {
            resendOtp()
        }

        setOtpInputFilters()
        setOtpAutoMove()

        verify_button.setOnClickListener {
            val userEmailId = mail_text_view.text.toString()
            val otpSent = setupOtpVerifyValidation()

            val retrofit = obj.createService(API::class.java)
            retrofit.verify_otp(userEmailId, otpSent).enqueue(object : Callback<VerifyOtp> {
                override fun onResponse(call: Call<VerifyOtp?>, response: Response<VerifyOtp?>) {
                    when {
                        response.isSuccessful -> {
                            val sharedprf= getSharedPreferences("ExpensoPref",MODE_PRIVATE)
                            val editor= sharedprf.edit()
                            editor.putString("Email_ID_User",userEmailId)
                            editor.apply()
                            val intent = Intent(this@Otp_Verification, LogIn::class.java)
                            Toast.makeText(this@Otp_Verification, "Otp Verified successfully", Toast.LENGTH_SHORT).show()
                            startActivity(intent)
                            finish()
                        }
                        response.code() == 400 -> {
                            invalid_Otp_view.setTextColor(Color.RED)
                            invalid_Otp_view.text="Invalid OTP"
                        }
                        else -> {
                            Toast.makeText(this@Otp_Verification, "Login failed: ${response.code()}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                override fun onFailure(call: Call<VerifyOtp?>, t: Throwable) {
                    Toast.makeText(this@Otp_Verification, "Network error: ${t.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    private fun resendOtp(){
        val userEmailId = mail_text_view.text.toString()
        val retrofit= obj.createService(API::class.java)
        retrofit.resend_otp(userEmailId).enqueue(object : Callback<Resend_Otp>{
            override fun onResponse(call: Call<Resend_Otp?>, response: Response<Resend_Otp?>) {
                if(response.isSuccessful){
                    invalid_Otp_view.setTextColor(Color.GREEN)
                    invalid_Otp_view.text="New Otp Send"
                }else if (response.code() == 404){
                    invalid_Otp_view.setTextColor(Color.RED)
                    invalid_Otp_view.text="Invalid OTP"
                }else{
                    Toast.makeText(this@Otp_Verification,"Resend Otp Failed",Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(
                call: Call<Resend_Otp?>,
                t: Throwable
            ) {
                Toast.makeText(this@Otp_Verification,t.localizedMessage,Toast.LENGTH_SHORT).show()
            }

        })
    }

    private fun setOtpInputFilters() {
        val filter = InputFilter.LengthFilter(1)
        val digitsOnly = DigitsKeyListener.getInstance("0123456789")

        listOf(otp1, otp2, otp3, otp4).forEach { otpField ->
            otpField.filters = arrayOf(filter)
            otpField.keyListener = digitsOnly
        }
    }

    private fun setOtpAutoMove() {
        moveToNextOnInput(otp1, otp2)
        moveToNextOnInput(otp2, otp3)
        moveToNextOnInput(otp3, otp4)
    }

    private fun moveToNextOnInput(current: EditText, next: EditText) {
        current.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (s?.length == 1) next.requestFocus()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    fun setupOtpVerifyValidation(): String {
        return otp1.text.toString().trim() +
                otp2.text.toString().trim() +
                otp3.text.toString().trim() +
                otp4.text.toString().trim()
    }
}
