package org.dev_alex.mojo_qa.mojo.fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.location.Location;
import android.media.ExifInterface;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.util.Pair;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.legacy.widget.Space;
import androidx.appcompat.widget.PopupMenu;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.DigitsKeyListener;
import android.util.Base64;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
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

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.GravityEnum;
import com.afollestad.materialdialogs.MaterialDialog;
import com.darsh.multipleimageselect.activities.AlbumSelectActivity;
import com.darsh.multipleimageselect.helpers.Constants;
import com.darsh.multipleimageselect.models.Image;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.gcacace.signaturepad.views.SignaturePad;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.renderer.XAxisRenderer;
import com.github.mikephil.charting.utils.MPPointF;
import com.github.mikephil.charting.utils.Transformer;
import com.github.mikephil.charting.utils.ViewPortHandler;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.lalongooo.videocompressor.video.MediaController;

import net.cachapa.expandablelayout.ExpandableLayout;
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent;

import org.dev_alex.mojo_qa.mojo.App;
import org.dev_alex.mojo_qa.mojo.BuildConfig;
import org.dev_alex.mojo_qa.mojo.Data;
import org.dev_alex.mojo_qa.mojo.R;
import org.dev_alex.mojo_qa.mojo.activities.AuthActivity;
import org.dev_alex.mojo_qa.mojo.activities.ImageViewActivity;
import org.dev_alex.mojo_qa.mojo.activities.MainActivity;
import org.dev_alex.mojo_qa.mojo.activities.OpenLinkActivity;
import org.dev_alex.mojo_qa.mojo.custom_views.CustomWebview;
import org.dev_alex.mojo_qa.mojo.custom_views.camera.CustomCamera2Activity;
import org.dev_alex.mojo_qa.mojo.custom_views.indicator.IndicatorLayout;
import org.dev_alex.mojo_qa.mojo.models.IndicatorModel;
import org.dev_alex.mojo_qa.mojo.models.Page;
import org.dev_alex.mojo_qa.mojo.models.User;
import org.dev_alex.mojo_qa.mojo.services.BitmapService;
import org.dev_alex.mojo_qa.mojo.services.LoginHistoryService;
import org.dev_alex.mojo_qa.mojo.services.RequestService;
import org.dev_alex.mojo_qa.mojo.services.TokenService;
import org.dev_alex.mojo_qa.mojo.services.Utils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.UUID;

import icepick.Icepick;
import icepick.State;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.BufferedSink;
import okio.Okio;

import static android.app.Activity.RESULT_OK;
import static org.dev_alex.mojo_qa.mojo.services.Utils.setupCloseKeyboardUI;

public class TemplateFragment extends Fragment {
    private static String NODE_FOR_TASKS = "229ed0ec-3592-4788-87f0-6b0616599166";
    private String NODE_FOR_FILES = "4899bb8e-0b0b-4889-82d6-eb16fcd6b90f";

    private final String MEDIA_PATH_JSON_ARRAY = "media_paths";
    private final String SIGNATURE_PREVIEW_JSON_ARRAY = "sign_state";
    private SimpleDateFormat isoDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.getDefault());
    private SimpleDateFormat isoDateFormatNoTimeZone = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
    SimpleDateFormat xDateFormat = new SimpleDateFormat("dd-MM\nHH:mm", Locale.getDefault());
    private final int VIDEO_REQUEST_CODE = 10;
    private final int PHOTO_REQUEST_CODE = 11;
    private final int AUDIO_REQUEST_CODE = 12;
    private final int DOCUMENT_REQUEST_CODE = 13;
    private final int IMAGE_SHOW_REQUEST_CODE = 110;
    private final int SCAN_CODE_REQUEST_CODE = 120;

    public boolean isOpenLink = false;
    public String taskUUID = null;
    public boolean isReportOpenLink = false;

    public long taskId;
    public long documentId;

    private View rootView;
    private ProgressDialog loopDialog;

    public ArrayList<Page> pages;
    private ArrayList<String> requiredElementTags;
    private ArrayList<String> requiredCategoriesTags;

    public int currentPagePos;
    public JSONObject template;

    public TextView scanTo = null;
    public JSONObject scanToObj = null;

    public Pair<LinearLayout, JSONObject> currentMediaBlock;
    @State
    public String cameraVideoPath;
    public boolean isTaskFinished;
    private ProgressDialog progressDialog;
    private Handler handler;

    View separator;

    private boolean waiting = false;

    private Location userLocation = null;

    public static TemplateFragment newInstance(long taskId, boolean isTaskFinished) {
        Bundle args = new Bundle();
        args.putLong("task_id", taskId);
        args.putBoolean("is_finished", isTaskFinished);

        TemplateFragment fragment = new TemplateFragment();
        fragment.setArguments(args);
        return fragment;
    }
