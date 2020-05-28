/*
 * MIT License
 *
 * Copyright (c) 2019 Nikola Jakšić
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.nikola.jakshic.spiderchart

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import java.lang.Math.*

class SpiderChart @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val DEFAULT_LABEL_SIZE = 12F // in scaled pixels
    private val DEFAULT_LABEL_COLOR = Color.DKGRAY
    private val DEFAULT_LABEL_MARGIN_SIZE = 8F // in density pixels
    private val DEFAULT_WEB_COLOR = Color.LTGRAY
    private val DEFAULT_WEB_BACKGROUND_COLOR = Color.TRANSPARENT
    private val DEFAULT_WEB_STROKE_WIDTH = 0.5F // in density pixels
    private val DEFAULT_WEB_EDGE_COLOR = Color.DKGRAY
    private val DEFAULT_WEB_EDGE_STROKE_WIDTH = 0.8F // in density pixels
    private val DEFAULT_ROTATION_ANGLE = 90F // in degrees

    private val dataPath = Path()
    private val backgroundPath = Path()

    private val dataPaint = Paint()
    private val backgroundPaint = Paint()
    private val edgePaint = Paint()
    private val webPaint = Paint()
    private val labelPaint = Paint()

    // Used to calculate the height of the text
    private val textBounds = Rect()

    private var data = mutableListOf<SpiderData>()
    private val labels = mutableListOf<String>()

    private var labelMarginSize = 0f // in density pixels
    private var rotationAngle = 0f // in degrees

    private var drawWeb: Boolean = true
    private var drawLabels: Boolean = true

    private var maxLabelWidth = 0f
    private var maxLabelHeight = 0f

    private var edgeCount = 0

    // Points that are used to draw the lines for polygon/web. Single line (edge) has 4 points,
    // start and end point for the X-axis, start and end point for the Y-axis.
    private var points = FloatArray(edgeCount * 4)

    init {
        webPaint.isAntiAlias = true
        webPaint.style = Paint.Style.STROKE
        webPaint.color = DEFAULT_WEB_COLOR
        webPaint.strokeWidth = dpToPixels(DEFAULT_WEB_STROKE_WIDTH)

        edgePaint.isAntiAlias = true
        edgePaint.style = Paint.Style.STROKE
        edgePaint.color = DEFAULT_WEB_EDGE_COLOR
        edgePaint.strokeWidth = dpToPixels(DEFAULT_WEB_EDGE_STROKE_WIDTH)

        backgroundPaint.isAntiAlias = true
        backgroundPaint.style = Paint.Style.FILL
        backgroundPaint.color = DEFAULT_WEB_BACKGROUND_COLOR

        labelPaint.isAntiAlias = true
        labelPaint.color = DEFAULT_LABEL_COLOR
        labelPaint.textSize = spToPixels(DEFAULT_LABEL_SIZE)

        dataPaint.isAntiAlias = true
        dataPaint.style = Paint.Style.FILL

        labelMarginSize = DEFAULT_LABEL_MARGIN_SIZE
        rotationAngle = DEFAULT_ROTATION_ANGLE

        if (isInEditMode) {
            // Fake data for the layout editor
            val labels = listOf("ART0", "ART1", "ART2", "ART3", "ART4", "ART5")

            val values1 = floatArrayOf(45f, 82f, 76f, 55f, 55f, 55f)
            val values2 = floatArrayOf(85f, 72f, 41f, 75f, 75f, 75f)

            val color1 = Color.argb(125, 193, 230, 219)
            val color2 = Color.argb(125, 209, 217, 234)

            val data1 = SpiderData(values1, color1)
            val data2 = SpiderData(values2, color2)

            setLabels(labels)
            setData(listOf(data1, data2))
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val height = MeasureSpec.getSize(heightMeasureSpec)
        maxLabelWidth = getMaxLabelWidth() + labelMarginSize
        maxLabelHeight = getMaxLabelHeight() + labelMarginSize
        val radius = min((width - maxLabelWidth * 2) / 2f, (height - maxLabelHeight * 2) / 2f)
        val desiredHeight = 2 * radius + 2 * maxLabelHeight
        val desiredWidth = 2 * radius + 2 * maxLabelWidth
        val measuredWidth = resolveSize(desiredWidth.toInt(), widthMeasureSpec)
        val measuredHeight = resolveSize(desiredHeight.toInt(), heightMeasureSpec)
        setMeasuredDimension(measuredWidth, measuredHeight)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (edgeCount == 0) return

        val radius = min((width - maxLabelWidth * 2) / 2f, (height - maxLabelHeight * 2) / 2f)

        drawWebBackground(canvas, backgroundPath, backgroundPaint, radius)
        if (drawWeb) drawWeb(canvas, webPaint, radius)
        drawPolygon(canvas, edgePaint, radius)
        drawData(canvas, dataPath, dataPaint, radius)
        if (drawLabels) drawLabels(canvas, labelPaint, radius)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        resetPaths()
    }

    private fun drawPolygon(canvas: Canvas, paint: Paint, radiusLength: Float) {
        val centerX = width / 2f
        val centerY = height / 2f

        for (i in 0..edgeCount) {
            val angle = i * (360.0 / edgeCount) - rotationAngle

            val x = centerX + (radiusLength) * (cos(toRadians(angle))).toFloat()
            val y = centerY + (radiusLength) * (sin(toRadians(angle))).toFloat()

            if (i == 0) {
                points[i] = x // start point
                points[i + 1] = y // start point
            } else {
                points[i * 4 - 2] = x // end point
                points[i * 4 - 1] = y // end point

                // If we have reached to the last edge, no need to save the starting points for the next one.
                if (i != edgeCount) {
                    // Next point starts where the last one ended.
                    points[i * 4] = x // next start point
                    points[i * 4 + 1] = y // next start point
                }
            }
        }
        canvas.drawLines(points, paint)
    }

    private fun drawData(canvas: Canvas, path: Path, paint: Paint, radiusLength: Float) {
        val centerX = width / 2f
        val centerY = height / 2f

        for (entry in data) {
            val values = entry.values
            val color = entry.color

            paint.color = color
            path.reset() // reset path from the previous entry

            for (i in 0 until values.size) {
                val angle = i * (360.0 / edgeCount) - rotationAngle

                val value = values[i]

                val x = centerX + (radiusLength * (value / 100)) * (cos(toRadians(angle))).toFloat()
                val y = centerY + (radiusLength * (value / 100)) * (sin(toRadians(angle))).toFloat()

                if (i == 0) {
                    path.moveTo(x, y)
                } else {
                    path.lineTo(x, y)
                }
            }
            canvas.drawPath(path, paint)
        }
    }

    private fun drawWeb(canvas: Canvas, paint: Paint, radiusLength: Float) {
        fun drawRadii(canvas: Canvas, paint: Paint, radiusLength: Float) {
            val centerX = width / 2f
            val centerY = height / 2f

            for (i in 0 until edgeCount) {
                val angle = i * (360.0 / edgeCount) - rotationAngle

                val x = centerX + (radiusLength) * (cos(toRadians(angle))).toFloat()
                val y = centerY + (radiusLength) * (sin(toRadians(angle))).toFloat()

                points[i * 4] = x
                points[i * 4 + 1] = y
                points[i * 4 + 2] = centerX
                points[i * 4 + 3] = centerY
            }
            canvas.drawLines(points, paint)
        }

        for (i in 0 until edgeCount) {
            if (i == 0) {
                drawRadii(canvas, paint, radiusLength - i * (radiusLength / edgeCount))
            } else {
                drawPolygon(canvas, paint, radiusLength - i * (radiusLength / edgeCount))
            }
        }
    }

    private fun drawWebBackground(canvas: Canvas, path: Path, paint: Paint, radiusLength: Float) {
        val centerX = width / 2f
        val centerY = height / 2f

        for (i in 0..edgeCount) {
            val angle = i * (360.0 / edgeCount) - rotationAngle

            val x = centerX + (radiusLength) * (cos(toRadians(angle))).toFloat()
            val y = centerY + (radiusLength) * (sin(toRadians(angle))).toFloat()

            if (i == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
        }
        canvas.drawPath(path, paint)
    }

    private fun drawLabels(canvas: Canvas, paint: Paint, radiusLength: Float) {
        val centerX = width / 2f
        val centerY = height / 2f

        for (i in 0 until labels.size) {
            val angle = i * (360.0 / edgeCount) - rotationAngle

            val x = centerX + (radiusLength) * (cos(toRadians(angle))).toFloat()
            val y = centerY + (radiusLength) * (sin(toRadians(angle))).toFloat()

            var textX = x
            var textY = y

            val textHeight = paint.measureTextHeight(labels[i])

            if (x == centerX) {
                paint.textAlign = Paint.Align.CENTER
            }
            if (x < centerX) {
                textX = x - labelMarginSize
                paint.textAlign = Paint.Align.RIGHT
            }
            if (x > centerX) {
                textX = x + labelMarginSize
                paint.textAlign = Paint.Align.LEFT
            }
            if (y == centerY) {
                textY = y + textHeight / 2
                if (x < centerX) {
                    textX = x - labelMarginSize
                }
                if (x > centerX) {
                    textX = x + labelMarginSize
                }
            }
            if (y > centerY) {
                textY = y + textHeight + labelMarginSize
            }
            if (y < centerY) {
                textY = y - labelMarginSize
            }

            canvas.drawText(labels[i], textX, textY, paint)
        }
    }

    fun setData(data: List<SpiderData>) {
        if (data == null) {
            throw IllegalArgumentException("List<SpiderData> must not be null.")
        }
        var size = 0
        for (i in 0 until data.size) {
            if (data[i] == null) {
                throw IllegalArgumentException("SpiderData must not contain null values.")
            }
            if (i == 0) {
                size = data[i].values.size
            } else {
                if (size != data[i].values.size) {
                    throw IllegalArgumentException("All SpiderData.values must be the same size.")
                }
            }
        }

        this.data.clear()
        this.data.addAll(data)

        if (data.isNotEmpty()) {
            edgeCount = data[0].values.size
            points = FloatArray(edgeCount * 4)
        }
    }

    fun getData() = data

    fun setLabels(labels: List<String>) {
        if (labels == null) {
            throw IllegalArgumentException("List<String> must not be null.")
        }
        for (label in labels) {
            if (label == null) {
                throw IllegalArgumentException("List<String> must not contain null values.")
            }
        }

        this.labels.clear()
        this.labels.addAll(labels)
    }

    fun getLabels() = labels

    /**
     * Sets the size of the label to the given value.
     * The default value is [DEFAULT_LABEL_SIZE].
     *
     * @param size The scaled pixel size.
     */
    fun setLabelSize(size: Float) {
        labelPaint.textSize = spToPixels(size)
    }

    /** Returns the size of the label in scaled pixels. */
    fun getLabelSize() = pixelsToSp(labelPaint.textSize)

    /**
     * Sets the color of the label.
     *
     * @param color A color value in the form 0xAARRGGBB.
     */
    fun setLabelColor(color: Int) {
        labelPaint.color = color
    }

    /** Returns the color of the label. */
    fun getLabelColor() = labelPaint.color

    /**
     * Sets the margin size of the label to the given value.
     * The default value is [DEFAULT_LABEL_MARGIN_SIZE].
     *
     * @param size The density pixel size.
     */
    fun setLabelMarginSize(size: Float) {
        labelMarginSize = dpToPixels(size)
    }

    /** Returns the margin size of the label in density pixels. */
    fun getLabelMarginSize() = pixelsToDp(labelMarginSize)

    /**
     * Sets the color of the web.
     *
     * @param color A color value in the form 0xAARRGGBB.
     */
    fun setWebColor(color: Int) {
        webPaint.color = color
    }

    /** Returns the color of the web. */
    fun getWebColor() = webPaint.color

    /**
     * Sets the background color of the web.
     *
     * @param color A color value in the form 0xAARRGGBB.
     */
    fun setWebBackgroundColor(color: Int) {
        backgroundPaint.color = color
    }

    /** Returns the background color of the web. */
    fun getWebBackgroundColor() = backgroundPaint.color

    /**
     * Sets the stroke width of the web to the given value.
     * The default value is [DEFAULT_WEB_STROKE_WIDTH].
     *
     * @param width The density pixel size.
     */
    fun setWebStrokeWidth(width: Float) {
        webPaint.strokeWidth = dpToPixels(width)
    }

    /** Returns the stroke width of the web in density pixels. */
    fun getWebStrokeWidth() = pixelsToDp(webPaint.strokeWidth)

    /**
     * Sets the color of the web edges.
     *
     * @param color A color value in the form 0xAARRGGBB.
     */
    fun setWebEdgeColor(color: Int) {
        edgePaint.color = color
    }

    /** Returns the color of the web edge. */
    fun getWebEdgeColor() = edgePaint.color

    /**
     * Sets the web stroke width to the given value.
     * The default value is [DEFAULT_WEB_STROKE_WIDTH].
     *
     * @param width The density pixel size.
     */
    fun setWebEdgeStrokeWidth(width: Float) {
        edgePaint.strokeWidth = dpToPixels(width)
    }

    /** Returns the stroke width of the web edge in density pixels. */
    fun getWebEdgeStrokeWidth() = pixelsToDp(edgePaint.strokeWidth)

    /**
     * Sets the rotation angle.
     *
     * @param value Angle value in degrees.
     */
    fun setRotationAngle(value: Float) {
        rotationAngle = value
    }

    /** Returns the rotation angle in degrees. */
    fun getRotationAngle() = rotationAngle

    /**
     * Sets whether the web should be drawn or not.
     * The default value is true.
     */
    fun setDrawWeb(enabled: Boolean) {
        drawWeb = enabled
    }

    /** Returns whether the web will be drawn or not. */
    fun getDrawWeb() = drawWeb

    /**
     * Sets whether the labels should be drawn or not.
     * The default value is true.
     */
    fun setDrawLabels(enabled: Boolean) {
        drawLabels = enabled
    }

    /** Returns whether the labels will be drawn or not. */
    fun getDrawLabels() = drawLabels

    fun refresh() {
        resetPaths()
        requestLayout() // request re-measurement for labels change
        invalidate()
    }

    /** Resets all the paths. */
    private fun resetPaths() {
        dataPath.reset()
        backgroundPath.reset()
    }

    /** Returns the maximum width of the labels. */
    private fun getMaxLabelWidth(): Float {
        var maxWidth = 0f
        for (label in labels) {
            val width = labelPaint.measureTextWidth(label)
            if (maxWidth < width) {
                maxWidth = width
            }
        }
        return maxWidth
    }

    /** Returns the maximum height of the labels. */
    private fun getMaxLabelHeight(): Float {
        var maxHeight = 0f
        for (label in labels) {
            val height = labelPaint.measureTextHeight(label)
            if (maxHeight < height) {
                maxHeight = height
            }
        }
        return maxHeight
    }

    /**
     * Returns the width of the [text].
     * This is just a convenience function for [Paint.measureText].
     */
    private fun Paint.measureTextWidth(text: String): Float {
        return measureText(text)
    }

    /** Returns the height of the [text]. */
    private fun Paint.measureTextHeight(text: String): Float {
        getTextBounds(text, 0, text.length, textBounds)
        return textBounds.height().toFloat()
    }

    /** Converts density pixels to pixels. */
    private fun dpToPixels(dp: Float): Float {
        return dp * resources.displayMetrics.density
    }

    /** Converts pixels to density pixels. */
    private fun pixelsToDp(pixels: Float): Float {
        return pixels / resources.displayMetrics.density
    }

    /** Converts scaled pixels to pixels. */
    private fun spToPixels(sp: Float): Float {
        return sp * resources.displayMetrics.scaledDensity
    }

    /** Converts pixels to scaled pixels. */
    private fun pixelsToSp(pixels: Float): Float {
        return pixels / resources.displayMetrics.scaledDensity
    }
}