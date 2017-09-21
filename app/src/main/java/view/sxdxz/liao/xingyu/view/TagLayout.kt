package view.sxdxz.liao.xingyu.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

/**
 *  TagView是一个布局。它的孩子会随着添加顺序从左到右，从上到下依次排列。它的排列方式遵循以下原则
 *  1.当横向的空间足够放入一个孩子时，这个孩子总是会排在现有的孩子右边
 *  2.当横向的空间不足以放入一个孩子时，这个孩子会在下一行排列
 *  3.每行的行高取决于这一行里面最高的孩子
 *  4.当布局的宽度为wrap_content时，每行的长度等于这一行所有孩子的宽度的和
 *
 *  可以使用@param setLineMaxColumn() 来设置一行最多有多少列。默认最多5列
 *  当一个元素是改行最后的元素时，可以使用0dp来设置该元素占用剩余空间
 * Created by liaoxingyu on 8/27/15.
 */
class TagLayout @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ViewGroup(context, attrs, defStyleAttr) {
    private var maxLineColumn = 5
    private val childPositions = mutableMapOf<View, ChildPosition>()

    private class ChildPosition(val view: View) {
        var left = 0
        var right = 0
        var top = 0
        var bottom = 0
        override fun toString(): String =
                "ChildPosition(view=${if (view is TextView) view.text else view}, left=$left, right=$right, top=$top, bottom=$bottom)"
    }

