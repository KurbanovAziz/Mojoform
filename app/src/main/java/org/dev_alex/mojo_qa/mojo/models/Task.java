package org.dev_alex.mojo_qa.mojo.models;

import java.util.ArrayList;
import java.util.Date;

public class Task {
    public Long id;
    public String url;
    public String owner;
    public String assignee;
    public String delegationState;
    public String name;
    public String description;
    public Date createTime;
    public Date dueDate;
    public Integer priority;
    public Boolean suspended;
    public String taskDefinitionKey;
    public String tenantId;
    public String category;
    public String formKey;
    public Long parentTaskId;
    public String parentTaskUrl;
    public Long executionId;
    public String executionUrl;
    public Long processInstanceId;
    public String processInstanceUrl;
    public String processDefinitionId;
    public String processDefinitionUrl;
    public ArrayList<Variable> variables;
}
