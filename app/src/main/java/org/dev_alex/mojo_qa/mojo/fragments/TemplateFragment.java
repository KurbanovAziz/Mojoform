package org.dev_alex.mojo_qa.mojo.fragments;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.LayerDrawable;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.util.Pair;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.Space;
import android.support.v7.widget.PopupMenu;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.DigitsKeyListener;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import net.cachapa.expandablelayout.ExpandableLayout;

import org.dev_alex.mojo_qa.mojo.App;
import org.dev_alex.mojo_qa.mojo.Data;
import org.dev_alex.mojo_qa.mojo.R;
import org.dev_alex.mojo_qa.mojo.activities.AuthActivity;
import org.dev_alex.mojo_qa.mojo.activities.ImageViewActivity;
import org.dev_alex.mojo_qa.mojo.activities.MainActivity;
import org.dev_alex.mojo_qa.mojo.models.Page;
import org.dev_alex.mojo_qa.mojo.services.BitmapService;
import org.dev_alex.mojo_qa.mojo.services.RequestService;
import org.dev_alex.mojo_qa.mojo.services.Utils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.net.HttpURLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;

import okhttp3.Response;

import static android.app.Activity.RESULT_OK;

public class TemplateFragment extends Fragment {
    private final String MEDIA_PATH_JSON_ARRAY = "media_paths";
    private SimpleDateFormat isoDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.getDefault());
    private final int VIDEO_REQUEST_CODE = 10;
    private final int PHOTO_REQUEST_CODE = 11;
    private final int AUDIO_REQUEST_CODE = 12;
    private final int DOCUMENT_REQUEST_CODE = 13;
    private final int IMAGE_SHOW_REQUEST_CODE = 110;

    private View rootView;
    private ProgressDialog loopDialog;
    private String templateId;
    private String taskId;
    private ArrayList<Page> pages;
    private int currentPagePos;
    private JSONObject template;

    private Pair<LinearLayout, JSONObject> currentMediaBlock;
    private String cameraImagePath;
    private String cameraVideoPath;

    private ProgressDialog progressDialog;


    public static TemplateFragment newInstance(String templateId, String taskId) {
        Bundle args = new Bundle();
        args.putString("template_id", templateId);
        args.putString("task_id", taskId);

        TemplateFragment fragment = new TemplateFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.fragment_template, container, false);
            ((MainActivity) getActivity()).drawer.getDrawerLayout().setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
            templateId = getArguments().getString("template_id");
            taskId = getArguments().getString("task_id");

            initDialog();
            setupHeader();

            setListeners();

            new GetTemplateTask(templateId).execute();
        }
        return rootView;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PHOTO_REQUEST_CODE && resultCode == RESULT_OK) {
            if (data == null)
                new ProcessingBitmapTask(cameraImagePath, true).execute();
            else {
                Uri selectedImage = data.getData();
                String picturePath = selectedImage.getPath();
                if (picturePath.endsWith(".png") || picturePath.endsWith(".jpg") || picturePath.endsWith(".jpeg") || picturePath.endsWith(".bmp"))
                    new ProcessingBitmapTask(picturePath, true).execute();
                else {
                    String[] filePathColumn = {MediaStore.Images.Media.DATA};
                    Cursor cursor = getActivity().getContentResolver().query(selectedImage, filePathColumn, null, null, null);

                    if (cursor == null) {
                        Toast.makeText(getContext(), "что-то пошло не так", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    cursor.moveToFirst();
                    int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                    picturePath = cursor.getString(columnIndex);
                    cursor.close();

                    String expansion = picturePath.substring(picturePath.lastIndexOf('.') + 1);
                    if (!expansion.equals("jpg") && !expansion.equals("png") && !expansion.equals("jpeg") && !expansion.equals("bmp")) {
                        Toast.makeText(getContext(), "Пожалуйста, выберите файл в формет jpg, png или bmp", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    new ProcessingBitmapTask(picturePath, true).execute();
                }
            }
        }

        if (requestCode == VIDEO_REQUEST_CODE && resultCode == RESULT_OK) {
            if (data == null || data.getAction() != null && data.getAction().equals("inline-data"))
                createVideoPreview(cameraVideoPath, true);
            else {
                Uri selectedImage = data.getData();
                String[] filePathColumn = {MediaStore.Images.Media.DATA};
                Cursor cursor = getActivity().getContentResolver().query(selectedImage, filePathColumn, null, null, null);

                if (cursor == null) {
                    Toast.makeText(getContext(), "что-то пошло не так", Toast.LENGTH_SHORT).show();
                    return;
                }
                cursor.moveToFirst();
                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                String videoPath = cursor.getString(columnIndex);
                cursor.close();

                createVideoPreview(videoPath, true);
            }
        }

        if (requestCode == IMAGE_SHOW_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            try {
                JSONArray deletedImages = new JSONArray(data.getStringExtra("deleted_images"));
                for (int i = 0; i < deletedImages.length(); i++) {
                    String imagePath = deletedImages.getString(i);
                    deleteMediaPath(currentMediaBlock.second, imagePath);

                    LinearLayout imageContainer = (LinearLayout) ((HorizontalScrollView) currentMediaBlock.first.getChildAt(1)).getChildAt(0);
                    for (int j = 0; j < imageContainer.getChildCount(); j++)
                        if (imageContainer.getChildAt(j).getTag().equals(imagePath))
                            imageContainer.removeViewAt(j);
                }
            } catch (Exception exc) {
                exc.printStackTrace();
            }
        }

        if (requestCode == DOCUMENT_REQUEST_CODE && resultCode == RESULT_OK && data != null)
            createDocumentPreview(data.getData().getPath(), true);

        if (requestCode == AUDIO_REQUEST_CODE && resultCode == RESULT_OK) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = {MediaStore.Images.Media.DATA};
            Cursor cursor = getActivity().getContentResolver().query(selectedImage, filePathColumn, null, null, null);

            if (cursor == null) {
                Toast.makeText(getContext(), "что-то пошло не так", Toast.LENGTH_SHORT).show();
                return;
            }
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String audioPath = cursor.getString(columnIndex);
            cursor.close();
            createAudioPreview(audioPath, true);
        }
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

        rootView.findViewById(R.id.hold_task_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (templateId != null) {
                    saveTemplateState();
                    getActivity().getSupportFragmentManager().popBackStack();
                }
            }
        });

        rootView.findViewById(R.id.finish_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Pair<Boolean, ArrayList<JSONObject>> result = checkIfTemplateIsFilled(template);

                if (result.first) {
                    new SendMediaTask(result.second).execute();
                } else
                    Toast.makeText(getContext(), R.string.not_all_required_fields_are_filled, Toast.LENGTH_LONG).show();
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

        progressDialog = new ProgressDialog(getContext(), R.style.ProgressDialogStyle);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setMessage(getString(R.string.loading_please_wait));
        progressDialog.setIndeterminate(false);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setCancelable(false);
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

    private void saveTemplateState() {
        try {
            if (!template.has("StartTime"))
                template.put("StartTime", isoDateFormat.format(new Date()));
            SharedPreferences mSettings;
            mSettings = App.getContext().getSharedPreferences("templates", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = mSettings.edit();

            editor.putString(templateId + Data.currentUser.userName, template.toString());
            editor.apply();
        } catch (Exception exc) {
            exc.printStackTrace();
        }
    }


    private Pair<Boolean, ArrayList<JSONObject>> checkIfTemplateIsFilled(JSONObject template) {
        try {
            ArrayList<JSONObject> photoObjects = new ArrayList<>();

            if (template != null) {
                JSONArray pagesJson = template.getJSONArray("items");
                for (int i = 0; i < pagesJson.length(); i++) {
                    JSONObject pageJson = pagesJson.getJSONObject(i).getJSONObject("page");
                    if (pageJson.has("items")) {
                        Pair<Boolean, ArrayList<JSONObject>> result = checkIfContainerIsFilled(pageJson.getJSONArray("items"));
                        if (!result.first)
                            return new Pair<>(false, null);
                        else
                            photoObjects.addAll(result.second);
                    }
                }
                return new Pair<>(true, photoObjects);
            }
        } catch (Exception exc) {
            exc.printStackTrace();
        }
        return new Pair<>(false, null);
    }

    private Pair<Boolean, ArrayList<JSONObject>> checkIfContainerIsFilled(JSONArray dataJson) throws Exception {
        ArrayList<JSONObject> photoObjects = new ArrayList<>();

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
            final JSONObject value = dataJson.getJSONObject(i).getJSONObject(fields.get(i));
            switch (fields.get(i)) {
                case "category":

                    Pair<Boolean, ArrayList<JSONObject>> result = checkIfContainerIsFilled(value.getJSONArray("items"));
                    if (!result.first)
                        return new Pair<>(false, null);
                    else
                        photoObjects.addAll(result.second);
                    break;

                case "question":
                    if (!value.has("value") && !(value.has("is_required") && !value.getBoolean("is_required")))
                        return new Pair<>(false, null);
                    break;

                case "text":
                    break;

                case "lineedit":
                    if (!value.has("value") && !(value.has("is_required") && !value.getBoolean("is_required")))
                        return new Pair<>(false, null);
                    break;

                case "textarea":
                    if (!value.has("value") && !(value.has("is_required") && !value.getBoolean("is_required")))
                        return new Pair<>(false, null);
                    break;

                case "checkbox":
                    if (!value.has("value") && !(value.has("is_required") && !value.getBoolean("is_required")))
                        return new Pair<>(false, null);
                    break;

                case "slider":
                    if (!value.has("value") && !(value.has("is_required") && !value.getBoolean("is_required")))
                        return new Pair<>(false, null);
                    break;

                case "photo":
                    if ((!value.has(MEDIA_PATH_JSON_ARRAY) || (value.has(MEDIA_PATH_JSON_ARRAY) && value.getJSONArray(MEDIA_PATH_JSON_ARRAY).length() == 0))
                            && !(value.has("is_required") && !value.getBoolean("is_required")))
                        return new Pair<>(false, null);
                    else
                        photoObjects.add(value);
                    break;

                case "richedit":
                    break;

                default:
                    Log.d("jeka", fields.get(i));
            }
        }
        return new Pair<>(true, photoObjects);
    }

    private void renderTemplate() {
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
                        fillContainer(rootContainer, pageJson.getJSONArray("items"), 0);

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

    private void fillContainer(LinearLayout container, JSONArray dataJson, int offset) throws Exception {
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
            final JSONObject value = dataJson.getJSONObject(i).getJSONObject(fields.get(i));
            switch (fields.get(i)) {
                case "category":
                    createCategory(value, container, offset);
                    break;

                case "question":
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

                    if (value.has("value"))
                        ((EditText) editTextSingleLineContainer.getChildAt(1)).setText(value.getString("value"));
                    ((EditText) editTextSingleLineContainer.getChildAt(1)).addTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                        }

                        @Override
                        public void onTextChanged(CharSequence s, int start, int before, int count) {

                        }

                        @Override
                        public void afterTextChanged(Editable s) {
                            try {
                                if (s.toString().trim().isEmpty())
                                    value.remove("value");
                                else
                                    value.put("value", s.toString().trim());
                            } catch (Exception ignored) {
                            }
                        }
                    });
                    container.addView(editTextSingleLineContainer);
                    break;

                case "textarea":
                    LinearLayout editTextMultiLineContainer = (LinearLayout) getActivity().getLayoutInflater().inflate(R.layout.textarea, container, false);

                    if (value.has("caption"))
                        ((TextView) editTextMultiLineContainer.getChildAt(0)).setText(value.getString("caption"));
                    else
                        ((TextView) editTextMultiLineContainer.getChildAt(0)).setText("Нет текста");

                    if (value.has("value"))
                        ((EditText) editTextMultiLineContainer.getChildAt(1)).setText(value.getString("value"));
                    ((EditText) editTextMultiLineContainer.getChildAt(1)).addTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                        }

                        @Override
                        public void onTextChanged(CharSequence s, int start, int before, int count) {

                        }

                        @Override
                        public void afterTextChanged(Editable s) {
                            try {
                                if (s.toString().trim().isEmpty())
                                    value.remove("value");
                                else
                                    value.put("value", s.toString().trim());
                            } catch (Exception ignored) {
                            }
                        }
                    });
                    container.addView(editTextMultiLineContainer);
                    break;

                case "checkbox":
                    LinearLayout checkBoxContainer = (LinearLayout) getActivity().getLayoutInflater().inflate(R.layout.checkbox, container, false);

                    if (value.has("caption"))
                        ((TextView) checkBoxContainer.getChildAt(1)).setText(value.getString("caption"));
                    else
                        ((TextView) checkBoxContainer.getChildAt(1)).setText("Нет текста");

                    ((CheckBox) checkBoxContainer.getChildAt(0)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            try {
                                value.put("value", isChecked);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });

                    if (value.has("value"))
                        ((CheckBox) checkBoxContainer.getChildAt(0)).setChecked(value.getBoolean("value"));
                    container.addView(checkBoxContainer);
                    break;

                case "slider":
                    createSeekBar(value, container);
                    break;

                case "photo":
                    createMediaBlock(value, container);
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

    private void createCategory(JSONObject value, LinearLayout container, int offset) throws Exception {
        int color;
        switch (offset) {
            case 0:
                color = Color.parseColor("#B7B5EC");
                break;
            case 1:
                color = Color.parseColor("#C3E1FE");
                break;
            case 2:
                color = Color.parseColor("#DBF5D8");
                break;
            case 3:
                color = Color.parseColor("#F3D8F5");
                break;
            case 4:
                color = Color.parseColor("#F4EAD8");
                break;

            default:
                color = Color.parseColor("#F4EAD8");
                break;
        }

        LinearLayout categoryHeader = (LinearLayout) getActivity().getLayoutInflater().inflate(R.layout.category_header, container, false);
        ((LayerDrawable) categoryHeader.getBackground()).findDrawableByLayerId(R.id.background).setColorFilter(color, PorterDuff.Mode.SRC_IN);

        if (value.has("name"))
            ((TextView) categoryHeader.getChildAt(1)).setText(value.getString("name"));
        else
            ((TextView) categoryHeader.getChildAt(1)).setText("Нет заголовка");

        LinearLayout expandableContent = (LinearLayout) getActivity().getLayoutInflater().inflate(R.layout.expandable_content, container, false);
        ((LayerDrawable) expandableContent.getBackground()).findDrawableByLayerId(R.id.background).setColorFilter(color, PorterDuff.Mode.SRC_IN);

        fillContainer(expandableContent, value.getJSONArray("items"), offset + 1);


        final ExpandableLayout expandableLayout = new ExpandableLayout(getContext());
        expandableLayout.setOrientation(ExpandableLayout.VERTICAL);
        expandableLayout.setDuration(800);
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

    private void createSeekBar(final JSONObject value, LinearLayout container) throws Exception {
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
            boolean trackByUser = false;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                ((TextView) seekBarContainer.getChildAt(3)).setText(formatFloat((progress / digitsOffset) + minValue));
                if (trackByUser)
                    changeValue.setText(formatFloat((progress / digitsOffset) + minValue));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                trackByUser = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                trackByUser = false;
            }
        });

        if (minValue < 0)
            changeValue.setKeyListener(DigitsKeyListener.getInstance("-0123456789."));
        else
            changeValue.setKeyListener(DigitsKeyListener.getInstance("0123456789."));

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
                    float floatValue = Float.parseFloat(s.toString());
                    if (floatValue < minValue)
                        result = formatFloat(minValue);
                    else if (floatValue > maxValue)
                        result = formatFloat(maxValue);
                    else if (!result.endsWith(".") && !formatFloat(floatValue).equals(result))
                        result = formatFloat(floatValue);

                    if (!s.toString().equals(result)) {
                        changeValue.setText(result);
                    }
                    if (!s.toString().isEmpty())
                        seekBar.setProgress(Math.round(Float.parseFloat(result) - minValue) * digitsOffset);

                    float seekBarProgress = seekBar.getProgress() / digitsOffset + minValue;
                    if (Float.compare(seekBarProgress, Float.parseFloat(result)) != 0)
                        changeValue.setTextColor(Color.RED);
                    else
                        changeValue.setTextColor(Color.BLACK);

                    try {
                        value.put("value", Float.parseFloat(result));
                    } catch (Exception ignored) {
                    }
                }
            }
        });

        container.addView(seekBarContainer);
        if (value.has("value")) {
            changeValue.setText(value.getString("value"));
            seekBar.setProgress(Math.round(Float.parseFloat(value.getString("value")) - minValue) * digitsOffset);
        } else {
            seekBar.setProgress(0);
            changeValue.setText(formatFloat((seekBar.getProgress() / digitsOffset) + minValue));
        }
    }

    private void createMediaBlock(final JSONObject value, LinearLayout container) throws Exception {
        final LinearLayout mediaLayout = (LinearLayout) getActivity().getLayoutInflater().inflate(R.layout.media_layout, container, false);

        final ExpandableLayout expandableLayout = (ExpandableLayout) ((LinearLayout) mediaLayout.getChildAt(0)).getChildAt(1);
        LinearLayout buttonsContainer = (LinearLayout) expandableLayout.getChildAt(0);
        ((LinearLayout) mediaLayout.getChildAt(0)).getChildAt(0).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                expandableLayout.toggle();
            }
        });

        buttonsContainer.getChildAt(0).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkExternalPermissions()) {
                    currentMediaBlock = new Pair<>(mediaLayout, value);
                    Pair<Intent, File> intentFilePair = BitmapService.getPickImageIntent(getContext());
                    cameraImagePath = intentFilePair.second.getAbsolutePath();
                    startActivityForResult(intentFilePair.first, PHOTO_REQUEST_CODE);
                } else
                    requestExternalPermissions();
            }
        });

        buttonsContainer.getChildAt(1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkExternalPermissions()) {
                    currentMediaBlock = new Pair<>(mediaLayout, value);
                    Pair<Intent, File> intentFilePair = BitmapService.getPickVideoIntent(getContext());
                    cameraVideoPath = intentFilePair.second.getAbsolutePath();
                    startActivityForResult(intentFilePair.first, VIDEO_REQUEST_CODE);
                } else
                    requestExternalPermissions();
            }
        });

        buttonsContainer.getChildAt(2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkAudioPermissions()) {
                    currentMediaBlock = new Pair<>(mediaLayout, value);
                    Intent intent = new Intent(MediaStore.Audio.Media.RECORD_SOUND_ACTION);
                    startActivityForResult(intent, AUDIO_REQUEST_CODE);
                } else
                    requestAudioPermissions();
            }
        });

        buttonsContainer.getChildAt(3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkExternalPermissions()) {
                    currentMediaBlock = new Pair<>(mediaLayout, value);

                    Intent intent = new Intent("com.sec.android.app.myfiles.PICK_DATA");
                    intent.putExtra("CONTENT_TYPE", "*/*");
                    intent.addCategory(Intent.CATEGORY_DEFAULT);
                    startActivityForResult(intent, DOCUMENT_REQUEST_CODE);
                } else
                    requestExternalPermissions();
            }
        });

        container.addView(mediaLayout);

        if (value.has(MEDIA_PATH_JSON_ARRAY)) {
            currentMediaBlock = new Pair<>(mediaLayout, value);
            for (int j = 0; j < value.getJSONArray(MEDIA_PATH_JSON_ARRAY).length(); j++) {
                String mediaPath = value.getJSONArray(MEDIA_PATH_JSON_ARRAY).getString(j);
                String mimeType = Utils.getMimeType(mediaPath);
                if (mimeType.startsWith("image"))
                    new ProcessingBitmapTask(mediaPath, false).execute();
                else if (mimeType.startsWith("audio"))
                    createAudioPreview(mediaPath, false);
                else if (mimeType.startsWith("video"))
                    createVideoPreview(mediaPath, false);
                else
                    createDocumentPreview(mediaPath, false);
            }
        }
    }

    private void createSelectBtnContainer(final JSONObject value, LinearLayout container) throws Exception {
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
                    if (isChecked) {
                        for (CompoundButton compoundButton : buttons)
                            if (!compoundButton.equals(selectedButton)) {
                                compoundButton.setChecked(false);
                                ((TextView) ((FrameLayout) compoundButton.getParent()).getChildAt(1)).setTextColor(Color.parseColor("#4c3e60"));
                            } else
                                ((TextView) ((FrameLayout) compoundButton.getParent()).getChildAt(1)).setTextColor(Color.WHITE);

                        try {
                            String btnId = (String) selectedButton.getTag();
                            value.put("value", btnId);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
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

                FrameLayout selectBtnFrame = (FrameLayout) getActivity().getLayoutInflater().inflate(R.layout.select_btn, currentRow, false);
                RadioButton selectBtn = (RadioButton) selectBtnFrame.getChildAt(0);
                TextView selectBtnText = (TextView) selectBtnFrame.getChildAt(1);

                selectBtn.setOnCheckedChangeListener(radioButtonListener);
                selectBtn.setTag(option.getString("id"));
                buttons.add(selectBtn);

                selectBtn.setChecked(value.has("value") && value.getString("value").equals(option.getString("id")));

                if (option.has("caption"))
                    selectBtnText.setText(option.getString("caption"));
                else
                    selectBtnText.setText("Нет текста:(");

                currentRow.addView(selectBtnFrame);
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

    private boolean checkExternalPermissions() {
        int permissionCheckWrite = ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int permissionCheckRead = ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE);
        return (permissionCheckRead == PackageManager.PERMISSION_GRANTED && permissionCheckWrite == PackageManager.PERMISSION_GRANTED);
    }

    private boolean checkAudioPermissions() {
        return ContextCompat.checkSelfPermission(getContext(), Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
    }


    private void requestExternalPermissions() {
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
        }
    }

    private void requestAudioPermissions() {
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.RECORD_AUDIO}, 0);
        }
    }


    private FrameLayout createImgFrame(Bitmap photo) {
        LinearLayout imageContainer = (LinearLayout) ((HorizontalScrollView) currentMediaBlock.first.getChildAt(1)).getChildAt(0);
        FrameLayout imageFrame = (FrameLayout) getActivity().getLayoutInflater().inflate(R.layout.image_with_frame_layout, imageContainer, false);
        ImageView imageView = (ImageView) imageFrame.getChildAt(0);
        imageView.setImageBitmap(photo);

        final JSONObject currentValue = currentMediaBlock.second;
        final LinearLayout currentLayout = currentMediaBlock.first;
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Intent showPhotoIntent = new Intent(getContext(), ImageViewActivity.class);
                    JSONArray imageSrc = new JSONArray();
                    for (int i = 0; i < currentValue.getJSONArray(MEDIA_PATH_JSON_ARRAY).length(); i++)
                        if (Utils.isImage(currentValue.getJSONArray(MEDIA_PATH_JSON_ARRAY).getString(i)))
                            imageSrc.put(currentValue.getJSONArray(MEDIA_PATH_JSON_ARRAY).getString(i));

                    showPhotoIntent.putExtra("images", imageSrc.toString());
                    startActivityForResult(showPhotoIntent, IMAGE_SHOW_REQUEST_CODE);
                    currentMediaBlock = new Pair<>(currentLayout, currentValue);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        return imageFrame;
    }

    private void createDocumentPreview(String path, boolean copyToCache) {
        try {
            final String fileName = path.substring(path.lastIndexOf("/") + 1);
            File file = new File(path);
            String filePathInCache;

            if (copyToCache) {
                filePathInCache = getContext().getExternalCacheDir() + "/" + fileName;

                int i = 1;
                while (new File(filePathInCache).exists())
                    filePathInCache = filePathInCache.substring(0, filePathInCache.lastIndexOf(".")) + "(" + i++ + ")"
                            + filePathInCache.substring(filePathInCache.lastIndexOf("."));

                Utils.copy(file, new File(filePathInCache));
            } else
                filePathInCache = path;

            final String finalFilePathInCache = filePathInCache;
            final Uri fileUri = FileProvider.getUriForFile(getContext(), getContext().getApplicationContext().getPackageName() + ".provider", new File(filePathInCache));

            final LinearLayout fileContainer = (LinearLayout) currentMediaBlock.first.getChildAt(4);
            final LinearLayout fileLayout = (LinearLayout) getActivity().getLayoutInflater().inflate(R.layout.file_layout, fileContainer, false);

            ((TextView) fileLayout.getChildAt(1)).setText(filePathInCache.substring(filePathInCache.lastIndexOf("/") + 1));
            fileLayout.setTag(filePathInCache);
            fileLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        Intent viewIntent = new Intent(Intent.ACTION_VIEW);
                        viewIntent.setDataAndType(fileUri, Utils.getMimeType(finalFilePathInCache));
                        viewIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        startActivity(viewIntent);
                    } catch (Exception exc) {
                        exc.printStackTrace();
                        Toast.makeText(getContext(), "Нет приложения, которое может открыть этот файл", Toast.LENGTH_LONG).show();
                    }
                }
            });

            final JSONObject currentMediaObject = currentMediaBlock.second;
            fileLayout.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    showPopupMenu(view, new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            if (item.getItemId() == R.id.action_delete) {
                                deleteMediaPath(currentMediaObject, finalFilePathInCache);
                                fileContainer.removeView(fileLayout);
                            }
                            return true;
                        }
                    });
                    return true;
                }
            });

            if (copyToCache)
                addMediaPath(filePathInCache);

            fileContainer.addView(fileLayout);

        } catch (Exception exc) {
            Toast.makeText(getContext(), "Ошибка при добавлении файла: " + exc.getLocalizedMessage(), Toast.LENGTH_LONG).show();
        }

    }

    private void createAudioPreview(String path, boolean copyToCache) {
        try {
            Log.d("mojo-log", String.valueOf(new File(path).exists()));
            String audioPathInCache;

            if (copyToCache) {
                audioPathInCache = getContext().getExternalCacheDir() + "/" + System.currentTimeMillis() + path.substring(path.lastIndexOf("."));
                Utils.copy(new File(path), new File(audioPathInCache));
                audioPathInCache = new File(audioPathInCache).getAbsolutePath();
            } else
                audioPathInCache = path;

            final Uri fileUri = FileProvider.getUriForFile(getContext(), getContext().getApplicationContext().getPackageName() + ".provider", new File(audioPathInCache));

            final LinearLayout audioContainer = (LinearLayout) currentMediaBlock.first.getChildAt(3);
            final LinearLayout audioLayout = (LinearLayout) getActivity().getLayoutInflater().inflate(R.layout.audio_layout, audioContainer, false);

            String fileName = "Запись " + new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault()).format(
                    new Date(Long.parseLong(audioPathInCache.substring(audioPathInCache.lastIndexOf("/") + 1,
                            audioPathInCache.lastIndexOf("."))))) + audioPathInCache.substring(audioPathInCache.lastIndexOf("."));

            ((TextView) audioLayout.getChildAt(1)).setText(fileName);
            audioLayout.setTag(audioPathInCache);
            audioLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        Intent viewIntent = new Intent(Intent.ACTION_VIEW);
                        viewIntent.setDataAndType(fileUri, "audio/*");
                        viewIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        startActivity(viewIntent);
                    } catch (Exception exc) {
                        exc.printStackTrace();
                        Toast.makeText(getContext(), "Нет установленного аудиоплеера", Toast.LENGTH_LONG).show();
                    }
                }
            });

            final JSONObject currentMediaObject = currentMediaBlock.second;
            final String finalAudioPathInCache = audioPathInCache;
            audioLayout.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    showPopupMenu(view, new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            if (item.getItemId() == R.id.action_delete) {
                                deleteMediaPath(currentMediaObject, finalAudioPathInCache);
                                audioContainer.removeView(audioLayout);
                            }
                            return true;
                        }
                    });
                    return true;
                }
            });

            if (copyToCache)
                addMediaPath(audioPathInCache);

            audioContainer.addView(audioLayout);

        } catch (Exception exc) {
            Toast.makeText(getContext(), "Ошибка при добавлении аудио: " + exc.getLocalizedMessage(), Toast.LENGTH_LONG).show();
        }

    }

    private void createVideoPreview(final String path, boolean copyToCache) {
        try {
            String videoPathInCache;

            if (copyToCache) {
                videoPathInCache = getContext().getExternalCacheDir() + "/" + System.currentTimeMillis() + path.substring(path.lastIndexOf("."));
                Utils.copy(new File(path), new File(videoPathInCache));
                videoPathInCache = new File(videoPathInCache).getAbsolutePath();
            } else
                videoPathInCache = path;

            final Uri videoUri = FileProvider.getUriForFile(getContext(), getContext().getApplicationContext().getPackageName() + ".provider", new File(videoPathInCache));
            final LinearLayout videoContainer = (LinearLayout) ((HorizontalScrollView) currentMediaBlock.first.getChildAt(2)).getChildAt(0);

            final RelativeLayout videoPreview = (RelativeLayout) getActivity().getLayoutInflater().inflate(R.layout.video_preview_layout, videoContainer, false);
            Bitmap curThumb = ThumbnailUtils.createVideoThumbnail(videoPathInCache, MediaStore.Video.Thumbnails.FULL_SCREEN_KIND);
            ((ImageView) videoPreview.getChildAt(0)).setImageBitmap(curThumb);

            videoPreview.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        Intent viewIntent = new Intent(Intent.ACTION_VIEW, videoUri);
                        viewIntent.setDataAndType(videoUri, "video/*");
                        viewIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        startActivity(viewIntent);
                    } catch (Exception exc) {
                        exc.printStackTrace();
                        Toast.makeText(getContext(), "Нет установленного аудиоплеера", Toast.LENGTH_LONG).show();
                    }
                }
            });

            final JSONObject currentMediaObject = currentMediaBlock.second;
            final String finalVideoPathInCache = videoPathInCache;
            videoPreview.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    showPopupMenu(view, new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            if (item.getItemId() == R.id.action_delete) {
                                deleteMediaPath(currentMediaObject, finalVideoPathInCache);
                                videoContainer.removeView(videoPreview);
                            }
                            return true;
                        }
                    });
                    return true;
                }
            });

            videoContainer.addView(videoPreview);
            if (copyToCache)
                addMediaPath(videoPathInCache);

        } catch (Exception exc) {
            Toast.makeText(getContext(), "Ошибка при добавлении вижео: " + exc.getLocalizedMessage(), Toast.LENGTH_LONG).show();
        }

    }

    private void addMediaPath(String mediaPath) throws JSONException {
        if (currentMediaBlock.second.has(MEDIA_PATH_JSON_ARRAY))
            currentMediaBlock.second.getJSONArray(MEDIA_PATH_JSON_ARRAY).put(mediaPath);
        else {
            JSONArray mediaPaths = new JSONArray();
            mediaPaths.put(mediaPath);
            currentMediaBlock.second.put(MEDIA_PATH_JSON_ARRAY, mediaPaths);
        }
        saveTemplateState();
    }

    private void deleteMediaPath(JSONObject currentMediaBlock, String mediaPath) {
        try {
            if (currentMediaBlock.has(MEDIA_PATH_JSON_ARRAY))
                currentMediaBlock.put(MEDIA_PATH_JSON_ARRAY, Utils.removeItemWithValue(currentMediaBlock.getJSONArray(MEDIA_PATH_JSON_ARRAY), mediaPath));
            saveTemplateState();
            new File(mediaPath).delete();
        } catch (Exception exc) {
            exc.printStackTrace();
        }
    }

    private void showPopupMenu(View v, PopupMenu.OnMenuItemClickListener onMenuItemClickListener) {
        PopupMenu popupMenu = new PopupMenu(getContext(), v);
        popupMenu.inflate(R.menu.delete_menu);
        popupMenu.setOnMenuItemClickListener(onMenuItemClickListener);
        popupMenu.setGravity(Gravity.RIGHT);
        popupMenu.show();
    }

    private class GetTemplateTask extends AsyncTask<Void, Void, Integer> {
        private String templateId;

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
                SharedPreferences mSettings;
                mSettings = App.getContext().getSharedPreferences("templates", Context.MODE_PRIVATE);
                String templateJson = mSettings.getString(templateId + Data.currentUser.userName, "");
                if (!templateJson.equals("")) {
                    template = new JSONObject(templateJson);
                    return HttpURLConnection.HTTP_OK;
                } else {
                    String url = "/api/fs-mojo/get/template/" + templateId;
                    Response response = RequestService.createGetRequest(url);

                    if (response.code() == 200) {
                        String responseStr = response.body().string();
                        template = new JSONObject(responseStr);
                    }
                    return response.code();
                }
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
                startActivity(new Intent(getContext(), AuthActivity.class));
                getActivity().finish();
            } else {
                renderTemplate();
            }
        }
    }

    private class SendMediaTask extends AsyncTask<Void, Integer, Integer> {
        private final int SUCCESS = 0;
        private ArrayList<JSONObject> mediaObjects;
        private int successfullySentMediaCt, totalSize;

        SendMediaTask(ArrayList<JSONObject> mediaObjects) {
            this.mediaObjects = mediaObjects;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            totalSize = 0;

            try {
                for (JSONObject jsonObject : mediaObjects)
                    if (jsonObject.has(MEDIA_PATH_JSON_ARRAY))
                        for (int i = 0; i < jsonObject.getJSONArray(MEDIA_PATH_JSON_ARRAY).length(); i++)
                            if (!jsonObject.has("sent_medias") ||
                                    (jsonObject.has("sent_medias") && !Utils.containsValue(jsonObject.getJSONArray("sent_medias"), jsonObject.getJSONArray(MEDIA_PATH_JSON_ARRAY).getString(i))))
                                totalSize++;

            } catch (Exception exc) {
                exc.printStackTrace();
            }
            progressDialog.setMax(totalSize + 1);
            progressDialog.show();
        }

        @Override
        protected Integer doInBackground(Void... params) {
            try {
                String nodeId = "28e95811-cabb-49b2-b927-8d321327267c";

                successfullySentMediaCt = 0;
                for (JSONObject jsonObject : mediaObjects)
                    if (jsonObject.has(MEDIA_PATH_JSON_ARRAY))
                        for (int i = 0; i < jsonObject.getJSONArray(MEDIA_PATH_JSON_ARRAY).length(); i++)
                            if (!jsonObject.has("sent_medias") ||
                                    (jsonObject.has("sent_medias") && !Utils.containsValue(jsonObject.getJSONArray("sent_medias"), jsonObject.getJSONArray(MEDIA_PATH_JSON_ARRAY).getString(i)))) {
                                String mediaPath = jsonObject.getJSONArray(MEDIA_PATH_JSON_ARRAY).getString(i);
                                try {
                                    File mediaFile = new File(mediaPath);
                                    Response response = RequestService.createSendFileRequest("/api/fs/upload/binary/" + nodeId, mediaFile);

                                    if (response.code() == 200) {
                                        String mediaId = new JSONObject(response.body().string()).getJSONObject("entry").getString("id");

                                        successfullySentMediaCt++;
                                        if (jsonObject.has("sent_medias"))
                                            jsonObject.getJSONArray("sent_medias").put(mediaPath);
                                        else {
                                            JSONArray sentImages = new JSONArray();
                                            sentImages.put(mediaPath);
                                            jsonObject.put("sent_medias", sentImages);
                                        }

                                        if (jsonObject.has("values"))
                                            jsonObject.getJSONArray("values").put(mediaId);
                                        else {
                                            JSONArray values = new JSONArray();
                                            values.put(mediaId);
                                            jsonObject.put("values", values);
                                        }
                                    }

                                    Log.d("mojo-log", String.valueOf(response.code()));
                                } catch (Exception exc) {
                                    exc.printStackTrace();
                                }
                                publishProgress(i);
                            }
            } catch (Exception exc) {
                exc.printStackTrace();
            }
            return SUCCESS;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            progressDialog.setProgress(values[0] + 1);
        }

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);
            if (successfullySentMediaCt < totalSize) {
                saveTemplateState();
                if (progressDialog != null && progressDialog.isShowing())
                    progressDialog.dismiss();
                Toast.makeText(getContext(), R.string.error_not_all_images_were_sent, Toast.LENGTH_SHORT).show();
            } else {
                new CompleteTemplateTask().execute();
            }
        }
    }

    private class CompleteTemplateTask extends AsyncTask<Void, Integer, Integer> {
        private JSONObject currentTemplateCopy;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            try {
                currentTemplateCopy = new JSONObject(template.toString());
                Pair<Boolean, ArrayList<JSONObject>> result = checkIfTemplateIsFilled(currentTemplateCopy);
                ArrayList<JSONObject> mediaBlocks = result.second;
                for (JSONObject jsonObject : mediaBlocks) {
                    if (jsonObject.has("sent_medias"))
                        jsonObject.remove("sent_medias");

                    if (jsonObject.has(MEDIA_PATH_JSON_ARRAY))
                        jsonObject.remove(MEDIA_PATH_JSON_ARRAY);
                }

                currentTemplateCopy.put("executor", Data.currentUser.userName);
                if (!template.has("StartTime"))
                    currentTemplateCopy.put("StartTime", isoDateFormat.format(new Date()));
                if (!currentTemplateCopy.has("DueTime"))
                    currentTemplateCopy.put("DueTime", isoDateFormat.format(new Date()));
                if (!currentTemplateCopy.has("CompleteTime"))
                    currentTemplateCopy.put("CompleteTime", isoDateFormat.format(new Date()));

            } catch (Exception exc) {
                exc.printStackTrace();
            }
        }

        @Override
        protected Integer doInBackground(Void... params) {
            try {
                String nodeId = "28e95811-cabb-49b2-b927-8d321327267c";

                JSONObject jsonObject = new JSONObject();
                jsonObject.put("template", currentTemplateCopy.toString());
                Response response = RequestService.createPostRequest("/api/fs-mojo/create/" + nodeId + "/document", jsonObject.toString());
                if (response.code() == 200) {
                    String finishTaskJson = "{\n" +
                            "  \"action\" : \"complete\",\n" +
                            "  \"variables\" : []\n" +
                            "}";

                    response = RequestService.createPostRequest("/runtime/tasks/" + taskId, finishTaskJson);
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
            if (progressDialog != null && progressDialog.isShowing())
                progressDialog.dismiss();

            saveTemplateState();

            if (responseCode == null)
                Toast.makeText(getContext(), R.string.network_error, Toast.LENGTH_LONG).show();
            else if (responseCode == 401) {
                startActivity(new Intent(getContext(), AuthActivity.class));
                getActivity().finish();
            } else if (responseCode == 200) {
                ((TasksFragment) getActivity().getSupportFragmentManager().findFragmentByTag("tasks")).needUpdate = true;
                getActivity().getSupportFragmentManager().popBackStack();
            } else
                Toast.makeText(getContext(), R.string.unknown_error, Toast.LENGTH_LONG).show();

        }
    }

    private class ProcessingBitmapTask extends AsyncTask<Void, Void, Integer> {
        private String cachedImgPath;
        private Bitmap photo;
        private String picturePath;
        private boolean copyToChache;
        private String errorMessage;


        ProcessingBitmapTask(String picturePath, boolean withCopyInChache) {
            this.picturePath = picturePath;
            this.copyToChache = withCopyInChache;
        }

        @Override
        protected Integer doInBackground(Void... params) {
            int imgSize = Math.round(TypedValue.applyDimension
                    (TypedValue.COMPLEX_UNIT_DIP, 93, getResources().getDisplayMetrics()));

            try {
                final BitmapFactory.Options tmpOptions = new BitmapFactory.Options();
                final BitmapFactory.Options options = new BitmapFactory.Options();

                tmpOptions.inJustDecodeBounds = true;
                BitmapFactory.decodeFile(picturePath, tmpOptions);
                options.inSampleSize = BitmapService.calculateInSampleSize(tmpOptions, imgSize);
                options.inJustDecodeBounds = false;

                photo = BitmapFactory.decodeFile(picturePath, options);
                photo = BitmapService.modifyOrientation(photo, picturePath);

                if (copyToChache) {
                    options.inSampleSize = BitmapService.calculateInSampleSize(tmpOptions, 1200);
                    cachedImgPath = BitmapService.saveBitmapToCache(BitmapService.modifyOrientation(BitmapFactory.decodeFile(picturePath, options), picturePath));

                    if (cachedImgPath == null) {
                        errorMessage = "Не удалось сохранить изображение";
                        return null;
                    }
                } else
                    cachedImgPath = picturePath;

                return 1;

            } catch (Exception exc) {
                errorMessage = exc.getMessage();
                return null;
            }
        }

        @Override
        protected void onPostExecute(Integer resCode) {
            super.onPostExecute(resCode);
            try {
                if (resCode != null) {
                    FrameLayout imageContainer = createImgFrame(photo);
                    imageContainer.setTag(cachedImgPath);
                    ((LinearLayout) ((HorizontalScrollView) currentMediaBlock.first.getChildAt(1)).getChildAt(0)).addView(imageContainer);

                    if (copyToChache)
                        addMediaPath(cachedImgPath);

                } else
                    Toast.makeText(getContext(), "При обработке фото произошла ошибка: " + errorMessage, Toast.LENGTH_SHORT).show();
            } catch (Exception exc) {
                exc.printStackTrace();
                Toast.makeText(getContext(), "При обработке фото произошла ошибка: " + exc.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }
}
