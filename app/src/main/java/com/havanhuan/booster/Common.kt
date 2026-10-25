package com.havanhuan.booster

import android.content.Context
import android.media.MediaPlayer
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.content.Context.VIBRATOR_SERVICE

/**
 * Cấu hình key kích hoạt của app.
 * ĐỔI KEY TẠI ĐÂY — chỉ cần sửa 1 dòng duy nhất.
 */
object KeyConfig {
    const val ACTIVE_KEY = "havanhuan"
}

/** Lưu trạng thái đăng nhập (tự nhớ key đã kích hoạt). */
object Prefs {
    private const val FILE = "havanhuan_prefs"
    private const val LOGGED_IN = "logged_in"

    fun isLoggedIn(context: Context): Boolean =
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE)
            .getBoolean(LOGGED_IN, false)

    fun setLoggedIn(context: Context, value: Boolean) {
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE)
            .edit().putBoolean(LOGGED_IN, value).apply()
    }
}

/** Phát file âm thanh trong res/raw, tự giải phóng sau khi phát xong. */
object Sound {
    fun play(context: Context, resId: Int) {
        try {
            MediaPlayer.create(context, resId)?.apply {
                setOnCompletionListener { it.release() }
                start()
            }
        } catch (_: Exception) {
        }
    }
}

/** Rung nhẹ tạo cảm giác phản hồi vật lý. */
fun Context.vibrate(ms: Long) {
    try {
        val vibrator = getSystemService(VIBRATOR_SERVICE) as? Vibrator ?: return
        if (Build.VERSION.SDK_INT >= 26) {
            vibrator.vibrate(
                VibrationEffect.createOneShot(ms, VibrationEffect.DEFAULT_AMPLITUDE)
            )
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(ms)
        }
    } catch (_: Exception) {
    }
}
