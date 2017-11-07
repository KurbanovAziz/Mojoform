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

import org.dev_alex.mojo_qa.mojo.BuildConfig;
import org.dev_alex.mojo_qa.mojo.R;
import org.dev_alex.mojo_qa.mojo.adapters.PanelAdapter;
import org.dev_alex.mojo_qa.mojo.models.Panel;
import org.dev_alex.mojo_qa.mojo.services.LoginHistoryService;
import org.dev_alex.mojo_qa.mojo.services.Utils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
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
                .addToBackStack("dd")
                .replace(R.id.container, PanelFragment.newInstance(panel))
                .commit();
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

                String url = host + "/getPanel";

                JSONObject mainDataObject = new JSONObject();
                mainDataObject.put("userId", LoginHistoryService.getCurrentUser().username);

                JSONObject rootObject = new JSONObject();
                rootObject.put("_postanalitycs_getpanel", mainDataObject);

                final MediaType JSON = MediaType.parse("application/json");
                RequestBody body = RequestBody.create(JSON, rootObject.toString());

                OkHttpClient client = new OkHttpClient();
                Request.Builder requestBuilder = new Request.Builder()
                        .url(url)
                        .post(body)
                        .header("Authorization", "Basic YWRtaW46S0FESTdhc3VmandrbGVuaGtsOA==")
                        .addHeader("Content-Type", "application/json")
                        .header("Accept", "application/json");

                Response response = client.newCall(requestBuilder.build()).execute();
                String responseStr = response.body().string();

                JSONArray panelsJson = new JSONObject(responseStr).getJSONObject("Results").getJSONArray("panels");
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

                recyclerView.setAdapter(new PanelAdapter(panels, new PanelAdapter.OnPanelClickListener() {
                    @Override
                    public void onClick(Panel panel) {
                        onPanelClick(panel);
                    }
                }));

            } catch (Exception exc) {
                exc.printStackTrace();
            }
        }
    }
}
