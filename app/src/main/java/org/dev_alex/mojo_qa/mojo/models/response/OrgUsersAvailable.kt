package org.dev_alex.mojo_qa.mojo.models.response

import org.dev_alex.mojo_qa.mojo.models.Org
import org.dev_alex.mojo_qa.mojo.models.OrgUserGroup

class OrgUsersAvailable(
    val organisation: List<Org>,
    val group: List<OrgUserGroup>?
)