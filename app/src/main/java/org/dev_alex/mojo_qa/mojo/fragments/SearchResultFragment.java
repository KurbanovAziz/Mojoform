package org.dev_alex.mojo_qa.mojo.fragments;

import android.animation.ObjectAnimator;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.dev_alex.mojo_qa.mojo.BuildConfig;
import org.dev_alex.mojo_qa.mojo.R;

import org.dev_alex.mojo_qa.mojo.adapters.PanelAdapter;
import org.dev_alex.mojo_qa.mojo.adapters.SearchFileAdapter;
import org.dev_alex.mojo_qa.mojo.custom_views.MultiSpinner;
import org.dev_alex.mojo_qa.mojo.models.Organisation;
import org.dev_alex.mojo_qa.mojo.models.Panel;
import org.dev_alex.mojo_qa.mojo.services.BitmapCacheService;
import org.dev_alex.mojo_qa.mojo.services.RequestService;
import org.dev_alex.mojo_qa.mojo.services.Utils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okio.BufferedSink;
import okio.Okio;

public class SearchResultFragment extends Fragment {
    private final int FILE_OPEN_REQUEST_CODE = 1;

    private View rootView;
    private RecyclerView recyclerView;
    private ImageView loopDialog;
    private GetPanelTask searchTask = null;


    private SimpleDateFormat isoDateFormatNoTimeZone = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());


    public BitmapCacheService bitmapCacheService;
    private java.io.File openingFile;


    public static SearchResultFragment newInstance() {
        Bundle args = new Bundle();
        SearchResultFragment fragment = new SearchResultFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_serch_result, container, false);
        recyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);

        bitmapCacheService = new BitmapCacheService();
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        Utils.setupCloseKeyboardUI(getActivity(), rootView);
        initDialog();
        setListeners();
        return rootView;
    }

    private void initDialog() {
        loopDialog = rootView.findViewById(R.id.image_progress);

        ObjectAnimator animation = ObjectAnimator.ofFloat(loopDialog, View.ROTATION_Y, 0.0f, 360f);
        animation.setDuration(2400);
        animation.setRepeatCount(ObjectAnimator.INFINITE);
        animation.setInterpolator(new AccelerateDecelerateInterpolator());
        animation.start();
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().findViewById(R.id.main_menu_search_block).setVisibility(View.VISIBLE);
        getActivity().findViewById(R.id.main_menu_buttons_block).setVisibility(View.GONE);
    }

    private void setListeners() {
        getActivity().findViewById(R.id.search_back_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.hideSoftKeyboard(getActivity());
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });
        getActivity().findViewById(R.id.search_reset).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((EditText) getActivity().findViewById(R.id.search_text)).setText("");
                InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                getActivity().findViewById(R.id.search_text).clearFocus();
            }
        });

        getActivity().findViewById(R.id.search_back_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.hideSoftKeyboard(getActivity());
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });

        ((EditText) getActivity().findViewById(R.id.search_text)).addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() >= 3) {
                    if (searchTask != null && searchTask.getStatus() != AsyncTask.Status.FINISHED)
                        searchTask.cancel(true);
                    searchTask = new GetPanelTask(s.toString());

                    searchTask.execute();
                }
            }
        });
    }

    public class GetPanelTask extends AsyncTask<Void, Void, Integer> {

        JSONObject jsonObject;
        public List<Panel> panels;
        Response responseComplex = null;
        Response responseSimple= null;
        private String searchStr;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            loopDialog.setVisibility(View.VISIBLE);
        }
        GetPanelTask(String searchStr) {
            this.searchStr = searchStr;
        }


        @Override
        protected Integer doInBackground(Void... params) {
            try {

                    responseComplex = RequestService.createGetRequest("/api/analytic2" + "?type=COMPLEX" + "&search=" + searchStr);
                    responseSimple = RequestService.createGetRequest("/api/analytic2" +  "?type=SIMPLE" + "&search=" + searchStr);

                String responseComplexStr = responseComplex.body().string();
                JSONObject jsonObjectComplex = new JSONObject(responseComplexStr);
                JSONArray panelsJsonComplex = jsonObjectComplex.getJSONArray("list");
                ArrayList<Panel> panelsComplex  = new ObjectMapper().readValue(panelsJsonComplex.toString(), new TypeReference<ArrayList<Panel>>() {});

                String responseSimpleStr = responseSimple.body().string();
                JSONObject jsonObjectSimple = new JSONObject(responseSimpleStr);
                JSONArray panelsJsonSimple = jsonObjectSimple.getJSONArray("list");
                ArrayList<Panel> panelsSimple  = new ObjectMapper().readValue(panelsJsonSimple.toString(), new TypeReference<ArrayList<Panel>>() {});
                panelsComplex.addAll(panelsSimple);
                jsonObject = jsonObjectSimple;
                panels = panelsComplex;


                for (Panel panel : panels){
                    panel.prc =  new BigDecimal(panel.prc).setScale(2, RoundingMode.HALF_EVEN).doubleValue();;
                    panel.fixDate();}


                return responseComplex.code();
            } catch (Exception exc) {
                Log.e("Panel", "unexpected JSON exception", exc);

                exc.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(Integer responseCode) {
            super.onPostExecute(responseCode);
                try {

                showPanels(panels);



            } catch (Exception exc) {
                Log.e("spinner", exc.toString());
                exc.printStackTrace();
            }
        }
    }



    private void showPanels(List<Panel> panels) {
        List<Panel> syntheticPanels = new ArrayList<>();
        List<Panel> normalPanels = new ArrayList<>();

        for (Panel panel : panels) {
            if (panel.type.equals("complex"))
                syntheticPanels.add(panel);
            else
                normalPanels.add(panel);
        }
        loopDialog.setVisibility(View.GONE);

        Collections.sort(syntheticPanels, (o1, o2) -> o1.name.compareToIgnoreCase(o2.name));
        Collections.sort(normalPanels, (o1, o2) -> o1.name.compareToIgnoreCase(o2.name));

        panels.clear();
        panels.addAll(syntheticPanels);
        panels.add(Panel.getSeparatorPanel());
        panels.addAll(normalPanels);
        Panel panel = new Panel();
        panels.add(0, panel);

        recyclerView.setAdapter(new PanelAdapter(panels, new PanelAdapter.OnPanelClickListener() {
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




}
