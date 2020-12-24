package org.dev_alex.mojo_qa.mojo

import org.dev_alex.mojo_qa.mojo.CreateTaskModel.TaskType.*
import org.dev_alex.mojo_qa.mojo.models.File
import org.dev_alex.mojo_qa.mojo.models.OrgUser
import org.dev_alex.mojo_qa.mojo.models.response.OrgUsersResponse
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class CreateTaskModel private constructor() {
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
    var selectedPeriod: TaskPeriod? = null

    // Notify Ranges
    var notifyRanges: MutableList<NotifyRange> = ArrayList()

    // Users
    var allUsers: MutableList<OrgUser> = ArrayList()
    var selectedUsers: MutableList<OrgUser> = ArrayList()

    fun saveUsers(response: OrgUsersResponse) {
        val totalUsers = ArrayList(response.users)
        response.groups.forEach { group ->
            group.users.forEach {
                if (!totalUsers.contains(it)) {
                    totalUsers.add(it)
                }
            }
        }
        allUsers = totalUsers
    }

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

    fun isValid(): Boolean {
        if (taskName == null || taskName?.isBlank() == true) return false

        return when (taskType) {
            PERIODICAL -> {
                periodicalTaskHour != null && periodicalTaskMinutes != null && selectedPeriod != null
            }
            ONE_SHOT -> {
                startOneShotDate ?: return false
                endOneShotDate?.after(startOneShotDate) == true
            }
            PRIVATE_POLL, OPEN_POLL -> {
                //endOpenPollDate?.after(Date()) == true
                true
            }
            CONSTANT -> true
            null -> false
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

        allUsers = ArrayList()
        selectedUsers = ArrayList()
        notifyRanges = ArrayList()
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
        var selectedUsersMap: MutableMap<String, OrgUser> = HashMap()
        val selectedUsersList: List<OrgUser>
            get() = selectedUsersMap.values.toList()

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