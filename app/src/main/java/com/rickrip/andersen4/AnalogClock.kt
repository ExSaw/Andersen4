package com.rickrip.andersen4

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import java.util.*
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

class AnalogClock @JvmOverloads constructor(
    context: Context, attrs: AttributeSet?, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var hours = 0.0f
    private var minutes = 0.0f
    private var seconds = 0.0f
    private var milliseconds = 0.0f

    private var widthCenter = 0.0f
    private var heightCenter = 0.0f
    private var radius = 0.0f
    private var centerDotRadius = 0.0f

    private var angle = 0.0f
    private var hoursArrowLength = 0.0f
    private var minutesArrowLength = 0.0f
    private var secondsArrowLength = 0.0f
    private var millisecondsArrowLength = 0.0f

    //attrs
    private var clockColor = Color.BLACK
    private var hoursArrowColor = Color.BLACK
    private var minutesArrowColor = Color.BLACK
    private var secondsArrowColor = Color.BLACK
    private var arrowsGeneralLength = 1.0f
    private var padding = 30.0f // for the distance between the screen edge and the clock
    private var showMillisecondsArrow = false
    private var tickInterval = 100

    init {
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.AnalogClock,
            0, 0
        ).apply {

            try {
                clockColor = getColor(R.styleable.AnalogClock_clockColor, Color.BLACK)
                hoursArrowColor = getColor(R.styleable.AnalogClock_hoursArrowColor, Color.BLACK)
                minutesArrowColor = getColor(R.styleable.AnalogClock_minutesArrowColor, Color.BLACK)
                secondsArrowColor = getColor(R.styleable.AnalogClock_secondsArrowColor, Color.BLACK)
                arrowsGeneralLength = getFloat(R.styleable.AnalogClock_arrowsLength, 1.0f)
                padding = getDimension(R.styleable.AnalogClock_clockPadding, 30.0f)
                showMillisecondsArrow =
                    getBoolean(R.styleable.AnalogClock_showMillisecondsArrow, false)
                tickInterval = getInteger(R.styleable.AnalogClock_tickInterval, 100)

            } finally {
                recycle()
            }
        }
    }

    private val paintCircle = Paint().apply {
        reset()
        color = clockColor
        style = Paint.Style.STROKE
        strokeWidth = context.resources.getDimensionPixelSize(R.dimen.circle_stroke_width).toFloat()
        isAntiAlias = true
    }

    private val paintCenterCircle = Paint().apply {
        reset()
        color = Color.GRAY
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    private val paintHoursLine = Paint().apply {
        reset()
        color = hoursArrowColor
        style = Paint.Style.STROKE
        strokeWidth = context.resources.getDimensionPixelSize(R.dimen.hour_stroke_width).toFloat()
        isAntiAlias = true
    }

    private val paintMinutesLine = Paint().apply {
        reset()
        color = minutesArrowColor
        style = Paint.Style.STROKE
        strokeWidth = context.resources.getDimensionPixelSize(R.dimen.minute_stroke_width).toFloat()
        isAntiAlias = true
    }

    private val paintSecondsLine = Paint().apply {
        reset()
        color = secondsArrowColor
        style = Paint.Style.STROKE
        strokeWidth = context.resources.getDimensionPixelSize(R.dimen.second_stroke_width).toFloat()
        isAntiAlias = true
    }

    private val paintMillisecondsLine = Paint().apply {
        reset()
        color = Color.GREEN
        style = Paint.Style.STROKE
        strokeWidth =
            context.resources.getDimensionPixelSize(R.dimen.milliSecond_stroke_width).toFloat()
        isAntiAlias = true
    }

    private val paintSegmentHoursLines = Paint().apply {
        reset()
        color = clockColor
        style = Paint.Style.STROKE
        strokeWidth =
            context.resources.getDimensionPixelSize(R.dimen.segment_hour_stroke_width).toFloat()
        isAntiAlias = true
    }

    private val paintSegmentMinutesLines = Paint().apply {
        reset()
        color = clockColor
        style = Paint.Style.STROKE
        strokeWidth =
            context.resources.getDimensionPixelSize(R.dimen.segment_minute_stroke_width).toFloat()
        isAntiAlias = true
    }

    private fun drawCircle(canvas: Canvas) {
        canvas.drawCircle(widthCenter, heightCenter, radius, paintCircle)
    }

    private fun drawSegments(canvas: Canvas) {

        for (num in 1..12) {
            angle = (PI / 6.0f * (num - 3)).toFloat()
            canvas.drawLine(
                (widthCenter + cos(angle) * radius),
                (heightCenter + sin(angle) * radius),
                (widthCenter + cos(angle) * radius * 0.9f), // x
                (heightCenter + sin(angle) * radius * 0.9f), // y
                paintSegmentHoursLines
            )
        }
        for (num in 1..60) {
            angle = (PI / 30.0f * (num - 3)).toFloat()
            canvas.drawLine(
                (widthCenter + cos(angle) * radius),
                (heightCenter + sin(angle) * radius),
                (widthCenter + cos(angle) * radius * 0.75f), // x
                (heightCenter + sin(angle) * radius * 0.75f), // y
                paintSegmentMinutesLines
            )
        }
    }

    private fun drawArrows(canvas: Canvas) {
        val calendar: Calendar = Calendar.getInstance()
        hours = calendar.get(Calendar.HOUR).toFloat()
        minutes = calendar.get(Calendar.MINUTE).toFloat()
        seconds = calendar.get(Calendar.SECOND).toFloat()
        milliseconds = calendar.get(Calendar.MILLISECOND).toFloat()
        seconds += milliseconds * 0.001f
        minutes += seconds / 60.0f
        hours += minutes / 60.0f
        //Log.d("'", "$milliseconds")

        drawHourArrow(canvas, hours * 5.0f)
        drawMinuteArrow(canvas, minutes)
        drawSecondsArrow(canvas, seconds)
        if (showMillisecondsArrow) {
            drawMillisecondsArrow(canvas, milliseconds)
        }
    }

    private fun calculateAngle(position: Float): Float {
        return (PI * position / 30.0f - PI * 0.5f).toFloat()
    }

    private fun drawHourArrow(canvas: Canvas, position: Float) {
        angle = calculateAngle(position)
        canvas.drawLine(
            widthCenter, heightCenter,
            (widthCenter + cos(angle) * hoursArrowLength), // x
            (heightCenter + sin(angle) * hoursArrowLength), // y
            paintHoursLine
        )

    }

    private fun drawMinuteArrow(canvas: Canvas, position: Float) {
        angle = calculateAngle(position)
        canvas.drawLine(
            widthCenter, heightCenter, // center
            (widthCenter + cos(angle) * minutesArrowLength), // x
            (heightCenter + sin(angle) * minutesArrowLength), // y
            paintMinutesLine
        )
    }

    private fun drawSecondsArrow(canvas: Canvas, position: Float) {
        angle = calculateAngle(position)
        canvas.drawLine(
            widthCenter, heightCenter, // center
            (widthCenter + cos(angle) * secondsArrowLength), // x
            (heightCenter + sin(angle) * secondsArrowLength), // y
            paintSecondsLine
        )
    }

    private fun drawMillisecondsArrow(canvas: Canvas, position: Float) {
        angle = calculateAngle(position)
        canvas.drawLine(
            widthCenter, heightCenter, // center
            (widthCenter + cos(angle) * millisecondsArrowLength), // x
            (heightCenter + sin(angle) * millisecondsArrowLength), // y
            paintMillisecondsLine
        )
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)

        // circle
        widthCenter = width * 0.5f
        heightCenter = height * 0.5f
        radius = min(width, height) * 0.5f - padding // max radius minus padding
        centerDotRadius = radius * 0.05f

        // clock
        hoursArrowLength = radius - radius * (0.5f + arrowsGeneralLength * 0.1f)
        minutesArrowLength = radius - radius * (0.25f + arrowsGeneralLength * 0.1f)
        secondsArrowLength = radius - radius * (0.1f + arrowsGeneralLength * 0.1f)
        millisecondsArrowLength = radius - radius * (0.05f + arrowsGeneralLength * 0.1f)

    }

    override fun onDraw(canvas: Canvas) { // 60 times a sec
        super.onDraw(canvas)

        drawSegments(canvas)
        drawCircle(canvas)
        drawArrows(canvas)
        canvas.drawCircle(widthCenter, heightCenter, centerDotRadius, paintCenterCircle)

        postInvalidateDelayed(tickInterval.toLong())
    }

}