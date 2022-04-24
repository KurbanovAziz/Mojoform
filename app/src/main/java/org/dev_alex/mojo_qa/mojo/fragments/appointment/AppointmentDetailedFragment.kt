package org.dev_alex.mojo_qa.mojo.fragments.appointment

import android.app.ProgressDialog
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import io.reactivex.rxjava3.disposables.Disposable
import kotlinx.android.synthetic.main.fragment_appointment_detailed.*
import org.dev_alex.mojo_qa.mojo.AppointmentsModel
import org.dev_alex.mojo_qa.mojo.R
import org.dev_alex.mojo_qa.mojo.models.response.appointment.AppointmentData
import org.dev_alex.mojo_qa.mojo.services.Utils


@Suppress("DEPRECATION")
class AppointmentDetailedFragment : Fragment() {
    private var loopDialog: ProgressDialog? = null
    private var loadDisposable: Disposable? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_appointment_detailed, container, false)
        Utils.setupCloseKeyboardUI(activity, rootView)
        initDialog()

        loadAppointment(arguments?.getLong(ARG_APPOINTMENT_ID) ?: 0L)
        setupHeader()
        return rootView
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

    private fun initDialog() {
        loopDialog = ProgressDialog(context, R.style.ProgressDialogStyle)
        loopDialog?.setProgressStyle(ProgressDialog.STYLE_SPINNER)
        loopDialog?.setMessage(getString(R.string.loading_please_wait))
        loopDialog?.isIndeterminate = true
        loopDialog?.setCanceledOnTouchOutside(false)
        loopDialog?.setCancelable(false)
    }

    private fun showAppointment(appointment: AppointmentData) {
        val executorsText = (appointment.executors?.accounts?.map { if (it.fullname.isBlank()) it.username else it.fullname }.orEmpty() +
                appointment.executors?.groups?.map { it.name }.orEmpty()).joinToString(", ")

        val taskType = when (appointment.type) {
            "openlinks" -> requireContext().getString(R.string.task_type_open_poll)
            "closedlinks" -> requireContext().getString(R.string.task_type_private_poll)
            "constantly" -> requireContext().getString(R.string.task_type_constant)
            "periodic" -> requireContext().getString(R.string.task_type_periodical)
            "oneshot" -> requireContext().getString(R.string.task_type_oneshot)
            else -> appointment.type
        }

        styleKeyValueTextView(getString(R.string.organization), appointment.name, tvOrganization)
        styleKeyValueTextView(getString(R.string.form), appointment.templateName.orEmpty(), tvForm)
        styleKeyValueTextView(getString(R.string.type_of_task), taskType, tvType)
        styleKeyValueTextView(getString(R.string.executors), executorsText, tvExecutors)
    }

    override fun onDestroyView() {
        loadDisposable?.dispose()
        super.onDestroyView()
    }

    private fun loadAppointment(id: Long) {
        loadDisposable?.dispose()

        loopDialog?.show()
        loadDisposable = AppointmentsModel.loadAppointmentDetails(id)
            .subscribe({
                loopDialog?.dismiss()
                showAppointment(it)
            }, {
                loopDialog?.dismiss()
                it.printStackTrace()
            })
    }

    private fun styleKeyValueTextView(key: String, value: String, textView: TextView) {
        val resValue = if (value.isBlank()) "-" else value
        val sourceString = "<b>$key:</b>   $resValue"
        textView.text = Html.fromHtml(sourceString)
    }

    companion object {
        private const val ARG_APPOINTMENT_ID = "arg_appointment_id"

        @JvmStatic
        fun newInstance(appointmentId: Long): AppointmentDetailedFragment {
            return AppointmentDetailedFragment().apply {
                arguments = Bundle().apply {
                    putLong(ARG_APPOINTMENT_ID, appointmentId)
                }
            }
        }
    }
}