//
    public static TemplateFragment newInstance(String taskUUID, boolean isReportMode) {
        Bundle args = new Bundle();
        args.putString("task_uuid", taskUUID);
        args.putBoolean("is_finished", false);
        args.putBoolean("is_report_open_link", isReportMode);

        TemplateFragment fragment = new TemplateFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(null);
        Icepick.restoreInstanceState(this, savedInstanceState);
        setRetainInstance(true);
        handler = new Handler();

        if (getArguments().containsKey("task_id")) {
            taskId = getArguments().getLong("task_id");
            isOpenLink = false;
        } else {
            taskUUID = getArguments().getString("task_uuid");
            isOpenLink = true;
            isReportOpenLink = getArguments().getBoolean("is_report_open_link", false);
        }
        isTaskFinished = getArguments().getBoolean("is_finished");
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(null);
        Icepick.saveInstanceState(this, outState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (rootView == null) {
            isoDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

            requiredElementTags = new ArrayList<>();
            requiredCategoriesTags = new ArrayList<>();

            rootView = inflater.inflate(R.layout.fragment_template, container, false);
            setupCloseKeyboardUI(getActivity(), rootView);

            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).drawer.getDrawerLayout().setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
            }

            rootView.findViewById(R.id.page_selector_block).getLayoutParams().width =
                    (int) (getResources().getDisplayMetrics().widthPixels * (2.0 / 5.0));

            initDialog();
            setupHeader();
            setListeners();

            new GetTemplateTask().execute();

            KeyboardVisibilityEvent.setEventListener(
                    getActivity(),
                    isOpen -> {
                        if (!isAdded()) {
                            return;
                        }
                        View rootContainer = rootView.findViewById(R.id.root_container);
                        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) rootContainer.getLayoutParams();
                        Resources resources = getResources();
                        if (isOpen) {
                            params.bottomMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 200, resources.getDisplayMetrics());
                        } else {
                            params.bottomMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 0, resources.getDisplayMetrics());
                        }
                        rootContainer.setLayoutParams(params);
                        rootContainer.requestLayout();
                    });
        } else
            initDialog();

        return rootView;
    }

    @Override
    public void onViewCreated(@androidx.annotation.NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (checkLocationPermissions()) {
            requestLocation();
        } else {
            requestLocationPermissions();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @androidx.annotation.NonNull String[] permissions, @androidx.annotation.NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (checkLocationPermissions()) {
            requestLocation();
        }
    }

    @SuppressLint("MissingPermission")
    private void requestLocation() {
        if (checkLocationPermissions()) {
            FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext());
            fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                if (location != null) {
                    userLocation = location;
                }
            });
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Constants.REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            ArrayList<Image> images = data.getParcelableArrayListExtra(Constants.INTENT_EXTRA_IMAGES);
            for (Image image : images) {
                processImageFile(new File(image.path), false, null, true);
            }
        }

        if (requestCode == PHOTO_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            String photoPath = data.getStringExtra("photo_path");
            processImageFile(new File(photoPath), false, null, false);
        }

        if (requestCode == VIDEO_REQUEST_CODE && resultCode == RESULT_OK) {
            if (new File(cameraVideoPath).exists() || data == null)
                createVideoPreview(cameraVideoPath, true);
            else {
                String videoPath = Utils.getRealPathFromIntentData(getContext(), data.getData());
                if (videoPath == null)
                    Toast.makeText(getContext(), R.string.something_wrong, Toast.LENGTH_SHORT).show();
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
            else {
                String uriStr;
                if(!data.getData().toString().contains("0@media")){
                uriStr = "content://0@media/external/file/" + data.getData().toString().substring(data.getData().toString().lastIndexOf("3A") + 2);}
                else{uriStr = data.getData().toString();}
                Log.e("aaa", uriStr);
                documentPath = Utils.getPathFromUri(getContext(), Uri.parse(uriStr));
            }

            if (documentPath == null)
                Toast.makeText(getContext(), R.string.something_wrong, Toast.LENGTH_SHORT).show();
            else {
                String mimeType = Utils.getMimeType(documentPath);
                if (mimeType.startsWith("image"))
                    processImageFile(new File(documentPath), false, null, true);
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
                Toast.makeText(getContext(), getString(R.string.something_wrong) + " " + data.getDataString(), Toast.LENGTH_SHORT).show();
            else
                createAudioPreview(audioPath, true);
        }

        if (requestCode == SCAN_CODE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                String contents = data.getStringExtra("SCAN_RESULT");
                tryParseCode(contents);
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        Data.currentTaskId = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        Data.currentTaskId = taskId;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        saveTemplateState();
    }

    private void setupHeader() {
        ((TextView) getActivity().findViewById(R.id.title)).setText(getString(R.string.tasks));
        getActivity().findViewById(R.id.grid_btn).setVisibility(View.GONE);
        getActivity().findViewById(R.id.sandwich_btn).setVisibility(View.GONE);
        getActivity().findViewById(R.id.group_by_btn).setVisibility(View.GONE);
        getActivity().findViewById(R.id.search_btn).setVisibility(View.GONE);
        getActivity().findViewById(R.id.notification_btn).setVisibility(View.GONE);
        getActivity().findViewById(R.id.qr_btn).setVisibility(View.GONE);

        getActivity().findViewById(R.id.back_btn).setVisibility(View.VISIBLE);
        getActivity().findViewById(R.id.back_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (rootView.findViewById(R.id.scroll_view).canScrollVertically(-1))
                        ((ScrollView) rootView.findViewById(R.id.scroll_view)).fullScroll(View.FOCUS_UP);
                    else if (getActivity() != null && getActivity().getSupportFragmentManager() != null) {
                        if (getActivity() instanceof OpenLinkActivity) {
                            getActivity().finish();
                            return;
                        }
                        getActivity().getSupportFragmentManager().popBackStack();
                        if (getActivity() instanceof MainActivity) {
                            ((MainActivity) getActivity()).drawer.getDrawerLayout().setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
                        }
                    }
                } catch (Exception exc) {
                    exc.printStackTrace();
                }
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
                saveTemplateState();
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });

        rootView.findViewById(R.id.finish_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (userLocation == null) {
                    requestLocation();
                }
                new WaitForMediaReadyTask().execute();
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

    private void showGalleryOrPhotoPickDialog() {
        new MaterialDialog.Builder(getContext())
                .title(R.string.to_add_photo)
                .cancelable(true)
                .content(R.string.select_photo_source)
                .buttonsGravity(GravityEnum.CENTER)
                .autoDismiss(true)
                .positiveText(R.string.camera)
                .negativeText(R.string.gallery)
                .onPositive((dialog, which) -> CustomCamera2Activity.startForResult(TemplateFragment.this, PHOTO_REQUEST_CODE))
                .onNegative((dialog, which) -> {
                    Intent intent = new Intent(getContext(), AlbumSelectActivity.class);
                    intent.putExtra(Constants.INTENT_EXTRA_LIMIT, 10);
                    startActivityForResult(intent, Constants.REQUEST_CODE);
                })
                .build()
                .show();
    }

    private void setPage(Page page) {
        try {
            FrameLayout rootTemplateContainer = rootView.findViewById(R.id.root_container);
            rootTemplateContainer.removeAllViewsInLayout();

            LinearLayout rootPageContainer = new LinearLayout(getContext());
            rootPageContainer.setOrientation(LinearLayout.VERTICAL);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            rootPageContainer.setLayoutParams(layoutParams);

            int pageI = pages.indexOf(page);
            JSONArray pagesJson = template.getJSONArray("items");
            JSONObject pageJson = pagesJson.getJSONObject(pageI).getJSONObject("page");

            if (pageJson.has("items"))
                fillContainer(rootPageContainer, pageJson.getJSONArray("items"), 0);

            rootTemplateContainer.addView(rootPageContainer);
            setupCloseKeyboardUI(getActivity(), rootPageContainer);


            for (int i = 0; i < requiredElementTags.size(); i++) {
                String requiredElementTag = requiredElementTags.get(i);
                setBlockMarkedAsRequired(requiredElementTag);
            }
            for (int i = 0; i < requiredCategoriesTags.size(); i++) {
                String requiredCategoryTag = requiredCategoriesTags.get(i);
                setCategoryMarkedAsRequired(requiredCategoryTag);
            }

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
        } catch (Exception exc) {
            Toast.makeText(getContext(), getString(R.string.render_error) + exc.getMessage(), Toast.LENGTH_LONG).show();
            exc.printStackTrace();
        }
    }

    private void updateArrows() {
        if (pages != null && !pages.isEmpty()) {
            rootView.findViewById(R.id.left_arrow).setVisibility(currentPagePos > 0 ? View.VISIBLE : View.INVISIBLE);
            rootView.findViewById(R.id.right_arrow).setVisibility(currentPagePos < pages.size() - 1 ? View.VISIBLE : View.INVISIBLE);

            if (currentPagePos == pages.size() - 1) {
                /*rootView.findViewById(R.id.left_space).setVisibility(View.GONE);
                rootView.findViewById(R.id.right_space).setVisibility(View.GONE);*/
                rootView.findViewById(R.id.finish_btn_container).setVisibility(View.VISIBLE);
            } else {
                if (!isTaskFinished) {
                   /* rootView.findViewById(R.id.left_space).setVisibility(View.VISIBLE);
                    rootView.findViewById(R.id.right_space).setVisibility(View.VISIBLE);*/
                    rootView.findViewById(R.id.finish_btn_container).setVisibility(View.GONE);
                }
            }
        } else {
         /*   rootView.findViewById(R.id.left_space).setVisibility(View.GONE);
            rootView.findViewById(R.id.right_space).setVisibility(View.GONE);*/
            rootView.findViewById(R.id.finish_btn_container).setVisibility(View.VISIBLE);


            rootView.findViewById(R.id.left_arrow).setVisibility(View.INVISIBLE);
            rootView.findViewById(R.id.right_arrow).setVisibility(View.INVISIBLE);
        }
    }

    private void saveTemplateState() {
        try {
            if (isTaskFinished || isOpenLink)
                return;

            if (!template.has("StartTime"))
                template.put("StartTime", isoDateFormat.format(new Date()));
            SharedPreferences mSettings;
            mSettings = App.getContext().getSharedPreferences("templates", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = mSettings.edit();

            editor.putString(taskId + LoginHistoryService.getCurrentUser().username, template.toString());
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
                    JSONObject pageJson = pagesJson.getJSONObject(i).getJSONObject("page");

                    final Page page = new Page(pageJson.getString("caption"), pageJson.getString("id"));
                    pages.add(page);

                    LinearLayout pageContainer = rootView.findViewById(R.id.page_container);
                    TextView cardPage = (TextView) getActivity().getLayoutInflater().inflate(R.layout.card_page, pageContainer, false);
                    cardPage.setText(page.name);
                    cardPage.setMaxLines(1);
                    cardPage.setFocusable(true);
                    cardPage.setClickable(true);
                    cardPage.setEllipsize(TextUtils.TruncateAt.END);

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

                    if (isTaskFinished) {
                        rootView.findViewById(R.id.main_buttons_block).setVisibility(View.GONE);
                        rootView.findViewById(R.id.finished_buttons_block).setVisibility(View.VISIBLE);

                        rootView.findViewById(R.id.close_btn_container).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                getActivity().getSupportFragmentManager().popBackStack();
                            }
                        });
                        rootView.findViewById(R.id.download_pdf_btn_container).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (checkExternalPermissions()) {
                                    try {
                                        String pdfName = template.getString("name") + "_" +
                                                new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.getDefault()).format(new Date());
                                        new DownloadPdfTask(documentId, pdfName).execute();
                                    } catch (Exception exc) {
                                        exc.printStackTrace();
                                    }
                                } else
                                    requestExternalPermissions();
                            }
                        });

                        User me = LoginHistoryService.getCurrentUser();
                        if ((me.is_manager == null || !me.is_manager) && (me.is_orgowner == null || !me.is_orgowner)) {
                            rootView.findViewById(R.id.download_pdf_btn_container).setVisibility(View.GONE);
                        }
                    } else {
                        rootView.findViewById(R.id.main_buttons_block).setVisibility(View.VISIBLE);
                        rootView.findViewById(R.id.finished_buttons_block).setVisibility(View.GONE);
                    }
                    rootView.findViewById(R.id.buttons_block).setVisibility(View.VISIBLE);
                }
                setupClosePagePopupUI(rootView);
            }
        } catch (Exception exc) {
            exc.printStackTrace();
            Toast.makeText(getContext(), R.string.invalid_template, Toast.LENGTH_SHORT).show();
            getActivity().getSupportFragmentManager().popBackStack();
            exc.printStackTrace();
        }
    }

    public void setupClosePagePopupUI(final View view) {
        if (view.getId() != R.id.page_select_layout)
            view.setOnTouchListener(new View.OnTouchListener() {
                public boolean onTouch(View v, MotionEvent event) {
                    final Rect viewRect = new Rect();
                    rootView.findViewById(R.id.page_selector_block).getGlobalVisibleRect(viewRect);
                    if (!viewRect.contains((int) event.getRawX(), (int) event.getRawY())) {
                        rootView.findViewById(R.id.page_select_layout).setVisibility(View.GONE);
                    }
                    return false;
                }
            });

        if (view instanceof ViewGroup) {
            if (view.getId() != R.id.page_select_layout)
                for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                    View innerView = ((ViewGroup) view).getChildAt(i);
                    setupClosePagePopupUI(innerView);
                }
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
            separator = getActivity().getLayoutInflater().inflate(R.layout.separator, container, false);

            switch (fields.get(i)) {
                case "category":


                    //container.addView(separator);
                    createCategory(value, container, offset);


                    break;

                case "question":
                    container.addView(separator);


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


                    if (isTaskFinished)
                        ((ViewGroup) editTextSingleLineContainer.getChildAt(1)).getChildAt(0).setEnabled(false);
                    else {
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
                                scanQrCode(((EditText) ((ViewGroup) editTextSingleLineContainer.getChildAt(1)).getChildAt(0)), null);
                            }
                        });

                        ((ViewGroup) editTextSingleLineContainer.getChildAt(1)).getChildAt(2).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                scanBarCode(((EditText) ((ViewGroup) editTextSingleLineContainer.getChildAt(1)).getChildAt(0)), null);
                            }
                        });
                    }
                    container.addView(separator);

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

                    if (isTaskFinished)
                        (((ViewGroup) editTextMultiLineContainer.getChildAt(1)).getChildAt(0)).setEnabled(false);
                    else {
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
                                scanQrCode(((EditText) ((ViewGroup) editTextMultiLineContainer.getChildAt(1)).getChildAt(0)), null);
                            }
                        });

                        ((ViewGroup) editTextMultiLineContainer.getChildAt(1)).getChildAt(2).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                scanBarCode(((EditText) ((ViewGroup) editTextMultiLineContainer.getChildAt(1)).getChildAt(0)), null);
                            }
                        });
                    }
                    container.addView(separator);

                    container.addView(boxInContainerWithId(editTextMultiLineContainer, value.getString("id")));

                    break;

                case "money":
                    final LinearLayout moneyContainer = (LinearLayout) getActivity().getLayoutInflater().inflate(R.layout.text_plan, container, false);
                    ((ViewGroup) moneyContainer.getChildAt(0)).getChildAt(1).setVisibility((value.has("is_required") && !value.getBoolean("is_required")) ? View.GONE : View.VISIBLE);
                    final EditText etPlan = ((EditText) ((ViewGroup) ((ViewGroup) ((ViewGroup) moneyContainer.getChildAt(1)).getChildAt(0)).getChildAt(1)).getChildAt(0));
                    final View tvPlanLabel = ((ViewGroup) ((ViewGroup) ((ViewGroup) moneyContainer.getChildAt(1)).getChildAt(0)).getChildAt(0)).getChildAt(0);
                    final EditText etFact = ((EditText) ((ViewGroup) ((ViewGroup) ((ViewGroup) moneyContainer.getChildAt(1)).getChildAt(0)).getChildAt(1)).getChildAt(1));

                    final View btQrCode = ((ViewGroup) moneyContainer.getChildAt(1)).getChildAt(1);
                    final View btBarcode = ((ViewGroup) moneyContainer.getChildAt(1)).getChildAt(2);

                    final TextView captionLabel = ((TextView) ((ViewGroup) moneyContainer.getChildAt(0)).getChildAt(0));

                    if (value.has("barcode")) {
                        captionLabel.setText(value.getString("barcode"));
                    } else {
                        if (value.has("caption"))
                            captionLabel.setText(value.getString("caption"));
                        else
                            captionLabel.setText(R.string.no_text);
                    }

                    if (value.has("_plan"))
                        etPlan.setText(value.getString("_plan"));

                    if (value.has("_fact"))
                        etFact.setText(value.getString("_fact"));

                    if (isTaskFinished) {
                        etPlan.setEnabled(false);
                        etFact.setEnabled(false);
                        btBarcode.setVisibility(View.GONE);
                        btQrCode.setVisibility(View.GONE);
                    } else {
                        etPlan.addTextChangedListener(new TextWatcher() {
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
                                        value.remove("_plan");
                                    else
                                        value.put("_plan", Double.parseDouble(s.toString().trim()));
                                } catch (Exception ignored) {
                                    value.remove("_plan");
                                }
                            }
                        });
                        etFact.addTextChangedListener(new TextWatcher() {
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
                                        value.remove("_fact");
                                    else
                                        value.put("_fact", Double.parseDouble(s.toString().trim()));
                                } catch (Exception ignored) {
                                    value.remove("_fact");
                                }
                            }
                        });

                        if (value.has("plan")) {
                            etPlan.setText(value.getString("plan"));
                            etPlan.setEnabled(false);

                            if (value.has("hidePlan") && value.getBoolean("hidePlan")) {
                                etPlan.setVisibility(View.INVISIBLE);
                                tvPlanLabel.setVisibility(View.INVISIBLE);
                            }
                        }

                        btBarcode.setOnClickListener(view -> scanBarCode(captionLabel, value));
                        btQrCode.setOnClickListener(view -> scanQrCode(captionLabel, value));
                    }
                    container.addView(separator);

                    container.addView(boxInContainerWithId(moneyContainer, value.getString("id")));

                    break;

                case "checkbox":
                    LinearLayout checkBoxContainer = (LinearLayout) getActivity().getLayoutInflater().inflate(R.layout.checkbox, container, false);

                    if (value.has("caption"))
                        ((TextView) checkBoxContainer.getChildAt(1)).setText(value.getString("caption"));
                    else
                        ((TextView) checkBoxContainer.getChildAt(1)).setText(R.string.no_text);

                    if (!isTaskFinished)
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
                    else
                        checkBoxContainer.getChildAt(0).setEnabled(false);

                    if (value.has("value"))
                        ((CheckBox) checkBoxContainer.getChildAt(0)).setChecked(value.getBoolean("value"));
                    else {
                        ((CheckBox) checkBoxContainer.getChildAt(0)).setChecked(true);
                        ((CheckBox) checkBoxContainer.getChildAt(0)).setChecked(false);
                    }
                    container.addView(separator);

                    container.addView(boxInContainerWithId(checkBoxContainer, value.getString("id")));

                    break;

                case "slider":
                    container.addView(separator);

                    createSeekBar(value, container);


                    break;

                case "photo":
                    container.addView(separator);

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
                        CustomWebview richEdit = new CustomWebview(getContext());
                        LinearLayout.LayoutParams richEditLayoutParams = new LinearLayout.LayoutParams
                                (ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

                        richEdit.getSettings().setBuiltInZoomControls(true);
                        richEdit.getSettings().setSupportZoom(true);
                        richEdit.getSettings().setDisplayZoomControls(false);
                        richEdit.setFocusable(true);
                        richEdit.setFocusableInTouchMode(true);
                        richEdit.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);


                        richEditLayoutParams.leftMargin = paddings;
                        richEditLayoutParams.rightMargin = paddings;
                        richEditLayoutParams.topMargin = paddings;
                        richEdit.setLayoutParams(richEditLayoutParams);

                        String mime = "text/html";
                        String encoding = "utf-8";

                        String html = value.getString("html");
                        if (!html.contains("<html"))
                            html = "<html><head></head><body> " + html + " </body></html>";
                        html = html.replace(App.getHost(), "");
                        html = html.replace("/api", App.getHost() + "/api");
                        html = Utils.formatHtmlContentSize(html);

                        richEdit.getSettings().setJavaScriptEnabled(true);
                        String cookieString = "auth_token=" + TokenService.getToken();
                        CookieManager.getInstance().setCookie(App.getHost() + "/", cookieString);
                        CookieManager.getInstance().setAcceptCookie(true);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                            CookieManager.getInstance().setAcceptThirdPartyCookies(richEdit, true);

                        richEdit.loadDataWithBaseURL("", html, mime, encoding, null);
                        container.addView(separator);

                        container.addView(richEdit);

                    }
                    break;

                case "signature":
                    container.addView(separator);

                    createSignature(value, container);

                    break;

                case "indicator":
                    if (!isTaskFinished) {
                        Resources resources = getContext().getResources();

                        ImageView indicator = new ImageView(getContext());
                        indicator.setAdjustViewBounds(true);

                        switch (value.getString("type")) {
                            case "indicator":
                                indicator.setImageResource(R.drawable.default_indicator);
                                break;
                            case "spline":
                                indicator.setImageResource(R.drawable.default_spline);
                                break;
                            case "histogram":
                                indicator.setImageResource(R.drawable.default_histogram);
                                break;

                        }
                        container.addView(indicator);

                        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) indicator.getLayoutParams();
                        layoutParams.height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 180, resources.getDisplayMetrics());
                        layoutParams.gravity = Gravity.CENTER_HORIZONTAL;
                        layoutParams.width = LinearLayout.LayoutParams.WRAP_CONTENT;
                        indicator.setLayoutParams(layoutParams);
                        indicator.requestLayout();
                    } else {
                        String type = value.getString("type");
                        switch (type) {
                            case "indicator":
                                IndicatorModel indicatorModel = new IndicatorModel();

                                if (value.has("ranges")) {
                                    ArrayList<IndicatorModel.Range> ranges = new ObjectMapper()
                                            .readValue(value.getJSONArray("ranges").toString(), new TypeReference<ArrayList<IndicatorModel.Range>>() {
                                            });
                                    indicatorModel.ranges = ranges;
                                }

                                IndicatorLayout indicatorLayout = new IndicatorLayout(getContext(), indicatorModel);
                                if (value.has("value")) {
                                    indicatorLayout.setCurrentValue(value.getInt("value"));
                                    container.addView(indicatorLayout);
                                }
                                break;

                            case "spline":
                            case "histogram":
                                Resources resources = getContext().getResources();
                                int chartHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 300, resources.getDisplayMetrics());
                                RelativeLayout chartContainer = new RelativeLayout(getContext());
                                chartContainer.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, chartHeight));

                                final JSONArray ticks = value.getJSONArray("tick");

                                List<Entry> entries = new ArrayList<>();
                                List<BarEntry> barEntries = new ArrayList<>();
                                final List<String> xValues = new ArrayList<>();

                                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

                                for (int k = 0; k < ticks.length(); k++) {
                                    JSONObject tick = ticks.getJSONObject(k);
                                    Date date = sdf.parse(tick.getString("DT"));

                                    entries.add(new Entry(k, (float) tick.getDouble("value")));
                                    BarEntry barEntry = new BarEntry((float) k, (float) tick.getDouble("value"));
                                    barEntries.add(barEntry);
                                    xValues.add(xDateFormat.format(date));
                                }
                                if (entries.isEmpty()) {
                                    entries.add(new Entry(0, 0));
                                    barEntries.add(new BarEntry(0, 0));

                                    TextView emptyText = new TextView(getContext());
                                    emptyText.setText(R.string.no_data);
                                    chartContainer.addView(emptyText);

                                    RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams
                                            (ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                                    layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
                                    emptyText.setLayoutParams(layoutParams);
                                    emptyText.requestLayout();
                                }


                                if (type.equals("spline")) {
                                    LineChart lineChart = new LineChart(getContext());
                                    lineChart.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));

                                    LineDataSet dataSet = new LineDataSet(entries, "");
                                    dataSet.setMode(LineDataSet.Mode.LINEAR);
                                    dataSet.setDrawCircles(false);

                                    dataSet.setDrawHighlightIndicators(true);
                                    dataSet.setHighlightEnabled(true);
                                    dataSet.setHighLightColor(Color.RED);

                                    dataSet.setValueTextColor(Color.BLACK);

                                    dataSet.setLineWidth(2f);
                                    dataSet.setColor(Color.parseColor("#AFA8DC"));

                                    dataSet.setDrawFilled(true);
                                    dataSet.setFillDrawable(ContextCompat.getDrawable(getContext(), R.drawable.spline_gradient_bg));

                                    LineData lineData = new LineData(dataSet);
                                    lineData.setDrawValues(false);

                                    lineChart.setData(lineData);
                                    lineChart.getLegend().setEnabled(false);
                                    lineChart.getDescription().setEnabled(false);
                                    lineChart.setTouchEnabled(true);
                                    lineChart.setScaleYEnabled(false);
                                    lineChart.setScaleXEnabled(true);
                                    lineChart.setAutoScaleMinMaxEnabled(true);

                                    setupAxis(lineChart.getXAxis(), xValues);

                                    lineChart.getAxisRight().setEnabled(false);
                                    lineChart.invalidate(); // refresh
                                    lineChart.getAxisLeft().resetAxisMinimum();
                                    lineChart.getAxisLeft().setAxisMaximum(lineChart.getAxisLeft().getAxisMaximum() * 1.2f);

                                    lineChart.getAxisLeft().setGridColor(Color.parseColor("#374E3F60"));
                                    lineChart.getXAxis().setGridColor(Color.parseColor("#374E3F60"));


                                    lineChart.getXAxis().setDrawAxisLine(false);
                                    lineChart.getAxisLeft().setDrawAxisLine(false);
                                    lineChart.getAxisRight().setDrawAxisLine(false);
                                    lineChart.setXAxisRenderer(new CustomXAxisRenderer(lineChart.getViewPortHandler(), lineChart.getXAxis(), lineChart.getTransformer(YAxis.AxisDependency.LEFT)));
                                    lineChart.setExtraBottomOffset(50);

                                    chartContainer.addView(lineChart);
                                } else {
                                    BarChart barChart = new BarChart(getContext());
                                    barChart.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));
                                    barChart.setDrawValueAboveBar(false);

                                    BarDataSet set = new BarDataSet(barEntries, "BarDataSet");

                                    BarData barData = new BarData(set);
                                    barData.setDrawValues(false);
                                    barData.setBarWidth(1f);

                                    barChart.getAxisRight().setEnabled(false);
                                    barChart.getAxisLeft().setGridColor(Color.parseColor("#374E3F60"));
                                    barChart.getAxisLeft().setAxisLineColor(Color.parseColor("#374E3F60"));
                                    barChart.getXAxis().setGridColor(Color.parseColor("#374E3F60"));
                                    barChart.getXAxis().setAxisLineColor(Color.parseColor("#374E3F60"));

                                    barChart.setData(barData);
                                    barChart.setFitBars(false);
                                    barChart.getLegend().setEnabled(false);
                                    barChart.getDescription().setEnabled(false);
                                    barChart.setScaleYEnabled(true);
                                    setupAxis(barChart.getXAxis(), xValues);

                                    barChart.invalidate();
                                    barChart.zoom(barEntries.size() / 10, 1, 0, 0);
                                    chartContainer.addView(barChart);
                                }
                                container.addView(chartContainer);
                                break;
                        }
                    }

                default:
                    Log.d("jeka", fields.get(i));
            }

            Space space = new Space(getContext());
            space.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, 10, getResources().getDisplayMetrics())));
            container.addView(space);
        }

        if (isTaskFinished && template.has("analytic_value")) {
            try {
                JSONObject analyticsValue = template.getJSONObject("analytic_value");

                TextView analyticsText = new TextView(getContext());
                analyticsText.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    analyticsText.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                }
                analyticsText.setGravity(Gravity.CENTER);
                analyticsText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
                analyticsText.setTextColor(Color.parseColor("#4E3F60"));

                analyticsText.setText(String.format(Locale.getDefault(),
                        "%d | %d " + getString(R.string.balls) + "\n %d%% | %d%%",
                        (int) analyticsValue.getDouble("val"), (int) analyticsValue.getDouble("max"),
                        (int) analyticsValue.getDouble("prc"), 100));

                container.addView(analyticsText);
                Space space = new Space(getContext());
                space.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP, 10, getResources().getDisplayMetrics())));
                container.addView(space);
            } catch (Exception exc) {
                exc.printStackTrace();
            }
        }
    }

    private void setupAxis(XAxis xAxis, final List<String> xValues) {
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        //xAxis.setLabelRotationAngle(90);
        xAxis.setDrawLabels(true);
        xAxis.setEnabled(true);
        xAxis.setTextColor(Color.parseColor("#4E3F60"));
        // xAxis.setGranularity(3f);

        xAxis.setValueFormatter(new IAxisValueFormatter() {

            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                String stringValue;
                if (xValues.size() >= 0 && value >= 0) {
                    int finalIPos = -1;

                    if ((((int) value == value)))
                        finalIPos = (int) value;
                    else {
                        int startV = (int) value;
                        int endV = startV + 1;
                        if (endV >= xValues.size())
                            finalIPos = (int) value;
                        else {
                            try {
                                long startDate = xDateFormat.parse(xValues.get(startV)).getTime();
                                long endDate = xDateFormat.parse(xValues.get(endV)).getTime();
                                long delta = endDate - startDate;

                                double valueDelta = value - ((int) value);
                                long resultDate = startDate + (long) (delta * valueDelta);
                                return xDateFormat.format(new Date(resultDate));
                            } catch (Exception exc) {
                                exc.printStackTrace();
                                return "exc";
                            }
                        }
                    }

                    if (finalIPos != -1 && finalIPos < xValues.size()) {
                        stringValue = xValues.get(finalIPos);
                    } else {
                        stringValue = "";
                    }
                } else {
                    stringValue = "";
                }
                return stringValue;
            }
        });
    }

    private void tryParseCode(String code) {
        if (scanTo != null) {
            scanTo.setText(code);
        }
        if (scanToObj != null) {
            try {
                scanToObj.put("barcode", code);
            } catch (Exception exc) {
                exc.printStackTrace();
            }
        }

        OkHttpClient client = new OkHttpClient.Builder().addInterceptor(chain -> {
            Response response = chain.proceed(chain.request());

            return response.newBuilder()
                    .removeHeader("Content-Type")
                    .addHeader("Content-Type", "text/html; charset=windows-1251")
                    .build();
        }).build();

        RequestBody formBody = new FormBody.Builder()
                .add("search_query", code)
                .build();

        Request request = new Request.Builder()
                .post(formBody)
                .url("http://ru.disai.org")
                .addHeader("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8")
                .addHeader("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/69.0.3497.100 Safari/537.36")
                .build();

        loopDialog.show();

        Handler handler = new Handler();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@androidx.annotation.NonNull Call call, @androidx.annotation.NonNull IOException e) {
                if (loopDialog != null && loopDialog.isShowing()) {
                    loopDialog.dismiss();
                }
                e.printStackTrace();
            }

            @Override
            public void onResponse(@androidx.annotation.NonNull Call call, @androidx.annotation.NonNull Response response) {
                if (loopDialog != null && loopDialog.isShowing()) {
                    loopDialog.dismiss();
                }

                try {
                    ResponseBody responseBody = response.body();
                    if (response.isSuccessful() && responseBody != null) {
                        Document doc = Jsoup.parse(responseBody.source().inputStream(), "windows-1251", "");
                        String valueHtml = doc.select("table").first().child(0).child(1).child(0).html();
                        String value = valueHtml.substring(valueHtml.indexOf(">") + 1, valueHtml.indexOf("</"));
                        if (!value.isEmpty()) {
                            handler.post(() -> {
                                if (scanTo != null) {
                                    scanTo.setText(value + " " + code);
                                }
                            });

                            if (scanToObj != null) {
                                scanToObj.put("barcode", value);
                            }
                        }
                    }
                } catch (Exception exc) {
                    exc.printStackTrace();
                }
            }
        });
    }

    private void scanQrCode(TextView scanTo, JSONObject scanToObj) {
        try {
            this.scanTo = scanTo;
            this.scanToObj = scanToObj;
            Intent intent = new Intent("com.google.zxing.client.android.SCAN");
            intent.putExtra("SCAN_MODE", "QR_CODE_MODE"); // "PRODUCT_MODE for bar codes
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivityForResult(intent, SCAN_CODE_REQUEST_CODE);

        } catch (Exception e) {
            Uri marketUri = Uri.parse("market://details?id=com.google.zxing.client.android");
            Intent marketIntent = new Intent(Intent.ACTION_VIEW, marketUri);
            startActivity(marketIntent);
        }
    }

    private void scanBarCode(TextView scanTo, JSONObject scanToObj) {
        try {
            this.scanTo = scanTo;
            this.scanToObj = scanToObj;
            Intent intent = new Intent("com.google.zxing.client.android.SCAN");
            intent.putExtra("SCAN_MODE", "PRODUCT_MODE");
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
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
            ((TextView) categoryHeader.getChildAt(1)).setText(R.string.no_title);

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
        final View qrCodeBtn = ((ViewGroup) seekBarContainer.getChildAt(3)).getChildAt(1);
        final View barcodeBtn = ((ViewGroup) seekBarContainer.getChildAt(3)).getChildAt(2);
        final TextView captionLabel = ((TextView) seekBarContainer.getChildAt(0));

        if (value.has("barcode")) {
            captionLabel.setText(value.getString("barcode"));
        } else {
            if (value.has("caption"))
                captionLabel.setText(value.getString("caption"));
            else
                captionLabel.setText(R.string.no_text);
        }

        if (value.has("measure"))
            ((TextView) ((LinearLayout) ((LinearLayout) seekBarContainer.getChildAt(3)).getChildAt(0)).getChildAt(2)).setText(value.getString("measure"));

        final SeekBar seekBar = ((SeekBar) seekBarContainer.getChildAt(1));
        final EditText changeValue = (EditText) ((LinearLayout) ((LinearLayout) seekBarContainer.getChildAt(3)).getChildAt(0)).getChildAt(1);

        final float minValue = (float) value.getDouble("min_value");
        final float maxValue = (float) value.getDouble("max_value");
        float step = (float) value.getDouble("step");
        String minValueStr = formatFloat(minValue), maxValueStr = formatFloat(maxValue), stepValueStr = formatFloat(step);


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
                        value.put("value", String.format(Locale.US, "%." + finalDigitsAfterPoint + "f", Float.parseFloat(result)));
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

        if (isTaskFinished) {
            seekBar.setEnabled(false);
            changeValue.setEnabled(false);
            qrCodeBtn.setVisibility(View.GONE);
            barcodeBtn.setVisibility(View.GONE);
        }

        qrCodeBtn.setOnClickListener(view -> scanQrCode(captionLabel, value));

        barcodeBtn.setOnClickListener(view -> scanBarCode(captionLabel, value));
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
                    value.put(SIGNATURE_PREVIEW_JSON_ARRAY, BitmapService.getBitmapBytesEncodedBase64(bitmap));
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

        if (isTaskFinished) {
            signaturePad.setEnabled(false);
            ((ViewGroup) signatureContainer.getChildAt(2)).getChildAt(1).setVisibility(View.GONE);
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

        if (isTaskFinished) {
            expandableLayout.collapse();

            if (value.has("media_ids")) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy\nHH:mm", Locale.getDefault());

                ViewGroup mediaContainer = (ViewGroup) ((ViewGroup) mediaLayout.getChildAt(1)).getChildAt(0);
                mediaContainer.removeAllViewsInLayout();
                final JSONArray mediaIds = value.getJSONArray("media_ids");
                final JSONArray fileNames = value.getJSONArray("file_names");
                final JSONArray fileTypes = value.getJSONArray("file_types");
                final JSONArray createDates = value.getJSONArray("create_date");

                for (int i = 0; i < mediaIds.length(); i++) {
                    final String mediaId = mediaIds.getString(i);
                    final String fileName = fileNames.getString(i);
                    final String fileType = fileTypes.getString(i);
                    final Long createDate = createDates.getLong(i);

                    View mediaFrame = LayoutInflater.from(getContext())
                            .inflate(R.layout.small_image_with_frame_layout, mediaContainer, false);
                    if (fileType.contains("image")) {
                        ((ImageView) mediaFrame.findViewById(R.id.media_image)).setImageResource(R.drawable.image_placeholder);
                        ((TextView) mediaFrame.findViewById(R.id.media_date)).setText(dateFormat.format(new Date(createDate)));
                        mediaFrame.findViewById(R.id.media_date).setVisibility(View.VISIBLE);
                    } else if (fileType.contains("video"))
                        ((ImageView) mediaFrame.findViewById(R.id.media_image)).setImageResource(R.drawable.video_placeholder);
                    else if (fileType.contains("audio"))
                        ((ImageView) mediaFrame.findViewById(R.id.media_image)).setImageResource(R.drawable.audio_placeholder);
                    else
                        ((ImageView) mediaFrame.findViewById(R.id.media_image)).setImageResource(R.drawable.file_placeholder);

                    mediaFrame.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            new OpenFileTask(mediaId, fileName).execute();
                        }
                    });
                    mediaContainer.addView(mediaFrame);

                }
            }


        } else {
            ((LinearLayout) mediaLayout.getChildAt(0)).getChildAt(0).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    expandableLayout.toggle();
                }
            });

            buttonsContainer.getChildAt(0).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (checkExternalPermissions() && checkCameraPermissions()) {
                        try {
                            currentMediaBlock = new Pair<>(mediaLayout, value);
                            showGalleryOrPhotoPickDialog();
                            /*
                            Pair<Intent, File> intentFilePair = BitmapService.getPickImageIntent(getContext());
                            cameraImagePath = intentFilePair.second.getAbsolutePath();
                            startActivityForResult(intentFilePair.first, PHOTO_REQUEST_CODE);*/
                        } catch (Exception exc) {
                            Toast.makeText(getContext(), R.string.no_camera, Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        requestCameraPermissions();
                    }
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
                            Toast.makeText(getContext(), R.string.no_camera, Toast.LENGTH_SHORT).show();
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
                            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            startActivityForResult(intent, AUDIO_REQUEST_CODE);
                        } catch (Exception exc) {
                            exc.printStackTrace();
                            Toast.makeText(getContext(), R.string.no_audio_recorder_app, Toast.LENGTH_SHORT).show();
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
                            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            intent.addCategory(Intent.CATEGORY_OPENABLE);

                            Intent sIntent = new Intent("com.sec.android.app.myfiles.PICK_DATA");
                            sIntent.addCategory(Intent.CATEGORY_DEFAULT);

                            Intent chooserIntent;
                            if (getActivity().getPackageManager().resolveActivity(sIntent, 0) != null) {
                                // it is device with samsung file manager
                                chooserIntent = Intent.createChooser(sIntent, getString(R.string.to_select_file));
                                chooserIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                                chooserIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                chooserIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{intent});
                            } else
                                chooserIntent = Intent.createChooser(intent, getString(R.string.to_select_file));

                            startActivityForResult(chooserIntent, DOCUMENT_REQUEST_CODE);

                        } catch (Exception exc) {
                            Toast.makeText(getContext(), R.string.no_file_manager, Toast.LENGTH_SHORT).show();
                        }
                    } else
                        requestExternalPermissions();
                }
            });
        }

        container.addView(boxInContainerWithId(mediaLayout, value.getString("id")));

        currentMediaBlock = new Pair<>(mediaLayout, value);

        if (value.has(MEDIA_PATH_JSON_ARRAY)) {
            JSONArray valuesArray = value.getJSONArray(MEDIA_PATH_JSON_ARRAY);
            boolean needToRestart = true;
            while (needToRestart) {
                needToRestart = false;
                for (int j = 0; j < valuesArray.length(); j++) {
                    String mediaPath = valuesArray.getJSONObject(j).getString("path");
                    if (!new File(mediaPath).exists()) {
                        valuesArray = Utils.removeItemAt(valuesArray, j);
                        needToRestart = true;
                        break;
                    }

                }
            }
            value.put(MEDIA_PATH_JSON_ARRAY, valuesArray);
        }

        if (value.has(MEDIA_PATH_JSON_ARRAY)) {
            for (int j = 0; j < value.getJSONArray(MEDIA_PATH_JSON_ARRAY).length(); j++) {
                String mediaPath = value.getJSONArray(MEDIA_PATH_JSON_ARRAY).getJSONObject(j).getString("path");
                String mimeType = value.getJSONArray(MEDIA_PATH_JSON_ARRAY).getJSONObject(j).getString("mime");

                if (mimeType.startsWith("image"))
                    processImageFile(new File(mediaPath), true, new Pair<>(mediaLayout, value), false);
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

                                    if (optional.has("caption")) {
                                        TextView caption = new TextView(getContext());
                                        caption.setText(optional.getString("caption"));
                                        caption.setTextColor(ContextCompat.getColor(getContext(), R.color.accent));
                                        caption.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
                                        optionalContainer.addView(caption);
                                        int paddings = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, getResources().getDisplayMetrics());
                                        caption.setPadding(paddings, paddings, paddings, paddings / 3);
                                    }

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
                ViewGroup selectBtnFrame = (ViewGroup) getActivity().getLayoutInflater().inflate(R.layout.select_btn, selectBtnContainer, false);

                final CheckBox selectBtn = (CheckBox) selectBtnFrame.getChildAt(0);
                TextView selectBtnText = (TextView) selectBtnFrame.getChildAt(2);
                if (option.has("caption"))
                    selectBtnText.setText(option.getString("caption"));
                else
                    selectBtnText.setText(R.string.no_text_);

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
                if (!isTaskFinished)
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
        if (isTaskFinished)
            showPopupSelectListBtn.setVisibility(View.GONE);

        if (isList) {
            applySelectResultsInListView(value, selectBtnContainer);

            if (!isTaskFinished)
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
                        ViewGroup selectBtnFrame = (ViewGroup) getActivity().getLayoutInflater().inflate(R.layout.select_btn, answersContainer, false);
                        CheckBox selectBtn = (CheckBox) selectBtnFrame.getChildAt(0);
                        TextView selectBtnText = (TextView) selectBtnFrame.getChildAt(2);

                        if (option.has("caption"))
                            selectBtnText.setText(option.getString("caption"));
                        else
                            selectBtnText.setText(R.string.no_text_);

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
                ((TextView) ((ViewGroup) checkButton.getParent()).getChildAt(2)).setTextColor(Color.WHITE);

                if (buttonJson.has("color"))
                    bgShape.setColor(Color.parseColor(buttonJson.getString("color")));
                else
                    bgShape.setColor(Color.parseColor("#c99fe3"));
            } else {
                bgShape.setColor(Color.WHITE);
                ((TextView) ((ViewGroup) checkButton.getParent()).getChildAt(2)).setTextColor(Color.parseColor("#4c3e60"));
            }


            ((ViewGroup) checkButton.getParent()).getChildAt(1).setBackground(bgShape);
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

    private boolean checkLocationPermissions() {
        int permissionCoarse = ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION);
        int permissionFine = ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION);
        return (permissionCoarse == PackageManager.PERMISSION_GRANTED && permissionFine == PackageManager.PERMISSION_GRANTED);
    }

    private boolean checkExternalPermissions() {
        int permissionCheckWrite = ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int permissionCheckRead = ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE);
        return (permissionCheckRead == PackageManager.PERMISSION_GRANTED && permissionCheckWrite == PackageManager.PERMISSION_GRANTED);
    }

    private boolean checkCameraPermissions() {
        int permissionCheckCamera = ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA);
        return (permissionCheckCamera == PackageManager.PERMISSION_GRANTED);
    }

    private boolean checkAudioPermissions() {
        return (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED)
                && checkExternalPermissions();
    }

    private void requestLocationPermissions() {
        ActivityCompat.requestPermissions(getActivity(),
                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, 0);
    }

    private void requestExternalPermissions() {
        ActivityCompat.requestPermissions(getActivity(),
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
    }

    private void requestCameraPermissions() {
        ActivityCompat.requestPermissions(getActivity(),
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA}, 0);
    }

    private void requestAudioPermissions() {
        ActivityCompat.requestPermissions(getActivity(),
                new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
    }

    private ViewGroup boxInContainerWithId(ViewGroup content, String tag) {
        int topAndBottomPaddings = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, getResources().getDisplayMetrics());
        FrameLayout boxLayout = new FrameLayout(getContext());
        boxLayout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        boxLayout.addView(content);
        boxLayout.setTag(tag);
        boxLayout.setPadding(0, topAndBottomPaddings, 0, topAndBottomPaddings);
        return boxLayout;
    }

    private void setBlockMarkedAsRequired(String tag) {
        if (!requiredElementTags.contains(tag))
            requiredElementTags.add(tag);

        ViewGroup container = (ViewGroup) rootView.findViewById(R.id.root_container).findViewWithTag(tag);

        if (container != null)
            container.setBackgroundColor(Color.parseColor("#FEDADA"));
    }

    private void setCategoryMarkedAsRequired(String tag) {
        if (!requiredCategoriesTags.contains(tag))
            requiredCategoriesTags.add(tag);

        ViewGroup categoryHeader = getCategoryByTag(tag + "_head");
        ViewGroup categoryContainer = getCategoryByTag(tag + "_cont");

        if (categoryHeader != null)
            ((LayerDrawable) categoryHeader.getBackground()).findDrawableByLayerId(R.id.background).setColorFilter(Color.parseColor("#FEDADA"), PorterDuff.Mode.SRC_IN);

        if (categoryContainer != null)
            ((LayerDrawable) categoryContainer.getBackground()).findDrawableByLayerId(R.id.background).setColorFilter(Color.parseColor("#FEDADA"), PorterDuff.Mode.SRC_IN);
    }

    private void resetCategoryColor(String tag, int offset) {
        requiredCategoriesTags.remove(tag);
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
        return (ViewGroup) rootView.findViewById(R.id.root_container).findViewWithTag(tag);

    }

    private void resetRequiredBlock(String tag) {
        requiredElementTags.remove(tag);

        ViewGroup container = (ViewGroup) rootView.findViewById(R.id.root_container).findViewWithTag(tag);
        if (container != null)
            container.setBackgroundColor(Color.TRANSPARENT);
    }

    private ViewGroup createImgFrame(Bitmap photo) {
        LinearLayout imageContainer = (LinearLayout) ((HorizontalScrollView) currentMediaBlock.first.getChildAt(1)).getChildAt(0);
        ViewGroup imageFrame = (ViewGroup) LayoutInflater.from(getContext()).inflate(R.layout.image_with_frame_layout, imageContainer, false);
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
                        if (Utils.isImage(currentValue.getJSONArray(MEDIA_PATH_JSON_ARRAY).getJSONObject(i).getString("path")))
                            imageSrc.put(currentValue.getJSONArray(MEDIA_PATH_JSON_ARRAY).getJSONObject(i).getString("path"));

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
                        Toast.makeText(getContext(), R.string.no_app_to_open, Toast.LENGTH_LONG).show();
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
                addMediaPath(filePathInCache, Utils.getMimeType(finalFilePathInCache));

            fileContainer.addView(fileLayout);

        } catch (Exception exc) {
            Toast.makeText(getContext(), getString(R.string.error_adding_file) + exc.getLocalizedMessage(), Toast.LENGTH_LONG).show();
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

            String fileName = getString(R.string.record) + new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault()).format(
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
                        Toast.makeText(getContext(), R.string.no_audio_app, Toast.LENGTH_LONG).show();
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
                addMediaPath(audioPathInCache, Utils.getMimeType(audioPathInCache));

            audioContainer.addView(audioLayout);

        } catch (Exception exc) {
            exc.printStackTrace();
            Toast.makeText(getContext(), getString(R.string.error_adding_audio) + exc.getLocalizedMessage(), Toast.LENGTH_LONG).show();
        }

    }

    private void createVideoPreview(final String path, boolean copyToCache) {
        try {
            String videoPathInCache;

            if (copyToCache) {
                videoPathInCache = getContext().getExternalCacheDir() + "/" + System.currentTimeMillis() + path.substring(path.lastIndexOf("."));
                Utils.copy(new File(path), new File(videoPathInCache));
                videoPathInCache = new File(videoPathInCache).getAbsolutePath();
                new VideoCompressor(videoPathInCache).execute();
                return;
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
                        Toast.makeText(getContext(), R.string.no_videoplayer, Toast.LENGTH_LONG).show();
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


        } catch (Exception exc) {
            Toast.makeText(getContext(), getString(R.string.error_adding_video) + exc.getLocalizedMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private class VideoCompressor extends AsyncTask<Void, Void, Boolean> {
        private String filePath;
        private String newFilePath;

        public VideoCompressor(String filePath) {
            this.filePath = filePath;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            loopDialog.show();
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            newFilePath = getContext().getExternalCacheDir() + "/" + UUID.randomUUID().toString() + ".mp4";
            return MediaController.getInstance().convertVideo2(filePath, newFilePath, getContext());
        }

        @Override
        protected void onPostExecute(Boolean compressed) {
            super.onPostExecute(compressed);
            try {
                String resPath = filePath;
                loopDialog.dismiss();
                if (compressed) {
                    resPath = newFilePath;
                    new File(filePath).delete();
                    Log.d("qweqw", "Compression successfully!");

                }

                addMediaPath(resPath, Utils.getMimeType(resPath));
                createVideoPreview(resPath, false);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void addMediaPath(String mediaPath, String mimeType) throws JSONException {
        if (!currentMediaBlock.second.has(MEDIA_PATH_JSON_ARRAY))
            currentMediaBlock.second.put(MEDIA_PATH_JSON_ARRAY, new JSONArray());

        JSONObject media = new JSONObject();
        media.put("path", mediaPath);
        media.put("mime", mimeType);

        currentMediaBlock.second.getJSONArray(MEDIA_PATH_JSON_ARRAY).put(media);
        saveTemplateState();
    }

    private void deleteMediaPath(JSONObject currentMediaBlock, String mediaPath) {
        try {
            if (currentMediaBlock.has(MEDIA_PATH_JSON_ARRAY)) {
                JSONArray tmp = new JSONArray();
                for (int j = 0; j < currentMediaBlock.getJSONArray(MEDIA_PATH_JSON_ARRAY).length(); j++)
                    if (!currentMediaBlock.getJSONArray(MEDIA_PATH_JSON_ARRAY).getJSONObject(j).getString("path").equals(mediaPath))
                        tmp.put(currentMediaBlock.getJSONArray(MEDIA_PATH_JSON_ARRAY).getJSONObject(j));
                currentMediaBlock.put(MEDIA_PATH_JSON_ARRAY, tmp);
            }
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

    private void printLog(String TAG, String message) {
        int maxLogSize = 1000;
        for (int i = 0; i <= message.length() / maxLogSize; i++) {
            int start = i * maxLogSize;
            int end = (i + 1) * maxLogSize;
            end = end > message.length() ? message.length() : end;
            Log.v(TAG, message.substring(start, end));
        }
    }

    private JSONObject transformFinishedTemplate(JSONObject finishedTemplate) {
        try {
            SimpleDateFormat parseFormat = new SimpleDateFormat("yyyy.dd.MM HH:mm::ss", Locale.getDefault());

            JSONArray values = finishedTemplate.getJSONObject("document").getJSONArray("values");
            JSONObject template = finishedTemplate.getJSONObject("template");

            for (int i = 0; i < values.length(); i++) {
                JSONObject value;
                String elemType;
                String elemId;
                String elemValue;
                try {
                    value = values.getJSONObject(i);
                    elemType = value.getString("type");
                    elemId = value.getString("id");
                    elemValue = value.getString("value");
                } catch (Exception exc) {
                    continue;
                }
                if (elemType == null || elemId == null || elemValue == null)
                    continue;


                JSONObject item = findTemplateElementById(template, elemId);
                String elementName = getTemplateElementNameById(template, elemId);

                if (item == null)
                    continue;

                switch (elemType) {
                    case "question":
                        if (item.has("values"))
                            item.getJSONArray("values").put(elemValue);
                        else {
                            JSONArray valuesArray = new JSONArray();
                            valuesArray.put(elemValue);
                            item.put("values", valuesArray);
                        }
                        break;

                    case "text":
                    case "slider":
                    case "float":
                        item.put("value", elemValue);
                        break;

                    case "barcode":
                        item.put("barcode", elemValue);
                        break;

                    case "checkbox":
                        boolean boolValue = Boolean.parseBoolean(elemValue);
                        item.put("value", boolValue);
                        break;

                    case "plan":
                        item.put("_plan", value.getDouble("value"));
                        break;

                    case "fact":
                        item.put("_fact", value.getDouble("value"));
                        break;

                    case "media_id":
                        if (elementName == null)
                            break;

                        if (elementName.equals("signature")) {
                            downloadSignature(item, elemValue);
                        } else {
                            if (item.has("media_ids")) {
                                item.getJSONArray("media_ids").put(elemValue);
                                item.getJSONArray("file_names").put(value.getString("fileName"));
                                item.getJSONArray("file_types").put(value.getString("mimeType"));
                                try {
                                    item.getJSONArray("create_date").put(parseFormat.parse(value.getString("create_date")).getTime());
                                } catch (ParseException e) {
                                    item.getJSONArray("create_date").put(new Date().getTime());
                                }
                            } else {
                                JSONArray mediaIds = new JSONArray();
                                mediaIds.put(elemValue);
                                item.put("media_ids", mediaIds);

                                JSONArray fileNames = new JSONArray();
                                fileNames.put(value.getString("fileName"));
                                item.put("file_names", fileNames);

                                JSONArray fileTypes = new JSONArray();
                                fileTypes.put(value.getString("mimeType"));
                                item.put("file_types", fileTypes);

                                JSONArray createDates = new JSONArray();
                                try {
                                    createDates.put(parseFormat.parse(value.getString("create_date")).getTime());
                                } catch (ParseException e) {
                                    createDates.put(new Date().getTime());
                                }
                                item.put("create_date", createDates);
                            }
                        }
                }
            }
            if (template.has("analytic_value"))
                template.put("analytic_value", finishedTemplate.getJSONObject("document").getJSONObject("analytic_value"));
            return template;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void downloadSignature(JSONObject signature, String mediaId) {
        try {
            if (mediaId.contains(":"))
                mediaId = mediaId.substring(mediaId.lastIndexOf(":") + 1);
            String url = "/api/file/get/" + mediaId;
            Response response = RequestService.createGetRequest(url);
            File resultFile = new File(getContext().getCacheDir(), mediaId);
            if (!resultFile.exists()) {
                if (response.code() == 200) {
                    BufferedSink sink = Okio.buffer(Okio.sink(resultFile));
                    sink.writeAll(response.body().source());
                    sink.close();
                }
                response.body().close();
            }

            if (resultFile.exists()) {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inPreferredConfig = Config.ARGB_8888;
                Bitmap bitmap = BitmapFactory.decodeFile(resultFile.getAbsolutePath(), options);
                signature.put(SIGNATURE_PREVIEW_JSON_ARRAY, BitmapService.getBitmapBytesEncodedBase64(bitmap));
            }
        } catch (Exception exc) {
            exc.printStackTrace();
        }
    }

    private JSONObject findTemplateElementById(JSONObject template, String id) {
        try {
            JSONArray pages = template.getJSONArray("items");
            for (int i = 0; i < pages.length(); i++) {
                JSONArray items = pages.getJSONObject(i).getJSONObject("page").getJSONArray("items");
                JSONObject foundedElement = findJsonElementById(items, id);
                if (foundedElement != null)
                    return foundedElement;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    private JSONObject findJsonElementById(JSONArray items, String id) {
        try {
            for (int i = 0; i < items.length(); i++) {
                JSONObject item = items.getJSONObject(i);

                String elementName = null;
                Iterator<String> iterator = item.keys();
                if (iterator.hasNext())
                    elementName = iterator.next();

                if (elementName == null)
                    continue;

                JSONObject foundedItem;
                switch (elementName) {
                    case "category":
                        foundedItem = findJsonElementById(item.getJSONObject(elementName).getJSONArray("items"), id);
                        if (foundedItem != null)
                            return foundedItem;
                        break;

                    case "question":
                        JSONObject question = item.getJSONObject(elementName);
                        if (question.has("id") && question.getString("id").equals(id))
                            return question;
                        else if (question.has("optionals")) {
                            JSONArray optionals = question.getJSONArray("optionals");
                            for (int j = 0; j < optionals.length(); j++) {
                                foundedItem = findJsonElementById(optionals.getJSONObject(j).getJSONObject("optional").getJSONArray("items"), id);
                                if (foundedItem != null)
                                    return foundedItem;
                            }
                        }
                        break;

                    default:
                        if (item.getJSONObject(elementName).has("id") && item.getJSONObject(elementName).getString("id").equals(id))
                            return item.getJSONObject(elementName);
                        break;
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;

    }

    private String getTemplateElementNameById(JSONObject template, String id) {
        try {
            JSONArray pages = template.getJSONArray("items");
            for (int i = 0; i < pages.length(); i++) {
                JSONArray items = pages.getJSONObject(i).getJSONObject("page").getJSONArray("items");
                String foundedElementName = findJsonElementNameById(items, id);
                if (foundedElementName != null)
                    return foundedElementName;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String findJsonElementNameById(JSONArray items, String id) {
        try {
            for (int i = 0; i < items.length(); i++) {
                JSONObject item = items.getJSONObject(i);

                String elementName = null;
                Iterator<String> iterator = item.keys();
                if (iterator.hasNext())
                    elementName = iterator.next();

                if (elementName == null)
                    continue;

                String foundedItem;
                switch (elementName) {
                    case "category":
                        foundedItem = findJsonElementNameById(item.getJSONObject(elementName).getJSONArray("items"), id);
                        if (foundedItem != null)
                            return foundedItem;
                        break;

                    case "question":
                        JSONObject question = item.getJSONObject(elementName);
                        if (question.has("id") && question.getString("id").equals(id))
                            return elementName;
                        else if (question.has("optionals")) {
                            JSONArray optionals = question.getJSONArray("optionals");
                            for (int j = 0; j < optionals.length(); j++) {
                                foundedItem = findJsonElementNameById(optionals.getJSONObject(j).getJSONObject("optional").getJSONArray("items"), id);
                                if (foundedItem != null)
                                    return foundedItem;
                            }
                        }
                        break;

                    default:
                        if (item.getJSONObject(elementName).has("id") && item.getJSONObject(elementName).getString("id").equals(id))
                            return elementName;
                        break;
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;

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
                if (isTaskFinished) {
                    String url = "/api/tasks/" + taskId;
                    Response response = RequestService.createGetRequest(url);
                    if (response.code() == 200) {
                        String responseStr = response.body().string();
                        JSONObject responseJson = new JSONObject(responseStr);
                        documentId = responseJson.getLong("document_id");

                        template = transformFinishedTemplate(responseJson);
                    }
                    return response.code();
                } else {
                    String templateJson = "";

                    if (!isOpenLink) {
                        SharedPreferences mSettings;
                        mSettings = App.getContext().getSharedPreferences("templates", Context.MODE_PRIVATE);
                        templateJson = mSettings.getString(taskId + LoginHistoryService.getCurrentUser().username, "");
                    }

                    if (templateJson != null && !templateJson.equals("")) {
                        template = new JSONObject(templateJson);
                        return HttpURLConnection.HTTP_OK;
                    } else {
                        String linkPath = isReportOpenLink ? "closedlink" : "openlink";
                        String url = isOpenLink ? "/api/" + linkPath + "/" + taskUUID : "/api/tasks/" + taskId;
                        Response response = RequestService.createGetRequest(url);

                        if (response.code() == 200) {
                            String responseStr = response.body().string();
                            JSONObject responseJson = new JSONObject(responseStr);

                            template = responseJson.getJSONObject("template");
                            template.put("StartTime", isoDateFormat.format(new Date()));
                            if (responseJson.has("expire_time"))
                                template.put("DueTime", isoDateFormat.format(new Date(responseJson.getLong("expire_time"))));
                        }
                        return response.code();
                    }
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
                    if (isOpenLink) {
                        Toast.makeText(getContext(), "Задача недоступна", Toast.LENGTH_SHORT).show();
                        getActivity().getSupportFragmentManager().popBackStack();
                        return;
                    }
                    Toast.makeText(getContext(), R.string.unknown_error, Toast.LENGTH_LONG).show();
                    getActivity().getSupportFragmentManager().popBackStack();
                }

            } catch (Exception exc) {
                exc.printStackTrace();
            }
        }
    }

    private class WaitForMediaReadyTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (waiting)
                loopDialog.show();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            while (waiting) {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            loopDialog.dismiss();

            Pair<Boolean, ArrayList<JSONObject>> result = checkIfTemplateIsFilled(template);

            if (result.first) {
                new SendMediaTask(result.second).execute();
            } else
                Toast.makeText(getContext(), R.string.not_all_required_fields_are_filled, Toast.LENGTH_LONG).show();
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
                if (waiting) {
                    while (waiting) {
                        Thread.sleep(250);
                    }
                }
                int sentCt = 0;

                if (!isOpenLink) {
                    Response tokenResponse = RequestService.createGetRequest("/api/user/");
                    if (tokenResponse.code() != 202 && tokenResponse.code() != 200) {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                new MaterialDialog.Builder(getContext())
                                        .title(R.string.accept_account)
                                        .content(R.string.input_pass)
                                        .inputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD)
                                        .autoDismiss(false)
                                        .input(getString(R.string.pass), "", new MaterialDialog.InputCallback() {
                                            @Override
                                            public void onInput(@androidx.annotation.NonNull MaterialDialog dialog, CharSequence input) {
                                                if (input.length() > 0) {
                                                    new LoginTask(LoginHistoryService.getCurrentUser().username, input.toString()).execute();
                                                    dialog.dismiss();
                                                }
                                            }
                                        })
                                        .positiveText(R.string.done)
                                        .negativeText(R.string.cancel_)
                                        .onNegative(new MaterialDialog.SingleButtonCallback() {
                                            @Override
                                            public void onClick(@androidx.annotation.NonNull MaterialDialog dialog, @androidx.annotation.NonNull DialogAction which) {
                                                Toast.makeText(getContext(), "bbb", Toast.LENGTH_LONG).show();
                                                dialog.dismiss();
                                            }
                                        })
                                        .show();
                            }
                        });
                        return -102;
                    }
                }

                String linkPath = isReportOpenLink ? "closedlink" : "openlink";
                String mediaUploadUrl = isOpenLink ? "/api/" + linkPath + "/" + taskUUID + "/upload" : "/api/file/upload";

                successfullySentMediaCt = 0;
                for (JSONObject jsonObject : mediaObjects)
                    if (jsonObject.has(MEDIA_PATH_JSON_ARRAY))
                        for (int i = 0; i < jsonObject.getJSONArray(MEDIA_PATH_JSON_ARRAY).length(); i++) {
                            if (!jsonObject.has("sent_medias") ||
                                    (jsonObject.has("sent_medias") && !Utils.containsValue(jsonObject.getJSONArray("sent_medias"), jsonObject.getJSONArray(MEDIA_PATH_JSON_ARRAY).getString(i)))) {
                                JSONObject media = jsonObject.getJSONArray(MEDIA_PATH_JSON_ARRAY).getJSONObject(i);
                                String mediaPath = media.getString("path");
                                String mimeType = media.getString("mime");
                                try {
                                    File mediaFile = new File(mediaPath);

                                    final Response response = RequestService.createSendFilePostRequest(mediaUploadUrl, MediaType.parse(mimeType), mediaFile);

                                    if (response.code() == 200) {
                                        String mediaId = "attach:" + new JSONObject(response.body().string()).getString("id");

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
                                    } else
                                        getActivity().runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                Toast.makeText(getContext(), getString(R.string.send_error) + response.code(), Toast.LENGTH_LONG).show();
                                            }
                                        });


                                    Log.d("mojo-log", String.valueOf(response.code()));
                                } catch (final Exception exc) {
                                    exc.printStackTrace();
                                    getActivity().runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(getContext(), getString(R.string.send_exc) + exc.getMessage(), Toast.LENGTH_LONG).show();
                                        }
                                    });
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
                            Response response = RequestService.createSendFilePostRequest(mediaUploadUrl, MediaType.parse("image/jpg"), mediaFile);
                            if (response.code() == 200) {
                                mediaFile.delete();
                                String mediaId = "attach:" + new JSONObject(response.body().string()).getString("id");
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
            if (progressDialog != null && progressDialog.isShowing())
                progressDialog.dismiss();

            if (result == -102)
                return;

            try {
                if (successfullySentMediaCt < totalSize) {
                    saveTemplateState();

                    Toast.makeText(getContext(), R.string.error_not_all_images_were_sent, Toast.LENGTH_SHORT).show();
                } else
                    new CompleteTemplateTask().execute();
            } catch (Exception exc) {
                exc.printStackTrace();
            }
        }
    }

    public class LoginTask extends AsyncTask<Void, Void, Integer> {
        private String username, password;
        private User user;


        public LoginTask(String username, String password) {
            this.username = username;
            this.password = password;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            loopDialog.show();
        }

        @Override
        protected Integer doInBackground(Void... params) {
            try {
                JSONObject requestJson = new JSONObject();
                requestJson.put("username", username);
                requestJson.put("password", password);

                Response response = RequestService.createPostRequest("/api/user/login", requestJson.toString());

                if (response.code() == 202 || response.code() == 200) {
                    String userJson = response.body().string();
                    user = new ObjectMapper().readValue(userJson, User.class);
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
                if (loopDialog != null && loopDialog.isShowing())
                    loopDialog.dismiss();

                if (responseCode == null)
                    Toast.makeText(getContext(), R.string.network_error, Toast.LENGTH_LONG).show();
                else if (responseCode == 401)
                    Toast.makeText(getContext(), R.string.invalid_username_or_password, Toast.LENGTH_LONG).show();
                else if (responseCode == 202 || responseCode == 200) {
                    if (user == null) {
                        Toast.makeText(getContext(), getString(R.string.unknown_error), Toast.LENGTH_LONG).show();
                        return;
                    }
                    LoginHistoryService.setCurrentUser(user);
                    LoginHistoryService.addUser(user);
                    TokenService.updateToken(user.token, user.username);

                    new WaitForMediaReadyTask().execute();
                } else
                    Toast.makeText(getContext(), getString(R.string.unknown_error) + "  code: " + responseCode, Toast.LENGTH_LONG).show();
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
            loopDialog.show();
            try {
                resultJson = new JSONObject();
                JSONArray values = getTemplateElementValues(template);
                resultJson.put("values", values);

                if (template.has("StartTime"))
                    resultJson.put("start_time", isoDateFormat.parse(template.getString("StartTime")).getTime() / 1000);
                else
                    resultJson.put("start_time", new Date().getTime() / 1000);

                resultJson.put("complete_time", new Date().getTime() / 1000);

                TimeZone timeZone = TimeZone.getDefault();
                resultJson.put("timezone", timeZone.getID());
                resultJson.put("client_id", "android v." + Build.VERSION.CODENAME + " app v. " + BuildConfig.VERSION_NAME);

                Log.d("mojo-log", "result template: " + resultJson.toString());
            } catch (Exception exc) {
                exc.printStackTrace();
            }
        }

        @Override
        protected Integer doInBackground(Void... params) {
            try {
                String linkPath = isReportOpenLink ? "closedlink" : "openlink";
                String url = isOpenLink ? "/api/" + linkPath + "/" + taskUUID + "/complete" : "/api/tasks/" + taskId + "/complete";
                Response response = RequestService.createPostRequest(url, resultJson.toString());
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

                saveTemplateState();

                if (responseCode == null)
                    Toast.makeText(getContext(), R.string.network_error, Toast.LENGTH_LONG).show();
                else if (responseCode == 401) {
                    startActivity(new Intent(getContext(), AuthActivity.class));
                    getActivity().finish();
                } else if (responseCode == 200) {
                    if (!isOpenLink) {
                        SharedPreferences mSettings;
                        mSettings = App.getContext().getSharedPreferences("templates", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = mSettings.edit();
                        editor.putString(taskId + LoginHistoryService.getCurrentUser().username, "");
                        editor.apply();
                    }

                    if (getActivity() != null && getActivity().getSupportFragmentManager().findFragmentByTag("tasks") != null) {
                        ((TasksFragment) getActivity().getSupportFragmentManager().findFragmentByTag("tasks")).needUpdate = true;
                        if (getActivity() instanceof MainActivity) {
                            ((MainActivity) getActivity()).drawer.getDrawerLayout().setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
                        }
                        getActivity().getSupportFragmentManager().popBackStack();
                    } else {
                        getActivity().finish();
                    }

                } else if (responseCode == 404) {
                    removeTemplate();
                    if (getActivity() != null && getActivity().getSupportFragmentManager().findFragmentByTag("tasks") != null) {
                        ((TasksFragment) getActivity().getSupportFragmentManager().findFragmentByTag("tasks")).needUpdate = true;
                        if (getActivity() instanceof MainActivity) {
                            ((MainActivity) getActivity()).drawer.getDrawerLayout().setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
                        }
                        getActivity().getSupportFragmentManager().popBackStack();
                    } else {
                        getActivity().finish();
                    }

                    Toast.makeText(getContext(), R.string.no_task_error, Toast.LENGTH_LONG).show();
                } else
                    Toast.makeText(getContext(), R.string.unknown_error, Toast.LENGTH_LONG).show();
            } catch (Exception exc) {
                exc.printStackTrace();
            }
        }
    }

    private void removeTemplate() {
        if (isOpenLink) {
            return;
        }

        SharedPreferences mSettings;
        mSettings = App.getContext().getSharedPreferences("templates", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mSettings.edit();
        editor.putString(taskId + LoginHistoryService.getCurrentUser().username, "");
        editor.apply();
    }

    @SuppressLint("CheckResult")
    private void processImageFile(File file, final boolean isRestore, final Pair<LinearLayout, JSONObject> toBlock, final boolean isFolder) {
        final int imgSize = Math.round(TypedValue.applyDimension
                (TypedValue.COMPLEX_UNIT_DIP, 93, App.getContext().getResources().getDisplayMetrics()));
        final int bigImgSize = 1300;

        if (!isRestore)
            waiting = true;

        Observable.just(file)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .flatMap(new Function<File, ObservableSource<Pair<Bitmap, String>>>() {
                    @Override
                    public ObservableSource<Pair<Bitmap, String>> apply(@NonNull File file) throws Exception {
                        String picturePath = file.getAbsolutePath();

                        BitmapFactory.Options tmpOptions = new BitmapFactory.Options();
                        BitmapFactory.Options options = new BitmapFactory.Options();

                        tmpOptions.inJustDecodeBounds = true;
                        BitmapFactory.decodeFile(picturePath, tmpOptions);
                        options.inSampleSize = BitmapService.calculateInSampleSize(tmpOptions, imgSize);
                        options.inJustDecodeBounds = false;

                        Bitmap bitmap = BitmapFactory.decodeFile(picturePath, options);
                        bitmap = BitmapService.modifyOrientation(bitmap, picturePath);


                        String resPicturePath = picturePath;
                        if (!isRestore) {
                            try {
                                tmpOptions = new BitmapFactory.Options();
                                options = new BitmapFactory.Options();

                                tmpOptions.inJustDecodeBounds = true;
                                BitmapFactory.decodeFile(picturePath, tmpOptions);
                                options.inSampleSize = BitmapService.calculateInSampleSize(tmpOptions, bigImgSize);
                                options.inJustDecodeBounds = false;

                                Bitmap resBitmap = BitmapFactory.decodeFile(picturePath, options);
                                resBitmap = BitmapService.modifyOrientation(resBitmap, picturePath);

                                SimpleDateFormat watermarkDateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault());
                                resBitmap = drawWaterMarkOnBitmap(resBitmap, watermarkDateFormat.format(new Date()), isFolder);

                                //resFile.delete();
                                File newFile = BitmapService.saveBitmapToFile(getContext(), resBitmap);
                                resPicturePath = newFile.getAbsolutePath();

                                ExifInterface exif = new ExifInterface(resPicturePath);

                                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy:MM:dd", Locale.getDefault());
                                SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());

                                exif.setAttribute(ExifInterface.TAG_DATETIME, dateFormat.format(new Date()) + " " + timeFormat.format(new Date()));
                                exif.setAttribute(ExifInterface.TAG_EXPOSURE_TIME, dateFormat.format(new Date()) + " " + timeFormat.format(new Date()));

                                exif.setAttribute(ExifInterface.TAG_GPS_DATESTAMP, dateFormat.format(new Date()));
                                exif.setAttribute(ExifInterface.TAG_GPS_TIMESTAMP, timeFormat.format(new Date()));
                                exif.saveAttributes();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        return Observable.just(new Pair<>(bitmap, resPicturePath));
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        new Consumer<Pair<Bitmap, String>>() {
                            @Override
                            public void accept(@NonNull Pair<Bitmap, String> bitmapStringPair) throws Exception {
                                ViewGroup imageContainer = createImgFrame(bitmapStringPair.first);
                                imageContainer.setTag(bitmapStringPair.second);

                                if (toBlock != null && toBlock.first != null) {
                                    ((LinearLayout) ((HorizontalScrollView) toBlock.first.getChildAt(1)).getChildAt(0)).addView(imageContainer);
                                } else {
                                    if (currentMediaBlock == null || currentMediaBlock.first == null)
                                        return;
                                    ((LinearLayout) ((HorizontalScrollView) currentMediaBlock.first.getChildAt(1)).getChildAt(0)).addView(imageContainer);
                                }

                                if (!isRestore) {
                                    addMediaPath(bitmapStringPair.second, Utils.getMimeType(bitmapStringPair.second));
                                    waiting = false;
                                }
                            }
                        },
                        new Consumer<Throwable>() {
                            @Override
                            public void accept(@NonNull Throwable throwable) throws Exception {
                                Toast.makeText(getContext(), getString(R.string.error_processing_photo) + throwable.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        });
    }

    public Bitmap drawWaterMarkOnBitmap(Bitmap bitmap, String mText, boolean isFolder) {
        try {
            Config bitmapConfig = bitmap.getConfig();
            if (bitmapConfig == null) {
                bitmapConfig = Config.ARGB_8888;
            }
            bitmap = bitmap.copy(bitmapConfig, true);

            Canvas canvas = new Canvas(bitmap);
            Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            paint.setColor(Color.parseColor("#58317f"));
            paint.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 15, getContext().getResources().getDisplayMetrics()));
            paint.setShadowLayer(1f, 0f, 1f, Color.DKGRAY);

            Rect bounds = new Rect();
            paint.getTextBounds(mText, 0, mText.length(), bounds);
            int x = (bitmap.getWidth() - bounds.width() - 25);
            int y = (bitmap.getHeight() - bounds.height());

            canvas.drawText(mText, x, y, paint);

            Drawable d = getResources().getDrawable(R.drawable.watermark_logo);
            int imageSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 23, getContext().getResources().getDisplayMetrics());
            d.setBounds(25, 25, 25 + imageSize, 25 + imageSize);
            d.draw(canvas);

            if (isFolder) {
                Drawable folderLogo = getResources().getDrawable(R.drawable.ic_folder_watermark);
                int imageSizeFolder = 50;
                int xOffset = 20;
                int yOffset = 40;
                folderLogo.setBounds(x - imageSizeFolder - xOffset, y - yOffset, x - xOffset, y - yOffset + imageSizeFolder);
                folderLogo.draw(canvas);
            }

            return bitmap;
        } catch (Exception exc) {
            exc.printStackTrace();
            return null;
        }
    }

    private class OpenFileTask extends AsyncTask<Void, Void, Integer> {
        private java.io.File resultFile;
        private String mediaId;
        private String fileName;

        OpenFileTask(String mediaId, String fileName) {
            if (mediaId.contains(":"))
                mediaId = mediaId.substring(mediaId.lastIndexOf(":") + 1);
            this.mediaId = mediaId;
            this.fileName = fileName;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            loopDialog.show();

            resultFile = new File(getContext().getCacheDir(), fileName);
        }

        @Override
        protected Integer doInBackground(Void... params) {
            try {
                if (resultFile.exists())
                    return 200;

                String url = "/api/file/download/" + mediaId;
                Response response = RequestService.createGetRequest(url);

                if (response.code() == 200) {

                    BufferedSink sink = Okio.buffer(Okio.sink(resultFile));
                    sink.writeAll(response.body().source());
                    sink.close();
                }
                response.body().close();

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

                if (responseCode == null)
                    Toast.makeText(getContext(), R.string.network_error, Toast.LENGTH_LONG).show();
                else if (responseCode == 401) {
                    startActivity(new Intent(getContext(), AuthActivity.class));
                    getActivity().finish();
                } else if (responseCode == 200) {
                    try {
                        Intent viewIntent = new Intent(Intent.ACTION_VIEW);
                        Uri fileUri = FileProvider.getUriForFile(getContext(), getContext().getApplicationContext().getPackageName() + ".provider", resultFile);
                        viewIntent.setDataAndType(fileUri, Utils.getMimeType(resultFile.getAbsolutePath()));
                        viewIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        startActivity(viewIntent);
                    } catch (Exception exc) {
                        exc.printStackTrace();
                        Toast.makeText(getContext(), R.string.no_app_to_open, Toast.LENGTH_LONG).show();
                        try {
                            resultFile.delete();
                        } catch (Exception exc1) {
                            exc1.printStackTrace();
                        }
                    }
                } else
                    Toast.makeText(getContext(), R.string.unknown_error, Toast.LENGTH_LONG).show();
            } catch (Exception exc) {
                exc.printStackTrace();
            }
        }
    }

    private class DownloadPdfTask extends AsyncTask<Void, Void, Integer> {
        private java.io.File resultFile;
        private long documentId;
        private String name;

        DownloadPdfTask(long documentId, String name) {
            this.documentId = documentId;
            this.name = name;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            loopDialog.show();
            File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            resultFile = new File(downloadsDir, name + ".pdf");
        }

        @Override
        protected Integer doInBackground(Void... params) {
            try {
                if (resultFile.exists())
                    return 200;

                String url = "/api/fs-mojo/document/id/" + documentId + "/pdf";
                Response response = RequestService.createGetRequest(url);

                if (response.code() == 200) {
                    BufferedSink sink = Okio.buffer(Okio.sink(resultFile));
                    sink.writeAll(response.body().source());
                    sink.close();
                }
                response.body().close();

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

                if (responseCode == null)
                    Toast.makeText(getContext(), R.string.network_error, Toast.LENGTH_LONG).show();
                else if (responseCode == 401) {
                    startActivity(new Intent(getContext(), AuthActivity.class));
                    getActivity().finish();
                } else if (responseCode == 200) {
                    try {
                        Intent viewIntent = new Intent(Intent.ACTION_VIEW);
                        Uri fileUri = FileProvider.getUriForFile(getContext(), getContext().getApplicationContext().getPackageName() + ".provider", resultFile);
                        viewIntent.setDataAndType(fileUri, "application/pdf");
                        viewIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        startActivity(viewIntent);

                        Toast.makeText(getContext(), R.string.saved_to_gallery, Toast.LENGTH_LONG).show();
                    } catch (Exception exc) {
                        exc.printStackTrace();
                        Toast.makeText(getContext(), R.string.no_app_to_open, Toast.LENGTH_LONG).show();
                        try {
                            resultFile.delete();
                        } catch (Exception exc1) {
                            exc1.printStackTrace();
                        }
                    }
                } else
                    Toast.makeText(getContext(), R.string.unknown_error, Toast.LENGTH_LONG).show();
            } catch (Exception exc) {
                exc.printStackTrace();
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

    private Pair<Boolean, ArrayList<JSONObject>> checkIfContainerIsFilled(JSONArray dataJson,
                                                                          int offset) throws Exception {
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

                case "money":
                    if (!value.has("_plan") && !value.has("_fact")) {
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

                    if (userLocation != null) {
                        JSONObject latValue = new JSONObject();
                        latValue.put("id", "geo");
                        latValue.put("type", "latitude");
                        latValue.put("value", userLocation.getLatitude());

                        JSONObject lngValue = new JSONObject();
                        lngValue.put("id", "geo");
                        lngValue.put("type", "longitude");
                        lngValue.put("value", userLocation.getLongitude());

                        resultValues.put(latValue);
                        resultValues.put(lngValue);
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

                case "money":
                    if (value.has("_plan")) {
                        JSONObject objectValue = new JSONObject();
                        objectValue.put("id", value.getString("id"));
                        objectValue.put("value", value.getDouble("_plan"));
                        objectValue.put("type", "plan");
                        containerValues.put(objectValue);
                    }

                    if (value.has("_fact")) {
                        JSONObject objectValue = new JSONObject();
                        objectValue.put("id", value.getString("id"));
                        objectValue.put("value", value.getDouble("_fact"));
                        objectValue.put("type", "fact");
                        containerValues.put(objectValue);
                    }

                    if (value.has("barcode")) {
                        JSONObject objectValue = new JSONObject();
                        objectValue.put("id", value.getString("id"));
                        objectValue.put("value", value.getString("barcode"));
                        objectValue.put("type", "barcode");
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
                    if (value.has("barcode")) {
                        JSONObject objectValue = new JSONObject();
                        objectValue.put("id", value.getString("id"));
                        objectValue.put("value", value.getString("barcode"));
                        objectValue.put("type", "barcode");
                        containerValues.put(objectValue);
                    }

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

    public static class CustomXAxisRenderer extends XAxisRenderer {
        public CustomXAxisRenderer(ViewPortHandler viewPortHandler, XAxis xAxis, Transformer trans) {
            super(viewPortHandler, xAxis, trans);
        }

        @Override
        protected void drawLabel(Canvas c, String formattedLabel, float x, float y, MPPointF anchor, float angleDegrees) {
            if (formattedLabel == null || formattedLabel.isEmpty() || !formattedLabel.contains("\n")) {
                if (formattedLabel == null)
                    formattedLabel = "";
                com.github.mikephil.charting.utils.Utils.drawXAxisValue(c, formattedLabel, x, y, mAxisLabelPaint, anchor, angleDegrees);
            } else {
                String line[] = formattedLabel.split("\n");
                com.github.mikephil.charting.utils.Utils.drawXAxisValue(c, line[0], x, y, mAxisLabelPaint, anchor, angleDegrees);
                com.github.mikephil.charting.utils.Utils.drawXAxisValue(c, line[1], x, y + mAxisLabelPaint.getTextSize(), mAxisLabelPaint, anchor, angleDegrees);
            }
        }

    }

    @Override
    public void onStop() {
        super.onStop();
        saveTemplateState();
    }
}