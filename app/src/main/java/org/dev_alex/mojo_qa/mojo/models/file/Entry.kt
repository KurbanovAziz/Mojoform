package org.dev_alex.mojo_qa.mojo.models.file


import com.google.gson.annotations.SerializedName

data class Entry(
    val aspectNames: List<String>,
    val createdAt: String,
    val createdByUser: CreatedByUser,
    val id: String,
    val isFile: Boolean,
    val isFolder: Boolean,
    val isLocked: Boolean,
    val modifiedAt: String,
    val modifiedByUser: ModifiedByUser,
    val name: String,
    val nodeType: String,
    val properties: Properties
)