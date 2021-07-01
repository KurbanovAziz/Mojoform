package org.dev_alex.mojo_qa.mojo.models

import java.io.Serializable

class OrgUserGroup(
        val users: List<OrgUser>?,
        val description: String?,
        val id: Int,
        val name: String
): Serializable