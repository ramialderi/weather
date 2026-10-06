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
     * Matches the Pflotsh SuperHD week style requested in the screenshot.
     */
    fun createWeeklyChartBitmap(
        forecastList: List<DailyForecastEntity>,
        width: Int = 640,
        height: Int = 140
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        if (forecastList.isEmpty()) {
            return bitmap
        }

        val count = forecastList.size
        val paddingLeft = 24f
        val paddingRight = 24f
        val paddingTop = 16f
        val paddingBottom = 16f

        val chartWidth = width - paddingLeft - paddingRight
        val chartHeight = height - paddingTop - paddingBottom

        // Compute step between day centers
        val stepX = if (count > 1) chartWidth / (count - 1) else chartWidth

        // Find max rain to scale precipitation peaks
        val maxRain = forecastList.maxOfOrNull { it.precipitationSum }?.toFloat()?.coerceAtLeast(1.0f) ?: 5f
        val effectiveMaxRain = maxRain.coerceAtLeast(3f)

        // Find min and max temp
        val minTemp = forecastList.minOfOrNull { it.tempMin }?.toFloat() ?: 10f
        val maxTemp = forecastList.maxOfOrNull { it.tempMax }?.toFloat() ?: 35f
        val tempSpan = (maxTemp - minTemp).coerceAtLeast(2f)

        // Draw subtle horizontal reference lines
        val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(40, 255, 255, 255)
            strokeWidth = 1.5f
            style = Paint.Style.STROKE
        }
        canvas.drawLine(paddingLeft, height - paddingBottom, width - paddingRight, height - paddingBottom, gridPaint)
        canvas.drawLine(paddingLeft, height - paddingBottom - chartHeight * 0.5f, width - paddingRight, height - paddingBottom - chartHeight * 0.5f, gridPaint)

        // 1. Draw Precipitation (Rain) filled area / wave
        val rainFillPath = Path()
        val rainStrokePath = Path()

        val rainPoints = ArrayList<Pair<Float, Float>>()
        for (i in 0 until count) {
            val x = paddingLeft + i * stepX
            val rain = forecastList[i].precipitationSum.toFloat()
            // Height ratio based on rainfall amount
            val rainRatio = (rain / effectiveMaxRain).coerceIn(0f, 1f)
            // Rain wave sits in bottom 75% of chart
            val y = (height - paddingBottom) - (rainRatio * (chartHeight * 0.75f))
            rainPoints.add(Pair(x, y))
        }

        if (rainPoints.isNotEmpty()) {
            rainFillPath.moveTo(paddingLeft, height - paddingBottom)
            rainFillPath.lineTo(rainPoints[0].first, rainPoints[0].second)
            rainStrokePath.moveTo(rainPoints[0].first, rainPoints[0].second)

            for (i in 0 until rainPoints.size - 1) {
                val p0 = rainPoints[i]
                val p1 = rainPoints[i + 1]
                val midX = (p0.first + p1.first) / 2f
                rainFillPath.cubicTo(midX, p0.second, midX, p1.second, p1.first, p1.second)
                rainStrokePath.cubicTo(midX, p0.second, midX, p1.second, p1.first, p1.second)
            }

            rainFillPath.lineTo(width - paddingRight, height - paddingBottom)
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

        // 2. Draw Max Temperature Line (Warm Yellow/Orange Curve)
        val tempMaxPoints = ArrayList<Pair<Float, Float>>()
        for (i in 0 until count) {
            val x = paddingLeft + i * stepX
            val tMax = forecastList[i].tempMax.toFloat()
            val ratio = (tMax - minTemp) / tempSpan
            val y = paddingTop + (1f - ratio) * (chartHeight * 0.45f)
            tempMaxPoints.add(Pair(x, y))
        }

        if (tempMaxPoints.isNotEmpty()) {
            val maxTempPath = Path()
            maxTempPath.moveTo(tempMaxPoints[0].first, tempMaxPoints[0].second)
            for (i in 0 until tempMaxPoints.size - 1) {
                val p0 = tempMaxPoints[i]
                val p1 = tempMaxPoints[i + 1]
                val midX = (p0.first + p1.first) / 2f
                maxTempPath.cubicTo(midX, p0.second, midX, p1.second, p1.first, p1.second)
            }

            val maxTempPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                strokeWidth = 3f
                shader = LinearGradient(
                    paddingLeft, 0f, width - paddingRight, 0f,
                    intArrayOf(
                        Color.rgb(251, 191, 36), // #FBBF24
                        Color.rgb(249, 115, 22), // #F97316
                        Color.rgb(234, 179, 8)
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

        // 3. Draw Min Temperature Line (Cool Cyan / Blue Curve)
        val tempMinPoints = ArrayList<Pair<Float, Float>>()
        for (i in 0 until count) {
            val x = paddingLeft + i * stepX
            val tMin = forecastList[i].tempMin.toFloat()
            val ratio = (tMin - minTemp) / tempSpan
            val y = (paddingTop + chartHeight * 0.45f) + (1f - ratio) * (chartHeight * 0.4f)
            tempMinPoints.add(Pair(x, y))
        }

        if (tempMinPoints.isNotEmpty()) {
            val minTempPath = Path()
            minTempPath.moveTo(tempMinPoints[0].first, tempMinPoints[0].second)
            for (i in 0 until tempMinPoints.size - 1) {
                val p0 = tempMinPoints[i]
                val p1 = tempMinPoints[i + 1]
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
