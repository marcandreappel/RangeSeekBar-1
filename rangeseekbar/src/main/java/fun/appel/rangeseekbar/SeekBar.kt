package `fun`.appel.rangeseekbar

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.content.res.Resources
import android.content.res.TypedArray
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import android.os.Build
import android.support.annotation.ColorInt
import android.support.annotation.DrawableRes
import android.support.annotation.IntDef
import android.support.v4.content.ContextCompat
import android.text.TextUtils
import android.util.AttributeSet

import java.text.DecimalFormat


/**
 * ================================================
 * 作    者：JayGoo
 * 版    本：
 * 创建日期：2018/5/8
 * 描    述:
 * ================================================
 */

open class SeekBar(internal var rangeSeekBar: RangeSeekBar?, attrs: AttributeSet, internal var isLeft: Boolean) {

    /**
     * the indicator show mode
     * [.INDICATOR_SHOW_WHEN_TOUCH]
     * [.INDICATOR_ALWAYS_SHOW]
     * [.INDICATOR_ALWAYS_SHOW_AFTER_TOUCH]
     * [.INDICATOR_ALWAYS_SHOW]
     * @param indicatorShowMode
     */
    var indicatorShowMode: Int = 0

    //进度提示背景的高度，宽度如果是0的话会自适应调整
    //Progress prompted the background height, width,
    var indicatorHeight: Int = 0
    var indicatorWidth: Int = 0
    //进度提示背景与按钮之间的距离
    //The progress indicates the distance between the background and the button
    var indicatorMargin: Int = 0
    private var indicatorDrawableId: Int = 0
    var indicatorArrowSize: Int = 0
    var indicatorTextSize: Int = 0
    var indicatorTextColor: Int = 0
    var indicatorRadius: Float = 0.toFloat()
    var indicatorBackgroundColor: Int = 0
    var indicatorPaddingLeft: Int = 0
    var indicatorPaddingRight: Int = 0
    var indicatorPaddingTop: Int = 0
    var indicatorPaddingBottom: Int = 0
    private var thumbDrawableId: Int = 0
    var thumbInactivatedDrawableId: Int = 0
        private set
    var thumbWidth: Int = 0
    var thumbHeight: Int = 0

    //when you touch or move, the thumb will scale, default not scale
    /**
     * when you touch or move, the thumb will scale, default not scale
     *
     * @return default 1.0f
     */
    var thumbScaleRatio: Float = 0.toFloat()
        internal set

    //****************** the above is attr value  ******************//

    internal var left: Int = 0
    internal var right: Int = 0
    internal var top: Int = 0
    internal var bottom: Int = 0
    internal var currPercent: Float = 0.toFloat()
    internal var material = 0f
    var isShowIndicator: Boolean = false
        private set
    internal var thumbBitmap: Bitmap? = null
    internal var thumbInactivatedBitmap: Bitmap? = null
    internal var indicatorBitmap: Bitmap? = null
    internal var anim: ValueAnimator? = null
    internal var userText2Draw: String? = null
    protected var activate = false
    /**
     * if visble is false, will clear the Canvas
     *
     * @param visible
     */
    var isVisible = true
    internal var indicatorTextStringFormat: String? = null
    internal var indicatorArrowPath = Path()
    internal var indicatorTextRect = Rect()
    internal var indicatorRect = Rect()
    internal var paint = Paint(Paint.ANTI_ALIAS_FLAG)
    var indicatorTextDecimalFormat: DecimalFormat? = null
        internal set
    internal var scaleThumbWidth: Int = 0
    internal var scaleThumbHeight: Int = 0

    val context: Context?
        get() = rangeSeekBar!!.context

    val resources: Resources?
        get() = if (context != null) context!!.resources else null

    val rawHeight: Float
        get() = indicatorHeight.toFloat() + indicatorArrowSize.toFloat() + indicatorMargin.toFloat() + thumbScaleHeight

    /**
     * include indicator text Height、padding、margin
     *
     * @return The actual occupation height of indicator
     */
    val indicatorRawHeight: Int
        get() = if (indicatorHeight > 0) {
            if (indicatorBitmap != null) {
                indicatorHeight + indicatorMargin
            } else {
                indicatorHeight + indicatorArrowSize + indicatorMargin
            }
        } else {
            if (indicatorBitmap != null) {
                Utils.measureText("8", indicatorTextSize.toFloat()).height() + indicatorPaddingTop + indicatorPaddingBottom + indicatorMargin
            } else {
                Utils.measureText("8", indicatorTextSize.toFloat()).height() + indicatorPaddingTop + indicatorPaddingBottom + indicatorMargin + indicatorArrowSize
            }
        }

