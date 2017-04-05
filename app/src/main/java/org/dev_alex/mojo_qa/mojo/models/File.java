package org.dev_alex.mojo_qa.mojo.models;

import android.support.v4.util.Pair;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;

public class File {
    public String name;

    public Boolean isFile;
    public Boolean isFolder;
    public Boolean isLocked;

    public Date modifiedAt;
    public Date createdAt;

    public String nodeType;
    public String id;
    public String parentId;

    public SimpleUser createdByUser;
    public SimpleUser modifiedByUser;

    public Content content;

    public Pair<String, JSONObject> properties;
    public ArrayList<String> aspectNames;
}
