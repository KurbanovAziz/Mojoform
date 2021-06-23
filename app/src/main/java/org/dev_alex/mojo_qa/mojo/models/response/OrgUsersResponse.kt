package org.dev_alex.mojo_qa.mojo.models.response

import com.google.gson.annotations.SerializedName
import org.dev_alex.mojo_qa.mojo.models.OrgUser
import org.dev_alex.mojo_qa.mojo.models.OrgUserGroup

class OrgUsersResponse(
    val available: OrgUsersAvailable?,
    val groups: List<OrgUserGroup>?,
    @SerializedName("list")
    val users: List<OrgUser>
)