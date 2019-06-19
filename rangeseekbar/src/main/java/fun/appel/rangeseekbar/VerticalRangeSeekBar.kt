package `fun`.appel.rangeseekbar

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.support.annotation.IntDef
import android.text.TextUtils
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ViewGroup

import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy

class VerticalRangeSeekBar(context: Context, attrs: AttributeSet? = null) : RangeSeekBar(context, attrs) {

    /**
     * set VerticalRangeSeekBar Orientation
     * [.DIRECTION_LEFT]
     * [.DIRECTION_RIGHT]
     * @param orientation
     */
    var orientation = DIRECTION_LEFT
    /**
     * set tick mark text direction
     * [.TEXT_DIRECTION_VERTICAL]
     * [.TEXT_DIRECTION_HORIZONTAL]
     * @param tickMarkDirection
     */
    var tickMarkDirection = TEXT_DIRECTION_VERTICAL

    private var maxTickMarkWidth: Int = 0


    override val tickMarkRawHeight: Int
        get() {
            if (maxTickMarkWidth > 0) return tickMarkTextMargin + maxTickMarkWidth
            if (tickMarkTextArray != null && tickMarkTextArray.size > 0) {
                val arrayLength = tickMarkTextArray.size
                maxTickMarkWidth = Utils.measureText(tickMarkTextArray[0].toString(), tickMarkTextSize.toFloat()).width()
                for (i in 1 until arrayLength) {
                    val width = Utils.measureText(tickMarkTextArray[i].toString(), tickMarkTextSize.toFloat()).width()
                    if (maxTickMarkWidth < width) {
                        maxTickMarkWidth = width
                    }
                }
                return tickMarkTextMargin + maxTickMarkWidth
            }
            return 0
        }

    override var tickMarkTextSize: Int
        get() = super.tickMarkTextSize
        set(tickMarkTextSize) {
            super.tickMarkTextSize = tickMarkTextSize
            maxTickMarkWidth = 0
        }

    override var tickMarkTextArray: Array<CharSequence>
        get() = super.tickMarkTextArray
        set(tickMarkTextArray) {
            super.tickMarkTextArray = tickMarkTextArray
            maxTickMarkWidth = 0
        }

    /**
     * if is single mode, please use it to get the SeekBar
     *
     * @return left seek bar
     */
    override var leftSeekBar: VerticalSeekBar?
        get() = leftSeekBar
        set(value: VerticalSeekBar?) {
            super.leftSeekBar = value
        }

    override var rightSeekBar: VerticalSeekBar?
        get() = rightSeekBar
        set(value: VerticalSeekBar?) {
            super.rightSeekBar = value
        }

    //text direction of VerticalRangeSeekBar. include indicator and tickMark

    /**
     * @hide
     */
    @IntDef(TEXT_DIRECTION_VERTICAL, TEXT_DIRECTION_HORIZONTAL)
    @Retention(RetentionPolicy.SOURCE)
    annotation class TextDirectionDef

    //direction of VerticalRangeSeekBar

    /**
     * @hide
     */
    @IntDef(DIRECTION_LEFT, DIRECTION_RIGHT)
    @Retention(RetentionPolicy.SOURCE)
    annotation class DirectionDef

    init {
        initAttrs(attrs)
        initSeekBar(attrs)
    }

