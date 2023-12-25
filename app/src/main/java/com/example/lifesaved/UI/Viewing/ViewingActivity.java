package com.example.lifesaved.UI.Viewing;

import static com.example.lifesaved.UI.Folders.HomePageActivity.GALLERY_REQUEST_CODE;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.widget.VideoView;

import com.example.lifesaved.UI.OnLongClickListener;
import com.example.lifesaved.UI.Video.VideoActivity;
import com.example.lifesaved.models.Folder;
import com.example.lifesaved.UI.Folders.HomePageActivity;
import com.example.lifesaved.UI.Login.MainActivity;
import com.example.lifesaved.UI.OnItemClickListener;
import com.example.lifesaved.R;
import com.example.lifesaved.models.Image;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.storage.FirebaseStorage;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.smarteist.autoimageslider.SliderView;

import org.jcodec.api.android.AndroidSequenceEncoder;
import org.jcodec.common.io.FileChannelWrapper;
import org.jcodec.common.io.NIOUtils;
import org.jcodec.common.model.Rational;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class ViewingActivity extends AppCompatActivity implements OnItemClickListener, AdapterView.OnItemSelectedListener, OnLongClickListener {

    private static final int REQUEST_WRITE_EXTERNAL_STORAGE = 2;


    private Dialog dialog;
    private ViewingPresenter presenter;
    private SliderView sliderView;
    private SliderAdapter sliderAdapter;
    private ImageAdapter imageAdapter;

    private static final String TAG = "ViewingActivity";

    public ArrayList<Image> imageArrayList = new ArrayList<>();

    private ArrayList<Image> sliderDataArrayList = new ArrayList<>();

    private Uri imageUri = null;
    private Handler endOfVideohandler;
    private String videoFileLocation;

    private int selectedTimer = 1;
    private int selectedSpinnerIndex = 0;
    private String[] SpinnerItems = new String[]{"Select interval in seconds", "0.5", "0.75", "1.00", "1.50", "2.00", "2.50", "3.00", "3.50", "4.00"};
    private int[] SpinnerItemsInt = new int[]{0,500, 750, 1000, 1500, 2000, 2500, 3000, 3500, 4000};
    private Folder process;
    private MySpinnerDialog spinnerDialog = new MySpinnerDialog();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viewing);


        endOfVideohandler = new Handler(){

            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);
                showVideo();
            }
        };
        sliderView = findViewById(R.id.slider_viewing);

        sliderAdapter = new SliderAdapter(this, sliderDataArrayList);
        sliderView.setAutoCycleDirection(SliderView.LAYOUT_DIRECTION_LTR);
        sliderView.setSliderAdapter(sliderAdapter);

        sliderView.setScrollTimeInMillis(1500);
        sliderView.setAutoCycle(true);
        sliderView.startAutoCycle();


        Gson gson = new Gson();
        Intent intent = getIntent();
        String json = intent.getStringExtra("myjson");
        Folder f1 = gson.fromJson(json, Folder.class);

        process = f1;
        String fname = f1.getName();
        TextView viewingTitle = findViewById(R.id.textView_ViewingPage_title);
        viewingTitle.setText("" + fname);



        RecyclerView recyclerView = findViewById(R.id.recycleritem_viewing_images);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);


        recyclerView.setLayoutManager(layoutManager);

        imageAdapter = new ImageAdapter(imageArrayList);
        imageAdapter.setListener(this);
        imageAdapter.setLongClickListener(this);
        recyclerView.setAdapter(imageAdapter);

        presenter = new ViewingPresenter(this);

        videoFileLocation = presenter.FileName(f1.getName());

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
                if (ContextCompat.checkSelfPermission(ViewingActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        == PackageManager.PERMISSION_GRANTED) {
                    // Permission is already granted, proceed with the operation.
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(intent, REQUEST_WRITE_EXTERNAL_STORAGE);
                } else {
                    // Permission is not granted, request it.
                    ActivityCompat.requestPermissions(ViewingActivity.this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            REQUEST_WRITE_EXTERNAL_STORAGE);
                }
            }
        });

        ImageButton back = findViewById(R.id.imageButton_viewing_back_button);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ViewingActivity.this, HomePageActivity.class);
                startActivity(intent);
            }
        });

        ToggleButton toggle = findViewById(R.id.toggleButton_viewing_toggle_slideshow);
        toggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (toggle.isChecked()) {
                    sliderView.setScrollTimeInMillis(selectedTimer);
                    sliderView.setAutoCycle(true);
                    sliderView.startAutoCycle();
                } else {
                    sliderView.setAutoCycle(false);
                    sliderView.stopAutoCycle();
                }
            }
        });

        //get the spinner from the xml.
        Spinner dropdown = findViewById(R.id.spinner_viewing_times);
        //create a list of items for the spinner.
        //create an adapter to describe how the items are displayed, adapters are used in several places in android.
        //There are multiple variations of this, but this is the basic variant.
        ArrayAdapter adapter = new ArrayAdapter<String>(this, com.bumptech.glide.R.layout.support_simple_spinner_dropdown_item, SpinnerItems){
            @Override
            public boolean isEnabled(int position) {
                // Disable the first item (default value) so it cannot be selected
                return position != 0;
            }

            @Override
            public boolean areAllItemsEnabled() {
                // Enable all items except the first (default value)
                return false;
            }
        };
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dropdown.setAdapter(adapter);

        dropdown.setOnItemSelectedListener(this);


        Button createVideo = findViewById(R.id.button_viewing_create_video);
        createVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(selectedSpinnerIndex == 0){
                    Toast.makeText(ViewingActivity.this, "Please select a time", Toast.LENGTH_SHORT).show();
                    return;
                }else{
                    FragmentManager fm = getSupportFragmentManager();

                    spinnerDialog.show(fm, "some_tag");
                    presenter.createVideo(v,imageArrayList);
                }

            }
        });
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        //spinner
        Log.e(TAG, "onPositionSelected: " + position);
        Log.e(TAG, "onItemSelected: " + parent.getItemAtPosition(position).toString());
        selectedTimer = SpinnerItemsInt[position];
        selectedSpinnerIndex = position;
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        //spinner
    }

    public boolean onOptionsItemSelected(MenuItem item){
        Intent intent = new Intent(this, HomePageActivity.class);
        startActivity(intent);
        return true;
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);
        switch (requestCode) {
            case REQUEST_WRITE_EXTERNAL_STORAGE: //camera
                if (resultCode == RESULT_OK) {

                    Bitmap bitmap = (Bitmap) imageReturnedIntent.getExtras().get("data");
                    //convert bitmap to uri
                    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
                    String path = MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, "Title", null);
                    Uri selectedImage = Uri.parse(path);

                    imageUri = selectedImage;
                    presenter.add();
                }

                break;
            case GALLERY_REQUEST_CODE: //gallery
                if (resultCode == RESULT_OK) {
                    Uri selectedImage = imageReturnedIntent.getData();
                    imageUri = selectedImage;
                    presenter.add();
                }

                break;
        }
        dialog.dismiss();
    }


    @Override
    public void passImageButton(ImageButton buttonView, int index) {
        buttonView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Image img = imageArrayList.get(index);
                presenter.deleteImage(img, process);
                buttonView.setVisibility(View.INVISIBLE);
            }
        });
