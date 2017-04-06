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
import java.util.Collections;
import java.util.Comparator;

import okhttp3.Credentials;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class TasksFragment extends Fragment {
    private View rootView;
    private ProgressDialog loopDialog;
    private RecyclerView recyclerView;

    private ArrayList<Task> finishedTasks;
    private ArrayList<Task> busyTasks;


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

        initDialog();
        setupHeader();
        new GetTasksTask().execute();
        ((RadioButton) rootView.findViewById(R.id.busy)).setChecked(true);

        setListeners();
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
                        recyclerView.setAdapter(new TaskAdapter(getContext(), finishedTasks));
                        break;

                    case R.id.busy:
                        recyclerView.setAdapter(new TaskAdapter(getContext(), busyTasks));
                        break;

                    case R.id.all:
                        ArrayList<Task> allTasks = new ArrayList<>(busyTasks);
                        allTasks.addAll(finishedTasks);

                        Collections.sort(allTasks, new Comparator<Task>() {
                            @Override
                            public int compare(Task task1, Task task2) {
                                if (task1.dueDate == null)
                                    return -1;
                                if (task2.dueDate == null)
                                    return 1;

                                if (task1.dueDate.getTime() == task2.dueDate.getTime())
                                    return 0;

                                return (task1.dueDate.after(task2.dueDate)) ? 1 : -1;
                            }
                        });

                        recyclerView.setAdapter(new TaskAdapter(getContext(), allTasks));
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

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            loopDialog.show();
        }

        @Override
        protected Integer doInBackground(Void... params) {
            try {
                Response response = null;
                finishedTasks = new ArrayList<>();
                busyTasks = new ArrayList<>();
                String url;

                for (int i = 0; i < 2; i++) {
                    if (i == 0)
                        url = "http://jira.dev-alex.org:19080/activiti-rest/service/history/" +
                                "historic-task-instances?taskAssignee=" + Data.currentUser.userName;
                    else
                        url = "http://jira.dev-alex.org:19080/activiti-rest/service/runtime/tasks?taskAssignee="
                                + Data.currentUser.userName + "&includeProcessVariables=TRUE";

                    OkHttpClient client = new OkHttpClient();
                    Request request = new Request.Builder().header("Authorization", Credentials.basic("kermit", "kermit"))
                            .url(url).build();

                    response = client.newCall(request).execute();

                    if (response.code() == 200) {
                        JSONArray tasksJson = new JSONObject(response.body().string()).getJSONArray("data");
                        if (i == 0)
                            finishedTasks = new ObjectMapper().readValue(tasksJson.toString(), new TypeReference<ArrayList<Task>>() {
                            });
                        else
                            busyTasks = new ObjectMapper().readValue(tasksJson.toString(), new TypeReference<ArrayList<Task>>() {
                            });
                    }
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
            }
            recyclerView.setAdapter(new TaskAdapter(getContext(), busyTasks));
        }
    }
}
