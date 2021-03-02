package org.dev_alex.mojo_qa.mojo.fragments.appointment

import android.app.ProgressDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import io.reactivex.rxjava3.disposables.Disposable
import kotlinx.android.synthetic.main.fragment_appointment_list.*
import kotlinx.android.synthetic.main.fragment_appointment_list.view.*
import org.dev_alex.mojo_qa.mojo.AppointmentsModel
import org.dev_alex.mojo_qa.mojo.R
import org.dev_alex.mojo_qa.mojo.adapters.AppointmentAdapter
import org.dev_alex.mojo_qa.mojo.adapters.AppointmentAdapter.AppointmentEventListener
import org.dev_alex.mojo_qa.mojo.models.response.appointment.AppointmentData
import org.dev_alex.mojo_qa.mojo.services.Utils

@Suppress("DEPRECATION")
class AppointmentListFragment : Fragment() {
    private var loopDialog: ProgressDialog? = null
    private var loadDisposable: Disposable? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_appointment_list, container, false)
        rootView.recyclerView.layoutManager = LinearLayoutManager(context)
        Utils.setupCloseKeyboardUI(activity, rootView)
        initDialog()

        loadAppointments()
        setupHeader()
        return rootView
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
        loopDialog = ProgressDialog(context, R.style.ProgressDialogStyle)
        loopDialog?.setProgressStyle(ProgressDialog.STYLE_SPINNER)
        loopDialog?.setMessage(getString(R.string.loading_please_wait))
        loopDialog?.isIndeterminate = true
        loopDialog?.setCanceledOnTouchOutside(false)
        loopDialog?.setCancelable(false)
    }

    private fun onAppointmentClick(appointment: AppointmentData) {}

    private fun showAppointments(appointments: List<AppointmentData>) {
        recyclerView.adapter = AppointmentAdapter(appointments, object : AppointmentEventListener {})
    }

    override fun onDestroyView() {
        loadDisposable?.dispose()
        super.onDestroyView()
    }

    private fun loadAppointments() {
        loadDisposable?.dispose()

        loopDialog?.show()
        loadDisposable = AppointmentsModel.loadAppointments()
                .subscribe({
                    AppointmentsModel.appointments = it.orEmpty()
                    loopDialog?.dismiss()
                    showAppointments(it.orEmpty())
                }, {
                    loopDialog?.dismiss()
                    it.printStackTrace()
                })
    }

    companion object {
        @JvmStatic
        fun newInstance(): AppointmentListFragment {
            return AppointmentListFragment()
        }
    }
}