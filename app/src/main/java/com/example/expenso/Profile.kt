package com.example.expenso

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.example.expenso.ApiUsage.API
import com.example.expenso.ApiUsage.Constant
import com.example.expenso.ApiUsage.ImageUtil
import com.example.expenso.ApiUsage.obj
import com.example.expenso.Data_Class.RecieveProfileDetails
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.google.android.material.bottomnavigation.BottomNavigationView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response



class Profile : AppCompatActivity() {
    lateinit var email: TextView
    lateinit var name: TextView
    lateinit var phone: TextView
    lateinit var address: TextView
    lateinit var profile_image: ImageView
    lateinit var profile_info_log_in_button: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        email = findViewById<TextView>(R.id.user_email)
        name= findViewById<TextView>(R.id.user_name)
        phone= findViewById<TextView>(R.id.user_phone)
        address = findViewById<TextView>(R.id.user_adress)
        profile_image = findViewById(R.id.profile_image)
        profile_info_log_in_button = findViewById(R.id.profile_log_in_button)
        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottom_nav)
        bottomNavigationView.selectedItemId = R.id.nav_profile // Set Profile as the default selected item
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_category -> {
                    // Open Budget Categories Activity
                    startActivity(Intent(this, Budget_Categories::class.java))
                    // Change icon color to blue when selected
                    item.icon?.setTint(Color.parseColor("#0000FF"))  // Set to blue
                    return@setOnItemSelectedListener true
                }
                R.id.nav_profile -> {
                    // Open Profile Activity
                    startActivity(Intent(this, Profile::class.java))
                    return@setOnItemSelectedListener true
                }
                R.id.nav_home->{
                    startActivity(Intent(this, Transaction::class.java))
                    return@setOnItemSelectedListener true
                }
                R.id.nav_report->{
                    startActivity(Intent(this, Expenses::class.java))
                    return@setOnItemSelectedListener true
                }
                else -> false
            }
        }





        val token= Constant.getSavedToken(this)
        val retrofit=obj.createService(API::class.java)
        retrofit.get_profile_details(token.toString())
            .enqueue(object : Callback<RecieveProfileDetails> {
                override fun onResponse(
                    call: Call<RecieveProfileDetails>,
                    response: Response<RecieveProfileDetails>
                ) {
                    if (response.isSuccessful) {
                        val profile = response.body()?.profile
                        if (profile != null) {
                            // 1. TextViews
                            name.text    = profile.fullname
                            phone.text   = profile.phone
                            address.text = profile.address
                            val emailid = Constant.getSavedEmail(this@Profile)
                            email.text= emailid
                            Constant.saveUserName(this@Profile, name.text.toString())
                            Constant.saveUserPhone(this@Profile,profile.phone)
                            Constant.saveUserAddress(this@Profile,profile.address)
                            Log.d("ProfilePic", "Profile picture URL: ${profile.profilePic}")

                            val imagePath = profile?.profilePic  // adjust this based on actual structure
                            if (imagePath != null) {
                                Constant.saveProfilePic(this@Profile, imagePath)
                                val fullUrl = ImageUtil.getFullImageUrl(imagePath)
                                Log.d("ImageLoad", "Full image URL: $fullUrl")
                                Log.d("ImageLoad", "Trying to load image from: $fullUrl")
                                Glide.with(this@Profile)
                                    .load(imagePath)
                                    .placeholder(R.drawable.default_photo_chiku)
                                    .error(R.drawable.default_photo_chiku)
                                    .into(profile_image)
                            }

                            // URL or path


                        } else {
                            Log.d("Profile", "Profile picture URL is empty")
                            Toast.makeText(this@Profile, "Empty profile data", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        val errorBody = response.errorBody()?.string()
                        Log.e("API Error", "Code: ${response.code()}, Message: ${response.message()}, Body: $errorBody")
                        Toast.makeText(this@Profile, "Server error: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<RecieveProfileDetails>, t: Throwable) {
                    Toast.makeText(this@Profile, "Network error: ${t.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
            })





        name.setOnClickListener {
            startActivity(Intent(this@Profile, Settings::class.java))
            finish()
        }
        profile_info_log_in_button.setOnClickListener {
            startActivity(Intent(this@Profile, Edit_Profile::class.java))
        }
        email.setOnClickListener {
            startActivity(Intent(this@Profile, Budget_Categories::class.java))
        }

        phone.setOnClickListener { startActivity(Intent(this@Profile, Analysis::class.java)) }
    }


}