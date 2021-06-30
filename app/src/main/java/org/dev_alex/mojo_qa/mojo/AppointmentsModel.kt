package org.dev_alex.mojo_qa.mojo

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers
import org.dev_alex.mojo_qa.mojo.models.response.appointment.AppointmentData
import org.dev_alex.mojo_qa.mojo.services.RequestService

object AppointmentsModel {
    var appointments: List<AppointmentData> = emptyList()

    fun clear() {
        appointments = emptyList()
    }

    fun selfUpdate() {
        loadAppointments()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                appointments = it.orEmpty()
            }, {
                it.printStackTrace()
            })
    }

    fun loadAppointmentDetails(appointmentId: Long): Observable<AppointmentData> {
        return Observable.create<AppointmentData> {
            val url = "/api/tasks/ref/$appointmentId"
            val response = RequestService.createGetRequest(url)

            if (response.code == 200) {
                val responseJson = response.body?.string() ?: "{}"
                val responseData: AppointmentData = Gson().fromJson(responseJson, AppointmentData::class.java)
                it.onNext(responseData)
                it.onComplete()
            } else {
                it.onError(Exception("code = ${response.code}"))
            }
        }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
    }

    fun loadAppointments(): Observable<List<AppointmentData>> {
        return Observable.create<List<AppointmentData>> {
            val url = "/api/tasks/refs/"
            val response = RequestService.createGetRequest(url)

            if (response.code == 200) {
                val responseJson = response.body?.string() ?: "[]"
                val itemType = object : TypeToken<List<AppointmentData>>() {}.type
                val responseData: List<AppointmentData> = Gson().fromJson(responseJson, itemType)
                it.onNext(responseData)
                it.onComplete()
            } else {
                it.onError(Exception("code = ${response.code}"))
            }
        }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
    }
}