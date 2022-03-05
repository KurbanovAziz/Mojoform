package org.dev_alex.mojo_qa.mojo.fragments;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.fasterxml.jackson.core.type.TypeReference;
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
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;

import org.dev_alex.mojo_qa.mojo.R;
import org.dev_alex.mojo_qa.mojo.activities.MainActivity;
import org.dev_alex.mojo_qa.mojo.adapters.CommentAdapter;
import org.dev_alex.mojo_qa.mojo.adapters.ComplexGraphAdapter;
import org.dev_alex.mojo_qa.mojo.adapters.IndicatorGraphAdapter;
import org.dev_alex.mojo_qa.mojo.adapters.ResultGraphAdapter;
import org.dev_alex.mojo_qa.mojo.models.Employee;
import org.dev_alex.mojo_qa.mojo.models.GraphInfo;
import org.dev_alex.mojo_qa.mojo.models.Indicator;
import org.dev_alex.mojo_qa.mojo.models.Notification;
import org.dev_alex.mojo_qa.mojo.models.Panel;
import org.dev_alex.mojo_qa.mojo.models.Ranges;
import org.dev_alex.mojo_qa.mojo.models.User;
import org.dev_alex.mojo_qa.mojo.models.Value;
import org.dev_alex.mojo_qa.mojo.services.RequestService;
import org.dev_alex.mojo_qa.mojo.services.Utils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

import okhttp3.Response;
import okio.BufferedSink;
import okio.Okio;

public class GraphFragment extends Fragment implements ResultGraphAdapter.GraphClickListener{
    public static final String DAY = "day";
    public static final String WEEK = "week";
    public static final String MONTH = "month";
    public static final String YEAR = "year";
    private RecyclerView recyclerView;
    boolean isComplex;
    private static final String TYPE_ARG = "type";
    private static final String ID_ARG = "panel_id";
    private static final String IS_PERCENTS_ARG = "is_percents";
    public static String name;

    private View rootView;
    private String type;
    private long panelId;
    private boolean isPercents;
    private GraphInfo graphInfo;
    public List<Panel> panels = new ArrayList<>();
    public List<Indicator> indicators = new ArrayList<>();
    public static List<Employee> users = new ArrayList<>();
    private ProgressDialog loopDialog;





    public static List<Ranges> ranges;
    LinearLayout chartContainer;
    ViewGroup container;
    ImageView sendBTN;
    EditText messageET;
    String message;
    long vid;
    long from;
    long to;
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
        initDialog();
        type = getArguments().getString(TYPE_ARG);
        panelId = getArguments().getLong(ID_ARG);
        isPercents = getArguments().getBoolean(IS_PERCENTS_ARG);
        this.container = container;




