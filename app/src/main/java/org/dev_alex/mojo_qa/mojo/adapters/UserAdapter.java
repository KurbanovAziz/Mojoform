package org.dev_alex.mojo_qa.mojo.adapters;


import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.dev_alex.mojo_qa.mojo.R;
import org.dev_alex.mojo_qa.mojo.fragments.LoginHistoryFragment;
import org.dev_alex.mojo_qa.mojo.fragments.UserLoginFragment;
import org.dev_alex.mojo_qa.mojo.models.User;
import org.dev_alex.mojo_qa.mojo.services.LoginHistoryService;

import java.util.ArrayList;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;


public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {
    private ArrayList<User> users;
    private LoginHistoryFragment parentFragment;

    static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView userName;
        CircleImageView avatarImage;
        TextView userInitials;

        UserViewHolder(View itemView) {
            super(itemView);
            userInitials = (TextView) itemView.findViewById(R.id.user_initials);
            avatarImage = (CircleImageView) itemView.findViewById(R.id.profile_image);
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

        Bitmap avatar = LoginHistoryService.getAvatar(user.username);
        if (avatar == null) {
            viewHolder.avatarImage.setVisibility(View.GONE);
            viewHolder.userInitials.setVisibility(View.VISIBLE);
            if (TextUtils.isEmpty(user.firstName) && TextUtils.isEmpty(user.lastName))
                viewHolder.userInitials.setText(user.username.charAt(0));
            else
                viewHolder.userInitials.setText(String.format(Locale.getDefault(), "%s%s",
                        TextUtils.isEmpty(user.firstName) ? "" : user.firstName.charAt(0),
                        TextUtils.isEmpty(user.lastName) ? "" : user.lastName.charAt(0)));
        } else {
            viewHolder.userInitials.setVisibility(View.GONE);
            viewHolder.avatarImage.setVisibility(View.VISIBLE);
            viewHolder.avatarImage.setImageBitmap(avatar);
        }

        if (TextUtils.isEmpty(user.firstName) && TextUtils.isEmpty(user.lastName))
            viewHolder.userName.setText(user.username);
        else
            viewHolder.userName.setText(String.format(Locale.getDefault(),
                    "%s %s", TextUtils.isEmpty(user.firstName) ? "" : user.firstName,
                    TextUtils.isEmpty(user.lastName) ? "" : user.lastName));

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

