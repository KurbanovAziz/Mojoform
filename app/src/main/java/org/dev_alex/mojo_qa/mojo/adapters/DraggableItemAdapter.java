package org.dev_alex.mojo_qa.mojo.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.woxthebox.draglistview.DragItemAdapter;

import org.dev_alex.mojo_qa.mojo.R;

import java.util.List;

public class DraggableItemAdapter extends DragItemAdapter<String, DraggableItemAdapter.ViewHolder> {
    public static final int TASKS = 123;
    public static final int DOCS = 231;
    public static final int ANALYTICS = 312;

    private int mLayoutId;
    private int mGrabHandleId;
    private boolean mDragOnLongPress;
    private Context context;

    public DraggableItemAdapter(Context context, List<String> list, int layoutId, int grabHandleId, boolean dragOnLongPress) {
        mLayoutId = layoutId;
        mGrabHandleId = grabHandleId;
        mDragOnLongPress = dragOnLongPress;
        this.context = context;
        setItemList(list);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(mLayoutId, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        String text = mItemList.get(position);
        switch (text) {
            case "docs":
                holder.value.setText(context.getString(R.string.documents));
                holder.icon.setImageResource(R.drawable.documents);
                break;
            case "tasks":
                holder.value.setText(context.getString(R.string.tasks));
                holder.icon.setImageResource(R.drawable.tasks);
                break;
            case "analytics":
                holder.value.setText(context.getString(R.string.analystics));
                holder.icon.setImageResource(R.drawable.analystics_icon);
                break;
        }
        holder.itemView.setTag(mItemList.get(position));
    }

    @Override
    public long getUniqueItemId(int position) {
        switch (getItemList().get(position)) {
            case "docs":
                return DOCS;
            case "tasks":
                return TASKS;
            case "analytics":
                return ANALYTICS;
        }
        return -1;
    }

    class ViewHolder extends DragItemAdapter.ViewHolder {
        TextView value;
        ImageView icon;

        ViewHolder(final View itemView) {
            super(itemView, mGrabHandleId, mDragOnLongPress);
            value = (TextView) itemView.findViewById(R.id.value);
            icon = (ImageView) itemView.findViewById(R.id.left_icon);
        }

    }
}