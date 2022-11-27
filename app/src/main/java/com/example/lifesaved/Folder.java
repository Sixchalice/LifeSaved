package com.example.lifesaved;

import android.graphics.Bitmap;
import android.widget.ImageView;

public class Folder {
   String name;
   String ImgUrl;

    public Folder(String name, String imgUrl) {
        this.name = name;
        this.ImgUrl = imgUrl;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImgUrl() {
        return ImgUrl;
    }

    public void setImgUrl(String imgUrl) {
        ImgUrl = imgUrl;
    }
}
