package org.dev_alex.mojo_qa.mojo.models.file


import com.google.gson.annotations.SerializedName

data class Properties(
    @SerializedName("mojo:id")
    val mojoId: Int,
    @SerializedName("mojo:is_copy")
    val mojoIsCopy: Boolean
)