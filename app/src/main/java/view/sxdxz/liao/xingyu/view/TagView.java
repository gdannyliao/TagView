package view.sxdxz.liao.xingyu.view;

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import static android.view.View.MeasureSpec.makeMeasureSpec;

/**
 * Created by liaoxingyu on 8/27/15.
 * kiss me , bb.
 */
public class TagView extends ViewGroup {
    private int mMaxLineColumn = 5;
    private int maxHeight;

    public TagView(Context context) {
        super(context);
        init();
    }

    public TagView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TagView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec) - getPaddingLeft() - getPaddingRight();
        int height = MeasureSpec.getSize(heightMeasureSpec) - getPaddingTop() - getPaddingBottom();

        int childCount = getChildCount();
        if (childCount != 0) {
            int lineColumn = getLineColumn(childCount);

            int childWidth = width / lineColumn;
            int childHeight = getCellHeight(height, lineColumn, childCount);
            if (childHeight > maxHeight) {
                childHeight = maxHeight;
            }
            int childWidthSpec = makeMeasureSpec(childWidth, MeasureSpec.EXACTLY);
            int childHeightSpec = makeMeasureSpec(childHeight, MeasureSpec.EXACTLY);
            for (int i = 0; i < childCount; i++) {
                TextView child = (TextView) getChildAt(i);
                child.measure(childWidthSpec, childHeightSpec);
            }
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    private int getLineColumn(int cellCount) {
        int lineColumn = cellCount;
        if (lineColumn >= mMaxLineColumn)
            lineColumn = mMaxLineColumn;
        return lineColumn;
    }

    private int getCellHeight(int layoutHeight, int lineColumn, int cellCount) {
        double rowCount = Math.ceil((double) cellCount / lineColumn);
        return (int) (layoutHeight / rowCount);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int childCount = getChildCount();
        if (childCount == 0) {
            return;
        }

        int paddingLeft = getPaddingLeft();
        int paddingTop = getPaddingTop();

        int lineColumn = getLineColumn(childCount);

        for (int i = 0, row = 0; i < childCount; i++) {
            View child = getChildAt(i);
            if (child.getVisibility() != GONE) {
                LayoutParams layoutParams = child.getLayoutParams();
                int marginLeft = 0, marginTop = 0, marginRight = 0, marginBottom = 0;
                //FIXME 让onMeasure来测量
                if (layoutParams instanceof MarginLayoutParams) {
                    MarginLayoutParams marginLP = (MarginLayoutParams) layoutParams;
                    marginLeft = marginLP.leftMargin;
                    marginTop = marginLP.topMargin;
                    marginRight = marginLP.rightMargin;
                    marginBottom = marginLP.bottomMargin;
                }

                int childHeight = child.getMeasuredHeight();
                int childWidth = child.getMeasuredWidth();
                int col = i % lineColumn;
                if (col == 0 && i != 0) {
                    row++;
                }

                int left = col * childWidth + paddingLeft + marginLeft;
                int right = left + childWidth - marginLeft - marginRight;
                int top = row * childHeight + paddingTop + marginTop;
                int bottom = top + childHeight - marginTop - marginBottom;

                child.layout(left, top, right, bottom);
            }
        }
    }

    private void init() {
        Resources resources = getResources();
        maxHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 48, resources.getDisplayMetrics());
        if (isInEditMode()) {
            String[] tags = new String[]{"tag0", "tag1", "tag2", "tag3", "tag four", "tag five", "tag six"};
            setTags(tags);
        }
    }

    public void setMaxLineColumn(int column) {
        mMaxLineColumn = column;
    }

    public void setTags(String[] tags) {
        if (tags == null) {
            throw new IllegalArgumentException("tags list can not be null");
        }
        Context context = getContext();
        for (String t : tags) {
            TextView textView = new TextView(context);
            textView.setText(t);
            addView(textView);
        }
    }

    public void setTags(ArrayList<String> tags) {
        if (tags == null) {
            throw new IllegalArgumentException("tags list can not be null");
        }
        Context context = getContext();
        for (String t : tags) {
            TextView textView = new TextView(context);
//            textView.setBackgroundResource(R.drawable.selector_tag_button);
            MarginLayoutParams lp = new MarginLayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            lp.setMargins(4, 4, 4, 4);
            textView.setLayoutParams(lp);
            textView.setText(t);
            addView(textView);
        }
    }

    @Override
    public void setOnClickListener(OnClickListener l) {
        for (int i = 0, count = getChildCount(); i < count; i++) {
            View child = getChildAt(i);
            child.setOnClickListener(l);
        }
    }
}