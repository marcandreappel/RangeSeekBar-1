package `fun`.appel.rangeseekbar

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import android.os.Parcelable
import android.support.annotation.ColorInt
import android.support.annotation.DrawableRes
import android.support.annotation.IntDef
import android.text.TextUtils
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup

import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.ArrayList

import SeekBar.INDICATOR_ALWAYS_HIDE
import SeekBar.INDICATOR_ALWAYS_SHOW


class RangeSeekBar(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {

    var progressTop: Int = 0
    var progressBottom: Int = 0
    var progressLeft: Int = 0
    var progressRight: Int = 0
    private var seekBarMode: Int = 0
    /**
     * [.TICK_MARK_GRAVITY_LEFT] is number tick mark, it will locate the position according to the value.
     * [.TICK_MARK_GRAVITY_RIGHT] is text tick mark, it will be equally positioned.
     * @param tickMarkMode
     */
    var tickMarkMode: Int = 0
    //The spacing between the tick mark and the progress bar
    var tickMarkTextMargin: Int = 0
    //tick mark text and prompt text size
    open var tickMarkTextSize: Int = 0
    /**
     * the tick mark text gravity
     * [.TICK_MARK_GRAVITY_LEFT]
     * [.TICK_MARK_GRAVITY_RIGHT]
     * [.TICK_MARK_GRAVITY_CENTER]
     * @param tickMarkGravity
     */
    var tickMarkGravity: Int = 0
    /**
     * the tick mark layout gravity
     * Gravity.TOP and Gravity.BOTTOM
     * @param tickMarkLayoutGravity
     */
    var tickMarkLayoutGravity: Int = 0
    var tickMarkTextColor: Int = 0
    var tickMarkInRangeTextColor: Int = 0
    //刻度上显示的文字
    //The texts displayed on the scale
    open var tickMarkTextArray: Array<CharSequence>? = null
    //进度条圆角
    //radius of progress bar
    var progressRadius: Float = 0.toFloat()
    //进度中进度条的颜色
    //the color of seekBar in progress
    var progressColor: Int = 0
    //默认进度条颜色
    //the default color of the progress bar
    var progressDefaultColor: Int = 0

    //the drawable of seekBar in progress
    private var progressDrawableId: Int = 0
    //the default Drawable of the progress bar
    private var progressDefaultDrawableId: Int = 0

    //the progress height
    var progressHeight: Int = 0
    // the progress width
    var progressWidth: Int = 0
    //the range interval of RangeSeekBar
    var minInterval: Float = 0.toFloat()
        private set

    /**
     * the RangeSeekBar gravity
     * Gravity.TOP and Gravity.BOTTOM
     * @param gravity
     */
    var gravity: Int = 0
    //enable RangeSeekBar two thumb Overlap
    var isEnableThumbOverlap: Boolean = false

    //the color of step divs
    var stepsColor: Int = 0
    //the width of each step
    var stepsWidth: Float = 0.toFloat()
    //the height of each step
    var stepsHeight: Float = 0.toFloat()
    //the radius of step divs
    var stepsRadius: Float = 0.toFloat()
    //steps is 0 will disable StepSeekBar
    var steps: Int = 0
    //the thumb will automatic bonding close to its value
    var isStepsAutoBonding: Boolean = false
    private var stepsDrawableId: Int = 0
    //True values set by the user
    var minProgress: Float = 0.toFloat()
        private set
    var maxProgress: Float = 0.toFloat()
        private set
    //****************** the above is attr value  ******************//

    private var isEnable = true
    internal var touchDownX: Float = 0.toFloat()
    internal var touchDownY: Float = 0.toFloat()
    //剩余最小间隔的进度
    internal var reservePercent: Float = 0.toFloat()
    internal var isScaleThumb = false
    internal var paint = Paint()
    internal var progressDefaultDstRect = RectF()
    internal var progressDstRect = RectF()
    internal var progressSrcRect = Rect()
    internal var stepDivRect = RectF()
    internal var tickMarkTextRect = Rect()
    /**
     * if is single mode, please use it to get the SeekBar
     *
     * @return left seek bar
     */
    open var leftSeekBar: SeekBar? = null
        internal set
    open var rightSeekBar: SeekBar? = null
        internal set
    internal var currTouchSB: SeekBar? = null
    internal var progressBitmap: Bitmap? = null
    internal var progressDefaultBitmap: Bitmap? = null
    internal var stepsBitmaps: List<Bitmap> = ArrayList()
    var progressPaddingRight: Int = 0
        private set
    private var callback: OnRangeChangedListener? = null

    protected open val tickMarkRawHeight: Int
        get() = if (tickMarkTextArray != null && tickMarkTextArray!!.size > 0) {
            tickMarkTextMargin + Utils.measureText(tickMarkTextArray!![0].toString(), tickMarkTextSize).height() + 3
        } else 0

    protected val rawHeight: Float
        get() {
            var rawHeight: Float
            if (seekBarMode == SEEKBAR_MODE_SINGLE) {
                rawHeight = leftSeekBar!!.rawHeight
                if (tickMarkLayoutGravity == Gravity.BOTTOM && tickMarkTextArray != null) {
                    val h = Math.max((leftSeekBar!!.thumbScaleHeight - progressHeight) / 2, tickMarkRawHeight)
                    rawHeight = rawHeight - leftSeekBar!!.thumbScaleHeight / 2 + progressHeight / 2f + h
                }
            } else {
                rawHeight = Math.max(leftSeekBar!!.rawHeight, rightSeekBar!!.rawHeight)
                if (tickMarkLayoutGravity == Gravity.BOTTOM && tickMarkTextArray != null) {
                    val thumbHeight = Math.max(leftSeekBar!!.thumbScaleHeight, rightSeekBar!!.thumbScaleHeight)
                    val h = Math.max((thumbHeight - progressHeight) / 2, tickMarkRawHeight.toFloat())
                    rawHeight = rawHeight - thumbHeight / 2 + progressHeight / 2f + h
                }
            }
            return rawHeight
        }

    /**
     * @return the two seekBar state , see [SeekBarState]
     */
    val rangeSeekBarState: Array<SeekBarState>
        get() {
            val leftSeekBarState = SeekBarState()
            leftSeekBarState.value = leftSeekBar!!.progress

            leftSeekBarState.indicatorText = String.valueOf(leftSeekBarState.value)
            if (Utils.compareFloat(leftSeekBarState.value, minProgress) === 0) {
                leftSeekBarState.isMin = true
            } else if (Utils.compareFloat(leftSeekBarState.value, maxProgress) === 0) {
                leftSeekBarState.isMax = true
            }

            val rightSeekBarState = SeekBarState()
            if (seekBarMode == SEEKBAR_MODE_RANGE) {
                rightSeekBarState.value = rightSeekBar!!.progress
                rightSeekBarState.indicatorText = String.valueOf(rightSeekBarState.value)
                if (Utils.compareFloat(rightSeekBar!!.currPercent, minProgress) === 0) {
                    rightSeekBarState.isMin = true
                } else if (Utils.compareFloat(rightSeekBar!!.currPercent, maxProgress) === 0) {
                    rightSeekBarState.isMax = true
                }
            }

            return arrayOf(leftSeekBarState, rightSeekBarState)
        }

    /**
     * @hide
     */
    @IntDef(SEEKBAR_MODE_SINGLE, SEEKBAR_MODE_RANGE)
    annotation class SeekBarModeDef

    /**
     * @hide
     */
    @IntDef(TRICK_MARK_MODE_NUMBER, TRICK_MARK_MODE_OTHER)
    annotation class TickMarkModeDef

    /**
     * @hide
     */
    @IntDef(TICK_MARK_GRAVITY_LEFT, TICK_MARK_GRAVITY_CENTER, TICK_MARK_GRAVITY_RIGHT)
    annotation class TickMarkGravityDef

    /**
     * @hide
     */
    @IntDef(Gravity.TOP, Gravity.BOTTOM)
    annotation class TickMarkLayoutGravityDef

    /**
     * @hide
     */
    @IntDef(Gravity.TOP, Gravity.CENTER, Gravity.BOTTOM)
    annotation class GravityDef

    object Gravity {
        const val TOP = 0
        const val BOTTOM = 1
        const val CENTER = 2
    }

    init {
        initAttrs(attrs)
        initPaint()
        initSeekBar(attrs)
        initStepsBitmap()
    }

    private fun initProgressBitmap() {
        if (progressBitmap == null) {
            progressBitmap = Utils.drawableToBitmap(context, progressWidth, progressHeight, progressDrawableId)
        }
        if (progressDefaultBitmap == null) {
            progressDefaultBitmap = Utils.drawableToBitmap(context, progressWidth, progressHeight, progressDefaultDrawableId)
        }
    }

    private fun verifyStepsMode(): Boolean {
        return if (steps < 1 || stepsHeight <= 0 || stepsWidth <= 0) false else true
    }

    private fun initStepsBitmap() {
        if (!verifyStepsMode() || stepsDrawableId == 0) return
        if (stepsBitmaps.isEmpty()) {
            val bitmap = Utils.drawableToBitmap(context, stepsWidth.toInt(), stepsHeight.toInt(), stepsDrawableId)
            for (i in 0..steps) {
                stepsBitmaps.add(bitmap)
            }
        }
    }

    private fun initSeekBar(attrs: AttributeSet?) {
        leftSeekBar = SeekBar(this, attrs, true)
        rightSeekBar = SeekBar(this, attrs, false)
        rightSeekBar!!.setVisible(seekBarMode != SEEKBAR_MODE_SINGLE)
    }


    private fun initAttrs(attrs: AttributeSet?) {
        try {
            val t = context.obtainStyledAttributes(attrs, R.styleable.RangeSeekBar)
            seekBarMode = t.getInt(R.styleable.RangeSeekBar_rsb_mode, SEEKBAR_MODE_RANGE)
            minProgress = t.getFloat(R.styleable.RangeSeekBar_rsb_min, 0f)
            maxProgress = t.getFloat(R.styleable.RangeSeekBar_rsb_max, 100f)
            minInterval = t.getFloat(R.styleable.RangeSeekBar_rsb_min_interval, 0f)
            gravity = t.getInt(R.styleable.RangeSeekBar_rsb_gravity, Gravity.TOP)
            progressColor = t.getColor(R.styleable.RangeSeekBar_rsb_progress_color, -0xb4269e)
            progressRadius = t.getDimension(R.styleable.RangeSeekBar_rsb_progress_radius, -1f).toInt().toFloat()
            progressDefaultColor = t.getColor(R.styleable.RangeSeekBar_rsb_progress_default_color, -0x282829)
            progressDrawableId = t.getResourceId(R.styleable.RangeSeekBar_rsb_progress_drawable, 0)
            progressDefaultDrawableId = t.getResourceId(R.styleable.RangeSeekBar_rsb_progress_drawable_default, 0)
            progressHeight = t.getDimension(R.styleable.RangeSeekBar_rsb_progress_height, Utils.dp2px(context, 2)).toInt()
            tickMarkMode = t.getInt(R.styleable.RangeSeekBar_rsb_tick_mark_mode, TRICK_MARK_MODE_NUMBER)
            tickMarkGravity = t.getInt(R.styleable.RangeSeekBar_rsb_tick_mark_gravity, TICK_MARK_GRAVITY_CENTER)
            tickMarkLayoutGravity = t.getInt(R.styleable.RangeSeekBar_rsb_tick_mark_layout_gravity, Gravity.TOP)
            tickMarkTextArray = t.getTextArray(R.styleable.RangeSeekBar_rsb_tick_mark_text_array)
            tickMarkTextMargin = t.getDimension(R.styleable.RangeSeekBar_rsb_tick_mark_text_margin, Utils.dp2px(context, 7)).toInt()
            tickMarkTextSize = t.getDimension(R.styleable.RangeSeekBar_rsb_tick_mark_text_size, Utils.dp2px(context, 12)).toInt()
            tickMarkTextColor = t.getColor(R.styleable.RangeSeekBar_rsb_tick_mark_text_color, progressDefaultColor)
            tickMarkInRangeTextColor = t.getColor(R.styleable.RangeSeekBar_rsb_tick_mark_text_color, progressColor)
            steps = t.getInt(R.styleable.RangeSeekBar_rsb_steps, 0)
            stepsColor = t.getColor(R.styleable.RangeSeekBar_rsb_step_color, -0x626263)
            stepsRadius = t.getDimension(R.styleable.RangeSeekBar_rsb_step_radius, 0f)
            stepsWidth = t.getDimension(R.styleable.RangeSeekBar_rsb_step_width, 0f)
            stepsHeight = t.getDimension(R.styleable.RangeSeekBar_rsb_step_height, 0f)
            stepsDrawableId = t.getResourceId(R.styleable.RangeSeekBar_rsb_step_drawable, 0)
            isStepsAutoBonding = t.getBoolean(R.styleable.RangeSeekBar_rsb_step_auto_bonding, true)
            t.recycle()
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }


    /**
     * measure progress bar position
     */
    protected fun onMeasureProgress(w: Int, h: Int) {
        val viewHeight = h - paddingBottom - paddingTop
        if (h <= 0) return

        if (gravity == Gravity.TOP) {
            //calculate the height of indicator and thumb exceeds the part of the progress
            var maxIndicatorHeight = 0f
            if (leftSeekBar!!.indicatorShowMode !== INDICATOR_ALWAYS_HIDE || rightSeekBar!!.indicatorShowMode !== INDICATOR_ALWAYS_HIDE) {
                maxIndicatorHeight = Math.max(leftSeekBar!!.indicatorRawHeight, rightSeekBar!!.indicatorRawHeight)
            }
            var thumbHeight = Math.max(leftSeekBar!!.thumbScaleHeight, rightSeekBar!!.thumbScaleHeight)
            thumbHeight -= progressHeight / 2f

            //default height is indicator + thumb exceeds the part of the progress bar
            //if tickMark height is greater than (indicator + thumb exceeds the part of the progress)
            progressTop = (maxIndicatorHeight + (thumbHeight - progressHeight) / 2f).toInt()
            if (tickMarkTextArray != null && tickMarkLayoutGravity == Gravity.TOP) {
                progressTop = Math.max(tickMarkRawHeight.toFloat(), maxIndicatorHeight + (thumbHeight - progressHeight) / 2f).toInt()
            }
            progressBottom = progressTop + progressHeight
        } else if (gravity == Gravity.BOTTOM) {
            if (tickMarkTextArray != null && tickMarkLayoutGravity == Gravity.BOTTOM) {
                progressBottom = viewHeight - tickMarkRawHeight
            } else {
                progressBottom = (viewHeight - Math.max(leftSeekBar!!.thumbScaleHeight, rightSeekBar!!.thumbScaleHeight) / 2f + progressHeight / 2f) as Int
            }
            progressTop = progressBottom - progressHeight
        } else {
            progressTop = (viewHeight - progressHeight) / 2
            progressBottom = progressTop + progressHeight
        }

        val maxThumbWidth = Math.max(leftSeekBar!!.thumbScaleWidth, rightSeekBar!!.thumbScaleWidth) as Int
        progressLeft = maxThumbWidth / 2 + paddingLeft
        progressRight = w - maxThumbWidth / 2 - paddingRight
        progressWidth = progressRight - progressLeft
        progressDefaultDstRect.set(progressLeft.toFloat(), progressTop.toFloat(), progressRight.toFloat(), progressBottom.toFloat())
        progressPaddingRight = w - progressRight
        //default value
        if (progressRadius <= 0) {
            progressRadius = ((progressBottom - progressTop) * 0.45f).toInt().toFloat()
        }
        initProgressBitmap()
    }

    //Android 7.0以后，优化了View的绘制，onMeasure和onSizeChanged调用顺序有所变化
    //Android7.0以下：onMeasure--->onSizeChanged--->onMeasure
    //Android7.0以上：onMeasure--->onSizeChanged
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var heightSize = View.MeasureSpec.getSize(heightMeasureSpec)
        val heightMode = View.MeasureSpec.getMode(heightMeasureSpec)
        /*
         * onMeasure传入的widthMeasureSpec和heightMeasureSpec不是一般的尺寸数值，而是将模式和尺寸组合在一起的数值
         * MeasureSpec.EXACTLY 是精确尺寸
         * MeasureSpec.AT_MOST 是最大尺寸
         * MeasureSpec.UNSPECIFIED 是未指定尺寸
         */

        if (heightMode == View.MeasureSpec.EXACTLY) {
            heightSize = View.MeasureSpec.makeMeasureSpec(heightSize, View.MeasureSpec.EXACTLY)
        } else if (heightMode == View.MeasureSpec.AT_MOST && parent is ViewGroup
                && heightSize == ViewGroup.LayoutParams.MATCH_PARENT) {
            heightSize = View.MeasureSpec.makeMeasureSpec((parent as ViewGroup).measuredHeight, View.MeasureSpec.AT_MOST)
        } else {
            val heightNeeded: Int
            if (gravity == Gravity.CENTER) {
                if (tickMarkTextArray != null && tickMarkLayoutGravity == Gravity.BOTTOM) {
                    heightNeeded = (2 * (rawHeight - tickMarkRawHeight)).toInt()
                } else {
                    heightNeeded = (2 * (rawHeight - Math.max(leftSeekBar!!.thumbScaleHeight, rightSeekBar!!.thumbScaleHeight) / 2)) as Int
                }
            } else {
                heightNeeded = rawHeight.toInt()
            }
            heightSize = View.MeasureSpec.makeMeasureSpec(heightNeeded, View.MeasureSpec.EXACTLY)
        }
        super.onMeasure(widthMeasureSpec, heightSize)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        onMeasureProgress(w, h)
        //set default value
        setRange(minProgress, maxProgress, minInterval)
        // initializes the positions of the two thumbs
        val lineCenterY = (progressBottom + progressTop) / 2
        leftSeekBar!!.onSizeChanged(progressLeft, lineCenterY)
        if (seekBarMode == SEEKBAR_MODE_RANGE) {
            rightSeekBar!!.onSizeChanged(progressLeft, lineCenterY)
        }
    }


    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        onDrawTickMark(canvas, paint)
        onDrawProgressBar(canvas, paint)
        onDrawSteps(canvas, paint)
        onDrawSeekBar(canvas)
    }

    //绘制刻度，并且根据当前位置是否在刻度范围内设置不同的颜色显示
    // Draw the scales, and according to the current position is set within
    // the scale range of different color display
    protected open fun onDrawTickMark(canvas: Canvas, paint: Paint) {
        if (tickMarkTextArray != null) {
            val trickPartWidth = progressWidth / (tickMarkTextArray!!.size - 1)
            for (i in tickMarkTextArray!!.indices) {
                val text2Draw = tickMarkTextArray!![i].toString()
                if (TextUtils.isEmpty(text2Draw)) continue
                paint.getTextBounds(text2Draw, 0, text2Draw.length, tickMarkTextRect)
                paint.color = tickMarkTextColor
                //平分显示
                val x: Float
                if (tickMarkMode == TRICK_MARK_MODE_OTHER) {
                    if (tickMarkGravity == TICK_MARK_GRAVITY_RIGHT) {
                        x = (progressLeft + i * trickPartWidth - tickMarkTextRect.width()).toFloat()
                    } else if (tickMarkGravity == TICK_MARK_GRAVITY_CENTER) {
                        x = progressLeft + i * trickPartWidth - tickMarkTextRect.width() / 2f
                    } else {
                        x = (progressLeft + i * trickPartWidth).toFloat()
                    }
                } else {
                    val num = Utils.parseFloat(text2Draw)
                    val states = rangeSeekBarState
                    if (Utils.compareFloat(num, states[0].value) !== -1 && Utils.compareFloat(num, states[1].value) !== 1 && seekBarMode == SEEKBAR_MODE_RANGE) {
                        paint.color = tickMarkInRangeTextColor
                    }
                    //按实际比例显示
                    x = progressLeft + progressWidth * (num - minProgress) / (maxProgress - minProgress) - tickMarkTextRect.width() / 2f
                }
                val y: Float
                if (tickMarkLayoutGravity == Gravity.TOP) {
                    y = (progressTop - tickMarkTextMargin).toFloat()
                } else {
                    y = (progressBottom + tickMarkTextMargin + tickMarkTextRect.height()).toFloat()
                }
                canvas.drawText(text2Draw, x, y, paint)
            }
        }
    }

    //绘制进度条
    // draw the progress bar
    protected fun onDrawProgressBar(canvas: Canvas, paint: Paint) {

        //draw default progress
        if (Utils.verifyBitmap(progressDefaultBitmap)) {
            canvas.drawBitmap(progressDefaultBitmap!!, null, progressDefaultDstRect, paint)
        } else {
            paint.color = progressDefaultColor
            canvas.drawRoundRect(progressDefaultDstRect, progressRadius, progressRadius, paint)
        }

        //draw progress
        if (seekBarMode == SEEKBAR_MODE_RANGE) {
            progressDstRect.top = progressTop.toFloat()
            progressDstRect.left = leftSeekBar!!.left + leftSeekBar!!.thumbScaleWidth / 2f + progressWidth * leftSeekBar!!.currPercent
            progressDstRect.right = rightSeekBar!!.left + rightSeekBar!!.thumbScaleWidth / 2f + progressWidth * rightSeekBar!!.currPercent
            progressDstRect.bottom = progressBottom.toFloat()
        } else {
            progressDstRect.top = progressTop.toFloat()
            progressDstRect.left = leftSeekBar!!.left + leftSeekBar!!.thumbScaleWidth / 2f
            progressDstRect.right = leftSeekBar!!.left + leftSeekBar!!.thumbScaleWidth / 2f + progressWidth * leftSeekBar!!.currPercent
            progressDstRect.bottom = progressBottom.toFloat()
        }

        if (Utils.verifyBitmap(progressBitmap)) {
            progressSrcRect.top = 0
            progressSrcRect.bottom = progressBitmap!!.height
            val bitmapWidth = progressBitmap!!.width
            if (seekBarMode == SEEKBAR_MODE_RANGE) {
                progressSrcRect.left = (bitmapWidth * leftSeekBar!!.currPercent) as Int
                progressSrcRect.right = (bitmapWidth * rightSeekBar!!.currPercent) as Int
            } else {
                progressSrcRect.left = 0
                progressSrcRect.right = (bitmapWidth * leftSeekBar!!.currPercent) as Int
            }
            canvas.drawBitmap(progressBitmap!!, progressSrcRect, progressDstRect, null)
        } else {
            paint.color = progressColor
            canvas.drawRoundRect(progressDstRect, progressRadius, progressRadius, paint)
        }

    }

    //draw steps
    protected fun onDrawSteps(canvas: Canvas, paint: Paint) {
        if (!verifyStepsMode()) return
        val stepMarks = progressWidth / steps
        val extHeight = (stepsHeight - progressHeight) / 2f
        for (k in 0..steps) {
            val x = progressLeft + k * stepMarks - stepsWidth / 2f
            stepDivRect.set(x, progressTop - extHeight, x + stepsWidth, progressBottom + extHeight)
            if (stepsBitmaps.isEmpty() || stepsBitmaps.size <= k) {
                paint.color = stepsColor
                canvas.drawRoundRect(stepDivRect, stepsRadius, stepsRadius, paint)
            } else {
                canvas.drawBitmap(stepsBitmaps[k], null, stepDivRect, paint)
            }
        }
    }

    //绘制SeekBar相关
    protected fun onDrawSeekBar(canvas: Canvas) {
        //draw left SeekBar
        if (leftSeekBar!!.indicatorShowMode === INDICATOR_ALWAYS_SHOW) {
            leftSeekBar!!.setShowIndicatorEnable(true)
        }
        leftSeekBar!!.draw(canvas)
        //draw right SeekBar
        if (seekBarMode == SEEKBAR_MODE_RANGE) {
            if (rightSeekBar!!.indicatorShowMode === INDICATOR_ALWAYS_SHOW) {
                rightSeekBar!!.setShowIndicatorEnable(true)
            }
            rightSeekBar!!.draw(canvas)
        }
    }

    //初始化画笔
    private fun initPaint() {
        paint.style = Paint.Style.FILL
        paint.color = progressDefaultColor
        paint.textSize = tickMarkTextSize.toFloat()
    }


    private fun changeThumbActivateState(hasActivate: Boolean) {
        if (hasActivate && currTouchSB != null) {
            val state = currTouchSB === leftSeekBar
            leftSeekBar!!.activate = state
            if (seekBarMode == SEEKBAR_MODE_RANGE)
                rightSeekBar!!.activate = !state
        } else {
            leftSeekBar!!.activate = false
            if (seekBarMode == SEEKBAR_MODE_RANGE)
                rightSeekBar!!.activate = false
        }
    }

    protected open fun getEventX(event: MotionEvent): Float {
        return event.x
    }

    protected open fun getEventY(event: MotionEvent): Float {
        return event.y
    }

    /**
     * scale the touch seekBar thumb
     */
    private fun scaleCurrentSeekBarThumb() {
        if (currTouchSB != null && currTouchSB!!.getThumbScaleRatio() > 1f && !isScaleThumb) {
            isScaleThumb = true
            currTouchSB!!.scaleThumb()
        }
    }

    /**
     * reset the touch seekBar thumb
     */
    private fun resetCurrentSeekBarThumb() {
        if (currTouchSB != null && currTouchSB!!.getThumbScaleRatio() > 1f && isScaleThumb) {
            isScaleThumb = false
            currTouchSB!!.resetThumb()
        }
    }

    //calculate currTouchSB percent by MotionEvent
    protected fun calculateCurrentSeekBarPercent(touchDownX: Float): Float {
        if (currTouchSB == null) return 0f
        var percent = (touchDownX - progressLeft) * 1f / progressWidth
        if (touchDownX < progressLeft) {
            percent = 0f
        } else if (touchDownX > progressRight) {
            percent = 1f
        }
        //RangeMode minimum interval
        if (seekBarMode == SEEKBAR_MODE_RANGE) {
            if (currTouchSB === leftSeekBar) {
                if (percent > rightSeekBar!!.currPercent - reservePercent) {
                    percent = rightSeekBar!!.currPercent - reservePercent
                }
            } else if (currTouchSB === rightSeekBar) {
                if (percent < leftSeekBar!!.currPercent + reservePercent) {
                    percent = leftSeekBar!!.currPercent + reservePercent
                }
            }
        }
        return percent
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!isEnable) return true

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                touchDownX = getEventX(event)
                touchDownY = getEventY(event)
                if (seekBarMode == SEEKBAR_MODE_RANGE) {
                    if (rightSeekBar!!.currPercent >= 1 && leftSeekBar!!.collide(getEventX(event), getEventY(event))) {
                        currTouchSB = leftSeekBar
                        scaleCurrentSeekBarThumb()
                    } else if (rightSeekBar!!.collide(getEventX(event), getEventY(event))) {
                        currTouchSB = rightSeekBar
                        scaleCurrentSeekBarThumb()
                    } else {
                        var performClick = (touchDownX - progressLeft) * 1f / progressWidth
                        val distanceLeft = Math.abs(leftSeekBar!!.currPercent - performClick)
                        val distanceRight = Math.abs(rightSeekBar!!.currPercent - performClick)
                        if (distanceLeft < distanceRight) {
                            currTouchSB = leftSeekBar
                        } else {
                            currTouchSB = rightSeekBar
                        }
                        performClick = calculateCurrentSeekBarPercent(touchDownX)
                        currTouchSB!!.slide(performClick)
                    }
                } else {
                    currTouchSB = leftSeekBar
                    scaleCurrentSeekBarThumb()
                }

                //Intercept parent TouchEvent
                if (parent != null) {
                    parent.requestDisallowInterceptTouchEvent(true)
                }
                if (callback != null) {
                    callback!!.onStartTrackingTouch(this, currTouchSB === leftSeekBar)
                }
                changeThumbActivateState(true)
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                val x = getEventX(event)
                if (seekBarMode == SEEKBAR_MODE_RANGE && leftSeekBar!!.currPercent === rightSeekBar!!.currPercent) {
                    currTouchSB!!.materialRestore()
                    if (callback != null) {
                        callback!!.onStopTrackingTouch(this, currTouchSB === leftSeekBar)
                    }
                    if (x - touchDownX > 0) {
                        //method to move right
                        if (currTouchSB !== rightSeekBar) {
                            currTouchSB!!.setShowIndicatorEnable(false)
                            resetCurrentSeekBarThumb()
                            currTouchSB = rightSeekBar
                        }
                    } else {
                        //method to move left
                        if (currTouchSB !== leftSeekBar) {
                            currTouchSB!!.setShowIndicatorEnable(false)
                            resetCurrentSeekBarThumb()
                            currTouchSB = leftSeekBar
                        }
                    }
                    if (callback != null) {
                        callback!!.onStartTrackingTouch(this, currTouchSB === leftSeekBar)
                    }
                }
                scaleCurrentSeekBarThumb()
                currTouchSB!!.material = if (currTouchSB!!.material >= 1) 1 else currTouchSB!!.material + 0.1f
                touchDownX = x
                currTouchSB!!.slide(calculateCurrentSeekBarPercent(touchDownX))
                currTouchSB!!.setShowIndicatorEnable(true)

                if (callback != null) {
                    val states = rangeSeekBarState
                    callback!!.onRangeChanged(this, states[0].value, states[1].value, true)
                }
                invalidate()
                //Intercept parent TouchEvent
                if (parent != null) {
                    parent.requestDisallowInterceptTouchEvent(true)
                }
                changeThumbActivateState(true)
            }
            MotionEvent.ACTION_CANCEL -> {
                if (seekBarMode == SEEKBAR_MODE_RANGE) {
                    rightSeekBar!!.setShowIndicatorEnable(false)
                }
                if (currTouchSB === leftSeekBar) {
                    resetCurrentSeekBarThumb()
                } else if (currTouchSB === rightSeekBar) {
                    resetCurrentSeekBarThumb()
                }
                leftSeekBar!!.setShowIndicatorEnable(false)
                if (callback != null) {
                    val states = rangeSeekBarState
                    callback!!.onRangeChanged(this, states[0].value, states[1].value, false)
                }
                //Intercept parent TouchEvent
                if (parent != null) {
                    parent.requestDisallowInterceptTouchEvent(true)
                }
                changeThumbActivateState(false)
            }
            MotionEvent.ACTION_UP -> {
                if (verifyStepsMode() && isStepsAutoBonding) {
                    val percent = calculateCurrentSeekBarPercent(getEventX(event))
                    val stepPercent = 1.0f / steps
                    val stepSelected = BigDecimal((percent / stepPercent).toDouble()).setScale(0, RoundingMode.HALF_UP).toInt()
                    currTouchSB!!.slide(stepSelected * stepPercent)
                }

                if (seekBarMode == SEEKBAR_MODE_RANGE) {
                    rightSeekBar!!.setShowIndicatorEnable(false)
                }
                leftSeekBar!!.setShowIndicatorEnable(false)
                currTouchSB!!.materialRestore()
                resetCurrentSeekBarThumb()
                if (callback != null) {
                    val states = rangeSeekBarState
                    callback!!.onRangeChanged(this, states[0].value, states[1].value, false)
                }
                //Intercept parent TouchEvent
                if (parent != null) {
                    parent.requestDisallowInterceptTouchEvent(true)
                }
                if (callback != null) {
                    callback!!.onStopTrackingTouch(this, currTouchSB === leftSeekBar)
                }
                changeThumbActivateState(false)
            }
        }
        return super.onTouchEvent(event)
    }

    public override fun onSaveInstanceState(): Parcelable? {
        val superState = super.onSaveInstanceState()
        val ss = SavedState(superState)
        ss.minValue = minProgress
        ss.maxValue = maxProgress
        ss.rangeInterval = minInterval
        val results = rangeSeekBarState
        ss.currSelectedMin = results[0].value
        ss.currSelectedMax = results[1].value
        return ss
    }

    public override fun onRestoreInstanceState(state: Parcelable) {
        try {
            val ss = state as SavedState
            super.onRestoreInstanceState(ss.superState)
            val min = ss.minValue
            val max = ss.maxValue
            val rangeInterval = ss.rangeInterval
            setRange(min, max, rangeInterval)
            val currSelectedMin = ss.currSelectedMin
            val currSelectedMax = ss.currSelectedMax
            setProgress(currSelectedMin, currSelectedMax)
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    //******************* Attributes getter and setter *******************//

    fun setOnRangeChangedListener(listener: OnRangeChangedListener) {
        callback = listener
    }

    fun setProgress(value: Float) {
        setProgress(value, maxProgress)
    }

    fun setProgress(leftValue: Float, rightValue: Float) {
        var leftValue = leftValue
        var rightValue = rightValue
        leftValue = Math.min(leftValue, rightValue)
        rightValue = Math.max(leftValue, rightValue)
        if (rightValue - leftValue < minInterval) {
            leftValue = rightValue - minInterval
        }

        if (leftValue < minProgress) {
            throw IllegalArgumentException("setProgress() min < (preset min - offsetValue) . #min:$leftValue #preset min:$rightValue")
        }
        if (rightValue > maxProgress) {
            throw IllegalArgumentException("setProgress() max > (preset max - offsetValue) . #max:$rightValue #preset max:$rightValue")
        }

        val range = maxProgress - minProgress
        leftSeekBar!!.currPercent = Math.abs(leftValue - minProgress) / range
        if (seekBarMode == SEEKBAR_MODE_RANGE) {
            rightSeekBar!!.currPercent = Math.abs(rightValue - minProgress) / range
        }

        if (callback != null) {
            callback!!.onRangeChanged(this, leftValue, rightValue, false)
        }
        invalidate()
    }

    /**
     * 设置范围
     *
     * @param min         最小值
     * @param max         最大值
     * @param minInterval 最小间隔
     */
    @JvmOverloads
    fun setRange(min: Float, max: Float, minInterval: Float = minInterval) {
        if (max <= min) {
            throw IllegalArgumentException("setRange() max must be greater than min ! #max:$max #min:$min")
        }
        if (minInterval < 0) {
            throw IllegalArgumentException("setRange() interval must be greater than zero ! #minInterval:$minInterval")
        }
        if (minInterval >= max - min) {
            throw IllegalArgumentException("setRange() interval must be less than (max - min) ! #minInterval:" + minInterval + " #max - min:" + (max - min))
        }

        maxProgress = max
        minProgress = min
        this.minInterval = minInterval
        reservePercent = minInterval / (max - min)

        //set default value
        if (seekBarMode == SEEKBAR_MODE_RANGE) {
            if (leftSeekBar!!.currPercent + reservePercent <= 1 && leftSeekBar!!.currPercent + reservePercent > rightSeekBar!!.currPercent) {
                rightSeekBar!!.currPercent = leftSeekBar!!.currPercent + reservePercent
            } else if (rightSeekBar!!.currPercent - reservePercent >= 0 && rightSeekBar!!.currPercent - reservePercent < leftSeekBar!!.currPercent) {
                leftSeekBar!!.currPercent = rightSeekBar!!.currPercent - reservePercent
            }
        }
        invalidate()
    }


    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        this.isEnable = enabled
    }

    fun setIndicatorText(progress: String) {
        leftSeekBar!!.setIndicatorText(progress)
        if (seekBarMode == SEEKBAR_MODE_RANGE) {
            rightSeekBar!!.setIndicatorText(progress)
        }
    }

    /**
     * format number indicator text
     *
     * @param formatPattern format rules
     */
    fun setIndicatorTextDecimalFormat(formatPattern: String) {
        leftSeekBar!!.setIndicatorTextDecimalFormat(formatPattern)
        if (seekBarMode == SEEKBAR_MODE_RANGE) {
            rightSeekBar!!.setIndicatorTextDecimalFormat(formatPattern)
        }
    }

    /**
     * format string indicator text
     *
     * @param formatPattern format rules
     */
    fun setIndicatorTextStringFormat(formatPattern: String) {
        leftSeekBar!!.setIndicatorTextStringFormat(formatPattern)
        if (seekBarMode == SEEKBAR_MODE_RANGE) {
            rightSeekBar!!.setIndicatorTextStringFormat(formatPattern)
        }
    }

    fun setProgressColor(@ColorInt progressDefaultColor: Int, @ColorInt progressColor: Int) {
        this.progressDefaultColor = progressDefaultColor
        this.progressColor = progressColor
    }

    fun getSeekBarMode(): Int {
        return seekBarMode
    }

    /**
     * [.SEEKBAR_MODE_SINGLE] is single SeekBar
     * [.SEEKBAR_MODE_RANGE] is range SeekBar
     * @param seekBarMode
     */
    fun setSeekBarMode(@SeekBarModeDef seekBarMode: Int) {
        this.seekBarMode = seekBarMode
        rightSeekBar!!.setVisible(seekBarMode != SEEKBAR_MODE_SINGLE)
    }

    fun getProgressDrawableId(): Int {
        return progressDrawableId
    }

    fun setProgressDrawableId(@DrawableRes progressDrawableId: Int) {
        this.progressDrawableId = progressDrawableId
        progressBitmap = null
        initProgressBitmap()
    }

    fun getProgressDefaultDrawableId(): Int {
        return progressDefaultDrawableId
    }

    fun setProgressDefaultDrawableId(@DrawableRes progressDefaultDrawableId: Int) {
        this.progressDefaultDrawableId = progressDefaultDrawableId
        progressDefaultBitmap = null
        initProgressBitmap()
    }


    fun setTypeface(typeFace: Typeface) {
        paint.typeface = typeFace
    }

    fun getStepsDrawableId(): Int {
        return stepsDrawableId
    }

    fun setStepsDrawableId(@DrawableRes stepsDrawableId: Int) {
        this.stepsBitmaps.clear()
        this.stepsDrawableId = stepsDrawableId
        initStepsBitmap()
    }

    fun getStepsBitmaps(): List<Bitmap> {
        return stepsBitmaps
    }

    fun setStepsBitmaps(stepsBitmaps: List<Bitmap>?) {
        if (stepsBitmaps == null || stepsBitmaps.isEmpty() || stepsBitmaps.size <= steps) {
            throw IllegalArgumentException("stepsBitmaps must > steps !")
        }
        this.stepsBitmaps.clear()
        this.stepsBitmaps.addAll(stepsBitmaps)
    }

    fun setStepsDrawable(stepsDrawableIds: List<Int>?) {
        if (stepsDrawableIds == null || stepsDrawableIds.isEmpty() || stepsDrawableIds.size <= steps) {
            throw IllegalArgumentException("stepsDrawableIds must > steps !")
        }
        if (!verifyStepsMode()) {
            throw IllegalArgumentException("stepsWidth must > 0, stepsHeight must > 0,steps must > 0 First!!")
        }
        val stepsBitmaps = ArrayList<Bitmap>()
        for (i in stepsDrawableIds.indices) {
            stepsBitmaps.add(Utils.drawableToBitmap(context, stepsWidth.toInt(), stepsHeight.toInt(), stepsDrawableIds[i]))
        }
        setStepsBitmaps(stepsBitmaps)
    }

    companion object {

        private val MIN_INTERCEPT_DISTANCE = 100

        //normal seekBar mode
        const val SEEKBAR_MODE_SINGLE = 1
        //RangeSeekBar
        const val SEEKBAR_MODE_RANGE = 2

        //number according to the actual proportion of the number of arranged;
        const val TRICK_MARK_MODE_NUMBER = 0
        //other equally arranged
        const val TRICK_MARK_MODE_OTHER = 1

        //tick mark text gravity
        const val TICK_MARK_GRAVITY_LEFT = 0
        const val TICK_MARK_GRAVITY_CENTER = 1
        const val TICK_MARK_GRAVITY_RIGHT = 2
    }
}
