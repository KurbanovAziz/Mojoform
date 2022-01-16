package org.dev_alex.mojo_qa.mojo.fragments;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.util.Range;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
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

import org.dev_alex.mojo_qa.mojo.R;
import org.dev_alex.mojo_qa.mojo.adapters.ComplexGraphAdapter;
import org.dev_alex.mojo_qa.mojo.adapters.ResultGraphAdapter;
import org.dev_alex.mojo_qa.mojo.models.GraphInfo;
import org.dev_alex.mojo_qa.mojo.models.IndicatorModel;
import org.dev_alex.mojo_qa.mojo.models.Organisation;
import org.dev_alex.mojo_qa.mojo.models.Panel;
import org.dev_alex.mojo_qa.mojo.models.Ranges;
import org.dev_alex.mojo_qa.mojo.models.Value;
import org.dev_alex.mojo_qa.mojo.services.RequestService;
import org.dev_alex.mojo_qa.mojo.services.Utils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import okhttp3.Response;

public class GraphFragment extends Fragment {
    public static final String DAY = "day";
    public static final String WEEK = "week";
    public static final String MONTH = "month";
    public static final String YEAR = "year";
    private RecyclerView recyclerView;


    private static final String TYPE_ARG = "type";
    private static final String ID_ARG = "panel_id";
    private static final String IS_PERCENTS_ARG = "is_percents";

    private View rootView;
    private String type;
    private long panelId;
    private boolean isPercents;
    private GraphInfo graphInfo;
    public List<Panel> panels = new ArrayList<>();
    public List<Ranges> ranges;

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
        recyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
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



        Panel panel = new Panel();
        panels.add(0, panel);

        recyclerView.setAdapter(new ResultGraphAdapter(panels, new ResultGraphAdapter.OnPanelClickListener() {
            @Override
            public void onClick(Panel panel) {
                onPanelClick(panel);
            }
        }));

    }
    private void onPanelClick(Panel panel) {
        getActivity()
                .getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container, GraphListFragment.newInstance(panel))
                .addToBackStack(null)
                .commit();
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
        Resources resources = getContext().getResources();
        int chartHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 300, resources.getDisplayMetrics());
        LinearLayout chartContainer = rootView.findViewById(R.id.LLcontainer);
        chartContainer.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, chartHeight));

        float yMin = 0, yMax = 0;
        List<BarEntry> barEntries = new ArrayList<>();
        final List<String> xValues = new ArrayList<>();

        int k = 0;
        for (Value value : graphInfo.values) {
            if (isValueOk(value)){
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

   Toast.makeText(getContext(), "На графике нет значений за этот период", Toast.LENGTH_LONG).show();
        }

        ArrayList<Integer> colors = new ArrayList<>();
        /*    if (graphInfo.has("ranges")) {
            ranges = new ObjectMapper()
                    .readValue(graphObj.getJSONArray("ranges").toString(), new TypeReference<ArrayList<IndicatorModel.Range>>() {
                    });
        }
        */
        for (k = 0; k < barEntries.size(); k++) {
            float value = barEntries.get(k).getY();
            int defaultColor = Color.parseColor("#42aaff");

            if(ranges != null && ranges.size() != 0){
            for (Ranges range : ranges) {
                if (value >= range.from && value <= range.to)
                    defaultColor = Color.parseColor("#AA" + range.color.substring(1));
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
boolean isComplex;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
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
                catch (Exception e){
                    ranges = new ArrayList<>();
                }












                JSONObject jsonObjectComplex = new JSONObject(responseStr);
                if(jsonObjectComplex.has("complex_info")){
                JSONArray panelsJsonComplex = jsonObjectComplex.getJSONObject("complex_info").getJSONArray("components");

                ArrayList<Panel> panels1  = new ObjectMapper().readValue(panelsJsonComplex.toString(), new TypeReference<ArrayList<Panel>>() {});
                panels = panels1;
                isComplex = true;
                for (Panel panel : panels){
                    panel.fixDate(); }}
                else {
                    JSONArray panelsJsonComplex = jsonObjectComplex.getJSONArray("values");
                    String resultName = jsonObjectComplex.getJSONObject("template_info").getJSONObject("template").getString("name");
                    ArrayList<Panel> panels1  = new ObjectMapper().readValue(panelsJsonComplex.toString(), new TypeReference<ArrayList<Panel>>() {});

                    isComplex = false;
                    panels = new ArrayList<Panel>();

                    for (Panel panel : panels1){
                        panel.fixDate();
                        panel.name = resultName;
                        if(isPanelOk(panel)){panels.add(panel); }
                        else{Log.e("lol", "kek");}
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
    }
}
