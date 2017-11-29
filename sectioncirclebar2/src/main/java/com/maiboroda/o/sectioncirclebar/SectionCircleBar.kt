package com.maiboroda.o.sectioncirclebar

import android.content.Context
import android.content.res.TypedArray
import android.graphics.*
import android.graphics.drawable.Drawable
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import java.lang.RuntimeException

/**
 * This is circle bar view with sections.
 * Padding must be equal
 */
class SectionCircleBar(context: Context?, attrs: AttributeSet?) : View(context, attrs) {

    private var totalDegrees = 360.0
    private var startAngle = 270.0
    private var center: PointF? = null
    private var layoutHeight = 0
    private var layoutWidth = 0
    private var onSectionClick: ((Int) -> Unit)? = null

    private var touchEventCount: Int = 0

    //main circle(rim)
    private var rimBounds = RectF()
    private var rimWidth = 80
    private var rimColor = 0x557c2f37
    private val rimPaint = Paint()

    //pointer
    private var pointer: Drawable? = null
    private var pointerAngle = 0.0
    private var pointerDrawable: Int? = null

    //bar(section)
    private val sectionPaint = Paint()
    private var sectionColor = -0xff6978
    private var sectionCount = 7
    private var sectionScale = 0.98f
    private var sectionDegree = 360f / sectionCount
    private val sectionScaleDegree = sectionDegree * sectionScale
    private var sectionColors = intArrayOf(sectionColor)
//    private var barColorsDiff = intArrayOf(ContextCompat.getColor(context, R.color.excellent),
//        ContextCompat.getColor(context, R.color.veryGood),
//        ContextCompat.getColor(context, R.color.good),
//        ContextCompat.getColor(context, R.color.quiteGood),
//        ContextCompat.getColor(context, R.color.notGood),
//        ContextCompat.getColor(context, R.color.bad),
//        ContextCompat.getColor(context, R.color.terrible))
//    private var sectionColors = intArrayOf(Color.GREEN, Color.YELLOW, Color.RED)

    //text
    private val textPaint = Paint()
    private var textSize: Int = 100
    private var textColor: Int = 0x557c2f37
    private var text: String = ""
    private var textList: List<String>? = null

    private var previewText: String = "?"
    private val previewPaint = Paint()
    private var previewColor: Int = 0x557c2f37
    private var previewTextSize: Int = 200
    private var isPreview: Boolean = true

    init {
        parseAttrs(context?.obtainStyledAttributes(attrs, R.styleable.SectionCircleBar)!!)
        setupRimPaint()
        setupPointerPaint()
        setupTextPaint()
        setupPreviewPaint()
    }

    fun setTextList(list: List<String>) {
        if (sectionCount != list.size)
            throw RuntimeException("List size must be equal to sections count")
        textList = list
    }

    fun setListener(listener: (Int) -> Unit) {
        this.onSectionClick = listener
    }

    private fun parseAttrs(a: TypedArray) {
        rimWidth = a.getDimension(R.styleable.SectionCircleBar_rimWidth, rimWidth.toFloat()).toInt()
        rimColor = a.getColor(R.styleable.SectionCircleBar_rimColor, rimColor)

        sectionCount = a.getInt(R.styleable.SectionCircleBar_sectionCount, sectionCount)
        sectionColor = a.getColor(R.styleable.SectionCircleBar_sectionColor, sectionColor)

        textColor = a.getColor(R.styleable.SectionCircleBar_textColor, textColor)
        textSize = a.getDimension(R.styleable.SectionCircleBar_textSize, textSize.toFloat()).toInt()

        if (a.hasValue(R.styleable.SectionCircleBar_previewText))
            previewText = a.getString(R.styleable.SectionCircleBar_previewText)
        previewColor = a.getColor(R.styleable.SectionCircleBar_previewTextColor, previewColor)
        previewTextSize = a.getDimension(R.styleable.SectionCircleBar_previewTextSize, previewTextSize.toFloat()).toInt()

        if (a.hasValue(R.styleable.SectionCircleBar_pointerDrawable))
            pointerDrawable = a.getResourceId(R.styleable.SectionCircleBar_pointerDrawable, 0)

        val array = a.getTextArray(R.styleable.SectionCircleBar_textList)
        if (array != null)
            setTextList(array.map { it.toString() })

        a.recycle()
    }

