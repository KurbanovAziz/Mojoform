package org.dev_alex.mojo_qa.mojo.fragments.create_task

import android.app.DatePickerDialog
import android.app.ProgressDialog
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.widget.AdapterView.OnItemSelectedListener
import androidx.appcompat.widget.AppCompatSpinner
import androidx.fragment.app.Fragment
import com.google.gson.Gson
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_create_task_info.*
import org.dev_alex.mojo_qa.mojo.CreateTaskModel
import org.dev_alex.mojo_qa.mojo.CreateTaskModel.TaskType
import org.dev_alex.mojo_qa.mojo.R
import org.dev_alex.mojo_qa.mojo.models.File
import org.dev_alex.mojo_qa.mojo.models.response.OrgUsersResponse
import org.dev_alex.mojo_qa.mojo.services.RequestService
import org.dev_alex.mojo_qa.mojo.services.Utils
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


@Suppress("DEPRECATION")
class CreateTaskInfoFragment : Fragment() {
    private var loopDialog: ProgressDialog? = null
    private var loadDisposable: Disposable? = null

    private val model: CreateTaskModel
        get() = CreateTaskModel.instance!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_create_task_info, container, false)

        Utils.setupCloseKeyboardUI(activity, rootView)
        initDialog()
        setupHeader()
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        tvFileName.text = model.file?.name
        etTaskName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                model.taskName = s.toString()
            }
        })
        initTaskTypeSpinner()
        initOneShotTaskViews()
        initOpenPollTaskViews()
        initPeriodicalTaskViews()
        initConstantTaskViews()

        btSelectExecutor.setOnClickListener {
            val validationErrorRes = model.isValid()
            if (validationErrorRes == null) {
                showNextFragment(SelectTaskExecutorsFragment.newInstance())
            } else {
                Toast.makeText(context, validationErrorRes, Toast.LENGTH_SHORT).show()
            }
        }

        btSelectRules.setOnClickListener {
            val validationErrorRes = model.isValid()

            if (validationErrorRes == null) {
                loadUsersAndShowRules()
            } else {
                Toast.makeText(context, validationErrorRes, Toast.LENGTH_SHORT).show()
            }
        }

        btExit.setOnClickListener {
            activity?.supportFragmentManager?.popBackStack()
        }
    }

    private fun initTaskTypeSpinner() {
        val taskTypes = TaskType.values()
        val taskTypesString = taskTypes.map { getString(it.nameRes) }

        val adapter = ArrayAdapter(requireContext(), R.layout.spinner_task_type_item, taskTypesString)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spTaskType.adapter = adapter
        spTaskType.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedType = taskTypes[position]
                model.taskType = selectedType

                vOneShotBlock.visibility = View.GONE
                vPollTasksBlock.visibility = View.GONE
                vPeriodicalBlock.visibility = View.GONE
                vConstantBlock.visibility = View.GONE

                btSelectExecutor.visibility = View.GONE
                btSelectRules.visibility = View.GONE
                when (selectedType) {
                    TaskType.PRIVATE_POLL -> {
                        btSelectRules.visibility = View.VISIBLE
                        vPollTasksBlock.visibility = View.VISIBLE
                    }
                    TaskType.OPEN_POLL -> {
                        btSelectRules.visibility = View.VISIBLE
                        vPollTasksBlock.visibility = View.VISIBLE
                    }

                    TaskType.CONSTANT -> {
                        btSelectExecutor.visibility = View.VISIBLE
                        vConstantBlock.visibility = View.VISIBLE
                    }
                    TaskType.PERIODICAL -> {
                        btSelectExecutor.visibility = View.VISIBLE
                        vPeriodicalBlock.visibility = View.VISIBLE
                    }
                    TaskType.ONE_SHOT -> {
                        vOneShotBlock.visibility = View.VISIBLE
                        btSelectExecutor.visibility = View.VISIBLE
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                btSelectExecutor.visibility = View.GONE
                btSelectRules.visibility = View.GONE
            }
        }
    }

    private fun initOneShotTaskViews() {
        model.startOneShotDate = Date()
        model.endOneShotDate = Date()

        val styleTextDateView = { tv: TextView, date: Date ->
            val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
            tv.text = sdf.format(date)
        }

        styleTextDateView(btOneShotStartDate, model.startOneShotDate ?: Date())
        styleTextDateView(btOneShotEndDate, model.endOneShotDate ?: Date())

        btOneShotStartDate.setOnClickListener {
            showDatePicker(model.startOneShotDate ?: Date()) {
                model.updateStartOneShotDatePart(it)
                styleTextDateView(btOneShotStartDate, it)
            }
        }
        btOneShotEndDate.setOnClickListener {
            showDatePicker(model.endOneShotDate ?: Date()) {
                model.updateEndOneShotDatePart(it)
                styleTextDateView(btOneShotEndDate, it)
            }
        }

        initHourSpinner(spOneShotStartHour) {
            model.updateStartOneShotHourPart(it)
        }
        initHourSpinner(spOneShotEndHour) {
            model.updateEndOneShotHourPart(it)
        }

        initMinuteSpinner(spOneShotStartMinute) {
            model.updateStartOneShotMinutePart(it)
        }
        initMinuteSpinner(spOneShotEndMinute) {
            model.updateEndOneShotMinutePart(it)
        }

        val currentDate = Calendar.getInstance().apply { time = Date() }
        spOneShotStartHour.setSelection(currentDate.get(Calendar.HOUR_OF_DAY))
        spOneShotEndHour.setSelection(currentDate.get(Calendar.HOUR_OF_DAY) + 1)

        spOneShotStartMinute.setSelection(currentDate.get(Calendar.MINUTE))
        spOneShotEndMinute.setSelection(currentDate.get(Calendar.MINUTE))
    }

    private fun initPeriodicalTaskViews() {
        initHourSpinner(spPeriodicalHour) {
            model.periodicalTaskHour = it
        }
        initMinuteSpinner(spPeriodicalMinute) {
            model.periodicalTaskMinutes = it
        }

        val currentDate = Calendar.getInstance().apply { time = Date() }
        spPeriodicalHour.setSelection(currentDate.get(Calendar.HOUR_OF_DAY))
        spPeriodicalMinute.setSelection(currentDate.get(Calendar.MINUTE))

        val clearContent = { vPeriodContent.removeAllViews() }
        val renderRadioButton = { title: String ->
            CheckBox(context).apply {
                text = title
                setTextColor(resources.getColorStateList(R.color.radiobutton_text_color))
                setBackgroundResource(R.drawable.date_radiobutton_bg)
                setButtonDrawable(android.R.color.transparent)
                gravity = Gravity.CENTER
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 23f)

                val size = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 35f, resources.displayMetrics).toInt()
                layoutParams = ViewGroup.LayoutParams(size, size).apply {
                    gravity = Gravity.CENTER
                }
                setPadding(0, 0, 0, TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2f, resources.displayMetrics).toInt())
            }
        }
        val renderTitle = { title: String ->
            TextView(context).apply {
                text = title
                setTextColor(Color.parseColor("#726583"))
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
            }
        }

        val renderDaysContent = {
            clearContent()
            vPeriodContent.addView(renderTitle(getString(R.string.every_day)))
        }
        val renderWeeksContent = {
            clearContent()
            vPeriodContent.addView(renderTitle(getString(R.string.every_week)))

            val gridLayout = GridLayout(context).apply {
                columnCount = 7
                layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                    topMargin = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 6f, resources.displayMetrics).toInt()
                }
            }

            val weekDays = ArrayList<Int>()
            (0..6).toList().forEach { dayPos ->
                val dayName = resources.getStringArray(R.array.day_names)[dayPos]
                val compoundButton = renderRadioButton(dayName)
                compoundButton.setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) {
                        weekDays.add(dayPos)
                    } else {
                        weekDays.remove(dayPos)
                    }
                    model.selectedPeriod = CreateTaskModel.TaskPeriod.Weekly(weekDays)
                }
                val frame = FrameLayout(requireContext()).apply {
                    layoutParams = GridLayout.LayoutParams().apply {
                        columnSpec = GridLayout.spec(GridLayout.UNDEFINED, GridLayout.FILL, 1f)
                        bottomMargin = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5f, resources.displayMetrics).toInt()
                    }
                }
                frame.addView(compoundButton)
                gridLayout.addView(frame)
            }
            vPeriodContent.addView(gridLayout)
        }
        val renderMonthContent = {
            clearContent()
            vPeriodContent.addView(renderTitle(getString(R.string.every_month)))
            val gridLayout = GridLayout(context).apply {
                columnCount = 7
                layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                    topMargin = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 6f, resources.displayMetrics).toInt()
                }
            }

            val daysList = ArrayList<Int>()
            (1..31).toList().forEach { day ->
                val compoundButton = renderRadioButton(day.toString())
                compoundButton.setOnCheckedChangeListener { buttonView, isChecked ->
                    if (isChecked) {
                        daysList.add(day)
                    } else {
                        daysList.remove(day)
                    }
                    model.selectedPeriod = CreateTaskModel.TaskPeriod.Monthly(daysList)
                }
                val frame = FrameLayout(requireContext()).apply {
                    layoutParams = GridLayout.LayoutParams().apply {
                        columnSpec = GridLayout.spec(GridLayout.UNDEFINED, GridLayout.FILL, 1f)
                        bottomMargin = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5f, resources.displayMetrics).toInt()
                    }
                }
                frame.addView(compoundButton)
                gridLayout.addView(frame)
            }
            vPeriodContent.addView(gridLayout)
        }

        vPeriodGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rbPeriodDays -> {
                    renderDaysContent()
                    model.selectedPeriod = CreateTaskModel.TaskPeriod.Daily
                }
                R.id.rbPeriodWeeks -> {
                    renderWeeksContent()
                    model.selectedPeriod = CreateTaskModel.TaskPeriod.Weekly(emptyList())
                }
                R.id.rbPeriodMonth -> {
                    renderMonthContent()
                    model.selectedPeriod = CreateTaskModel.TaskPeriod.Monthly(emptyList())
                }
            }
        }

        rbPeriodDays.isChecked = true
    }

    private fun initOpenPollTaskViews() {
        model.endOpenPollDate = Date()

        val styleTextDateView = { tv: TextView, date: Date ->
            val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
            tv.text = sdf.format(date)
        }

        styleTextDateView(btOpenPollEndDate, model.endOpenPollDate ?: Date())

        btOpenPollEndDate.setOnClickListener {
            showDatePicker(model.endOpenPollDate ?: Date()) {
                model.endOpenPollDate = it
                styleTextDateView(btOpenPollEndDate, it)
            }
        }


        val countArray = (1..1000).toList()
        val countArrayStr = countArray.map { it.toString() }
        val adapter = ArrayAdapter(requireContext(), R.layout.spinner_task_type_item, countArrayStr)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        spOpenPollCount.adapter = adapter
        spOpenPollCount.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                model.pollPersonsCount = countArray[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }

        spOpenPollCount.setSelection(9)
    }

    private fun initConstantTaskViews() {

    }

    private fun initHourSpinner(spinner: AppCompatSpinner, callback: (Int) -> Unit) {
        val hoursArray = (0..23).toList()
        val hoursArrayStr = hoursArray.map { String.format("%02d", it) }
        val adapter = ArrayAdapter(requireContext(), R.layout.spinner_task_type_item, hoursArrayStr)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        spinner.adapter = adapter
        spinner.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                callback(hoursArray[position])
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }
    }

    private fun initMinuteSpinner(spinner: AppCompatSpinner, callback: (Int) -> Unit) {
        val minutesArray = (0..59).toList()
        val hoursArrayStr = minutesArray.map { String.format("%02d", it) }
        val adapter = ArrayAdapter(requireContext(), R.layout.spinner_task_type_item, hoursArrayStr)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        spinner.adapter = adapter
        spinner.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                callback(minutesArray[position])
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }
    }

    private fun showDatePicker(preset: Date, callback: (Date) -> Unit) {
        val cal = Calendar.getInstance().apply { time = preset }
        val year = cal[Calendar.YEAR]
        val month = cal[Calendar.MONTH]
        val day = cal[Calendar.DAY_OF_MONTH]

        val datePickerDialog = DatePickerDialog(requireContext(), { _, year, monthOfYear, dayOfMonth ->
            val resCalendar = Calendar.getInstance()
            resCalendar[Calendar.YEAR] = year
            resCalendar[Calendar.MONTH] = monthOfYear
            resCalendar[Calendar.DAY_OF_MONTH] = dayOfMonth

            callback(resCalendar.time)
        }, year, month, day)
        datePickerDialog.show()
    }

    private fun setupHeader() {
        (activity?.findViewById<View>(R.id.title) as TextView).text = getString(R.string.assignments)
        activity?.findViewById<View>(R.id.back_btn)?.visibility = View.GONE
        activity?.findViewById<View>(R.id.grid_btn)?.visibility = View.GONE
        activity?.findViewById<View>(R.id.sandwich_btn)?.visibility = View.VISIBLE
        activity?.findViewById<View>(R.id.group_by_btn)?.visibility = View.GONE
        activity?.findViewById<View>(R.id.search_btn)?.visibility = View.GONE
        activity?.findViewById<View>(R.id.notification_btn)?.visibility = View.GONE
        activity?.findViewById<View>(R.id.qr_btn)?.visibility = View.GONE
    }

    private fun initDialog() {
        loopDialog = ProgressDialog(context, R.style.ProgressDialogStyle).apply {
            setProgressStyle(ProgressDialog.STYLE_SPINNER)
            setMessage(getString(R.string.loading_please_wait))
            isIndeterminate = true
            setCanceledOnTouchOutside(false)
            setCancelable(false)
        }
    }

    private fun showNextFragment(fragment: Fragment) {
        activity
                ?.supportFragmentManager
                ?.beginTransaction()
                ?.replace(R.id.container, fragment)
                ?.addToBackStack(null)
                ?.commit()
    }

    private fun loadUsersAndShowRules() {
        loadDisposable?.dispose()

        loopDialog?.show()
        loadDisposable = Observable.create<OrgUsersResponse> {
            val url = "/api/orgs/${model.orgId.orEmpty()}/users"
            val response = RequestService.createGetRequest(url)

            if (response.code == 200) {
                val responseJson = response.body?.string() ?: "{}"
                val responseData = Gson().fromJson(responseJson, OrgUsersResponse::class.java)
                it.onNext(responseData)
                it.onComplete()
            } else {
                it.onError(Exception("code = ${response.code}"))
            }
        }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    model.saveUsers(it)
                    loopDialog?.dismiss()
                    showNextFragment(SelectTaskRulesFragment.newInstance())
                }, {
                    loopDialog?.dismiss()
                    it.printStackTrace()
                })
    }

    override fun onDestroyView() {
        loadDisposable?.dispose()
        loopDialog?.dismiss()
        super.onDestroyView()
    }

    companion object {
        @JvmStatic
        fun newInstance(file: File, orgId: String): CreateTaskInfoFragment {
            CreateTaskModel.instance?.clear()
            CreateTaskModel.instance?.file = file
            CreateTaskModel.instance?.orgId = orgId
            return CreateTaskInfoFragment()
        }
    }
}