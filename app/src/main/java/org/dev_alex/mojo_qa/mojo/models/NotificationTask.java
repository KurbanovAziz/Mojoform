package org.dev_alex.mojo_qa.mojo.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class NotificationTask {
    public long id;
    public Ref ref;
    public User executor;
    public boolean suspended;
    public Long complete_time;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class User {
        public long id;
        public String fullname;
        public String username;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Ref {
        public long id;
        public String name;
        public String type;
    }

    public void fixTime() {
        if (complete_time != null)
            complete_time *= 1000;
    }
}
