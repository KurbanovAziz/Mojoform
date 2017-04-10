package org.dev_alex.mojo_qa.mojo.fragments;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import net.cachapa.expandablelayout.ExpandableLayout;

import org.dev_alex.mojo_qa.mojo.R;
import org.dev_alex.mojo_qa.mojo.activities.AuthActivity;
import org.dev_alex.mojo_qa.mojo.activities.MainActivity;
import org.dev_alex.mojo_qa.mojo.services.RequestService;
import org.dev_alex.mojo_qa.mojo.services.TokenService;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;

import okhttp3.Response;

public class TemplateFragment extends Fragment {
    private View rootView;
    private ProgressDialog loopDialog;
    private String templateId;

    public static TemplateFragment newInstance(String templateId) {
        Bundle args = new Bundle();
        args.putString("template_id", templateId);

        TemplateFragment fragment = new TemplateFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_template, container, false);
        ((MainActivity) getActivity()).drawer.getDrawerLayout().setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        templateId = getArguments().getString("template_id");

        initDialog();
        setupHeader();

        setListeners();

        new GetTemplateTask(templateId).execute();
        return rootView;
    }

    private void setupHeader() {
        ((TextView) getActivity().findViewById(R.id.title)).setText(getString(R.string.tasks));
        getActivity().findViewById(R.id.grid_btn).setVisibility(View.GONE);
        getActivity().findViewById(R.id.sandwich_btn).setVisibility(View.GONE);
        getActivity().findViewById(R.id.group_by_btn).setVisibility(View.GONE);
        getActivity().findViewById(R.id.search_btn).setVisibility(View.GONE);

        getActivity().findViewById(R.id.back_btn).setVisibility(View.VISIBLE);
        getActivity().findViewById(R.id.back_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager().popBackStack();
                ((MainActivity) getActivity()).drawer.getDrawerLayout().setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
            }
        });
    }

    private void setListeners() {

    }

    private void initDialog() {
        loopDialog = new ProgressDialog(getContext(), R.style.ProgressDialogStyle);
        loopDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        loopDialog.setMessage(getString(R.string.loading_please_wait));
        loopDialog.setIndeterminate(true);
        loopDialog.setCanceledOnTouchOutside(false);
        loopDialog.setCancelable(false);
    }

    private void renderTemplate(JSONObject template) {
        try {
            LinearLayout rootContainer = (LinearLayout) rootView.findViewById(R.id.root_container);
            JSONArray pages = template.getJSONArray("items");
            for (int i = 0; i < pages.length(); i++) {
                JSONObject page = pages.getJSONObject(i).getJSONObject("page");
                if (page.has("items"))
                    fillContainer(rootContainer, page.getJSONArray("items"));

            }
        } catch (Exception exc) {
            exc.printStackTrace();
        }
    }

    private void fillContainer(LinearLayout container, JSONArray dataJson) throws Exception {
        ArrayList<String> fields = new ArrayList<>();
        for (int i = 0; i < dataJson.length(); i++) {
            JSONObject value = dataJson.getJSONObject(i);
            Iterator<String> iterator = value.keys();
            while (iterator.hasNext()) {
                String currentKey = iterator.next();
                fields.add(currentKey);
            }
        }

        for (int i = 0; i < fields.size(); i++) {
            JSONObject value = dataJson.getJSONObject(i).getJSONObject(fields.get(i));
            switch (fields.get(i)) {
                case "category":
                    LinearLayout categoryHeader = (LinearLayout) getActivity().getLayoutInflater().inflate(R.layout.category_header, null, false);
                    if (value.has("caption"))
                        ((TextView) categoryHeader.getChildAt(1)).setText(value.getString("caption"));
                    else
                        ((TextView) categoryHeader.getChildAt(1)).setText("Нет заголовка");

                    LinearLayout expandableContent = (LinearLayout) getActivity().getLayoutInflater().inflate(R.layout.expandable_content, null, false);
                    fillContainer(expandableContent, value.getJSONArray("items"));

                    final ExpandableLayout expandableLayout = new ExpandableLayout(getContext());
                    expandableLayout.setOrientation(ExpandableLayout.VERTICAL);
                    expandableLayout.addView(expandableContent);

                    categoryHeader.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            expandableLayout.toggle();
                        }
                    });

                    container.addView(categoryHeader);
                    container.addView(expandableLayout);
                    break;

                case "select":
                    break;

                case "text":
                    break;

                case "lineedit":
                    break;

                case "textarea":
                    break;

                case "checkbox":
                    break;

                case "slider":
                    break;
            }
        }
        Log.d("jeka", String.valueOf(fields.size()));
    }

    private class GetTemplateTask extends AsyncTask<Void, Void, Integer> {
        private String templateId;
        private JSONObject template;

        public GetTemplateTask(String templateId) {
            this.templateId = templateId;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            loopDialog.show();
        }

        @Override
        protected Integer doInBackground(Void... params) {
            try {
                String url = "/api/fs-mojo/get/template/" + templateId;

                Response response = RequestService.createGetRequest(url);

                if (response.code() == 200) {
                    String responseStr = response.body().string();
                    template = new JSONObject(responseStr);
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
                renderTemplate(template);
            }
        }
    }
}
