package org.dev_alex.mojo_qa.mojo.custom_views;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.webkit.WebView;

public class CustomWebview extends WebView {

    public CustomWebview(Context context) {
        super(context);
    }

    public CustomWebview(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomWebview(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Log.d("touch_ct", "Touch Count =" + event.getPointerCount());
        if (event.getPointerCount() > 1)
            requestDisallowInterceptTouchEvent(true);
        return super.onTouchEvent(event);
    }
}