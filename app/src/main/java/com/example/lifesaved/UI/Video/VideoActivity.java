package com.example.lifesaved.UI.Video;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

import com.example.lifesaved.R;
import com.example.lifesaved.models.Folder;
import com.example.lifesaved.models.Image;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

public class VideoActivity extends AppCompatActivity {


    private VideoPresenter presenter;

    private Folder process;
    private VideoView videoView;
    private String fileLocation;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);

        Gson gson = new Gson();
        Intent intent = getIntent();
        String jsonFolder = intent.getStringExtra("myjsonFolder");
        process = gson.fromJson(jsonFolder, Folder.class);

        fileLocation = intent.getStringExtra("fileLocation");

        String fname = process.getName();
        Log.e("VideoActivity", "onCreate: " + fname);
        TextView viewingTitle = findViewById(R.id.textView_videoactivity_title);
        viewingTitle.setText("" + fname);

        presenter = new VideoPresenter(this);

        videoView = findViewById(R.id.videoView2);

        showVideo();

        ImageButton backbutton = findViewById(R.id.imageButton_videoactivity_back);
        backbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        FloatingActionButton downloadButton = findViewById(R.id.floatingActionButton_videoactivity_download);
        downloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.SaveVideoToGallery(fileLocation);
            }
        });
    }

    public void showVideo(){
        Log.e("VideoActivity", "showVideo: " );
        MediaController mediaController = new MediaController(this);
        mediaController.setAnchorView(videoView);
        videoView.setMediaController(mediaController);
        File file1 = getExternalFilesDir(null);
        String path = file1.getAbsolutePath() + "/lifesavedVideos";
        File file = new File(path+"/" + fileLocation + ".mp4");

        if(file.exists()){

            Log.e("VideoActivity", "file exists: showVideo: " + file.getAbsolutePath());
            Log.e("VideoActivity", "file exists: showVideo: " + file.getAbsolutePath());

            Log.e("VideoActivity", "showVideo: " + file.getAbsolutePath());
            videoView.setVideoURI(Uri.parse(file.getAbsolutePath()));
            videoView.start();
        }
        else{
            Log.e("VideoActivity", "file does not exist: showVideo: " + file.getAbsolutePath());
        }
    }

    public Folder getFolder() {
        return process;
    }
}