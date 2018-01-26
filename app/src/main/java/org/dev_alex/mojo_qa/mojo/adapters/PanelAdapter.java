package org.dev_alex.mojo_qa.mojo.adapters;


import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
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
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Locale;

public class PanelAdapter extends RecyclerView.Adapter<PanelAdapter.TaskViewHolder> {
    private List<Panel> panels;
    private OnPanelClickListener onPanelClickListener;

    static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView panelName;
        TextView panelStats;
        ImageView panelIcon;

        TaskViewHolder(View itemView) {
            super(itemView);
            panelIcon = (ImageView) itemView.findViewById(R.id.panel_icon_image);
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
            v = new View(viewGroup.getContext());

            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dpToPx(viewGroup.getContext(), 2.5f));
            layoutParams.rightMargin = dpToPx(viewGroup.getContext(), 8);
            layoutParams.leftMargin = dpToPx(viewGroup.getContext(), 8);
            layoutParams.topMargin = dpToPx(viewGroup.getContext(), 18);
            layoutParams.bottomMargin = dpToPx(viewGroup.getContext(), 12);

            v.setLayoutParams(layoutParams);
            v.setBackgroundColor(Color.parseColor("#4E3F60"));
        }
        return new TaskViewHolder(v);
    }

    @Override
    public void onBindViewHolder(TaskViewHolder viewHolder, int i) {
        final Panel panel = panels.get(i);

        if (getItemViewType(i) == 0) {
            viewHolder.panelName.setText(panel.name);
            viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onPanelClickListener.onClick(panel);
                }
            });

            try {
                double firstVal, secondVal;
                boolean isPercents = true;

                if (TextUtils.isEmpty(panel.config)) {
                    firstVal = panel.current.day.prc;
                    secondVal = panel.complete.day.prc;
                } else {
                    JSONObject config = new JSONObject(panel.config);
                    Value valueCurrent;
                    Value valueComplete;
                    switch (config.getString("aggregation")) {
                        case "day":
                            valueCurrent = panel.current.day;
                            valueComplete = panel.complete.day;
                            break;
                        case "week":
                            valueCurrent = panel.current.week;
                            valueComplete = panel.complete.week;
                            break;
                        case "month":
                            valueCurrent = panel.current.month;
                            valueComplete = panel.complete.month;
                            break;
                        case "year":
                            valueCurrent = panel.current.year;
                            valueComplete = panel.complete.year;
                            break;

                        default:
                            valueCurrent = new Value();
                            valueComplete = new Value();
                    }

                    isPercents = config.getInt("dataType") == 2;
                    if (isPercents) {
                        firstVal = valueCurrent.prc;
                        secondVal = valueComplete.prc;
                    } else {
                        firstVal = valueCurrent.val;
                        secondVal = valueComplete.val;
                    }
                }
                viewHolder.panelStats.setText(String.format(Locale.getDefault(), "%.1f | %.1f%s",
                        firstVal, secondVal, isPercents ? "%" : " баллов"));
            } catch (JSONException e) {
                e.printStackTrace();
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

