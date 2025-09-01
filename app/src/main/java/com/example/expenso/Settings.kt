package com.example.expenso

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.expenso.ApiUsage.Constant
import com.example.expenso.ProfileInfo

class Settings : AppCompatActivity() {
    lateinit var notification_tv: TextView
    lateinit var accounts_tv: TextView
    lateinit var currency_key: TextView
    lateinit var analysis_key: TextView
    lateinit var logout: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        notification_tv = findViewById(R.id.notification_tv)
        accounts_tv = findViewById(R.id.accounts_tv)
        currency_key = findViewById(R.id.currency_key)
        analysis_key = findViewById(R.id.analysis_key)
        logout = findViewById(R.id.logout)
        val app = findViewById<TextView>(R.id.app)
        app.setOnClickListener { startActivity(Intent(this@Settings, Transaction::class.java)) }

        logout.setOnClickListener {
            Constant.clearLoginData(this@Settings)

            // Go back to Login screen
            val intent = Intent(this@Settings, LogIn::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish() // kil
        }
        accounts_tv.setOnClickListener {
            startActivity(Intent(this@Settings, Profile::class.java))
        }
        notification_tv.setOnClickListener {
            val intent = Intent(this@Settings, ChangePassword::class.java)
            startActivity(intent)
        }
        analysis_key.setOnClickListener {
            val intent = Intent(this@Settings, Analysis::class.java)
            startActivity(intent)
        }

        currency_key.setOnClickListener {
            startActivity(Intent(this@Settings, Home::class.java))
        }

    }
}