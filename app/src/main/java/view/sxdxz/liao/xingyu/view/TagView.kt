package view.sxdxz.liao.xingyu.view

import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import java.util.*

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
class TagView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ViewGroup(context, attrs, defStyleAttr) {
    private var maxLineColumn = 5
    private var maxHeight: Int = 0
    private val layoutCaches = mutableMapOf<View, LayoutPosition>()

    private class LayoutPosition(val view: View) {
        var left = 0
        var right = 0
        var top = 0
        var bottom = 0
        override fun toString(): String =
                "LayoutPosition(view=${if (view is TextView) view.text else view}, left=$left, right=$right, top=$top, bottom=$bottom)"
    }

    init {
        maxHeight = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 48f, resources.displayMetrics).toInt()
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

        //单行累积总宽，即最宽元素的宽
        var usedWidth = 0
        //单行累积总高，即最高元素的高
        var usedHeight = 0
        //容器整体宽度
        var containerWidth = 0
        var containerHeight = 0
        var lineTop = 0
        var row = 0
        var col = 0

        fun hasEnoughSpace(childWidth: Int): Boolean = width - usedWidth >= childWidth

        for (i in 0 until childCount) {
            val child = getChildAt(i)
            if (child.visibility == View.GONE) continue
            //假设父布局有足够的空间让子view放置
            if (child.layoutParams.width == 0) {
                child.layoutParams.width = width - usedWidth
                measureChildWithMargins(child, widthMeasureSpec, usedWidth, heightMeasureSpec, 0)
            } else measureChildWithMargins(child, widthMeasureSpec, 0, heightMeasureSpec, 0)
            val lp = child.layoutParams as MarginLayoutParams
            val childWidth = child.measuredWidth + lp.leftMargin + lp.rightMargin
            val childHeight = child.measuredHeight + lp.topMargin + lp.bottomMargin

            var layoutCache = layoutCaches[child]
            if (layoutCache == null) {
                layoutCache = LayoutPosition(child)
                layoutCaches.put(child, layoutCache)
            }

            if (hasEnoughSpace(childWidth) && col < maxLineColumn) {
                layoutCache.left = usedWidth
                layoutCache.top = lineTop
                layoutCache.right = layoutCache.left + childWidth
                layoutCache.bottom = layoutCache.top + childHeight

                usedWidth += childWidth
                col++
                if (childHeight > usedHeight) usedHeight = childHeight
                if (usedWidth > containerWidth) containerWidth = usedWidth
            } else {
                col = 0
                row++
                usedWidth = 0
                containerHeight += usedHeight
                lineTop = containerHeight

                layoutCache.left = usedWidth
                layoutCache.top = lineTop
                layoutCache.right = layoutCache.left + childWidth
                layoutCache.bottom = layoutCache.top + childHeight

                usedWidth = childWidth
                usedHeight = childHeight
                println("next line")
            }
            println("usedWidth=$usedWidth, usedHeight=$usedHeight")

            println("v=${if (child is TextView) child.text else child},c=$layoutCache")
        }
        containerHeight += usedHeight

        val wMode = MeasureSpec.getMode(widthMeasureSpec)
        val hMode = MeasureSpec.getMode(heightMeasureSpec)

        var finalWidth = width
        var finalHeight = height
        if (layoutParams.width != LayoutParams.MATCH_PARENT) {
            finalWidth = containerWidth
        }
        if (layoutParams.height != LayoutParams.MATCH_PARENT) {
            finalHeight = containerHeight
        }
        setMeasuredDimension(finalWidth, finalHeight)
        println("end measure w=$width, h=$height, wm=${Utils.toMeasureModeName(wMode)}, hm=${Utils.toMeasureModeName(hMode)}")
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            if (child.visibility == View.GONE) continue
            val layoutPosition = layoutCaches[child] ?: continue
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

    fun setTags(tags: ArrayList<String>) {
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
        println("w=$w wm=${Utils.toMeasureModeName(wm)} h=$h hm=${Utils.toMeasureModeName(hm)}")
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }
}