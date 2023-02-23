package org.dev_alex.mojo_qa.mojo.fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.journeyapps.barcodescanner.CaptureActivity;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;
import com.prolificinteractive.materialcalendarview.OnMonthChangedListener;
import com.prolificinteractive.materialcalendarview.OnRangeSelectedListener;

import net.cachapa.expandablelayout.ExpandableLayout;

import org.dev_alex.mojo_qa.mojo.App;
import org.dev_alex.mojo_qa.mojo.EventDecorator;
import org.dev_alex.mojo_qa.mojo.R;
import org.dev_alex.mojo_qa.mojo.activities.AuthActivity;
import org.dev_alex.mojo_qa.mojo.adapters.TaskAdapter;
import org.dev_alex.mojo_qa.mojo.custom_views.RelativeLayoutWithPopUp;
import org.dev_alex.mojo_qa.mojo.models.Task;
import org.dev_alex.mojo_qa.mojo.services.LoginHistoryService;
import org.dev_alex.mojo_qa.mojo.services.RequestService;
import org.json.JSONArray;
import org.json.JSONObject;

import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import okhttp3.Response;

public class TasksFragment extends Fragment {

    private enum CurrentAdapterType {FINISHED, BUSY, PERMANENT}

    private RelativeLayoutWithPopUp rootView;
    private RelativeLayout taskPopupWindow;

    private ProgressDialog loopDialog;
    private RecyclerView recyclerView;

    private ArrayList<Task> finishedTasks;
    private ArrayList<Task> busyTasks;
    private ArrayList<Task> permanentTasks;
    private Calendar currentDate;
    private CalendarDay selectedDate;
    private CalendarDay selectedDateStart;
    private CalendarDay selectedDateEnd;
    private boolean withDay = false;
    private boolean withRange = false;

    private ArrayList<CalendarDay> daysWithOverdueTasks = new ArrayList<>();
    private ArrayList<CalendarDay> daysWithActualTasks = new ArrayList<>();

    private String searchText = null;
    private TextWatcher searchListener;
    private CurrentAdapterType currentAdapterType = null;

    public boolean needUpdate = false;
    private final int SCAN_CODE_REQUEST_CODE = 0x0000c0de;


