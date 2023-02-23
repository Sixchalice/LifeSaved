package com.example.lifesaved.UI.Viewing;

import android.net.Uri;

public class Image {

    Uri uri;

    public Image(Uri uri) {
        this.uri = uri;
    }

    public Image() {
        this.uri = null;
    }

    public void setImgUri(Uri uri) {
        this.uri = uri;
    }

    public Uri getImgUri() {
        return this.uri;
    }

}