    private fun initAttrs(attrs: AttributeSet?) {
        try {
            val t = context.obtainStyledAttributes(attrs, R.styleable.VerticalRangeSeekBar)
            orientation = t.getInt(R.styleable.VerticalRangeSeekBar_rsb_orientation, DIRECTION_LEFT)
            tickMarkDirection = t.getInt(R.styleable.VerticalRangeSeekBar_rsb_tick_mark_orientation, TEXT_DIRECTION_VERTICAL)
            t.recycle()
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }


    protected fun initSeekBar(attrs: AttributeSet?) {
        leftSeekBar = VerticalSeekBar(this, attrs, true)
        rightSeekBar = VerticalSeekBar(this, attrs, false)
        rightSeekBar!!.setVisible(seekBarMode != RangeSeekBar.SEEKBAR_MODE_SINGLE)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(h, w, oldh, oldw)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var widthSize = View.MeasureSpec.getSize(widthMeasureSpec)
        val widthMode = View.MeasureSpec.getMode(widthMeasureSpec)
        /*
         * onMeasure传入的widthMeasureSpec和heightMeasureSpec不是一般的尺寸数值，而是将模式和尺寸组合在一起的数值
         * MeasureSpec.EXACTLY 是精确尺寸
         * MeasureSpec.AT_MOST 是最大尺寸
         * MeasureSpec.UNSPECIFIED 是未指定尺寸
         */

        if (widthMode == View.MeasureSpec.EXACTLY) {
            widthSize = View.MeasureSpec.makeMeasureSpec(widthSize, View.MeasureSpec.EXACTLY)
        } else if (widthMode == View.MeasureSpec.AT_MOST && parent is ViewGroup
                && widthSize == ViewGroup.LayoutParams.MATCH_PARENT) {
            widthSize = View.MeasureSpec.makeMeasureSpec((parent as ViewGroup).measuredHeight, View.MeasureSpec.AT_MOST)
        } else {
            val heightNeeded: Int
            if (gravity == RangeSeekBar.Gravity.CENTER) {
                heightNeeded = 2 * progressTop + progressHeight
            } else {
                heightNeeded = rawHeight.toInt()
            }
            widthSize = View.MeasureSpec.makeMeasureSpec(heightNeeded, View.MeasureSpec.EXACTLY)
        }
        super.onMeasure(widthSize, heightMeasureSpec)
    }

    override fun onDraw(canvas: Canvas) {
        if (orientation == DIRECTION_LEFT) {
            canvas.rotate(-90f)
            canvas.translate((-height).toFloat(), 0f)
        } else {
            canvas.rotate(90f)
            canvas.translate(0f, (-width).toFloat())
        }
        super.onDraw(canvas)
    }

    override fun onDrawTickMark(canvas: Canvas, paint: Paint) {
        if (tickMarkTextArray != null) {
            val arrayLength = tickMarkTextArray.size
            val trickPartWidth = progressWidth / (arrayLength - 1)
            for (i in 0 until arrayLength) {
                val text2Draw = tickMarkTextArray[i].toString()
                if (TextUtils.isEmpty(text2Draw)) continue
                paint.getTextBounds(text2Draw, 0, text2Draw.length, tickMarkTextRect)
                paint.color = tickMarkTextColor
                //平分显示
                val x: Float
                if (tickMarkMode == RangeSeekBar.TRICK_MARK_MODE_OTHER) {
                    if (tickMarkGravity == RangeSeekBar.TICK_MARK_GRAVITY_RIGHT) {
                        x = (progressLeft + i * trickPartWidth - tickMarkTextRect.width()).toFloat()
                    } else if (tickMarkGravity == RangeSeekBar.TICK_MARK_GRAVITY_CENTER) {
                        x = progressLeft + i * trickPartWidth - tickMarkTextRect.width() / 2f
                    } else {
                        x = (progressLeft + i * trickPartWidth).toFloat()
                    }
                } else {
                    val num = Utils.parseFloat(text2Draw)
                    val states = rangeSeekBarState
                    if (Utils.compareFloat(num, states[0].value) != -1 && Utils.compareFloat(num, states[1].value) != 1 && seekBarMode == RangeSeekBar.SEEKBAR_MODE_RANGE) {
                        paint.color = tickMarkInRangeTextColor
                    }
                    //按实际比例显示
                    x = progressLeft + progressWidth * (num - minProgress) / (maxProgress - minProgress) - tickMarkTextRect.width() / 2f
                }
                val y: Float
                if (tickMarkLayoutGravity == RangeSeekBar.Gravity.TOP) {
                    y = (progressTop - tickMarkTextMargin).toFloat()
                } else {
                    y = (progressBottom + tickMarkTextMargin + tickMarkTextRect.height()).toFloat()
                }
                var degrees = 0
                val rotateX = x + tickMarkTextRect.width() / 2f
                val rotateY = y - tickMarkTextRect.height() / 2f
                if (tickMarkDirection == TEXT_DIRECTION_VERTICAL) {
                    if (orientation == DIRECTION_LEFT) {
                        degrees = 90
                    } else if (orientation == DIRECTION_RIGHT) {
                        degrees = -90
                    }
                }
                if (degrees != 0) {
                    canvas.rotate(degrees.toFloat(), rotateX, rotateY)
                }
                canvas.drawText(text2Draw, x, y, paint)
                if (degrees != 0) {
                    canvas.rotate((-degrees).toFloat(), rotateX, rotateY)
                }
            }
        }

    }

    override fun getEventX(event: MotionEvent): Float {
        return if (orientation == DIRECTION_LEFT) {
            height - event.y
        } else {
            event.y
        }
    }

    override fun getEventY(event: MotionEvent): Float {
        return if (orientation == DIRECTION_LEFT) {
            event.x
        } else {
            -event.x + width
        }
    }

    companion object {

        val TEXT_DIRECTION_VERTICAL = 1
        val TEXT_DIRECTION_HORIZONTAL = 2

        val DIRECTION_LEFT = 1
        val DIRECTION_RIGHT = 2
    }
}
