package view.sxdxz.liao.xingyu.view

import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.view.View.MeasureSpec.makeMeasureSpec
import android.view.ViewGroup
import android.widget.TextView
import java.util.*

/**
 * Created by liaoxingyu on 8/27/15.
 * kiss me , bb.
 */
class TagView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ViewGroup(context, attrs, defStyleAttr) {
    private var mMaxLineColumn = 5
    private var maxHeight: Int = 0

    init {
        maxHeight = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 48f, resources.displayMetrics).toInt()
        if (isInEditMode) {
            val tags = arrayOf("tag0", "tag1", "tag2", "tag3", "tag four", "tag five", "tag six")
            setTags(tags)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = View.MeasureSpec.getSize(widthMeasureSpec) - paddingLeft - paddingRight
        val height = View.MeasureSpec.getSize(heightMeasureSpec) - paddingTop - paddingBottom

        val childCount = childCount
        if (childCount != 0) {
            val lineColumn = getLineColumn(childCount)

            val childWidth = width / lineColumn
            var childHeight = getCellHeight(height, lineColumn, childCount)
            if (childHeight > maxHeight) {
                childHeight = maxHeight
            }
            val childWidthSpec = makeMeasureSpec(childWidth, View.MeasureSpec.EXACTLY)
            val childHeightSpec = makeMeasureSpec(childHeight, View.MeasureSpec.EXACTLY)
            (0 until childCount)
                    .map { getChildAt(it) as TextView }
                    .forEach { it.measure(childWidthSpec, childHeightSpec) }
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    private fun getLineColumn(cellCount: Int): Int {
        var lineColumn = cellCount
        if (lineColumn >= mMaxLineColumn)
            lineColumn = mMaxLineColumn
        return lineColumn
    }

    private fun getCellHeight(layoutHeight: Int, lineColumn: Int, cellCount: Int): Int {
        val rowCount = Math.ceil(cellCount.toDouble() / lineColumn)
        return (layoutHeight / rowCount).toInt()
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        if (childCount == 0) return

        val lineColumn = getLineColumn(childCount)

        var i = 0
        var row = 0
        while (i < childCount) {
            val child = getChildAt(i)
            if (child.visibility != View.GONE) {
                val layoutParams = child.layoutParams
                var marginLeft = 0
                var marginTop = 0
                var marginRight = 0
                var marginBottom = 0

                if (layoutParams is ViewGroup.MarginLayoutParams) {
                    marginLeft = layoutParams.leftMargin
                    marginTop = layoutParams.topMargin
                    marginRight = layoutParams.rightMargin
                    marginBottom = layoutParams.bottomMargin
                }

                val childHeight = child.measuredHeight
                val childWidth = child.measuredWidth
                val col = i % lineColumn
                if (col == 0 && i != 0) row++

                val left = col * childWidth + paddingLeft + marginLeft
                val right = left + childWidth - marginLeft - marginRight
                val top = row * childHeight + paddingTop + marginTop
                val bottom = top + childHeight - marginTop - marginBottom

                child.layout(left, top, right, bottom)
            }
            i++
        }
    }

    fun setMaxLineColumn(column: Int) {
        mMaxLineColumn = column
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
            //            textView.setBackgroundResource(R.drawable.selector_tag_button);
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