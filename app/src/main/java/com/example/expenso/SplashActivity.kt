package com.example.expenso

import android.content.Intent
import android.os.Bundle
import android.os.Looper
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.os.postDelayed
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.os.Handler



class SplashActivity : AppCompatActivity() {
    private lateinit var splashImage: ImageView
    private lateinit var splashFrame: FrameLayout

    private val images = listOf(
        R.drawable.mask1, // Frame 1
        R.drawable.mask2, // Frame 2 (same background)
        R.drawable.mask2  // Frame 3 (different background)
    )
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        splashImage = findViewById(R.id.splashImage)
        splashFrame = findViewById(R.id.splashFrame)

        showFrame(0)
    }
    private fun showFrame(index: Int) {
        if (index >= images.size) {
            startActivity(Intent(this, ViewPager::class.java))
            finish()
            return
        }

        // Change background before 3rd frame
        if (index == 2) {
            splashFrame.setBackgroundColor(ContextCompat.getColor(this, R.color.white)) // or any color
        }

        splashImage.setImageResource(images[index])
        splashImage.alpha = 0f
        splashImage.animate()
            .alpha(1f)
            .setDuration(600)
            .withEndAction {
                Handler(Looper.getMainLooper()).postDelayed({
                    showFrame(index + 1)
                }, 800)
            }
            .start()
    }
}
