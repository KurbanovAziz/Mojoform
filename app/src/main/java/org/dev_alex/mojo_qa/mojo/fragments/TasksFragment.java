package org.dev_alex.mojo_qa.mojo.fragments;

import android.app.ProgressDialog;
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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import org.json.JSONArray;
import org.json.JSONObject;

import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import okhttp3.Credentials;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class TasksFragment extends Fragment {
    private View rootView;
    private ProgressDialog loopDialog;
    private RecyclerView recyclerView;

    private ArrayList<Task> finishedTasks;
    private ArrayList<Task> busyTasks;
    private Calendar currentDate;
    private boolean withDay = false;

    private ArrayList<CalendarDay> daysWithOverdueTasks = new ArrayList<>();
    private ArrayList<CalendarDay> daysWithActualTasks = new ArrayList<>();

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
    }

    private void setupHeader() {
        ((TextView) getActivity().findViewById(R.id.title)).setText(getString(R.string.tasks));
        getActivity().findViewById(R.id.grid_btn).setVisibility(View.GONE);
        getActivity().findViewById(R.id.back_btn).setVisibility(View.GONE);
        getActivity().findViewById(R.id.search_btn).setVisibility(View.GONE);
        getActivity().findViewById(R.id.group_by_btn).setVisibility(View.GONE);

        getActivity().findViewById(R.id.sandwich_btn).setVisibility(View.VISIBLE);
    }

    private void setListeners() {
        ((RadioGroup) rootView.findViewById(R.id.task_toggle)).setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
                switch (checkedId) {
                    case R.id.ended:
                        recyclerView.setAdapter(new TaskAdapter(TasksFragment.this, finishedTasks));
                        break;

                    case R.id.busy:
                        recyclerView.setAdapter(new TaskAdapter(TasksFragment.this, busyTasks));
                        break;

                    case R.id.all:
                        ArrayList<Task> allTasks = new ArrayList<>(busyTasks);
                        allTasks.addAll(finishedTasks);

                        Collections.sort(allTasks, new Comparator<Task>() {
                            @Override
                            public int compare(Task task1, Task task2) {
                                if (task1.dueDate == null)
                                    return 1;
                                if (task2.dueDate == null)
                                    return -1;

                                if (task1.dueDate.getTime() == task2.dueDate.getTime())
                                    return 0;

                                return (task1.dueDate.after(task2.dueDate)) ? -1 : 1;
                            }
                        });

                        recyclerView.setAdapter(new TaskAdapter(TasksFragment.this, allTasks));
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
        MaterialCalendarView calendarView = (MaterialCalendarView) rootView.findViewById(R.id.calendarView);
        for (Task task : monthTasks)
            if (task.dueDate.after(new Date())) {
                if (!daysWithActualTasks.contains(CalendarDay.from(task.dueDate)))
                    daysWithActualTasks.add(CalendarDay.from(task.dueDate));
            } else if (!daysWithOverdueTasks.contains(CalendarDay.from(task.dueDate)))
                daysWithOverdueTasks.add(CalendarDay.from(task.dueDate));

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

    public void showFillTemplateWindow(String templateId, String taskId, String taskNodeId,
                                       long dueDate, String initiator, String siteId) {
        getActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, TemplateFragment.newInstance
                        (templateId, taskId, taskNodeId, dueDate, siteId, initiator)).addToBackStack(null).commit();
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
                String url;

                SimpleDateFormat isoDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.getDefault());
                isoDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));


                String dateParams;
                if (withDay) {
                    Calendar dayCalendar = Calendar.getInstance();
                    dayCalendar.setTime(currentDate.getTime());
                    dateParams = "&dueAfter=" + isoDateFormat.format(dayCalendar.getTime());

                    dayCalendar.add(Calendar.DAY_OF_MONTH, 1);
                    dateParams += "&dueBefore=" + isoDateFormat.format(dayCalendar.getTime());
                } else {
                    Calendar monthCalendar = Calendar.getInstance();
                    monthCalendar.setTime(currentDate.getTime());
                    monthCalendar.set(Calendar.DAY_OF_MONTH, 1);
                    dateParams = "&dueAfter=" + isoDateFormat.format(monthCalendar.getTime());

                    monthCalendar.add(Calendar.MONTH, 1);
                    dateParams += "&dueBefore=" + isoDateFormat.format(monthCalendar.getTime());
                }
                String sortParams = "&sort=dueDate&order=desc&size=100";

                for (int i = 0; i < 2; i++) {
                    if (i == 0)
                        url = App.getTask_host() + "/history/" +
                                "historic-task-instances?finished=TRUE&taskAssignee=" + Data.currentUser.userName + "&includeProcessVariables=TRUE" + dateParams + sortParams;
                    else
                        url = App.getTask_host() + "/runtime/tasks?assignee="
                                + Data.currentUser.userName + "&includeProcessVariables=TRUE" + dateParams + sortParams;

                    OkHttpClient client = new OkHttpClient();
                    Request request = new Request.Builder().header("Authorization", Credentials.basic(Data.taskAuthLogin, Data.taskAuthPass))
                            .url(url).build();

                    response = client.newCall(request).execute();

                    if (response.code() == 200) {
                        JSONArray tasksJson = new JSONObject(response.body().string()).getJSONArray("data");
                        if (i == 0) {
                            finishedTasks = new ObjectMapper().readValue(tasksJson.toString(), new TypeReference<ArrayList<Task>>() {
                            });
                            ArrayList<Task> resultTasks = new ArrayList<>();
                            for (Task task : finishedTasks)
                                if (task.deleteReason == null)
                                    resultTasks.add(task);
                            finishedTasks = resultTasks;
                        } else
                            busyTasks = new ObjectMapper().readValue(tasksJson.toString(), new TypeReference<ArrayList<Task>>() {
                            });
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
                    ((RadioButton) rootView.findViewById(R.id.busy)).setChecked(true);
                    recyclerView.setAdapter(new TaskAdapter(TasksFragment.this, busyTasks));
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
                        + Data.currentUser.userName + "&includeProcessVariables=TRUE" + dateParams + sortParams;

                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder().header("Authorization", Credentials.basic(Data.taskAuthLogin, Data.taskAuthPass))
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
