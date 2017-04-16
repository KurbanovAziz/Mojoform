package org.dev_alex.mojo_qa.mojo.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.dev_alex.mojo_qa.mojo.R;
import org.dev_alex.mojo_qa.mojo.activities.AuthActivity;
import org.dev_alex.mojo_qa.mojo.models.User;

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

            ((TextView) rootView.findViewById(R.id.user_name)).setText(user.firstName + " " + user.lastName);
            ((TextView) rootView.findViewById(R.id.user_initials)).setText(String.format(Locale.getDefault(), "%s%s", user.firstName.charAt(0), user.lastName.charAt(0)));

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
                    ((AuthActivity) getActivity()).new LoginTask(user.userName,
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
