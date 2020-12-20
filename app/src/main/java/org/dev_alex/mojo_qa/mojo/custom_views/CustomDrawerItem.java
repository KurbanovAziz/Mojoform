package org.dev_alex.mojo_qa.mojo.custom_views;

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;

import org.dev_alex.mojo_qa.mojo.R;

import java.util.List;


public class CustomDrawerItem extends SecondaryDrawerItem {
    private int fontSizeSp;
    private int marginTopDp;

    public View badge;
    public boolean isVisible = false;

    public CustomDrawerItem(int fontSizeSp, int marginTopDp) {
        this.fontSizeSp = fontSizeSp;
        this.marginTopDp = marginTopDp;
    }

    @Override
    public void bindView(ViewHolder viewHolder, List payloads) {
        super.bindView(viewHolder, payloads);
        DisplayMetrics displaymetrics = viewHolder.itemView.getContext().getResources().getDisplayMetrics();
        int marginTop = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, marginTopDp, displaymetrics);
        int imageHeightDp = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 45, displaymetrics);
        int imagePaddingRight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, displaymetrics);
        int heightDp = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 62, displaymetrics);

        RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) viewHolder.itemView.getLayoutParams();
        layoutParams.topMargin = marginTop;
        layoutParams.height = heightDp;
        viewHolder.itemView.setLayoutParams(layoutParams);

        TextView text = ((TextView) viewHolder.itemView.findViewById(R.id.material_drawer_name));
        LinearLayout.LayoutParams textParams = (LinearLayout.LayoutParams) text.getLayoutParams();
        textParams.height = LinearLayout.LayoutParams.WRAP_CONTENT;
        text.setLayoutParams(textParams);

        text.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSizeSp);

        ImageView icon = (ImageView) viewHolder.itemView.findViewById(R.id.material_drawer_icon);
        icon.setScaleType(ImageView.ScaleType.FIT_START);
        LinearLayout.LayoutParams iconParams = (LinearLayout.LayoutParams) icon.getLayoutParams();
        iconParams.width = LinearLayout.LayoutParams.WRAP_CONTENT;
        iconParams.gravity = Gravity.LEFT | Gravity.CENTER_VERTICAL;
        iconParams.height = imageHeightDp;

        icon.setLayoutParams(iconParams);
        icon.setAdjustViewBounds(true);
        icon.setPadding(icon.getPaddingLeft(), icon.getPaddingTop(), imagePaddingRight, icon.getPaddingBottom());

        ViewGroup parent = (ViewGroup) icon.getParent();

        badge = new View(parent.getContext());
        badge.setBackgroundResource(R.drawable.bg_red_circle);
        parent.addView(badge, 1);

        int size = convertToPx(parent.getContext(), 6);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(size, size);
        params.leftMargin = -convertToPx(parent.getContext(), 21);
        params.rightMargin = convertToPx(parent.getContext(), 21);
        params.topMargin = convertToPx(parent.getContext(), 17);
        badge.setLayoutParams(params);

        if (isVisible) {
            badge.setVisibility(View.VISIBLE);
        } else {
            badge.setVisibility(View.INVISIBLE);
        }
    }

    private int convertToPx(Context context, int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
    }
}