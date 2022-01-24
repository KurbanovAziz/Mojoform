package org.dev_alex.mojo_qa.mojo.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;
import java.util.ArrayList;
import java.io.Serializable;
import java.io.StringReader;
import java.util.ArrayList;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public class Indicator implements Serializable {
        public long id;
        public double max;
        public double min;
        public double prc;
        public long timestamp;
        public long userID;
        public double val;
        public String name;

    }

