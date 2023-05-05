package com.example.lifesaved.UI.Video;


import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import com.example.lifesaved.models.Image;
import com.example.lifesaved.persistence.Repository;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

public class VideoPresenter{

    private VideoActivity view;
    private static int count = 0;

    public VideoPresenter(VideoActivity view) {
        this.view = view;
    }

    public void SaveVideoToGallery(String fileLocation) {
        File file1 = view.getExternalFilesDir(null);
        String path = file1.getAbsolutePath() + "/lifesavedVideos";
        File file = new File(path+"/" + fileLocation + ".mp4");

        if(file.exists()){
            count++;
            File newFile = new File(path+"/" + fileLocation + "" + count + ".mp4");
            try {
                cloneFile(file, newFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
            Log.e("VideoActivity", "SaveVideoToGallery: " + newFile.getAbsolutePath());
            galleryAddVid(newFile);
        }
    }

    public void cloneFile(File src, File dst) throws IOException {
        try (InputStream in = new FileInputStream(src)) {
            try (OutputStream out = new FileOutputStream(dst)) {
                // Transfer bytes from in to out
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
            }
        }
    }
    private void galleryAddVid(File file) {

        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri contentUri = addVideo(file);
        mediaScanIntent.setData(contentUri);
        view.sendBroadcast(mediaScanIntent);
    }

    public Uri addVideo(File videoFile) {
        ContentValues values = new ContentValues(3);
        values.put(MediaStore.Video.Media.TITLE, "My video title");
        values.put(MediaStore.Video.Media.MIME_TYPE, "video/mp4");
        values.put(MediaStore.Video.Media.DATA, videoFile.getAbsolutePath());
        return view.getContentResolver().insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values);
    }
}
