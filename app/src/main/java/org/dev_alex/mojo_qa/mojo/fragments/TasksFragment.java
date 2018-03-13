package org.dev_alex.mojo_qa.mojo.fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
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
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;
import com.prolificinteractive.materialcalendarview.OnMonthChangedListener;

import net.cachapa.expandablelayout.ExpandableLayout;

import org.dev_alex.mojo_qa.mojo.App;
import org.dev_alex.mojo_qa.mojo.Data;
import org.dev_alex.mojo_qa.mojo.EventDecorator;
import org.dev_alex.mojo_qa.mojo.R;
import org.dev_alex.mojo_qa.mojo.adapters.TaskAdapter;
import org.dev_alex.mojo_qa.mojo.models.Task;
import org.dev_alex.mojo_qa.mojo.models.User;
import org.dev_alex.mojo_qa.mojo.services.LoginHistoryService;
import org.dev_alex.mojo_qa.mojo.services.RequestService;
import org.json.JSONArray;
import org.json.JSONObject;

import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import okhttp3.Credentials;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class TasksFragment extends Fragment {
    private enum CurrentAdapterType {FINISHED, BUSY, PERMANENT}

    private View rootView;
    private ProgressDialog loopDialog;
    private RecyclerView recyclerView;

    private ArrayList<Task> finishedTasks;
    private ArrayList<Task> busyTasks;
    private ArrayList<Task> permanentTasks;
    private Calendar currentDate;
    private boolean withDay = false;

    private ArrayList<CalendarDay> daysWithOverdueTasks = new ArrayList<>();
    private ArrayList<CalendarDay> daysWithActualTasks = new ArrayList<>();

    private String searchText = null;
    private TextWatcher searchListener;
    private CurrentAdapterType currentAdapterType = null;

    public boolean needUpdate = false;

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
            rootView = inflater.inflate(R.layout.fragment_tasks, container, false);

            recyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);
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

        getActivity().findViewById(R.id.sandwich_btn).setVisibility(View.VISIBLE);
        getActivity().findViewById(R.id.search_btn).setVisibility(View.VISIBLE);

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
        recyclerView.setAdapter(new TaskAdapter(this, currentTaskList));
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
                        updateTaskAdapter(new TaskAdapter(TasksFragment.this, finishedTasks), CurrentAdapterType.FINISHED);
                        break;

                    case R.id.busy:
                        updateTaskAdapter(new TaskAdapter(TasksFragment.this, busyTasks), CurrentAdapterType.BUSY);
                        break;

                    case R.id.permanent:
                        updateTaskAdapter(new TaskAdapter(TasksFragment.this, permanentTasks), CurrentAdapterType.PERMANENT);
                        break;
                }
            }
        });

        final ExpandableLayout expandableLayout = ((ExpandableLayout) rootView.findViewById(R.id.expandable_calendar_layout));

        final MaterialCalendarView calendarView = (MaterialCalendarView) rootView.findViewById(R.id.calendarView);
        calendarView.setTopbarVisible(false);
        calendarView.setCurrentDate(CalendarDay.from(currentDate), true);

        calendarView.setOnDateChangedListener(new OnDateSelectedListener() {
            @Override
            public void onDateSelected(@NonNull MaterialCalendarView widget, @NonNull CalendarDay date, boolean selected) {
                expandableLayout.collapse();
                currentDate.setTime(date.getDate());
                withDay = true;
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        updateDate(true);
                    }
                }, 500);
            }
        });

        calendarView.setOnMonthChangedListener(new OnMonthChangedListener() {
            @Override
            public void onMonthChanged(MaterialCalendarView widget, CalendarDay date) {
                Log.d("mojo-log", String.valueOf(date.getMonth()));
                if (CalendarDay.from(currentDate).getMonth() != date.getMonth()) {
                    withDay = false;
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
                if (!expandableLayout.isExpanded())
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            new UpdateCurrentMonthDecorators().execute();
                        }
                    }, 700);
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

    private void updateDate(boolean needUpdate) {
        String date;
        currentDate.set(Calendar.HOUR_OF_DAY, 0);
        currentDate.set(Calendar.MINUTE, 0);
        currentDate.set(Calendar.SECOND, 0);
        currentDate.set(Calendar.MILLISECOND, 0);

        if (withDay)
            date = new SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).format(currentDate.getTime());
        else {
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

                SimpleDateFormat isoDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.getDefault());
                isoDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));


                String dateParams;
                String endDateParams;
                if (withDay) {
                    Calendar dayCalendar = Calendar.getInstance();
                    dayCalendar.setTime(currentDate.getTime());
                    dateParams = "&dueAfter=" + isoDateFormat.format(dayCalendar.getTime());
                    endDateParams = "&taskCompletedAfter=" + isoDateFormat.format(dayCalendar.getTime());

                    dayCalendar.add(Calendar.DAY_OF_MONTH, 1);
                    dateParams += "&dueBefore=" + isoDateFormat.format(dayCalendar.getTime());
                    endDateParams += "&taskCompletedBefore=" + isoDateFormat.format(dayCalendar.getTime());
                } else {
                    Calendar monthCalendar = Calendar.getInstance();
                    monthCalendar.setTime(currentDate.getTime());
                    monthCalendar.set(Calendar.DAY_OF_MONTH, 1);
                    dateParams = "&dueAfter=" + isoDateFormat.format(monthCalendar.getTime());
                    endDateParams = "&taskCompletedAfter=" + isoDateFormat.format(monthCalendar.getTime());

                    monthCalendar.add(Calendar.MONTH, 1);
                    dateParams += "&dueBefore=" + isoDateFormat.format(monthCalendar.getTime());
                    endDateParams += "&taskCompletedBefore=" + isoDateFormat.format(monthCalendar.getTime());
                }
                String sortParams;

                User currentUser = LoginHistoryService.getCurrentUser();
                for (int i = 0; i < 3; i++) {
                    if (i == 0) {
                        url = "/api/tasks/archive";
                    } else if (i == 1) {
                        sortParams = "&sort=dueDate&order=desc&size=100";
                        url = "/api/tasks/active?filter=oneshot,periodic";
                    } else {
                        url = "/api/tasks/active?filter=constantly";
                    }

                    response = RequestService.createGetRequest(url);
                    Log.d("mojo-response", "url = " + url);

                    if (response.code() == 200) {
                        JSONArray tasksJson = new JSONArray(response.body().string());
                        Log.d("mojo-response", "tasks size = " + tasksJson.length());
                        Log.d("mojo-response", "tasks = " + tasksJson.toString());

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
                                for (Task task : busyTasks)
                                    task.fixTime();
                            } else {
                                permanentTasks = new ObjectMapper().readValue(tasksJson.toString(), new TypeReference<ArrayList<Task>>() {
                                });

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
                    Toast.makeText(getContext(), R.string.tasks_are_temporary_unavailable, Toast.LENGTH_LONG).show();
                } else if (responseCode == 200) {
                    if (!withDay)
                        updateDecorators(busyTasks);

                    if (currentAdapterType == null)
                        currentAdapterType = CurrentAdapterType.BUSY;
                    switch (currentAdapterType) {
                        case BUSY:
                            ((RadioButton) rootView.findViewById(R.id.busy)).setChecked(true);
                            updateTaskAdapter(new TaskAdapter(TasksFragment.this, busyTasks), CurrentAdapterType.BUSY);
                            break;
                        case FINISHED:
                            ((RadioButton) rootView.findViewById(R.id.ended)).setChecked(true);
                            updateTaskAdapter(new TaskAdapter(TasksFragment.this, finishedTasks), CurrentAdapterType.FINISHED);
                            break;
                        case PERMANENT:
                            ((RadioButton) rootView.findViewById(R.id.permanent)).setChecked(true);
                            updateTaskAdapter(new TaskAdapter(TasksFragment.this, permanentTasks), CurrentAdapterType.PERMANENT);
                            break;
                    }

                } else
                    Toast.makeText(getContext(), R.string.unknown_error, Toast.LENGTH_LONG).show();
            } catch (Exception exc) {
                exc.printStackTrace();
            }
        }
    }

    private class UpdateCurrentMonthDecorators extends AsyncTask<Void, Void, Integer> {
        private ArrayList<Task> monthTasks;

        protected Integer doInBackground(Void... params) {
            try {
                monthTasks = new ArrayList<>();
                String url;

                SimpleDateFormat isoDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.getDefault());
                isoDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));


                String dateParams;
                Calendar monthCalendar = Calendar.getInstance();
                monthCalendar.setTime(currentDate.getTime());
                monthCalendar.set(Calendar.DAY_OF_MONTH, 1);
                dateParams = "&dueAfter=" + isoDateFormat.format(monthCalendar.getTime());

                monthCalendar.add(Calendar.MONTH, 1);
                dateParams += "&dueBefore=" + isoDateFormat.format(monthCalendar.getTime());
                String sortParams = "&sort=dueDate&order=desc&size=100";

                url = App.getTask_host() + "/runtime/tasks?assignee="
                        + LoginHistoryService.getCurrentUser().username + "&includeProcessVariables=TRUE" + dateParams + sortParams;

                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder().header("Authorization", Credentials.basic(Data.getTaskAuthLogin(), Data.taskAuthPass))
                        .url(url).build();

                Response response = client.newCall(request).execute();

                if (response.code() == 200) {
                    JSONArray tasksJson = new JSONObject(response.body().string()).getJSONArray("data");
                    monthTasks = new ObjectMapper().readValue(tasksJson.toString(), new TypeReference<ArrayList<Task>>() {
                    });
                }
                return null;
            } catch (Exception exc) {
                exc.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(Integer responseCode) {
            super.onPostExecute(responseCode);
            try {
                updateDecorators(monthTasks);
            } catch (Exception exc) {
                exc.printStackTrace();
            }
        }
    }
}
