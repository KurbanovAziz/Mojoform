package org.dev_alex.mojo_qa.mojo.custom_views.indicator;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import org.dev_alex.mojo_qa.mojo.R;
import org.dev_alex.mojo_qa.mojo.models.IndicatorModel;

public class IndicatorLayout extends FrameLayout {
    private IndicatorModel indicatorModel;
    private IndicatorView indicatorView;
    private int currentValue = -11112222;

    public IndicatorLayout(@NonNull Context context, IndicatorModel indicatorModel) {
        super(context);
        this.indicatorModel = indicatorModel;
        initLayout(context);
    }

    private void initLayout(Context context) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View view = layoutInflater.inflate(R.layout.indicator_layout, this, true);

        indicatorView = new IndicatorView(context, indicatorModel);
        ((ViewGroup) view.findViewById(R.id.indicator_frame)).addView(indicatorView);
        updateTextValue();
    }

    private void updateTextValue() {
        if (currentValue == -11112222) {
            ((TextView) findViewById(R.id.indicator_value)).setText("Нет данных");
        } else {
            ((TextView) findViewById(R.id.indicator_value)).setText("");

            for (IndicatorModel.Range range : indicatorModel.ranges)
                if (currentValue >= range.from && currentValue <= range.to)
                    ((TextView) findViewById(R.id.indicator_value)).setText(range.name);
        }
    }

    public void setCurrentValue(int currentValue) {
        this.currentValue = currentValue;
        updateTextValue();
        indicatorView.setCurrentIndicatorValue(currentValue);
    }
}
