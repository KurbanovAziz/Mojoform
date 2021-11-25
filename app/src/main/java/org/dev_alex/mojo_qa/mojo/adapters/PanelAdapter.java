package org.dev_alex.mojo_qa.mojo.adapters;


import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.dev_alex.mojo_qa.mojo.R;
import org.dev_alex.mojo_qa.mojo.models.Panel;
import org.dev_alex.mojo_qa.mojo.models.Value;
import org.json.JSONObject;

import java.util.List;
import java.util.Locale;

public class PanelAdapter extends RecyclerView.Adapter<PanelAdapter.TaskViewHolder> {
    private List<Panel> panels;
    private OnPanelClickListener onPanelClickListener;

    static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView panelName;
        TextView panelStats;
        ImageView point;
        ImageView panelIcon;

        TaskViewHolder(View itemView) {
            super(itemView);
            panelIcon = (ImageView) itemView.findViewById(R.id.panel_icon_image);
            point = (ImageView) itemView.findViewById(R.id.point);
            panelName = (TextView) itemView.findViewById(R.id.panel_name);
            panelStats = (TextView) itemView.findViewById(R.id.panel_stats);

        }
    }


    public PanelAdapter(List<Panel> panels, OnPanelClickListener onPanelClickListener) {
        this.panels = panels;
        this.onPanelClickListener = onPanelClickListener;
    }

    @Override
    public int getItemViewType(int position) {
        return panels.get(position).isSeparator() ? 1 : 0;
    }

    @Override
    public TaskViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View v;
        if (viewType == 0)
            v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.card_panel, viewGroup, false);
        else {
  v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.simple_text, viewGroup, false);


        }
        return new TaskViewHolder(v);
    }

    @Override
    public void onBindViewHolder(TaskViewHolder viewHolder, int i) {
        final Panel panel = panels.get(i);

        if (getItemViewType(i) == 0) {
            viewHolder.panelName.setText(panel.name);
            if (panel.tags.get(0).getColor() != null){
                viewHolder.point.setImageResource(R.drawable.point);
                viewHolder.point.setColorFilter(Color.parseColor(panel.tags.get(0).getColor()));
            }
            viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onPanelClickListener.onClick(panel);
                }
            });

            try {
                Double firstVal = null, secondVal = null;
                boolean isPercents = true;

                if (TextUtils.isEmpty(panel.config)) {
                    if (panel.current.day != null)
                        firstVal = panel.current.day.prc;
                    if (panel.complete.day != null)
                        secondVal = panel.complete.day.prc;
                } else {
                    JSONObject config = new JSONObject(panel.config);
                    Value valueCurrent = null;
                    Value valueComplete = null;
                    switch (config.getString("aggregation")) {
                        case "day":
                            if (panel.current.day != null)
                                valueCurrent = panel.current.day;
                            if (panel.complete.day != null)
                                valueComplete = panel.complete.day;
                            break;
                        case "week":
                            if (panel.current.week != null)
                                valueCurrent = panel.current.week;
                            if (panel.complete.week != null)
                                valueComplete = panel.complete.week;
                            break;
                        case "month":
                            if (panel.current.month != null)
                                valueCurrent = panel.current.month;
                            if (panel.complete.month != null)
                                valueComplete = panel.complete.month;
                            break;
                        case "year":
                            if (panel.current.year != null)
                                valueCurrent = panel.current.year;
                            if (panel.complete.year != null)
                                valueComplete = panel.complete.year;
                            break;

                        default:
                            valueCurrent = new Value();
                            valueComplete = new Value();
                    }

                    isPercents = config.getInt("dataType") == 2;
                    if (isPercents) {
                        if (valueCurrent != null)
                            firstVal = valueCurrent.prc;
                        if (valueComplete != null)
                            secondVal = valueComplete.prc;
                    } else {
                        if (valueCurrent != null)
                            firstVal = valueCurrent.val;
                        if (valueComplete != null)
                            secondVal = valueComplete.val;
                    }
                }
                if (firstVal != null && secondVal != null) {
                    viewHolder.panelStats.setText(String.format(Locale.getDefault(), "%.1f | %.1f%s",
                            firstVal, secondVal, isPercents ? "%" : " " + viewHolder.itemView.getContext().getString(R.string.balls)));
                } else {
                    viewHolder.panelStats.setText("");
                }
            } catch (Exception exc) {
                exc.printStackTrace();
            }
        }
    }

    @Override
    public int getItemCount() {
        return panels.size();
    }

    public interface OnPanelClickListener {
        void onClick(Panel panel);
    }

    private int dpToPx(Context context, float dp) {
        Resources resources = context.getResources();
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.getDisplayMetrics());
    }
}

