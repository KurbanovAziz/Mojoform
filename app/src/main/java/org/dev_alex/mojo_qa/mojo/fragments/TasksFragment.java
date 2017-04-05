package org.dev_alex.mojo_qa.mojo.fragments;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.dev_alex.mojo_qa.mojo.Data;
import org.dev_alex.mojo_qa.mojo.R;
import org.dev_alex.mojo_qa.mojo.activities.AuthActivity;
import org.dev_alex.mojo_qa.mojo.adapters.TaskAdapter;
import org.dev_alex.mojo_qa.mojo.models.Task;
import org.dev_alex.mojo_qa.mojo.services.TokenService;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import okhttp3.Credentials;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class TasksFragment extends Fragment {
    private final int ALL_TASKS = 1;
    private final int FINISHED_TASKS = 2;
    private final int BUSY_TASKS = 3;

    private View rootView;
    private ProgressDialog loopDialog;
    private RecyclerView recyclerView;


    public static TasksFragment newInstance() {
        Bundle args = new Bundle();
        TasksFragment fragment = new TasksFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_tasks, container, false);

        recyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        setListeners();
        initDialog();
        setupHeader();

        ((RadioButton) rootView.findViewById(R.id.busy)).setChecked(true);

        return rootView;
    }


    private void setupHeader() {
        ((TextView) getActivity().findViewById(R.id.title)).setText(getString(R.string.tasks));
        getActivity().findViewById(R.id.grid_btn).setVisibility(View.GONE);
        getActivity().findViewById(R.id.back_btn).setVisibility(View.GONE);

        getActivity().findViewById(R.id.sandwich_btn).setVisibility(View.VISIBLE);
        getActivity().findViewById(R.id.download_btn).setVisibility(View.VISIBLE);
        getActivity().findViewById(R.id.search_btn).setVisibility(View.VISIBLE);
    }

    private void setListeners() {
        ((RadioGroup) rootView.findViewById(R.id.task_toggle)).setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
                switch (checkedId) {
                    case R.id.ended:
                        new GetTasksTask(FINISHED_TASKS).execute();
                        break;

                    case R.id.busy:
                        new GetTasksTask(BUSY_TASKS).execute();
                        break;

                    case R.id.all:
                        new GetTasksTask(ALL_TASKS).execute();
                        break;
                }
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

    private class GetTasksTask extends AsyncTask<Void, Void, Integer> {
        private ArrayList<Task> tasks;
        private int type;

        GetTasksTask(int type) {
            this.type = type;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            loopDialog.show();
        }

        @Override
        protected Integer doInBackground(Void... params) {
            try {
                tasks = new ArrayList<>();

                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder().header("Authorization", Credentials.basic("kermit", "kermit"))
                        .url("http://jira.dev-alex.org:19080/activiti-rest/service/runtime/tasks?taskAssignee="
                                + Data.currentUser.userName + "&includeProcessVariables=TRUE").build();

                Response response = client.newCall(request).execute();

                if (response.code() == 200) {
                    JSONArray tasksJson = new JSONObject(response.body().string()).getJSONArray("data");
                    tasks = new ObjectMapper().readValue(tasksJson.toString(), new TypeReference<ArrayList<Task>>() {
                    });
                }

                return response.code();
            } catch (Exception exc) {
                exc.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(Integer responseCode) {
            super.onPostExecute(responseCode);
            if (loopDialog != null && loopDialog.isShowing())
                loopDialog.dismiss();

            if (responseCode == null)
                Toast.makeText(getContext(), R.string.network_error, Toast.LENGTH_LONG).show();
            else if (responseCode == 401) {
                TokenService.deleteToken();
                startActivity(new Intent(getContext(), AuthActivity.class));
                getActivity().finish();
            } else {
                recyclerView.setAdapter(new TaskAdapter(getContext(), tasks));
            }
        }
    }
}
