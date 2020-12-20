package org.dev_alex.mojo_qa.mojo.custom_views;

import android.content.Context;
import android.graphics.Rect;
import android.os.Build;
import androidx.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.RelativeLayout;

import org.dev_alex.mojo_qa.mojo.R;

import java.util.ArrayList;

public class RelativeLayoutWithPopUp extends RelativeLayout {
    ArrayList<ViewGroup> popUpWindows = new ArrayList<>();

    public RelativeLayoutWithPopUp(Context context) {
        super(context);
    }

    public RelativeLayoutWithPopUp(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RelativeLayoutWithPopUp(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public RelativeLayoutWithPopUp(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        for (final ViewGroup popUpWindow : popUpWindows) {
            if (popUpWindow.getVisibility() == VISIBLE && (popUpWindow.getTag() == null || (int) popUpWindow.getTag() == 0)) {
                final Rect viewRect = new Rect();
                popUpWindow.getGlobalVisibleRect(viewRect);
                if (!viewRect.contains((int) ev.getRawX(), (int) ev.getRawY())) {
                    Animation mFadeOutAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.fadeout);

                    mFadeOutAnimation.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {
                            popUpWindow.setTag(1);
                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            popUpWindow.setVisibility(GONE);
                            popUpWindow.setTag(0);

                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {

                        }
                    });
                    popUpWindow.startAnimation(mFadeOutAnimation);
                    return false;
                }
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    public void addPopUpWindow(ViewGroup window) {
        popUpWindows.add(window);
    }
}
