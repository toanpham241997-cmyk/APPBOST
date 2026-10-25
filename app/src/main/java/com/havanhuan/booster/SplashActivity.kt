package com.havanhuan.booster

import android.animation.ObjectAnimator
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import android.view.animation.DecelerateInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

/**
 * Màn hình intro mở app:
 * - Logo hexagon neon phóng to + lơ lửng nhẹ nhàng
 * - Halo sáng nhấp nháy, hạt sáng bay (ParticleView)
 * - Tên app + tagline hiện lần lượt, thanh tiến trình neon chạy đầy
 * - Âm thanh whoosh + chime phát đúng khoảnh khắc
 * Sau ~4.4s tự chuyển sang màn key (hoặc màn chính nếu đã đăng nhập).
 */
class SplashActivity : AppCompatActivity() {

    private var mediaPlayer: MediaPlayer? = null
    private var navigated = false
    private var floatAnimator: ObjectAnimator? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val logo = findViewById<ImageView>(R.id.ivLogo)
        val halo = findViewById<ImageView>(R.id.ivHalo)
        val name = findViewById<TextView>(R.id.tvAppName)
        val tagline = findViewById<TextView>(R.id.tvTagline)
        val progress = findViewById<ProgressBar>(R.id.progressBar)
        val loading = findViewById<TextView>(R.id.tvLoading)
        findViewById<ParticleView>(R.id.particles)

        // 1. Logo "bắn" vào giữa màn hình
        logo.scaleX = 0f
        logo.scaleY = 0f
        logo.alpha = 0f
        logo.animate()
            .scaleX(1f).scaleY(1f).alpha(1f)
            .setDuration(900)
            .setInterpolator(OvershootInterpolator(1.5f))
            .setStartDelay(150)
            .start()

        // 2. Logo lơ lửng lên xuống nhẹ nhàng
        floatAnimator = ObjectAnimator.ofFloat(logo, View.TRANSLATION_Y, 0f, -14f).apply {
            duration = 1500
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.REVERSE
            interpolator = DecelerateInterpolator()
            startDelay = 1150
            start()
        }

        // 3. Quầng sáng nhấp nháy sau logo
        halo.startAnimation(AnimationUtils.loadAnimation(this, R.anim.glow_pulse))

        // 4. Nội dung hiện lần lượt
        animateIn(name, 650)
        animateIn(tagline, 950)
        animateIn(progress, 1250)
        animateIn(loading, 1550)

        // 5. Thanh tiến trình chạy
        progress.max = 100
        ObjectAnimator.ofInt(progress, "progress", 0, 100).apply {
            duration = 3000
            interpolator = DecelerateInterpolator()
            startDelay = 1400
            start()
        }

        // 6. Âm thanh whoosh + chime (whoosh bắt đầu ~0.85s sau khi logo bay tới)
        window.decorView.postDelayed({
            if (!isFinishing) {
                try {
                    mediaPlayer = MediaPlayer.create(this, R.raw.intro_sound)?.also { it.start() }
                } catch (_: Exception) {
                }
            }
        }, 850)

        // Rung nhẹ trúng nhịp chime
        window.decorView.postDelayed({ vibrate(45) }, 1750)

        // 7. Chuyển màn hình
        window.decorView.postDelayed({ navigateNext() }, 4400)
    }

    private fun animateIn(view: View, delay: Long) {
        view.alpha = 0f
        view.translationY = 34f
        view.animate()
            .alpha(1f).translationY(0f)
            .setDuration(650)
            .setInterpolator(DecelerateInterpolator())
            .setStartDelay(delay)
            .start()
    }

    private fun navigateNext() {
        if (navigated || isFinishing) return
        navigated = true
        val next = if (Prefs.isLoggedIn(this)) MainActivity::class.java else KeyActivity::class.java
        startActivity(Intent(this, next))
        @Suppress("DEPRECATION")
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        finish()
    }

    override fun onDestroy() {
        floatAnimator?.cancel()
        mediaPlayer?.release()
        mediaPlayer = null
        super.onDestroy()
    }
}
