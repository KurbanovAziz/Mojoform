package org.dev_alex.mojo_qa.mojo.adapters;


import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.dev_alex.mojo_qa.mojo.R;
import org.dev_alex.mojo_qa.mojo.models.Panel;

import java.util.List;


public class PanelAdapter extends RecyclerView.Adapter<PanelAdapter.TaskViewHolder> {
    private List<Panel> panels;
    private OnPanelClickListener onPanelClickListener;

    static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView panelName;
        ImageView panelIcon;

        TaskViewHolder(View itemView) {
            super(itemView);
            panelIcon = (ImageView) itemView.findViewById(R.id.panel_icon_image);
            panelName = (TextView) itemView.findViewById(R.id.panel_name);
        }
    }


    public PanelAdapter(List<Panel> panels, OnPanelClickListener onPanelClickListener) {
        this.panels = panels;
        this.onPanelClickListener = onPanelClickListener;
    }

    @Override
    public TaskViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.card_panel, viewGroup, false);
        return new TaskViewHolder(v);
    }

    @Override
    public void onBindViewHolder(TaskViewHolder viewHolder, int i) {
        final Panel panel = panels.get(i);
        viewHolder.panelName.setText(panel.name);
        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onPanelClickListener.onClick(panel);
            }
        });
    }

    @Override
    public int getItemCount() {
        return panels.size();
    }

    public interface OnPanelClickListener {
        void onClick(Panel panel);
    }
}

