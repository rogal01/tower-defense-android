package com.example.myapp.game

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatButton

/**
 * Floating Hero Power Action Orb with glowing elemental halos and circular cooldown sweeps.
 */
class HeroPowerOrbView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatButton(context, attrs, defStyleAttr) {

    var haloColor: Int = 0xFFFF7043.toInt()
        private set
    var powerEmoji: String = ""
        private set
    var remainingCooldown: Float = 0f
        private set
    var totalCooldown: Float = 1f
        private set

    private val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.parseColor("#E60D1520")
    }

    private val haloBloomPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 6f
    }

    private val haloInnerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 2.5f
    }

    private val sweepPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.parseColor("#B30D1520")
    }

    private val leadSweepPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 3f
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        color = Color.WHITE
        isFakeBoldText = true
    }

    private val boundsRect = RectF()

    init {
        // Clear standard button background to render custom obsidian arcade orb
        background = null
    }

    fun setPowerState(emoji: String, remainingCd: Float, totalCd: Float, haloColor: Int) {
        this.powerEmoji = emoji
        this.remainingCooldown = remainingCd
        this.totalCooldown = if (totalCd > 0f) totalCd else 1f
        this.haloColor = haloColor
        this.text = if (remainingCd > 0f) "${remainingCd.toInt() + 1}" else emoji
        this.alpha = if (remainingCd > 0f) 0.55f else 1.0f
        postInvalidate()
    }

    override fun onDraw(canvas: Canvas) {
        val w = width.toFloat()
        val h = height.toFloat()
        if (w <= 0f || h <= 0f) {
            super.onDraw(canvas)
            return
        }

        val cx = w / 2f
        val cy = h / 2f
        val radius = (minOf(w, h) / 2f) - 4f
        boundsRect.set(cx - radius, cy - radius, cx + radius, cy + radius)

        // 1. Glowing Elemental Halo (outer bloom + sharp inner ring)
        haloBloomPaint.color = (haloColor and 0x00FFFFFF) or 0x4D000000 // 30% alpha
        canvas.drawCircle(cx, cy, radius, haloBloomPaint)

        haloInnerPaint.color = haloColor
        canvas.drawCircle(cx, cy, radius, haloInnerPaint)

        // 2. Obsidian Glass Core
        canvas.drawCircle(cx, cy, radius - 1.5f, bgPaint)

        // 3. Circular Cooldown Sweep (radial dark arc + glowing leading edge)
        if (remainingCooldown > 0f) {
            val sweepAngle = (360f * (remainingCooldown / totalCooldown)).coerceIn(0f, 360f)
            canvas.drawArc(boundsRect, -90f, sweepAngle, true, sweepPaint)

            leadSweepPaint.color = haloColor
            val rad = Math.toRadians((-90f + sweepAngle).toDouble())
            val endX = cx + (radius - 1.5f) * Math.cos(rad).toFloat()
            val endY = cy + (radius - 1.5f) * Math.sin(rad).toFloat()
            canvas.drawLine(cx, cy, endX, endY, leadSweepPaint)
        }

        // 4. Centered Emoji or Countdown Overlay
        if (remainingCooldown > 0f) {
            textPaint.textSize = radius * 0.85f
            textPaint.color = Color.WHITE
            val cdText = "${remainingCooldown.toInt() + 1}s"
            val textY = cy - ((textPaint.descent() + textPaint.ascent()) / 2f)
            canvas.drawText(cdText, cx, textY, textPaint)
        } else if (powerEmoji.isNotEmpty()) {
            textPaint.textSize = radius * 0.95f
            val textY = cy - ((textPaint.descent() + textPaint.ascent()) / 2f)
            canvas.drawText(powerEmoji, cx, textY, textPaint)
        } else {
            super.onDraw(canvas)
        }
    }
}
