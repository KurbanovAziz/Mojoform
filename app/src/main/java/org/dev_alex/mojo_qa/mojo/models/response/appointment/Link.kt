package org.dev_alex.mojo_qa.mojo.models.response.appointment


import com.google.gson.annotations.SerializedName

data class Link(
    @SerializedName("execution_limit")
    val executionLimit: Int,
    @SerializedName("expire_date")
    val expireDate: Int,
    val id: Int,
    val link: String,
    val name: String,
    val uuid: String
)