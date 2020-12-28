package org.dev_alex.mojo_qa.mojo.models.response.appointment


import com.google.gson.annotations.SerializedName

data class Range(
    val accounts: List<Any>,
    val emails: List<Any>,
    val from: Double,
    val id: Int,
    val msg: String,
    val to: Double,
    val type: String,
    val value: String
)