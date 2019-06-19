package `fun`.appel.rangeseekbar

import android.content.res.TypedArray
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet

import VerticalRangeSeekBar.DIRECTION_LEFT
import VerticalRangeSeekBar.DIRECTION_RIGHT
import VerticalRangeSeekBar.TEXT_DIRECTION_VERTICAL


/**
 * //                       _ooOoo_
 * //                      o8888888o
 * //                      88" . "88
 * //                      (| -_- |)
 * //                       O\ = /O
 * //                   ____/`---'\____
 * //                 .   ' \\| |// `.
 * //                  / \\||| : |||// \
 * //                / _||||| -:- |||||- \
 * //                  | | \\\ - /// | |
 * //                | \_| ''\---/'' | |
 * //                 \ .-\__ `-` ___/-. /
 * //              ______`. .' /--.--\ `. . __
 * //           ."" '< `.___\_<|>_/___.' >'"".
 * //          | | : `- \`.;`\ _ /`;.`/ - ` : | |
 * //            \ \ `-. \_ __\ /__ _/ .-` / /
 * //    ======`-.____`-.___\_____/___.-`____.-'======
 * //                       `=---='
 * //
 * //    .............................................
 * //             佛祖保佑             永无BUG
 * =====================================================
 * 作    者：JayGoo
 * 创建日期：2019-06-05
 * 描    述:
 * =====================================================
 */
class VerticalSeekBar(rangeSeekBar: RangeSeekBar, attrs: AttributeSet, isLeft: Boolean) : SeekBar(rangeSeekBar, attrs, isLeft) {

    var indicatorTextOrientation: Int = 0
    internal var verticalSeekBar: VerticalRangeSeekBar? = null

    init {
        initAttrs(attrs)
        verticalSeekBar = rangeSeekBar as VerticalRangeSeekBar
    }

    private fun initAttrs(attrs: AttributeSet) {
        try {
            val t = context!!.obtainStyledAttributes(attrs, R.styleable.VerticalRangeSeekBar)
            indicatorTextOrientation = t.getInt(R.styleable.VerticalRangeSeekBar_rsb_indicator_text_orientation, TEXT_DIRECTION_VERTICAL)
            t.recycle()
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    override fun onDrawIndicator(canvas: Canvas, paint: Paint, text2Draw: String?) {
        if (text2Draw == null) return
        //draw indicator
        if (indicatorTextOrientation == TEXT_DIRECTION_VERTICAL) {
            drawVerticalIndicator(canvas, paint, text2Draw)
        } else {
            super.onDrawIndicator(canvas, paint, text2Draw)
        }
    }

    protected fun drawVerticalIndicator(canvas: Canvas, paint: Paint, text2Draw: String) {
        //measure indicator text
        paint.textSize = indicatorTextSize.toFloat()
        paint.style = Paint.Style.FILL
        paint.color = indicatorBackgroundColor
        paint.getTextBounds(text2Draw, 0, text2Draw.length, indicatorTextRect)

        var realIndicatorWidth = indicatorTextRect.height() + indicatorPaddingLeft + indicatorPaddingRight
        if (indicatorWidth > realIndicatorWidth) {
            realIndicatorWidth = indicatorWidth
        }

        var realIndicatorHeight = indicatorTextRect.width() + indicatorPaddingTop + indicatorPaddingBottom
        if (indicatorHeight > realIndicatorHeight) {
            realIndicatorHeight = indicatorHeight
        }

        indicatorRect.left = scaleThumbWidth / 2 - realIndicatorWidth / 2
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
        val tx = indicatorRect.left + (indicatorRect.width() - indicatorTextRect.width()) / 2 + indicatorPaddingLeft - indicatorPaddingRight
        val ty = indicatorRect.bottom - (indicatorRect.height() - indicatorTextRect.height()) / 2 + indicatorPaddingTop - indicatorPaddingBottom

        //draw indicator text
        paint.color = indicatorTextColor

        var degrees = 0
        val rotateX = tx + indicatorTextRect.width() / 2f
        val rotateY = ty - indicatorTextRect.height() / 2f

        if (indicatorTextOrientation == TEXT_DIRECTION_VERTICAL) {
            if (verticalSeekBar!!.orientation == DIRECTION_LEFT) {
                degrees = 90
            } else if (verticalSeekBar!!.orientation == DIRECTION_RIGHT) {
                degrees = -90
            }
        }
        if (degrees != 0) {
            canvas.rotate(degrees.toFloat(), rotateX, rotateY)
        }
        canvas.drawText(text2Draw, tx.toFloat(), ty.toFloat(), paint)
        if (degrees != 0) {
            canvas.rotate((-degrees).toFloat(), rotateX, rotateY)
        }
    }
}
