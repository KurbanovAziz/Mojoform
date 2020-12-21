package org.dev_alex.mojo_qa.mojo.models.response

import org.dev_alex.mojo_qa.mojo.models.OrgUser
import org.dev_alex.mojo_qa.mojo.models.OrgUserGroup

class OrgUsersResponse(
        val groups: List<OrgUserGroup>,
        val users: List<OrgUser>
)