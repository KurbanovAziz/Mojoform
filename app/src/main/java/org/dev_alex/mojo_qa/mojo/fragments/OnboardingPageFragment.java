package org.dev_alex.mojo_qa.mojo.fragments;

import android.graphics.Typeface;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.dev_alex.mojo_qa.mojo.R;
import org.dev_alex.mojo_qa.mojo.event.OnboardingFinishedEvent;
import org.dev_alex.mojo_qa.mojo.event.OnboardingSkippedEvent;
import org.greenrobot.eventbus.EventBus;

public class OnboardingPageFragment extends Fragment {
    private final static String PAGE_NUMBER = "page_number";
    private View rootView = null;


    public static OnboardingPageFragment newInstance(int pageNumber) {

        Bundle args = new Bundle();
        args.putInt(PAGE_NUMBER, pageNumber);

        OnboardingPageFragment fragment = new OnboardingPageFragment();
        fragment.setArguments(args);
        return fragment;
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        int pageNumber = getArguments().getInt(PAGE_NUMBER);
        Typeface tfBold = Typeface.createFromAsset(getContext().getAssets(), "fonts/sf_ui_bold.ttf");
        Typeface tfMedium = Typeface.createFromAsset(getContext().getAssets(), "fonts/sf_ui_medium.ttf");
        Typeface tfThin = Typeface.createFromAsset(getContext().getAssets(), "fonts/sf_ui_thin.ttf");


        switch (pageNumber) {
            case 0:
                rootView = inflater.inflate(R.layout.fragment_onboarding_first_page, container, false);
                ((TextView) rootView.findViewById(R.id.hi)).setTypeface(tfBold);
                ((TextView) rootView.findViewById(R.id.main)).setTypeface(tfMedium);
                ((TextView) rootView.findViewById(R.id.swipe)).setTypeface(tfMedium);
                ((TextView) rootView.findViewById(R.id.skip)).setTypeface(tfMedium);

                rootView.findViewById(R.id.skip).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        EventBus.getDefault().post(new OnboardingSkippedEvent());
                    }
                });
                break;

            case 1:
                rootView = inflater.inflate(R.layout.fragment_onboarding_second_page, container, false);
                ((TextView) rootView.findViewById(R.id.title)).setTypeface(tfBold);
                ((TextView) rootView.findViewById(R.id.sub_title)).setTypeface(tfMedium);
                ((TextView) rootView.findViewById(R.id.main_part)).setTypeface(tfThin);
                ((TextView) rootView.findViewById(R.id.bottom_part)).setTypeface(tfMedium);
                break;

            case 2:
                rootView = inflater.inflate(R.layout.fragment_onboarding_three_page, container, false);
                ((TextView) rootView.findViewById(R.id.title)).setTypeface(tfBold);
                ((TextView) rootView.findViewById(R.id.main_part)).setTypeface(tfMedium);
                break;

            case 3:
                rootView = inflater.inflate(R.layout.fragment_onboarding_four_page, container, false);
                ((TextView) rootView.findViewById(R.id.title)).setTypeface(tfBold);
                ((TextView) rootView.findViewById(R.id.main_part)).setTypeface(tfMedium);
                ((TextView) rootView.findViewById(R.id.finish)).setTypeface(tfBold);

                rootView.findViewById(R.id.finish).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        EventBus.getDefault().post(new OnboardingFinishedEvent());
                    }
                });
                break;
        }
        return rootView;
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
}
