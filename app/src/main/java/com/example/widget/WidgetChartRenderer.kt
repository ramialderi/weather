package com.example.widget

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Shader
import com.example.data.local.DailyForecastEntity

object WidgetChartRenderer {

    /**
     * Generates a sleek weekly chart bitmap showing:
     * 1. Filled precipitation (rain) mountain peaks / waves across the week (in cyan/blue).
     * 2. High temperature trend line (in warm yellow/orange).
     * 3. Low temperature trend line (in cool light blue).
     *
     * RTL ALIGNED: Day 0 (اليوم) is positioned on the far RIGHT, and Day 6 (يوم 7)
     * is positioned on the far LEFT to perfectly match the Arabic RTL day columns above it!
     */
    fun createWeeklyChartBitmap(
        forecastList: List<DailyForecastEntity>,
        width: Int = 480,
        height: Int = 110
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        if (forecastList.isEmpty()) {
            return bitmap
        }

        val count = forecastList.size
        val paddingTop = 12f
        val paddingBottom = 12f
        val chartHeight = height - paddingTop - paddingBottom
        val colWidth = width.toFloat() / count

        // In RTL: day 0 is on the far right, day count-1 is on the far left.
        // This maps each day i to the exact center of its column.
        fun getXForDay(i: Int): Float = width - (i + 0.5f) * colWidth

        // Find max rain to scale precipitation peaks
        val maxRain = forecastList.maxOfOrNull { it.precipitationSum }?.toFloat()?.coerceAtLeast(1.0f) ?: 5f
        val effectiveMaxRain = maxRain.coerceAtLeast(3f)

        // Find min and max temp
        val minTemp = forecastList.minOfOrNull { it.tempMin }?.toFloat() ?: 10f
        val maxTemp = forecastList.maxOfOrNull { it.tempMax }?.toFloat() ?: 35f
        val tempSpan = (maxTemp - minTemp).coerceAtLeast(2f)

        // Draw subtle horizontal reference lines
        val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(35, 255, 255, 255)
            strokeWidth = 1.2f
            style = Paint.Style.STROKE
        }
        canvas.drawLine(0f, height - paddingBottom, width.toFloat(), height - paddingBottom, gridPaint)
        canvas.drawLine(0f, height - paddingBottom - chartHeight * 0.5f, width.toFloat(), height - paddingBottom - chartHeight * 0.5f, gridPaint)

        // 1. Compute Rain points ordered from Left to Right (day count-1 down to 0)
        // so that the path connects smoothly from left (x=0) to right (x=width)
        val rainPoints = ArrayList<Pair<Float, Float>>()
        for (i in (count - 1) downTo 0) {
            val x = getXForDay(i)
            val rain = forecastList[i].precipitationSum.toFloat()
            val rainRatio = (rain / effectiveMaxRain).coerceIn(0f, 1f)
            val y = (height - paddingBottom) - (rainRatio * (chartHeight * 0.72f))
            rainPoints.add(Pair(x, y))
        }

        if (rainPoints.isNotEmpty()) {
            val rainFillPath = Path()
            val rainStrokePath = Path()

            val firstPoint = rainPoints[0]
            rainFillPath.moveTo(0f, height - paddingBottom)
            rainFillPath.lineTo(firstPoint.first, firstPoint.second)
            rainStrokePath.moveTo(firstPoint.first, firstPoint.second)

            for (k in 0 until rainPoints.size - 1) {
                val p0 = rainPoints[k]
                val p1 = rainPoints[k + 1]
                val midX = (p0.first + p1.first) / 2f
                rainFillPath.cubicTo(midX, p0.second, midX, p1.second, p1.first, p1.second)
                rainStrokePath.cubicTo(midX, p0.second, midX, p1.second, p1.first, p1.second)
            }

            val lastPoint = rainPoints.last()
            rainFillPath.lineTo(width.toFloat(), lastPoint.second)
            rainFillPath.lineTo(width.toFloat(), height - paddingBottom)
            rainFillPath.close()

            // Paint for rain filled gradient (Cyan -> Blue -> Transparent)
            val rainFillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.FILL
                shader = LinearGradient(
                    0f, paddingTop, 0f, height - paddingBottom,
                    intArrayOf(
                        Color.argb(160, 56, 189, 248), // #38BDF8
                        Color.argb(90, 2, 132, 199),  // #0284C7
                        Color.argb(20, 15, 23, 42)
                    ),
                    floatArrayOf(0f, 0.6f, 1f),
                    Shader.TileMode.CLAMP
                )
            }
            canvas.drawPath(rainFillPath, rainFillPaint)

