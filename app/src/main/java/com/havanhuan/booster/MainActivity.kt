package com.havanhuan.booster

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import java.util.Locale

/** Dashboard đọc số liệu thiết bị thực tế và mở các công cụ hệ thống có sẵn. */
class MainActivity : AppCompatActivity() {

    private lateinit var gauge: GaugeView
    private lateinit var status: TextView
    private lateinit var boostButton: AppCompatButton
    private var lastSnapshot: DeviceSnapshot? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        gauge = findViewById(R.id.gauge)
        status = findViewById(R.id.tvStatus)
        boostButton = findViewById(R.id.btnBoost)

        findViewById<View>(R.id.cardRam).setOnClickListener {
            val s = refreshStats()
            showInfo(R.string.ram_title, getString(R.string.ram_detail, s.ramAvailableBytes.toSizeLabel(), s.ramTotalBytes.toSizeLabel()))
        }
        findViewById<View>(R.id.cardJunk).setOnClickListener {
            openSystemSettings(Settings.ACTION_INTERNAL_STORAGE_SETTINGS)
        }
        findViewById<View>(R.id.cardCpu).setOnClickListener {
            val s = refreshStats()
            val temp = s.batteryTemperatureC?.let { String.format(Locale.getDefault(), "%.1f °C", it) }
                ?: getString(R.string.not_available)
            showInfo(R.string.cpu_title, getString(R.string.temperature_detail, temp))
        }
        findViewById<View>(R.id.cardBattery).setOnClickListener {
            openSystemSettings(Settings.ACTION_BATTERY_SAVER_SETTINGS)
        }
        findViewById<View>(R.id.cardGame).setOnClickListener { openAppPicker() }

        boostButton.setOnClickListener {
            vibrate(25)
            Sound.play(this, R.raw.blip)
            val reading = refreshStats()
            status.text = getString(R.string.scan_finished, reading.ramFreePercent.toInt())
            Snackbar.make(findViewById(R.id.dashboardRoot), R.string.scan_disclaimer, Snackbar.LENGTH_LONG).show()
        }

        findViewById<View>(R.id.btnRefresh).setOnClickListener {
            refreshStats()
            vibrate(15)
        }
        findViewById<View>(R.id.btnLogout).setOnClickListener {
            MaterialAlertDialogBuilder(this)
                .setTitle(R.string.logout_title)
                .setMessage(R.string.logout_msg)
                .setPositiveButton(R.string.logout_yes) { _, _ ->
                    Prefs.setLoggedIn(this, false)
                    startActivity(Intent(this, KeyActivity::class.java))
                    finish()
                }
                .setNegativeButton(R.string.cancel, null)
                .show()
        }

        listOf<View>(
            findViewById(R.id.cardGauge),
            findViewById(R.id.btnBoost),
            findViewById(R.id.row1),
            findViewById(R.id.row2),
            findViewById(R.id.cardGame)
        ).forEachIndexed { i, v ->
            v.alpha = 0f
            v.translationY = 24f
            v.animate().alpha(1f).translationY(0f)
                .setDuration(410L)
                .setStartDelay(i * 65L)
                .setInterpolator(DecelerateInterpolator())
                .start()
        }
    }

    override fun onResume() {
        super.onResume()
        refreshStats()
    }

    private fun refreshStats(): DeviceSnapshot {
        val reading = DeviceSnapshot.capture(this)
        lastSnapshot = reading
        val ramFree = reading.ramFreePercent.coerceIn(0f, 100f)
        gauge.animateTo(ramFree, 700)
        findViewById<TextView>(R.id.tvRamValue).text = getString(
            R.string.ram_value, reading.ramAvailableBytes.toSizeLabel(), reading.ramTotalBytes.toSizeLabel()
        )
        findViewById<TextView>(R.id.tvJunkValue).text = getString(
            R.string.storage_value, reading.storageAvailableBytes.toSizeLabel()
        )
        findViewById<TextView>(R.id.tvCpuValue).text = reading.batteryTemperatureC?.let {
            String.format(Locale.getDefault(), "%.1f °C", it)
        } ?: getString(R.string.not_available)
        findViewById<TextView>(R.id.tvBatteryValue).text = reading.batteryPercent?.let {
            getString(R.string.battery_value, it)
        } ?: getString(R.string.not_available)
        findViewById<TextView>(R.id.tvGameValue).text = getString(R.string.game_before)
        status.text = getString(R.string.status_ready, ramFree.toInt())
        return reading
    }

    private fun showInfo(titleId: Int, message: String) {
        MaterialAlertDialogBuilder(this)
            .setTitle(titleId)
            .setMessage(message)
            .setPositiveButton(R.string.understood, null)
            .show()
    }

    private fun openSystemSettings(action: String) {
        try {
            startActivity(Intent(action))
        } catch (_: ActivityNotFoundException) {
            try { startActivity(Intent(Settings.ACTION_SETTINGS)) }
            catch (_: ActivityNotFoundException) {
                Snackbar.make(findViewById(R.id.dashboardRoot), R.string.settings_unavailable, Snackbar.LENGTH_LONG).show()
            }
        }
    }

    private fun openAppPicker() {
        val launchers = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        try {
            startActivity(Intent.createChooser(launchers, getString(R.string.choose_app)))
        } catch (_: ActivityNotFoundException) {
            Snackbar.make(findViewById(R.id.dashboardRoot), R.string.settings_unavailable, Snackbar.LENGTH_LONG).show()
        }
    }
}
