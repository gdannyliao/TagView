package view.sxdxz.liao.xingyu.view;

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import view.sxdxz.liao.xingyu.myapplication.R;

import static android.view.View.MeasureSpec.EXACTLY;
import static android.view.View.MeasureSpec.makeMeasureSpec;

/**
 * Created by liaoxingyu on 8/27/15.
 */
public class TagView extends ViewGroup {
    private int mMaxLineColumn = 5;
    private String mTitle;
    private TextView mTitleView;
    private boolean hasTitle;
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
            int cellCount = getCellCount(childCount);
            int childHeight;
            if (cellCount != 0) {
                int lineColumn = getLineColumn(cellCount);

                int childWidth = width / lineColumn;
                childHeight = getCellHeight(height, lineColumn, cellCount);
                if (childHeight > maxHeight) {
                    childHeight = maxHeight;
                }
                int childWidthSpec = makeMeasureSpec(childWidth, MeasureSpec.AT_MOST);
                int childHeightSpec = makeMeasureSpec(childHeight, MeasureSpec.AT_MOST);
                for (int i = hasTitle ? 1 : 0; i < childCount; i++) {
                    TextView child = (TextView) getChildAt(i);
                    child.measure(childWidthSpec, childHeightSpec);
                    //because of AT_MOST, we must reset TextView's gravity
                    child.setGravity(Gravity.CENTER);
                }
            } else {
                childHeight = height > maxHeight ? maxHeight : height;
            }
            if (hasTitle) {
                TextView title = (TextView) getChildAt(0);
                title.measure(makeMeasureSpec(width, MeasureSpec.EXACTLY), makeMeasureSpec(childHeight, EXACTLY));
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
        int childHeight;
        if (cellCount != 0) {
            int lineColumn = getLineColumn(cellCount);

            int childWidth = width / lineColumn;
            childHeight = getCellHeight(height, lineColumn, cellCount);
            if (childHeight > maxHeight) {
                childHeight = maxHeight;
            }

            for (int i = hasTitle ? 1 : 0, row = hasTitle ? 1 : 0; i < childCount; i++) {
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

                    int childIndex = i;
                    if (hasTitle) {
                        childIndex--;
                    }
                    int col = childIndex % lineColumn;
                    if (col == 0 && childIndex != 0) {
                        row++;
                    }

                    int left = col * childWidth + paddingLeft + marginLeft;
                    int right = left + childWidth - marginLeft - marginRight;
                    int top = row * childHeight + paddingTop + marginTop;
                    int bottom = top + childHeight - marginTop - marginBottom;

                    child.layout(left, top, right, bottom);
                }
            }
        } else {
            childHeight = height;
        }
        if (hasTitle) {
            View title = getChildAt(0);
            title.layout(paddingLeft, paddingTop, paddingLeft + width, paddingTop + childHeight);
        }
    }

    private void init() {
        Resources resources = getResources();
        maxHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 48, resources.getDisplayMetrics());
        if (isInEditMode()) {
            setTitle("title");
            String[] tags = new String[]{"tag0", "tag1", "tag2", "tag3", "tag four", "tag five", "tag six"};
            setTags(tags);
        }
    }

    public void setMaxLineColumn(int column) {
        mMaxLineColumn = column;
    }

    public void setTitle(String title) {
        mTitle = title;
        TextView titleView = new TextView(getContext());
        titleView.setText(title);
        titleView.setGravity(Gravity.CENTER_VERTICAL);
        Resources res = getResources();
        int paddingSmall
                = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, res.getDisplayMetrics());
        titleView.setPadding(paddingSmall, 0, 0, 0);
        titleView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
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
            lp.setMargins(4,4,4,4);
            textView.setLayoutParams(lp);
            textView.setText(t);
            addView(textView);
        }
    }

    @Override
    public void setOnClickListener(OnClickListener l) {
        for (int i = hasTitle ? 1 : 0, count = getChildCount(); i < count; i++) {
            View child = getChildAt(i);
            child.setOnClickListener(l);
        }
    }
}