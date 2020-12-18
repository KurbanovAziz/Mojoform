package org.dev_alex.mojo_qa.mojo

import org.dev_alex.mojo_qa.mojo.models.File
import java.util.*

class CreateTaskModel private constructor() {
    var file: File? = null
    var name: String? = null
    var type: TaskType? = null

    // OneShot
    var startOneShotDate: Date? = null
    var endOneShotDate: Date? = null

    fun updateStartOneShotMinutePart(minute: Int) {
        val calendar = Calendar.getInstance()
        calendar.time = startOneShotDate ?: Date()
        calendar.set(Calendar.MINUTE, minute)
        startOneShotDate = calendar.time
    }

    fun updateEndOneShotMinutePart(minute: Int) {
        val calendar = Calendar.getInstance()
        calendar.time = endOneShotDate ?: Date()
        calendar.set(Calendar.MINUTE, minute)
        endOneShotDate = calendar.time
    }

    fun updateStartOneShotHourPart(hour: Int) {
        val calendar = Calendar.getInstance()
        calendar.time = startOneShotDate ?: Date()
        calendar.set(Calendar.HOUR, hour)
        startOneShotDate = calendar.time
    }

    fun updateEndOneShotHourPart(hour: Int) {
        val calendar = Calendar.getInstance()
        calendar.time = endOneShotDate ?: Date()
        calendar.set(Calendar.HOUR, hour)
        endOneShotDate = calendar.time
    }

    fun updateStartOneShotDatePart(date: Date) {
        val dateCalendar = Calendar.getInstance().apply { time = date }

        val calendar = Calendar.getInstance()
        calendar.time = startOneShotDate ?: Date()
        calendar.set(Calendar.DAY_OF_YEAR, dateCalendar.get(Calendar.DAY_OF_YEAR))
        startOneShotDate = calendar.time
    }

    fun updateEndOneShotDatePart(date: Date) {
        val dateCalendar = Calendar.getInstance().apply { time = date }

        val calendar = Calendar.getInstance()
        calendar.time = endOneShotDate ?: Date()
        calendar.set(Calendar.DAY_OF_YEAR, dateCalendar.get(Calendar.DAY_OF_YEAR))
        endOneShotDate = calendar.time
    }

    // OpenPoll
    var endOpenPollDate: Date? = null
    var pollPersonsCount: Int? = null

    // Periodical
    var periodicalTaskHour: Int? = null
    var periodicalTaskMinutes: Int? = null

    var selectedPeriod: TaskPeriod? = null

    enum class TaskType(val nameRes: Int) {
        CONSTANT(R.string.task_type_constant),
        PERIODICAL(R.string.task_type_periodical),
        ONE_SHOT(R.string.task_type_oneshot),
        OPEN_LINK(R.string.task_type_by_link),
        OPEN_POLL(R.string.task_type_open_poll);
    }

    sealed class TaskPeriod {
        object Daily : TaskPeriod()
        data class Weekly(var days: List<Int>) : TaskPeriod()
        data class Monthly(var days: List<Int>) : TaskPeriod()
    }

    companion object {
        var instance: CreateTaskModel? = null
            get() {
                if (field == null) {
                    field = CreateTaskModel()
                }
                return field
            }
            private set
    }
}