        if (rootView == null) {
            rootView = inflater.inflate(R.layout.fragment_graph, container, false);

            setListeners();
            Utils.setupCloseKeyboardUI(getActivity(), rootView);
            if(GraphListFragment.isIndicatorShow){
                
            }
            else {
            new GetGraphTask().execute();}
        }
        chartContainer = rootView.findViewById(R.id.recycler_container);
        chartContainer.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        recyclerView = (RecyclerView) getActivity().getLayoutInflater().inflate(R.layout.recycle_graph, container, false);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        chartContainer.addView(recyclerView);
        return rootView;
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (rootView != null) {
            ViewGroup parentViewGroup = (ViewGroup) rootView.getParent();
            if (parentViewGroup != null) {
                parentViewGroup.removeAllViews();
            }
        }
    }

    private void setListeners() {

    }
    private void showComplexPanels(List<Panel> panels) {
        List<Panel> syntheticPanels = new ArrayList<>();
        List<Panel> normalPanels = new ArrayList<>();

        for (Panel panel : panels) {
            if (panel.type.equals("complex"))
                syntheticPanels.add(panel);
            else
                normalPanels.add(panel);
        }

        Collections.sort(syntheticPanels, (o1, o2) -> o1.name.compareToIgnoreCase(o2.name));
        Collections.sort(normalPanels, (o1, o2) -> o1.name.compareToIgnoreCase(o2.name));

        panels.clear();
        panels.addAll(syntheticPanels);
        panels.add(Panel.getSeparatorPanel());
        panels.addAll(normalPanels);
        Panel panel = new Panel();
        panels.add(0, panel);

        recyclerView.setAdapter(new ComplexGraphAdapter(panels, new ComplexGraphAdapter.OnPanelClickListener() {
            @Override
            public void onClick(Panel panel) {
                onPanelClick(panel);
            }
        }));

    }

    private void showResultPanels(List<Panel> panels) {



        Indicator indicator = new Indicator();
        indicators.add(0, indicator);

        recyclerView.setAdapter(new ResultGraphAdapter(indicators, this, users));

    }

    private void onPanelClick(Panel panel) {
        getActivity()
                .getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container, GraphListFragment.newInstance(panel))
                .addToBackStack(null)
                .commit();
    }
    private void initDialog() {
        loopDialog = new ProgressDialog(getContext(), R.style.ProgressDialogStyle);
        loopDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        loopDialog.setMessage(getString(R.string.loading_please_wait));
        loopDialog.setIndeterminate(true);
        loopDialog.setCanceledOnTouchOutside(false);
        loopDialog.setCancelable(false);
    }

    private void buildGraph() {
        try {
            View view = renderHistogram(graphInfo);
            ((ViewGroup) rootView).addView(view);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private View renderHistogram(GraphInfo graphInfo) throws Exception {
        Collections.reverse(graphInfo.values);
        Resources resources = getContext().getResources();
        int chartHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 300, resources.getDisplayMetrics());
        LinearLayout chartContainer = rootView.findViewById(R.id.LLcontainer);
        chartContainer.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, chartHeight));
        float yMin = 0, yMax = 0;
        List<BarEntry> barEntries = new ArrayList<>();
        final List<String> xValues = new ArrayList<>();
        int k = 0;
        for (Value value : graphInfo.values) {
            if (value != null){
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
        }}
        if (barEntries.isEmpty()) {
            barEntries.add(new BarEntry(0, 0));
   Toast.makeText(getContext(), "На графике нет значений за этот период", Toast.LENGTH_LONG).show();}
        ArrayList<Integer> colors = new ArrayList<>();
        for (k = 0; k < barEntries.size(); k++) {
            float value = barEntries.get(k).getY();
            int defaultColor = Color.parseColor("#42aaff");

            if(ranges != null && ranges.size() != 0){
            for (Ranges range : ranges) {
                if (value >= range.from && value <= range.to){
                    try {
                    String colorString = "#AA" + range.color.substring(1);
                    defaultColor = Color.parseColor(colorString);}
                    catch (Exception ignored){}
                }
            }
            }
            else{
                try {
                defaultColor = Color.parseColor("#ff0000");
                if(value < 86)  defaultColor = Color.parseColor("#ff0000");
                if (value > 85 && value < 96){  defaultColor = Color.parseColor("#eaff00");}
                if (value > 95 && value < 101){  defaultColor = Color.parseColor("#15ff00");}
                if (value > 100){defaultColor = Color.parseColor("#42aaff");}}
                catch (Exception e ){
                    e.printStackTrace();
                }
            }
            colors.add(defaultColor);
        }
        BarChart barChart = new BarChart(getContext());
        barChart.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, chartHeight));
        barChart.setDrawValueAboveBar(false);
        BarDataSet set = new BarDataSet(barEntries, "BarDataSet");
        set.setColors(colors);
        BarData barData = new BarData(set);
        barData.setDrawValues(false);
        barData.setBarWidth(0.8f);
        //barChart.getAxisLeft().setAxisMaximum(barChart.getAxisLeft().getAxisMaximum() * 1.2f);
        barChart.getAxisRight().setEnabled(false);
        barChart.getAxisLeft().setGridColor(Color.parseColor("#374E3F60"));
        barChart.getAxisLeft().setAxisLineColor(Color.parseColor("#374E3F60"));
        barChart.getXAxis().setGridColor(Color.parseColor("#374E3F60"));
        barChart.getXAxis().setAxisLineColor(Color.parseColor("#374E3F60"));
        barChart.setBackgroundColor(Color.parseColor("#fffafa"));
        barChart.setData(barData);
        barChart.setFitBars(false);
        barChart.getLegend().setEnabled(false);
        barChart.getDescription().setEnabled(false);
        barChart.setScaleYEnabled(true);
        setupAxis(barChart.getXAxis(), xValues);
        Date date = new Date();
        barChart.invalidate();
        barChart.zoom(barEntries.size() / 10, 0.9f, 0, 0);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams
                (ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
        CustomMarkerView mv = new CustomMarkerView(getContext(), R.layout.higlight_marker, xValues, 0 , xValues.size() - 1, yMin, yMax);
        //barChart.setMarker(mv);
        chartContainer.addView(barChart);
        barChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener()
        {
            @Override
            public void onValueSelected(Entry e, Highlight h)
            {
                try {
                    if(!isComplex){
                    from = graphInfo.values.get((int) h.getX()).from;
                    to = graphInfo.values.get((int) h.getX()).to;
                    new GetRaw().execute();}
                    final Dialog graphDialog = new Dialog(getContext());
                    graphDialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
                    graphDialog.setContentView(LayoutInflater.from(getContext()).inflate(R.layout.graph_dialog, null, false));
                    graphDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                    graphDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
                    String graphFrom = dateFormat.format(graphInfo.values.get((int) h.getX()).from);
                    String graphTo = dateFormat.format(graphInfo.values.get((int) h.getX()).to);
                    String date = "от " + graphFrom + " до " + graphTo;
                    final TextView dateTV = graphDialog.findViewById(R.id.graph_date);
                    dateTV.setText(date);
                    final TextView kolvoTV = graphDialog.findViewById(R.id.kolvo);
                    kolvoTV.setText("Количество комментариев: " + graphInfo.values.get((int) h.getX()).comments.size());


                    final TextView textView = graphDialog.findViewById(R.id.information);
                    final Button commentBTN = graphDialog.findViewById(R.id.commentsBTN);
                    commentBTN.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if(!isComplex){
                                from = graphInfo.values.get((int) h.getX()).from;
                                to = graphInfo.values.get((int) h.getX()).to;
                                new GetRaw().execute();}
                            final Dialog commentsDialog = new Dialog(getContext());

                            commentsDialog.setContentView(LayoutInflater.from(getContext()).inflate(R.layout.comments_dialog, null, false));
                            commentsDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                            commentsDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

                            ImageView kresticIV = commentsDialog.findViewById(R.id.krestic);
                            kresticIV.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    commentsDialog.cancel();
                                    graphDialog.cancel();
                                }
                            });
                            RecyclerView recyclerComment = commentsDialog.findViewById(R.id.recycler_view);
                            recyclerComment.setLayoutManager(new LinearLayoutManager(getContext()));
                            recyclerComment.setAdapter(new CommentAdapter(getActivity(), graphInfo.values.get((int) h.getX()).comments));
                            sendBTN = (ImageView) commentsDialog.findViewById(R.id.send);
                            messageET = (EditText) commentsDialog.findViewById(R.id.et_text_message);
                            messageET.addTextChangedListener(new TextWatcher() {
                                @Override
                                public void onTextChanged(CharSequence s, int start, int before, int count) {
                                    sendBTN.setVisibility(View.VISIBLE);
                                }

                                @Override
                                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                                }
                                @Override
                                public void afterTextChanged(Editable s) {

                                }
                            });
                            TextView labelTV = commentsDialog.findViewById(R.id.label);
                            TextView timeTV = commentsDialog.findViewById(R.id.time);
                            labelTV.setText(name);
                            SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy | HH:mm", Locale.getDefault());
                            String time = dateFormat.format(graphInfo.values.get((int) h.getX()).from * 1000);
                            timeTV.setText(time);

                            sendBTN.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    message = messageET.getText().toString();
                                    vid = graphInfo.values.get((int) h.getX()).id;
                                    new SendComment().execute();
                                    commentsDialog.cancel();
                                    graphDialog.cancel();
                                }
                            });
                            commentsDialog.show();
                        }
                    });
                    textView.setText("Баллы " + new BigDecimal(graphInfo.values.get((int) h.getX()).val).setScale(2, RoundingMode.HALF_EVEN).doubleValue()  +  " | Проценты " +  new BigDecimal(graphInfo.values.get((int) h.getX()).prc).setScale(2, RoundingMode.HALF_EVEN).doubleValue() + "%");
                    graphDialog.show();
                }

                catch (Exception exc){}
            }

            @Override
            public void onNothingSelected()
            {

            }
        });


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
                            } }}
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
        }    }
    public boolean isPanelOk(Panel panel){
        Date date = new Date();
        switch (type){
            case (YEAR):
                long from = panel.from * 1000;
                long to = panel.to *1000;
                long limit1 = date.getTime() - Long.parseLong("73072000000");
                return to < date.getTime() && from > limit1;
            case (MONTH):
                long from2 = panel.from * 1000;
                long to2 = panel.to *1000;
                long limit2 = date.getTime() - Long.parseLong("10713600000");
                return to2 < date.getTime() && from2 > limit2;
            case (WEEK):
                long from3 = panel.from * 1000;
                long to3 = panel.to *1000;
                long date3 = date.getTime();
                long limit3 = date.getTime() - Long.parseLong("3024000000");
                return to3 < date.getTime() && from3 > limit3;
            case(DAY):
                long from4 = panel.from * 1000;
                long to4 = panel.to *1000;
                long date4 = date.getTime();
                long limit4 = date.getTime() - Long.parseLong("691200000");
                return to4 < date.getTime() && from4 > limit4;

        }
        return false;

    }
    public boolean isValueOk(Value panel){
        Date date = new Date();
        if(panel == null) return false;

        switch (type){
            case (YEAR):
                long from = panel.from;
                long to = panel.to;
                if(panel.from == 0 && panel.to == 0) return false;

                long limit1 = date.getTime() - Long.parseLong("63072000000");
                return to < date.getTime() && from > limit1;
            case (MONTH):
                long from2 = panel.from;
                long to2 = panel.to ;
                if(panel.from == 0 && panel.to == 0) return false;

                long limit2 = date.getTime() - Long.parseLong("10713600000");
                return to2 < date.getTime() && from2 > limit2;
            case (WEEK):
                long from3 = panel.from ;
                long to3 = panel.to ;
                if(panel.from == 0 && panel.to == 0) return false;

                long limit3 = date.getTime() - Long.parseLong("3024000000");
                return to3 < date.getTime() && from3 > limit3;
            case(DAY):
                long from4 = panel.from ;
                long to4 = panel.to ;
                if(panel.from == 0 && panel.to == 0) return false;
                long limit4 = date.getTime() - Long.parseLong("691200000");
                return to4 < date.getTime() && from4 > limit4;

        }
        return false;

    }


    public long getTimeLimit(){
        Date date = new Date();
        switch (type){
            case (YEAR):
                return date.getTime() - Long.parseLong("63072000000");
            case (MONTH):
                return date.getTime() - Long.parseLong("10713600000");
            case (WEEK):
                return date.getTime() - Long.parseLong("3024000000");
            case(DAY):
                return date.getTime() - Long.parseLong("691200000");

        }
        return date.getTime();

    }



    private int dpToPx(int dp) {
        Resources resources = getContext().getResources();
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.getDisplayMetrics());
    }

    public class GetGraphTask extends AsyncTask<Void, Void, Integer> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            loopDialog.show();

        }

        @Override
        protected Integer doInBackground(Void... params) {
            try {

                Response response = RequestService.createGetRequest("/api/analytic/graph/" + panelId + "/" + type);
                String responseStr = response.body().string();
                Response findResponse = RequestService.createGetRequest("/api/analytic/find/" + panelId);
                String findResponseStr = findResponse.body().string();

                graphInfo = new ObjectMapper().readValue(responseStr, GraphInfo.class);
                graphInfo.fixDates();
                try {
                    String configStr = new JSONObject(findResponseStr).getString("config");
                    JSONArray configJsonArray = new JSONObject(configStr).getJSONArray("ranges");
                    ArrayList<Ranges> ranges1  = new ObjectMapper().readValue(configJsonArray.toString(), new TypeReference<ArrayList<Ranges>>() {});
                    ranges = ranges1;
                }
                catch (Exception e) {
                    ranges = new ArrayList<>();
                }
                JSONObject jsonObjectComplex = new JSONObject(responseStr);
                if(jsonObjectComplex.has("complex_info")){
                JSONArray panelsJsonComplex = jsonObjectComplex.getJSONObject("complex_info").getJSONArray("components");

                ArrayList<Panel> panels1  = new ObjectMapper().readValue(panelsJsonComplex.toString(), new TypeReference<ArrayList<Panel>>() {});
                panels = panels1;
                isComplex = true;
                for (Panel panel : panels){
                    panel.prc =  new BigDecimal(panel.prc).setScale(2, RoundingMode.HALF_EVEN).doubleValue();;
                    panel.fixDate(); }}
                else {
                    JSONArray panelsJsonComplex = jsonObjectComplex.getJSONArray("values");
                    String resultName = jsonObjectComplex.getJSONObject("template_info").getJSONObject("template").getString("name");
                    ArrayList<Panel> panels1  = new ObjectMapper().readValue(panelsJsonComplex.toString(), new TypeReference<ArrayList<Panel>>() {});

                    ArrayList<Indicator>  indicators1  = new ArrayList<Indicator>();

                    isComplex = false;
                    name = resultName;
                    panels = new ArrayList<Panel>();
                    for (Indicator indicator : indicators1){
                        indicator.name = resultName;
                        indicator.prc =  new BigDecimal(indicator.prc).setScale(2, RoundingMode.HALF_EVEN).doubleValue();
                        indicators.add(indicator);
                    }

                    for (Panel panel : panels1){
                        panel.fixDate();
                        panel.prc = new BigDecimal(panel.prc).setScale(2, RoundingMode.HALF_EVEN).doubleValue();
                        panel.name = resultName;
                        panels.add(panel);
                   }
                }


                return response.code();
            } catch (Exception exc) {
                StringWriter writer = new StringWriter();
                exc.printStackTrace( new PrintWriter(writer,true ));
                Log.e("aaa", "exeption stack is :\n" + writer.toString());
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


            if (responseCode == 200 || responseCode == 201){
                buildGraph();
                if(isComplex){
            showComplexPanels(panels);}
            else {showResultPanels(panels);}}

            else {
                Toast.makeText(getContext(), R.string.try_later, Toast.LENGTH_SHORT).show();
                getActivity().getSupportFragmentManager().popBackStack();
            }
        }
            catch (Exception e){}}
    }
    public class GetRaw extends AsyncTask<Void, Void, Integer> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            loopDialog.show();

        }

        @Override
        protected Integer doInBackground(Void... params) {
            try {
                    Response indicatorResponse = RequestService.createGetRequestWithQuery("/api/analytic/raw/" + panelId, from / 1000, to / 1000);
                    String indicatorResponseStr = indicatorResponse.body().string();
                    JSONObject jsonObjectIndicator= new JSONObject(indicatorResponseStr);
                    JSONArray indicatorsJsonArray = jsonObjectIndicator.getJSONArray("datas");
                    ArrayList<Indicator>  indicators1  = new ObjectMapper().readValue(indicatorsJsonArray.toString(), new TypeReference<ArrayList<Indicator>>() {});
                    JSONArray usersJsonArray = jsonObjectIndicator.getJSONArray("users");
                    ArrayList<Employee>  users1  = new ObjectMapper().readValue(usersJsonArray.toString(), new TypeReference<ArrayList<Employee>>() {});
                    if (users1 != null){
                    users = users1;}
                    indicators.clear();
                    indicators.add(new Indicator());
                for (Indicator indicator : indicators1){
                    indicator.name = name;
                    indicator.prc =  new BigDecimal(indicator.prc).setScale(2, RoundingMode.HALF_EVEN).doubleValue();
                    indicators.add(indicator);
                }
                    return indicatorResponse.code();
            } catch (Exception exc) {
                StringWriter writer = new StringWriter();
                exc.printStackTrace( new PrintWriter(writer,true ));
                Log.e("aaa", "exeption stack is :\n" + writer.toString());
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

            if (responseCode == 200 || responseCode == 201){
                Objects.requireNonNull(recyclerView.getAdapter()).notifyDataSetChanged();

            }

            else {
                Toast.makeText(getContext(), R.string.try_later, Toast.LENGTH_SHORT).show();
                getActivity().getSupportFragmentManager().popBackStack();
            }}
            catch (Exception e){}
        }
    }
    @Override
    public void onDownloadPdfClick(Indicator indicator) {
        if (checkExternalPermissions()) {
            new DownloadPdfTask(indicator.id, UUID.randomUUID().toString()).execute();
        } else {
            requestExternalPermissions();
        }
    }

    @Override
    public void onDownloadDocClick(Indicator indicator) {
        if (checkExternalPermissions()) {
            new DownloadDocTask(indicator.id, UUID.randomUUID().toString()).execute();
        } else {
            requestExternalPermissions();
        }
    }
    private class DownloadPdfTask extends AsyncTask<Void, Void, Integer> {
        private java.io.File resultFile;
        private long id;
        private String name;

        DownloadPdfTask(long id, String name) {
            this.id = id;
            this.name = name;}

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            loopDialog.show();
            File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            resultFile = new File(downloadsDir, name + ".pdf");
        }

        @Override
        protected Integer doInBackground(Void... params) {
            try {
                if (resultFile.exists())
                    return 200;

                String url = "/api/fs-mojo/document/id/" + id + "/pdf";
                Response response = RequestService.createGetRequest(url);

                if (response.code() == 200) {
                    BufferedSink sink = Okio.buffer(Okio.sink(resultFile));
                    sink.writeAll(response.body().source());
                    sink.close();
                }
                response.body().close();

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


                if (responseCode == null)
                    Toast.makeText(getContext(), R.string.network_error, Toast.LENGTH_LONG).show();
                else if (responseCode == 200) {
                    try {
                        Intent viewIntent = new Intent(Intent.ACTION_VIEW);
                        Uri fileUri = FileProvider.getUriForFile(getContext(), getContext().getApplicationContext().getPackageName() + ".provider", resultFile);
                        viewIntent.setDataAndType(fileUri, "application/pdf");
                        viewIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        startActivity(viewIntent);
                    } catch (Exception exc) {
                        exc.printStackTrace();
                        Toast.makeText(getContext(), "Нет приложения, которое может открыть этот файл", Toast.LENGTH_LONG).show();
                        try {
                            resultFile.delete();
                        } catch (Exception exc1) {
                            exc1.printStackTrace();
                        }
                    }
                } else
                    Toast.makeText(getContext(), R.string.unknown_error, Toast.LENGTH_LONG).show();
            } catch (Exception exc) {
                exc.printStackTrace();
            }
        }
    }
    private class DownloadDocTask extends AsyncTask<Void, Void, Integer> {
        private java.io.File resultFile;
        private long id;
        private String name;

        DownloadDocTask(long id, String name) {
            this.id = id;
            this.name = name;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            loopDialog.show();
            File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            resultFile = new File(downloadsDir, name + ".docx");
        }

        @Override
        protected Integer doInBackground(Void... params) {
            try {
                if (resultFile.exists())
                    return 200;

                String url = "/api/fs-mojo/document/id/" + id + "/docx";
                Response response = RequestService.createGetRequest(url);

                if (response.code() == 200) {
                    BufferedSink sink = Okio.buffer(Okio.sink(resultFile));
                    sink.writeAll(response.body().source());
                    sink.close();
                }
                response.body().close();

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
                if (responseCode == null)
                    Toast.makeText(getContext(), R.string.network_error, Toast.LENGTH_LONG).show();
                else if (responseCode == 200) {
                    try {
                        Intent viewIntent = new Intent(Intent.ACTION_VIEW);
                        Uri fileUri = FileProvider.getUriForFile(getContext(), getContext().getApplicationContext().getPackageName() + ".provider", resultFile);
                        viewIntent.setDataAndType(fileUri, "application/msword");
                        viewIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        startActivity(viewIntent);
                    } catch (Exception exc) {
                        exc.printStackTrace();
                        Toast.makeText(getContext(), "Нет приложения, которое может открыть этот файл", Toast.LENGTH_LONG).show();
                        try {
                            resultFile.delete();
                        } catch (Exception exc1) {
                            exc1.printStackTrace();
                        }
                    }
                } else
                    Toast.makeText(getContext(), R.string.unknown_error, Toast.LENGTH_LONG).show();
            } catch (Exception exc) {
                exc.printStackTrace();
            }
        }
    }
    public class SendComment extends AsyncTask<Void, Void, Integer> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            loopDialog.show();
        }

        @Override
        protected Integer doInBackground(Void... params) {
            try {
                if (message != null) {
                    Response response = RequestService.createPostRequest(String.format("/api/analytic/comment/%s/%s", panelId, vid), message);
                    return response.code();
                }
                return null;
            } catch (Exception exc) {
                StringWriter writer = new StringWriter();
                exc.printStackTrace(new PrintWriter(writer, true));
                Log.e("aaa", "exeption stack is :\n" + writer.toString());
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
            if (responseCode == 200 || responseCode == 201){
                Toast.makeText(getContext(), "Сообщение отправлено", Toast.LENGTH_SHORT).show();
GraphListFragment.restartGraphFragment(panelId, isPercents);
            }
            else {
                Toast.makeText(getContext(), R.string.try_later, Toast.LENGTH_SHORT).show();
            }
        }
        catch (Exception e){}}
    }

    private boolean checkExternalPermissions() {
        int permissionCheckWrite = ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int permissionCheckRead = ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE);
        return (permissionCheckRead == PackageManager.PERMISSION_GRANTED && permissionCheckWrite == PackageManager.PERMISSION_GRANTED);
    }

    private void requestExternalPermissions() {
        ActivityCompat.requestPermissions(getActivity(),
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
    }

}
