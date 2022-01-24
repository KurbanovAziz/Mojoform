package org.dev_alex.mojo_qa.mojo.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import org.dev_alex.mojo_qa.mojo.models.response.Comment;

import java.util.ArrayList;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Value {
    public long from;
    public long to;
    public long id;
    public ArrayList<Comment> comments;

    public double max;
    public double min;
    public double prc;
    public double val;
}