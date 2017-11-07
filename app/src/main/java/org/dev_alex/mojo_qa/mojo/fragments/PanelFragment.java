package org.dev_alex.mojo_qa.mojo.fragments;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.dev_alex.mojo_qa.mojo.R;
import org.dev_alex.mojo_qa.mojo.models.Panel;
import org.dev_alex.mojo_qa.mojo.services.RequestService;
import org.dev_alex.mojo_qa.mojo.services.Utils;
import org.json.JSONArray;
import org.json.JSONObject;

import okhttp3.Response;

public class PanelFragment extends Fragment {
    private View rootView;
    private ProgressDialog loopDialog;
    private Panel panel;
    private JSONObject panelJson;


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
            panel = (Panel) getArguments().getSerializable("panel");

            Utils.setupCloseKeyboardUI(getActivity(), rootView);
            initDialog();
            new GetPanelTask().execute();

            rootView.findViewById(R.id.page_btn).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    rootView.findViewById(R.id.page_select_layout).setVisibility(View.VISIBLE);
                }
            });
        }
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

    private void showPage(int pageI) {
        try {
            JSONObject page = panelJson.getJSONArray("items").getJSONObject(pageI).getJSONObject("page");
            ((TextView) rootView.findViewById(R.id.page_name)).setText(page.getString("caption"));
            ((TextView) rootView.findViewById(R.id.page_i)).setText(String.valueOf(pageI + 1));

            for (int i = 0; i < ((LinearLayout) rootView.findViewById(R.id.page_container)).getChildCount(); i++)
                if (pageI == i) {
                    ((LinearLayout) rootView.findViewById(R.id.page_container)).getChildAt(i).setBackgroundColor(Color.parseColor("#ff322452"));
                    ((LinearLayout) rootView.findViewById(R.id.page_container)).getChildAt(i).setAlpha(1);
                } else {
                    ((LinearLayout) rootView.findViewById(R.id.page_container)).getChildAt(i).setBackgroundColor(Color.TRANSPARENT);
                    ((LinearLayout) rootView.findViewById(R.id.page_container)).getChildAt(i).setAlpha(0.83f);
                }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addPages() {
        try {
            ((FrameLayout.LayoutParams) rootView.findViewById(R.id.page_select_layout).getLayoutParams()).leftMargin = 0;
            rootView.findViewById(R.id.page_select_layout).requestLayout();

            JSONArray pages = panelJson.getJSONArray("items");
            for (int i = 0; i < pages.length(); i++) {
                JSONObject page = pages.getJSONObject(i).getJSONObject("page");

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
                        showPage(finalI);
                        rootView.findViewById(R.id.page_select_layout).setVisibility(View.GONE);
                    }
                });
            }
        } catch (Exception exc) {
            exc.printStackTrace();
        }
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
                Response response = RequestService.createGetRequest("/api/fs-analytic/get/" + panel.panel_id);
                String responseStr = response.body().string();
                panelJson = new JSONObject(responseStr);
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

                addPages();
                showPage(0);

            } catch (Exception exc) {
                exc.printStackTrace();
            }
        }
    }
}
