package com.example.expenso

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.Log
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.MutableLiveData
import com.example.expenso.ApiUsage.API
import com.example.expenso.ApiUsage.Constant
import com.example.expenso.ApiUsage.obj
import com.example.expenso.Data_Class.LogINData
import com.example.expenso.Data_Class.RecieveLogInData
import com.example.expenso.Data_Class.RegisterData
import com.example.expenso.view.ProfileInfoForLogIn
import com.google.android.material.bottomsheet.BottomSheetDialog
import retrofit2.Callback
import retrofit2.Call
import okhttp3.ResponseBody
import retrofit2.Response
import kotlin.toString

class LogIn : AppCompatActivity() {
    lateinit var do_not_have_account: TextView
    lateinit var loginTitle: TextView
    lateinit var EditText_Email: EditText
    lateinit var EditText_Password: EditText
    lateinit var Edit_Text_forgot_password: TextView
    lateinit var log_in: Button
    lateinit var valid_mail_view: TextView
    lateinit var valid_password_view: TextView
    lateinit var eyeImageView: ImageView
    var isPasswordVisible = false
    var profilepic:String?=null
    lateinit var checkbox_remember: CheckBox
    lateinit var EditText_first_name: EditText
    lateinit var EditText_last_name: EditText
    lateinit var EditText_mail: EditText
    lateinit var EditText_New_Password: EditText
    lateinit var valid_pass_view: TextView
    lateinit var valid_email_view: TextView
    lateinit var EditText_Confirm_Password: EditText
    lateinit var submit_button: Button
    lateinit var All_ready_have_account: TextView
    lateinit var togglePassword: ImageView
    lateinit var toggleConfirmPassword: ImageView
    lateinit var valid_last_view: TextView
    lateinit var valid_first_view: TextView
    var isPasswordVisiblee = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login)
        show_login_Popup()
    }
    fun show_login_Popup(){
        val dialog = BottomSheetDialog(this)
        val view = LayoutInflater.from(this).inflate(R.layout.login_popup, null)
        dialog.setContentView(view)
        dialog.show()
        dialog.setCanceledOnTouchOutside(false)

        // Prevent dismiss on back press
        dialog.setCancelable(false)
        loginTitle = view.findViewById(R.id.loginTitle)
        EditText_Email = view.findViewById(R.id.EditText_Email)
        EditText_Password = view.findViewById(R.id.EditText_Password)
        Edit_Text_forgot_password = view.findViewById(R.id.Edit_Text_forgot_password)
        log_in = view.findViewById(R.id.log_in)
        do_not_have_account = view.findViewById(R.id.do_not_have_account)
        valid_mail_view =view.findViewById(R.id.valid_mail_view)
        valid_password_view = view.findViewById(R.id.valid_password_view)
        eyeImageView = view.findViewById(R.id.eyeImageView)
        checkbox_remember = view.findViewById(R.id.checkbox_remember)
        checkbox_remember.visibility= View.GONE
        valid_mail_view.text=""
        valid_password_view.text=""
        eyeImageView.setOnClickListener {
            isPasswordVisible = !isPasswordVisible
            if (isPasswordVisible) {
                EditText_Password.inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                eyeImageView.setImageResource(R.drawable.ic_eye_open)
            } else {
                EditText_Password.inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
                eyeImageView.setImageResource(R.drawable.ic_eye_closed)
            }
            EditText_Password.setSelection(EditText_Password.text.length)
        }

        change_button_color()

        val sharedpref= getSharedPreferences("ExpensoPref",MODE_PRIVATE)
        val verifyemail= sharedpref.getString("Email_ID_User","")
        if (!verifyemail.isNullOrEmpty()) {
            EditText_Email.setText(verifyemail)

            // OPTIONAL: clear it after reading, so it doesn't stay forever
            val editor = sharedpref.edit()
            editor.remove("Email_ID_User")
            editor.apply()
        }else{
            setupEmailValidation()
        }
        setupPasswordValidation()
        profilepic= Constant.getProfilePic(this)
        login_credentials()


        do_not_have_account.setOnClickListener {
            dialog.dismiss()
            setupSignUp()

        }

        Edit_Text_forgot_password.setOnClickListener {
            setupforgotpassword()
        }
    }

    fun  setupSignUp(){
        val dialogs = BottomSheetDialog(this)
        val views = LayoutInflater.from(this).inflate(R.layout.signup_popup, null)
        dialogs.setContentView(views)
        dialogs.show()
        dialogs.setCanceledOnTouchOutside(false)

        // Prevent dismiss on back press
        dialogs.setCancelable(false)
        EditText_first_name = views.findViewById(R.id.EditText_first_name)
        EditText_last_name = views.findViewById(R.id.EditText_last_name)
        EditText_mail = views.findViewById(R.id.EditText_mail)
        EditText_New_Password = views.findViewById(R.id.EditText_New_Password)
        valid_pass_view = views.findViewById(R.id.valid_pass_view)
        EditText_Confirm_Password = views.findViewById(R.id.EditText_Confirm_Password)
        submit_button = views.findViewById(R.id.signup)
        valid_email_view = views.findViewById(R.id.valid_email_view)
        All_ready_have_account = views.findViewById(R.id.All_ready_have_account)
        togglePassword = views.findViewById(R.id.togglePassword)
        toggleConfirmPassword = views.findViewById(R.id.toggleConfirmPassword)
        valid_first_view = views.findViewById(R.id.valid_first_view)
        valid_last_view = views.findViewById(R.id.valid_last_view)
        valid_email_view.text=""
        valid_pass_view.text=""
        setcolor()
        setupFirstNameValidation()
        setupLastNameValidation()
        setupEmailValidation()
        setupPasswordValidation()
        setupConfirmPasswordValidation()

        All_ready_have_account.setOnClickListener {
            dialogs.dismiss()
            show_login_Popup()
        }

        togglePassword.setOnClickListener {
            isPasswordVisible = !isPasswordVisible
            EditText_New_Password.inputType = if (isPasswordVisible) {
                togglePassword.setImageResource(R.drawable.ic_eye_open)
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            } else {
                togglePassword.setImageResource(R.drawable.ic_eye_closed)
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            }
            // Safely set selection
            EditText_New_Password.text?.let { text ->
                EditText_New_Password.setSelection(text.length)
            }
        }

        toggleConfirmPassword.setOnClickListener {
            isPasswordVisiblee = !isPasswordVisiblee
            EditText_Confirm_Password.inputType = if (isPasswordVisiblee) {
                toggleConfirmPassword.setImageResource(R.drawable.ic_eye_open)
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            } else {
                toggleConfirmPassword.setImageResource(R.drawable.ic_eye_closed)
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            }
            // Safely set selection
            EditText_Confirm_Password.text?.let { text ->
                EditText_Confirm_Password.setSelection(text.length)
            }
        }


        submit_button.setOnClickListener {
            validateAndSubmit()
        }
    }
    private fun validateAndSubmit() {
        val firstName = EditText_first_name.text.toString().trim()
        val lastName = EditText_last_name.text.toString().trim()
        val userEmail = EditText_mail.text.toString().trim()
        val userPassword = EditText_New_Password.text.toString().trim()
        val userconfirmPassword = EditText_Confirm_Password.text.toString().trim()

        if (validateInputs(firstName, lastName, userEmail, userPassword, userconfirmPassword)) {
            registerUser(firstName, lastName, userEmail, userPassword, userconfirmPassword)
        }else{
            Toast.makeText(this@LogIn,"All Fields are Mandatory",Toast.LENGTH_SHORT).show()
        }
    }

    private fun validateInputs(
        firstName: String,
        lastName: String,
        email: String,
        password: String,
        confirmPassword: String
    ): Boolean {
        var isValid = true

        if (firstName.isEmpty() || firstName.length < 3 || !firstName.matches(Regex("^[A-Za-z]+$"))) {
            valid_first_view.text = "Enter a valid first name"
            isValid = false
        }

        if (lastName.isEmpty() || lastName.length < 3 || !lastName.matches(Regex("^[A-Za-z]+$"))) {
            valid_last_view.text = "Enter a valid last name"
            isValid = false
        }

        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            valid_email_view.text = "Enter a valid email address"
            isValid = false
        }

        val upperCase = Regex("[A-Z]")
        val lowerCase = Regex("[a-z]")
        val specialChar = Regex("[^A-Za-z0-9]")
        val digit = Regex("[0-9]")
        val passLength = password.length >= 6

        if (!password.contains(upperCase) || !password.contains(lowerCase)
            || !password.contains(specialChar) || !password.contains(digit) || !passLength
        ) {
            valid_pass_view.text = "Include: Uppercase, Lowercase, Digit, Special char, Min 6 char"
            isValid = false
        }

        if (password != confirmPassword) {
            valid_pass_view.setTextColor(Color.RED)
            "Password Mismatched"
            isValid = false
        }

        return isValid
    }

    private fun registerUser(
        firstName: String,
        lastName: String,
        email: String,
        password: String,
        confirmPassword: String
    ) {
        Log.d("SignUP", "Registering with: $firstName, $lastName, $email, $password, $confirmPassword")

        val retrofit = obj.createService(API::class.java)
        retrofit.register_user(firstName, lastName, email, password, confirmPassword)
            .enqueue(object : Callback<RegisterData> {
                override fun onResponse(call: Call<RegisterData>, response: Response<RegisterData>) {
                    Log.d("SignUP", "Response code: ${response.code()}")
                    when {
                        response.isSuccessful -> {
                            response.body()?.let {
                                Log.d("SignUP", "Response body: ${response.body()}")
                                val intent = Intent(this@LogIn, Otp_Verification::class.java)
                                intent.putExtra("mail_ID", email)
                                Toast.makeText(this@LogIn, "User Registration Successful", Toast.LENGTH_SHORT).show()
                                startActivity(intent)
                            } ?: run {
                                Toast.makeText(this@LogIn, "Invalid response from server", Toast.LENGTH_SHORT).show()
                            }
                        }
                        response.code() == 400 -> {
                            Log.d("SignUP", "Error body: ${response.errorBody()?.string()}")
                            valid_email_view.text="Email Already Exist"
                        }
                        else -> {
                            Toast.makeText(this@LogIn, "Login failed: ${response.code()}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                override fun onFailure(call: Call<RegisterData>, t: Throwable) {
                    Log.e("SignUP", "Network error: ${t.message}", t)
                    Toast.makeText(this@LogIn, "Network error: ${t.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
            })
    }


    fun setupFirstNameValidation() {
        EditText_first_name.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                val first_name = p0.toString()
                val name_case = Regex("[^A-Za-z]")
                val has_invalid_chars = name_case.containsMatchIn(first_name)
                val is_long_enough = first_name.length >= 3
                val error = mutableListOf<String>()

                if (has_invalid_chars) error.add("Only alphabets are allowed")
                if (!is_long_enough) error.add("Name Must Have Minimum 3 Characters")

                valid_first_view.text = if (error.isNotEmpty()) error.joinToString(" ,") else ""
            }

            override fun afterTextChanged(p0: Editable?) {
                p0?.let {
                    val capitalized = it.toString().lowercase().replaceFirstChar { char -> char.uppercase() }
                    if (it.toString() != capitalized) {
                        EditText_first_name.removeTextChangedListener(this)
                        EditText_first_name.setText(capitalized)
                        EditText_first_name.setSelection(capitalized.length)
                        EditText_first_name.addTextChangedListener(this)
                    }
                }
            }

        })
    }

    fun setupLastNameValidation() {
        EditText_last_name.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                val temp_fname = EditText_first_name.text.toString().trim()
                if (temp_fname.isEmpty()) {
                    EditText_last_name.post {
                        Toast.makeText(this@LogIn, "Please Enter First Name", Toast.LENGTH_SHORT)
                            .show()
                        EditText_first_name.requestFocus()
                    }
                }
            }
        }
        EditText_last_name.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                val lastname = p0.toString()
                val onlyLettersRegex = Regex("^[a-zA-Z]*$")
                val isOnlyLetters = lastname.matches(onlyLettersRegex)
                val isMinLength = lastname.length >= 3

                val errorMessages = mutableListOf<String>()
                if (!isOnlyLetters) errorMessages.add("Only characters are allowed")
                if (!isMinLength) errorMessages.add("Min 3 characters")

                valid_last_view.text = errorMessages.joinToString(", ")
            }

            override fun afterTextChanged(p0: Editable?) {
                p0?.let {
                    val capitalized = it.toString().lowercase().replaceFirstChar { char -> char.uppercase() }
                    if (it.toString() != capitalized) {
                        EditText_last_name.removeTextChangedListener(this)
                        EditText_last_name.setText(capitalized)
                        EditText_last_name.setSelection(capitalized.length)
                        EditText_last_name.addTextChangedListener(this)
                    }
                }
            }

        })
    }

    fun getPasswordStrength(password: String): String {
        return if (password.length >= 6) {
            valid_pass_view.setTextColor(Color.GREEN)
            "Strong Password"
        } else {
            valid_pass_view.setTextColor(Color.RED)
            "Weak Password"
        }
    }

    fun setupConfirmPasswordValidation() {
        EditText_Confirm_Password.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                val temppassword = EditText_New_Password.text.toString().trim()
                if (temppassword.isEmpty()) {
                    Toast.makeText(this@LogIn, "Enter Password First", Toast.LENGTH_SHORT)
                        .show()
                    EditText_New_Password.requestFocus()
                }
            }
        }
        EditText_Confirm_Password.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                val temp = EditText_New_Password.text.toString().trim()
                val currentpassword = EditText_Confirm_Password.text.toString().trim()
                if (temp != currentpassword) {
                    valid_pass_view.setTextColor(Color.RED) // Set to red here
                    valid_pass_view.text = "Password Mismatched"
                } else {
                    valid_pass_view.setTextColor(Color.GREEN) // Optional: reset color if matched
                    valid_pass_view.text = ""
                }
            }

            override fun afterTextChanged(p0: Editable?) {
            }

        })
    }

    fun setcolor() {
        val text = "Already have an Account ? Sign in"
        val spannableString = SpannableString(text)
        spannableString.setSpan(
            ForegroundColorSpan(Color.BLACK),
            text.indexOf("Already have an Account ? "),
            text.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        All_ready_have_account.text = spannableString
        spannableString.setSpan(
            ForegroundColorSpan(Color.YELLOW), text.indexOf("Sign in"), text.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        All_ready_have_account.text = spannableString
    }



    fun setupforgotpassword(){
        val intent=Intent(this@LogIn , Forgot_Password::class.java)
        startActivity(intent)
    }

    fun setupEmailValidation() {
        EditText_Email.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val emailInput = s.toString().trim()

                if (emailInput.isEmpty()) {
                    valid_mail_view.text = ""
                } else if (!Patterns.EMAIL_ADDRESS.matcher(emailInput).matches()) {
                    valid_mail_view.text = "Enter a valid email"
                } else {
                    valid_mail_view.text = "" // Clear error
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    fun setupPasswordValidation() {
        EditText_Password.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                val email = EditText_Email.text.toString().trim()
                if (email.isEmpty()) {
                    valid_mail_view.text = "Please enter email first"
                    valid_mail_view.setTextColor(Color.RED)
                    EditText_Email.requestFocus()
                }
            }
        }
        EditText_Password.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val password = s.toString()

                val uppercase = Regex("[A-Z]")
                val lowercase = Regex("[a-z]")
                val digit = Regex("[0-9]")
                val special = Regex("[^A-Za-z0-9]") // any non-alphanumeric char

                val hasUpper = password.contains(uppercase)
                val hasLower = password.contains(lowercase)
                val hasDigit = password.contains(digit)
                val hasSpecial = password.contains(special)
                val isLongEnough = password.length >= 6

                val errors = mutableListOf<String>()

                if (!hasUpper) errors.add("Uppercase")
                if (!hasLower) errors.add("Lowercase")
                if (!hasDigit) errors.add("Digit")
                if (!hasSpecial) errors.add("Special Char")
                if (!isLongEnough) errors.add("Min 6 chars")

                if (errors.isNotEmpty()) {
                    valid_password_view.text = "Include: ${errors.joinToString(", ")}"
                } else {
                    // Show strength
                    valid_password_view.text = ""
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }


    fun change_button_color(){
        val text = "Don’t have an Account ? Sign up"
        val spannableString = SpannableString(text)
        spannableString.setSpan(ForegroundColorSpan(Color.BLACK),text.indexOf("Don’t have an Account ?"),text.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        do_not_have_account.text = spannableString
        spannableString.setSpan(ForegroundColorSpan(Color.BLUE), text.indexOf("Sign up"), text.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        do_not_have_account.text = spannableString
    }

    fun login_credentials() {
        log_in.setOnClickListener {
            val email = EditText_Email.text.toString().trim()
            val password = EditText_Password.text.toString().trim()
            val retrofit = obj.createService(API::class.java)
            retrofit.log_in_user(email, password).enqueue(object : Callback<RecieveLogInData> {
                override fun onResponse(
                    call: Call<RecieveLogInData>,
                    response: Response<RecieveLogInData>
                ) {
                    when {
                        response.isSuccessful -> {
                            response.body()?.let { loginData ->
                                if (loginData.success) {
                                    // Inside onResponse -> if (loginData.success) { ... }
                                    checkprofile(loginData.token)
                                    Constant.saveLoginData(this@LogIn, loginData.user.email, loginData.token, true)
                                } else {
                                    Toast.makeText(this@LogIn, loginData.message, Toast.LENGTH_SHORT).show()
                                }
                            } ?: run {
                                Toast.makeText(
                                    this@LogIn,
                                    "Invalid response from server",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }

                        response.code() == 401 -> { // Unauthorized
                            valid_password_view.text = "Invalid email or password"
                            valid_password_view.setTextColor(Color.RED)
                        }

                        else -> { // Other errors
                            Toast.makeText(
                                this@LogIn,
                                "Login failed: ${response.code()}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }

                override fun onFailure(call: Call<RecieveLogInData>, t: Throwable) {
                    Log.e("LogInActivity", "Network error: ${t.message}", t)
                    Toast.makeText(
                        this@LogIn,
                        "Network error: ${t.localizedMessage}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
        }
    }

    fun checkprofile(token:String){
        Log.d("Token = ",token.toString())
        val profileRetrofit= obj.createService(API::class.java)
        profileRetrofit.get_profile_details_for_login(token).enqueue(object: Callback<ProfileInfoForLogIn>{
            override fun onResponse(
                call: Call<ProfileInfoForLogIn?>,
                response: Response<ProfileInfoForLogIn?>
            ) {
                if (response.isSuccessful && response.body()?.success == true) {
                    // Save profile created flag
                    Constant.setProfileCreated(this@LogIn,
                        Constant.getSavedEmail(this@LogIn).toString(), true)
                    startActivity(Intent(this@LogIn, Transaction::class.java))
                    finish()
                }
                else {
                    // Handle "profile not found" using errorBody
                    val errorJson = response.errorBody()?.string()
                    Log.e("Profile API", "ErrorBody: $errorJson")

                    if (errorJson?.contains("Profile not found") == true) {
                        Constant.setProfileCreated(this@LogIn,
                            Constant.getSavedEmail(this@LogIn).toString(), false)
                        startActivity(Intent(this@LogIn, ProfileInfo::class.java))
                        finish()
                    }
                    else {
                        Toast.makeText(this@LogIn, "Unexpected error from server", Toast.LENGTH_SHORT).show()
                    }
                }
            }


            override fun onFailure(
                call: Call<ProfileInfoForLogIn?>,
                t: Throwable
            ) {
                Toast.makeText(this@LogIn,t.localizedMessage, Toast.LENGTH_SHORT).show()
            }
        })
    }




}

