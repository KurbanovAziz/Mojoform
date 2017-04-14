package org.dev_alex.mojo_qa.mojo.models;

import java.util.ArrayList;

public class FileSystemStackEntry {
    public ArrayList<File> folders;
    public ArrayList<File> files;
    public String parentName;
    public String id;

    public FileSystemStackEntry(ArrayList<File> folders, ArrayList<File> files, String parentName, String id) {
        this.folders = folders;
        this.files = files;
        this.parentName = parentName;
        this.id = id;
    }
}
