package org.dev_alex.mojo_qa.mojo.models


import com.google.gson.annotations.SerializedName

data class OrgUser(
        val config: String,
        val description: String,
        val email: String,
        val firstName: String,
        val fullname: String,
        @SerializedName("has_avatar")
        val hasAvatar: Boolean,
        val id: Int,
        @SerializedName("is_deleted")
        val isDeleted: Boolean,
        @SerializedName("is_orgowner")
        val isOrgowner: Boolean,
        val langcode: String,
        val lastName: String,
        @SerializedName("push_disabled")
        val pushDisabled: Boolean,
        val username: String
) {
    override fun toString(): String {
        return firstName.takeIf { it.isNotBlank() } ?: fullname
    }
}