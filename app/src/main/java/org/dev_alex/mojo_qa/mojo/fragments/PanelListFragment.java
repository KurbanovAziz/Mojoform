package org.dev_alex.mojo_qa.mojo.fragments;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.dev_alex.mojo_qa.mojo.R;
import org.dev_alex.mojo_qa.mojo.adapters.PanelAdapter;
import org.dev_alex.mojo_qa.mojo.models.Panel;
import org.dev_alex.mojo_qa.mojo.services.RequestService;
import org.dev_alex.mojo_qa.mojo.services.Utils;
import org.json.JSONArray;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import okhttp3.Response;

public class PanelListFragment extends Fragment {
    private View rootView;
    private ProgressDialog loopDialog;
    private RecyclerView recyclerView;
    private Switch allAnalyticsSwitch;

    private GetPanelsTask getPanelsTask = null;

    public static PanelListFragment newInstance() {
        return new PanelListFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.fragment_panel_list, container, false);

            allAnalyticsSwitch = (Switch) rootView.findViewById(R.id.all_analytics_switch);

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
                    if (getPanelsTask != null && getPanelsTask.getStatus() != AsyncTask.Status.FINISHED)
                        getPanelsTask.cancel(false);

                    getPanelsTask = new GetPanelsTask();
                    getPanelsTask.execute();
                }
            });
        }

        setupHeader();
        return rootView;
    }

    private void setupHeader() {
        ((TextView) getActivity().findViewById(R.id.title)).setText(getString(R.string.analystics));
        getActivity().findViewById(R.id.back_btn).setVisibility(View.GONE);

        getActivity().findViewById(R.id.grid_btn).setVisibility(View.GONE);
        getActivity().findViewById(R.id.sandwich_btn).setVisibility(View.VISIBLE);
        getActivity().findViewById(R.id.group_by_btn).setVisibility(View.GONE);
        getActivity().findViewById(R.id.search_btn).setVisibility(View.GONE);
        getActivity().findViewById(R.id.notification_btn).setVisibility(View.GONE);
        getActivity().findViewById(R.id.qr_btn).setVisibility(View.GONE);
    }

    private void initDialog() {
        loopDialog = new ProgressDialog(getContext(), R.style.ProgressDialogStyle);
        loopDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        loopDialog.setMessage(getString(R.string.loading_please_wait));
        loopDialog.setIndeterminate(true);
        loopDialog.setCanceledOnTouchOutside(false);
        loopDialog.setCancelable(false);
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

        recyclerView.setAdapter(new PanelAdapter(panels, new PanelAdapter.OnPanelClickListener() {
            @Override
            public void onClick(Panel panel) {
                onPanelClick(panel);
            }
        }));
    }

    private class GetPanelsTask extends AsyncTask<Void, Void, Integer> {
        private List<Panel> panels;
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
                panels = new ArrayList<>();
                Response response = RequestService.createGetRequest("/api/analytic" + (isAll ? "/all" : ""));
                String responseStr = response.body().string();

                JSONArray panelsJson = new JSONArray(responseStr);
                panels = new ObjectMapper().readValue(panelsJson.toString(), new TypeReference<ArrayList<Panel>>() {
                });

                for (Panel panel : panels)
                    panel.fixDate();

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

                if (!isCancelled())
                    showPanels(panels);

            } catch (Exception exc) {
                exc.printStackTrace();
            }
        }
    }
}
