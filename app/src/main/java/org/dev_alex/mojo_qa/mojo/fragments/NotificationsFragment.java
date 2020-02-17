package org.dev_alex.mojo_qa.mojo.fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.dev_alex.mojo_qa.mojo.R;
import org.dev_alex.mojo_qa.mojo.activities.AuthActivity;
import org.dev_alex.mojo_qa.mojo.adapters.NotificationAdapter;
import org.dev_alex.mojo_qa.mojo.custom_views.RelativeLayoutWithPopUp;
import org.dev_alex.mojo_qa.mojo.models.Notification;
import org.dev_alex.mojo_qa.mojo.services.RequestService;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import okhttp3.Response;

public class NotificationsFragment extends Fragment implements NotificationAdapter.NotificationClickListener {
    private static final String ARG_NOTIFICATION_ID = "ARG_NOTIF";

    private final int SORT_BY_NAME = 1;
    private final int SORT_BY_NAME_DESC = 2;
    private final int SORT_BY_CREATED_AT = 3;
    private final int SORT_BY_CREATED_AT_DESC = 4;
    private int sortType = SORT_BY_NAME;

    private RelativeLayoutWithPopUp rootView;

    private ProgressDialog loopDialog;
    private RecyclerView recyclerView;
    private RelativeLayout sortTypePopupWindow;

    private ArrayList<Notification> notifications;

    private String searchText = null;
    private TextWatcher searchListener;

    private Integer pendingNotificationId = null;

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

    @Override
    public void onResume() {
        super.onResume();
        setupHeader();
        if (recyclerView != null && recyclerView.getAdapter() != null)
            recyclerView.getAdapter().notifyDataSetChanged();
    }

    private void setupHeader() {
        ((TextView) getActivity().findViewById(R.id.title)).setText(getString(R.string.notifications));
        getActivity().findViewById(R.id.grid_btn).setVisibility(View.GONE);
        getActivity().findViewById(R.id.back_btn).setVisibility(View.GONE);

        getActivity().findViewById(R.id.group_by_btn).setVisibility(View.VISIBLE);
        getActivity().findViewById(R.id.sandwich_btn).setVisibility(View.VISIBLE);
        getActivity().findViewById(R.id.search_btn).setVisibility(View.VISIBLE);
        getActivity().findViewById(R.id.notification_btn).setVisibility(View.VISIBLE);

        getActivity().findViewById(R.id.group_by_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sortTypePopupWindow.setVisibility(View.VISIBLE);
            }
        });
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

        Log.d("lambada","pending id = " + pendingNotificationId);
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

        Log.d("lambada","find pos = " + pendingPos);
        if (pendingPos >= 0) {
            pendingNotificationId = null;
            recyclerView.scrollToPosition(pendingPos);
        }

        if (searchText == null || searchText.isEmpty())
            resetSearch();
        if (searchText != null && !searchText.isEmpty())
            applySearch();
    }

    private void setListeners() {
        ((SwipeRefreshLayout) rootView.findViewById(R.id.swipe_refresh)).setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                ((SwipeRefreshLayout) rootView.findViewById(R.id.swipe_refresh)).setRefreshing(false);
                updateNotifications();
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
                sortType = SORT_BY_NAME;
                sortTypePopupWindow.setVisibility(View.GONE);
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

                sortType = SORT_BY_NAME_DESC;
                sortTypePopupWindow.setVisibility(View.GONE);
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

                sortType = SORT_BY_CREATED_AT;
                sortTypePopupWindow.setVisibility(View.GONE);
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

                sortType = SORT_BY_CREATED_AT_DESC;
                sortTypePopupWindow.setVisibility(View.GONE);
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
        new GetNotificationsTask().execute();
    }

    private void styleNotificationBadge() {
        boolean needToShow = false;

        for (Notification notification : notifications) {
            if (!notification.is_readed) {
                needToShow = true;
                break;
            }
        }

        View badgeView = getActivity().findViewById(R.id.vNotificationButtonBadge);
        if (badgeView != null) {
            badgeView.setVisibility(needToShow ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public void onNotificationRead(Notification notification) {
        new ReadNotificationsTask(notification.id).execute();
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

                String sortParameter = "?orderBy=";
                if (sortType == SORT_BY_CREATED_AT)
                    sortParameter += "date";
                else if (sortType == SORT_BY_CREATED_AT_DESC)
                    sortParameter += "date_desc";
                else if (sortType == SORT_BY_NAME)
                    sortParameter += "name";
                else if (sortType == SORT_BY_NAME_DESC)
                    sortParameter += "name_desc";

                url = "/api/notifications" + sortParameter;

                response = RequestService.createGetRequest(url);
                Log.d("mojo-response", "url = " + url + "&size=100");

                if (response.code() == 200) {
                    JSONObject responseJson = new JSONObject(response.body().string());
                    JSONArray notificationsJson = responseJson.getJSONArray("list");
                    Log.d("mojo-response", "notifications = " + notificationsJson);

                    notifications = new ObjectMapper().readValue(notificationsJson.toString(), new TypeReference<ArrayList<Notification>>() {
                    });
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
        }
    }

}
