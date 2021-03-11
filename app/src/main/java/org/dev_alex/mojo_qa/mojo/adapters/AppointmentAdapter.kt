package org.dev_alex.mojo_qa.mojo.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.card_appointment.view.*
import org.dev_alex.mojo_qa.mojo.R
import org.dev_alex.mojo_qa.mojo.adapters.AppointmentAdapter.AppointmentViewHolder
import org.dev_alex.mojo_qa.mojo.models.response.appointment.AppointmentData
import java.text.SimpleDateFormat
import java.util.*

class AppointmentAdapter(private val appointments: List<AppointmentData>, private val listener: AppointmentEventListener) : RecyclerView.Adapter<AppointmentViewHolder>() {
    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): AppointmentViewHolder {
        val v = LayoutInflater.from(viewGroup.context).inflate(R.layout.card_appointment, viewGroup, false)
        return AppointmentViewHolder(v)
    }

    override fun onBindViewHolder(viewHolder: AppointmentViewHolder, i: Int) {
        val appointment = appointments[i]
        viewHolder.bind(appointment)
    }

    override fun getItemCount(): Int {
        return appointments.size
    }

    interface AppointmentEventListener {

    }

    class AppointmentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val sdf = SimpleDateFormat("dd MMMM yyyy | HH:mm", Locale.getDefault())

        fun bind(appointment: AppointmentData) {
            with(itemView) {
                if (appointment.createDate != null) {
                    appointmentDate.visibility = View.VISIBLE
                    appointmentDate.text = sdf.format(Date(appointment.createDate ?: 0L))
                } else {
                    appointmentDate.visibility = View.GONE
                }
                appointmentTitle.text = appointment.name

                tvAppointmentType.text = when (appointment.type) {
                    "openlinks" -> context.getString(R.string.task_type_open_poll)
                    "closedlinks" -> context.getString(R.string.task_type_private_poll)
                    "constantly" -> context.getString(R.string.task_type_constant)
                    "periodic" -> context.getString(R.string.task_type_periodical)
                    "oneshot" -> context.getString(R.string.task_type_oneshot)
                    else -> appointment.type
                }

                if (appointment.type == "openlinks") {
                    ivAppointmentIcon.setImageResource(R.drawable.ic_open_link)
                } else if (appointment.type == "closedlinks") {
                    ivAppointmentIcon.setImageResource(R.drawable.ic_close_link)
                } else if (appointment.type == "constantly") {
                    ivAppointmentIcon.setImageResource(R.drawable.file_icon)
                } else if (appointment.type == "periodic") {
                    ivAppointmentIcon.setImageResource(R.drawable.ic_periodical)
                } else if (appointment.type == "oneshot") {
                    ivAppointmentIcon.setImageResource(R.drawable.ic_oneshot)
                }

                btClose.setOnClickListener { v -> vExpandable.collapse(true) }
                vMainAppointmentBlock.setOnClickListener { v ->
                    vExpandable.toggle(true)
                }

                vExpandable.collapse(false)
            }
        }
    }
}