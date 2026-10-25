package com.havanhuan.booster

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.SweepGradient
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator

/**
 * Khung viền neon xoay tròn quanh thẻ nhập key (hiệu ứng gradient chạy),
 * kèm 4 góc HUD kiểu khoa học viễn tưởng. Có thể flash xanh (đúng key)
 * hoặc flash đỏ (sai key) kèm rung.
 */
class NeonBorderView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }
    private val path = Path()
    private val rect = RectF()

    private var radius = dp(26f)
    private var borderStroke = dp(3f)
    private var cornerLen = dp(26f)
    private var cornerInset = dp(9f)
    private var cornerStroke = dp(3.5f)

    private var shader: SweepGradient? = null
    private var angle = 0f
    private var animator: ValueAnimator? = null
    private var revertRunnable: Runnable? = null

    private var colors = DEFAULT_COLORS.copyOf()

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (w > 0 && h > 0) {
            shader = SweepGradient(w / 2f, h / 2f, colors, null)
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val w = width.toFloat()
        val h = height.toFloat()
        val s = shader ?: return

        val m = Matrix()
        m.postRotate(angle, w / 2f, h / 2f)
        s.setLocalMatrix(m)

        rect.set(borderStroke, borderStroke, w - borderStroke, h - borderStroke)
        path.reset()
        path.addRoundRect(rect, radius, radius, Path.Direction.CW)

        // Lớp glow (nét to, mờ)
        paint.shader = s
        paint.strokeWidth = borderStroke * 2.6f
        paint.alpha = 70
        canvas.drawPath(path, paint)

        // Lớp nét chính (rõ)
        paint.strokeWidth = borderStroke
        paint.alpha = 255
        canvas.drawPath(path, paint)

        // 4 góc HUD màu neon xanh cố định
        paint.shader = null
        paint.color = 0xFF00E5FF.toInt()
        paint.strokeWidth = cornerStroke
        drawCornerBrackets(canvas, w, h)
    }

    private fun drawCornerBrackets(canvas: Canvas, w: Float, h: Float) {
        val i = cornerInset
        val l = cornerLen
        // Góc trên trái
        canvas.drawLine(i, i, i + l, i, paint)
        canvas.drawLine(i, i, i, i + l, paint)
        // Góc trên phải
        canvas.drawLine(w - i - l, i, w - i, i, paint)
        canvas.drawLine(w - i, i, w - i, i + l, paint)
        // Góc dưới trái
        canvas.drawLine(i, h - i - l, i, h - i, paint)
        canvas.drawLine(i, h - i, i + l, h - i, paint)
        // Góc dưới phải
        canvas.drawLine(w - i - l, h - i, w - i, h - i, paint)
        canvas.drawLine(w - i, h - i - l, w - i, h - i, paint)
    }

    /** Bắt đầu xoay gradient vô hạn. */
    fun startRotating() {
        if (animator?.isRunning == true) return
        animator = ValueAnimator.ofFloat(0f, 360f).apply {
            duration = 3600
            repeatCount = ValueAnimator.INFINITE
            interpolator = LinearInterpolator()
            addUpdateListener {
                angle = it.animatedValue as Float
                invalidate()
            }
            start()
        }
    }

    fun stopRotating() {
        animator?.cancel()
        animator = null
    }

    /** Nháy viền xanh lá khi key đúng. */
    fun flashSuccess() {
        flash(intArrayOf(0xFF00E676.toInt(), 0xFF69F0AE.toInt(), 0xFF00E676.toInt(), 0xFFB9F6CA.toInt(), 0xFF00E676.toInt()))
    }

    /** Nháy viền đỏ khi key sai. */
    fun flashError() {
        flash(intArrayOf(0xFFFF5252.toInt(), 0xFFFF8A80.toInt(), 0xFFFF5252.toInt(), 0xFFFF8A80.toInt(), 0xFFFF5252.toInt()))
    }

    private fun flash(newColors: IntArray) {
        revertRunnable?.let { removeCallbacks(it) }
        colors = newColors
        if (width > 0 && height > 0) {
            shader = SweepGradient(width / 2f, height / 2f, colors, null)
        }
        invalidate()
        val revert = Runnable {
            colors = DEFAULT_COLORS.copyOf()
            if (width > 0 && height > 0) {
                shader = SweepGradient(width / 2f, height / 2f, colors, null)
            }
            invalidate()
        }
        revertRunnable = revert
        postDelayed(revert, 1100)
    }

    private fun dp(v: Float): Float = v * resources.displayMetrics.density

    companion object {
        private val DEFAULT_COLORS = intArrayOf(
            0xFF00E5FF.toInt(),
            0xFF2979FF.toInt(),
            0xFFB388FF.toInt(),
            0xFFF500FF.toInt(),
            0xFF00E5FF.toInt()
        )
    }
}
