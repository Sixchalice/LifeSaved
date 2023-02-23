package com.example.lifesaved.UI.Viewing;

import static com.example.lifesaved.UI.Folders.HomePageActivity.GALLERY_REQUEST_CODE;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.lifesaved.UI.Folders.Folder;
import com.example.lifesaved.UI.Folders.HomePageActivity;
import com.example.lifesaved.UI.Login.MainActivity;
import com.example.lifesaved.UI.OnItemClickListener;
import com.example.lifesaved.R;
import com.example.lifesaved.persistence.Repository;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.gson.Gson;

import java.util.ArrayList;

public class ViewingActivity extends AppCompatActivity implements OnItemClickListener {

    FirebaseStorage storage = FirebaseStorage.getInstance();

    private Dialog dialog;

    private ViewingPresenter presenter;

    private ImageAdapter imageAdapter;

    private static final String TAG = "FolderActivity";
    private static int countInterval = 0;
    private static boolean continuePlaying = true;
    public ArrayList<Image> imageArrayList = new ArrayList<>();

    private Uri imageUri = null;

    private Folder f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viewing);



        Gson gson = new Gson();
        Folder f1 = gson.fromJson(getIntent().getStringExtra("myjson"), Folder.class);
        //TODO: use gson to recieve name of folder
//        String fname = f.getName();

        TextView viewingTitle = findViewById(R.id.textView_ViewingPage_title);
//        viewingTitle.setText("" + fname);


        RecyclerView recyclerView = findViewById(R.id.recycleritem_viewing_images);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);


        recyclerView.setLayoutManager(layoutManager);

        imageAdapter = new ImageAdapter(imageArrayList);
        imageAdapter.setListener(this);
        recyclerView.setAdapter(imageAdapter);

        presenter = new ViewingPresenter(this);


        //button to open dialog:
        dialog = new Dialog(this);

        Window window = dialog.getWindow();
        WindowManager.LayoutParams wlp = window.getAttributes();


        wlp.gravity = Gravity.BOTTOM;
        window.setAttributes(wlp);

        dialog.setContentView(R.layout.dialog_selectphoto);
        dialog.getWindow().setBackgroundDrawableResource(R.drawable.rounded_dialog);

        FloatingActionButton pickOption = findViewById(R.id.floatingActionButton_viewing_addimage_dialog);
        pickOption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.show();
            }
        });
        //button 1
        ImageView addFromGallery = dialog.findViewById(R.id.imageView_selectFrom_gallery);
        addFromGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setType("*/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(intent, GALLERY_REQUEST_CODE);
            }
        });
        //button 2
        ImageView addFromCamera = dialog.findViewById(R.id.imageView_selectFrom_camera);
        addFromCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(takePicture, 0);
            }
        });


        SeekBar seekBar = findViewById(R.id.seekBar_viewing_setTime);
        int maxValOfSeek = seekBar.getMax();
        seekBar.setProgress(maxValOfSeek / 2);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progressChangedValue = 0;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                progressChangedValue = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // Respond to start of tracking
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                countInterval = progressChangedValue;
            }
        });
        ImageView stopPlaying = findViewById(R.id.imageView_viewing_pause_button);
        stopPlaying.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                continuePlaying = false;
            }
        });
        ImageView startPlaying = findViewById(R.id.imageView_viewing_play_button);
        startPlaying.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.e(TAG, "onlick worked");
                Log.e(TAG, "size: " + countInterval);
                // Create a new CountdownTimer object.
//                CountDownTimer timer = new CountDownTimer((imageArrayList.size() * (countInterval * 1000)), countInterval * 1000) {
                  CountDownTimer timer = new CountDownTimer(20000, 1000) {
                    // This method will be called every second until the timer is finished.
                    int i = 0;

                    public void onTick(long millisUntilFinished) {
                        Log.e(TAG, "ticked");

                        // Update the text view with the current time remaining.
                        ImageView image = findViewById(R.id.imageView_viewing_mainImage);
                        if (i % 2 == 0) {
                            image.setImageResource(R.drawable.folder);

                        } else {
                            image.setImageResource(R.drawable.star);

                        }
                        i++;
                        if (!continuePlaying) {
                            this.cancel();
                            this.onFinish();
                        }
                        //TextView timerTextView = findViewById(R.id.timer_text_view);
                        //timerTextView.setText(String.valueOf(millisUntilFinished / 1000));
                    }

                    // This method will be called when the timer is finished.
                    public void onFinish() {
                        // Update the text view with the time elapsed.
                        continuePlaying = true;
                        //TextView timerTextView = findViewById(R.id.timer_text_);
                    }
                };
                timer.start();
            }
        });

    }
//when know how:
    StorageReference storageRef = storage.getReference();

    StorageReference inFolder = storageRef.child("folders");

    protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);
        switch(requestCode) {
            case 0:
                if(resultCode == RESULT_OK){
                    Uri selectedImage = imageReturnedIntent.getData();
                    imageUri = selectedImage;
                    presenter.add();
                }

                break;
            case GALLERY_REQUEST_CODE: //gallery
                if(resultCode == RESULT_OK){
                    Uri selectedImage = imageReturnedIntent.getData();
                    imageUri = selectedImage;
                    presenter.add();
                }

                break;
        }
        dialog.dismiss();
    }


    @Override
    public void onItemClick(int index) {

        Log.e(TAG, "clicked" + index);

        ImageView image = findViewById(R.id.imageView_viewing_mainImage);
        image.setImageResource(R.drawable.red_star);
    }

    public void setDefaultFields(ArrayList<Image> images) {
        this.imageArrayList.clear();
        this.imageArrayList.addAll(images);

        imageAdapter.notifyDataSetChanged();


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menuitem_logout:
                FirebaseAuth.getInstance().signOut();
                sendToLogin();
                finish();

                return true;
            case R.id.menuitem_home:
                sendToHome();
        }
        return super.onOptionsItemSelected(item);
    }
    public void sendToLogin() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    public void sendToHome() {
        Intent intent = new Intent(this, HomePageActivity.class);
        startActivity(intent);
    }

    public Uri getUri() {
        return imageUri;
    }

    public Folder getFolder() {
        //TODO: use gson to recieve folder
        Folder folder = new Folder();
        return folder;
    }
}