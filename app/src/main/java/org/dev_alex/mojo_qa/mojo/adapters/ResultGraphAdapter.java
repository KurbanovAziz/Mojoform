package org.dev_alex.mojo_qa.mojo.adapters;


import android.content.Context;
import android.content.res.Resources;

import androidx.recyclerview.widget.RecyclerView;

import android.graphics.Color;
import android.text.Html;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import net.cachapa.expandablelayout.ExpandableLayout;

import org.dev_alex.mojo_qa.mojo.R;
import org.dev_alex.mojo_qa.mojo.fragments.GraphFragment;
import org.dev_alex.mojo_qa.mojo.models.Employee;
import org.dev_alex.mojo_qa.mojo.models.Indicator;
import org.dev_alex.mojo_qa.mojo.models.Notification;
import org.dev_alex.mojo_qa.mojo.models.Ranges;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import io.reactivex.internal.observers.EmptyCompletableObserver;

public class ResultGraphAdapter extends RecyclerView.Adapter<ResultGraphAdapter.ResultViewHolder> {
    private List<Indicator> indicators;
    private List<Employee> employees;
    String name;

    private GraphClickListener onPanelClickListener;
    private SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy | HH:mm", Locale.getDefault());

    static class ResultViewHolder extends RecyclerView.ViewHolder {
        TextView panelName;
        TextView panelStats;
        ImageView panelIcon;
        TextView notificationTitle;
        TextView notificationDate;
        TextView notificationDescription;
        TextView informationTV;
        ImageView moreBtn;
        ImageView btDownloadPdf;
        ImageView btDownloadDoc;
        View vButtonsBlock;
        ExpandableLayout expandableLayout;
        View btClose;
        View mainNotificationView;
        View notificationNewBorder;

        ResultViewHolder(View itemView) {
            super(itemView);
            panelIcon = (ImageView) itemView.findViewById(R.id.panel_icon_imag);
            panelName = (TextView) itemView.findViewById(R.id.panel_nam);
            notificationDate = (TextView) itemView.findViewById(R.id.result_date);
            notificationTitle = itemView.findViewById(R.id.notification_title);
            notificationDescription = itemView.findViewById(R.id.tvNotificationDesc);
            moreBtn = itemView.findViewById(R.id.more_btn);
            btClose = itemView.findViewById(R.id.btClose);
            btDownloadPdf = itemView.findViewById(R.id.btDownloadPdf);
            btDownloadDoc = itemView.findViewById(R.id.btDownloadDoc);
            vButtonsBlock = itemView.findViewById(R.id.vButtonsBlock);
            informationTV = itemView.findViewById(R.id.information);
            expandableLayout = itemView.findViewById(R.id.vExpandable);
            mainNotificationView = itemView.findViewById(R.id.main_block);
            notificationNewBorder = itemView.findViewById(R.id.vNotificationUnread);
        }
    }


    public ResultGraphAdapter(List<Indicator> indicators, GraphClickListener onPanelClickListener, List<Employee> employees, String name) {
        this.indicators = indicators;
        this.onPanelClickListener = onPanelClickListener;
        this.employees = employees;
        this.name = name;
    }

    @Override
    public int getItemViewType(int position) {
        if(position == 0){return 2;}
        return 0;   }

    @Override
    public ResultViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View v;
        if (viewType == 0)
            v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.result_card_panel, viewGroup, false);
        else
            v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.result_text, viewGroup, false);

        return new ResultViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ResultViewHolder viewHolder, int i) {
        final Indicator indicator = indicators.get(i);
        final Indicator notification = indicators.get(i);



        if (getItemViewType(i) == 0) {
            viewHolder.mainNotificationView.setOnClickListener(v -> {
                viewHolder.expandableLayout.toggle(true);
                float value = (float) indicator.prc;
                int defaultColor = Color.parseColor("#5E5E5E");
                if(GraphFragment.ranges != null && GraphFragment.ranges.size() != 0){
                    for (Ranges range : GraphFragment.ranges) {
                        if (value >= range.from && value <= range.to){
                            try {
                                String colorString = "#AA" + range.color.substring(1);
                                defaultColor = Color.parseColor(colorString);}
                            catch (Exception ignored){}
                        }
                    }
                }
                else{
                    try {
                        defaultColor = Color.parseColor("#ff0000");
                        if(value < 86)  defaultColor = Color.parseColor("#ff0000");
                        if (value > 85 && value < 96){  defaultColor = Color.parseColor("#eaff00");}
                        if (value > 95 && value < 101){  defaultColor = Color.parseColor("#15ff00");}
                        if (value > 100){defaultColor = Color.parseColor("#42aaff");}}
                    catch (Exception e ){
                        e.printStackTrace();
                    }
                    if(defaultColor != Color.parseColor("#4E3F60") ){
                        viewHolder.panelIcon.setImageResource(R.drawable.bell_image);
                    }
                    else {
                        viewHolder.panelIcon.setImageResource(R.drawable.bell_task);
                    }
                    viewHolder.panelIcon.setColorFilter(defaultColor);
                }


                });
            viewHolder.expandableLayout.collapse(false);
            viewHolder.btDownloadPdf.setOnClickListener(v -> onPanelClickListener.onDownloadPdfClick(notification));
            viewHolder.btDownloadDoc.setOnClickListener(v -> onPanelClickListener.onDownloadDocClick(notification));
            viewHolder.btClose.setOnClickListener(v -> viewHolder.expandableLayout.collapse(true)); viewHolder.panelIcon.setImageResource(R.drawable.result); viewHolder.panelIcon.setColorFilter(Color.parseColor("#4E3F60"));
            viewHolder.panelName.setText(name);
            viewHolder.notificationDate.setText(sdf.format(new Date(indicator.timestamp)));

            Context context = viewHolder.itemView.getContext();
            String employee = "";
            for (Employee employee1 : GraphFragment.users){
                if(notification.userID == employee1.id){employee = employee1.fullname;}
            }

            String executorPart;
                executorPart = context.getString(R.string.executor) + ":<br/><b>" + employee + "</b><br/><br/>";
                String notificationDescription = context.getString(R.string.result) + ":<br/>" +
                        context.getString(R.string.points) + ": <b>" + notification.val + "</b><br/>" + context.getString(R.string.percent) + ": <b>" + notification.prc + "%</b><br/><br/>" +
                        executorPart +
                        context.getString(R.string.finish_time) + ":<br/><b>" + sdf.format(new Date(indicator.timestamp)) + "</b>";
            viewHolder.informationTV.setText(Html.fromHtml(notificationDescription));
}



if(viewHolder.panelStats != null){
                    viewHolder.panelStats.setText(
                            new StringBuilder().append(indicator.val).append(" | ").append(indicator.prc).append("%").toString());
                viewHolder.panelName.setText(name);}



        }


    @Override
    public int getItemCount() {
        return indicators.size();
    }

    public interface OnPanelClickListener {
        void onClick(Indicator panel);
    }

    private int dpToPx(Context context, float dp) {
        Resources resources = context.getResources();
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.getDisplayMetrics());
    }
    public interface GraphClickListener {
        void onDownloadPdfClick(Indicator indicator);

        void onDownloadDocClick(Indicator indicator);
    }
}

