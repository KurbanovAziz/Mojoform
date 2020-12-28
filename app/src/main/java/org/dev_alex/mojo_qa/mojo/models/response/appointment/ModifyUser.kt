package org.dev_alex.mojo_qa.mojo.models.response.appointment


import com.google.gson.annotations.SerializedName

data class ModifyUser(
    val fullname: String,
    @SerializedName("has_avatar")
    val hasAvatar: Boolean,
    val id: Int,
    @SerializedName("is_deleted")
    val isDeleted: Boolean,
    val username: String
)