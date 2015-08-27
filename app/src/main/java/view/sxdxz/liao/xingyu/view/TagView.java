package view.sxdxz.liao.xingyu.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import static android.view.View.MeasureSpec.makeMeasureSpec;
import static android.view.View.MeasureSpec.EXACTLY;

/**
 * Created by liaoxingyu on 8/27/15.
 */
public class TagView extends ViewGroup {
    private int mMaxLineColumn = 5;
    private String mTitle;
    private TextView mTitleView;
    private boolean hasTitle;

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
            int cellCount = getCellCount(childCount);
            int lineColumn = getLineColumn(cellCount);

            int childWidth = width / lineColumn;
            int childHeight = getCellHeight(height, lineColumn, cellCount);

            int childWidthSpec = makeMeasureSpec(childWidth, EXACTLY);
            int childHeightSpec = makeMeasureSpec(childHeight, EXACTLY);
            for (int i = hasTitle ? 1 : 0; i < childCount; i++) {
                View child = getChildAt(i);
                child.measure(childWidthSpec, childHeightSpec);
            }
            if (hasTitle) {
                View title = getChildAt(0);
                title.measure(makeMeasureSpec(width, EXACTLY), childHeightSpec);
            }
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
//        setMeasuredDimension(width + getPaddingLeft() + getPaddingRight(), height + getPaddingTop() + getPaddingBottom());
    }

    private int getLineColumn(int cellCount) {
        int lineColumn = cellCount;
        if (lineColumn >= mMaxLineColumn)
            lineColumn = mMaxLineColumn;
        return lineColumn;
    }

    private int getCellCount(int childCount) {
        int cellCount = childCount;
        if (hasTitle) {
            cellCount--;
        }
        return cellCount;
    }

    private int getCellHeight(int layoutHeight, int lineColumn, int cellCount) {
        double rowCount = Math.ceil((double) cellCount / lineColumn);
        if (hasTitle) {
            rowCount++;
        }
        return (int) (layoutHeight / rowCount);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int childCount = getChildCount();
        if (childCount == 0) {
            return;
        }
        int paddingLeft = getPaddingLeft();
        int paddingRight = getPaddingRight();
        int paddingTop = getPaddingTop();
        int paddingBottom = getPaddingBottom();

        int width = r - l - paddingLeft - paddingRight;
        int height = b - t - paddingTop - paddingBottom;

        int cellCount = getCellCount(childCount);
        int lineColumn = getLineColumn(cellCount);

        int childWidth = width / lineColumn;
        int childHeight = getCellHeight(height, lineColumn, cellCount);

        for (int i = hasTitle ? 1 : 0, row = hasTitle ? 1 : 0; i < childCount; i++) {
            View child = getChildAt(i);
            int childIndex = i;
            if (hasTitle) {
                childIndex--;
            }
            int col = childIndex % lineColumn;
            if (col == 0 && childIndex != 0) {
                row++;
            }

            int left = col * childWidth + paddingLeft;
            int right = left + childWidth;
            int top = row * childHeight + paddingTop;
            int bottom = top + childHeight;

            child.layout(left, top, right, bottom);
        }
        if (hasTitle) {
            View title = getChildAt(0);
            title.layout(paddingLeft, paddingTop, paddingLeft + width, paddingTop + childHeight);
        }
    }

    private void init() {
        if (isInEditMode()) {
            setTitle("title");
            String[] tags = new String[]{"tag0", "tag1", "tag2", "tag3", "tag four", "tag five", "tag six"};
            setTags(tags);
        }
    }

    public void setTitle(String title) {
        mTitle = title;
        TextView titleView = new TextView(getContext());
        titleView.setText(title);
        titleView.setGravity(Gravity.CENTER);
        addView(titleView, 0);
        hasTitle = true;
    }

    public void setTags(String[] tags) {
        if (tags == null) {
            throw new IllegalArgumentException("tags list can not be null");
        }
        Context context = getContext();
        for (String t : tags) {
            TextView textView = new TextView(context);
            textView.setGravity(Gravity.CENTER);
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
            textView.setText(t);
            addView(textView);
        }
    }
}