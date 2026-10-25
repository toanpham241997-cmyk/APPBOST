package com.havanhuan.booster

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import android.view.animation.OvershootInterpolator
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton

/**
 * Màn hình nhập key kích hoạt:
 * - Thẻ kính tối nằm giữa khung viền neon xoay tròn liên tục + 4 góc HUD
 * - Nhập đúng key (KeyConfig.ACTIVE_KEY) -> viền nháy xanh, âm chime, vào màn chính
 * - Nhập sai -> thẻ rung, viền nháy đỏ, hiện thông báo lỗi
 * - Đăng nhập thành công được ghi nhớ (Prefs) nên lần mở sau bỏ qua bước này.
 */
class KeyActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_key)

        val border = findViewById<NeonBorderView>(R.id.neonBorder)
        val card = findViewById<View>(R.id.cardRoot)
        val title = findViewById<TextView>(R.id.tvTitle)
        val keyInput = findViewById<EditText>(R.id.etKey)
        val errorText = findViewById<TextView>(R.id.tvError)
        val unlockBtn = findViewById<AppCompatButton>(R.id.btnUnlock)
        findViewById<ParticleView>(R.id.particles)

        border.startRotating()

        // Hiệu ứng vào màn
        title.alpha = 0f
        title.animate().alpha(1f).setDuration(600).start()
        card.scaleX = 0.85f
        card.scaleY = 0.85f
        card.alpha = 0f
        card.animate()
            .scaleX(1f).scaleY(1f).alpha(1f)
            .setDuration(650)
            .setInterpolator(OvershootInterpolator(1.2f))
            .setStartDelay(250)
            .start()

        unlockBtn.setOnClickListener { attemptUnlock() }
        keyInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                attemptUnlock()
                true
            } else {
                false
            }
        }
    }

    private fun attemptUnlock() {
        val border = findViewById<NeonBorderView>(R.id.neonBorder)
        val card = findViewById<View>(R.id.cardRoot)
        val keyInput = findViewById<EditText>(R.id.etKey)
        val errorText = findViewById<TextView>(R.id.tvError)
        val unlockBtn = findViewById<AppCompatButton>(R.id.btnUnlock)

        vibrate(25)
        val typed = keyInput.text.toString().trim()

        if (typed == KeyConfig.ACTIVE_KEY) {
            // ===== KEY ĐÚNG =====
            errorText.visibility = View.GONE
            border.flashSuccess()
            unlockBtn.text = getString(R.string.key_verifying)
            unlockBtn.isEnabled = false
            keyInput.isEnabled = false
            Sound.play(this, R.raw.success)
            vibrate(70)
            Prefs.setLoggedIn(this, true)
            window.decorView.postDelayed({
                startActivity(Intent(this, MainActivity::class.java))
                @Suppress("DEPRECATION")
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
                finish()
            }, 1000)
        } else {
            // ===== KEY SAI =====
            errorText.text = getString(R.string.key_error)
            errorText.alpha = 0f
            errorText.visibility = View.VISIBLE
            errorText.animate().alpha(1f).setDuration(200).start()
            border.flashError()
            card.startAnimation(AnimationUtils.loadAnimation(this, R.anim.shake))
            vibrate(160)
            Sound.play(this, R.raw.blip)
            keyInput.requestFocus()
        }
    }
}
