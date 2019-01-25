package view.sxdxz.liao.xingyu.view

import android.content.Context
import android.support.v7.widget.AppCompatTextView
import android.util.AttributeSet
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.TextView

/**
 *  TagLayout是一个布局。它的孩子会随着添加顺序从左到右，从上到下依次排列。它的排列方式遵循以下原则
 *  1.当横向的空间足够放入一个孩子时，这个孩子总是会排在现有的孩子右边
 *  2.当横向的空间不足以放入一个孩子，或达到最大列数时，这个孩子会在下一行排列
 *  3.每行的行高取决于这一行里面最高的孩子
 *  4.当布局的宽度为wrap_content时，每行的长度等于这一行所有孩子的宽度的和
 *
 *  可以使用[maxLineColumn] 来设置一行最多有多少列。默认最多5列
 *  当一个元素是改行最后的元素时，可以使用0dp来设置该元素占用剩余空间
 *
 * Created by liaoxingyu on 8/27/15.
 */
class TagLayout @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ViewGroup(context, attrs, defStyleAttr) {
    var maxLineColumn = 5
    var verticalGap = 0

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
        super.onFinishInflate()
        if (isInEditMode && childCount == 0) {
            val tags = listOf("tag0", "tag1", "tag2", "tag3", "tag four", "tag five", "tag six")
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

            //加上间隙之后的偏移量
            val verticalOffset = if (col == 0) 0 else verticalGap
            val childTotalWidth = child.measuredWidth + lp.leftMargin + lp.rightMargin + verticalOffset
            val childTotalHeight = child.measuredHeight + lp.topMargin + lp.bottomMargin

            var cp = childPositions[child]
            if (cp == null) {
                cp = ChildPosition(child)
                childPositions[child] = cp
            }

            if (hasEnoughWidth(childTotalWidth) && col < maxLineColumn) {
                setDisplaySize(cp, child, lp, usedWidth + verticalOffset, lineTop)

                usedWidth += childTotalWidth
                col++
                if (childTotalHeight > lineMaxHeight) lineMaxHeight = childTotalHeight
                if (usedWidth > containerWidth) containerWidth = usedWidth
            } else {
                //上一行空间不够，将当前项填入下一行
                containerHeight += lineMaxHeight
                lineTop = containerHeight
                setDisplaySize(cp, child, lp, 0, lineTop)

                //当前行已是下一行，且第一个空已被填入，所以这里要重算usedWidth
                usedWidth = childTotalWidth
                lineMaxHeight = child.measuredHeight + lp.topMargin + lp.bottomMargin

                col = 1
                row++
                if (PRINT_LOG)
                    println("next line")
            }
            if (PRINT_LOG)
                println("usedWidth=$usedWidth, lineMaxHeight=$lineMaxHeight, ctnH=$containerHeight, c=$cp")
        }
        containerHeight += lineMaxHeight

        val wMode = MeasureSpec.getMode(widthMeasureSpec)
        val hMode = MeasureSpec.getMode(heightMeasureSpec)

        var finalWidth = width
        if (layoutParams.width != MATCH_PARENT) {
            finalWidth = containerWidth
        }
        val myLp = layoutParams
        //判断该类的父容器期望的高
        val finalHeight = when (hMode) {
            MeasureSpec.EXACTLY -> when {
                myLp.height >= 0 -> myLp.height
                myLp.height == MATCH_PARENT -> height
                else -> minOf(height, containerHeight)
            }
            MeasureSpec.UNSPECIFIED -> containerHeight
            MeasureSpec.AT_MOST -> {
                when {
                    myLp.height >= 0 -> myLp.height
                    myLp.height == MATCH_PARENT -> minOf(height, containerHeight)
                    else -> minOf(height, containerHeight)
                }
            }
            else -> minOf(height, containerHeight)
        }
        //TODO 17.9.21 如果height 小于containerHeight，即该类可使用的最大高小于该类的子类所需要的高，那应该通知子类做调整
        //FIXME 最后一个元素的matchParent在linearLayout无效，measureChildWithMargins得出的值是wrap content的值
        // TODO: 1/24/2019 textview中加入\n后，右边0DP的view会消失
        setMeasuredDimension(finalWidth, finalHeight)
        if (PRINT_LOG)
            println("end measure lpw=${myLp.width} lph=${myLp.height} maxH=$height ctnH=$containerHeight w=$width, h=$height, wm=${Utils.toMeasureModeName(wMode)}, hm=${Utils.toMeasureModeName(hMode)}")
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            if (child.visibility == View.GONE) continue
            val layoutPosition = childPositions[child] ?: continue
            if (layoutPosition.bottom > measuredHeight) continue
            child.layout(layoutPosition.left, layoutPosition.top
                    , layoutPosition.right, layoutPosition.bottom)
        }
    }

    override fun generateDefaultLayoutParams(): LayoutParams =
            MarginLayoutParams(WRAP_CONTENT, WRAP_CONTENT)

    override fun generateLayoutParams(attrs: AttributeSet?): LayoutParams =
            MarginLayoutParams(context, attrs)

    override fun generateLayoutParams(p: LayoutParams?): LayoutParams =
            MarginLayoutParams(p)

    fun setTags(tags: List<Any>) {
        for (t in tags) {
            val textView = AppCompatTextView(context)
            val lp = MarginLayoutParams(WRAP_CONTENT, WRAP_CONTENT)
            lp.setMargins(4, 4, 4, 4)
            textView.layoutParams = lp
            textView.text = t.toString()
            textView.tag = t
            textView.setOnClickListener(globalClickListener)
            addView(textView)
        }
    }

    private val globalClickListener: View.OnClickListener = OnClickListener {
        tagClickListener?.onTagClick(it.tag)
    }

    interface TagClickListener {
        fun onTagClick(tag: Any)
    }

    var tagClickListener: TagClickListener? = null

    companion object {
        const val PRINT_LOG = false
    }
}

object Utils {
    fun toMeasureModeName(mode: Int): String = when (mode) {
        View.MeasureSpec.EXACTLY -> "EXACTLY"
        View.MeasureSpec.AT_MOST -> "AT_MOST"
        else -> "UNSPECIFIED"
    }
}