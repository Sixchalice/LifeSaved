package com.example.lifesaved.persistence;

import com.example.lifesaved.UI.Folders.Folder;

import java.util.ArrayList;

public class FolderDTO {
    String name;
    ArrayList<String> userIds = new ArrayList<>();
    int amntOfImages;

    public FolderDTO(Folder f1) {
        this.name = f1.getName();
        this.userIds = f1.getUserIds();
        amntOfImages = f1.getAmntOfImages();
    }

    public FolderDTO() {}

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public ArrayList<String> getUserIds() { return userIds; }
    public void setUserIds(ArrayList<String> uid) { this.userIds = uid; }

    public int getAmntOfImages() { return amntOfImages; }
    public void setAmntOfImages(int amntOfImages) { this.amntOfImages = amntOfImages; }


    public Folder toFolder() {
        Folder f1 = new Folder();
        f1.setName(this.name);
        f1.setUserIds(this.userIds);
        f1.setAmntOfImages(this.amntOfImages);
        return f1;
    }
}
