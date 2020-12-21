package org.dev_alex.mojo_qa.mojo.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.Date;

@JsonIgnoreProperties(ignoreUnknown = true)
public class File {
    public String name;

    public Boolean isFile;
    public Boolean isFolder;
    public Boolean isLocked;

    public Date modifiedAt;
    public Date createdAt;

    public String nodeType;
    public String id;
    public String orgId;
    public String parentId;

    public SimpleUser createdByUser;
    public SimpleUser modifiedByUser;

    public Content content;

    //public JSONObject properties;
    public ArrayList<String> aspectNames;

/*    public void setProperties(String string) {
        try {
            properties = new JSONObject(string);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void setProperties(JSONObject jsonObject) {
        try {
            properties = new JSONObject(jsonObject.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }*/
}
