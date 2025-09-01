package com.example.expenso

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.example.expenso.ApiUsage.API
import com.example.expenso.ApiUsage.Constant
import com.example.expenso.ApiUsage.ImageUtil
import com.example.expenso.ApiUsage.obj
import com.example.expenso.Data_Class.Recieve_Profile_Update
import com.example.expenso.ProfileInfo
import com.google.android.material.bottomnavigation.BottomNavigationView
import retrofit2.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Response
import java.io.File

class Edit_Profile : AppCompatActivity() {
    lateinit var addimage: ImageView
    lateinit var addAddress: EditText
    lateinit var addPhone: EditText
    lateinit var saveChanges: Button
    lateinit var nameview: TextView
    lateinit var emailview: TextView
    private var tempPhotoUri: Uri? = null
    private var tempPhotoFile: File? = null
    lateinit var profile_image: ImageView
    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            profile_image.setImageURI(it)
            tempPhotoFile = uriToFile(it)
            Log.d("ProfileInfo", "Selected file: ${tempPhotoFile?.absolutePath}")

        }
    }
    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success && tempPhotoUri != null) {
            profile_image.setImageURI(tempPhotoUri)
            Log.d("ProfileInfo", "Camera image path: ${tempPhotoFile?.absolutePath}")

        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)
        nameview = findViewById<TextView>(R.id.user_name)
        emailview = findViewById<TextView>(R.id.user_email)
        addAddress = findViewById<EditText>(R.id.user_adress)
        addPhone = findViewById<EditText>(R.id.user_phone)
        saveChanges = findViewById<Button>(R.id.save_changes)
        addimage = findViewById<ImageView>(R.id.pic)
        profile_image = findViewById(R.id.profile_image)

        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottom_nav)
        bottomNavigationView.selectedItemId = R.id.nav_profile
        setupnameandemail()
        addimage.setOnClickListener {
            showImagePickerDialog()
        }
        setupPhone()

        val token = Constant.getSavedToken(this)
        saveChanges.setOnClickListener {
            val confirmAddress = addAddress.text.toString()
            val confirmPhone = addPhone.text.toString()
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
            retrofit.update_profile(token.toString(),addressPart,phonePart,body).enqueue(object: Callback<Recieve_Profile_Update>{
                override fun onResponse(
                    call: Call<Recieve_Profile_Update?>,
                    response: Response<Recieve_Profile_Update?>
                ) {
                    if(response.isSuccessful){
                        Toast.makeText(this@Edit_Profile,"Profile Updated Successfully",Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this@Edit_Profile, Profile::class.java))
                        finish()
                    }else{
                        val error= response.body()
                        Log.d("Error from server",error.toString())
                        Toast.makeText(this@Edit_Profile,"Error from server",Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(
                    call: Call<Recieve_Profile_Update?>,
                    t: Throwable
                ) {
                    Toast.makeText(this@Edit_Profile,t.localizedMessage,Toast.LENGTH_SHORT).show()
                }

            })

        }



    }
    fun setupnameandemail(){
        val name= Constant.getSavedUserName(this)
        val email = Constant.getSavedEmail(this)
        val address = Constant.getSavedUserAddress(this)
        val phone = Constant.getSavedUserPhone(this)
        val photo = Constant.getProfilePic(this)

        if (!photo.isNullOrEmpty()) {
            val fullUrl = ImageUtil.getFullImageUrl(photo)  // Construct full URL
            Log.d("ImageLoad", "Loading profile image from: $fullUrl")
            Glide.with(this)
                .load(fullUrl)
                .placeholder(R.drawable.default_photo_chiku)
                .error(R.drawable.default_photo_chiku)
                .into(profile_image)
        }

        nameview.text=name
        emailview.text=email
        addAddress.setText(address)
        addPhone.setText(phone)



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
        addPhone.setOnFocusChangeListener{_ , hasFocuse->
            if(hasFocuse){
                if(addAddress.text.isEmpty()){
                    Toast.makeText(this@Edit_Profile,"Address Field is mandatory",Toast.LENGTH_SHORT).show()
                    addAddress.requestFocus()
                }
            }
        }
        addPhone.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val phoneNumber = s.toString()

                if (phoneNumber.isNotEmpty() && !phoneNumber.matches(Regex("^\\d*$"))) {
                    Toast.makeText(this@Edit_Profile, "Only numerics are allowed", Toast.LENGTH_SHORT).show()
                }

                if (phoneNumber.length > 10) {
                    Toast.makeText(this@Edit_Profile, "Phone number cannot exceed 10 digits", Toast.LENGTH_SHORT).show()
                }
            }

            override fun afterTextChanged(s: Editable?) {
                val phoneNumber = s.toString()
                if (!phoneNumber.matches(Regex("^\\d{10}$"))) {
                    Toast.makeText(this@Edit_Profile, "Phone number must be exactly 10 digits", Toast.LENGTH_SHORT).show()
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