            // Rain stroke outline
            val rainStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                strokeWidth = 2.5f
                color = Color.argb(220, 56, 189, 248)
            }
            canvas.drawPath(rainStrokePath, rainStrokePaint)
        }

        // 2. Draw Max Temperature Line (Warm Yellow/Orange Curve from Left to Right)
        val tempMaxPoints = ArrayList<Pair<Float, Float>>()
        for (i in (count - 1) downTo 0) {
            val x = getXForDay(i)
            val tMax = forecastList[i].tempMax.toFloat()
            val ratio = (tMax - minTemp) / tempSpan
            val y = paddingTop + (1f - ratio) * (chartHeight * 0.45f)
            tempMaxPoints.add(Pair(x, y))
        }

        if (tempMaxPoints.isNotEmpty()) {
            val maxTempPath = Path()
            maxTempPath.moveTo(tempMaxPoints[0].first, tempMaxPoints[0].second)
            for (k in 0 until tempMaxPoints.size - 1) {
                val p0 = tempMaxPoints[k]
                val p1 = tempMaxPoints[k + 1]
                val midX = (p0.first + p1.first) / 2f
                maxTempPath.cubicTo(midX, p0.second, midX, p1.second, p1.first, p1.second)
            }

            val maxTempPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                strokeWidth = 3f
                shader = LinearGradient(
                    0f, 0f, width.toFloat(), 0f,
                    intArrayOf(
                        Color.rgb(234, 179, 8),
                        Color.rgb(249, 115, 22),
                        Color.rgb(251, 191, 36) // #FBBF24
                    ),
                    null,
                    Shader.TileMode.CLAMP
                )
            }
            canvas.drawPath(maxTempPath, maxTempPaint)

            // Draw small dot on each day point
            val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.FILL
                color = Color.rgb(251, 191, 36)
            }
            for (pt in tempMaxPoints) {
                canvas.drawCircle(pt.first, pt.second, 3.5f, dotPaint)
            }
        }

        // 3. Draw Min Temperature Line (Cool Cyan / Blue Curve from Left to Right)
        val tempMinPoints = ArrayList<Pair<Float, Float>>()
        for (i in (count - 1) downTo 0) {
            val x = getXForDay(i)
            val tMin = forecastList[i].tempMin.toFloat()
            val ratio = (tMin - minTemp) / tempSpan
            val y = (paddingTop + chartHeight * 0.45f) + (1f - ratio) * (chartHeight * 0.4f)
            tempMinPoints.add(Pair(x, y))
        }

        if (tempMinPoints.isNotEmpty()) {
            val minTempPath = Path()
            minTempPath.moveTo(tempMinPoints[0].first, tempMinPoints[0].second)
            for (k in 0 until tempMinPoints.size - 1) {
                val p0 = tempMinPoints[k]
                val p1 = tempMinPoints[k + 1]
                val midX = (p0.first + p1.first) / 2f
                minTempPath.cubicTo(midX, p0.second, midX, p1.second, p1.first, p1.second)
            }

            val minTempPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                strokeWidth = 2.5f
                color = Color.argb(180, 147, 197, 253) // Light Blue
            }
            canvas.drawPath(minTempPath, minTempPaint)

            val minDotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.FILL
                color = Color.rgb(147, 197, 253)
            }
            for (pt in tempMinPoints) {
                canvas.drawCircle(pt.first, pt.second, 2.8f, minDotPaint)
            }
        }

        return bitmap
    }
}
