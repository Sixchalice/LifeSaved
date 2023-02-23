package com.example.lifesaved.UI.Folders;

import android.net.Uri;

import com.google.firebase.auth.FirebaseAuth;

import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;

public class Folder {
    String name;
    ArrayList<String> userIds = new ArrayList<>();
    String fid;
    Uri icon;
    int amntOfImages;
    public Folder(String name, Uri icon) {
        this.name = name;
        this.icon = icon;
        this.amntOfImages = 0;
    }

    public Folder(String name) {
        this.name = name;
        this.amntOfImages = 0;
    }

    public Folder(){}
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public ArrayList<String> getUserIds() { return userIds; }
    public void setUserIds(ArrayList<String> uid) {
        this.userIds = uid;
    }

    public void setFid(String fid) { this.fid = fid; }
    public String getFid() { return fid; }

    public Uri getIcon() { return icon; }
    public void setIcon(Uri icon) { this.icon = icon; }

    public void setAmntOfImages(int amntOfImages) { this.amntOfImages = amntOfImages; }
    public int getAmntOfImages() { return amntOfImages; }
}
