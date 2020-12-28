package org.dev_alex.mojo_qa.mojo.models.response.appointment


import com.google.gson.annotations.SerializedName

data class CreateAppointmentResponse(
    val config: Config,
    @SerializedName("create_date")
    val createDate: Int,
    @SerializedName("execution_period")
    val executionPeriod: Int,
    val executors: Executors,
    @SerializedName("expire_notification")
    val expireNotification: Boolean,
    val id: Int,
    val links: List<Link>,
    @SerializedName("modify_date")
    val modifyDate: Int,
    @SerializedName("modify_user")
    val modifyUser: ModifyUser,
    val name: String,
    val ranges: List<Range>,
    @SerializedName("template_node")
    val templateNode: String,
    val type: String
)