package org.dev_alex.mojo_qa.mojo.fragments.create_task

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.widget.AppCompatSpinner
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.google.gson.Gson
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_select_task_rules.*
import kotlinx.android.synthetic.main.view_add_user_email.view.*
import kotlinx.android.synthetic.main.view_selected_executor.view.*
import kotlinx.android.synthetic.main.view_task_control_range.*
import kotlinx.android.synthetic.main.view_task_control_range.view.*
import org.dev_alex.mojo_qa.mojo.CreateTaskModel
import org.dev_alex.mojo_qa.mojo.CreateTaskModel.TaskType.*
import org.dev_alex.mojo_qa.mojo.R
import org.dev_alex.mojo_qa.mojo.activities.PickExecutorsActivity
import org.dev_alex.mojo_qa.mojo.event.ExecutorsPickedEvent
import org.dev_alex.mojo_qa.mojo.models.response.appointment.AppointmentData
import org.dev_alex.mojo_qa.mojo.services.RequestService
import org.dev_alex.mojo_qa.mojo.services.Utils
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONArray
import org.json.JSONObject
import java.util.*
import kotlin.collections.ArrayList


@Suppress("DEPRECATION")
class SelectTaskRulesFragment : Fragment() {
    private var loopDialog: ProgressDialog? = null

    private val model: CreateTaskModel
        get() = CreateTaskModel.instance!!

    private var loadDisposable: Disposable? = null

