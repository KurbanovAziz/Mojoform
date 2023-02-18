package org.dev_alex.mojo_qa.mojo.fragments;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.OrientationHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.material.button.MaterialButton;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnMonthChangedListener;

import org.dev_alex.mojo_qa.mojo.R;
import org.dev_alex.mojo_qa.mojo.activities.AuthActivity;
import org.dev_alex.mojo_qa.mojo.activities.MainActivity;
import org.dev_alex.mojo_qa.mojo.adapters.NotificationAdapter;
import org.dev_alex.mojo_qa.mojo.custom_views.MultiSpinner;
import org.dev_alex.mojo_qa.mojo.custom_views.RelativeLayoutWithPopUp;
import org.dev_alex.mojo_qa.mojo.models.Notification;
import org.dev_alex.mojo_qa.mojo.models.Organisation;
import org.dev_alex.mojo_qa.mojo.services.RequestService;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import okhttp3.Response;
import okio.BufferedSink;
import okio.Okio;

public class NotificationsFragment extends Fragment implements NotificationAdapter.NotificationClickListener {
    private static final String ARG_NOTIFICATION_ID = "ARG_NOTIF";
    private RelativeLayoutWithPopUp rootView;
    private ProgressDialog loopDialog;
    private RecyclerView recyclerView;
    private RelativeLayout sortTypePopupWindow;
    private static boolean needUpdate = true;
    private ArrayList<Notification> notifications;
    Context context;
    private String searchText = null;
    private TextWatcher searchListener;
    private Integer pendingNotificationId = null;
    public MultiSpinner multiSpinner;
    public ArrayList<String> tags = new ArrayList<>();
    ArrayList<Organisation> organisations;
    private Date fromFilter;
    private Date toFilter;
    private String readTypeFilter;
    private String orderTypeFilter;
    public Dialog filterDialog;
    public int countItem;


