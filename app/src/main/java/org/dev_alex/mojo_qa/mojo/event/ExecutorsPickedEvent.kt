package org.dev_alex.mojo_qa.mojo.event

import org.dev_alex.mojo_qa.mojo.models.OrgUser
import org.dev_alex.mojo_qa.mojo.models.OrgUserGroup

class ExecutorsPickedEvent(val users: List<OrgUser>,val groups: List<OrgUserGroup>)