package org.dev_alex.mojo_qa.mojo.adapters;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.media.Image;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.dev_alex.mojo_qa.mojo.R;
import org.dev_alex.mojo_qa.mojo.models.GraphInfo;
import org.dev_alex.mojo_qa.mojo.models.Indicator;
import org.dev_alex.mojo_qa.mojo.models.Panel;
import org.dev_alex.mojo_qa.mojo.models.Ranges;
import org.dev_alex.mojo_qa.mojo.models.response.Comment;
import org.dev_alex.mojo_qa.mojo.services.RequestService;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import okhttp3.Response;

public class CommentAdapter  extends RecyclerView.Adapter<CommentAdapter.ViewHolder>{

    private final LayoutInflater inflater;
    private ArrayList<Comment> comments = new ArrayList<>();

    private final Context context;

    public CommentAdapter(Context context, ArrayList<Comment> comments) {
        this.context = context;
        if(comments != null){
        this.comments = comments;}
        else {Toast.makeText(context, "Пока комментариев нет", Toast.LENGTH_SHORT).show();}
        this.inflater = LayoutInflater.from(context);
    }
    @Override
    public CommentAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = inflater.inflate(R.layout.comment_item, parent, false);
        return new ViewHolder(view, context);
    }

    @Override
    public void onBindViewHolder(final CommentAdapter.ViewHolder holder, int position) {
        Comment comment = comments.get(position);
        String time = "";
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy | HH:mm", Locale.getDefault());
        time = dateFormat.format(comment.time * 1000);
        holder.commentTV.setText(comment.comment);
        holder.userNameTV.setText(comment.fullname);
        holder.timeTV.setText(time);
    }

    @Override
    public int getItemCount() {
        return comments.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView commentTV;
        final TextView timeTV;
        final TextView userNameTV;

        ViewHolder(View view, final Context context1){
            super(view);
            commentTV = (TextView) view.findViewById(R.id.comment);
            timeTV = (TextView) view.findViewById(R.id.time);
            userNameTV = (TextView) view.findViewById(R.id.name);

        }
    }
}
