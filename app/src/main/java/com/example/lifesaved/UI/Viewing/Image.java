package com.example.lifesaved.UI.Viewing;

import android.net.Uri;

public class Image {

    Uri imgUri;

    public Image(Uri uri) {
        this.imgUri = uri;
    }

    public Image() {
        this.imgUri = null;
    }

    public void setImgUri(Uri uri) {
        this.imgUri = uri;
    }

    public Uri getImgUri() {
        return this.imgUri;
    }

}
