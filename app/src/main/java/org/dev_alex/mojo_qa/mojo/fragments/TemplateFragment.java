package org.dev_alex.mojo_qa.mojo.fragments;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.LayerDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.Space;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.DigitsKeyListener;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
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
import java.util.Locale;
import java.util.Random;

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
                    createCategory(value, container);
                    break;

                case "select":
                    break;

                case "text":
                    WebView myWebView = new WebView(getContext());
                    String mime = "text/html";
                    String encoding = "utf-8";
                    String html;

                    if (value.has("text"))
                        html = value.getString("text");
                    else
                        html = "Нет текста";

                    myWebView.getSettings().setJavaScriptEnabled(true);
                    myWebView.loadDataWithBaseURL(null, html, mime, encoding, null);
                    container.addView(myWebView);
                    break;

                case "lineedit":
                    break;

                case "textarea":
                    break;

                case "checkbox":
                    LinearLayout checkBoxContainer = (LinearLayout) getActivity().getLayoutInflater().inflate(R.layout.checkbox, container, false);

                    if (value.has("caption"))
                        ((TextView) checkBoxContainer.getChildAt(1)).setText(value.getString("caption"));
                    else
                        ((TextView) checkBoxContainer.getChildAt(1)).setText("Нет текста");

                    container.addView(checkBoxContainer);
                    break;

                case "slider":
                    createSeekBar(value, container);
                    break;

                default:
            }
            Log.d("jeka", fields.get(i));

            Space space = new Space(getContext());
            space.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, 10, getResources().getDisplayMetrics())));
            container.addView(space);
        }
    }

    private void createCategory(JSONObject value, LinearLayout container) throws Exception {
        Random rnd = new Random();
        int color = Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));

        LinearLayout categoryHeader = (LinearLayout) getActivity().getLayoutInflater().inflate(R.layout.category_header, container, false);
        ((LayerDrawable) categoryHeader.getBackground()).findDrawableByLayerId(R.id.background).setColorFilter(color, PorterDuff.Mode.SRC_IN);

        if (value.has("caption"))
            ((TextView) categoryHeader.getChildAt(1)).setText(value.getString("caption"));
        else
            ((TextView) categoryHeader.getChildAt(1)).setText("Нет заголовка");

        LinearLayout expandableContent = (LinearLayout) getActivity().getLayoutInflater().inflate(R.layout.expandable_content, container, false);
        ((LayerDrawable) expandableContent.getBackground()).findDrawableByLayerId(R.id.background).setColorFilter(color, PorterDuff.Mode.SRC_IN);

        fillContainer(expandableContent, value.getJSONArray("items"));


        final ExpandableLayout expandableLayout = new ExpandableLayout(getContext());
        expandableLayout.setOrientation(ExpandableLayout.VERTICAL);
        expandableLayout.addView(expandableContent);
        if (value.has("collapsed"))
            if (value.getBoolean("collapsed"))
                expandableLayout.collapse();
            else
                expandableLayout.expand();

        categoryHeader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                expandableLayout.toggle();
            }
        });


        container.addView(categoryHeader);
        container.addView(expandableLayout);
    }

    private void createSeekBar(JSONObject value, LinearLayout container) throws Exception {
        final LinearLayout seekBarContainer = (LinearLayout) getActivity().getLayoutInflater().inflate(R.layout.slider, container, false);
        if (value.has("caption"))
            ((TextView) seekBarContainer.getChildAt(0)).setText(value.getString("caption"));
        else
            ((TextView) seekBarContainer.getChildAt(0)).setText("Нет текста");


        final SeekBar seekBar = ((SeekBar) seekBarContainer.getChildAt(1));
        final EditText changeValue = (EditText) ((LinearLayout) seekBarContainer.getChildAt(4)).getChildAt(1);

        final float minValue = (float) value.getDouble("min_value");
        final float maxValue = (float) value.getDouble("max_value");
        float step = (float) value.getDouble("step");
        String minValueStr = String.valueOf(minValue), maxValueStr = String.valueOf(maxValue);

        final int digitsAfterPoint = Math.max((minValueStr.contains(".")) ? 0 : minValueStr.substring(minValueStr.indexOf("." + 1)).length(),
                (maxValueStr.contains(".")) ? 0 : maxValueStr.substring(maxValueStr.indexOf("." + 1)).length());
        final int digitsOffset = (int) Math.pow(10, digitsAfterPoint);

        ((TextView) ((RelativeLayout) seekBarContainer.getChildAt(2)).getChildAt(0)).setText(formatFloat(minValue));

        ((TextView) ((RelativeLayout) seekBarContainer.getChildAt(2)).getChildAt(1)).setText(formatFloat(maxValue));
        seekBar.setMax((int) ((maxValue - minValue) * digitsOffset));
        seekBar.incrementProgressBy((int) (step * digitsOffset));
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                ((TextView) seekBarContainer.getChildAt(3)).setText(formatFloat((progress / digitsOffset) + minValue));
                changeValue.setText(formatFloat((progress / digitsOffset) + minValue));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        if (digitsOffset > 1) {
            if (minValue < 0)
                changeValue.setKeyListener(DigitsKeyListener.getInstance("-0123456789."));
            else
                changeValue.setKeyListener(DigitsKeyListener.getInstance("0123456789."));
        } else {
            if (minValue < 0)
                changeValue.setKeyListener(DigitsKeyListener.getInstance("-0123456789"));
            else
                changeValue.setKeyListener(DigitsKeyListener.getInstance("0123456789"));
        }

        changeValue.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String result;
                if (!s.toString().isEmpty()) {
                    result = s.toString();
                    float value = Float.parseFloat(s.toString());
                    if (value < minValue)
                        result = formatFloat(minValue);
                    else if (value > maxValue)
                        result = formatFloat(maxValue);
                    else if (!formatFloat(value).equals(s.toString()))
                        result = (formatFloat(value));
                    else {
                        int pointI = s.toString().indexOf(".");
                        if (pointI != -1) {
                            if (s.toString().substring(s.toString().indexOf("." + 1)).length() > digitsAfterPoint) {
                                result = s.toString().substring(0, s.toString().indexOf("." + 1) + digitsAfterPoint);
                            }
                        }
                    }

                    if (!s.toString().equals(result)) {
                        changeValue.setText(result);
                    }
                    if (!s.toString().isEmpty())
                        seekBar.setProgress((int) ((Float.parseFloat(result) - minValue) * digitsOffset));
                }
            }
        });

        container.addView(seekBarContainer);
        seekBar.setProgress(0);
    }

    public static String formatFloat(float d) {
        if (d == (long) d)
            return String.format(Locale.getDefault(), "%d", (int) d);
        else
            return String.format("%s", d);
    }

    private class GetTemplateTask extends AsyncTask<Void, Void, Integer> {
        private String templateId;
        private JSONObject template;

        GetTemplateTask(String templateId) {
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