//        ConstraintLayout constraintLayout = findViewById(R.id.constraintLayout_homepage);
//        constraintLayout.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Log.e(TAG, "onConstraintLayoutClick: " + "clicked");
//                buttonView.setVisibility(View.INVISIBLE);
//            }
//        });
    }

    @Override
    public void onItemClick(int index) {
        Log.e(TAG, "clicked" + index);
        Uri uri = imageArrayList.get(index).getImgUri();

        sliderView.setCurrentPagePosition(index);
        sliderView.stopAutoCycle();
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                sliderView.startAutoCycle();
            }
        }, 5000);   //5 seconds
    }

    public void setDefaultFields(ArrayList<Image> images) {
        this.imageArrayList.clear();
        this.imageArrayList.addAll(images);
        this.sliderDataArrayList.clear();
        this.sliderDataArrayList.addAll(images);

        sliderAdapter.notifyDataSetChanged();
        imageAdapter.notifyDataSetChanged();
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

        Intent intent = getIntent();
        String json = intent.getStringExtra("myjson");
        Gson gson = new Gson();
        Folder f1 = gson.fromJson(json, Folder.class);

        return f1;
    }

    public void display(String message) {
        Log.e(TAG, message);
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    public void notifydatasetwaschanged() {
        Log.e(TAG, "notifydatasetwaschanged");
        imageAdapter.notifyDataSetChanged();

        sliderAdapter.notifyDataSetChanged();
    }
    public String GetVideoFileLocation(){
        return videoFileLocation;
    }

    public void showVideo(){

        spinnerDialog.dismiss();

        Intent intent = new Intent(this, VideoActivity.class);
        GsonBuilder builder = new GsonBuilder();
        builder.serializeNulls();
        Gson gson1 = builder.create();
        String jsonFolder = gson1.toJson(process);
        intent.putExtra("myjsonFolder", jsonFolder);
        intent.putExtra("fileLocation", videoFileLocation);
        startActivityForResult(intent, 1);
    }

    public double getDelay() {
        double delay = Double.parseDouble(SpinnerItems[selectedSpinnerIndex]);
        return delay;
    }
}
