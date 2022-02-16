package org.dev_alex.mojo_qa.mojo.fragments;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.JsonReader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.dev_alex.mojo_qa.mojo.R;
import org.dev_alex.mojo_qa.mojo.adapters.PanelAdapter;
import org.dev_alex.mojo_qa.mojo.custom_views.MultiSpinner;
import org.dev_alex.mojo_qa.mojo.models.Organisation;
import org.dev_alex.mojo_qa.mojo.models.Panel;
import org.dev_alex.mojo_qa.mojo.services.RequestService;
import org.dev_alex.mojo_qa.mojo.services.Utils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import okhttp3.Response;

public class PanelListFragment extends Fragment {
    private View rootView;
    private ProgressDialog loopDialog;
    private RecyclerView recyclerView;
    private CheckBox allAnalyticsSwitch;
    JSONObject jsonObject;
    public List<Panel> panels;
    public List<Organisation> organisations = new ArrayList<>();
    ArrayList<String> tags = new ArrayList<>();
    public MultiSpinner multiSpinner;

    private GetPanelsTask getPanelsTask = null;
    public static boolean wasCreated = false;
    Response responseComplex = null;
    Response responseSimple= null;
    //


    public static PanelListFragment newInstance() {
        return new PanelListFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        if (rootView == null) {
            rootView = inflater.inflate(R.layout.fragment_panel_list, container, false);

            allAnalyticsSwitch =  rootView.findViewById(R.id.all_analytics_switch);

            recyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            Utils.setupCloseKeyboardUI(getActivity(), rootView);

            initDialog();


            getPanelsTask = new GetPanelsTask();
            getPanelsTask.execute();

            allAnalyticsSwitch.setChecked(false);
            allAnalyticsSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (getPanelsTask != null && getPanelsTask.getStatus() != AsyncTask.Status.FINISHED){

                        getPanelsTask.cancel(false);}

                    getPanelsTask = new GetPanelsTask();
                    getPanelsTask.execute();
                }
            });
        }

        setupHeader();
        return rootView;
    }

    @Override
    public void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    private void setupHeader() {
        ((TextView) getActivity().findViewById(R.id.title)).setText(getString(R.string.analystics));
        getActivity().findViewById(R.id.back_btn).setVisibility(View.GONE);

        getActivity().findViewById(R.id.grid_btn).setVisibility(View.GONE);
        getActivity().findViewById(R.id.sandwich_btn).setVisibility(View.VISIBLE);
        getActivity().findViewById(R.id.group_by_btn).setVisibility(View.VISIBLE);
        getActivity().findViewById(R.id.search_btn).setVisibility(View.VISIBLE);
        getActivity().findViewById(R.id.notification_btn).setVisibility(View.GONE);
        getActivity().findViewById(R.id.spin).setVisibility(View.VISIBLE);
        getActivity().findViewById(R.id.qr_btn).setVisibility(View.GONE);
        getActivity().findViewById(R.id.search_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container, SearchResultFragment.newInstance()).addToBackStack(null).commit();
            }
        });
        getActivity().findViewById(R.id.group_by_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    private void initDialog() {
        loopDialog = new ProgressDialog(getContext(), R.style.ProgressDialogStyle);
        loopDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        loopDialog.setMessage(getString(R.string.loading_please_wait));
        loopDialog.setIndeterminate(true);
        loopDialog.setCanceledOnTouchOutside(false);
        loopDialog.setCancelable(false);
    }
    @Override
    public void onResume() {
        super.onResume();
        getActivity().findViewById(R.id.main_menu_search_block).setVisibility(View.GONE);
        getActivity().findViewById(R.id.main_menu_buttons_block).setVisibility(View.VISIBLE);
    }

    private void onPanelClick(Panel panel) {
        getActivity()
                .getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container, GraphListFragment.newInstance(panel))
                .addToBackStack(null)
                .commit();
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
    public void onItemsSelected(boolean[] selected) {
        tags.clear();
        for(int i = 0; i < selected.length; i++){
            if(selected[i]){
                tags.add(organisations.get(i).getId());
            }
        }
        if(tags.size() == 0){
            Toast.makeText(getContext(), "Вы ничего не выбрали", Toast.LENGTH_SHORT).show();
        }
        updateNotifications();
}


    private void updateNotifications() {
            new GetPanelsTask().execute();
    }

    private class GetPanelsTask extends AsyncTask<Void, Void, Integer> {

        private boolean isAll;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            isAll = allAnalyticsSwitch.isChecked();

            loopDialog.show();
        }

        @Override
        protected Integer doInBackground(Void... params) {
            try {
                String filter = "";
                if(organisations.size() == 0 || tags.size() == 0){
                    responseComplex = RequestService.createGetRequest("/api/analytic2" + (isAll ? "?type=COMPLEX" : "?type=ROOTS_COMPLEX"));
                    responseSimple = RequestService.createGetRequest("/api/analytic2" + (isAll ? "?type=SIMPLE" : "?type=ROOTS_SIMPLE"));
                }
                else {
                    wasCreated = true;
                    String sortParameter = "tag=";
                    filter = "?";
                    for (int i = 0; i < tags.size(); i++){
                        filter = filter + sortParameter + tags.get(i);
                        if(i != (tags.size() - 1)){
                            filter = filter + "&";
                        }
                    }

                    responseComplex = RequestService.createGetRequest("/api/analytic2" + filter+ (isAll ? "&type=COMPLEX" : "&type=ROOTS_COMPLEX"));
                    responseSimple = RequestService.createGetRequest("/api/analytic2" + filter+(isAll ? "&type=SIMPLE" : "&type=ROOTS_SIMPLE"));                }
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
                if(!wasCreated){

                JSONArray organizationsJson = jsonObject.getJSONObject("available").getJSONArray("tags");
                organisations = new ObjectMapper().readValue(organizationsJson.toString(), new TypeReference<ArrayList<Organisation>>() {});
                ArrayList<String> organisationNames = new ArrayList<>();
                multiSpinner = (MultiSpinner) getActivity().findViewById(R.id.spin);
                for (int i = 0; i < organisations.size(); i++){
                    organisationNames.add(organisations.get(i).getName());
                }
                multiSpinner.setItems(getActivity(), organisations, "aaa", PanelListFragment.this::onItemsSelected);
            wasCreated = true;}

                if (loopDialog != null && loopDialog.isShowing())
                    loopDialog.dismiss();
                if (!isCancelled())
                    showPanels(panels);

            } catch (Exception exc) {
                Log.e("spinner", exc.toString());
                exc.printStackTrace();
            }
        }
    }



}
