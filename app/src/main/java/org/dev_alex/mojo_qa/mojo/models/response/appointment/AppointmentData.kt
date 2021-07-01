package org.dev_alex.mojo_qa.mojo.models.response.appointment


import com.google.gson.annotations.SerializedName

data class AppointmentData(
        val id: Int,
        val name: String,
        val type: String,

        val config: Config?,
        @SerializedName("create_date")
        var createDate: Long?,
        @SerializedName("execution_period")
        val executionPeriod: Int?,
        val executors: Executors?,
        @SerializedName("expire_notification")
        val expireNotification: Boolean?,
        val links: List<Link>?,
        @SerializedName("modify_date")
        var modifyDate: Long?,
        @SerializedName("modify_user")
        val modifyUser: ModifyUser?,
        val ranges: List<Range>?,
        @SerializedName("template_node")
        val templateNode: String?
) {
    var templateName: String? = null

    fun fixDate() {
        createDate = if (createDate != null) {
            (createDate ?: 0L) * 1000
        } else {
            null
        }

        modifyDate = if (modifyDate != null) {
            (modifyDate ?: 0L) * 1000
        } else {
            null
        }
    }
}