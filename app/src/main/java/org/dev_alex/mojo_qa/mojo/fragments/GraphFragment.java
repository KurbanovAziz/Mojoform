package org.dev_alex.mojo_qa.mojo.fragments;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;

import org.dev_alex.mojo_qa.mojo.R;
import org.dev_alex.mojo_qa.mojo.models.GraphInfo;
import org.dev_alex.mojo_qa.mojo.models.IndicatorModel;
import org.dev_alex.mojo_qa.mojo.models.Value;
import org.dev_alex.mojo_qa.mojo.services.RequestService;
import org.dev_alex.mojo_qa.mojo.services.Utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import okhttp3.Response;

public class GraphFragment extends Fragment {
    public static final String DAY = "day";
    public static final String WEEK = "week";
    public static final String MONTH = "month";
    public static final String YEAR = "year";

    private static final String TYPE_ARG = "type";
    private static final String ID_ARG = "panel_id";
    private static final String IS_PERCENTS_ARG = "is_percents";

    private View rootView;
    private String type;
    private long panelId;
    private boolean isPercents;
    private GraphInfo graphInfo;
    private SimpleDateFormat xDateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
    private SimpleDateFormat xDateFormatNoYear = new SimpleDateFormat("dd-MM", Locale.getDefault());


    public static GraphFragment newInstance(String type, long id, boolean isPersents) {
        Bundle args = new Bundle();
        GraphFragment fragment = new GraphFragment();

        args.putString(TYPE_ARG, type);
        args.putLong(ID_ARG, id);
        args.putBoolean(IS_PERCENTS_ARG, isPersents);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        type = getArguments().getString(TYPE_ARG);
        panelId = getArguments().getLong(ID_ARG);
        isPercents = getArguments().getBoolean(IS_PERCENTS_ARG);

        if (rootView == null) {
            rootView = inflater.inflate(R.layout.fragment_graph, container, false);
            setListeners();
            Utils.setupCloseKeyboardUI(getActivity(), rootView);

            new GetGraphTask().execute();
        }
        return rootView;
    }

    private void setListeners() {

    }

    private void buildGraph() {
        try {
            ((ViewGroup) rootView).addView(renderHistogram(graphInfo));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private View renderHistogram(GraphInfo graphInfo) throws Exception {
        Resources resources = getContext().getResources();
        int chartHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 300, resources.getDisplayMetrics());
        RelativeLayout chartContainer = new RelativeLayout(getContext());
        chartContainer.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, chartHeight));

        float yMin = 0, yMax = 0;
        List<BarEntry> barEntries = new ArrayList<>();
        final List<String> xValues = new ArrayList<>();

        int k = 0;
        for (Value value : graphInfo.values) {
            Date date = new Date(value.from);

            BarEntry barEntry;

            if (isPercents) {
                barEntry = new BarEntry((float) k, (float) value.prc);

                if (k == 0) {
                    yMax = (float) value.prc;
                    yMin = (float) value.prc;
                } else {
                    if (value.prc > yMax)
                        yMax = (float) value.prc;

                    if (value.prc < yMin)
                        yMin = (float) value.prc;
                }
            } else {
                barEntry = new BarEntry((float) k, (float) value.val);

                if (k == 0) {
                    yMax = (float) value.val;
                    yMin = (float) value.val;
                } else {
                    if (value.val > yMax)
                        yMax = (float) value.val;

                    if (value.val < yMin)
                        yMin = (float) value.val;
                }
            }
            barEntries.add(barEntry);
            xValues.add(xDateFormat.format(date));
            k++;
        }
        if (barEntries.isEmpty()) {
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
        /*    if (graphInfo.has("ranges")) {
            ranges = new ObjectMapper()
                    .readValue(graphObj.getJSONArray("ranges").toString(), new TypeReference<ArrayList<IndicatorModel.Range>>() {
                    });
        }
        */
        for (k = 0; k < barEntries.size(); k++)

        {
            float value = barEntries.get(k).getY();
            int defaultColor = Color.parseColor("#2baaf6");

            for (IndicatorModel.Range range : ranges) {
                if (value >= range.from && value <= range.to)
                    defaultColor = Color.parseColor("#AA" + range.color.substring(1));
            }
            colors.add(defaultColor);
        }


        BarChart barChart = new BarChart(getContext());
        barChart.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));
        barChart.setDrawValueAboveBar(false);

        BarDataSet set = new BarDataSet(barEntries, "BarDataSet");
        set.setColors(colors);

        BarData barData = new BarData(set);
        barData.setDrawValues(false);
        barData.setBarWidth(1f);

        //barChart.getAxisLeft().setAxisMaximum(barChart.getAxisLeft().getAxisMaximum() * 1.2f);

        barChart.getAxisRight().

                setEnabled(false);
        barChart.getAxisLeft().

                setGridColor(Color.parseColor("#374E3F60"));
        barChart.getAxisLeft().

                setAxisLineColor(Color.parseColor("#374E3F60"));
        barChart.getXAxis().

                setGridColor(Color.parseColor("#374E3F60"));
        barChart.getXAxis().

                setAxisLineColor(Color.parseColor("#374E3F60"));

        barChart.setData(barData);
        barChart.setFitBars(false);
        barChart.getLegend().

                setEnabled(false);
        barChart.getDescription().

                setEnabled(false);
        barChart.setScaleYEnabled(true);

        setupAxis(barChart.getXAxis(), xValues);

        barChart.invalidate();
        barChart.zoom(barEntries.size() / 10, 0.9f, 0, 0);

        CustomMarkerView mv = new CustomMarkerView(getContext(), R.layout.higlight_marker, xValues, 0, xValues.size() - 1, yMin, yMax);
        barChart.setMarker(mv);
        chartContainer.addView(barChart);

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
                                return xDateFormatNoYear.format(new Date(resultDate));
                            } catch (Exception exc) {
                                exc.printStackTrace();
                                return "exc";
                            }
                        }
                    }

                    if (finalIPos != -1 && finalIPos < xValues.size()) {
                        try {
                            stringValue = xDateFormatNoYear.format(xDateFormat.parse(xValues.get(finalIPos)));
                        } catch (ParseException e) {
                            e.printStackTrace();
                            stringValue = "";
                        }
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

                String yValStr = String.format(Locale.getDefault(), "%.2f", e.getY());
                if (isPercents)
                    yValStr += " %";
                else
                    yValStr += " б.";

                yValue.setText(yValStr);
                xValue.requestLayout();
                yValue.requestLayout();
                yValue.invalidate();

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

    public class GetGraphTask extends AsyncTask<Void, Void, Integer> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Integer doInBackground(Void... params) {
            try {
                Response response = RequestService.createGetRequest("/api/analytic/graph/" + panelId + "/" + type);
                String responseStr = response.body().string();

                graphInfo = new ObjectMapper().readValue(responseStr, GraphInfo.class);
                graphInfo.fixDates();

                return response.code();
            } catch (Exception exc) {
                exc.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(Integer responseCode) {
            super.onPostExecute(responseCode);
            if (responseCode == 200 || responseCode == 201)
                buildGraph();
            else {
                Toast.makeText(getContext(), "Попробуйте позже", Toast.LENGTH_SHORT).show();
                getActivity().getSupportFragmentManager().popBackStack();
            }
        }
    }
}
