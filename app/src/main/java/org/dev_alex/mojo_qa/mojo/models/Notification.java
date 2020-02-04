package org.dev_alex.mojo_qa.mojo.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Notification {
    public long id;
    public Task task;
    public String type;
    public boolean is_readed;
    public Long create_date;

    public void fixTime() {
        if (create_date != null) {
            create_date *= 1000;
        }
    }
}
