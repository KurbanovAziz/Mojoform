package org.dev_alex.mojo_qa.mojo.fragments;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
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
import android.util.Base64;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.WebView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.github.gcacace.signaturepad.views.SignaturePad;

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
import org.dev_alex.mojo_qa.mojo.services.TokenService;
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
    private static String NODE_FOR_TASKS = "229ed0ec-3592-4788-87f0-6b0616599166";
    private final String NODE_FOR_FILES = "4899bb8e-0b0b-4889-82d6-eb16fcd6b90f";

    private final String MEDIA_PATH_JSON_ARRAY = "media_paths";
    private final String SIGNATURE_PREVIEW_JSON_ARRAY = "sign_state";
    private SimpleDateFormat isoDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.getDefault());
    private final int VIDEO_REQUEST_CODE = 10;
    private final int PHOTO_REQUEST_CODE = 11;
    private final int AUDIO_REQUEST_CODE = 12;
    private final int DOCUMENT_REQUEST_CODE = 13;
    private final int IMAGE_SHOW_REQUEST_CODE = 110;
    private final int SCAN_CODE_REQUEST_CODE = 120;


    private String templateId;
    private String taskId;
    private String siteId;
    private String initiator;
    private long dueDate;

    private View rootView;
    private ProgressDialog loopDialog;

    private ArrayList<Page> pages;
    private int currentPagePos;
    private JSONObject template;
    private EditText scanTo;

    private Pair<LinearLayout, JSONObject> currentMediaBlock;
    private String cameraImagePath;
    private String cameraVideoPath;

    private ProgressDialog progressDialog;


    public static TemplateFragment newInstance(String templateId, String taskId, String nodeForTasks,
                                               long dueDate, String siteId, String initiator) {
        Bundle args = new Bundle();
        args.putString("template_id", templateId);
        args.putLong("due_date", dueDate);
        args.putString("task_id", taskId);
        args.putString("initiator", initiator);
        args.putString("site_id", siteId);
        if (nodeForTasks != null && !nodeForTasks.isEmpty())
            args.putString("task_node_id", nodeForTasks);
        else
            args.putString("task_node_id", NODE_FOR_TASKS);


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
            dueDate = getArguments().getLong("due_date");
            siteId = getArguments().getString("site_id");
            initiator = getArguments().getString("initiator");
            NODE_FOR_TASKS = getArguments().getString("task_node_id");

            initDialog();
            setupHeader();
            Utils.setupCloseKeyboardUI(getActivity(), rootView);
            setListeners();

            new GetTemplateTask().execute();

        }
        return rootView;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PHOTO_REQUEST_CODE && resultCode == RESULT_OK) {
            if (new File(cameraImagePath).exists() || data == null)
                new ProcessingBitmapTask(cameraImagePath, true).execute();
            else {
                try {
                    String picturePath = Utils.getRealPathFromIntentData(getContext(), data.getData());
                    if (picturePath == null) {
                        Toast.makeText(getContext(), "что-то пошло не так", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String expansion = picturePath.substring(picturePath.lastIndexOf('.') + 1);
                    if (!expansion.equals("jpg") && !expansion.equals("png") && !expansion.equals("jpeg") && !expansion.equals("bmp")) {
                        Toast.makeText(getContext(), "Пожалуйста, выберите файл в формет jpg, png или bmp", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    new ProcessingBitmapTask(picturePath, true).execute();
                } catch (Exception exc) {
                    exc.printStackTrace();
                    try {
                        new ProcessingBitmapTask(cameraImagePath, true).execute();
                    } catch (Exception exc1) {
                        Toast.makeText(getContext(), "Не удалось прикрепить изображение", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }

        if (requestCode == VIDEO_REQUEST_CODE && resultCode == RESULT_OK) {
            if (new File(cameraVideoPath).exists() || data == null)
                createVideoPreview(cameraVideoPath, true);
            else {
                String videoPath = Utils.getRealPathFromIntentData(getContext(), data.getData());
                if (videoPath == null)
                    Toast.makeText(getContext(), "что-то пошло не так", Toast.LENGTH_SHORT).show();
                else
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

        if (requestCode == DOCUMENT_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            String documentPath;

            if (new File(data.getData().getPath()).exists())
                documentPath = data.getData().getPath();
            else
                documentPath = Utils.getPathFromUri(getContext(), data.getData());

            if (documentPath == null)
                Toast.makeText(getContext(), "что-то пошло не так", Toast.LENGTH_SHORT).show();
            else {
                String mimeType = Utils.getMimeType(documentPath);
                if (mimeType.startsWith("image"))
                    new ProcessingBitmapTask(documentPath, true).execute();
                else if (mimeType.startsWith("audio"))
                    createAudioPreview(documentPath, true);
                else if (mimeType.startsWith("video"))
                    createVideoPreview(documentPath, true);
                else
                    createDocumentPreview(documentPath, true);
            }
        }

        if (requestCode == AUDIO_REQUEST_CODE && resultCode == RESULT_OK) {
            String audioPath = Utils.getRealPathFromIntentData(getContext(), data.getData());
            if (audioPath == null)
                Toast.makeText(getContext(), "что-то пошло не так", Toast.LENGTH_SHORT).show();
            else
                createAudioPreview(audioPath, true);
        }

        if (requestCode == SCAN_CODE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                String contents = data.getStringExtra("SCAN_RESULT");
                if (scanTo != null)
                    scanTo.setText(contents);
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        saveTemplateState();
        Data.currentTaskId = "";
    }

    @Override
    public void onResume() {
        super.onResume();
        Data.currentTaskId = taskId;
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

        updateArrows();
    }

    private void updateArrows() {
        if (pages != null && !pages.isEmpty()) {
            rootView.findViewById(R.id.left_arrow).setVisibility(currentPagePos > 0 ? View.VISIBLE : View.INVISIBLE);
            rootView.findViewById(R.id.right_arrow).setVisibility(currentPagePos < pages.size() - 1 ? View.VISIBLE : View.INVISIBLE);

            if (currentPagePos == pages.size() - 1) {
                rootView.findViewById(R.id.left_space).setVisibility(View.GONE);
                rootView.findViewById(R.id.right_space).setVisibility(View.GONE);
                rootView.findViewById(R.id.finish_btn_container).setVisibility(View.VISIBLE);
            } else {
                rootView.findViewById(R.id.left_space).setVisibility(View.VISIBLE);
                rootView.findViewById(R.id.right_space).setVisibility(View.VISIBLE);
                rootView.findViewById(R.id.finish_btn_container).setVisibility(View.GONE);
            }
        } else {
            rootView.findViewById(R.id.left_space).setVisibility(View.GONE);
            rootView.findViewById(R.id.right_space).setVisibility(View.GONE);
            rootView.findViewById(R.id.finish_btn_container).setVisibility(View.VISIBLE);


            rootView.findViewById(R.id.left_arrow).setVisibility(View.INVISIBLE);
            rootView.findViewById(R.id.right_arrow).setVisibility(View.INVISIBLE);
        }
    }

    private void saveTemplateState() {
        try {
            if (!template.has("StartTime"))
                template.put("StartTime", isoDateFormat.format(new Date()));
            SharedPreferences mSettings;
            mSettings = App.getContext().getSharedPreferences("templates", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = mSettings.edit();

            editor.putString(taskId + Data.currentUser.userName, template.toString());
            editor.apply();
        } catch (Exception exc) {
            exc.printStackTrace();
        }
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
                    Utils.setupCloseKeyboardUI(getActivity(), rootContainer);
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
            Toast.makeText(getContext(), "Некорректный шаблон", Toast.LENGTH_SHORT).show();
            getActivity().getSupportFragmentManager().popBackStack();
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
            int paddings = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, getResources().getDisplayMetrics());
            final JSONObject value = dataJson.getJSONObject(i).getJSONObject(fields.get(i));

            switch (fields.get(i)) {
                case "category":
                    createCategory(value, container, offset);
                    break;

                case "question":
                    createSelectBtnContainer(value, container, offset);
                    break;

                case "lineedit":
                    final LinearLayout editTextSingleLineContainer = (LinearLayout) getActivity().getLayoutInflater().inflate(R.layout.lineedit, container, false);
                    ((ViewGroup) editTextSingleLineContainer.getChildAt(0)).getChildAt(1).setVisibility((value.has("is_required") && !value.getBoolean("is_required")) ? View.GONE : View.VISIBLE);

                    if (value.has("caption"))
                        ((TextView) ((ViewGroup) editTextSingleLineContainer.getChildAt(0)).getChildAt(0)).setText(value.getString("caption"));
                    else
                        ((TextView) ((ViewGroup) editTextSingleLineContainer.getChildAt(0)).getChildAt(0)).setText("Нет текста");

                    if (value.has("value"))
                        ((EditText) ((ViewGroup) editTextSingleLineContainer.getChildAt(1)).getChildAt(0)).setText(value.getString("value"));
                    ((EditText) ((ViewGroup) editTextSingleLineContainer.getChildAt(1)).getChildAt(0)).addTextChangedListener(new TextWatcher() {
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

                    ((ViewGroup) editTextSingleLineContainer.getChildAt(1)).getChildAt(1).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            scanQrCode(((EditText) ((ViewGroup) editTextSingleLineContainer.getChildAt(1)).getChildAt(0)));
                        }
                    });

                    ((ViewGroup) editTextSingleLineContainer.getChildAt(1)).getChildAt(2).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            scanBarCode(((EditText) ((ViewGroup) editTextSingleLineContainer.getChildAt(1)).getChildAt(0)));
                        }
                    });

                    container.addView(boxInContainerWithId(editTextSingleLineContainer, value.getString("id")));
                    break;

                case "textarea":
                    final LinearLayout editTextMultiLineContainer = (LinearLayout) getActivity().getLayoutInflater().inflate(R.layout.textarea, container, false);
                    ((ViewGroup) editTextMultiLineContainer.getChildAt(0)).getChildAt(1).setVisibility((value.has("is_required") && !value.getBoolean("is_required")) ? View.GONE : View.VISIBLE);

                    if (value.has("caption"))
                        ((TextView) ((ViewGroup) editTextMultiLineContainer.getChildAt(0)).getChildAt(0)).setText(value.getString("caption"));
                    else
                        ((TextView) ((ViewGroup) editTextMultiLineContainer.getChildAt(0)).getChildAt(0)).setText("Нет текста");

                    if (value.has("value"))
                        ((EditText) ((ViewGroup) editTextMultiLineContainer.getChildAt(1)).getChildAt(0)).setText(value.getString("value"));
                    ((EditText) ((ViewGroup) editTextMultiLineContainer.getChildAt(1)).getChildAt(0)).addTextChangedListener(new TextWatcher() {
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

                    ((ViewGroup) editTextMultiLineContainer.getChildAt(1)).getChildAt(1).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            scanQrCode(((EditText) ((ViewGroup) editTextMultiLineContainer.getChildAt(1)).getChildAt(0)));
                        }
                    });

                    ((ViewGroup) editTextMultiLineContainer.getChildAt(1)).getChildAt(2).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            scanBarCode(((EditText) ((ViewGroup) editTextMultiLineContainer.getChildAt(1)).getChildAt(0)));
                        }
                    });

                    container.addView(boxInContainerWithId(editTextMultiLineContainer, value.getString("id")));
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
                    else {
                        ((CheckBox) checkBoxContainer.getChildAt(0)).setChecked(true);
                        ((CheckBox) checkBoxContainer.getChildAt(0)).setChecked(false);
                    }

                    container.addView(boxInContainerWithId(checkBoxContainer, value.getString("id")));
                    break;

                case "slider":
                    createSeekBar(value, container);
                    break;

                case "photo":
                    createMediaBlock(value, container);
                    break;

                case "richedit":
                    if (value.has("caption")) {
                        TextView caption = new TextView(getContext());
                        caption.setText(value.getString("caption"));
                        caption.setTextColor(ContextCompat.getColor(getContext(), R.color.accent));
                        caption.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
                        container.addView(caption);
                        caption.setPadding(paddings, 0, paddings, 0);
                    }

                    if (value.has("html")) {
                        WebView richEdit = new WebView(getContext());
                        LinearLayout.LayoutParams richEditLayoutParams = new LinearLayout.LayoutParams
                                (ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

                        richEditLayoutParams.leftMargin = paddings;
                        richEditLayoutParams.rightMargin = paddings;
                        richEditLayoutParams.topMargin = paddings;
                        richEdit.setLayoutParams(richEditLayoutParams);

                        String mime = "text/html";
                        String encoding = "utf-8";

                        String html = "<html><head></head><body> " + value.getString("html") + " </body></html>";
                        html = html.replace(getString(R.string.host), "");
                        html = html.replace("/api", getString(R.string.host) + "/api");
                        html = Utils.formatHtmlImagesSize(html);

                        richEdit.getSettings().setJavaScriptEnabled(true);
                        String cookieString = "auth_token=" + TokenService.getToken();
                        CookieManager.getInstance().setCookie(getString(R.string.host) + "/", cookieString);
                        CookieManager.getInstance().setAcceptCookie(true);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                            CookieManager.getInstance().setAcceptThirdPartyCookies(richEdit, true);

                        richEdit.loadDataWithBaseURL("", html, mime, encoding, null);
                        container.addView(richEdit);
                    }
                    break;

                case "signature":
                    createSignature(value, container);
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

    private void scanQrCode(EditText scanTo) {
        try {
            this.scanTo = scanTo;
            Intent intent = new Intent("com.google.zxing.client.android.SCAN");
            intent.putExtra("SCAN_MODE", "QR_CODE_MODE"); // "PRODUCT_MODE for bar codes
            startActivityForResult(intent, SCAN_CODE_REQUEST_CODE);

        } catch (Exception e) {
            Uri marketUri = Uri.parse("market://details?id=com.google.zxing.client.android");
            Intent marketIntent = new Intent(Intent.ACTION_VIEW, marketUri);
            startActivity(marketIntent);
        }
    }

    private void scanBarCode(EditText scanTo) {
        try {
            this.scanTo = scanTo;
            Intent intent = new Intent("com.google.zxing.client.android.SCAN");
            intent.putExtra("SCAN_MODE", "PRODUCT_MODE");
            startActivityForResult(intent, SCAN_CODE_REQUEST_CODE);

        } catch (Exception e) {
            Uri marketUri = Uri.parse("market://details?id=com.google.zxing.client.android");
            Intent marketIntent = new Intent(Intent.ACTION_VIEW, marketUri);
            startActivity(marketIntent);
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
        categoryHeader.setTag(value.getString("id") + "_head");
        ((LayerDrawable) categoryHeader.getBackground()).findDrawableByLayerId(R.id.background).setColorFilter(color, PorterDuff.Mode.SRC_IN);

        if (value.has("name"))
            ((TextView) categoryHeader.getChildAt(1)).setText(value.getString("name"));
        else
            ((TextView) categoryHeader.getChildAt(1)).setText("Нет заголовка");

        LinearLayout expandableContent = (LinearLayout) getActivity().getLayoutInflater().inflate(R.layout.expandable_content, container, false);
        expandableContent.setTag(value.getString("id") + "_cont");
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

        if (value.has("measure"))
            ((TextView) ((LinearLayout) ((LinearLayout) seekBarContainer.getChildAt(3)).getChildAt(1)).getChildAt(1)).setText(value.getString("measure"));

        final SeekBar seekBar = ((SeekBar) seekBarContainer.getChildAt(1));
        final EditText changeValue = (EditText) ((LinearLayout) ((LinearLayout) seekBarContainer.getChildAt(3)).getChildAt(1)).getChildAt(0);

        final float minValue = (float) value.getDouble("min_value");
        final float maxValue = (float) value.getDouble("max_value");
        float step = (float) value.getDouble("step");
        String minValueStr = String.valueOf(minValue), maxValueStr = String.valueOf(maxValue), stepValueStr = String.valueOf(step);

        int digitsAfterPoint = Math.max((!minValueStr.contains(".")) ? 0 : minValueStr.substring(minValueStr.indexOf(".") + 1).length(),
                (!maxValueStr.contains(".")) ? 0 : maxValueStr.substring(maxValueStr.indexOf(".") + 1).length());
        digitsAfterPoint = Math.max(digitsAfterPoint,
                (!stepValueStr.contains(".")) ? 0 : stepValueStr.substring(stepValueStr.indexOf(".") + 1).length());
        final int finalDigitsAfterPoint = digitsAfterPoint;

        final int digitsOffset = (int) Math.pow(10, digitsAfterPoint);

        ((TextView) ((RelativeLayout) seekBarContainer.getChildAt(2)).getChildAt(0)).setText(formatFloat(minValue));
        ((TextView) ((RelativeLayout) seekBarContainer.getChildAt(2)).getChildAt(1)).setText(formatFloat(maxValue));

        final int increment = (int) (step * digitsOffset);
        final int max = (int) (((maxValue - minValue) * digitsOffset) / increment);

        seekBar.setMax(max);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            boolean trackByUser = false;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (trackByUser) {
                    float currentValue = ((float) progress / digitsOffset) * increment + minValue;
                    if (progress == max)
                        currentValue = maxValue;

                    currentValue = Utils.trimFloatAfterPointValue(currentValue, finalDigitsAfterPoint);
                    Log.d("jekaaa", progress + " floatValue = " + currentValue);

                    changeValue.setText(formatFloat(currentValue));
                }
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

        final int finalDigitsAfterPoint1 = digitsAfterPoint;
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
                    result = s.toString().trim();
                    if (result.equals("-"))
                        return;

                    float floatValue = Float.parseFloat(s.toString());
                   /* if (floatValue < minValue)
                        result = formatFloat(minValue);
                    else if (floatValue > maxValue)
                        result = formatFloat(maxValue);*/
                    if (!result.endsWith(".") && !formatFloat(floatValue).equals(result))
                        result = formatFloat(floatValue);

                    if (!s.toString().equals(result)) {
                        changeValue.setText(result);
                    }
                    if (!s.toString().isEmpty())
                        seekBar.setProgress((int) (((Float.parseFloat(result) - minValue) * digitsOffset) / increment));


                    float currentFloatValue = Float.parseFloat(result);
                    if (currentFloatValue < minValue || currentFloatValue > maxValue)
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

        container.addView(boxInContainerWithId(seekBarContainer, value.getString("id")));
        if (value.has("value")) {
            changeValue.setText(value.getString("value"));
            seekBar.setProgress(Math.round(Float.parseFloat(value.getString("value")) - minValue) * digitsOffset);
        } else {
            seekBar.setProgress(0);
            changeValue.setText(formatFloat((seekBar.getProgress() / digitsOffset) + minValue));
        }
    }

    private void createSignature(final JSONObject value, LinearLayout container) throws Exception {
        final LinearLayout signatureContainer = (LinearLayout) getActivity().getLayoutInflater().inflate(R.layout.signature_layout, container, false);
        final SignaturePad signaturePad = (SignaturePad) signatureContainer.getChildAt(1);

        ((ViewGroup) signatureContainer.getChildAt(0)).getChildAt(1).setVisibility((value.has("is_required") && !value.getBoolean("is_required")) ? View.GONE : View.VISIBLE);
        if (value.has("caption"))
            ((TextView) ((ViewGroup) signatureContainer.getChildAt(0)).getChildAt(0)).setText(value.getString("caption"));
        else
            ((TextView) ((ViewGroup) signatureContainer.getChildAt(0)).getChildAt(0)).setText("Нет текста");

        ((ViewGroup) signatureContainer.getChildAt(2)).getChildAt(1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signaturePad.clear();
            }
        });

        signaturePad.setOnSignedListener(new SignaturePad.OnSignedListener() {
            @Override
            public void onStartSigning() {

            }

            @Override
            public void onSigned() {
                try {
                    Bitmap bitmap = signaturePad.getSignatureBitmap();
                    value.put("sign_state", BitmapService.getBitmapBytesEncodedBase64(bitmap));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onClear() {
                try {
                    Bitmap bitmap = signaturePad.getSignatureBitmap();
                    value.put("sign_state", BitmapService.getBitmapBytesEncodedBase64(bitmap));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        container.addView(boxInContainerWithId(signatureContainer, "id"));

        if (value.has(SIGNATURE_PREVIEW_JSON_ARRAY)) {
            String imageEncoded = value.getString(SIGNATURE_PREVIEW_JSON_ARRAY);
            byte[] decodedByte = Base64.decode(imageEncoded, 0);
            Bitmap bitmap = BitmapFactory.decodeByteArray(decodedByte, 0, decodedByte.length);
            if (bitmap != null)
                signaturePad.setSignatureBitmap(bitmap);
        }
    }

    private void createMediaBlock(final JSONObject value, LinearLayout container) throws Exception {
        final LinearLayout mediaLayout = (LinearLayout) getActivity().getLayoutInflater().inflate(R.layout.media_layout, container, false);


        ((ViewGroup) ((ViewGroup) mediaLayout.getChildAt(0)).getChildAt(0)).getChildAt(2)
                .setVisibility((value.has("is_required") && !value.getBoolean("is_required")) ? View.GONE : View.VISIBLE);

        if (value.has("caption"))
            ((TextView) ((ViewGroup) ((ViewGroup) mediaLayout.getChildAt(0)).getChildAt(0)).getChildAt(1)).setText(value.getString("caption"));

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
                    try {
                        currentMediaBlock = new Pair<>(mediaLayout, value);
                        Pair<Intent, File> intentFilePair = BitmapService.getPickImageIntent(getContext());
                        cameraImagePath = intentFilePair.second.getAbsolutePath();
                        startActivityForResult(intentFilePair.first, PHOTO_REQUEST_CODE);
                    } catch (Exception exc) {
                        Toast.makeText(getContext(), "У вас нет камеры", Toast.LENGTH_SHORT).show();
                    }
                } else
                    requestExternalPermissions();
            }
        });

        buttonsContainer.getChildAt(1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkExternalPermissions()) {
                    try {
                        currentMediaBlock = new Pair<>(mediaLayout, value);
                        Pair<Intent, File> intentFilePair = BitmapService.getPickVideoIntent(getContext());
                        cameraVideoPath = intentFilePair.second.getAbsolutePath();
                        startActivityForResult(intentFilePair.first, VIDEO_REQUEST_CODE);
                    } catch (Exception exc) {
                        Toast.makeText(getContext(), "У вас нет камеры", Toast.LENGTH_SHORT).show();
                    }
                } else
                    requestExternalPermissions();
            }
        });

        buttonsContainer.getChildAt(2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkAudioPermissions()) {
                    try {
                        currentMediaBlock = new Pair<>(mediaLayout, value);
                        Intent intent = new Intent(MediaStore.Audio.Media.RECORD_SOUND_ACTION);
                        startActivityForResult(intent, AUDIO_REQUEST_CODE);
                    } catch (Exception exc) {
                        Toast.makeText(getContext(), "У вас нет приложения для записи аудио", Toast.LENGTH_SHORT).show();
                    }
                } else
                    requestAudioPermissions();
            }
        });

        buttonsContainer.getChildAt(3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkExternalPermissions()) {
                    try {
                        currentMediaBlock = new Pair<>(mediaLayout, value);

                        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                        intent.setType("*/*");
                        intent.addCategory(Intent.CATEGORY_OPENABLE);

                        Intent sIntent = new Intent("com.sec.android.app.myfiles.PICK_DATA");
                        sIntent.addCategory(Intent.CATEGORY_DEFAULT);

                        Intent chooserIntent;
                        if (getActivity().getPackageManager().resolveActivity(sIntent, 0) != null) {
                            // it is device with samsung file manager
                            chooserIntent = Intent.createChooser(sIntent, "Выбрать файл");
                            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{intent});
                        } else
                            chooserIntent = Intent.createChooser(intent, "Выбрать файл");

                        startActivityForResult(chooserIntent, DOCUMENT_REQUEST_CODE);

                    } catch (Exception exc) {
                        Toast.makeText(getContext(), "У вас нет файлового менеджера, чтобы выбрать файл", Toast.LENGTH_SHORT).show();
                    }
                } else
                    requestExternalPermissions();
            }
        });

        container.addView(boxInContainerWithId(mediaLayout, value.getString("id")));

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

    private void createSelectBtnContainer(final JSONObject value, LinearLayout container, final int offset) throws Exception {
        boolean isList = !(value.has("typeView") && value.getString("typeView").equals("list"));

        final ScrollView listSelectBtnLayout = (ScrollView) getActivity().getLayoutInflater()
                .inflate(R.layout.select_btn_popup_layout, (ViewGroup) rootView.findViewById(R.id.select_btn_popup), false);
        final LinearLayout listSelectBtnContainer = (LinearLayout) listSelectBtnLayout.findViewById(R.id.list_select_btn_container);

        LinearLayout selectBtnLayout = (LinearLayout) getActivity().getLayoutInflater().inflate(R.layout.select_layout, container, false);
        final LinearLayout selectBtnContainer = (LinearLayout) selectBtnLayout.getChildAt(1);
        TextView showPopupSelectListBtn = (TextView) selectBtnLayout.getChildAt(2);
        final LinearLayout optionalContainer = (LinearLayout) selectBtnLayout.getChildAt(3);

        ((ViewGroup) selectBtnLayout.getChildAt(0)).getChildAt(1).setVisibility(View.VISIBLE);

        if (value.has("caption"))
            ((TextView) ((ViewGroup) selectBtnLayout.getChildAt(0)).getChildAt(0)).setText(value.getString("caption"));
        else
            ((TextView) ((ViewGroup) selectBtnLayout.getChildAt(0)).getChildAt(0)).setText("Нет текста");

        if (value.has("options")) {
            int maxAnswersCt = 1;
            if (value.has("answers_count") && value.getInt("answers_count") > 0)
                maxAnswersCt = value.getInt("answers_count");
            if (value.has("answers_count") && value.getInt("answers_count") < 0)
                maxAnswersCt = 100500;

            final ArrayList<Pair<CheckBox, JSONObject>> buttons = new ArrayList<>();
            final int finalMaxAnswersCt = maxAnswersCt;
            CompoundButton.OnCheckedChangeListener checkButtonListener = new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton selectedButton, boolean isChecked) {
                    try {
                        if (isChecked) {
                            for (Pair<CheckBox, JSONObject> pair : buttons)
                                if (pair.first.equals(selectedButton))
                                    styleSelectBtn(selectedButton, true, pair.second);

                            JSONArray selectedBtnIds = new JSONArray();
                            String btnId = (String) selectedButton.getTag();
                            selectedBtnIds.put(btnId);
                            int currentlySelected = 1;

                            for (Pair<CheckBox, JSONObject> pair : buttons) {
                                CompoundButton compoundButton = pair.first;

                                if (!compoundButton.equals(selectedButton)) {
                                    if (compoundButton.isChecked()) {
                                        currentlySelected++;
                                        if (currentlySelected > finalMaxAnswersCt)
                                            compoundButton.setChecked(false);
                                        else
                                            selectedBtnIds.put(compoundButton.getTag());
                                    }
                                }
                            }

                            value.put("values", selectedBtnIds);
                        } else {
                            styleSelectBtn(selectedButton, false, null);

                            if (value.has("values"))
                                value.put("values", Utils.removeItemWithValue(value.getJSONArray("values"), (String) selectedButton.getTag()));
                        }

                        optionalContainer.removeAllViewsInLayout();
                        optionalContainer.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                        if (value.has("optionals") && value.getJSONArray("optionals").length() > 0) {
                            JSONArray optionals = value.getJSONArray("optionals");
                            for (int i = 0; i < optionals.length(); i++) {
                                JSONObject optional = optionals.getJSONObject(i).getJSONObject("optional");
                                if (optional.has("keys") && optional.getJSONArray("keys").length() > 0 && value.has("values")
                                        && Utils.containsAllValues(value.getJSONArray("values"), optional.getJSONArray("keys"))) {
                                    fillContainer(optionalContainer, optional.getJSONArray("items"), offset + 1);
                                }
                            }
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };

            JSONArray options = value.getJSONArray("options");
            for (int j = 0; j < options.length(); j++) {
                JSONObject option = options.getJSONObject(j);
                FrameLayout selectBtnFrame = (FrameLayout) getActivity().getLayoutInflater().inflate(R.layout.select_btn, selectBtnContainer, false);

                final CheckBox selectBtn = (CheckBox) selectBtnFrame.getChildAt(0);
                TextView selectBtnText = (TextView) selectBtnFrame.getChildAt(2);
                if (option.has("caption"))
                    selectBtnText.setText(option.getString("caption"));
                else
                    selectBtnText.setText("Нет текста:(");

                if (j == 0) {
                    if (isList)
                        ((LinearLayout.LayoutParams) selectBtnFrame.getLayoutParams()).topMargin =
                                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 25, getResources().getDisplayMetrics());
                    else
                        ((LinearLayout.LayoutParams) selectBtnFrame.getLayoutParams()).topMargin = 0;
                }

                if (isList)
                    listSelectBtnContainer.addView(selectBtnFrame);
                else
                    selectBtnContainer.addView(selectBtnFrame);

                buttons.add(new Pair<>(selectBtn, option));
                selectBtn.setTag(option.getString("id"));
                selectBtn.setChecked(value.has("values") && Utils.containsValue(value.getJSONArray("values"), option.getString("id")));
                styleSelectBtn(selectBtn, selectBtn.isChecked(), option);
                selectBtn.setOnCheckedChangeListener(checkButtonListener);
            }

            optionalContainer.removeAllViewsInLayout();
            optionalContainer.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            if (value.has("optionals") && value.getJSONArray("optionals").length() > 0) {
                JSONArray optionals = value.getJSONArray("optionals");
                for (int i = 0; i < optionals.length(); i++) {
                    JSONObject optional = optionals.getJSONObject(i).getJSONObject("optional");
                    if (optional.has("keys") && optional.getJSONArray("keys").length() > 0 && value.has("values")
                            && Utils.containsAllValues(value.getJSONArray("values"), optional.getJSONArray("keys"))) {
                        fillContainer(optionalContainer, optional.getJSONArray("items"), offset + 1);
                    }
                }
            }
        }

        showPopupSelectListBtn.setVisibility(isList ? View.VISIBLE : View.GONE);
        if (isList) {
            applySelectResultsInListView(value, selectBtnContainer);

            showPopupSelectListBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        ((ViewGroup) rootView.findViewById(R.id.select_btn_popup)).removeAllViewsInLayout();
                        ((ViewGroup) rootView.findViewById(R.id.select_btn_popup)).addView(listSelectBtnLayout);
                        JSONArray valueBackups = value.has("values") ? value.getJSONArray("values") : new JSONArray();
                        value.put("values_backup", valueBackups);
                    } catch (Exception exc) {
                        exc.printStackTrace();
                    }
                }
            });
            if (value.has("caption"))
                ((TextView) listSelectBtnLayout.findViewById(R.id.select_layout_caption)).setText(value.getString("caption"));
            else
                ((TextView) listSelectBtnLayout.findViewById(R.id.select_layout_caption)).setText("Нет заголовка");

            listSelectBtnLayout.findViewById(R.id.select_layout_close).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        ((ViewGroup) rootView.findViewById(R.id.select_btn_popup)).removeAllViewsInLayout();
                        value.put("values", value.getJSONArray("values_backup"));
                    } catch (Exception exc) {
                        exc.printStackTrace();
                    }
                }
            });

            listSelectBtnLayout.findViewById(R.id.select_layout_close).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        ((ViewGroup) rootView.findViewById(R.id.select_btn_popup)).removeAllViewsInLayout();
                        value.put("values", value.getJSONArray("values_backup"));
                    } catch (Exception exc) {
                        exc.printStackTrace();
                    }
                }
            });

            listSelectBtnLayout.findViewById(R.id.select_layout_accept).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        ((ViewGroup) rootView.findViewById(R.id.select_btn_popup)).removeAllViewsInLayout();
                        applySelectResultsInListView(value, selectBtnContainer);
                    } catch (Exception exc) {
                        exc.printStackTrace();
                    }
                }
            });
        }

        container.addView(boxInContainerWithId(selectBtnLayout, value.getString("id")));
    }

    private void applySelectResultsInListView(JSONObject value, LinearLayout answersContainer) {
        try {
            boolean isFirst = true;
            answersContainer.removeAllViewsInLayout();
            JSONArray options = value.getJSONArray("options");
            if (value.has("values")) {
                for (int i = 0; i < options.length(); i++) {
                    JSONObject option = options.getJSONObject(i);
                    if (Utils.containsValue(value.getJSONArray("values"), option.getString("id"))) {
                        FrameLayout selectBtnFrame = (FrameLayout) getActivity().getLayoutInflater().inflate(R.layout.select_btn, answersContainer, false);
                        CheckBox selectBtn = (CheckBox) selectBtnFrame.getChildAt(0);
                        TextView selectBtnText = (TextView) selectBtnFrame.getChildAt(2);

                        if (option.has("caption"))
                            selectBtnText.setText(option.getString("caption"));
                        else
                            selectBtnText.setText("Нет текста:(");

                        if (isFirst) {
                            ((LinearLayout.LayoutParams) selectBtnFrame.getLayoutParams()).topMargin = 0;
                            isFirst = false;
                        }

                        styleSelectBtn(selectBtn, true, option);
                        answersContainer.addView(selectBtnFrame);
                    }
                }
            }
        } catch (Exception exc) {
            exc.printStackTrace();
        }
    }

    private void styleSelectBtn(CompoundButton checkButton, boolean isChecked, JSONObject buttonJson) {
        try {
            GradientDrawable bgShape = (GradientDrawable) ContextCompat.getDrawable(getContext(), R.drawable.shape_white_oval).mutate();

            if (isChecked) {
                ((TextView) ((FrameLayout) checkButton.getParent()).getChildAt(2)).setTextColor(Color.WHITE);

                if (buttonJson.has("color"))
                    bgShape.setColor(Color.parseColor(buttonJson.getString("color")));
                else
                    bgShape.setColor(Color.parseColor("#c99fe3"));
            } else {
                bgShape.setColor(Color.WHITE);
                ((TextView) ((FrameLayout) checkButton.getParent()).getChildAt(2)).setTextColor(Color.parseColor("#4c3e60"));
            }


            ((FrameLayout) checkButton.getParent()).getChildAt(1).setBackground(bgShape);
        } catch (Exception exc) {
            exc.printStackTrace();
        }
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

    private FrameLayout boxInContainerWithId(ViewGroup content, String tag) {
        int topAndBottomPaddings = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, getResources().getDisplayMetrics());
        FrameLayout boxLayout = new FrameLayout(getContext());
        boxLayout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        boxLayout.addView(content);
        boxLayout.setTag(tag);
        boxLayout.setPadding(0, topAndBottomPaddings, 0, topAndBottomPaddings);
        return boxLayout;
    }

    private void setBlockMarkedAsRequired(String tag) {
        ViewGroup container = null;
        for (Page page : pages) {
            container = (ViewGroup) page.layout.findViewWithTag(tag);
            if (container != null)
                break;
        }
        if (container != null)
            container.setBackgroundColor(Color.parseColor("#FEDADA"));
    }

    private void setCategoryMarkedAsRequired(String tag) {
        ViewGroup categoryHeader = getCategoryByTag(tag + "_head");
        ViewGroup categoryContainer = getCategoryByTag(tag + "_cont");

        if (categoryHeader != null)
            ((LayerDrawable) categoryHeader.getBackground()).findDrawableByLayerId(R.id.background).setColorFilter(Color.parseColor("#FEDADA"), PorterDuff.Mode.SRC_IN);

        if (categoryContainer != null)
            ((LayerDrawable) categoryContainer.getBackground()).findDrawableByLayerId(R.id.background).setColorFilter(Color.parseColor("#FEDADA"), PorterDuff.Mode.SRC_IN);
    }

    private void resetCategoryColor(String tag, int offset) {
        ViewGroup categoryHeader = getCategoryByTag(tag + "_head");
        ViewGroup categoryContainer = getCategoryByTag(tag + "_cont");

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

        if (categoryHeader != null)
            ((LayerDrawable) categoryHeader.getBackground()).findDrawableByLayerId(R.id.background).setColorFilter(color, PorterDuff.Mode.SRC_IN);

        if (categoryContainer != null)
            ((LayerDrawable) categoryContainer.getBackground()).findDrawableByLayerId(R.id.background).setColorFilter(color, PorterDuff.Mode.SRC_IN);
    }

    private ViewGroup getCategoryByTag(String tag) {
        ViewGroup container = null;
        for (Page page : pages) {
            container = (ViewGroup) page.layout.findViewWithTag(tag);
            if (container != null)
                return container;
        }
        return null;
    }

    private void resetRequiredBlock(String tag) {
        ViewGroup container = null;
        for (Page page : pages) {
            container = (ViewGroup) page.layout.findViewWithTag(tag);
            if (container != null)
                break;
        }
        if (container != null)
            container.setBackgroundColor(Color.TRANSPARENT);
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
                String templateJson = mSettings.getString(taskId + Data.currentUser.userName, "");
                if (!templateJson.equals("")) {
                    template = new JSONObject(templateJson);
                    return HttpURLConnection.HTTP_OK;
                } else {
                    String url = "/api/fs-mojo/get/template/" + templateId;
                    Response response = RequestService.createGetRequest(url);

                    if (response.code() == 200) {
                        String responseStr = response.body().string();
                        template = new JSONObject(responseStr);
                        template.put("StartTime", isoDateFormat.format(new Date()));
                        template.put("DueTime", isoDateFormat.format(new Date(dueDate)));
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
            try {
                if (loopDialog != null && loopDialog.isShowing())
                    loopDialog.dismiss();

                if (responseCode == null) {
                    Toast.makeText(getContext(), R.string.network_error, Toast.LENGTH_LONG).show();
                    getActivity().getSupportFragmentManager().popBackStack();
                } else if (responseCode == 401) {
                    startActivity(new Intent(getContext(), AuthActivity.class));
                    getActivity().finish();
                } else if (responseCode == 200) {
                    renderTemplate();
                } else {
                    Toast.makeText(getContext(), R.string.unknown_error, Toast.LENGTH_LONG).show();
                    getActivity().getSupportFragmentManager().popBackStack();
                }
            } catch (Exception exc) {
                exc.printStackTrace();
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
                        for (int i = 0; i < jsonObject.getJSONArray(MEDIA_PATH_JSON_ARRAY).length(); i++) {
                            if (!jsonObject.has("sent_medias") ||
                                    (jsonObject.has("sent_medias") && !Utils.containsValue(jsonObject.getJSONArray("sent_medias"), jsonObject.getJSONArray(MEDIA_PATH_JSON_ARRAY).getString(i))))
                                totalSize++;
                        }
                    else if (jsonObject.has(SIGNATURE_PREVIEW_JSON_ARRAY))
                        if (!jsonObject.has("was_sent"))
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
                int sentCt = 0;

                successfullySentMediaCt = 0;
                for (JSONObject jsonObject : mediaObjects)
                    if (jsonObject.has(MEDIA_PATH_JSON_ARRAY))
                        for (int i = 0; i < jsonObject.getJSONArray(MEDIA_PATH_JSON_ARRAY).length(); i++) {
                            if (!jsonObject.has("sent_medias") ||
                                    (jsonObject.has("sent_medias") && !Utils.containsValue(jsonObject.getJSONArray("sent_medias"), jsonObject.getJSONArray(MEDIA_PATH_JSON_ARRAY).getString(i)))) {
                                String mediaPath = jsonObject.getJSONArray(MEDIA_PATH_JSON_ARRAY).getString(i);
                                try {
                                    File mediaFile = new File(mediaPath);
                                    Response response = RequestService.createSendFileRequest("/api/fs/upload/binary/" + NODE_FOR_FILES, mediaFile);

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
                                publishProgress(sentCt++);
                            }
                        }
                    else if (jsonObject.has(SIGNATURE_PREVIEW_JSON_ARRAY) && !jsonObject.has("was_sent")) {
                        String imageEncoded = jsonObject.getString(SIGNATURE_PREVIEW_JSON_ARRAY);
                        byte[] decodedByte = Base64.decode(imageEncoded, 0);
                        Bitmap bitmap = BitmapFactory.decodeByteArray(decodedByte, 0, decodedByte.length);
                        if (bitmap != null) {
                            File mediaFile = BitmapService.saveBitmapToFile(getContext(), bitmap);
                            Response response = RequestService.createSendFileRequest("/api/fs/upload/binary/" + NODE_FOR_FILES, mediaFile);
                            if (response.code() == 200) {
                                mediaFile.delete();
                                String mediaId = new JSONObject(response.body().string()).getJSONObject("entry").getString("id");
                                successfullySentMediaCt++;
                                jsonObject.put("value", mediaId);
                                jsonObject.put("was_sent", true);
                            }
                            Log.d("mojo-log", String.valueOf(response.code()));
                        }
                        publishProgress(sentCt++);
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
            try {
                if (successfullySentMediaCt < totalSize) {
                    saveTemplateState();
                    if (progressDialog != null && progressDialog.isShowing())
                        progressDialog.dismiss();
                    Toast.makeText(getContext(), R.string.error_not_all_images_were_sent, Toast.LENGTH_SHORT).show();
                } else
                    new CompleteTemplateTask().execute();
            } catch (Exception exc) {
                exc.printStackTrace();
            }
        }
    }

    private class CompleteTemplateTask extends AsyncTask<Void, Integer, Integer> {
        private JSONObject resultJson;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            try {
                resultJson = new JSONObject();
                JSONArray values = getTemplateElementValues(template);
                resultJson.put("values", values);

                resultJson.put("template_id", templateId);
                resultJson.put("name", template.getString("name"));
                resultJson.put("executor", Data.currentUser.userName);
                if (template.has("StartTime"))
                    resultJson.put("StartTime", template.getString("StartTime"));
                else
                    resultJson.put("StartTime", isoDateFormat.format(new Date()));

                if (template.has("DueTime"))
                    resultJson.put("DueTime", template.getString("DueTime"));
                else
                    resultJson.put("DueTime", isoDateFormat.format(new Date()));

                resultJson.put("initiator", "admin");
                resultJson.put("site_id", "gzp");
                resultJson.put("CompleteTime", isoDateFormat.format(new Date()));

                Log.d("mojo-log", "result template: " + resultJson.toString());

            } catch (Exception exc) {
                exc.printStackTrace();
            }
        }

        @Override
        protected Integer doInBackground(Void... params) {
            try {
                Response response = RequestService.createPostRequest("/api/fs-mojo/create/" + NODE_FOR_TASKS + "/document", resultJson.toString());
                String responseBody = response.body().string();
                if (response.code() == 201 || response.code() == 200 || response.code() == 409) {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("action", "complete");
                    jsonObject.put("variables", new JSONArray());

                    response = RequestService.createPostRequestWithCustomUrl(getString(R.string.tasks_host) + "/runtime/tasks/" + taskId, jsonObject.toString());
                    int responseCode = response.code();
                    Log.d("mojo-log", "task complete response code: " + responseCode);
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
            try {
                if (progressDialog != null && progressDialog.isShowing())
                    progressDialog.dismiss();

                saveTemplateState();

                if (responseCode == null)
                    Toast.makeText(getContext(), R.string.network_error, Toast.LENGTH_LONG).show();
                else if (responseCode == 401) {
                    startActivity(new Intent(getContext(), AuthActivity.class));
                    getActivity().finish();
                } else if (responseCode == 200) {
                    SharedPreferences mSettings;
                    mSettings = App.getContext().getSharedPreferences("templates", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = mSettings.edit();
                    editor.putString(taskId + Data.currentUser.userName, "");
                    editor.apply();
                    ((TasksFragment) getActivity().getSupportFragmentManager().findFragmentByTag("tasks")).needUpdate = true;
                    getActivity().getSupportFragmentManager().popBackStack();
                } else
                    Toast.makeText(getContext(), R.string.unknown_error, Toast.LENGTH_LONG).show();
            } catch (Exception exc) {
                exc.printStackTrace();
            }
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


    private Pair<Boolean, ArrayList<JSONObject>> checkIfTemplateIsFilled(JSONObject template) {
        try {
            ArrayList<JSONObject> photoObjects = new ArrayList<>();
            boolean totalResult = true;

            if (template != null) {
                JSONArray pagesJson = template.getJSONArray("items");
                for (int i = 0; i < pagesJson.length(); i++) {
                    JSONObject pageJson = pagesJson.getJSONObject(i).getJSONObject("page");
                    if (pageJson.has("items")) {
                        Pair<Boolean, ArrayList<JSONObject>> result = checkIfContainerIsFilled(pageJson.getJSONArray("items"), 0);
                        if (!result.first) {
                            totalResult = false;
                            ((LinearLayout) rootView.findViewById(R.id.page_container)).getChildAt(i).setBackgroundColor(Color.parseColor("#F2C7C6"));
                            ((LinearLayout) rootView.findViewById(R.id.page_container)).getChildAt(i).setAlpha(1);
                        } else
                            photoObjects.addAll(result.second);
                    }
                }
                if (totalResult)
                    return new Pair<>(true, photoObjects);
            }
        } catch (Exception exc) {
            exc.printStackTrace();
        }
        return new Pair<>(false, null);
    }

    private Pair<Boolean, ArrayList<JSONObject>> checkIfContainerIsFilled(JSONArray dataJson, int offset) throws Exception {
        boolean totalResult = true;
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
            resetRequiredBlock(value.getString("id"));
            resetCategoryColor(value.getString("id"), offset);

            switch (fields.get(i)) {
                case "category":
                    Pair<Boolean, ArrayList<JSONObject>> result = checkIfContainerIsFilled(value.getJSONArray("items"), offset + 1);
                    if (!result.first) {
                        totalResult = false;
                        setCategoryMarkedAsRequired(value.getString("id"));
                    } else
                        photoObjects.addAll(result.second);
                    break;

                case "question":
                    if (!value.has("values") || value.has("values") && value.getJSONArray("values").length() == 0) {
                        totalResult = false;
                        setBlockMarkedAsRequired(value.getString("id"));
                    }

                    if (value.has("optionals") && value.getJSONArray("optionals").length() > 0) {
                        JSONArray optionals = value.getJSONArray("optionals");
                        for (int j = 0; j < optionals.length(); j++) {
                            JSONObject optional = optionals.getJSONObject(j).getJSONObject("optional");
                            if (optional.has("keys") && optional.getJSONArray("keys").length() > 0 && value.has("values")
                                    && Utils.containsAllValues(value.getJSONArray("values"), optional.getJSONArray("keys"))) {

                                Pair<Boolean, ArrayList<JSONObject>> optionalResult = checkIfContainerIsFilled(optional.getJSONArray("items"), offset + 1);
                                if (!optionalResult.first) {
                                    totalResult = false;
                                } else
                                    photoObjects.addAll(optionalResult.second);
                            }
                        }
                    }

                    break;

                case "photo":
                    if ((!value.has(MEDIA_PATH_JSON_ARRAY) || value.has(MEDIA_PATH_JSON_ARRAY) && value.getJSONArray(MEDIA_PATH_JSON_ARRAY).length() == 0)
                            && !(value.has("is_required") && !value.getBoolean("is_required"))) {
                        totalResult = false;
                        setBlockMarkedAsRequired(value.getString("id"));
                    } else
                        photoObjects.add(value);
                    break;

                case "text":
                    break;

                case "lineedit":
                    if (!value.has("value") && !(value.has("is_required") && !value.getBoolean("is_required"))) {
                        totalResult = false;
                        setBlockMarkedAsRequired(value.getString("id"));
                    }
                    if (value.has("value") && value.getString("value").trim().isEmpty()) {
                        totalResult = false;
                        setBlockMarkedAsRequired(value.getString("id"));
                    }

                    break;

                case "textarea":
                    if (!value.has("value") && !(value.has("is_required") && !value.getBoolean("is_required"))) {
                        totalResult = false;
                        setBlockMarkedAsRequired(value.getString("id"));
                    }
                    if (value.has("value") && value.getString("value").trim().isEmpty()) {
                        totalResult = false;
                        setBlockMarkedAsRequired(value.getString("id"));
                    }
                    break;

                case "checkbox":
                    if (!value.has("value") && !(value.has("is_required") && !value.getBoolean("is_required"))) {
                        totalResult = false;
                        setBlockMarkedAsRequired(value.getString("id"));
                    }
                    break;

                case "slider":
                    if (!value.has("value")) {
                        totalResult = false;
                        setBlockMarkedAsRequired(value.getString("id"));
                    }
                    break;

                case "signature":
                    if (!value.has(SIGNATURE_PREVIEW_JSON_ARRAY) && !(value.has("is_required") && !value.getBoolean("is_required"))) {
                        totalResult = false;
                        setBlockMarkedAsRequired(value.getString("id"));
                    } else
                        photoObjects.add(value);
                    break;

                case "richedit":
                    break;

                default:
                    Log.d("jeka", fields.get(i));
            }
        }
        if (totalResult)
            return new Pair<>(true, photoObjects);
        else
            return new Pair<>(false, null);
    }


    private JSONArray getTemplateElementValues(JSONObject template) {
        JSONArray resultValues = new JSONArray();

        try {
            if (template != null) {
                JSONArray pagesJson = template.getJSONArray("items");
                for (int i = 0; i < pagesJson.length(); i++) {
                    JSONObject pageJson = pagesJson.getJSONObject(i).getJSONObject("page");
                    if (pageJson.has("items")) {
                        JSONArray pageValues = getContainerElementValues(pageJson.getJSONArray("items"));
                        resultValues = Utils.addAllItemsToJson(resultValues, pageValues);
                    }
                }
            }
        } catch (Exception exc) {
            exc.printStackTrace();
        }

        return resultValues;
    }

    private JSONArray getContainerElementValues(JSONArray dataJson) throws Exception {
        JSONArray containerValues = new JSONArray();

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
                    JSONArray categoryValues = getContainerElementValues(value.getJSONArray("items"));
                    containerValues = Utils.addAllItemsToJson(containerValues, categoryValues);
                    break;

                case "question":
                    if (value.has("values")) {
                        for (int j = 0; j < value.getJSONArray("values").length(); j++) {
                            JSONObject objectValue = new JSONObject();
                            objectValue.put("id", value.getString("id"));
                            objectValue.put("value", value.getJSONArray("values").getString(j));
                            objectValue.put("type", "question");

                            containerValues.put(objectValue);
                        }

                        if (value.has("optionals") && value.getJSONArray("optionals").length() > 0) {
                            JSONArray optionals = value.getJSONArray("optionals");
                            for (int j = 0; j < optionals.length(); j++) {
                                JSONObject optional = optionals.getJSONObject(j).getJSONObject("optional");
                                if (optional.has("keys") && optional.getJSONArray("keys").length() > 0 && value.has("values")
                                        && Utils.containsAllValues(value.getJSONArray("values"), optional.getJSONArray("keys"))) {

                                    JSONArray optionalValues = getContainerElementValues(optional.getJSONArray("items"));
                                    containerValues = Utils.addAllItemsToJson(containerValues, optionalValues);
                                }
                            }
                        }
                    }
                    break;

                case "text":
                    break;

                case "lineedit":
                    if (value.has("value")) {
                        JSONObject objectValue = new JSONObject();
                        objectValue.put("id", value.getString("id"));
                        objectValue.put("value", value.getString("value"));
                        objectValue.put("type", "text");

                        containerValues.put(objectValue);
                    }
                    break;

                case "textarea":
                    if (value.has("value")) {
                        JSONObject objectValue = new JSONObject();
                        objectValue.put("id", value.getString("id"));
                        objectValue.put("value", value.getString("value"));
                        objectValue.put("type", "text");

                        containerValues.put(objectValue);
                    }
                    break;

                case "checkbox":
                    if (value.has("value")) {
                        JSONObject objectValue = new JSONObject();
                        objectValue.put("id", value.getString("id"));
                        objectValue.put("value", value.getBoolean("value"));
                        objectValue.put("type", "checkbox");

                        containerValues.put(objectValue);
                    }
                    break;

                case "slider":
                    if (value.has("value")) {
                        JSONObject objectValue = new JSONObject();
                        objectValue.put("id", value.getString("id"));
                        objectValue.put("value", value.getString("value"));
                        objectValue.put("type", "float");

                        containerValues.put(objectValue);
                    }
                    break;

                case "photo":
                    if (value.has("values")) {
                        for (int j = 0; j < value.getJSONArray("values").length(); j++) {
                            JSONObject objectValue = new JSONObject();
                            objectValue.put("id", value.getString("id"));
                            objectValue.put("value", value.getJSONArray("values").getString(j));
                            objectValue.put("type", "media_id");

                            containerValues.put(objectValue);
                        }
                    }
                    break;

                case "richedit":
                    break;

                case "signature":
                    if (value.has("value")) {
                        JSONObject objectValue = new JSONObject();
                        objectValue.put("id", value.getString("id"));
                        objectValue.put("value", value.getString("value"));
                        objectValue.put("type", "media_id");

                        containerValues.put(objectValue);
                    }
                    break;

                default:
                    Log.d("jeka", fields.get(i));
            }
        }
        return containerValues;
    }
}
