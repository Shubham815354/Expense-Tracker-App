package com.example.expenso

import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.get
import androidx.viewpager2.widget.ViewPager2
import com.example.expenso.ApiUsage.API
import com.example.expenso.ApiUsage.Constant
import com.example.expenso.ApiUsage.obj
import com.example.expenso.Data_Class.IntroSlide
import com.example.expenso.Data_Class.RecieveProfileDetails
import com.example.expenso.LogIn
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ViewPager : AppCompatActivity() {
    lateinit var introSliderViewPager: ViewPager2
    lateinit var indicatorContainer: LinearLayout
    private val introSliderAdapter = IntroSliderAdapter(
        listOf(
            IntroSlide("Easy Way To Monitor Your Expense", "Safe your future by managing your expense right now", R.drawable.viewpager_1),
            IntroSlide("Track Your Expenses And Save Your Money", "Safe your future by managing your expense right now", R.drawable.view_pager2),
            IntroSlide("Track Your Expenses And Save Your Money", "Safe your future by managing your expense right now", R.drawable.view_pager_3)
        )
    )
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Constant.isLoggedIn(this)) {
            val token = Constant.getSavedToken(this)

            if (token != null) {
                val retrofit = obj.createService(API::class.java)
                retrofit.get_profile_details(token)
                    .enqueue(object : Callback<RecieveProfileDetails> {
                        override fun onResponse(
                            call: Call<RecieveProfileDetails>,
                            response: Response<RecieveProfileDetails>
                        ) {
                            if (response.isSuccessful && response.body()?.profile != null) {
                                val email = Constant.getSavedEmail(this@ViewPager)
                                if (!email.isNullOrEmpty()) {
                                    Constant.setProfileCreated(this@ViewPager, email, true)
                                }
                                startActivity(Intent(this@ViewPager, Transaction::class.java))
                            } else {
                                startActivity(Intent(this@ViewPager, ProfileInfo::class.java))
                            }
                            finish()
                        }
                        override fun onFailure(call: Call<RecieveProfileDetails>, t: Throwable) {
                            Toast.makeText(this@ViewPager, "Error checking profile", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this@ViewPager, LogIn::class.java))
                            finish()
                        }
                    })
                return
            } else {
                startActivity(Intent(this@ViewPager, LogIn::class.java))
                finish()
                return  // 👈 IMPORTANT
            }
        }
        setContentView(R.layout.activity_view_pager)


    setContentView(R.layout.activity_view_pager)
        introSliderViewPager = findViewById(R.id.introSliderViewPager)
        indicatorContainer = findViewById(R.id.indicatorContainer)
        val leftArrow: ImageView = findViewById(R.id.arrowImage_left)
        val rightArrow: ImageView = findViewById(R.id.arrowImage_right)
        introSliderViewPager.adapter = introSliderAdapter
        setUpIndicators()
        setCurrentIndicator(0)
        leftArrow.setOnClickListener {
            val prevItem = introSliderViewPager.currentItem - 1
            if (prevItem >= 0) {
                introSliderViewPager.currentItem = prevItem
            }
        }

        rightArrow.setOnClickListener {
            val nextItem = introSliderViewPager.currentItem + 1
            if (nextItem < introSliderAdapter.itemCount) {
                introSliderViewPager.currentItem = nextItem
            } else {
                // Last slide: Open Profile activity
                startActivity(Intent(this@ViewPager, LogIn::class.java))
                finish()
            }
        }

        introSliderViewPager.registerOnPageChangeCallback(object: ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                setCurrentIndicator(position)

                // Hide left arrow on first page
                leftArrow.visibility = if (position == 0) ImageView.INVISIBLE else ImageView.VISIBLE

                // Hide right arrow on last page
                // Always show right arrow
                rightArrow.visibility = ImageView.VISIBLE

            }
        })



    }

    private fun setUpIndicators() {
        val indicators = arrayOfNulls<ImageView>(introSliderAdapter.itemCount)
        val layoutParams: LinearLayout.LayoutParams = LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT)
        layoutParams.setMargins(8,8,8,8)
        for(i in indicators.indices) {
            indicators[i] = ImageView(applicationContext)
            indicators[i].apply {
                this?.setImageDrawable(
                    ContextCompat.getDrawable(applicationContext, R.drawable.indicator_inactive)
                )
                this?.layoutParams = layoutParams
            }
            indicatorContainer.addView(indicators[i])
        }
    }

    private fun setCurrentIndicator(index: Int) {
        val childCount = indicatorContainer.childCount
        for(i in 0 until childCount) {
            val imageView = indicatorContainer[i] as ImageView
            if(i == index) {
                imageView.setImageDrawable(ContextCompat.getDrawable(applicationContext, R.drawable.indicator_active))
            } else {
                imageView.setImageDrawable(ContextCompat.getDrawable(applicationContext, R.drawable.indicator_inactive))
            }
        }
    }
}





