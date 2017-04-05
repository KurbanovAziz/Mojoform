package org.dev_alex.mojo_qa.mojo.custom_views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.dev_alex.mojo_qa.mojo.R;


public class PercentageView extends RelativeLayout {
    View percentageDark;
    RelativeLayout percentageLight;
    View background;
    TextView percents;

    int backgroundColor, lightPercentsColor, darkPercentsColor;
    int percentsNum;

    public PercentageView(Context context) {
        super(context);
        init();
    }

    public PercentageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray typedArray = context.getTheme().obtainStyledAttributes(
                attrs, R.styleable.PercentageView, 0, 0);

        backgroundColor = typedArray.getColor(R.styleable.PercentageView_backgroundColor, Color.parseColor("#E0D7E2"));
        lightPercentsColor = typedArray.getColor(R.styleable.PercentageView_lightPercentageColor, Color.GREEN);
        darkPercentsColor = typedArray.getColor(R.styleable.PercentageView_darkPercentageColor, Color.RED);
        percentsNum = typedArray.getInteger(R.styleable.PercentageView_percents, 11);
        init();
    }

    public PercentageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray typedArray = context.getTheme().obtainStyledAttributes(
                attrs, R.styleable.PercentageView, 0, 0);

        backgroundColor = typedArray.getColor(R.styleable.PercentageView_backgroundColor, Color.parseColor("#E0D7E2"));
        lightPercentsColor = typedArray.getColor(R.styleable.PercentageView_lightPercentageColor, Color.GREEN);
        darkPercentsColor = typedArray.getColor(R.styleable.PercentageView_darkPercentageColor, Color.RED);
        percentsNum = typedArray.getInteger(R.styleable.PercentageView_percents, 11);
        init();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public PercentageView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        TypedArray typedArray = context.getTheme().obtainStyledAttributes(
                attrs, R.styleable.PercentageView, 0, 0);

        backgroundColor = typedArray.getColor(R.styleable.PercentageView_backgroundColor, Color.parseColor("#E0D7E2"));
        lightPercentsColor = typedArray.getColor(R.styleable.PercentageView_lightPercentageColor, Color.GREEN);
        darkPercentsColor = typedArray.getColor(R.styleable.PercentageView_darkPercentageColor, Color.RED);
        percentsNum = typedArray.getInteger(R.styleable.PercentageView_percents, 11);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.percentage_view_layout, this);
        percentageDark = findViewById(R.id.percentage_dark);
        percentageLight = (RelativeLayout) findViewById(R.id.percentage_light);
        background = findViewById(R.id.background_color);
        percents = (TextView) findViewById(R.id.percents);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        GradientDrawable bgShape = (GradientDrawable) background.getBackground();
        bgShape.setColor(backgroundColor);

        GradientDrawable lightPercentsShape = (GradientDrawable) percentageLight.getBackground();
        lightPercentsShape.setColor(lightPercentsColor);

        percentageDark.setBackground(new BitmapDrawable(getResources(), drawDarkPercentageCircle()));

        percents.setText(String.valueOf(percentsNum));
    }

    public Bitmap drawDarkPercentageCircle() {
        Bitmap pallet = Bitmap.createBitmap(150, 150, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(pallet);

        final RectF oval = new RectF();
        Paint paint = new Paint();
        paint.setColor(darkPercentsColor); // установим белый цвет
        paint.setStrokeWidth(0);
        paint.setStyle(Paint.Style.FILL); // заливаем
        paint.setAntiAlias(true);
        oval.set(0, 0, pallet.getWidth(), pallet.getHeight());
        canvas.drawArc(oval, 270, -(360 * percentsNum / 100), true, paint); // рисуем пакмана

        return pallet;
    }

    public void setPercents(int percents) {
        this.percents.setText(String.valueOf(percents));
    }
}
