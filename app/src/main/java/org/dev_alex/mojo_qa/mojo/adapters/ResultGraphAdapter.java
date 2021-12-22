package org.dev_alex.mojo_qa.mojo.adapters;


import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;

import org.dev_alex.mojo_qa.mojo.R;
import org.dev_alex.mojo_qa.mojo.models.Panel;
import org.dev_alex.mojo_qa.mojo.models.Value;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import lecho.lib.hellocharts.model.PieChartData;
import lecho.lib.hellocharts.model.SliceValue;
import lecho.lib.hellocharts.view.PieChartView;

public class ResultGraphAdapter extends RecyclerView.Adapter<ResultGraphAdapter.ResultViewHolder> {
    private List<Panel> panels;
    private OnPanelClickListener onPanelClickListener;

    static class ResultViewHolder extends RecyclerView.ViewHolder {
        TextView panelName;
        TextView panelStats;
        ImageView panelIcon;

        ResultViewHolder(View itemView) {
            super(itemView);
            panelIcon = (ImageView) itemView.findViewById(R.id.panel_icon_imag);
            panelName = (TextView) itemView.findViewById(R.id.panel_nam);
            panelStats = (TextView) itemView.findViewById(R.id.panel_stat);

        }
    }


    public ResultGraphAdapter(List<Panel> panels, OnPanelClickListener onPanelClickListener) {
        this.panels = panels;
        this.onPanelClickListener = onPanelClickListener;
    }

    @Override
    public int getItemViewType(int position) {
        if(position == 0){return 2;}
        if (panels.get(position).isSeparator()){return 1;}
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
        final Panel panel = panels.get(i);

        if (getItemViewType(i) == 0) {
            viewHolder.panelName.setText(panel.name);
}
            viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onPanelClickListener.onClick(panel);
                }
            });

if(viewHolder.panelStats != null){
                    viewHolder.panelStats.setText(
                            new StringBuilder().append(panel.val).append(" | ").append(panel.prc).append("%").toString());
                viewHolder.panelName.setText(panel.name);}



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