    val thumbScaleHeight: Float
        get() = thumbHeight * thumbScaleRatio

    val thumbScaleWidth: Float
        get() = thumbWidth * thumbScaleRatio

    val progress: Float
        get() {
            val range = rangeSeekBar!!.maxProgress - rangeSeekBar!!.minProgress
            return rangeSeekBar!!.minProgress + range * currPercent
        }

    @IntDef(INDICATOR_SHOW_WHEN_TOUCH, INDICATOR_ALWAYS_HIDE, INDICATOR_ALWAYS_SHOW_AFTER_TOUCH, INDICATOR_ALWAYS_SHOW)
    annotation class IndicatorModeDef

    init {
        initAttrs(attrs)
        initBitmap()
        initVariables()
    }

    private fun initAttrs(attrs: AttributeSet) {
        val t = context!!.obtainStyledAttributes(attrs, R.styleable.RangeSeekBar) ?: return
        indicatorMargin = t.getDimension(R.styleable.RangeSeekBar_rsb_indicator_margin, 0f).toInt()
        indicatorDrawableId = t.getResourceId(R.styleable.RangeSeekBar_rsb_indicator_drawable, 0)
        indicatorShowMode = t.getInt(R.styleable.RangeSeekBar_rsb_indicator_show_mode, INDICATOR_ALWAYS_HIDE)
        indicatorHeight = t.getLayoutDimension(R.styleable.RangeSeekBar_rsb_indicator_height, WRAP_CONTENT)
        indicatorWidth = t.getLayoutDimension(R.styleable.RangeSeekBar_rsb_indicator_width, WRAP_CONTENT)
        indicatorTextSize = t.getDimension(R.styleable.RangeSeekBar_rsb_indicator_text_size, Utils.dp2px(context, 14f).toFloat()).toInt()
        indicatorTextColor = t.getColor(R.styleable.RangeSeekBar_rsb_indicator_text_color, Color.WHITE)
        indicatorBackgroundColor = t.getColor(R.styleable.RangeSeekBar_rsb_indicator_background_color, ContextCompat.getColor(context!!, R.color.colorAccent))
        indicatorPaddingLeft = t.getDimension(R.styleable.RangeSeekBar_rsb_indicator_padding_left, 0f).toInt()
        indicatorPaddingRight = t.getDimension(R.styleable.RangeSeekBar_rsb_indicator_padding_right, 0f).toInt()
        indicatorPaddingTop = t.getDimension(R.styleable.RangeSeekBar_rsb_indicator_padding_top, 0f).toInt()
        indicatorPaddingBottom = t.getDimension(R.styleable.RangeSeekBar_rsb_indicator_padding_bottom, 0f).toInt()
        indicatorArrowSize = t.getDimension(R.styleable.RangeSeekBar_rsb_indicator_arrow_size, 0f).toInt()
        thumbDrawableId = t.getResourceId(R.styleable.RangeSeekBar_rsb_thumb_drawable, R.drawable.rsb_default_thumb)
        thumbInactivatedDrawableId = t.getResourceId(R.styleable.RangeSeekBar_rsb_thumb_inactivated_drawable, 0)
        thumbWidth = t.getDimension(R.styleable.RangeSeekBar_rsb_thumb_width, Utils.dp2px(context, 26f).toFloat()).toInt()
        thumbHeight = t.getDimension(R.styleable.RangeSeekBar_rsb_thumb_height, Utils.dp2px(context, 26f).toFloat()).toInt()
        thumbScaleRatio = t.getFloat(R.styleable.RangeSeekBar_rsb_thumb_scale_ratio, 1f)
        indicatorRadius = t.getDimension(R.styleable.RangeSeekBar_rsb_indicator_radius, 0f)
        t.recycle()
    }

    protected fun initVariables() {
        scaleThumbWidth = thumbWidth
        scaleThumbHeight = thumbHeight
        if (indicatorHeight == WRAP_CONTENT) {
            indicatorHeight = Utils.measureText("8", indicatorTextSize.toFloat()).height() + indicatorPaddingTop + indicatorPaddingBottom
        }
        if (indicatorArrowSize <= 0) {
            indicatorArrowSize = thumbWidth / 4
        }
    }

