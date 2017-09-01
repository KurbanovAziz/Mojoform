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

    public User(String firstName, String lastName, String token, String username, boolean has_avatar) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.token = token;
        this.username = username;
        this.has_avatar = has_avatar;
    }

    public User(User user) {
        this.firstName = user.firstName;
        this.lastName = user.lastName;
        this.token = user.token;
        this.username = user.username;
        this.has_avatar = user.has_avatar;
    }

    public User() {
    }
}