    override fun onFinishInflate() {
        if (isInEditMode && childCount == 0) {
            val tags = arrayOf("tag0", "tag1", "tag2", "tag3", "tag four", "tag five", "tag six")
            setTags(tags)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = MeasureSpec.getSize(widthMeasureSpec) - paddingLeft - paddingRight
        val height = MeasureSpec.getSize(heightMeasureSpec) - paddingTop - paddingBottom

        //单行累积总宽
        var usedWidth = 0
        //单行最高元素的高
        var lineMaxHeight = 0
        //容器整体宽度
        var containerWidth = 0
        var containerHeight = 0
        var lineTop = 0
        var row = 0
        var col = 0

        fun hasEnoughWidth(childWidth: Int): Boolean = width - usedWidth >= childWidth

        fun setDisplaySize(cp: ChildPosition, child: View, layoutParams: MarginLayoutParams, startX: Int, startY: Int) {
            cp.left = startX + layoutParams.leftMargin
            cp.top = startY + layoutParams.topMargin
            cp.right = cp.left + child.measuredWidth
            cp.bottom = cp.top + child.measuredHeight
        }
        //used width表示图层已经使用的长度，childTotalWidth表示这个子类实际需要的总长（包括外边距）,displaySize表示子类显示需要的长度（不包括外边距）
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            if (child.visibility == View.GONE) continue
            val lp = child.layoutParams as MarginLayoutParams
            if (child.layoutParams.width == 0) {
                //match left space的情况
                child.layoutParams.width = width - usedWidth - lp.leftMargin - lp.rightMargin
                measureChildWithMargins(child, widthMeasureSpec, usedWidth, heightMeasureSpec, 0)
            } else {
                //假设父布局有足够的空间让子view放置
                measureChildWithMargins(child, widthMeasureSpec, 0, heightMeasureSpec, lineTop)
            }
            val childTotalWidth = child.measuredWidth + lp.leftMargin + lp.rightMargin
            val childTotalHeight = child.measuredHeight + lp.topMargin + lp.bottomMargin

            var cp = childPositions[child]
            if (cp == null) {
                cp = ChildPosition(child)
                childPositions.put(child, cp)
            }

            if (hasEnoughWidth(childTotalWidth) && col < maxLineColumn) {
                setDisplaySize(cp, child, lp, usedWidth, lineTop)

                usedWidth += childTotalWidth
                col++
                if (childTotalHeight > lineMaxHeight) lineMaxHeight = childTotalHeight
                if (usedWidth > containerWidth) containerWidth = usedWidth
            } else {
                col = 0
                row++
                usedWidth = 0
                containerHeight += lineMaxHeight
                lineTop = containerHeight

                setDisplaySize(cp, child, lp, usedWidth, lineTop)

                usedWidth = childTotalWidth
                lineMaxHeight = child.measuredHeight + lp.topMargin + lp.bottomMargin
                println("next line")
            }
            println("usedWidth=$usedWidth, lineMaxHeight=$lineMaxHeight, ctnH=$containerHeight, c=$cp")
        }
        containerHeight += lineMaxHeight

        val wMode = MeasureSpec.getMode(widthMeasureSpec)
        val hMode = MeasureSpec.getMode(heightMeasureSpec)

        var finalWidth = width
        if (layoutParams.width != LayoutParams.MATCH_PARENT) {
            finalWidth = containerWidth
        }
        val myLp = layoutParams as MarginLayoutParams
        //判断该类的父容器期望的高
        val finalHeight = when (MeasureSpec.getMode(height)) {
            MeasureSpec.EXACTLY -> when {
                myLp.height >= 0 -> myLp.height
                myLp.height == LayoutParams.MATCH_PARENT -> height
                else -> minOf(height, containerHeight)
            }
            else -> {
                when {
                    myLp.height >= 0 -> myLp.height
                    myLp.height == LayoutParams.MATCH_PARENT -> minOf(height, containerHeight)
                    else -> minOf(height, containerHeight)
                }
            }
        }
        //FIXME 最后一个元素的matchParent在linearLayout无效，measureChildWithMargins得出的值是wrap content的值
        setMeasuredDimension(finalWidth, finalHeight)
        println("end measure lpw=${myLp.width} lph=${myLp.height} maxH=$height ctnH=$containerHeight w=$width, h=$height, wm=${Utils.toMeasureModeName(wMode)}, hm=${Utils.toMeasureModeName(hMode)}")
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            if (child.visibility == View.GONE) continue
            val layoutPosition = childPositions[child] ?: continue
            //TODO 17.9.21 如果height 小于containerHeight，即该类可使用的最大高小于该类的子类所需要的高，那应该通知子类做调整。目前直接不显示该子类
            if (layoutPosition.bottom > measuredHeight) continue
            child.layout(layoutPosition.left, layoutPosition.top
                    , layoutPosition.right, layoutPosition.bottom)
        }
    }

    override fun generateDefaultLayoutParams(): LayoutParams =
            MarginLayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)

    override fun generateLayoutParams(attrs: AttributeSet?): LayoutParams =
            MarginLayoutParams(context, attrs)

    override fun generateLayoutParams(p: LayoutParams?): LayoutParams =
            MarginLayoutParams(p)

    fun setLineMaxColumn(column: Int) {
        maxLineColumn = column
    }

    fun setTags(tags: Array<String>) {
        for (t in tags) {
            val textView = TextView(context)
            textView.text = t
            addView(textView)
        }
    }

    fun setTags(tags: List<String>) {
        for (t in tags) {
            val textView = TextView(context)
            val lp = ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            lp.setMargins(4, 4, 4, 4)
            textView.layoutParams = lp
            textView.text = t
            addView(textView)
        }
    }

    override fun setOnClickListener(l: View.OnClickListener?) {
        var i = 0
        val count = childCount
        while (i < count) {
            val child = getChildAt(i)
            child.setOnClickListener(l)
            i++
        }
    }
}

object Utils {
    fun toMeasureModeName(mode: Int): String = when (mode) {
        View.MeasureSpec.EXACTLY -> "EXACTLY"
        View.MeasureSpec.AT_MOST -> "AT_MOST"
        else -> "UNSPECIFIED"
    }
}

class LogTextView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : TextView(context, attrs, defStyleAttr) {
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val w = MeasureSpec.getSize(widthMeasureSpec)
        val wm = MeasureSpec.getMode(widthMeasureSpec)
        val h = MeasureSpec.getSize(heightMeasureSpec)
        val hm = MeasureSpec.getMode(heightMeasureSpec)
        println("logTextView w=$w wm=${Utils.toMeasureModeName(wm)} h=$h hm=${Utils.toMeasureModeName(hm)} lpw=${layoutParams.width} lph=${layoutParams.height}")
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }
}