    private fun setupRimPaint() {
        rimPaint.color = rimColor
        rimPaint.isAntiAlias = true
        rimPaint.style = Paint.Style.STROKE
        rimPaint.strokeWidth = rimWidth.toFloat()
    }

    private fun setupTextPaint() {
        textPaint.isSubpixelText = true
        textPaint.isLinearText = true
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        textPaint.color = textColor
        textPaint.style = Paint.Style.FILL
        textPaint.isAntiAlias = true
        textPaint.textSize = textSize.toFloat()
    }

    private fun setupPreviewPaint() {
        previewPaint.isSubpixelText = true
        previewPaint.isLinearText = true
        previewPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        previewPaint.color = previewColor
        previewPaint.style = Paint.Style.FILL
        previewPaint.isAntiAlias = true
        previewPaint.textSize = previewTextSize.toFloat()
    }

    private fun setupPointerPaint() {
        pointerDrawable?.let { pointer = ContextCompat.getDrawable(context, it) }
    }

    private fun setupBarPaint() {
        when {
            sectionColors.size > 1 -> {
                sectionPaint.shader = SweepGradient(rimBounds.centerX(), rimBounds.centerY(), sectionColors, null)
                val matrix = Matrix()
                sectionPaint.shader.getLocalMatrix(matrix)

                matrix.postTranslate(-rimBounds.centerX(), -rimBounds.centerY())
                matrix.postRotate(startAngle.toFloat())
                matrix.postTranslate(rimBounds.centerX(), rimBounds.centerY())
                sectionPaint.shader.setLocalMatrix(matrix)
            }
            else -> {
                sectionPaint.color = sectionColor
                sectionPaint.shader = null
            }
        }

        sectionPaint.isAntiAlias = true
        sectionPaint.style = Paint.Style.STROKE
        sectionPaint.strokeWidth = rimWidth.toFloat()
    }

    private fun drawPointer(canvas: Canvas, angle: Double) {
        if (pointer == null) return
        //padding must be equal!
        val radius = measuredWidth / 2 - rimWidth / 2 - paddingTop
        val x = (center?.x!! + radius * Math.cos(Math.toRadians(angle - 90))) - rimWidth / 2
        val y = (center?.y!! + radius * Math.sin(Math.toRadians(angle - 90))) - rimWidth / 2

        pointer?.setBounds(x.toInt(), y.toInt(), (x + rimWidth).toInt(), (y + rimWidth).toInt())
        pointer?.draw(canvas)
    }

    private fun drawSection(canvas: Canvas, angle: Double) {
//        var blocks: Int = Math.ceil(angle / sectionDegree).toInt()
//        if (blocks <= 0) blocks = 1
//        sectionPaint.color = barColorsDiff[blocks - 1]
        drawBlocks(canvas, rimBounds, startAngle, angle, false, sectionPaint)
    }

    private fun drawText(canvas: Canvas, text: String) {
        val x = center?.x!! - textPaint.measureText(text) / 2
        val y = center?.y!! - ((textPaint.descent() + textPaint.ascent()) / 2)
        canvas.drawText(text, x, y, textPaint)
    }

    private fun drawPreview(canvas: Canvas, text: String) {
        val x = center?.x!! - previewPaint.measureText(text) / 2
        val y = center?.y!! - ((previewPaint.descent() + previewPaint.ascent()) / 2)
        canvas.drawText(text, x, y, previewPaint)
    }

    private fun drawBlocks(canvas: Canvas, circleBounds: RectF, startAngle: Double, degrees: Double, userCenter: Boolean, paint: Paint) {
        var tmpDegree = 0.0f
        while (Math.ceil(tmpDegree.toDouble()) < degrees) {
            canvas.drawArc(circleBounds, startAngle.toFloat() + tmpDegree, sectionScaleDegree, userCenter, paint)
            tmpDegree += sectionDegree
        }
    }