    public static TasksFragment newInstance() {
        Bundle args = new Bundle();
        TasksFragment fragment = new TasksFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        currentDate = Calendar.getInstance();
        currentDate.setTime(new Date());

        setRetainInstance(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (rootView == null) {
            rootView = (RelativeLayoutWithPopUp) inflater.inflate(R.layout.fragment_tasks, container, false);
            taskPopupWindow = rootView.findViewById(R.id.task_popup_layout);
            taskPopupWindow.setVisibility(View.GONE);
            rootView.addPopUpWindow(taskPopupWindow);


            recyclerView = rootView.findViewById(R.id.recycler_view);
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            ((RadioButton) rootView.findViewById(R.id.busy)).setChecked(true);

            initDialog();
            setListeners();

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    updateDate(true);
                }
            }, 500);
        }

        if (needUpdate) {
            ((MaterialCalendarView) rootView.findViewById(R.id.calendarView)).removeDecorators();
            ((MaterialCalendarView) rootView.findViewById(R.id.calendarView)).invalidateDecorators();
            rootView.findViewById(R.id.calendar_reset_btn).callOnClick();
        }

        return rootView;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            IntentResult intentResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
            if (intentResult != null) {
                String contents = intentResult.getContents();
                String UUID = contents.substring(contents.lastIndexOf("/")).replace("/", "");
                boolean isReport = false;
                if (contents.contains("reports")) {
                    isReport = true;
                }
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container, TemplateFragment.newInstance(UUID, isReport)).addToBackStack(null).commit();
            } else {
                super.onActivityResult(requestCode, resultCode, data);
                super.onActivityResult(requestCode, resultCode, data);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    @Override
    public void onResume() {
        super.onResume();
        setupHeader();
        if (recyclerView != null && recyclerView.getAdapter() != null)
            recyclerView.getAdapter().notifyDataSetChanged();
    }

    private void setupHeader() {
        ((TextView) getActivity().findViewById(R.id.title)).setText(getString(R.string.tasks));
        getActivity().findViewById(R.id.grid_btn).setVisibility(View.GONE);
        getActivity().findViewById(R.id.back_btn).setVisibility(View.GONE);
        getActivity().findViewById(R.id.group_by_btn).setVisibility(View.GONE);
        getActivity().findViewById(R.id.notification_btn).setVisibility(View.GONE);
        getActivity().findViewById(R.id.qr_btn).setVisibility(View.VISIBLE);

        getActivity().findViewById(R.id.sandwich_btn).setVisibility(View.VISIBLE);
        getActivity().findViewById(R.id.search_btn).setVisibility(View.VISIBLE);

        getActivity().findViewById(R.id.qr_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scanСode(v);
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
        ArrayList<Task> currentTaskList;
        switch (currentAdapterType) {
            case BUSY:
                currentTaskList = busyTasks;
                break;

            case FINISHED:
                currentTaskList = finishedTasks;
                break;

            case PERMANENT:
                currentTaskList = permanentTasks;
                break;

            default:
                currentTaskList = busyTasks;
                break;
        }

        ArrayList<Task> searchResult = new ArrayList<>();
        for (Task task : currentTaskList) {
            if (task.ref.name.toLowerCase().contains(searchText.toLowerCase()))
                searchResult.add(task);
        }
        recyclerView.setAdapter(new TaskAdapter(this, searchResult));
    }

    private void resetSearch() {
        ArrayList<Task> currentTaskList;
        switch (currentAdapterType) {
            case BUSY:
                currentTaskList = busyTasks;
                break;

            case FINISHED:
                currentTaskList = finishedTasks;
                break;

            case PERMANENT:
                currentTaskList = permanentTasks;
                break;

            default:
                currentTaskList = busyTasks;
                break;
        }
        //recyclerView.setAdapter(new TaskAdapter(this, currentTaskList));
    }

    private void updateTaskAdapter(TaskAdapter taskAdapter, CurrentAdapterType type) {
        currentAdapterType = type;
        recyclerView.setAdapter(taskAdapter);

        if (searchText == null || searchText.isEmpty())
            resetSearch();
        if (searchText != null && !searchText.isEmpty())
            applySearch();
    }

    private void setListeners() {
        ((RadioGroup) rootView.findViewById(R.id.task_toggle)).setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
                switch (checkedId) {
                    case R.id.ended:
                        showCalendarView();
                        updateTaskAdapter(new TaskAdapter(TasksFragment.this, finishedTasks, true), CurrentAdapterType.FINISHED);
                        break;

                    case R.id.busy:
                        hideCalendarView();
                        updateTaskAdapter(new TaskAdapter(TasksFragment.this, busyTasks, TaskAdapter.TaskType.BUSY), CurrentAdapterType.BUSY);
                        updateDate(true);

                        break;

                    case R.id.permanent:
                        updateTaskAdapter(new TaskAdapter(TasksFragment.this, permanentTasks, TaskAdapter.TaskType.PERMANENT), CurrentAdapterType.PERMANENT);
                        hideCalendarView();
                        updateDate(true);
                        break;
                }
            }
        });

        final ExpandableLayout expandableLayout = ((ExpandableLayout) rootView.findViewById(R.id.expandable_calendar_layout));

        final MaterialCalendarView calendarView = (MaterialCalendarView) rootView.findViewById(R.id.calendarView);
        calendarView.setTopbarVisible(false);
        calendarView.setCurrentDate(CalendarDay.from(currentDate), true);

        calendarView.setSelectionMode(MaterialCalendarView.SELECTION_MODE_RANGE);

        calendarView.setOnDateChangedListener(new OnDateSelectedListener() {
            @Override
            public void onDateSelected(@NonNull MaterialCalendarView widget, @NonNull CalendarDay date, boolean selected) {
                if (date == selectedDate) {
                    selectedDate = null;
                    widget.setDateSelected(date, true);
                    expandableLayout.collapse();
                    currentDate.setTime(date.getDate());
                    withRange = false;
                    withDay = true;
                    new Handler().postDelayed(() -> updateDate(true), 500);
                }
                if (selected) selectedDate = date;
            }
        });

        calendarView.setOnRangeSelectedListener(new OnRangeSelectedListener() {
            @Override
            public void onRangeSelected(@NonNull MaterialCalendarView widget, @NonNull List<CalendarDay> dates) {
                expandableLayout.collapse();
                selectedDateStart = dates.get(0);
                selectedDateEnd = dates.get(dates.size() - 1);
                currentDate.setTime(selectedDateStart.getDate());
                withDay = false;
                withRange = true;
                new Handler().postDelayed(() -> updateDate(true), 500);
            }
        });

        calendarView.setOnMonthChangedListener(new OnMonthChangedListener() {
            @Override
            public void onMonthChanged(MaterialCalendarView widget, CalendarDay date) {
                Log.d("mojo-log", String.valueOf(date.getMonth()));
                if (CalendarDay.from(currentDate).getMonth() != date.getMonth()) {
                    withDay = false;
                    withRange = false;
                    currentDate.setTime(date.getDate());
                    updateDate(true);
                }
            }
        });

        rootView.findViewById(R.id.calendar_reset_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                withDay = false;
                calendarView.clearSelection();
                if (CalendarDay.from(new Date()).getMonth() == CalendarDay.from(currentDate).getMonth())
                    updateDate(true);
                else
                    calendarView.setCurrentDate(CalendarDay.from(new Date()), true);
            }
        });


        rootView.findViewById(R.id.calendar_arrow_left).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                withDay = false;
                calendarView.goToPrevious();
            }
        });

        rootView.findViewById(R.id.calendar_arrow_right).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                withDay = false;
                calendarView.goToNext();
            }
        });


        rootView.findViewById(R.id.calendar_control_panel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                expandableLayout.toggle();
            }
        });


        ((SwipeRefreshLayout) rootView.findViewById(R.id.swipe_refresh)).setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                ((SwipeRefreshLayout) rootView.findViewById(R.id.swipe_refresh)).setRefreshing(false);
                updateDate(true);
            }
        });


        getActivity().findViewById(R.id.search_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startSearch();
            }
        });
    }

    private void hideCalendarView() {
        View calendarView = rootView.findViewById(R.id.calendar_control_panel);
        if (calendarView != null) calendarView.setVisibility(View.GONE);
    }

    private void showCalendarView() {
        View calendarView = rootView.findViewById(R.id.calendar_control_panel);
        if (calendarView != null) calendarView.setVisibility(View.VISIBLE);
    }

    private void updateDate(boolean needUpdate) {
        String date;
        currentDate.set(Calendar.HOUR_OF_DAY, 0);
        currentDate.set(Calendar.MINUTE, 0);
        currentDate.set(Calendar.SECOND, 0);
        currentDate.set(Calendar.MILLISECOND, 0);

        if (withDay) {
            date = new SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).format(currentDate.getTime());
        } else if (withRange) {
            String monthNameStart;
            String monthNameEnd;
            if (Locale.getDefault().getISO3Language().equals("rus")) {
                String monthList[] = {"Январь", "Февраль", "Март", "Апрель", "Май", "Июнь", "Июль", "Август", "Сентябрь", "Октябрь", "Ноябрь", "Декабрь"};
                monthNameStart = monthList[selectedDateStart.getMonth()];
                monthNameEnd = monthList[selectedDateEnd.getMonth()];
            } else {
                monthNameStart = new DateFormatSymbols(Locale.getDefault()).getMonths()[selectedDateStart.getMonth()];
                monthNameEnd = new DateFormatSymbols(Locale.getDefault()).getMonths()[selectedDateEnd.getMonth()];
            }
            date = String.format("%s %s - %s %s %s", selectedDateStart.getDay(), monthNameStart, selectedDateEnd.getDay(), monthNameEnd, selectedDateEnd.getYear());
        } else {
            ((MaterialCalendarView) rootView.findViewById(R.id.calendarView)).clearSelection();
            currentDate.set(Calendar.DAY_OF_MONTH, 1);
            String monthName;

            if (Locale.getDefault().getISO3Language().equals("rus")) {
                String monthList[] = {"Январь", "Февраль", "Март", "Апрель", "Май", "Июнь", "Июль", "Август", "Сентябрь", "Октябрь", "Ноябрь", "Декабрь"};
                monthName = monthList[currentDate.get(Calendar.MONTH)];
            } else
                monthName = new DateFormatSymbols(Locale.getDefault()).getMonths()[currentDate.get(Calendar.MONTH)];
            date = String.format("%s %s", monthName, currentDate.get(Calendar.YEAR));
        }
        ((TextView) rootView.findViewById(R.id.calendar_date)).setText(date);
        if (needUpdate)
            new GetTasksTask().execute();

        rootView.findViewById(R.id.calendar_reset_btn).setVisibility(
                (withDay || currentDate.get(Calendar.MONTH) != Calendar.getInstance().get(Calendar.MONTH)) ? View.VISIBLE : View.GONE);
    }

    public void scanСode(View v) {
        IntentIntegrator intentIntegrator = new IntentIntegrator(getActivity()) {
            @Override
            protected void startActivityForResult(Intent intent, int code) {
                TasksFragment.this.startActivityForResult(intent, SCAN_CODE_REQUEST_CODE); // REQUEST_CODE override
            }
        };

        intentIntegrator.setCaptureActivity(CaptureActivity.class);
        intentIntegrator.setOrientationLocked(true);
        intentIntegrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES);
        intentIntegrator.setPrompt("Сканируем...");
        intentIntegrator.initiateScan();
    }

    private void updateDecorators(ArrayList<Task> monthTasks) {
        MaterialCalendarView calendarView = rootView.findViewById(R.id.calendarView);

        for (Task task : monthTasks) {
            if (task.expire_time == null)
                continue;

            Date dueDate = new Date(task.expire_time);

            if (dueDate.after(new Date())) {
                if (!daysWithActualTasks.contains(CalendarDay.from(dueDate)))
                    daysWithActualTasks.add(CalendarDay.from(dueDate));
            } else if (!daysWithOverdueTasks.contains(CalendarDay.from(dueDate)))
                daysWithOverdueTasks.add(CalendarDay.from(dueDate));
        }

        calendarView.removeDecorators();
        calendarView.invalidateDecorators();

        EventDecorator actualTasksDecorator = new EventDecorator(Color.parseColor("#ff26c373"), daysWithActualTasks);
        EventDecorator overdueTasksDecorator = new EventDecorator(Color.RED, daysWithOverdueTasks);
        calendarView.addDecorators(actualTasksDecorator, overdueTasksDecorator);

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
    }

    private void initDialog() {
        loopDialog = new ProgressDialog(getContext(), R.style.ProgressDialogStyle);
        loopDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        loopDialog.setMessage(getString(R.string.loading_please_wait));
        loopDialog.setIndeterminate(true);
        loopDialog.setCanceledOnTouchOutside(false);
        loopDialog.setCancelable(false);
    }

    public void showTemplateWindow(long taskId, boolean isFinished) {
        getActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, TemplateFragment.newInstance(taskId, isFinished)).addToBackStack(null).commit();
    }

    public void showPopUpWindow(final Task task) {
        taskPopupWindow.setVisibility(View.VISIBLE);

        taskPopupWindow.findViewById(R.id.delete_block).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences mSettings;
                mSettings = App.getContext().getSharedPreferences("templates", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = mSettings.edit();
                editor.putString(task.id + LoginHistoryService.getCurrentUser().username, "");
                editor.apply();

                taskPopupWindow.setVisibility(View.GONE);
                recyclerView.getAdapter().notifyDataSetChanged();
            }
        });
        taskPopupWindow.findViewById(R.id.continue_block).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                taskPopupWindow.setVisibility(View.GONE);
                showTemplateWindow(task.id, false);
            }
        });
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
                permanentTasks = new ArrayList<>();
                String url;

                String dateFilters = "";
                if (withDay) {
                    Calendar dayCalendar = Calendar.getInstance();
                    dayCalendar.setTime(currentDate.getTime());
                    dateFilters += "from=" + dayCalendar.getTime().getTime() / 1000;
                    dayCalendar.add(Calendar.DAY_OF_MONTH, 1);
                    dateFilters += "&to=" + dayCalendar.getTime().getTime() / 1000;
                } else if (withRange) {
                    Calendar rangeCalendar = Calendar.getInstance();
                    rangeCalendar.setTime(selectedDateStart.getDate());
                    dateFilters += "from=" + rangeCalendar.getTime().getTime() / 1000;
                    rangeCalendar.setTime(selectedDateEnd.getDate());
                    rangeCalendar.add(Calendar.DATE, 1);
                    dateFilters += "&to=" + rangeCalendar.getTime().getTime() / 1000;
                } else {
                    Calendar monthCalendar = Calendar.getInstance();
                    monthCalendar.setTime(currentDate.getTime());
                    monthCalendar.set(Calendar.DAY_OF_MONTH, 1);
                    dateFilters += "from=" + monthCalendar.getTime().getTime() / 1000;
                    monthCalendar.add(Calendar.MONTH, 1);
                    monthCalendar.set(Calendar.DAY_OF_MONTH, 1);
                    dateFilters += "&to=" + monthCalendar.getTime().getTime() / 1000;
                }

                for (int i = 0; i < 3; i++) {
                    if (i == 0) {
                        url = "/api/tasks/archive?" + dateFilters;
                    } else if (i == 1) {
                        url = "/api/tasks/active?order=expire&filter=oneshot,periodic";
                    } else {
                        url = "/api/tasks/active?filter=constantly";
                    }

                    response = RequestService.createGetRequest(url);
                    Response allResponse = RequestService.createGetRequest("/api/tasks/active");
                    Log.d("mojo-response", "url = " + url);

                    if (response.code() == 200) {
                        ArrayList<Task> lastTasks = new ArrayList<>();
                        JSONArray tasksJson = new JSONArray(response.body().string());
                        SharedPreferences pos = App.getContext().getSharedPreferences("templates", Context.MODE_PRIVATE);
                        Map<String, ?> keys = pos.getAll();
                        for (Map.Entry<String, ?> entry : keys.entrySet()) {
                            if (entry.getKey().contains(LoginHistoryService.getCurrentUser().username)) {
                                String str = entry.getValue().toString();
                                try {
                                    JSONObject template = new JSONObject(str);
                                    Task task = new Task();
                                    task.ref = new Task.Ref();
                                    task.suspended = true;

                                    task.taskUUID = entry.getKey().replace(LoginHistoryService.getCurrentUser().username, "");
                                    try {
                                        task.ref.id = template.getLong("longId");
                                        task.ref.type = template.getString("typeTask");
                                        task.ref.name = template.getString("nameTask");
                                        try {
                                            task.complete_time = template.getLong("CompleteTime");
                                        } catch (Exception ignored) {
                                        }
                                    } catch (Exception ignored) {
                                        task.id = 0;
                                        task.ref.name = template.getString("name");
                                    }
                                    lastTasks.add(task);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                // Log.e("map values",entry.getKey() + ": " + entry.getValue().toString());
                                //Log.e("map values",entry.getKey() + ": " + entry.getValue().toString());
                            }
                        }
                        Log.d("mojo-response", "tasks size = " + tasksJson.length());
                        Log.d("mojo-response", "tasks = " + tasksJson);

                        if (i == 0) {
                            finishedTasks = new ObjectMapper().readValue(tasksJson.toString(), new TypeReference<ArrayList<Task>>() {
                            });
                            for (Task task : finishedTasks)
                                task.fixTime();
                            Log.d("mojo-response", "finished tasks size = " + finishedTasks.size());
                        } else {
                            if (i == 1) {
                                busyTasks = new ObjectMapper().readValue(tasksJson.toString(), new TypeReference<ArrayList<Task>>() {
                                });
                                for (Task busyTask : busyTasks) {
                                    lastTasks.removeIf(permanentT -> permanentT.ref.id == busyTask.id);
                                    lastTasks.removeIf(permanentT -> permanentT.ref.id == busyTask.ref.id);
                                    lastTasks.removeIf(permanentT -> permanentT.id == busyTask.ref.id);
                                }

                                busyTasks.addAll(lastTasks);

                                // String templateJson = mSettings.getString(task.id + LoginHistoryService.getCurrentUser().username, "");

                                for (Task task : busyTasks)
                                    task.fixTime();
                            } else {
                                permanentTasks = new ObjectMapper().readValue(tasksJson.toString(), new TypeReference<ArrayList<Task>>() {
                                });
                                for (Task lastT : lastTasks) {
                                    for (Task permanentT : permanentTasks) {
                                        Log.d("1", lastT.id + "");
                                        Log.d("1", lastT.ref.id + "");
                                        Log.d("1", lastT.ref.name + "");
                                        Log.d("2", permanentT.id + "");
                                        Log.d("2", permanentT.ref.id + "");
                                        Log.d("2", permanentT.ref.name + "");
                                    }
                                }

                                for (Task lastT : lastTasks) {
                                    permanentTasks.removeIf(permanentT -> lastT.ref.id == permanentT.id);
                                    permanentTasks.removeIf(permanentT -> lastT.ref.id == permanentT.ref.id);
                                }
                                for (Task task : permanentTasks)
                                    task.fixTime();
                            }
                        }
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
            try {
                if (loopDialog != null && loopDialog.isShowing())
                    loopDialog.dismiss();

                if (responseCode == null)
                    Toast.makeText(getContext(), R.string.network_error, Toast.LENGTH_LONG).show();
                else if (responseCode == 401) {
                    startActivity(new Intent(getContext(), AuthActivity.class));
                    getActivity().finish();
                } else if (responseCode == 200) {
                    if (!withDay)
                        updateDecorators(busyTasks);

                    if (currentAdapterType == null)
                        currentAdapterType = CurrentAdapterType.BUSY;
                    switch (currentAdapterType) {
                        case BUSY:
                            showCalendarView();
                            ((RadioButton) rootView.findViewById(R.id.busy)).setChecked(true);
                            updateTaskAdapter(new TaskAdapter(TasksFragment.this, busyTasks, TaskAdapter.TaskType.BUSY), CurrentAdapterType.BUSY);
                            break;
                        case FINISHED:
                            hideCalendarView();
                            ((RadioButton) rootView.findViewById(R.id.ended)).setChecked(true);
                            updateTaskAdapter(new TaskAdapter(TasksFragment.this, finishedTasks, true), CurrentAdapterType.FINISHED);

                            break;
                        case PERMANENT:
                            hideCalendarView();
                            ((RadioButton) rootView.findViewById(R.id.permanent)).setChecked(true);
                            updateTaskAdapter(new TaskAdapter(TasksFragment.this, permanentTasks, TaskAdapter.TaskType.PERMANENT), CurrentAdapterType.PERMANENT);

                            break;
                    }

                } else
                    Toast.makeText(getContext(), R.string.unknown_error, Toast.LENGTH_LONG).show();
            } catch (Exception exc) {
                exc.printStackTrace();
            }
        }
    }
}
