package org.dev_alex.mojo_qa.mojo.fragments.create_task

import android.app.DatePickerDialog
import android.app.ProgressDialog
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.AppCompatSpinner
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.TextView
import kotlinx.android.synthetic.main.fragment_create_task_info.*
import org.dev_alex.mojo_qa.mojo.CreateTaskModel
import org.dev_alex.mojo_qa.mojo.CreateTaskModel.TaskType
import org.dev_alex.mojo_qa.mojo.R
import org.dev_alex.mojo_qa.mojo.fragments.GraphListFragment
import org.dev_alex.mojo_qa.mojo.models.File
import org.dev_alex.mojo_qa.mojo.models.Panel
import org.dev_alex.mojo_qa.mojo.services.Utils
import java.text.SimpleDateFormat
import java.util.*


@Suppress("DEPRECATION")
class CreateTaskInfoFragment : Fragment() {
    private var loopDialog: ProgressDialog? = null

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
                model.name = s.toString()
            }
        })
        initTaskTypeSpinner()
        initOneShotTaskViews()
        initOpenPollTaskViews()
        initPeriodicalTaskViews()
    }

    private fun initTaskTypeSpinner() {
        val taskTypes = TaskType.values()
        val taskTypesString = taskTypes.map { getString(it.nameRes) }

        val adapter = ArrayAdapter(requireContext(), R.layout.spinner_task_type_item, taskTypesString)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spTaskType.adapter = adapter
        spTaskType.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View, position: Int, id: Long) {
                val selectedType = taskTypes[position]
                model.type = selectedType

                vOneShotBlock.visibility = View.GONE
                vOpenPollBlock.visibility = View.GONE
                vPeriodicalBlock.visibility = View.GONE

                btSelectExecutor.visibility = View.GONE
                btSelectRules.visibility = View.GONE
                when (selectedType) {
                    TaskType.OPEN_LINK -> {
                        btSelectRules.visibility = View.VISIBLE
                    }
                    TaskType.OPEN_POLL -> {
                        btSelectRules.visibility = View.VISIBLE
                        vOpenPollBlock.visibility = View.VISIBLE
                    }

                    TaskType.CONSTANT -> {
                        btSelectExecutor.visibility = View.VISIBLE
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

        spOneShotStartHour.setSelection(9)
        spOneShotEndHour.setSelection(21)
    }

    private fun initPeriodicalTaskViews() {
        initHourSpinner(spPeriodicalHour) {
            model.periodicalTaskHour = it
        }
        initMinuteSpinner(spPeriodicalMinute) {
            model.periodicalTaskMinutes = it
        }
        spPeriodicalHour.setSelection(12)
        spPeriodicalMinute.setSelection(48)
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

    private fun onPanelClick(panel: Panel) {
        activity
                ?.supportFragmentManager
                ?.beginTransaction()
                ?.replace(R.id.container, GraphListFragment.newInstance(panel))
                ?.addToBackStack(null)
                ?.commit()
    }

    companion object {
        @JvmStatic
        fun newInstance(file: File?): CreateTaskInfoFragment {
            CreateTaskModel.instance?.file = file
            return CreateTaskInfoFragment()
        }
    }
}