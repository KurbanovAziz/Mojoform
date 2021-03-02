package org.dev_alex.mojo_qa.mojo

import org.dev_alex.mojo_qa.mojo.models.response.appointment.AppointmentData

object AppointmentsModel {
    var appointments: List<AppointmentData> = emptyList()

    fun clear(){
        appointments = emptyList()
    }
}