    /**
     * 初始化进度提示的背景
     */
    private fun initBitmap() {
        setIndicatorDrawableId(indicatorDrawableId)
        setThumbDrawableId(thumbDrawableId, thumbWidth, thumbHeight)
        setThumbInactivatedDrawableId(thumbInactivatedDrawableId, thumbWidth, thumbHeight)
    }

    /**
     * 计算每个按钮的位置和尺寸
     * Calculates the position and size of each button
     *
     * @param x position x
     * @param y position y
     */
    protected fun onSizeChanged(x: Int, y: Int) {
        initVariables()
        initBitmap()
        left = (x - thumbScaleWidth / 2).toInt()
        right = (x + thumbScaleWidth / 2).toInt()
        top = y - thumbHeight / 2
        bottom = y + thumbHeight / 2
    }


    fun scaleThumb() {
        scaleThumbWidth = thumbScaleWidth.toInt()
        scaleThumbHeight = thumbScaleHeight.toInt()
        val y = rangeSeekBar!!.progressBottom
        top = y - scaleThumbHeight / 2
        bottom = y + scaleThumbHeight / 2
        setThumbDrawableId(thumbDrawableId, scaleThumbWidth, scaleThumbHeight)
    }

    fun resetThumb() {
        scaleThumbWidth = thumbWidth
        scaleThumbHeight = thumbHeight
        val y = rangeSeekBar!!.progressBottom
        top = y - scaleThumbHeight / 2
        bottom = y + scaleThumbHeight / 2
        setThumbDrawableId(thumbDrawableId, scaleThumbWidth, scaleThumbHeight)
    }

    /**
     * 绘制按钮和提示背景和文字
     * Draw buttons and tips for background and text
     *
     * @param canvas Canvas
     */
    protected fun draw(canvas: Canvas) {
        if (!isVisible) {
            return
        }
        val offset = (rangeSeekBar!!.progressWidth * currPercent).toInt()
        canvas.save()
        canvas.translate(offset.toFloat(), 0f)
        // translate canvas, then don't care left
        canvas.translate(left.toFloat(), 0f)
        if (isShowIndicator) {
            onDrawIndicator(canvas, paint, formatCurrentIndicatorText(userText2Draw))
        }
        onDrawThumb(canvas)
        canvas.restore()
    }


    /**
     * 绘制按钮
     * 如果没有图片资源，则绘制默认按钮
     *
     *
     * draw the thumb button
     * If there is no image resource, draw the default button
     *
     * @param canvas canvas
     */
    protected fun onDrawThumb(canvas: Canvas) {
        if (thumbInactivatedBitmap != null && !activate) {
            canvas.drawBitmap(thumbInactivatedBitmap!!, 0f, rangeSeekBar!!.progressTop + (rangeSeekBar!!.progressHeight - scaleThumbHeight) / 2f, null)
        } else if (thumbBitmap != null) {
            canvas.drawBitmap(thumbBitmap!!, 0f, rangeSeekBar!!.progressTop + (rangeSeekBar!!.progressHeight - scaleThumbHeight) / 2f, null)
        }
    }

    /**
     * 格式化提示文字
     * format the indicator text
     *
     * @param text2Draw
     * @return
     */
    protected fun formatCurrentIndicatorText(text2Draw: String?): String {
        var text2Draw = text2Draw
        val states = rangeSeekBar!!.rangeSeekBarState
        if (TextUtils.isEmpty(text2Draw)) {
            if (isLeft) {
                if (indicatorTextDecimalFormat != null) {
                    text2Draw = indicatorTextDecimalFormat!!.format(states[0].value.toDouble())
                } else {
                    text2Draw = states[0].indicatorText
                }
            } else {
                if (indicatorTextDecimalFormat != null) {
                    text2Draw = indicatorTextDecimalFormat!!.format(states[1].value.toDouble())
                } else {
                    text2Draw = states[1].indicatorText
                }
            }
        }
        if (indicatorTextStringFormat != null) {
            text2Draw = String.format(indicatorTextStringFormat!!, text2Draw)
        }
        return text2Draw
    }

