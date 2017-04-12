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
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import net.cachapa.expandablelayout.ExpandableLayout;

import org.dev_alex.mojo_qa.mojo.Data;
import org.dev_alex.mojo_qa.mojo.R;
import org.dev_alex.mojo_qa.mojo.activities.AuthActivity;
import org.dev_alex.mojo_qa.mojo.activities.MainActivity;
import org.dev_alex.mojo_qa.mojo.models.Page;
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
    private ArrayList<Page> pages;
    private int currentPagePos;

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
        rootView.findViewById(R.id.left_arrow).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (pages != null && !pages.isEmpty())
                    if (currentPagePos > 0)
                        setPage(pages.get(currentPagePos - 1));
            }
        });

        rootView.findViewById(R.id.right_arrow).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (pages != null && !pages.isEmpty())
                    if (currentPagePos < pages.size() - 1)
                        setPage(pages.get(currentPagePos + 1));
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

    private void renderTemplate(JSONObject template) {
        try {
            if (template != null) {
                if (template.has("name"))
                    ((TextView) rootView.findViewById(R.id.template_name)).setText(template.getString("name"));

                pages = new ArrayList<>();

                JSONArray pagesJson = template.getJSONArray("items");
                for (int i = 0; i < pagesJson.length(); i++) {
                    LinearLayout rootContainer = new LinearLayout(getContext());
                    rootContainer.setOrientation(LinearLayout.VERTICAL);
                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    rootContainer.setLayoutParams(layoutParams);

                    JSONObject pageJson = pagesJson.getJSONObject(i).getJSONObject("page");
                    if (pageJson.has("items"))
                        fillContainer(rootContainer, pageJson.getJSONArray("items"));

                    final Page page = new Page(pageJson.getString("caption"), pageJson.getString("id"), rootContainer);
                    pages.add(page);

                    LinearLayout pageContainer = (LinearLayout) rootView.findViewById(R.id.page_container);
                    TextView cardPage = (TextView) getActivity().getLayoutInflater().inflate(R.layout.card_page, pageContainer, false);
                    cardPage.setText(page.name);
                    pageContainer.addView(cardPage);
                    cardPage.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            setPage(page);
                            rootView.findViewById(R.id.page_select_layout).setVisibility(View.GONE);
                        }
                    });
                }
                if (!pages.isEmpty()) {
                    setPage(pages.get(0));

                    rootView.findViewById(R.id.page_name).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (rootView.findViewById(R.id.page_select_layout).getVisibility() == View.GONE)
                                rootView.findViewById(R.id.page_select_layout).setVisibility(View.VISIBLE);
                            else
                                rootView.findViewById(R.id.page_select_layout).setVisibility(View.GONE);
                        }
                    });

                    rootView.findViewById(R.id.buttons_block).setVisibility(View.VISIBLE);
                }
            }
        } catch (Exception exc) {
            exc.printStackTrace();
        }
    }

    private void setPage(Page page) {
        FrameLayout rootContainer = (FrameLayout) rootView.findViewById(R.id.root_container);
        rootContainer.removeAllViewsInLayout();
        rootContainer.addView(page.layout);

        ((TextView) rootView.findViewById(R.id.page_name)).setText(page.name);
        currentPagePos = pages.indexOf(page);
        for (int i = 0; i < ((LinearLayout) rootView.findViewById(R.id.page_container)).getChildCount(); i++)
            if (currentPagePos == i) {
                ((LinearLayout) rootView.findViewById(R.id.page_container)).getChildAt(i).setBackgroundColor(Color.parseColor("#ff322452"));
                ((LinearLayout) rootView.findViewById(R.id.page_container)).getChildAt(i).setAlpha(1);
            } else {
                ((LinearLayout) rootView.findViewById(R.id.page_container)).getChildAt(i).setBackgroundColor(Color.TRANSPARENT);
                ((LinearLayout) rootView.findViewById(R.id.page_container)).getChildAt(i).setAlpha(0.83f);
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
                    createSelectBtnContainer(value, container);
                    break;

                case "text":
                    WebView text = new WebView(getContext());
                    String mime = "text/html";
                    String encoding = "utf-8";
                    String html;

                    if (value.has("text"))
                        html = value.getString("text");
                    else
                        html = "Нет текста";

                    text.getSettings().setJavaScriptEnabled(true);
                    text.loadDataWithBaseURL(null, html, mime, encoding, null);
                    container.addView(text);
                    break;

                case "lineedit":
                    LinearLayout editTextSingleLineContainer = (LinearLayout) getActivity().getLayoutInflater().inflate(R.layout.lineedit, container, false);

                    if (value.has("caption"))
                        ((TextView) editTextSingleLineContainer.getChildAt(0)).setText(value.getString("caption"));
                    else
                        ((TextView) editTextSingleLineContainer.getChildAt(0)).setText("Нет текста");

                    container.addView(editTextSingleLineContainer);
                    break;

                case "textarea":
                    LinearLayout editTextMultiLineContainer = (LinearLayout) getActivity().getLayoutInflater().inflate(R.layout.textarea, container, false);

                    if (value.has("caption"))
                        ((TextView) editTextMultiLineContainer.getChildAt(0)).setText(value.getString("caption"));
                    else
                        ((TextView) editTextMultiLineContainer.getChildAt(0)).setText("Нет текста");

                    container.addView(editTextMultiLineContainer);
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

                case "photo":
                    break;

                case "richedit":
                    WebView richEdit = new WebView(getContext());
                    mime = "text/html";
                    encoding = "utf-8";

                    if (value.has("html"))
                        html = value.getString("html");
                    else
                        html = "Нет текста";

                    richEdit.getSettings().setJavaScriptEnabled(true);
                    richEdit.loadDataWithBaseURL(null, html, mime, encoding, null);
                    container.addView(richEdit);
                    Log.d("jeka", fields.get(i));
                    break;

                default:
                    Log.d("jeka", fields.get(i));
            }


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

    private void createSelectBtnContainer(JSONObject value, LinearLayout container) throws Exception {
        LinearLayout selectBtnLayout = (LinearLayout) getActivity().getLayoutInflater().inflate(R.layout.select_layout, container, false);
        LinearLayout selectBtnContainer = (LinearLayout) selectBtnLayout.getChildAt(1);

        if (value.has("caption"))
            ((TextView) selectBtnLayout.getChildAt(0)).setText(value.getString("caption"));
        else
            ((TextView) selectBtnLayout.getChildAt(0)).setText("Нет заголовка");

        if (value.has("options")) {
            final ArrayList<RadioButton> buttons = new ArrayList<>();
            CompoundButton.OnCheckedChangeListener radioButtonListener = new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton selectedButton, boolean isChecked) {
                    if (isChecked)
                        for (CompoundButton compoundButton : buttons)
                            if (!compoundButton.equals(selectedButton))
                                compoundButton.setChecked(false);
                }
            };

            LinearLayout currentRow = new LinearLayout(getContext());

            JSONArray options = value.getJSONArray("options");
            for (int j = 0; j < options.length(); j++) {
                JSONObject option = options.getJSONObject(j);
                if (j % 2 == 0) {
                    currentRow = new LinearLayout(getContext());
                    currentRow.setOrientation(LinearLayout.HORIZONTAL);
                    selectBtnContainer.addView(currentRow);
                }

                RadioButton selectBtn = (RadioButton) getActivity().getLayoutInflater().inflate(R.layout.select_btn, currentRow, false);
                selectBtn.setOnCheckedChangeListener(radioButtonListener);
                buttons.add(selectBtn);

                if (option.has("caption"))
                    selectBtn.setText(option.getString("caption"));
                else
                    selectBtn.setText("Нет текста:(");
                currentRow.addView(selectBtn);
            }
            if (options.length() % 2 == 1) {
                Space space = new Space(getContext());
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT);
                layoutParams.weight = 1;
                space.setLayoutParams(layoutParams);
                currentRow.addView(space);
            }
        }
        container.addView(selectBtnLayout);
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
                    template = new JSONObject(Data.tt);
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
