package org.dev_alex.mojo_qa.mojo.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import org.dev_alex.mojo_qa.mojo.R;
import org.dev_alex.mojo_qa.mojo.models.response.Comment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.ViewHolder> {

    private final LayoutInflater inflater;
    private ArrayList<Comment> comments = new ArrayList<>();

    private final Context context;

    public CommentAdapter(Context context, ArrayList<Comment> comments) {
        this.context = context;
        if (comments != null) {
            this.comments = comments;
        } else {
            Toast.makeText(context, "Пока комментариев нет", Toast.LENGTH_SHORT).show();
        }
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public CommentAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = inflater.inflate(R.layout.comment_item_updated, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final CommentAdapter.ViewHolder holder, int position) {
        Comment comment = comments.get(position);
        String time = "";

        String dateTimePattern = String.format(
                Locale.getDefault(),
                "dd MMMM yyyy %1$s %2$s HH:mm",
                context.getString(R.string.dialog_comments_time_year_addition),
                context.getString(R.string.dialog_comments_time_addition));

        SimpleDateFormat dateFormat = new SimpleDateFormat(dateTimePattern, Locale.getDefault());
        time = dateFormat.format(System.currentTimeMillis());
        time = dateFormat.format(comment.time * 1000);
        holder.commentTV.setText(comment.comment);
        holder.userNameTV.setText(comment.fullname);
        holder.timeTV.setText(time);
        if (position % 2 != 0) {
            holder.addMargin();
        }
    }

    @Override
    public int getItemCount() {
        return comments.size();
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {

        final TextView commentTV;
        final TextView timeTV;
        final TextView userNameTV;

        ViewHolder(View view) {
            super(view);
            commentTV = view.findViewById(R.id.comment_item_updated_message_tv);
            timeTV = view.findViewById(R.id.comment_item_updated_time_tv);
            userNameTV = view.findViewById(R.id.comment_item_updated_author_tv);
        }

        protected void addMargin() {
            ViewGroup.LayoutParams layoutParams = itemView.getLayoutParams();
            if (layoutParams != null) {
                if (layoutParams instanceof ViewGroup.MarginLayoutParams) {
                    ((ViewGroup.MarginLayoutParams) layoutParams).setMargins(50, 0, 0, 0);
                }
            }
        }
    }
}
