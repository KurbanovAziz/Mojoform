package org.dev_alex.mojo_qa.mojo

import org.dev_alex.mojo_qa.mojo.CreateTaskModel.TaskType.*
import org.dev_alex.mojo_qa.mojo.models.File
import org.dev_alex.mojo_qa.mojo.models.OrgUser
import org.dev_alex.mojo_qa.mojo.models.OrgUserGroup
import org.dev_alex.mojo_qa.mojo.models.response.OrgUsersResponse
import org.dev_alex.mojo_qa.mojo.models.response.appointment.AppointmentData
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class CreateTaskModel private constructor() {
    var createAppointmentResponse: AppointmentData? = null

    var orgId: String? = null
    var file: File? = null
    var taskName: String? = null
    var taskType: TaskType? = null

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
    var periodicalTimes: List<String>? = null
    var selectedPeriod: TaskPeriod? = null

    // Notify Ranges
    var notifyRanges: MutableList<NotifyRange> = ArrayList()

    // Users
    var selectedUsers: MutableList<OrgUser> = ArrayList()
    var selectedGroups: MutableList<OrgUserGroup> = ArrayList()

    enum class TaskType(val nameRes: Int) {
        CONSTANT(R.string.task_type_constant),
        PERIODICAL(R.string.task_type_periodical),
        ONE_SHOT(R.string.task_type_oneshot),
        PRIVATE_POLL(R.string.task_type_private_poll),
        OPEN_POLL(R.string.task_type_open_poll);
    }

    enum class NotifyRangeType(val nameRes: Int) {
        IN_RANGE(R.string.task_notify_in_range),
        OUT_OF_RANGE(R.string.task_notify_out_of_range)
    }

    fun isValid(): Int? {
        if (taskName == null || taskName?.isBlank() == true) return R.string.not_all_fields_filled
        if (AppointmentsModel.appointments.find { it.name == taskName } != null) return R.string.this_name_already_used

        return when (taskType) {
            PERIODICAL -> {
                if (periodicalTaskHour != null && periodicalTaskMinutes != null && selectedPeriod != null) {
                    null
                } else {
                    R.string.not_all_fields_filled
                }
            }
            ONE_SHOT -> {
                startOneShotDate ?: return R.string.not_all_fields_filled
                if (endOneShotDate?.after(startOneShotDate) == true) {
                    null
                } else {
                    R.string.not_all_fields_filled
                }
            }
            PRIVATE_POLL, OPEN_POLL -> {
                //endOpenPollDate?.after(Date()) == true
                null
            }
            CONSTANT -> null
            null -> R.string.not_all_fields_filled
        }
    }

    fun clear() {
        orgId = null
        file = null
        taskName = null
        taskType = null

        startOneShotDate = null
        endOneShotDate = null

        endOpenPollDate = null
        pollPersonsCount = null

        periodicalTaskHour = null
        periodicalTaskMinutes = null
        selectedPeriod = null
        periodicalTimes = null

        selectedUsers = ArrayList()
        selectedGroups = ArrayList()
        notifyRanges = ArrayList()

        createAppointmentResponse = null
    }

    sealed class TaskPeriod {
        object Daily : TaskPeriod()
        data class Weekly(var days: List<Int>) : TaskPeriod()
        data class Monthly(var days: List<Int>) : TaskPeriod()
    }

    class NotifyRange {
        var type: NotifyRangeType = NotifyRangeType.IN_RANGE
        var isPercent: Boolean = false

        var from: Int = 0
        var to: Int = 0
        var message: String = ""
        var selectedUsersList: MutableList<OrgUser> = ArrayList()

        var emailsMap: MutableMap<String, String> = HashMap()
        val emailsList: List<String>
            get() = emailsMap.values.toList()
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