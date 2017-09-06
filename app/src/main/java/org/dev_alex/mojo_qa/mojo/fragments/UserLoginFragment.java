package org.dev_alex.mojo_qa.mojo.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.dev_alex.mojo_qa.mojo.R;
import org.dev_alex.mojo_qa.mojo.activities.AuthActivity;
import org.dev_alex.mojo_qa.mojo.models.User;
import org.dev_alex.mojo_qa.mojo.services.Utils;

import java.util.Locale;

public class UserLoginFragment extends Fragment {
    private View rootView;
    private User user;


    public static UserLoginFragment newInstance(User user) {
        Bundle args = new Bundle();
        args.putSerializable("user", user);

        UserLoginFragment fragment = new UserLoginFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.fragment_user_login, container, false);
            user = (User) getArguments().getSerializable("user");

            if (user != null) {
                if (TextUtils.isEmpty(user.firstName) && TextUtils.isEmpty(user.lastName)) {
                    ((TextView) rootView.findViewById(R.id.user_name)).setText(user.username);
                    ((TextView) rootView.findViewById(R.id.user_initials)).setText(user.username);
                } else {
                    ((TextView) rootView.findViewById(R.id.user_initials)).setText(String.format(Locale.getDefault(),
                            "%s%s", TextUtils.isEmpty(user.firstName) ? "" : user.firstName.charAt(0),
                            TextUtils.isEmpty(user.lastName) ? "" : user.lastName.charAt(0)));

                    ((TextView) rootView.findViewById(R.id.user_name)).setText(String.format(Locale.getDefault(),
                            "%s %s", TextUtils.isEmpty(user.firstName) ? "" : user.firstName,
                            TextUtils.isEmpty(user.lastName) ? "" : user.lastName));
                }
            }

            Utils.setupCloseKeyboardUI(getActivity(), rootView);
            setListeners();
        }
        return rootView;
    }


    private void setListeners() {
        final EditText password = (EditText) rootView.findViewById(R.id.password);
        rootView.findViewById(R.id.login_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (password.getText().toString().trim().isEmpty())
                    Toast.makeText(getContext(), R.string.pass_is_empty, Toast.LENGTH_LONG).show();
                else
                    ((AuthActivity) getActivity()).new LoginTask(user.username,
                            password.getText().toString()).execute();
            }
        });

        rootView.findViewById(R.id.back_arrow).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });
    }
}
