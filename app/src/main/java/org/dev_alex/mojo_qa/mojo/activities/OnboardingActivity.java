package org.dev_alex.mojo_qa.mojo.activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import org.dev_alex.mojo_qa.mojo.App;
import org.dev_alex.mojo_qa.mojo.R;
import org.dev_alex.mojo_qa.mojo.event.OnboardingFinishedEvent;
import org.dev_alex.mojo_qa.mojo.event.OnboardingSkippedEvent;
import org.dev_alex.mojo_qa.mojo.fragments.OnboardingPageFragment;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class OnboardingActivity extends AppCompatActivity {

    private ViewPager viewPager;
    private RadioGroup radioGroup;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);
        viewPager = (ViewPager) findViewById(R.id.view_pager);
        radioGroup = (RadioGroup) findViewById(R.id.page_points_toggle);

        viewPager.setOffscreenPageLimit(3);

        viewPager.setAdapter(new OnBoardingPageAdapter(getSupportFragmentManager(), radioGroup.getChildCount()));
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                ((RadioButton) radioGroup.getChildAt(position)).setChecked(true);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onOnboardingSkippedEvent(OnboardingSkippedEvent event) {
        setOnboardingFinished(true);
        finish();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onOnboardingFinishedEvent(OnboardingFinishedEvent event) {
        setOnboardingFinished(true);
        finish();
    }

    private static class OnBoardingPageAdapter extends FragmentPagerAdapter {
        private int NUM_ITEMS;

        OnBoardingPageAdapter(FragmentManager fm, int pageCt) {
            super(fm);
            NUM_ITEMS = pageCt;
        }

        @Override
        public Fragment getItem(int position) {
            return OnboardingPageFragment.newInstance(position);
        }


        @Override
        public int getCount() {
            return NUM_ITEMS;
        }

        @Override
        public long getItemId(int position) {
            return System.currentTimeMillis();
        }
    }

    public static void setOnboardingFinished(boolean isFinished) {
        SharedPreferences mSettings;
        mSettings = App.getContext().getSharedPreferences("ONBOARDING_PREF", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mSettings.edit();
        editor.putBoolean("onboarding_finished", isFinished);
        editor.apply();
    }

    public static boolean isOnboardingFinished() {
        SharedPreferences mSettings;
        mSettings = App.getContext().getSharedPreferences("ONBOARDING_PREF", Context.MODE_PRIVATE);
        return mSettings.getBoolean("onboarding_finished", false);
    }
}
