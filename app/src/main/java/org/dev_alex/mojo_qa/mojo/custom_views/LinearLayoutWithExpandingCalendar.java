package org.dev_alex.mojo_qa.mojo.custom_views;

import android.content.Context;
import android.graphics.Rect;
import android.os.Build;
import androidx.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.LinearLayout;

import net.cachapa.expandablelayout.ExpandableLayout;

import org.dev_alex.mojo_qa.mojo.R;

public class LinearLayoutWithExpandingCalendar extends LinearLayout {
    public LinearLayoutWithExpandingCalendar(Context context) {
        super(context);
    }

    public LinearLayoutWithExpandingCalendar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public LinearLayoutWithExpandingCalendar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public LinearLayoutWithExpandingCalendar(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        ExpandableLayout expandableLayout = (ExpandableLayout) findViewById(R.id.expandable_calendar_layout);
        if (expandableLayout.isExpanded()) {
            final Rect viewRect = new Rect();
            expandableLayout.getGlobalVisibleRect(viewRect);
            if (!viewRect.contains((int) ev.getRawX(), (int) ev.getRawY())) {
                expandableLayout.collapse();
                return false;
            }
        }
        return super.dispatchTouchEvent(ev);
    }

}
