package com.example.lifesaved.models;

import android.net.Uri;
import android.util.Log;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class Image {

    Uri imgUri;
    int id;
    public Image(Uri uri) {
        this.imgUri = uri;
        this.id = -1;
    }
    public Image(String myUrlStr){
        Uri uri;
        URL url;
        try {
            url = new URL(myUrlStr);
            uri = Uri.parse( url.toURI().toString() );
            this.imgUri = uri;
        } catch (MalformedURLException | URISyntaxException e1) {
            e1.printStackTrace();
            Log.e("Image", "Error in Image constructor");
        }
    }
    public Image() {
        this.imgUri = null;
        this.id = -1;
    }

    public void setImgUri(Uri uri) {
        this.imgUri = uri;
    }
    public Uri getImgUri() {
        return this.imgUri;
    }


    public static void toUriList(List<Image> imageList, List<Uri> uriList){
        for (Image image : imageList) {
            uriList.add(image.getImgUri());
        }
    }

    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }

}
