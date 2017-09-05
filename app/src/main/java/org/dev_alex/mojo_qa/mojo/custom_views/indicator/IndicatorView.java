package org.dev_alex.mojo_qa.mojo.custom_views.indicator;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;

import org.dev_alex.mojo_qa.mojo.models.IndicatorModel;

import java.util.ArrayList;

public class IndicatorView extends View {
    private int w = dpToPx(210);
    private int h = dpToPx(210);
    private IndicatorModel indicatorModel;
    private static final int START_ANGLE_POINT = 128;
    private static final int ANGLE_ARC = 284;
    private Paint indicatorPaint;
    private Paint indicatorTextPaint;
    private Paint indicatorLineTextPaint;
    private RectF indicatorValueRect;
    private RectF indicatorLineRect;
    private RectF indicatorLineTextRect;
    private Paint gradientPaint;

    private ArrayList<Paint> indicatorLinePaints;
    private int currentIndicatorValue = -11112222;

    private Path mArc;
    private Bitmap bitmap;
    private Canvas tmpCanvas;

    public IndicatorView(Context context, IndicatorModel indicatorModel) {
        super(context);
        this.indicatorModel = indicatorModel;
        indicatorLinePaints = new ArrayList<>();
        final int indicatorLineWidth = dpToPx(6);

        for (int i = 0; i < indicatorModel.ranges.size(); i++) {
            Paint indicatorLinePaint = new Paint();
            indicatorLinePaint.setAntiAlias(true);
            indicatorLinePaint.setStyle(Paint.Style.STROKE);
            indicatorLinePaint.setStrokeWidth(indicatorLineWidth);
            indicatorLinePaint.setColor(Color.parseColor(indicatorModel.ranges.get(i).color));

            indicatorLinePaints.add(indicatorLinePaint);
        }

        final int indicatorWidth = dpToPx(25);

        indicatorPaint = new Paint();
        indicatorPaint.setAntiAlias(true);
        indicatorPaint.setStyle(Paint.Style.STROKE);
        indicatorPaint.setStrokeWidth(indicatorWidth);
        indicatorPaint.setColor(Color.parseColor("#2baaf6"));

        int indicatorValueOffset = dpToPx(43);
        indicatorValueRect = new RectF(indicatorValueOffset, indicatorValueOffset, w - indicatorValueOffset, h - indicatorValueOffset);

        int indicatorLineOffset = dpToPx(20);
        indicatorLineRect = new RectF(indicatorLineOffset, indicatorLineOffset, w - indicatorLineOffset, h - indicatorLineOffset);

        int indicatorLineTextOffset = dpToPx(12);
        indicatorLineTextRect = new RectF(indicatorLineTextOffset, indicatorLineTextOffset, w - indicatorLineTextOffset, h - indicatorLineTextOffset);


        indicatorTextPaint = new Paint();
        indicatorTextPaint.setColor(Color.parseColor("#8C8AB9"));
        indicatorTextPaint.setFakeBoldText(true);
        indicatorTextPaint.setTextAlign(Paint.Align.CENTER);
        indicatorTextPaint.setTextSize(spToPx(32));

        indicatorLineTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        indicatorLineTextPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        indicatorLineTextPaint.setColor(Color.parseColor("#55538D"));
        indicatorLineTextPaint.setFakeBoldText(true);
        indicatorLineTextPaint.setTextSize(spToPx(14));

        mArc = new Path();
        LinearGradient gradient = new LinearGradient(0, 0, 0, h,
                new int[]{Color.parseColor("#FFFFFFFF"), Color.parseColor("#DDFFFFFF"), Color.parseColor("#66FFFFFF")},
                new float[]{0, 0.3f, 1}, Shader.TileMode.REPEAT);
        gradientPaint = new Paint();
        gradientPaint.setDither(true);
        gradientPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.MULTIPLY));
        gradientPaint.setShader(gradient);

        bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        tmpCanvas = new Canvas(bitmap);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        bitmap.eraseColor(Color.TRANSPARENT);
        tmpCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        drawIndicator(tmpCanvas);
        canvas.drawBitmap(bitmap, 0, 0, null);
    }

    private void drawIndicator(Canvas canvas) {
        int totalRangesLength = 0;
        float angleForOnePoint;
        for (IndicatorModel.Range range : indicatorModel.ranges) {
            if (currentIndicatorValue >= range.from && currentIndicatorValue <= range.to)
                indicatorPaint.setColor(Color.parseColor(range.color));
            totalRangesLength += (range.to - range.from);
        }

        if (!indicatorModel.ranges.isEmpty()) {
            angleForOnePoint = ((float) ANGLE_ARC) / totalRangesLength;
            float startIndicatorAngel = START_ANGLE_POINT;
            for (int i = 0; i < indicatorModel.ranges.size(); i++) {
                IndicatorModel.Range range = indicatorModel.ranges.get(i);
                Paint indicatorPaint = indicatorLinePaints.get(i);
                float angel = angleForOnePoint * (range.to - range.from);

                canvas.drawArc(indicatorLineRect, startIndicatorAngel, angel, false, indicatorPaint);

                mArc.reset();
                if (i == 0) {
                    indicatorLineTextPaint.setTextAlign(Paint.Align.LEFT);
                    mArc.addArc(indicatorLineTextRect, startIndicatorAngel, angel);
                    canvas.drawTextOnPath(String.valueOf(range.from), mArc, 0, 0, indicatorLineTextPaint);
                } else {
                    indicatorLineTextPaint.setTextAlign(Paint.Align.CENTER);
                    mArc.addArc(indicatorLineTextRect, startIndicatorAngel - 40, 80);
                    canvas.drawTextOnPath(String.valueOf(range.from), mArc, 0, 0, indicatorLineTextPaint);
                }

                mArc.reset();
                if (i == indicatorModel.ranges.size() - 1) {
                    indicatorLineTextPaint.setTextAlign(Paint.Align.RIGHT);
                    mArc.addArc(indicatorLineTextRect, startIndicatorAngel, angel);
                    canvas.drawTextOnPath(String.valueOf(range.to), mArc, 0, 0, indicatorLineTextPaint);
                }

                startIndicatorAngel += angel;
            }
        }

        canvas.drawArc(indicatorValueRect, START_ANGLE_POINT, ANGLE_ARC, false, indicatorPaint);

        if (currentIndicatorValue != -11112222) {
            int xPos = (canvas.getWidth() / 2);
            int yPos = (int) ((canvas.getHeight() / 2) - ((indicatorTextPaint.descent() + indicatorTextPaint.ascent()) / 2));
            canvas.drawText(String.valueOf(currentIndicatorValue), xPos, yPos, indicatorTextPaint);
        }

        canvas.drawRect(0, 0, w, h, gradientPaint);
    }

    private int dpToPx(int dp) {
        Resources resources = getContext().getResources();
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.getDisplayMetrics());
    }

    private int spToPx(int sp) {
        Resources resources = getContext().getResources();
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, resources.getDisplayMetrics());
    }

    public void setCurrentIndicatorValue(int currentIndicatorValue) {
        this.currentIndicatorValue = currentIndicatorValue;
        invalidate();
        refreshDrawableState();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        ViewGroup.LayoutParams layoutParams = getLayoutParams();
        layoutParams.height = h;
        layoutParams.width = w;
        setLayoutParams(layoutParams);
        requestLayout();
    }
}