    private var selectedRange: CreateTaskModel.NotifyRange? = null
    private var selectedUsersBlock: LinearLayout? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_select_task_rules, container, false)

        Utils.setupCloseKeyboardUI(activity, rootView)
        initDialog()
        setupHeader()
        return rootView
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        EventBus.getDefault().register(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        addNotifyRange()

        btAddRange.setOnClickListener {
            addNotifyRange()
        }

        btCreateAppointment.setOnClickListener {
            sendData()
        }

        btSkip.setOnClickListener {
            model.notifyRanges = ArrayList()
            sendData()
        }

        btExit.setOnClickListener {
            activity?.supportFragmentManager?.popBackStack("CreateTaskInfoFragment", FragmentManager.POP_BACK_STACK_INCLUSIVE)
        }

        retainInstance = true
    }

    private fun setupHeader() {
        (activity?.findViewById<View>(R.id.title) as TextView).text = getString(R.string.assignments)
        activity?.findViewById<View>(R.id.back_btn)?.visibility = View.VISIBLE
        activity?.findViewById<View>(R.id.grid_btn)?.visibility = View.GONE
        activity?.findViewById<View>(R.id.sandwich_btn)?.visibility = View.GONE
        activity?.findViewById<View>(R.id.group_by_btn)?.visibility = View.GONE
        activity?.findViewById<View>(R.id.search_btn)?.visibility = View.GONE
        activity?.findViewById<View>(R.id.notification_btn)?.visibility = View.GONE
        activity?.findViewById<View>(R.id.qr_btn)?.visibility = View.GONE

        activity?.findViewById<View>(R.id.back_btn)?.setOnClickListener {
            activity?.supportFragmentManager?.popBackStack()
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onExecutorsPicked(event: ExecutorsPickedEvent) {
        selectedRange?.let { range ->
            range.selectedUsersList = event.users.toMutableList()
            selectedUsersBlock?.let {
                showPickedUsers(range, it)
            }
        }
    }

    private fun addNotifyRange() {
        val notifyRange = CreateTaskModel.NotifyRange()
        val rangeView = layoutInflater.inflate(R.layout.view_task_control_range, vRulesContainer, false)

        val rangeTypes = CreateTaskModel.NotifyRangeType.values()
        val rangeTypesString = rangeTypes.map { getString(it.nameRes) }
        val rangeTypeAdapter = ArrayAdapter(requireContext(), R.layout.spinner_task_type_item, rangeTypesString)
        rangeTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        rangeView.spTaskControlType.adapter = rangeTypeAdapter
        rangeView.spTaskControlType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                notifyRange.type = rangeTypes[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }
        rangeView.spTaskControlType.setSelection(0)

        initRangeValueSpinner(rangeView.spFrom) {
            notifyRange.from = it
        }
        initRangeValueSpinner(rangeView.spTo) {
            notifyRange.to = it
        }
        rangeView.spTo.setSelection(80)

        val valueTypes = listOf(getString(R.string.percent), getString(R.string.points))
        val valueTypeAdapter = ArrayAdapter(requireContext(), R.layout.spinner_task_type_item, valueTypes)
        valueTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        rangeView.spRangeType.adapter = valueTypeAdapter
        rangeView.spRangeType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                notifyRange.isPercent = position == 0
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }

        rangeView.etNotifyMessage.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun afterTextChanged(text: Editable?) {
                notifyRange.message = text?.toString().orEmpty()
            }
        })

        rangeView.btAddUser.setOnClickListener {
            selectedRange = notifyRange
            selectedUsersBlock = rangeView.vAddedUsersBlock

            startActivity(PickExecutorsActivity.getActivityIntent(context, notifyRange.selectedUsersList, emptyList()))
        }

        rangeView.btAddEmail.setOnClickListener {
            val inputView = createInputUserEmail(notifyRange)
            rangeView.vAddedEmailsBlock.addView(inputView)
        }

        vRulesContainer.addView(rangeView)
        model.notifyRanges.add(notifyRange)

        scrollView.post { scrollView?.fullScroll(ScrollView.FOCUS_DOWN) }
    }

    private fun showPickedUsers(range: CreateTaskModel.NotifyRange, selectedUsersBlock: LinearLayout) {
        selectedUsersBlock.removeAllViews()

        range.selectedUsersList.forEach { user ->
            val view = layoutInflater.inflate(R.layout.view_selected_executor, selectedUsersBlock, false)
            view.tvExecutor.text = user.fullname.takeIf { it.isNotBlank() } ?: user.username
            view.btDelete.setOnClickListener {
                range.selectedUsersList.remove(user)
                selectedUsersBlock.removeView(view)
            }

            selectedUsersBlock.addView(view)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun createInputUserEmail(range: CreateTaskModel.NotifyRange): View {
        val keyUUID = UUID.randomUUID().toString()

        val inputView = layoutInflater.inflate(R.layout.view_add_user_email, vAddedUsersBlock, false)
        val etEmail = inputView.etEmail

        etEmail.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun afterTextChanged(p0: Editable?) {
                val emailStr = p0?.toString().orEmpty()
                if (emailStr.isEmail()) {
                    range.emailsMap[keyUUID] = emailStr
                } else {
                    range.emailsMap.remove(keyUUID)
                }
            }
        })

        return inputView
    }

    private fun initRangeValueSpinner(spinner: AppCompatSpinner, callback: (Int) -> Unit) {
        val minutesArray = (0..100).toList()
        val hoursArrayStr = minutesArray.map { it.toString() }
        val adapter = ArrayAdapter(requireContext(), R.layout.spinner_task_type_item, hoursArrayStr)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        spinner.adapter = adapter
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                callback(minutesArray[position])
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }
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

    private fun sendData() {
        val jsonObject = JSONObject()
        jsonObject.put("name", model.taskName)
        jsonObject.put("documentFolderNode", model.file?.id)
        jsonObject.put("executionPeriod", true)

        val executorsJson = JSONObject().apply {
            put("accounts", JSONArray(model.selectedUsers.map { it.id }))
            put("groups", JSONArray(model.selectedGroups.map { it.id }))
        }
        jsonObject.put("executors", executorsJson)

        val configJson = JSONObject()
        configJson.put("documentFolder", model.file?.id)
        when (model.taskType) {
            CONSTANT -> {
                configJson.put("constantly", true)
            }
            PERIODICAL -> {
                val periodTime = String.format("%02d:%02d", model.periodicalTaskHour ?: 0, model.periodicalTaskMinutes ?: 0)
                val selectedPeriod = model.selectedPeriod

                val periodicalConfigJson = JSONObject().apply {
                    when (selectedPeriod) {
                        CreateTaskModel.TaskPeriod.Daily -> {
                            put("daily", JSONObject().apply {
                                put("times", JSONArray(model.periodicalTimes.orEmpty()))
                            })
                        }
                        is CreateTaskModel.TaskPeriod.Weekly -> {
                            put("weekly", JSONObject().apply {
                                put("times", JSONArray(model.periodicalTimes.orEmpty()))
                                put("weekDays", JSONArray(selectedPeriod.days))
                            })
                        }
                        is CreateTaskModel.TaskPeriod.Monthly -> {
                            put("monthly", JSONObject().apply {
                                put("times", JSONArray(model.periodicalTimes.orEmpty()))
                                put("monthDays", JSONArray(selectedPeriod.days))
                            })
                        }
                    }
                    put("is_start", true)
                }
                configJson.put("periodic", periodicalConfigJson)
                val period = ((model.periodicalTaskHour ?: 0) * 60 + (model.periodicalTaskMinutes ?: 0)) * 60
                jsonObject.put("execution_period", period)
            }
            ONE_SHOT -> {
                val oneShotConfigJson = JSONObject().apply {
                    put("datetime", model.startOneShotDate?.time ?: 0L)
                    put("is_start", true)
                }
                configJson.put("oneshot", oneShotConfigJson)

                val period = (model.endOneShotDate?.time ?: 0L) - (model.startOneShotDate?.time ?: 0L)
                jsonObject.put("execution_period", period)
            }
            PRIVATE_POLL -> {
                configJson.put("closedlinks", true)
            }
            OPEN_POLL -> {
                configJson.put("openlinks", true)
            }
        }
        jsonObject.put("config", configJson)


        if (model.taskType == PRIVATE_POLL || model.taskType == OPEN_POLL) {
            val pollJson = JSONObject().apply {
                put("expire_date", model.endOpenPollDate?.time?.div(1000) ?: 0L)
                put("execution_limit", model.pollPersonsCount ?: 0)
                put("name", model.taskName)
            }

            val pollJsonList = ArrayList<JSONObject>()
            pollJsonList.add(pollJson)
            jsonObject.put("links", JSONArray(pollJsonList))
        }

        val rangesJsonList = model.notifyRanges.map {
            JSONObject().apply {
                put("from", it.from)
                put("to", it.to)
                put("type", if (it.type == CreateTaskModel.NotifyRangeType.IN_RANGE) "IN" else "OUT")
                put("value", if (it.isPercent) "PERCENT" else "VALUE")
                put("msg", it.message)
                put("email", JSONArray(it.emailsList))

                val usersJsonList = it.selectedUsersList.map {
                    JSONObject().apply {
                        put("id", it.id)
                        put("is_push", true)
                        put("is_email", false)
                    }
                }

                put("accounts", JSONArray(usersJsonList))
            }
        }
        jsonObject.put("ranges", JSONArray(rangesJsonList))

        loadDisposable?.dispose()
        loopDialog?.show()
        loadDisposable = Observable.create<AppointmentData> {
            val url = "/api/tasks/refs/${model.file?.id}"
            val response = RequestService.createPostRequest(url, jsonObject.toString())

            if (response.code == 200) {
                val responseJson = response.body?.string() ?: "{}"
                val responseData = Gson().fromJson(responseJson, AppointmentData::class.java)
                it.onNext(responseData)
                it.onComplete()
            } else {
                val errorJson = response.body?.string() ?: "{}"
                it.onError(Exception("code = ${response.code}"))
            }
        }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                loopDialog?.dismiss()
                Toast.makeText(context, R.string.appointment_created_successfully, Toast.LENGTH_SHORT).show()

                if (model.taskType == PRIVATE_POLL || model.taskType == OPEN_POLL) {
                    model.createAppointmentResponse = it

                    activity?.supportFragmentManager?.popBackStack("CreateTaskInfoFragment", FragmentManager.POP_BACK_STACK_INCLUSIVE)
                    activity?.supportFragmentManager
                        ?.beginTransaction()
                        ?.replace(R.id.container, ShowPollDataFragment.newInstance(), null)
                        ?.commit()
                } else {
                    activity?.supportFragmentManager?.popBackStack("CreateTaskInfoFragment", FragmentManager.POP_BACK_STACK_INCLUSIVE)
                }
            }, {
                Toast.makeText(context, it.localizedMessage, Toast.LENGTH_SHORT).show()
                loopDialog?.dismiss()
                it.printStackTrace()
            })
    }

    override fun onDestroyView() {
        loadDisposable?.dispose()
        loopDialog?.dismiss()
        super.onDestroyView()
    }

    override fun onDestroy() {
        EventBus.getDefault().unregister(this)
        super.onDestroy()
    }

    companion object {
        @JvmStatic
        fun newInstance(): SelectTaskRulesFragment {
            return SelectTaskRulesFragment()
        }
    }

    fun String.isEmail(): Boolean {
        val emailRegex = "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$".toRegex(RegexOption.IGNORE_CASE)
        return matches(emailRegex)
    }
}