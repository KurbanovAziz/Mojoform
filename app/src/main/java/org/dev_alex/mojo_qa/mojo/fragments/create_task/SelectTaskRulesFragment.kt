package org.dev_alex.mojo_qa.mojo.fragments.create_task

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.widget.*
import androidx.appcompat.widget.AppCompatSpinner
import androidx.fragment.app.Fragment
import com.google.gson.Gson
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_select_task_rules.*
import kotlinx.android.synthetic.main.view_add_user_email.view.*
import kotlinx.android.synthetic.main.view_add_user_spinner.view.*
import kotlinx.android.synthetic.main.view_task_control_range.*
import kotlinx.android.synthetic.main.view_task_control_range.view.*
import org.dev_alex.mojo_qa.mojo.CreateTaskModel
import org.dev_alex.mojo_qa.mojo.CreateTaskModel.TaskType.*
import org.dev_alex.mojo_qa.mojo.R
import org.dev_alex.mojo_qa.mojo.models.OrgUser
import org.dev_alex.mojo_qa.mojo.models.response.OrgUsersResponse
import org.dev_alex.mojo_qa.mojo.services.RequestService
import org.dev_alex.mojo_qa.mojo.services.Utils
import org.json.JSONObject
import java.util.*


@Suppress("DEPRECATION")
class SelectTaskRulesFragment : Fragment() {
    private var loopDialog: ProgressDialog? = null

    private val model: CreateTaskModel
        get() = CreateTaskModel.instance!!

    private var loadDisposable: Disposable? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_select_task_rules, container, false)

        Utils.setupCloseKeyboardUI(activity, rootView)
        initDialog()
        setupHeader()
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        addNotifyRange()

        btAddRange.setOnClickListener {
            addNotifyRange()
        }

        btCreateAppointment.setOnClickListener {

        }
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
            if (notifyRange.selectedUsersList.size < model.allUsers.size) {
                val spinnerView = createPickUserSpinner(notifyRange)
                rangeView.vAddedUsersBlock.addView(spinnerView)
            } else {
                Toast.makeText(context, getString(R.string.no_available_for_add_users), Toast.LENGTH_SHORT).show()
            }
        }

        rangeView.btAddEmail.setOnClickListener {
            val inputView = createInputUserEmail(notifyRange)
            rangeView.vAddedEmailsBlock.addView(inputView)
        }

        vRulesContainer.addView(rangeView)
        model.notifyRanges.add(notifyRange)

        scrollView.post { scrollView?.fullScroll(ScrollView.FOCUS_DOWN) }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun createPickUserSpinner(range: CreateTaskModel.NotifyRange): View {
        val keyUUID = UUID.randomUUID().toString()

        val spinnerView = layoutInflater.inflate(R.layout.view_add_user_spinner, vAddedUsersBlock, false)
        val spinner = spinnerView.spinner

        val getAdapter = {
            var userList = model.allUsers - range.selectedUsersList
            (spinner.selectedItem as? OrgUser)?.let { userList = listOf(it) + userList }

            val adapter = ArrayAdapter(requireContext(), R.layout.spinner_task_type_item, userList)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

            adapter
        }

        spinner.adapter = getAdapter()
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                (spinner.adapter?.getItem(position) as? OrgUser)?.let {
                    range.selectedUsersMap[keyUUID] = it
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }

        spinner.setOnTouchListener { _, motionEvent ->
            if (motionEvent.action == MotionEvent.ACTION_DOWN) {
                spinner.adapter = getAdapter()
            }
            return@setOnTouchListener false
        }

        return spinnerView
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
            put("accounts", model.selectedUsers.map { it.id })
        }
        jsonObject.put("executors", executorsJson)


        jsonObject.put("config", 1) // ToDo
        when (model.taskType) {
            CONSTANT -> {

            }
            PERIODICAL -> {

            }
            ONE_SHOT -> {

            }
            PRIVATE_POLL -> {

            }
            OPEN_POLL -> {

            }
        }

        if (model.taskType == PRIVATE_POLL || model.taskType == OPEN_POLL) {
            val pollJson = JSONObject().apply {
                put("expire_date", model.endOpenPollDate?.time?.div(1000)?.or(0L))
                put("execution_limit", model.pollPersonsCount ?: 0)
                put("name", model.taskName)
            }

            jsonObject.put("links", listOf(pollJson))
        }

        model.notifyRanges.forEach {
            jsonObject.put("from", it.from)
            jsonObject.put("to", it.to)
            jsonObject.put("type", if (it.type == CreateTaskModel.NotifyRangeType.IN_RANGE) "IN" else "OUT")
            jsonObject.put("value", if (it.isPercent) "PERCENT" else "VALUE")
            jsonObject.put("msg", it.message)
            jsonObject.put("email", it.emailsList)

            val usersJsonList = it.selectedUsersList.map {
                JSONObject().apply {
                    put("id", it.id)
                    put("is_push", true)
                    put("is_email", false)
                }
            }
            jsonObject.put("accounts", usersJsonList)
        }

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
            }
        }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    loopDialog?.dismiss()
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
        fun newInstance(): SelectTaskRulesFragment {
            return SelectTaskRulesFragment()
        }
    }

    fun String.isEmail(): Boolean {
        val emailRegex = "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$".toRegex(RegexOption.IGNORE_CASE)
        return matches(emailRegex)
    }
}