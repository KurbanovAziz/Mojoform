package org.dev_alex.mojo_qa.mojo.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.dev_alex.mojo_qa.mojo.R;

public class LogoFragment extends Fragment {
    private View rootView;

    public static LogoFragment newInstance() {
        Bundle args = new Bundle();
        LogoFragment fragment = new LogoFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_logo, container, false);
        return rootView;
    }
}
