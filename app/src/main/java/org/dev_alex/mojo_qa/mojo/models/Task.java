package org.dev_alex.mojo_qa.mojo.models;

import java.util.ArrayList;
import java.util.Date;

public class Task {
    public String id;
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

    @Override
    public String toString() {
        return "Task{" +
                "id='" + id + '\'' +
                ", durationInMillis=" + durationInMillis +
                ", workTimeInMillis=" + workTimeInMillis +
                ", url='" + url + '\'' +
                ", owner='" + owner + '\'' +
                ", assignee='" + assignee + '\'' +
                ", delegationState='" + delegationState + '\'' +
                ", deleteReason='" + deleteReason + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", createTime=" + createTime +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", dueDate=" + dueDate +
                ", claimTime=" + claimTime +
                ", priority=" + priority +
                ", suspended=" + suspended +
                ", taskDefinitionKey='" + taskDefinitionKey + '\'' +
                ", tenantId='" + tenantId + '\'' +
                ", category='" + category + '\'' +
                ", formKey='" + formKey + '\'' +
                ", parentTaskId='" + parentTaskId + '\'' +
                ", parentTaskUrl='" + parentTaskUrl + '\'' +
                ", executionId='" + executionId + '\'' +
                ", executionUrl='" + executionUrl + '\'' +
                ", processInstanceId='" + processInstanceId + '\'' +
                ", processInstanceUrl='" + processInstanceUrl + '\'' +
                ", processDefinitionId='" + processDefinitionId + '\'' +
                ", processDefinitionUrl='" + processDefinitionUrl + '\'' +
                ", variables=" + variables +
                '}';
    }
}
