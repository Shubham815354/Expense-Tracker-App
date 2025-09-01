package com.example.expenso

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.expenso.ApiUsage.API
import com.example.expenso.ApiUsage.obj
import com.example.expenso.Data_Class.Contact
import com.google.android.material.bottomnavigation.BottomNavigationView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ContactUs : AppCompatActivity() {
    lateinit var contact_name: EditText
    lateinit var contact_email: EditText
    lateinit var contact_subject: EditText
    lateinit var contact_comment: EditText
    lateinit var this_month_transaction: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contact_us)

        contact_name= findViewById(R.id.contact_name)
        contact_email= findViewById(R.id.contact_email)
        contact_subject = findViewById(R.id.contact_subject)
        contact_comment = findViewById(R.id.contact_comment)
        this_month_transaction = findViewById(R.id.Submit_contact)
        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottom_nav_cont_)
        bottomNavigationView.selectedItemId = R.id.nav_home

        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home->{
                    startActivity(Intent(this, Home::class.java))
                    item.icon?.setTint(Color.parseColor("#0000FF"))
                    return@setOnItemSelectedListener true
                }
                R.id.nav_category -> {
                    // Open Budget Categories Activity
                    startActivity(Intent(this, Budget_Categories::class.java))

                    return@setOnItemSelectedListener true

                }
                R.id.nav_profile -> {
                    // Open Profile Activity
                    startActivity(Intent(this, Profile::class.java))
                    return@setOnItemSelectedListener true
                }
                R.id.nav_report->{
                    startActivity(Intent(this, Expenses::class.java))
                    return@setOnItemSelectedListener true
                }

                else -> false
            }
        }

        this_month_transaction.setOnClickListener {
            val request = Contact(
                name = contact_name.text.toString(),
            email =  contact_email.text.toString(),
            subject =contact_subject.text.toString(),
            message = contact_comment.text.toString()
            )

            //checkEmpty(contactName,contactEmail,contactSubject,contactComment)
            val retrofit= obj.createService(API::class.java)
            retrofit.contct_form(request).enqueue(object:Callback<Contact>{
                override fun onResponse(
                    call: Call<Contact>,
                    response: Response<Contact>
                ) {
                    if(response.isSuccessful){
                        Toast.makeText(this@ContactUs,"Message submited",Toast.LENGTH_SHORT).show()
                    }else{
                        val errorMsg = response.errorBody()?.string()
                        Toast.makeText(this@ContactUs, "Error: $errorMsg", Toast.LENGTH_LONG).show()
                        Log.d("Error Code=",response.code().toString())
                        Log.d("Error =",response.errorBody().toString())

                    }
                }

                override fun onFailure(
                    call: Call<Contact>,
                    t: Throwable
                ) {
                    Toast.makeText(this@ContactUs,t.localizedMessage,Toast.LENGTH_SHORT).show()
                    Log.d("Error t=",t.localizedMessage.toString())
                }

            })
        }

    }
   /*fun checkEmpty(contactName:String,contactEmail:String,contactSubject:String,contactComment:String){
        when{
            contactName.isEmpty()->{
                Toast.makeText(this@ContactUs,"Name is Mandatory",Toast.LENGTH_SHORT).show()
                return
            }
            contactEmail.isEmpty()->{
                Toast.makeText(this@ContactUs,"Email is Mandatory",Toast.LENGTH_SHORT).show()
                return
            }
            contactSubject.isEmpty()->{
                Toast.makeText(this@ContactUs,"Subject is Mandatory",Toast.LENGTH_SHORT).show()
                return
            }
            contactComment.isEmpty()->{
                Toast.makeText(this@ContactUs,"Comment is Mandatory",Toast.LENGTH_SHORT).show()
                return
            }else->{
            Toast.makeText(this@ContactUs,"Uploading Data",Toast.LENGTH_LONG).show()
            }
        }
    }*/
}