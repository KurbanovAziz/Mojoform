package org.dev_alex.mojo_qa.mojo.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Value {
    public long from;
    public long to;
    public long id;

    public double max;
    public double min;
    public double prc;
    public double val;
}