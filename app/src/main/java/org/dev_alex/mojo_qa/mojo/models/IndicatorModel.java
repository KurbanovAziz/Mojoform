package org.dev_alex.mojo_qa.mojo.models;


import java.util.ArrayList;

public class IndicatorModel {
    public ArrayList<Range> ranges;

    public static class Range {
        public String id;
        public int from;
        public int to;
        public String color;
        public String name;

        public Range() {
        }
    }

    public IndicatorModel() {
        ranges = new ArrayList<>();
    }
}
