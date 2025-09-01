package com.example.expenso

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.activity.result.contract.ActivityResultContracts
import com.example.expenso.ApiUsage.API
import com.example.expenso.ApiUsage.obj
import java.io.File
import android.text.TextWatcher
import android.util.Log
import com.example.expenso.ApiUsage.Constant
import com.example.expenso.Data_Class.RecieveProfileInfo
import com.example.expenso.LogIn
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.text.Regex
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody


class ProfileInfo : AppCompatActivity() {

    private lateinit var profileImage: ImageView
    private lateinit var addImage: ImageView
    private lateinit var loginButton: Button
    private lateinit var address: EditText
    private lateinit var phone: EditText

    private var tempPhotoUri: Uri? = null
    private var tempPhotoFile: File? = null


    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            profileImage.setImageURI(it)
            tempPhotoFile = uriToFile(it)
            Log.d("ProfileInfo", "Selected file: ${tempPhotoFile?.absolutePath}")

        }
    }

    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success && tempPhotoUri != null) {
            profileImage.setImageURI(tempPhotoUri)
            Log.d("ProfileInfo", "Camera image path: ${tempPhotoFile?.absolutePath}")

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_info)

        profileImage = findViewById(R.id.profile_image)
        addImage = findViewById(R.id.add_image)
        loginButton = findViewById(R.id.profile_info_log_in_button)
        address = findViewById<EditText>(R.id.EditText_address)
        phone = findViewById<EditText>(R.id.EditText_phone)
        loginButton.text="Upload"

        addImage.setOnClickListener {
            showImagePickerDialog()
        }

        setupPhone()

        val token = Constant.getSavedToken(this)
        loginButton.setOnClickListener {
            val confirmAddress = address.text.toString()
            val confirmPhone = phone.text.toString()
            val file = tempPhotoFile



            if (file == null || !file.exists()) {
                Log.d("ProfileInfo", "File exists: ${file?.exists()}")  // Debug log
                Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            } else if (confirmPhone.isEmpty() || confirmAddress.isEmpty()) {
                Toast.makeText(this, "Address and phone number are mandatory", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val mimeType = when (file.extension.lowercase()) {
                "jpg", "jpeg" -> "image/jpeg"
                "png" -> "image/png"
                else -> {
                    Toast.makeText(this, "Unsupported file format", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
            }

            val requestFile = file.asRequestBody(mimeType.toMediaTypeOrNull())
            val body = MultipartBody.Part.createFormData("profilePic", file.name, requestFile)
            val addressPart = confirmAddress.toRequestBody("text/plain".toMediaTypeOrNull())
            val phonePart = confirmPhone.toRequestBody("text/plain".toMediaTypeOrNull())

            val retrofit = obj.createService(API::class.java)
            retrofit.create_profile(token.toString(), addressPart, phonePart, body)
                .enqueue(object : Callback<RecieveProfileInfo> {
                    override fun onResponse(
                        call: Call<RecieveProfileInfo?>,
                        response: Response<RecieveProfileInfo?>
                    ) {
                        if (response.isSuccessful) {
                            val profileInfo = response.body()?.profile
                            if (profileInfo != null) {
                                val profilePic = profileInfo.profilePic
                                Constant.saveProfilePic(this@ProfileInfo, profilePic)

                                val email = Constant.getSavedEmail(this@ProfileInfo)
                                if (email != null) {
                                    Constant.setProfileCreated(this@ProfileInfo, email, true)
                                }

                                Toast.makeText(this@ProfileInfo, "Photo uploaded", Toast.LENGTH_SHORT).show()
                                Log.d("ProfileInfo", "Navigating to LogIn activity")
                                val intent = Intent(this@ProfileInfo, Transaction::class.java)
                                startActivity(intent)
                                finish()
                            }
                        } else {
                            Toast.makeText(this@ProfileInfo, "Error from server: ${response.errorBody()?.string()}", Toast.LENGTH_SHORT).show()
                        }
                    }


                    override fun onFailure(call: Call<RecieveProfileInfo?>, t: Throwable) {
                        Log.e("ProfileInfo", "API call failed: ${t.message}")
                        Toast.makeText(this@ProfileInfo, t.localizedMessage, Toast.LENGTH_SHORT).show()
                    }
                })
        }

    }

    private fun showImagePickerDialog() {
        val options = arrayOf("Take Photo", "Choose from Gallery")

        AlertDialog.Builder(this)
            .setTitle("Select Profile Photo")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> launchCamera()
                    1 -> galleryLauncher.launch("image/*")
                }
            }
            .show()
    }

    private fun launchCamera() {
        val photoFile = File.createTempFile("temp_image", ".jpg", cacheDir).apply {
            createNewFile()
            deleteOnExit()
        }
        tempPhotoFile = photoFile // Save file reference
        tempPhotoUri = FileProvider.getUriForFile(
            this,
            "${applicationContext.packageName}.provider",
            photoFile
        )
        cameraLauncher.launch(tempPhotoUri!!)
    }

    fun setupPhone(){
        phone.setOnFocusChangeListener{_ , hasFocuse->
            if(hasFocuse){
                if(address.text.isEmpty()){
                    Toast.makeText(this@ProfileInfo,"Address Field is mandatory",Toast.LENGTH_SHORT).show()
                    address.requestFocus()
                }
            }
        }
        phone.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val phoneNumber = s.toString()

                if (phoneNumber.isNotEmpty() && !phoneNumber.matches(Regex("^\\d*$"))) {
                    Toast.makeText(this@ProfileInfo, "Only numerics are allowed", Toast.LENGTH_SHORT).show()
                }

                if (phoneNumber.length > 10) {
                    Toast.makeText(this@ProfileInfo, "Phone number cannot exceed 10 digits", Toast.LENGTH_SHORT).show()
                }
            }

            override fun afterTextChanged(s: Editable?) {
                val phoneNumber = s.toString()
                if (!phoneNumber.matches(Regex("^\\d{10}$"))) {
                    Toast.makeText(this@ProfileInfo, "Phone number must be exactly 10 digits", Toast.LENGTH_SHORT).show()
                }
            }
        })

    }
    private fun uriToFile(uri: Uri): File? {
        val inputStream = contentResolver.openInputStream(uri) ?: return null
        val tempFile = File.createTempFile("selected_image", ".jpg", cacheDir)
        tempFile.outputStream().use { fileOut ->
            inputStream.copyTo(fileOut)
        }
        return tempFile
    }

}
