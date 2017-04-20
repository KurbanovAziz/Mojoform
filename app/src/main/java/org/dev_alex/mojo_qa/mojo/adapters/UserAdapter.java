package org.dev_alex.mojo_qa.mojo.adapters;


import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.dev_alex.mojo_qa.mojo.R;
import org.dev_alex.mojo_qa.mojo.fragments.LoginHistoryFragment;
import org.dev_alex.mojo_qa.mojo.fragments.UserLoginFragment;
import org.dev_alex.mojo_qa.mojo.models.User;

import java.util.ArrayList;
import java.util.Locale;


public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {
    private ArrayList<User> users;
    private LoginHistoryFragment parentFragment;

    static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView userName;
        TextView userInitials;

        UserViewHolder(View itemView) {
            super(itemView);
            userInitials = (TextView) itemView.findViewById(R.id.user_initials);
            userName = (TextView) itemView.findViewById(R.id.user_name);
        }
    }


    public UserAdapter(LoginHistoryFragment parentFragment, ArrayList<User> users) {
        this.parentFragment = parentFragment;
        this.users = users;
    }

    @Override
    public UserViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.card_logined_user, viewGroup, false);
        return new UserViewHolder(v);
    }

    @Override
    public void onBindViewHolder(UserViewHolder viewHolder, int i) {
        final User user = users.get(i);

        viewHolder.userName.setText(user.firstName + " " + user.lastName);
        viewHolder.userInitials.setText(String.format(Locale.getDefault(), "%s%s", user.firstName.isEmpty() ? "" : user.firstName.charAt(0), user.lastName.isEmpty() ? "" : user.lastName.charAt(0)));

        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                parentFragment.getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.container, UserLoginFragment.newInstance(user)).addToBackStack(null).commit();
            }
        });
    }

    @Override
    public int getItemCount() {
        return users.size();
    }
}

