package org.dev_alex.mojo_qa.mojo.models;

public class Task {
    public long id;
    public long document_id;
    public Ref ref;
    public boolean suspended;
    public Long expire_time;
    public Long start_time;
    public Long complete_time;

    public static class Ref {
        public long id;
        public String name;
        public String description;
        public String type;
    }

    public void fixTime() {
        if (expire_time != null)
            expire_time *= 1000;

        if (start_time != null)
            start_time *= 1000;

        if (complete_time != null)
            complete_time *= 1000;
    }
}
