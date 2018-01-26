package org.dev_alex.mojo_qa.mojo.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Panel implements Serializable {
    public long id;
    public String name;
    public String type;
    public String function;

    public TimeSeries current;
    public TimeSeries complete;
    public String config;

    public static Panel getSeparatorPanel() {
        Panel panel = new Panel();
        panel.name = null;
        panel.type = "separator";
        panel.id = -1;

        return panel;
    }

    public boolean isSeparator() {
        return name == null && type.equals("separator") && id == -1;
    }

    public static class TimeSeries {
        public Value day;
        public Value week;
        public Value month;
        public Value year;
    }
}
