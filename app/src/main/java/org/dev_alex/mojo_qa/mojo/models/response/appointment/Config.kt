package org.dev_alex.mojo_qa.mojo.models.response.appointment


import com.google.gson.annotations.SerializedName

data class Config(
    val closedlinks: Boolean,
    val documentFolder: String
)