    public static NotificationsFragment newInstance(Integer notificationId) {
        Bundle args = new Bundle();
        if (notificationId != null) {
            args.putInt(ARG_NOTIFICATION_ID, notificationId);
        }

        NotificationsFragment fragment = new NotificationsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        multiSpinner = (MultiSpinner) getActivity().findViewById(R.id.spin);
        filterDialog = new Dialog(getContext());
        filterDialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        filterDialog.setContentView(LayoutInflater.from(getContext()).inflate(R.layout.dialog_notification_filter, null, false));
        filterDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        filterDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        filterDialog.setCanceledOnTouchOutside(false);
        filterDialog.create();
        if (rootView == null) {
            if (getArguments() != null && getArguments().containsKey(ARG_NOTIFICATION_ID)) {
                pendingNotificationId = getArguments().getInt(ARG_NOTIFICATION_ID);
            }
            rootView = (RelativeLayoutWithPopUp) inflater.inflate(R.layout.fragment_notifications, container, false);
            recyclerView = rootView.findViewById(R.id.recycler_view);
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            sortTypePopupWindow = (RelativeLayout) rootView.findViewById(R.id.notif_sort_popup_layout);
            rootView.addPopUpWindow(sortTypePopupWindow);
            sortTypePopupWindow.setVisibility(View.GONE);
            initDialog();
            setListeners();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    updateNotifications();
                }
            }, 500);
        }
        return rootView;
    }
    public void onItemsSelected(boolean[] selected) {
        tags.clear();
        for(int i = 0; i < selected.length; i++){
            if(selected[i]){
                tags.add(organisations.get(i).getId());
            }
        }
        if(tags.size() == 0){
            Toast.makeText(getContext(), "Вы ничего не выбрали!", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        setupHeader();
        if (recyclerView != null && recyclerView.getAdapter() != null)
            recyclerView.getAdapter().notifyDataSetChanged();
    }
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }
    @Override
    public void onDetach() {
        super.onDetach();
        context = null;
    }

    private void setupHeader() {
        ((TextView) getActivity().findViewById(R.id.title)).setText(getString(R.string.notifications));
        getActivity().findViewById(R.id.grid_btn).setVisibility(View.GONE);
        getActivity().findViewById(R.id.back_btn).setVisibility(View.GONE);
        getActivity().findViewById(R.id.qr_btn).setVisibility(View.GONE);
        getActivity().findViewById(R.id.spin).setVisibility(View.GONE);



        getActivity().findViewById(R.id.group_by_btn).setVisibility(View.VISIBLE);
        getActivity().findViewById(R.id.sandwich_btn).setVisibility(View.VISIBLE);
        getActivity().findViewById(R.id.search_btn).setVisibility(View.VISIBLE);
        getActivity().findViewById(R.id.notification_btn).setVisibility(View.GONE);

        getActivity().findViewById(R.id.group_by_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                 ArrayList<String> organisationNames = new ArrayList<>();
                    for (int i = 0; i < organisations.size(); i++){
                        organisationNames.add(organisations.get(i).getName());
                    }
                    multiSpinner.setItems(getActivity(), context, organisations, "", NotificationsFragment.this::onItemsSelected);


                TextView calendarText = filterDialog.findViewById(R.id.calendarText);
                LinearLayout calendarLL = filterDialog.findViewById(R.id.calendarLL);
                RadioGroup readGroup = filterDialog.findViewById(R.id.read_group);
                RadioGroup orderGroup = filterDialog.findViewById(R.id.orderGroup);
                MaterialButton okeyBTN = filterDialog.findViewById(R.id.okeyBTN);
                ImageView kresticBTN = filterDialog.findViewById(R.id.krestik);
                kresticBTN.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        filterDialog.cancel();
                    }
                });
                okeyBTN.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        needUpdate = true;
                        updateNotifications();
                        filterDialog.cancel();
                    }
                });
                readGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(RadioGroup group, int checkedId) {
                        switch (checkedId) {
                            case R.id.radio_not_read:
                                readTypeFilter = "unreaded";
                                break;
                            case R.id.radio_read:
                                readTypeFilter = "readed";
                                break;
                            case R.id.radio_all_read:
                                readTypeFilter = "all";
                                break;
                            default:
                                break;
                        }
                    }
                });
                 orderGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(RadioGroup group, int checkedId) {
                        switch (checkedId) {
                            case R.id.radio_new:
                                orderTypeFilter = "date_desc";
                                break;
                            case R.id.radio_old:
                                orderTypeFilter = "date";
                                break;
                            default:
                                break;
                        }
                    }
                });
                calendarLL.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final Dialog calendarDialog = new Dialog(getContext());
                        View calendarDialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_calendar, null, false);
                        calendarDialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
                        calendarDialog.setContentView(calendarDialogView);
                        calendarDialog.getWindow().setLayout(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
                        calendarDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                        calendarDialog.show();
                        MaterialCalendarView calendarView = (MaterialCalendarView) calendarDialog.findViewById(R.id.calendarView);
                        calendarView.setTopbarVisible(false);
                        calendarView.setCurrentDate(CalendarDay.from(Calendar.getInstance()), true);
                        calendarView.setSelectionMode(MaterialCalendarView.SELECTION_MODE_RANGE);
                        TextView month = calendarDialog.findViewById(R.id.month);
                        CalendarDay calDate = calendarView.getCurrentDate();
                        month.setText(getCalendarText(calDate));

                        calendarView.setOnMonthChangedListener(new OnMonthChangedListener() {
                            @Override
                            public void onMonthChanged(MaterialCalendarView widget, CalendarDay date) {
                                CalendarDay calDate = calendarView.getCurrentDate();
                                month.setText(getCalendarText(calDate));
                            }
                        });
                        calendarView.addDecorator(new DayViewDecorator() {
                            @Override
                            public boolean shouldDecorate(CalendarDay day) {
                                Calendar currentCalendar = Calendar.getInstance();
                                currentCalendar.setTime(new Date());
                                return day.getCalendar().get(Calendar.DAY_OF_YEAR) == currentCalendar.get(Calendar.DAY_OF_YEAR);
                            }

                            @Override
                            public void decorate(DayViewFacade view) {
                                view.setBackgroundDrawable(ContextCompat.getDrawable(getContext(), R.drawable.image_ring));
                            }
                        });
                        MaterialButton setCalendarBTN = calendarDialog.findViewById(R.id.save);
                        setCalendarBTN.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                List<CalendarDay> days =  calendarView.getSelectedDates();
                               if (days.size() == 0){Toast.makeText(getContext(), "вы не выбрали период", Toast.LENGTH_SHORT).show();}
                               else {
                                   if (days.size() == 1){
                                       toFilter = days.get(0).getDate();
                                       fromFilter = days.get(0).getDate();
                                   }
                                   else {
                                   if (days.get(0).getDate().getTime() < days.get(1).getDate().getTime()){
                                   fromFilter = days.get(0).getDate();
                                   toFilter = days.get(1).getDate();}
                                   else {
                                       toFilter = days.get(0).getDate();
                                       fromFilter = days.get(1).getDate();}}
                                   SimpleDateFormat xDateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
                                   calendarText.setText(getString(R.string.from) + " "+ xDateFormat.format(fromFilter) + " " + getString(R.string.to)+ " " + xDateFormat.format(toFilter));
                                   calendarDialog.cancel();

                               }

                            }
                        });
                    }
                });

                filterDialog.show();
            }
        });
    }

    private String getCalendarText(CalendarDay calendarDay) {
        String monthName;
        if (Locale.getDefault().getISO3Language().equals("rus")) {
            String monthList[] = {"Январь", "Февраль", "Март", "Апрель", "Май", "Июнь", "Июль", "Август", "Сентябрь", "Октябрь", "Ноябрь", "Декабрь"};
            monthName = monthList[calendarDay.getMonth()];
        } else
            monthName = new DateFormatSymbols(Locale.getDefault()).getMonths()[calendarDay.getMonth()];
        return String.format("%s %s", monthName, calendarDay.getYear());
    }

    private void stopSearch() {
        searchText = null;
        getActivity().findViewById(R.id.main_menu_buttons_block).setVisibility(View.VISIBLE);
        getActivity().findViewById(R.id.main_menu_search_block).setVisibility(View.GONE);

        if (searchListener != null)
            ((EditText) getActivity().findViewById(R.id.search_text)).removeTextChangedListener(searchListener);

        resetSearch();
    }

    private void startSearch() {
        searchText = "";
        getActivity().findViewById(R.id.main_menu_buttons_block).setVisibility(View.GONE);
        getActivity().findViewById(R.id.main_menu_search_block).setVisibility(View.VISIBLE);

        getActivity().findViewById(R.id.search_back_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopSearch();
            }
        });

        getActivity().findViewById(R.id.search_reset).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((EditText) getActivity().findViewById(R.id.search_text)).setText("");
            }
        });

        searchListener = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                searchText = editable.toString();
                applySearch();
            }
        };
        ((EditText) getActivity().findViewById(R.id.search_text)).addTextChangedListener(searchListener);
        getActivity().findViewById(R.id.search_text).requestFocus();
        getActivity().findViewById(R.id.search_text).requestFocusFromTouch();
        InputMethodManager inputMethodManager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.showSoftInput(getActivity().findViewById(R.id.search_text), InputMethodManager.SHOW_IMPLICIT);
    }

    private void applySearch() {
        ArrayList<Notification> currentNotificationList = notifications;

        ArrayList<Notification> searchResult = new ArrayList<>();
        for (Notification notification : currentNotificationList) {
            if (notification.task.ref.name.toLowerCase().contains(searchText.toLowerCase()))
                searchResult.add(notification);
        }
        recyclerView.setAdapter(new NotificationAdapter(searchResult, this));
    }

    private void resetSearch() {
        recyclerView.setAdapter(new NotificationAdapter(notifications, this));
    }

    private void updateAdapter(NotificationAdapter adapter) {
        int pendingPos = -1;

        Log.d("lambada", "pending id = " + pendingNotificationId);
        if (pendingNotificationId != null) {
            for (int i = 0; i < notifications.size(); i++) {
                Notification notification = notifications.get(i);
                if (notification.id == pendingNotificationId) {
                    notification.needExpand = true;
                    pendingPos = i;
                    break;
                }
            }
        }

        recyclerView.setAdapter(adapter);

        Log.d("lambada", "find pos = " + pendingPos);
        if (pendingPos >= 0) {
            pendingNotificationId = null;
            recyclerView.scrollToPosition(pendingPos);
        }

        if (searchText == null || searchText.isEmpty())
            resetSearch();
        if (searchText != null && !searchText.isEmpty())
            applySearch();
    }
    public int findLastVisibleItemPosition() {
        final View child = findOneVisibleChild(recyclerView.getLayoutManager().getChildCount() - 1, -1, false, true);
        return child == null ? RecyclerView.NO_POSITION : recyclerView.getChildAdapterPosition(child);
    }
    View findOneVisibleChild(int fromIndex, int toIndex, boolean completelyVisible,
                             boolean acceptPartiallyVisible) {
        OrientationHelper helper;
        if (recyclerView.getLayoutManager().canScrollVertically()) {
            helper = OrientationHelper.createVerticalHelper(recyclerView.getLayoutManager());
        } else {
            helper = OrientationHelper.createHorizontalHelper(recyclerView.getLayoutManager());
        }

        final int start = helper.getStartAfterPadding();
        final int end = helper.getEndAfterPadding();
        final int next = toIndex > fromIndex ? 1 : -1;
        View partiallyVisible = null;
        for (int i = fromIndex; i != toIndex; i += next) {
            final View child = recyclerView.getLayoutManager().getChildAt(i);
            final int childStart = helper.getDecoratedStart(child);
            final int childEnd = helper.getDecoratedEnd(child);
            if (childStart < end && childEnd > start) {
                if (completelyVisible) {
                    if (childStart >= start && childEnd <= end) {
                        return child;
                    } else if (acceptPartiallyVisible && partiallyVisible == null) {
                        partiallyVisible = child;
                    }
                } else {
                    return child;
                }
            }
        }
        return partiallyVisible;
    }
    private void setListeners() {
        ((SwipeRefreshLayout) rootView.findViewById(R.id.swipe_refresh)).setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                ((SwipeRefreshLayout) rootView.findViewById(R.id.swipe_refresh)).setRefreshing(false);
                updateNotifications();
            }
        });
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                int lastvisibleitemposition = findLastVisibleItemPosition();

                if (lastvisibleitemposition == recyclerView.getAdapter().getItemCount() - 1) {
                    countItem = lastvisibleitemposition + 1;
                    if (countItem % 100 == 0){
                        new UpdateNotifications().execute();

                    }

                }
            }
        });


        getActivity().findViewById(R.id.search_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startSearch();
            }
        });

        sortTypePopupWindow.findViewById(R.id.sort_by_name).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sortTypePopupWindow.findViewById(R.id.sort_by_create_time_tick).setVisibility(View.GONE);
                sortTypePopupWindow.findViewById(R.id.sort_by_create_time_desc_tick).setVisibility(View.GONE);
                sortTypePopupWindow.findViewById(R.id.sort_by_name_desc_tick).setVisibility(View.GONE);

                sortTypePopupWindow.findViewById(R.id.sort_by_name_tick).setVisibility(View.VISIBLE);
                sortTypePopupWindow.setVisibility(View.GONE);
                needUpdate = true;
                updateNotifications();
            }
        });
        sortTypePopupWindow.findViewById(R.id.sort_by_name_desc).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sortTypePopupWindow.findViewById(R.id.sort_by_create_time_tick).setVisibility(View.GONE);
                sortTypePopupWindow.findViewById(R.id.sort_by_create_time_desc_tick).setVisibility(View.GONE);
                sortTypePopupWindow.findViewById(R.id.sort_by_name_tick).setVisibility(View.GONE);

                sortTypePopupWindow.findViewById(R.id.sort_by_name_desc_tick).setVisibility(View.VISIBLE);

                sortTypePopupWindow.setVisibility(View.GONE);
                needUpdate = true;

                updateNotifications();
            }
        });

        sortTypePopupWindow.findViewById(R.id.sort_by_create_time).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sortTypePopupWindow.findViewById(R.id.sort_by_name_desc_tick).setVisibility(View.GONE);
                sortTypePopupWindow.findViewById(R.id.sort_by_create_time_desc_tick).setVisibility(View.GONE);
                sortTypePopupWindow.findViewById(R.id.sort_by_name_tick).setVisibility(View.GONE);

                sortTypePopupWindow.findViewById(R.id.sort_by_create_time_tick).setVisibility(View.VISIBLE);

                sortTypePopupWindow.setVisibility(View.GONE);
                needUpdate = true;
                updateNotifications();
            }
        });

        sortTypePopupWindow.findViewById(R.id.sort_by_create_time_desc).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sortTypePopupWindow.findViewById(R.id.sort_by_name_desc_tick).setVisibility(View.GONE);
                sortTypePopupWindow.findViewById(R.id.sort_by_name_tick).setVisibility(View.GONE);
                sortTypePopupWindow.findViewById(R.id.sort_by_create_time_tick).setVisibility(View.GONE);

                sortTypePopupWindow.findViewById(R.id.sort_by_create_time_desc_tick).setVisibility(View.VISIBLE);

                sortTypePopupWindow.setVisibility(View.GONE);
                needUpdate = true;

                updateNotifications();
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


    private void updateNotifications() {
        if(needUpdate){
        new GetNotificationsTask().execute();}
    }

    private void styleNotificationBadge() {
        boolean needToShow = false;

        for (Notification notification : notifications) {
            if (!notification.is_readed) {
                needToShow = true;
                break;
            }
        }

        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).setNotificationBadgeVisible(needToShow);
        }
    }

    @Override
    public void onNotificationRead(Notification notification) {
        new ReadNotificationsTask(notification.id).execute();
    }

    @Override
    public void onDownloadPdfClick(Notification notification) {
        if (checkExternalPermissions()) {
            new DownloadPdfTask(notification.id, UUID.randomUUID().toString()).execute();
        } else {
            requestExternalPermissions();
        }
    }

    @Override
    public void onDownloadDocClick(Notification notification) {
        if (checkExternalPermissions()) {
            new DownloadDocTask(notification.id, UUID.randomUUID().toString()).execute();
        } else {
            requestExternalPermissions();
        }
    }


    private class UpdateNotifications extends AsyncTask<Void, Void, Integer> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            loopDialog.show();
        }

        @Override
        protected Integer doInBackground(Void... params) {
            try {
                Response response;
                String url;
                StringBuilder filter = new StringBuilder();
                filter.append("?orderBy=");
                if(orderTypeFilter != null){
                    filter.append(orderTypeFilter);
                }
                else filter.append("date_desc");

                filter.append("&type=");
                if(readTypeFilter != null){
                    filter.append(readTypeFilter);
                }
                else filter.append("all");
                if(fromFilter != null && toFilter != null){
                    filter.append("&from=" + (fromFilter.getTime() / (long)1000) + "&to=" + (toFilter.getTime() / (long)1000 + (long)86400));
                }

                if (tags.size() != 0){
                    filter.append("&");
                    for (int i = 0; i < tags.size(); i++){
                        filter.append("orgID=").append(tags.get(i));
                        if(i != (tags.size() - 1)){
                            filter.append("&");
                        }
                    }
                }

                url = "/api/notifications" + filter + "&size=100" + "&offset=" + countItem;

                response = RequestService.createGetRequest(url);
                Log.d("mojo-response", "url = " + url + "&size=100");

                if (response.code() == 200) {
                    JSONObject responseJson = new JSONObject(response.body().string());
                    JSONArray notificationsJson = responseJson.getJSONArray("list");
                    Log.d("mojo-response", "notifications = " + notificationsJson);

                    JSONArray availableJson = responseJson.getJSONObject("available").getJSONArray("organisation");

                    organisations = new ObjectMapper().readValue(availableJson.toString(), new TypeReference<ArrayList<Organisation>>() {});;
                    ArrayList<Notification> newNotification = new ObjectMapper().readValue(notificationsJson.toString(), new TypeReference<ArrayList<Notification>>() {});
                    for (Notification notification : newNotification)
                        notification.fixTime();
                    notifications.addAll(newNotification);

                    //notifications = new ArrayList(SortUtil.INSTANCE.sortNotifications(notifications));


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
                else if (responseCode == 401) {
                    startActivity(new Intent(getContext(), AuthActivity.class));
                    getActivity().finish();
                } else if (responseCode == 200) {
                    recyclerView.getAdapter().notifyDataSetChanged();
                } else
                    Toast.makeText(getContext(), R.string.unknown_error, Toast.LENGTH_LONG).show();
            } catch (Exception exc) {
                exc.printStackTrace();
            }

            styleNotificationBadge();
        }
    }


    private class GetNotificationsTask extends AsyncTask<Void, Void, Integer> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            loopDialog.show();
        }

        @Override
        protected Integer doInBackground(Void... params) {
            try {
                Response response;
                notifications = new ArrayList<>();
                String url;
                StringBuilder filter = new StringBuilder();
                filter.append("?orderBy=");
                if(orderTypeFilter != null){
                filter.append(orderTypeFilter);
                }
                else filter.append("date_desc");

                filter.append("&type=");
                if(readTypeFilter != null){
                filter.append(readTypeFilter);
                }
                else filter.append("all");
                if(fromFilter != null && toFilter != null){
                    filter.append("&from=" + (fromFilter.getTime() / (long)1000) + "&to=" + (toFilter.getTime() / (long)1000 + (long)86400));
                }

                if (tags.size() != 0){
                    filter.append("&");
                    for (int i = 0; i < tags.size(); i++){
                        filter.append("orgID=").append(tags.get(i));
                        if(i != (tags.size() - 1)){
                            filter.append("&");
                        }
                    }
                }

                url = "/api/notifications" + filter + "&size=100";

                response = RequestService.createGetRequest(url);
                Log.d("mojo-response", "url = " + url + "&size=100");

                if (response.code() == 200) {
                    JSONObject responseJson = new JSONObject(response.body().string());
                    JSONArray notificationsJson = responseJson.getJSONArray("list");
                    Log.d("mojo-response", "notifications = " + notificationsJson);

                    JSONArray availableJson = responseJson.getJSONObject("available").getJSONArray("organisation");

                    organisations = new ObjectMapper().readValue(availableJson.toString(), new TypeReference<ArrayList<Organisation>>() {});;

                    notifications = new ObjectMapper().readValue(notificationsJson.toString(), new TypeReference<ArrayList<Notification>>() {});

                    //notifications = new ArrayList(SortUtil.INSTANCE.sortNotifications(notifications));

                    for (Notification notification : notifications)
                        notification.fixTime();
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
                else if (responseCode == 401) {
                    startActivity(new Intent(getContext(), AuthActivity.class));
                    getActivity().finish();
                } else if (responseCode == 200) {
                    updateAdapter(new NotificationAdapter(notifications, NotificationsFragment.this));
                } else
                    Toast.makeText(getContext(), R.string.unknown_error, Toast.LENGTH_LONG).show();
            } catch (Exception exc) {
                exc.printStackTrace();
            }

            styleNotificationBadge();
        }
    }

    private class ReadNotificationsTask extends AsyncTask<Void, Void, Integer> {
        private long notificationId;

        public ReadNotificationsTask(long notificationId) {
            this.notificationId = notificationId;
        }

        @Override
        protected Integer doInBackground(Void... params) {
            try {
                Response response;

                String url = "/api/notifications/" + notificationId + "/readed";

                response = RequestService.createPostRequest(url, "{}");

                if (response.code() == 200) {

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
                if (responseCode == null)
                    Toast.makeText(getContext(), R.string.network_error, Toast.LENGTH_LONG).show();
                else if (responseCode == 401) {
                    startActivity(new Intent(getContext(), AuthActivity.class));
                    getActivity().finish();
                } else if (responseCode == 200) {

                } else
                    Toast.makeText(getContext(), R.string.unknown_error, Toast.LENGTH_LONG).show();
            } catch (Exception exc) {
                exc.printStackTrace();
            }

            styleNotificationBadge();
        }
    }

    private class DownloadPdfTask extends AsyncTask<Void, Void, Integer> {
        private java.io.File resultFile;
        private long id;
        private String name;

        DownloadPdfTask(long id, String name) {
            this.id = id;
            this.name = name;
            needUpdate = false;       }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            resultFile = new File(downloadsDir, name + ".pdf");
            loopDialog.show();
        }

        @Override
        protected Integer doInBackground(Void... params) {
            try {
                if (resultFile.exists())
                    return 200;

                String url = "/api/notifications/" + id + "/pdf";
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
                else if (responseCode == 200) {
                    try {
                        Intent viewIntent = new Intent(Intent.ACTION_VIEW);
                        Uri fileUri = FileProvider.getUriForFile(getContext(), getContext().getApplicationContext().getPackageName() + ".provider", resultFile);
                        viewIntent.setDataAndType(fileUri, "application/pdf");
                        viewIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        startActivity(viewIntent);
                    } catch (Exception exc) {
                        exc.printStackTrace();
                        Toast.makeText(getContext(), "Нет приложения, которое может открыть этот файл", Toast.LENGTH_LONG).show();
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

    private class DownloadDocTask extends AsyncTask<Void, Void, Integer> {
        private java.io.File resultFile;
        private long id;
        private String name;

        DownloadDocTask(long id, String name) {
            this.id = id;
            this.name = name;
            needUpdate = false;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            resultFile = new File(downloadsDir, name + ".docx");
            loopDialog.show();
        }

        @Override
        protected Integer doInBackground(Void... params) {
            try {
                if (resultFile.exists())
                    return 200;

                String url = "/api/notifications/" + id + "/docx";
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
            if (loopDialog != null && loopDialog.isShowing())
                loopDialog.dismiss();

            try {
                if (responseCode == null)
                    Toast.makeText(getContext(), R.string.network_error, Toast.LENGTH_LONG).show();
                else if (responseCode == 200) {
                    try {
                        Intent viewIntent = new Intent(Intent.ACTION_VIEW);
                        Uri fileUri = FileProvider.getUriForFile(getContext(), getContext().getApplicationContext().getPackageName() + ".provider", resultFile);
                        viewIntent.setDataAndType(fileUri, "application/msword");
                        viewIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        startActivity(viewIntent);
                    } catch (Exception exc) {
                        exc.printStackTrace();
                        Toast.makeText(getContext(), "Нет приложения, которое может открыть этот файл", Toast.LENGTH_LONG).show();
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


    private boolean checkExternalPermissions() {
        int permissionCheckWrite = ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int permissionCheckRead = ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE);
        return (permissionCheckRead == PackageManager.PERMISSION_GRANTED && permissionCheckWrite == PackageManager.PERMISSION_GRANTED);
    }

    private void requestExternalPermissions() {
        ActivityCompat.requestPermissions(getActivity(),
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
    }
}