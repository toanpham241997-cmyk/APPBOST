package com.havanhuan.booster

import android.content.Context
import android.graphics.BlurMaskFilter
import android.graphics.Canvas
import android.graphics.Paint
import android.os.Build
import android.util.AttributeSet
import android.view.View
import java.util.Random
import kotlin.math.sin

/**
 * Lớp hạt sáng neon bay lên (particle) dùng cho intro + màn hình key.
 * Các hạt lơ lửng, lấp lánh và trôi ngẫu nhiên như bụi sao.
 */
class ParticleView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private class Particle(
        var x: Float,
        var y: Float,
        var radius: Float,
        var speedY: Float,
        var speedX: Float,
        var alpha: Float,
        var color: Int,
        var phase: Float
    )

    private val particles = mutableListOf<Particle>()
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val random = Random()

    private val neonColors = intArrayOf(
        0xFF00E5FF.toInt(),
        0xFFB388FF.toInt(),
        0xFFF500FF.toInt(),
        0xFFFFFFFF.toInt()
    )

    private var running = false

    private val frameCallback = object : Runnable {
        override fun run() {
            if (running) {
                step()
                invalidate()
                postOnAnimation(this)
            }
        }
    }

    init {
        // BlurMaskFilter chỉ hoạt động trên hardware canvas từ API 28
        if (Build.VERSION.SDK_INT < 28) {
            setLayerType(LAYER_TYPE_SOFTWARE, null)
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (particles.isEmpty()) {
            repeat(44) { particles.add(spawn(initial = true)) }
        }
    }

    private fun spawn(initial: Boolean): Particle {
        val w = width.toFloat().coerceAtLeast(1f)
        val h = height.toFloat().coerceAtLeast(1f)
        return Particle(
            x = random.nextFloat() * w,
            y = if (initial) random.nextFloat() * h else h + random.nextFloat() * 40f,
            radius = 1.2f + random.nextFloat() * 4.2f,
            speedY = (2f + random.nextFloat() * 6f) * (h / 800f).coerceIn(0.6f, 2.2f),
            speedX = (random.nextFloat() - 0.5f) * 1.6f,
            alpha = 40f + random.nextFloat() * 160f,
            color = neonColors[random.nextInt(neonColors.size)],
            phase = random.nextFloat() * (2f * Math.PI.toFloat())
        )
    }

    private fun step() {
        val w = width.toFloat()
        val h = height.toFloat()
        val t = System.nanoTime() / 1_000_000_000f
        for (i in particles.indices) {
            val p = particles[i]
            p.y -= p.speedY
            p.x += p.speedX + 14f * sin(t * 2f + p.phase)
            p.alpha -= 0.35f
            if (p.y < -20f || p.alpha <= 10f || p.x < -30f || p.x > w + 30f) {
                particles[i] = spawn(initial = false)
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        for (p in particles) {
            paint.color = p.color
            paint.alpha = p.alpha.toInt().coerceIn(0, 255)
            paint.maskFilter = BlurMaskFilter(p.radius * 2.4f, BlurMaskFilter.Blur.NORMAL)
            canvas.drawCircle(p.x, p.y, p.radius, paint)
        }
        paint.maskFilter = null
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        start()
    }

    override fun onDetachedFromWindow() {
        stop()
        super.onDetachedFromWindow()
    }

    private fun start() {
        if (!running) {
            running = true
            postOnAnimation(frameCallback)
        }
    }

    private fun stop() {
        running = false
        removeCallbacks(frameCallback)
    }
}
