package org.dev_alex.mojo_qa.mojo.models;

import java.util.ArrayList;

public class FileSystemStackEntry {
    public ArrayList<File> folders;
    public ArrayList<File> files;
    public String parentName;

    public FileSystemStackEntry(ArrayList<File> folders, ArrayList<File> files, String parentName) {
        this.folders = folders;
        this.files = files;
        this.parentName = parentName;
    }
}
