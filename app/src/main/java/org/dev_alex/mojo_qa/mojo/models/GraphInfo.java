package org.dev_alex.mojo_qa.mojo.models;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GraphInfo {
    public long from;
    public long to;
    public List<Value> values;



    public void fixDates() {
        from *= 1000;
        to *= 1000;

        for (Value value : values) {
            value.from *= 1000;
            value.to *= 1000;
        }
    }
}
