package org.dev_alex.mojo_qa.mojo.fragments;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.core.content.ContextCompat;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;

import org.dev_alex.mojo_qa.mojo.R;
import org.dev_alex.mojo_qa.mojo.custom_views.indicator.IndicatorLayout;
import org.dev_alex.mojo_qa.mojo.models.IndicatorModel;
import org.json.JSONArray;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PanelPageFragment extends Fragment {
    private final static String PAGE = "page";

    private View rootView;
    private LinearLayout dashbordContainer;
    private SimpleDateFormat xDateFormat = new SimpleDateFormat("dd-MM", Locale.getDefault());
    private Integer color;


    public static PanelPageFragment newInstance(JSONObject page) {
        Bundle args = new Bundle();
        args.putString(PAGE, page.toString());
        PanelPageFragment fragment = new PanelPageFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_panel_page, container, false);
        refreshLayout();
        return rootView;
    }

    public void refreshLayout() {
        try {
            if (rootView != null) {
                dashbordContainer = (LinearLayout) rootView.findViewById(R.id.dashboard_container);
                JSONObject page = new JSONObject(getArguments().getString(PAGE));
                renderPageLayout(page);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void renderPageLayout(JSONObject pageJson) {
        try {
            dashbordContainer.removeAllViewsInLayout();

            JSONArray pageItems = pageJson.getJSONArray("items");
            for (int i = 0; i < pageItems.length(); i++) {
                JSONObject row = pageItems.getJSONObject(i).getJSONObject("row");
                JSONArray rowItems = row.getJSONArray("items");
                for (int j = 0; j < rowItems.length(); j++) {
                    JSONObject rowItem = rowItems.getJSONObject(j);
                    color= Integer.parseInt(rowItem.getJSONObject("color").getString("autoCaption"));
                    

                    View title = null;
                    if (rowItem.has("indicator")) {
                        title = renderGraphTitle(rowItem.getJSONObject("indicator").getString("autoCaption"));
                        dashbordContainer.addView(title);
                     
                        dashbordContainer.addView(renderIndicator(rowItem.getJSONObject("indicator")));
                    }

                    if (rowItem.has("histogram")) {
                        title = renderGraphTitle(rowItem.getJSONObject("histogram").getString("autoCaption"));
                        dashbordContainer.addView(title);
                        dashbordContainer.addView(renderHistogramOrSpline(rowItem.getJSONObject("histogram"), false));
                    }

                    if (rowItem.has("spline")) {
                        title = renderGraphTitle(rowItem.getJSONObject("spline").getString("autoCaption"));
                        dashbordContainer.addView(title);
                        dashbordContainer.addView(renderHistogramOrSpline(rowItem.getJSONObject("spline"), true));
                    }
                    


                    if (title != null) {
                        LinearLayout.LayoutParams layoutParams = ((LinearLayout.LayoutParams) title.getLayoutParams());

                        layoutParams.topMargin = dpToPx(18);
                        layoutParams.bottomMargin = dpToPx(-5);

                        if (i == 0 && j == 0)
                            layoutParams.topMargin = dpToPx(5);

                        title.setLayoutParams(layoutParams);
                        title.requestLayout();
                    }

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private View renderIndicator(JSONObject indicatorObj) throws Exception {
        IndicatorModel indicatorModel = new IndicatorModel();

        if (indicatorObj.has("ranges")) {
            indicatorModel.ranges = new ObjectMapper()
                    .readValue(indicatorObj.getJSONArray("ranges").toString(), new TypeReference<ArrayList<IndicatorModel.Range>>() {
                    });
        }

        IndicatorLayout indicatorLayout = new IndicatorLayout(getContext(), indicatorModel);
        indicatorLayout.setCurrentValue(indicatorObj.getInt("value"));
        return indicatorLayout;
    }

    private View renderGraphTitle(String title) {
        View titleView = LayoutInflater.from(getContext()).inflate(R.layout.title_graph, null);
        ((TextView) titleView.findViewById(R.id.title)).setText(title);
        renderPoint(color);
        return titleView;
    }
    private View renderPoint(Integer color) {
        View titleView = LayoutInflater.from(getContext()).inflate(R.layout.title_graph, null);
        titleView.findViewById(R.id.point).setBackgroundColor(color);

        return titleView;
    }

    private View renderHistogramOrSpline(JSONObject graphObj, boolean isSpline) throws Exception {
        Resources resources = getContext().getResources();
        int chartHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 300, resources.getDisplayMetrics());
        RelativeLayout chartContainer = new RelativeLayout(getContext());
        chartContainer.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, chartHeight));

        final JSONArray ticks = graphObj.getJSONArray("tick");

        float yMin = 0, yMax = 0;
        List<Entry> entries = new ArrayList<>();
        List<BarEntry> barEntries = new ArrayList<>();
        final List<String> xValues = new ArrayList<>();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        for (int k = 0; k < ticks.length(); k++) {
            JSONObject tick = ticks.getJSONObject(k);
            Date date = sdf.parse(tick.getString("DT"));

            entries.add(new Entry(k, (float) tick.getDouble("value")));
            BarEntry barEntry = new BarEntry((float) k, (float) tick.getDouble("value"));
            barEntries.add(barEntry);
            xValues.add(xDateFormat.format(date));

            if (k == 0) {
                yMax = (float) tick.getDouble("value");
                yMin = (float) tick.getDouble("value");
            } else {
                if (tick.getDouble("value") > yMax)
                    yMax = (float) tick.getDouble("value");

                if (tick.getDouble("value") < yMin)
                    yMin = (float) tick.getDouble("value");
            }
        }
        if (entries.isEmpty()) {
            entries.add(new Entry(0, 0));
            barEntries.add(new BarEntry(0, 0));

            TextView emptyText = new TextView(getContext());
            emptyText.setText(R.string.no_data);
            chartContainer.addView(emptyText);

            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams
                    (ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
            emptyText.setLayoutParams(layoutParams);
            emptyText.requestLayout();
        }

        ArrayList<Integer> colors = new ArrayList<>();
        ArrayList<IndicatorModel.Range> ranges = new ArrayList<>();
        if (graphObj.has("ranges")) {
            ranges = new ObjectMapper()
                    .readValue(graphObj.getJSONArray("ranges").toString(), new TypeReference<ArrayList<IndicatorModel.Range>>() {
                    });


        }

        for (int k = 0; k < entries.size(); k++) {
            float value = entries.get(k).getY();
            int defaultColor = Color.parseColor("#2baaf6");

            for (IndicatorModel.Range range : ranges) {
                if (value >= range.from && value <= range.to)
                    defaultColor = Color.parseColor("#AA" + range.color.substring(1));
            }
            colors.add(defaultColor);
        }


        if (isSpline) {
            LineChart lineChart = new LineChart(getContext());
            lineChart.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));

            LineDataSet dataSet = new LineDataSet(entries, "");

            dataSet.setCircleColors(colors);
            dataSet.setDrawCircles(true);
            dataSet.setCircleRadius(3f);
            dataSet.setDrawCircleHole(false);

            dataSet.setMode(LineDataSet.Mode.LINEAR);

            dataSet.setDrawHighlightIndicators(true);
            dataSet.setHighlightEnabled(true);
            dataSet.setHighLightColor(Color.RED);

            dataSet.setValueTextColor(Color.BLACK);

            dataSet.setLineWidth(2f);
            dataSet.setColor(Color.parseColor("#AFA8DC"));

            dataSet.setDrawFilled(true);

            LineData lineData = new LineData(dataSet);
            lineData.setDrawValues(false);

            lineChart.setData(lineData);
            lineChart.getLegend().setEnabled(false);
            lineChart.getDescription().setEnabled(false);
            lineChart.setTouchEnabled(true);
            lineChart.setScaleYEnabled(false);
            lineChart.setScaleXEnabled(true);
            lineChart.setAutoScaleMinMaxEnabled(true);

            setupAxis(lineChart.getXAxis(), xValues);

            lineChart.getAxisRight().setEnabled(false);
            lineChart.invalidate(); // refresh
            lineChart.getAxisLeft().resetAxisMinimum();
            lineChart.getAxisLeft().setAxisMaximum(lineChart.getAxisLeft().getAxisMaximum() * 1.2f);

            lineChart.getAxisLeft().setGridColor(Color.parseColor("#374E3F60"));
            lineChart.getXAxis().setGridColor(Color.parseColor("#374E3F60"));


            lineChart.getXAxis().setDrawAxisLine(false);
            lineChart.getAxisLeft().setDrawAxisLine(false);
            lineChart.getAxisRight().setDrawAxisLine(false);

            CustomMarkerView mv = new CustomMarkerView(getContext(), R.layout.higlight_marker, xValues, 0, xValues.size() - 1, yMin, yMax);
            lineChart.setMarker(mv);
            chartContainer.addView(lineChart);
        } else {
            BarChart barChart = new BarChart(getContext());
            barChart.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));
            barChart.setDrawValueAboveBar(false);

            BarDataSet set = new BarDataSet(barEntries, "BarDataSet");
            set.setColors(colors);

            BarData barData = new BarData(set);
            barData.setDrawValues(false);
            barData.setBarWidth(1f);

            //barChart.getAxisLeft().setAxisMaximum(barChart.getAxisLeft().getAxisMaximum() * 1.2f);

            barChart.getAxisRight().setEnabled(false);
            barChart.getAxisLeft().setGridColor(Color.parseColor("#374E3F60"));
            barChart.getAxisLeft().setAxisLineColor(Color.parseColor("#374E3F60"));
            barChart.getXAxis().setGridColor(Color.parseColor("#374E3F60"));
            barChart.getXAxis().setAxisLineColor(Color.parseColor("#374E3F60"));

            barChart.setData(barData);
            barChart.setFitBars(false);
            barChart.getLegend().setEnabled(false);
            barChart.getDescription().setEnabled(false);
            barChart.setScaleYEnabled(true);
            setupAxis(barChart.getXAxis(), xValues);

            barChart.invalidate();
            barChart.zoom(barEntries.size() / 10, 0.9f, 0, 0);

            CustomMarkerView mv = new CustomMarkerView(getContext(), R.layout.higlight_marker, xValues, 0, xValues.size() - 1, yMin, yMax);
            barChart.setMarker(mv);
            chartContainer.addView(barChart);
        }
        return chartContainer;
    }

    private void setupAxis(XAxis xAxis, final List<String> xValues) {
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        //xAxis.setLabelRotationAngle(90);
        xAxis.setDrawLabels(true);
        xAxis.setEnabled(true);
        xAxis.setTextColor(Color.parseColor("#4E3F60"));

        xAxis.setValueFormatter(new IAxisValueFormatter() {

            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                String stringValue;
                if (xValues.size() >= 0 && value >= 0) {
                    int finalIPos = -1;

                    if ((((int) value == value)))
                        finalIPos = (int) value;
                    else {
                        int startV = (int) value;
                        int endV = startV + 1;
                        if (endV >= xValues.size())
                            finalIPos = (int) value;
                        else {
                            try {
                                long startDate = xDateFormat.parse(xValues.get(startV)).getTime();
                                long endDate = xDateFormat.parse(xValues.get(endV)).getTime();
                                long delta = endDate - startDate;

                                double valueDelta = value - ((int) value);
                                long resultDate = startDate + (long) (delta * valueDelta);
                                return xDateFormat.format(new Date(resultDate));
                            } catch (Exception exc) {
                                exc.printStackTrace();
                                return "exc";
                            }
                        }
                    }

                    if (finalIPos != -1 && finalIPos < xValues.size()) {
                        stringValue = xValues.get(finalIPos);
                    } else {
                        stringValue = "";
                    }
                } else {
                    stringValue = "";
                }
                return stringValue;
            }
        });
    }

    public class CustomMarkerView extends MarkerView {

        private TextView xValue;
        private TextView yValue;
        private List<String> xValues;

        private float xMin, xMax, yMin, yMax;

        public CustomMarkerView(Context context, int layoutResource, List<String> xValues, float xMin, float xMax, float yMin, float yMax) {
            super(context, layoutResource);
            xValue = (TextView) findViewById(R.id.x_value);
            yValue = (TextView) findViewById(R.id.y_value);
            this.xValues = xValues;

            this.xMin = xMin;
            this.xMax = xMax;
            this.yMin = yMin;
            this.yMax = yMax;
        }

        @Override
        public void refreshContent(Entry e, Highlight highlight) {
            super.refreshContent(e, highlight);
            try {
                if (xValues.isEmpty()) {
                    xValue.setText("0");
                    yValue.setText("0");
                    xValue.requestLayout();
                    yValue.requestLayout();
                    return;
                }
                
                SimpleDateFormat dateFotmat = new SimpleDateFormat("dd.MM.yyyy | HH:mm", Locale.getDefault());
                xValue.setText(dateFotmat.format(xDateFormat.parse(xValues.get((int) e.getX())).getTime()));
                yValue.setText(String.format(Locale.getDefault(), "%.2f", e.getY()));
                xValue.requestLayout();
                yValue.requestLayout();

                float xVal = e.getX();
                float yVal = e.getY();

                float xPosPercent = (xMax - xMin) == 0 ? 0 : (xVal - xMin) / (xMax - xMin);
                float yPosPercent = (yMax - yMin) == 0 ? 0 : (yVal - yMin) / (yMax - yMin);

                float xOffset, yOffset;
                if (xPosPercent > 0.65)
                    xOffset = -getMeasuredWidth();
                else
                    xOffset = dpToPx(10);

                Log.d("testt", xPosPercent + " " + yPosPercent);
                if (yPosPercent < 0.25)
                    yOffset = -getMeasuredHeight();
                else
                    yOffset = dpToPx(-10);

                setOffset(xOffset, yOffset);

            } catch (ParseException e1) {
                e1.printStackTrace();
            }
        }
    }

    private int dpToPx(int dp) {
        Resources resources = getContext().getResources();
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.getDisplayMetrics());
    }
}
