package org.dev_alex.mojo_qa.mojo.custom_views;

import android.content.Context;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import android.util.AttributeSet;

public class MaxHeightRecycleView extends RecyclerView {
    private int maxHeight = (int) Math.round(getResources().getDisplayMetrics().heightPixels * 0.84);
    private boolean maxHeightEnabled = true;

    public void setMaxHeightPercentage(float percentage) {
        maxHeight = (int) Math.round(getResources().getDisplayMetrics().heightPixels * percentage);
    }

    public void setMaxHeightEnabled(boolean state) {
        maxHeightEnabled = state;
    }

    public MaxHeightRecycleView(Context context) {
        super(context);
    }

    public MaxHeightRecycleView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public MaxHeightRecycleView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onMeasure(int widthSpec, int heightSpec) {
        if (maxHeightEnabled)
            heightSpec = MeasureSpec.makeMeasureSpec(maxHeight, MeasureSpec.AT_MOST);
        super.onMeasure(widthSpec, heightSpec);
    }
}
