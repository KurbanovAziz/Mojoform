package org.dev_alex.mojo_qa.mojo.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import org.dev_alex.mojo_qa.mojo.R;
import org.dev_alex.mojo_qa.mojo.adapters.UserAdapter;
import org.dev_alex.mojo_qa.mojo.services.LoginHistoryService;
import org.dev_alex.mojo_qa.mojo.services.Utils;

public class LoginHistoryFragment extends Fragment {
    private View rootView;
    private RecyclerView recyclerView;


    public static LoginHistoryFragment newInstance() {
        Bundle args = new Bundle();
        LoginHistoryFragment fragment = new LoginHistoryFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.fragment_login_history, container, false);

            recyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            recyclerView.setAdapter(new UserAdapter(this, LoginHistoryService.getLastLoggedUsers()));
            Utils.setupCloseKeyboardUI(getActivity(), rootView);
            setListeners();
        }
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
