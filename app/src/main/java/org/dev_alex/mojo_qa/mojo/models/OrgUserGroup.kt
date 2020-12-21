package org.dev_alex.mojo_qa.mojo.models

data class OrgUserGroup(
        val users: List<OrgUser>,
        val description: String,
        val id: String,
        val name: String
)