package org.dev_alex.mojo_qa.mojo;


import org.dev_alex.mojo_qa.mojo.services.LoginHistoryService;

public class Data {
    public static final String taskAuthPass = "p234235dfgdeg";
    public static String currentTaskId = "";

    public static String getTaskAuthLogin() {
        return LoginHistoryService.getCurrentUser().username;
    }
}
