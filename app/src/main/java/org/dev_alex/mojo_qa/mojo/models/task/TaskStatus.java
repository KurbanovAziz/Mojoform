package org.dev_alex.mojo_qa.mojo.models.task;

import java.util.Objects;

public class TaskStatus {

    private String id = null;
    private String name = null;


    public TaskStatus(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TaskStatus that = (TaskStatus) o;
        return Objects.equals(id, that.id) && Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }

    @Override
    public String toString() {
        return "TaskStatus{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