    /**
     * Set the bounds of the component
     */
    private fun setupBounds() {
        // Width should equal to Height, find the min value to setup the circle
        val minValue = Math.min(layoutWidth, layoutHeight)

        // Calc the Offset if needed
        val xOffset = layoutWidth - minValue
        val yOffset = layoutHeight - minValue

        // Add the offset
        val paddingTop = (this.paddingTop + yOffset / 2).toFloat()
        val paddingBottom = (this.paddingBottom + yOffset / 2).toFloat()
        val paddingLeft = (this.paddingLeft + xOffset / 2).toFloat()
        val paddingRight = (this.paddingRight + xOffset / 2).toFloat()

        val width = width //this.getLayoutParams().width;
        val height = height //this.getLayoutParams().height;

        val circleWidthHalf = rimWidth / 2f

        rimBounds = RectF(paddingLeft + circleWidthHalf,
            paddingTop + circleWidthHalf,
            width.toFloat() - paddingRight - circleWidthHalf,
            height.toFloat() - paddingBottom - circleWidthHalf)

        center = PointF(rimBounds.centerX(), rimBounds.centerY())
    }

    /**
     * Use onSizeChanged instead of onAttachedToWindow to get the dimensions of the view,
     * because this method is called after measuring the dimensions of MATCH_PARENT and WRAP_CONTENT.
     * Use this dimensions to setup the bounds and paints.
     */
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        layoutWidth = w
        layoutHeight = h

        setupBounds()
        setupBarPaint()

        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        //Draw the rim
        if (rimWidth > 0)
            drawBlocks(canvas, rimBounds, startAngle, totalDegrees, false, rimPaint)

        drawSection(canvas, pointerAngle)
        drawPointer(canvas, pointerAngle)
        drawText(canvas, text)
        if (isPreview)
            drawPreview(canvas, previewText)
    }

    private fun setValue(value: Double) {
        isPreview = false
        var num = Math.ceil(value / sectionDegree)
        //avoid 0 value
        if (num < 1) num = 1.0
        //move pointer on right place(end of section)
        pointerAngle = num * sectionDegree
        if (textList != null)
            textList?.let { text = it[num.toInt() - 1] }

        onSectionClick?.invoke(num.toInt())

        invalidate()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_UP -> {
                touchEventCount = 0
                val point = PointF(event.x, event.y)
                val angle = getRotationAngleForPointFromStart(point)
                setValue(angle)
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                touchEventCount++
                return if (touchEventCount > 5) { //touch/move guard
                    val point = PointF(event.x, event.y)
                    val angle = getRotationAngleForPointFromStart(point)
                    setValue(angle)
                    true
                } else false
            }
            MotionEvent.ACTION_CANCEL -> {
                touchEventCount = 0
                return false
            }
        }

        return super.onTouchEvent(event)
    }

    /**
     * When this is called, make the view square.
     * From: http://www.jayway.com/2012/12/12/creating-custom-android-views-part-4-measuring-and-how-to-force-a-view-to-be-square/
     */
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        val size: Int
        val width = measuredWidth
        val height = measuredHeight
        val widthWithoutPadding = width - paddingLeft - paddingRight
        val heightWithoutPadding = height - paddingTop - paddingBottom

        size = if (widthWithoutPadding > heightWithoutPadding) heightWithoutPadding else widthWithoutPadding

        setMeasuredDimension(size + paddingLeft + paddingRight, size + paddingTop + paddingBottom)
    }

    private fun getRotationAngleForPointFromStart(point: PointF): Double {
        val angle = Math.round(calcRotationAngleInDegrees(center!!, point))
        val fromStart: Double = if (angle > startAngle) angle - startAngle else (angle + 90).toDouble()
        return normalizeAngle(fromStart)
    }

    private fun calcRotationAngleInDegrees(centerPt: PointF, targetPt: PointF): Double {
        val theta = Math.atan2((targetPt.y - centerPt.y).toDouble(), (targetPt.x - centerPt.x).toDouble())

        var angle = Math.toDegrees(theta)

        if (angle < 0)
            angle += totalDegrees

        return angle
    }

    private fun normalizeAngle(angle: Double): Double = (angle % totalDegrees + totalDegrees) % totalDegrees
}