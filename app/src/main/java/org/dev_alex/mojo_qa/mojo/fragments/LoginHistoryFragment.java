package org.dev_alex.mojo_qa.mojo.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import org.dev_alex.mojo_qa.mojo.R;

public class LoginHistoryFragment extends Fragment {
    private View rootView;


    public static LoginHistoryFragment newInstance() {
        Bundle args = new Bundle();
        LoginHistoryFragment fragment = new LoginHistoryFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_login_history, container, false);

        setListeners();
        return rootView;
    }


    private void setListeners() {
        rootView.findViewById(R.id.new_user_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.container, LoginFragment.newInstance()).addToBackStack(null).commit();
            }
        });

        ((Button) rootView.findViewById(R.id.new_user_btn)).setAllCaps(true);
    }
}
