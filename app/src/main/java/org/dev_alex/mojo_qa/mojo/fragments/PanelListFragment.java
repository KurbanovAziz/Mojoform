package org.dev_alex.mojo_qa.mojo.fragments;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import java.util.Comparator;
import java.util.List;

import okhttp3.Response;

public class PanelListFragment extends Fragment {
    private View rootView;
    private ProgressDialog loopDialog;
    private RecyclerView recyclerView;

    public static PanelListFragment newInstance() {
        return new PanelListFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.fragment_panel_list, container, false);
            recyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

            initDialog();
            new GetPanelsTask().execute();
            Utils.setupCloseKeyboardUI(getActivity(), rootView);
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
        List<Panel> synteticPanels = new ArrayList<>();
        List<Panel> normalPanels = new ArrayList<>();

        for (Panel panel : panels) {
            if (panel.type.equals("complex"))
                synteticPanels.add(panel);
            else
                normalPanels.add(panel);
        }

        Collections.sort(synteticPanels, new Comparator<Panel>() {
            @Override
            public int compare(Panel o1, Panel o2) {
                return o1.name.compareToIgnoreCase(o2.name);
            }
        });
        Collections.sort(normalPanels, new Comparator<Panel>() {
            @Override
            public int compare(Panel o1, Panel o2) {
                return o1.name.compareToIgnoreCase(o2.name);
            }
        });

        panels.clear();
        panels.addAll(synteticPanels);
        panels.add(Panel.getSeparatorPanel());
        panels.addAll(normalPanels);

        recyclerView.setAdapter(new PanelAdapter(panels, new PanelAdapter.OnPanelClickListener() {
            @Override
            public void onClick(Panel panel) {
                onPanelClick(panel);
            }
        }));
    }

    public class GetPanelsTask extends AsyncTask<Void, Void, Integer> {
        private List<Panel> panels;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            loopDialog.show();
        }

        @Override
        protected Integer doInBackground(Void... params) {
            try {
                panels = new ArrayList<>();
                Response response = RequestService.createGetRequest("/api/analytic");
                String responseStr = response.body().string();

                JSONArray panelsJson = new JSONArray(responseStr);
                panels = new ObjectMapper().readValue(panelsJson.toString(), new TypeReference<ArrayList<Panel>>() {
                });
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

                showPanels(panels);

            } catch (Exception exc) {
                exc.printStackTrace();
            }
        }
    }
}
