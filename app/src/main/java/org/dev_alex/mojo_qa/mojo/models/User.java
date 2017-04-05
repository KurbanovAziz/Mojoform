package org.dev_alex.mojo_qa.mojo.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class User {
    public String firstName;
    public String lastName;
    public String token;
    public String userName;
    public boolean has_avatar;
}
