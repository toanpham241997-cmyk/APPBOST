package com.havanhuan.booster

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.SweepGradient
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.View
import android.view.animation.DecelerateInterpolator
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * Đồng hồ đo điểm tối ưu kiểu game booster:
 * - Vòng track tối + vòng cung gradient neon theo tiến độ
 * - 2 vòng nét đứt trang trí quay ngược chiều nhau (cảm giác 3D động)
 * - Số phần trăm + nhãn "ĐIỂM TỐI ƯU" ở giữa
 */
class GaugeView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    var currentProgress: Float = 0f
        private set

    private val stroke = dp(13f)

    private val trackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = stroke
        color = 0xFF1C2748.toInt()
        strokeCap = Paint.Cap.ROUND
    }

    private val arcPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = stroke
        strokeCap = Paint.Cap.ROUND
    }

    private val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = stroke * 2.4f
        strokeCap = Paint.Cap.ROUND
        alpha = 60
    }

    private val dashOuter = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = dp(2f)
        color = 0x2E00E5FF
    }

    private val dashInner = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = dp(2f)
        color = 0x2EB388FF
    }

    private val valuePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
    }

    private val unitPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xCCFFFFFF.toInt()
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
    }

    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF8A94B8.toInt()
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
    }

    private var gradient: SweepGradient? = null
    private var animator: ValueAnimator? = null

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val cx = w / 2f
        val cy = h / 2f
        gradient = SweepGradient(
            cx, cy,
            intArrayOf(
                0xFF00E5FF.toInt(),
                0xFF2979FF.toInt(),
                0xFFB388FF.toInt(),
                0xFFF500FF.toInt(),
                0xFF00E5FF.toInt()
            ),
            floatArrayOf(0f, 0.28f, 0.55f, 0.8f, 1f)
        ).also {
            val m = Matrix()
            m.postRotate(-90f, cx, cy)
            it.setLocalMatrix(m)
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val cx = width / 2f
        val cy = height / 2f
        val half = min(width, height) / 2f
        val radius = half - stroke - dp(10f)

        val t = (System.currentTimeMillis() % 7000L) / 7000f

        // Vòng nét đứt ngoài quay thuận
        canvas.save()
        canvas.rotate(t * 360f, cx, cy)
        dashOuter.pathEffect = DashPathEffect(floatArrayOf(dp(5f), dp(11f)), 0f)
        canvas.drawCircle(cx, cy, radius + stroke + dp(4f), dashOuter)
        canvas.restore()

        // Vòng nét đứt trong quay ngược
        canvas.save()
        canvas.rotate(-t * 480f, cx, cy)
        dashInner.pathEffect = DashPathEffect(floatArrayOf(dp(3f), dp(9f)), 0f)
        canvas.drawCircle(cx, cy, radius - stroke * 1.4f, dashInner)
        canvas.restore()

        // Track nền + cung tiến độ
        val rect = RectF(cx - radius, cy - radius, cx + radius, cy + radius)
        canvas.drawArc(rect, 0f, 360f, false, trackPaint)

        gradient?.let {
            arcPaint.shader = it
            glowPaint.shader = it
            val sweep = 360f * currentProgress / 100f
            if (sweep > 0f) {
                canvas.drawArc(rect, -90f, sweep, false, glowPaint)
                canvas.drawArc(rect, -90f, sweep, false, arcPaint)
            }
        }

        // Chữ ở giữa: số lớn + dấu % nhỏ
        valuePaint.textSize = half * 0.30f
        unitPaint.textSize = half * 0.13f
        labelPaint.textSize = half * 0.105f

        val num = currentProgress.roundToInt().toString()
        val numW = valuePaint.measureText(num)
        val unitW = unitPaint.measureText("%")
        val total = numW + unitW + dp(2f)
        val baseY = cy + valuePaint.textSize * 0.32f

        canvas.drawText(num, cx - total / 2f + numW / 2f, baseY, valuePaint)
        canvas.drawText("%", cx + total / 2f - unitW / 2f, baseY, unitPaint)
        canvas.drawText(context.getString(R.string.gauge_label), cx, cy + half * 0.36f, labelPaint)
    }

    /** Chạy mượt chỉ số tới giá trị mục tiêu. */
    fun animateTo(target: Float, duration: Long = 1200L) {
        animator?.cancel()
        animator = ValueAnimator.ofFloat(currentProgress, target.coerceIn(0f, 100f)).apply {
            this.duration = duration
            interpolator = DecelerateInterpolator()
            addUpdateListener { a ->
                currentProgress = a.animatedValue as Float
                invalidate()
            }
            start()
        }
    }

    private fun dp(v: Float): Float = v * resources.displayMetrics.density
}
