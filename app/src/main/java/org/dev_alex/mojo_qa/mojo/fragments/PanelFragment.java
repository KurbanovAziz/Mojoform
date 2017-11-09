package org.dev_alex.mojo_qa.mojo.fragments;

import android.app.ProgressDialog;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

import org.dev_alex.mojo_qa.mojo.BuildConfig;
import org.dev_alex.mojo_qa.mojo.R;
import org.dev_alex.mojo_qa.mojo.custom_views.indicator.IndicatorLayout;
import org.dev_alex.mojo_qa.mojo.models.IndicatorModel;
import org.dev_alex.mojo_qa.mojo.models.Panel;
import org.dev_alex.mojo_qa.mojo.services.RequestService;
import org.dev_alex.mojo_qa.mojo.services.Utils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class PanelFragment extends Fragment {
    private View rootView;
    private ProgressDialog loopDialog;
    private Panel panel;
    private JSONObject panelJson;

    private LinearLayout dashbordContainer;
    private SimpleDateFormat isoDateFormatNoTimeZone = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());


    public static PanelFragment newInstance(Panel panel) {
        Bundle args = new Bundle();
        args.putSerializable("panel", panel);

        PanelFragment fragment = new PanelFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.fragment_panel, container, false);
            dashbordContainer = (LinearLayout) rootView.findViewById(R.id.dashbord_container);

            panel = (Panel) getArguments().getSerializable("panel");

            Utils.setupCloseKeyboardUI(getActivity(), rootView);
            initDialog();
            new GetPanelTask().execute();

            rootView.findViewById(R.id.page_btn).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    rootView.findViewById(R.id.page_select_layout).setVisibility(View.VISIBLE);
                }
            });

            ((TextView) rootView.findViewById(R.id.dashbord_name)).setText(panel.name);
        }

        setupHeader();
        return rootView;
    }

    private void initDialog() {
        loopDialog = new ProgressDialog(getContext(), R.style.ProgressDialogStyle);
        loopDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        loopDialog.setMessage(getString(R.string.loading_please_wait));
        loopDialog.setIndeterminate(true);
        loopDialog.setCanceledOnTouchOutside(false);
        loopDialog.setCancelable(false);
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

                    if (rowItem.has("indicator")) {
                        dashbordContainer.addView(renderGraphTitle(rowItem.getJSONObject("indicator").getString("autoCaption")));
                        dashbordContainer.addView(renderIndicator(rowItem.getJSONObject("indicator")));
                    }

                    if (rowItem.has("histogram")) {
                        dashbordContainer.addView(renderGraphTitle(rowItem.getJSONObject("histogram").getString("autoCaption")));
                        dashbordContainer.addView(renderHistogramOrSpline(rowItem.getJSONObject("histogram"), false));
                    }

                    if (rowItem.has("spline")) {
                        dashbordContainer.addView(renderGraphTitle(rowItem.getJSONObject("spline").getString("autoCaption")));
                        dashbordContainer.addView(renderHistogramOrSpline(rowItem.getJSONObject("spline"), true));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private View renderHistogramOrSpline(JSONObject graphObj, boolean isSpine) throws Exception {
        Resources resources = getContext().getResources();
        int chartHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 300, resources.getDisplayMetrics());
        RelativeLayout chartContainer = new RelativeLayout(getContext());
        chartContainer.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, chartHeight));

        final JSONArray ticks = graphObj.getJSONArray("tick");

        List<Entry> entries = new ArrayList<>();
        List<BarEntry> barEntries = new ArrayList<>();
        final List<String> xValues = new ArrayList<>();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat xDateFormat = new SimpleDateFormat("dd-MM", Locale.getDefault());

        for (int k = 0; k < ticks.length(); k++) {
            JSONObject tick = ticks.getJSONObject(k);
            Date date = sdf.parse(tick.getString("DT"));

            entries.add(new Entry(k, (float) tick.getDouble("value")));
            BarEntry barEntry = new BarEntry((float) k, (float) tick.getDouble("value"));
            barEntries.add(barEntry);
            xValues.add(xDateFormat.format(date));
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


        if (isSpine) {
            LineChart lineChart = new LineChart(getContext());
            lineChart.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));

            LineDataSet dataSet = new LineDataSet(entries, "");
            dataSet.setColor(Color.RED);
            dataSet.setValueTextColor(Color.BLACK);
            dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
            dataSet.setLineWidth(1f);

            LineData lineData = new LineData(dataSet);

            lineChart.setData(lineData);
            lineChart.getLegend().setEnabled(false);
            lineChart.getDescription().setEnabled(false);
            lineChart.setTouchEnabled(true);
            lineChart.setScaleYEnabled(true);
            lineChart.setScaleXEnabled(false);
            setupAxis(lineChart.getXAxis(), xValues);

            lineChart.invalidate(); // refresh

            lineChart.getAxisLeft().resetAxisMinimum();
            lineChart.getAxisLeft().setAxisMaximum(lineChart.getAxisLeft().getAxisMaximum() * 1.2f);

            chartContainer.addView(lineChart);
        } else {
            BarChart barChart = new BarChart(getContext());
            barChart.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));

            BarDataSet set = new BarDataSet(barEntries, "BarDataSet");

            BarData barData = new BarData(set);
            barData.setDrawValues(true);
            barData.setBarWidth(0.9f);

            barChart.setData(barData);
            barChart.setFitBars(true);
            barChart.getLegend().setEnabled(false);
            barChart.getDescription().setEnabled(false);
            barChart.setScaleYEnabled(true);
            setupAxis(barChart.getXAxis(), xValues);

            barChart.invalidate();
            chartContainer.addView(barChart);
        }
        return chartContainer;
    }

    private void setupAxis(XAxis xAxis, final List<String> xValues) {
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        //xAxis.setLabelRotationAngle(90);
        xAxis.setDrawLabels(true);
        xAxis.setEnabled(true);
        xAxis.setSpaceMin(2.0f);
        xAxis.setSpaceMax(2.0f);
        xAxis.setTextColor(Color.BLUE);

        xAxis.setValueFormatter(new IAxisValueFormatter() {

            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                String stringValue;
                if (xValues.size() >= 0 && value >= 0 && (((int) value == value))) {
                    if (value < xValues.size()) {
                        stringValue = xValues.get((int) value);
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
        return titleView;
    }

    private void setupHeader() {
        ((TextView) getActivity().findViewById(R.id.title)).setText(getString(R.string.analystics));
        getActivity().findViewById(R.id.back_btn).setVisibility(View.VISIBLE);
        getActivity().findViewById(R.id.back_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });

        getActivity().findViewById(R.id.grid_btn).setVisibility(View.GONE);
        getActivity().findViewById(R.id.sandwich_btn).setVisibility(View.GONE);
        getActivity().findViewById(R.id.group_by_btn).setVisibility(View.GONE);
        getActivity().findViewById(R.id.search_btn).setVisibility(View.GONE);
    }

    private void showPage(int pageI) {
        try {
            JSONObject page = panelJson.getJSONArray("items").getJSONObject(pageI).getJSONObject("page");
            ((TextView) rootView.findViewById(R.id.page_name)).setText(page.getString("caption"));

            for (int i = 0; i < ((LinearLayout) rootView.findViewById(R.id.page_container)).getChildCount(); i++)
                if (pageI == i) {
                    ((LinearLayout) rootView.findViewById(R.id.page_container)).getChildAt(i).setBackgroundColor(Color.parseColor("#ff322452"));
                    ((LinearLayout) rootView.findViewById(R.id.page_container)).getChildAt(i).setAlpha(1);
                } else {
                    ((LinearLayout) rootView.findViewById(R.id.page_container)).getChildAt(i).setBackgroundColor(Color.TRANSPARENT);
                    ((LinearLayout) rootView.findViewById(R.id.page_container)).getChildAt(i).setAlpha(0.83f);
                }

            renderPageLayout(page);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addPages() {
        try {
            ((FrameLayout.LayoutParams) rootView.findViewById(R.id.page_select_layout).getLayoutParams()).leftMargin = 0;
            rootView.findViewById(R.id.page_select_layout).requestLayout();

            JSONArray pages = panelJson.getJSONArray("items");
            for (int i = 0; i < pages.length(); i++) {
                JSONObject page = pages.getJSONObject(i).getJSONObject("page");

                LinearLayout pageContainer = (LinearLayout) rootView.findViewById(R.id.page_container);
                TextView cardPage = (TextView) getActivity().getLayoutInflater().inflate(R.layout.card_page, pageContainer, false);
                cardPage.setText(page.getString("caption"));
                cardPage.setMaxLines(1);
                //cardPage.setFocusable(true);
                //cardPage.setClickable(true);
                cardPage.setEllipsize(TextUtils.TruncateAt.END);

                pageContainer.addView(cardPage);
                final int finalI = i;
                cardPage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showPage(finalI);
                        rootView.findViewById(R.id.page_select_layout).setVisibility(View.GONE);
                    }
                });
            }
        } catch (Exception exc) {
            exc.printStackTrace();
        }
    }

    private void downloadGraphData() {
        try {
            JSONArray pages = panelJson.getJSONArray("items");
            for (int i = 0; i < pages.length(); i++) {
                JSONObject page = pages.getJSONObject(i).getJSONObject("page");
                JSONArray pageItems = page.getJSONArray("items");
                for (int j = 0; j < pageItems.length(); j++) {
                    JSONObject row = pageItems.getJSONObject(j).getJSONObject("row");
                    JSONArray rowItems = row.getJSONArray("items");
                    for (int k = 0; k < rowItems.length(); k++) {
                        JSONObject rowItem = rowItems.getJSONObject(k);
                        JSONObject graphObj;

                        if (rowItem.has("indicator"))
                            graphObj = rowItem.getJSONObject("indicator");
                        else if (rowItem.has("spline"))
                            graphObj = rowItem.getJSONObject("spline");
                        else
                            graphObj = rowItem.getJSONObject("histogram");

                        if (graphObj.has("dataUrl")) {
                            OkHttpClient client = new OkHttpClient();

                            String url = getIndicatorDataHost() + graphObj.getString("dataUrl");
                            if (!rowItem.has("indicator")) {
                                Calendar past = Calendar.getInstance();
                                past.setTime(new Date());
                                past.add(Calendar.YEAR, -5);

                                Calendar before = Calendar.getInstance();
                                before.setTime(new Date());
                                before.add(Calendar.YEAR, 5);

                                url += "&Past=" + isoDateFormatNoTimeZone.format(past.getTime()) +
                                        "&Before=" + isoDateFormatNoTimeZone.format(before.getTime()) +
                                        "&limit=1000";
                            }

                            Request.Builder requestBuilder = new Request.Builder()
                                    .url(url)
                                    .header("Authorization", "Basic YWRtaW46S0FESTdhc3VmandrbGVuaGtsOA==")
                                    .header("Accept", "application/json");

                            Response response = client.newCall(requestBuilder.build()).execute();
                            String responseStr = response.body().string();
                            JSONObject jsonObject = new JSONObject(responseStr);


                            Object dataSet = jsonObject.getJSONObject("data").get("dataSet");
                            JSONObject timeSeries;

                            if (dataSet instanceof JSONArray)
                                timeSeries = ((JSONArray) dataSet).getJSONObject(0).getJSONObject("timeSeries");
                            else
                                timeSeries = ((JSONObject) dataSet).getJSONObject("timeSeries");

                            if (rowItem.has("indicator")) {
                                graphObj.put("value", timeSeries.getJSONObject("tick").getInt("value"));
                            } else {
                                JSONArray tick = new JSONArray();
                                Object obj = timeSeries.get("tick");
                                if (obj instanceof JSONArray)
                                    tick = (JSONArray) obj;
                                else {
                                    tick.put(obj);
                                }
                                graphObj.put("tick", tick);
                            }

                        } else {
                            graphObj.put("value", -11112222);
                            graphObj.put("tick", new JSONArray());
                        }
                    }
                }
            }

        } catch (Exception exc) {
            exc.printStackTrace();
        }
    }

    private String getIndicatorDataHost() {
        String host;
        switch (BuildConfig.FLAVOR) {
            case "release_flavor":
                host = "https://servlet.dss.mojo.mojoform.com/services";
                break;

            case "debug_flavor":
                host = "https://servlet.dss.dev-alex.org/services";
                break;

            default:
                host = "https://servlet.dss.mojo.mojoform.com/services";
                break;
        }
        host += "/mojo_datastore.HTTPEndpoint";
        return host;
    }

    public class GetPanelTask extends AsyncTask<Void, Void, Integer> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            loopDialog.show();
        }

        @Override
        protected Integer doInBackground(Void... params) {
            try {
                Response response = RequestService.createGetRequest("/api/fs-analytic/get/" + panel.panel_id);
                String responseStr = response.body().string();
                panelJson = new JSONObject(responseStr);
                downloadGraphData();
                return response.code();
            } catch (Exception exc) {
                exc.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(Integer responseCode) {
            super.onPostExecute(responseCode);
            try {
                if (loopDialog != null && loopDialog.isShowing())
                    loopDialog.dismiss();

                addPages();
                showPage(0);

            } catch (Exception exc) {
                exc.printStackTrace();
            }
        }
    }
}
