package com.example.wifilocalization

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import kotlin.math.max
import kotlin.math.min

data class DrawAnchor(
    val name: String,
    val x: Double,
    val y: Double,
    val distance: Double?
)

class LocalizationView(context: Context, attrs: AttributeSet?) : View(context, attrs) {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    var anchors: List<DrawAnchor> = emptyList()
    var position: Pair<Double, Double>? = null

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawColor(Color.WHITE)
        if (anchors.isEmpty()) return

        val margin = 55f
        var maxX = max(10.0, anchors.maxOf { it.x } + 2.0)
        var maxY = max(8.0, anchors.maxOf { it.y } + 2.0)

        position?.let {
            maxX = max(maxX, it.first + 2.0)
            maxY = max(maxY, it.second + 2.0)
        }

        val scale = min(
            (width - 2 * margin) / maxX.toFloat(),
            (height - 2 * margin) / maxY.toFloat()
        )

        fun screenX(x: Double) = margin + x.toFloat() * scale
        fun screenY(y: Double) = height - margin - y.toFloat() * scale

        paint.color = 0xffe0e0e0.toInt()
        paint.strokeWidth = 1f
        paint.style = Paint.Style.STROKE

        for (i in 0..maxX.toInt()) {
            canvas.drawLine(screenX(i.toDouble()), margin, screenX(i.toDouble()), height - margin, paint)
        }
        for (i in 0..maxY.toInt()) {
            canvas.drawLine(margin, screenY(i.toDouble()), width - margin, screenY(i.toDouble()), paint)
        }

        anchors.forEach { anchor ->
            anchor.distance?.let { distance ->
                paint.style = Paint.Style.STROKE
                paint.color = 0xff78909c.toInt()
                paint.strokeWidth = 3f
                canvas.drawCircle(screenX(anchor.x), screenY(anchor.y), distance.toFloat() * scale, paint)
            }

            paint.style = Paint.Style.FILL
            paint.color = 0xff263238.toInt()
            canvas.drawCircle(screenX(anchor.x), screenY(anchor.y), 10f, paint)
            paint.textSize = 28f
            canvas.drawText(
                "${anchor.name} (${anchor.x}, ${anchor.y})",
                screenX(anchor.x) + 13f,
                screenY(anchor.y) - 12f,
                paint
            )
        }

        position?.let {
            paint.style = Paint.Style.FILL
            paint.color = 0xffd32f2f.toInt()
            canvas.drawCircle(screenX(it.first), screenY(it.second), 13f, paint)
            paint.textSize = 30f
            canvas.drawText("Phone", screenX(it.first) + 15f, screenY(it.second), paint)
        }
    }
}