    /**
     * This method will draw the indicator background dynamically according to the text.
     * you can use to set padding
     *
     * @param canvas    Canvas
     * @param text2Draw Indicator text
     */
    protected open fun onDrawIndicator(canvas: Canvas, paint: Paint, text2Draw: String?) {
        if (text2Draw == null) return
        paint.textSize = indicatorTextSize.toFloat()
        paint.style = Paint.Style.FILL
        paint.color = indicatorBackgroundColor
        paint.getTextBounds(text2Draw, 0, text2Draw.length, indicatorTextRect)
        var realIndicatorWidth = indicatorTextRect.width() + indicatorPaddingLeft + indicatorPaddingRight
        if (indicatorWidth > realIndicatorWidth) {
            realIndicatorWidth = indicatorWidth
        }

        var realIndicatorHeight = indicatorTextRect.height() + indicatorPaddingTop + indicatorPaddingBottom
        if (indicatorHeight > realIndicatorHeight) {
            realIndicatorHeight = indicatorHeight
        }

        indicatorRect.left = (scaleThumbWidth / 2f - realIndicatorWidth / 2f).toInt()
        indicatorRect.top = bottom - realIndicatorHeight - scaleThumbHeight - indicatorMargin
        indicatorRect.right = indicatorRect.left + realIndicatorWidth
        indicatorRect.bottom = indicatorRect.top + realIndicatorHeight
        //draw default indicator arrow
        if (indicatorBitmap == null) {
            //arrow three point
            //  b   c
            //    a
            val ax = scaleThumbWidth / 2
            val ay = indicatorRect.bottom
            val bx = ax - indicatorArrowSize
            val by = ay - indicatorArrowSize
            val cx = ax + indicatorArrowSize
            indicatorArrowPath.reset()
            indicatorArrowPath.moveTo(ax.toFloat(), ay.toFloat())
            indicatorArrowPath.lineTo(bx.toFloat(), by.toFloat())
            indicatorArrowPath.lineTo(cx.toFloat(), by.toFloat())
            indicatorArrowPath.close()
            canvas.drawPath(indicatorArrowPath, paint)
            indicatorRect.bottom -= indicatorArrowSize
            indicatorRect.top -= indicatorArrowSize
        }

        //indicator background edge processing
        val defaultPaddingOffset = Utils.dp2px(context, 1f)
        val leftOffset = indicatorRect.width() / 2 - (rangeSeekBar!!.progressWidth * currPercent).toInt() - rangeSeekBar!!.progressLeft + defaultPaddingOffset
        val rightOffset = indicatorRect.width() / 2 - (rangeSeekBar!!.progressWidth * (1 - currPercent)).toInt() - rangeSeekBar!!.progressPaddingRight + defaultPaddingOffset

        if (leftOffset > 0) {
            indicatorRect.left += leftOffset
            indicatorRect.right += leftOffset
        } else if (rightOffset > 0) {
            indicatorRect.left -= rightOffset
            indicatorRect.right -= rightOffset
        }

        //draw indicator background
        if (indicatorBitmap != null) {
            Utils.drawBitmap(canvas, paint, indicatorBitmap, indicatorRect)
        } else if (indicatorRadius > 0f) {
            canvas.drawRoundRect(RectF(indicatorRect), indicatorRadius, indicatorRadius, paint)
        } else {
            canvas.drawRect(indicatorRect, paint)
        }

        //draw indicator content text
        val tx: Int
        val ty: Int
        if (indicatorPaddingLeft > 0) {
            tx = indicatorRect.left + indicatorPaddingLeft
        } else if (indicatorPaddingRight > 0) {
            tx = indicatorRect.right - indicatorPaddingRight - indicatorTextRect.width()
        } else {
            tx = indicatorRect.left + (realIndicatorWidth - indicatorTextRect.width()) / 2
        }

        if (indicatorPaddingTop > 0) {
            ty = indicatorRect.top + indicatorTextRect.height() + indicatorPaddingTop
        } else if (indicatorPaddingBottom > 0) {
            ty = indicatorRect.bottom - indicatorTextRect.height() - indicatorPaddingBottom
        } else {
            ty = indicatorRect.bottom - (realIndicatorHeight - indicatorTextRect.height()) / 2 + 1
        }

        //draw indicator text
        paint.color = indicatorTextColor
        canvas.drawText(text2Draw, tx.toFloat(), ty.toFloat(), paint)
    }

    /**
     * 拖动检测
     *
     * @return is collide
     */
    protected fun collide(x: Float, y: Float): Boolean {
        val offset = (rangeSeekBar!!.progressWidth * currPercent).toInt()
        return x > left + offset && x < right + offset && y > top && y < bottom
    }

