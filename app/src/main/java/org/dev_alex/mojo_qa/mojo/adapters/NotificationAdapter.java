package org.dev_alex.mojo_qa.mojo.adapters;


import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import net.cachapa.expandablelayout.ExpandableLayout;

import org.dev_alex.mojo_qa.mojo.R;
import org.dev_alex.mojo_qa.mojo.models.Notification;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;


public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {
    private SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy | HH:mm", Locale.getDefault());

    private ArrayList<Notification> notifications;
    private NotificationClickListener listener;

    public NotificationAdapter(ArrayList<Notification> notifications, NotificationClickListener listener) {
        this.notifications = notifications;
        this.listener = listener;
    }

    static class NotificationViewHolder extends RecyclerView.ViewHolder {
        TextView notificationTitle;
        TextView notificationDate;
        TextView notificationDescription;
        ImageView moreBtn;

        ExpandableLayout expandableLayout;
        View btClose;
        View mainNotificationView;
        View notificationNewBorder;

        NotificationViewHolder(View itemView) {
            super(itemView);
            notificationDate = itemView.findViewById(R.id.notification_date);
            notificationTitle = itemView.findViewById(R.id.notification_title);
            notificationDescription = itemView.findViewById(R.id.tvNotificationDesc);
            moreBtn = itemView.findViewById(R.id.more_btn);
            btClose = itemView.findViewById(R.id.btClose);

            expandableLayout = itemView.findViewById(R.id.vExpandable);
            mainNotificationView = itemView.findViewById(R.id.vMainNotificationBlock);
            notificationNewBorder = itemView.findViewById(R.id.vNotificationUnread);
        }
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.card_notification, viewGroup, false);
        return new NotificationViewHolder(v);
    }

    @Override
    public void onBindViewHolder(NotificationViewHolder viewHolder, int i) {
        final Notification notification = notifications.get(i);
        viewHolder.notificationDate.setText(sdf.format(new Date(notification.create_date)));
        viewHolder.notificationTitle.setText(notification.task.ref.name);

        Context context = viewHolder.itemView.getContext();
        String finishTime = notification.task.complete_time == null ? context.getString(R.string.unknown) :
                sdf.format(new Date(notification.task.complete_time));

        String notificationDescription = context.getString(R.string.result) + ":<br/>" +
                context.getString(R.string.points) + ": <b>" + notification.task.value.val + "</b> " + context.getString(R.string.percent) + ": <b>" + notification.task.value.prc + "</b>%<br/><br/>" +
                context.getString(R.string.executor) + " - <b>" + notification.task.executor.fullname + "</b>(" + notification.task.executor.username + ")<br/><br/>" +
                context.getString(R.string.finish_time) + " - " + finishTime;

        viewHolder.notificationDescription.setText(Html.fromHtml(notificationDescription));

        viewHolder.btClose.setOnClickListener(v -> viewHolder.expandableLayout.collapse(true));
        viewHolder.mainNotificationView.setOnClickListener(v -> {
            viewHolder.expandableLayout.toggle(true);

            if (!notification.is_readed) {
                viewHolder.notificationNewBorder.setVisibility(View.GONE);
                notification.is_readed = true;

                listener.onNotificationRead(notification);
            }
        });

        viewHolder.expandableLayout.collapse(false);

        viewHolder.notificationNewBorder.setVisibility(notification.is_readed ? View.GONE : View.VISIBLE);
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    public interface NotificationClickListener {
        void onNotificationRead(Notification notification);
    }
}

