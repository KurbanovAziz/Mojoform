package org.dev_alex.mojo_qa.mojo.models;

import java.util.ArrayList;
import java.util.Date;

public class Task {
    public Long id;
    public Long durationInMillis;
    public Long workTimeInMillis;
    public String url;
    public String owner;
    public String assignee;
    public String delegationState;
    public String deleteReason;
    public String name;
    public String description;
    public Date createTime;
    public Date startTime;
    public Date endTime;
    public Date dueDate;
    public Date claimTime;
    public Integer priority;
    public Boolean suspended;
    public String taskDefinitionKey;
    public String tenantId;
    public String category;
    public String formKey;
    public String parentTaskId;
    public String parentTaskUrl;
    public String executionId;
    public String executionUrl;
    public String processInstanceId;
    public String processInstanceUrl;
    public String processDefinitionId;
    public String processDefinitionUrl;
    public ArrayList<Variable> variables;
}
