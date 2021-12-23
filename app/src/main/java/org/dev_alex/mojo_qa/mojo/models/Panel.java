package org.dev_alex.mojo_qa.mojo.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;
import java.io.StringReader;
import java.util.ArrayList;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Panel implements Serializable {
    public long id;
    public String name;
    public String type;
    public String function;

    public TimeSeries current;
    public TimeSeries complete;

    public String config;
    public String color;
    public String prc;
    public String val;
    public long from;
    public long to;

    public ArrayList<PanelTag> tags;

    public static Panel getSeparatorPanel() {
        Panel panel = new Panel();
        panel.name = null;
        panel.type = "separator";
        panel.id = -1;

        return panel;
    }

    public boolean isSeparator() {
        return name == null && "separator".equals(type) && id == -1;
    }

    public static class TimeSeries implements Serializable {
        public Value day;
        public Value week;
        public Value month;
        public Value year;
    }

    public void fixDate() {




        if (current != null) {
            if (current.day != null) {
                current.day.from *= 1000;
                current.day.to *= 1000;
            }
            if (current.week != null) {
                current.week.from *= 1000;
                current.week.to *= 1000;
            }
            if (current.month != null) {
                current.month.from *= 1000;
                current.month.to *= 1000;
            }
            if (current.year != null) {
                current.year.from *= 1000;
                current.year.to *= 1000;
            }
        }

        if (complete != null) {
            if (complete.day != null) {
                complete.day.from *= 1000;
                complete.day.to *= 1000;
            }
            if (complete.week != null) {
                complete.week.from *= 1000;
                complete.week.to *= 1000;
            }
            if (complete.month != null) {
                complete.month.from *= 1000;
                complete.month.to *= 1000;
            }
            if (complete.year != null) {
                complete.year.from *= 1000;
                complete.year.to *= 1000;
            }
        }
    }
}