    protected fun slide(percent: Float) {
        var percent = percent
        if (percent < 0)
            percent = 0f
        else if (percent > 1) percent = 1f
        currPercent = percent
    }

    protected fun setShowIndicatorEnable(isEnable: Boolean) {
        when (indicatorShowMode) {
            INDICATOR_SHOW_WHEN_TOUCH -> isShowIndicator = isEnable
            INDICATOR_ALWAYS_SHOW, INDICATOR_ALWAYS_SHOW_AFTER_TOUCH -> isShowIndicator = true
            INDICATOR_ALWAYS_HIDE -> isShowIndicator = false
        }
    }

    fun materialRestore() {
        if (anim != null) anim!!.cancel()
        anim = ValueAnimator.ofFloat(material, 0)
        anim!!.addUpdateListener { animation ->
            material = animation.animatedValue as Float
            if (rangeSeekBar != null) rangeSeekBar!!.invalidate()
        }
        anim!!.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                material = 0f
                if (rangeSeekBar != null) rangeSeekBar!!.invalidate()
            }
        })
        anim!!.start()
    }

    fun setIndicatorText(text: String) {
        userText2Draw = text
    }

    fun setIndicatorTextDecimalFormat(formatPattern: String) {
        indicatorTextDecimalFormat = DecimalFormat(formatPattern)
    }

    fun setIndicatorTextStringFormat(formatPattern: String) {
        indicatorTextStringFormat = formatPattern
    }

    fun getIndicatorDrawableId(): Int {
        return indicatorDrawableId
    }

    fun setIndicatorDrawableId(@DrawableRes indicatorDrawableId: Int) {
        if (indicatorDrawableId != 0) {
            this.indicatorDrawableId = indicatorDrawableId
            indicatorBitmap = BitmapFactory.decodeResource(resources, indicatorDrawableId)
        }
    }

    fun showIndicator(isShown: Boolean) {
        isShowIndicator = isShown
    }

    fun setThumbInactivatedDrawableId(@DrawableRes thumbInactivatedDrawableId: Int, width: Int, height: Int) {
        if (thumbInactivatedDrawableId != 0 && resources != null) {
            this.thumbInactivatedDrawableId = thumbInactivatedDrawableId
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                thumbInactivatedBitmap = Utils.drawableToBitmap(width, height, resources!!.getDrawable(thumbInactivatedDrawableId, null))
            } else {
                thumbInactivatedBitmap = Utils.drawableToBitmap(width, height, resources!!.getDrawable(thumbInactivatedDrawableId))
            }
        }
    }

    fun getThumbDrawableId(): Int {
        return thumbDrawableId
    }

    fun setThumbDrawableId(@DrawableRes thumbDrawableId: Int, width: Int, height: Int) {
        if (thumbDrawableId != 0 && resources != null && width > 0 && height > 0) {
            this.thumbDrawableId = thumbDrawableId
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                thumbBitmap = Utils.drawableToBitmap(width, height, resources!!.getDrawable(thumbDrawableId, null))
            } else {
                thumbBitmap = Utils.drawableToBitmap(width, height, resources!!.getDrawable(thumbDrawableId))
            }
        }
    }

    fun setThumbDrawableId(@DrawableRes thumbDrawableId: Int) {
        if (thumbWidth <= 0 || thumbHeight <= 0) {
            throw IllegalArgumentException("please set thumbWidth and thumbHeight first!")
        }
        if (thumbDrawableId != 0 && resources != null) {
            this.thumbDrawableId = thumbDrawableId
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                thumbBitmap = Utils.drawableToBitmap(thumbWidth, thumbHeight, resources!!.getDrawable(thumbDrawableId, null))
            } else {
                thumbBitmap = Utils.drawableToBitmap(thumbWidth, thumbHeight, resources!!.getDrawable(thumbDrawableId))
            }
        }
    }

    fun setTypeface(typeFace: Typeface) {
        paint.typeface = typeFace
    }

    companion object {
        //the indicator show mode
        const val INDICATOR_SHOW_WHEN_TOUCH = 0
        const val INDICATOR_ALWAYS_HIDE = 1
        const val INDICATOR_ALWAYS_SHOW_AFTER_TOUCH = 2
        const val INDICATOR_ALWAYS_SHOW = 3

        const val WRAP_CONTENT = -1
        const val MATCH_PARENT = -2
    }
}
