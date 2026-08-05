package com.example.zipmaster

import android.content.Intent
import android.os.Bundle
import android.view.animation.AnticipateOvershootInterpolator
import androidx.appcompat.app.AppCompatActivity
import androidx.core.animation.doOnEnd
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.zipmaster.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // انیمیشن لوگو
        binding.ivLogo.apply {
            alpha = 0f
            scaleX = 0f
            scaleY = 0f
            animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(800)
                .setInterpolator(AnticipateOvershootInterpolator())
                .start()
        }

        // انیمیشن عنوان
        binding.tvTitle.apply {
            alpha = 0f
            translationY = 50f
            animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(600)
                .setStartDelay(300)
                .start()
        }

        // انیمیشن کارت‌ها
        binding.cardExtract.apply {
            alpha = 0f
            translationX = -100f
            animate()
                .alpha(1f)
                .translationX(0f)
                .setDuration(500)
                .setStartDelay(500)
                .start()
        }

        binding.cardCompress.apply {
            alpha = 0f
            translationX = 100f
            animate()
                .alpha(1f)
                .translationX(0f)
                .setDuration(500)
                .setStartDelay(600)
                .start()
        }

        // کلیک‌ها
        binding.cardExtract.setOnClickListener {
            it.animate()
                .scaleX(0.95f)
                .scaleY(0.95f)
                .setDuration(100)
                .withEndAction {
                    it.animate().scaleX(1f).scaleY(1f).setDuration(100).start()
                    startActivity(Intent(this, ExtractActivity::class.java))
                    overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                }
                .start()
        }

        binding.cardCompress.setOnClickListener {
            it.animate()
                .scaleX(0.95f)
                .scaleY(0.95f)
                .setDuration(100)
                .withEndAction {
                    it.animate().scaleX(1f).scaleY(1f).setDuration(100).start()
                    startActivity(Intent(this, CompressActivity::class.java))
                    overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                }
                .start()
        }
    }
}
