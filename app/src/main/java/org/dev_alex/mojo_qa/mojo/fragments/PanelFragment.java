package org.dev_alex.mojo_qa.mojo.fragments;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.dev_alex.mojo_qa.mojo.BuildConfig;
import org.dev_alex.mojo_qa.mojo.R;
import org.dev_alex.mojo_qa.mojo.models.Panel;
import org.dev_alex.mojo_qa.mojo.services.RequestService;
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

    private ViewPager viewPager;
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
            viewPager = (ViewPager) rootView.findViewById(R.id.view_pager);

            panel = (Panel) getArguments().getSerializable("panel");

            setupClosePageUI(rootView);
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

    private void showPage(int pageI, boolean scrollViewpager) {
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

            if (scrollViewpager)
                viewPager.setCurrentItem(pageI, true);
            rootView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    setupClosePageUI(rootView);
                }
            }, 150);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addPages() {
        try {
            ((FrameLayout.LayoutParams) rootView.findViewById(R.id.page_select_layout).getLayoutParams()).leftMargin = 0;
            rootView.findViewById(R.id.page_select_layout).requestLayout();

            JSONArray pages = panelJson.getJSONArray("items");
            List<JSONObject> pagesJson = new ArrayList<>();

            for (int i = 0; i < pages.length(); i++) {
                JSONObject page = pages.getJSONObject(i).getJSONObject("page");
                pagesJson.add(page);

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
                        showPage(finalI, true);
                        rootView.findViewById(R.id.page_select_layout).setVisibility(View.GONE);
                    }
                });
            }

            viewPager.setAdapter(new MyPagerAdapter(getFragmentManager(), pagesJson));
            viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                }

                @Override
                public void onPageSelected(int position) {
                    showPage(position, false);
                }

                @Override
                public void onPageScrollStateChanged(int state) {

                }
            });
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


                            Object dataSet;

                            if (jsonObject.has("timeSeries")) {
                                dataSet = jsonObject;
                            } else
                                dataSet = jsonObject.getJSONObject("data").get("dataSet");

                            JSONObject timeSeries;

                            if (dataSet instanceof JSONArray) {
                                String timeSeriesStr = ((JSONArray) dataSet).getJSONObject(0).getString("timeSeries");
                                if (!timeSeriesStr.isEmpty())
                                    timeSeries = ((JSONArray) dataSet).getJSONObject(0).getJSONObject("timeSeries");
                                else {
                                    graphObj.put("value", -11112222);
                                    graphObj.put("tick", new JSONArray());
                                    continue;
                                }
                            } else {
                                String timeSeriesStr = ((JSONObject) dataSet).getString("timeSeries");
                                if (!timeSeriesStr.isEmpty())
                                    timeSeries = ((JSONObject) dataSet).getJSONObject("timeSeries");
                                else {
                                    graphObj.put("value", -11112222);
                                    graphObj.put("tick", new JSONArray());
                                    continue;
                                }
                            }

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

            case "demo_flavor":
                host = "https://servlet.dss.demo.mojoform.com/services";
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
                Response response = RequestService.createGetRequest("/api/fs-analytic/get/" + panel.id);
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

                if (responseCode != null && responseCode == 200) {
                    addPages();
                    showPage(0, true);
                } else
                    Toast.makeText(getContext(), R.string.error_during_load, Toast.LENGTH_LONG).show();

            } catch (Exception exc) {
                exc.printStackTrace();
            }
        }
    }

    public void setupClosePageUI(View rootView) {
        // Set up touch listener for non-text box views to hide keyboard.
        if (rootView.getId() != R.id.page_selector_block) {

            rootView.setOnTouchListener(new View.OnTouchListener() {
                public boolean onTouch(View v, MotionEvent event) {
                    PanelFragment.this.rootView.findViewById(R.id.page_select_layout).setVisibility(View.GONE);
                    return false;
                }
            });
        }

        //If a layout container, iterate over children and seed recursion.
        if (rootView instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) rootView).getChildCount(); i++) {
                View innerView = ((ViewGroup) rootView).getChildAt(i);
                setupClosePageUI(innerView);
            }
        }
    }


    public static class MyPagerAdapter extends FragmentPagerAdapter {
        private static int NUM_ITEMS = 3;
        private List<JSONObject> pages;

        public MyPagerAdapter(FragmentManager fragmentManager, List<JSONObject> pages) {
            super(fragmentManager);
            this.pages = pages;
            NUM_ITEMS = pages.size();
        }

        // Returns total number of pages
        @Override
        public int getCount() {
            return NUM_ITEMS;
        }

        // Returns the fragment to display for that page
        @Override
        public Fragment getItem(int position) {
            return PanelPageFragment.newInstance(pages.get(position));
        }

        @Override
        public long getItemId(int position) {
            return new Date().getTime();
        }
    }
}
