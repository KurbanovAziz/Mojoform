package org.dev_alex.mojo_qa.mojo.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class User implements Serializable {
    public String firstName;
    public String lastName;
    public String token;
    public String username;
    public boolean has_avatar;
    public Boolean is_orgowner = null;
    public Boolean is_manager = null;

    public User() {
    }
}
