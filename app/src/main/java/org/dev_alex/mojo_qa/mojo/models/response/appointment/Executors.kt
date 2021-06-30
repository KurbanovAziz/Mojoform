package org.dev_alex.mojo_qa.mojo.models.response.appointment


import org.dev_alex.mojo_qa.mojo.models.OrgUser
import org.dev_alex.mojo_qa.mojo.models.OrgUserGroup

data class Executors(
    val accounts: List<OrgUser>,
    val groups: List<OrgUserGroup>
)