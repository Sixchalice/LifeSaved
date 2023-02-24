package com.example.lifesaved.UI.Folders;

import androidx.core.app.ActivityCompat;
import android.Manifest;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.icu.text.CaseMap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.lifesaved.UI.Login.MainActivity;
import com.example.lifesaved.R;
import com.example.lifesaved.UI.OnItemClickListener;
import com.example.lifesaved.UI.Viewing.ViewingActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;

public class HomePageActivity extends AppCompatActivity implements OnItemClickListener {

    private static final int REQUEST_WRITE_EXTERNAL_STORAGE = 2;
    public static final int GALLERY_REQUEST_CODE = 1;

    private static final String TAG = "FolderActivity";
    public ArrayList<Folder> folderArrayList = new ArrayList<>();

    private HomePresenter presenter;
    private Dialog dialog;

    // final ProgressDialog progressDialog = new ProgressDialog(this);

    private FoldersAdapter folderAdapter;

    private Uri imageUri = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);




        dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_add_folder);

        RecyclerView recyclerView = findViewById(R.id.recyclerview_folders);

        int numberOfColumns = 2;
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(this, numberOfColumns);
        recyclerView.setLayoutManager(layoutManager);

        folderAdapter = new FoldersAdapter(folderArrayList);
        folderAdapter.setListener(this);
        recyclerView.setAdapter(folderAdapter);

        presenter = new HomePresenter(this);

        FloatingActionButton add = findViewById(R.id.floatingActionButton_homePage_add_button);
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.show();

                Button submit = dialog.findViewById(R.id.dialog_button_homepage_addfolder_confirm);
                submit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        presenter.add();

                        //progressDialog.setTitle("Uploading...");
                        //progressDialog.show();

                        dialog.dismiss();
                    }
                });
            }
        });

        Button updateUserIds = findViewById(R.id.button_homepage_updatenew_user);
        updateUserIds.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                presenter.updateUserIds("123456", "test1");
            }
        });
        //read from database for folder per user:

        //button 1
        ImageView addFromGallery = dialog.findViewById(R.id.imageview_dialog_add_img_to_folder_from_gallery);
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
        ImageView addFromCamera = dialog.findViewById(R.id.imageview_dialog_add_img_to_folder_from_camera);
        addFromCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(HomePageActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        == PackageManager.PERMISSION_GRANTED) {
                    // Permission is already granted, proceed with the operation.
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(intent, REQUEST_WRITE_EXTERNAL_STORAGE);
                } else {
                    // Permission is not granted, request it.
                    ActivityCompat.requestPermissions(HomePageActivity.this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            REQUEST_WRITE_EXTERNAL_STORAGE);
                }


            }
        });
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);
        Log.e(TAG, "onActivityResult: " + requestCode + " " + resultCode + " " + imageReturnedIntent);
        switch(requestCode) {
            case REQUEST_WRITE_EXTERNAL_STORAGE: //camera
                if (resultCode == RESULT_OK) {


                    Bitmap bitmap = (Bitmap) imageReturnedIntent.getExtras().get("data");
                    //convert bitmap to uri
                    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
                    String path = MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, "Title", null);
                    Uri selectedImage = Uri.parse(path);

                    imageUri = selectedImage;
                    ImageView imageView = dialog.findViewById(R.id.imageView_dialog_addfolder_viewimage);
                    imageView.setImageURI(selectedImage);
                }

                break;
            case GALLERY_REQUEST_CODE: //gallery
                if (resultCode == RESULT_OK) {
                    Uri selectedImage = imageReturnedIntent.getData();
                    /*  def a better way   */
                    imageUri = selectedImage;
                    ImageView imageView = dialog.findViewById(R.id.imageView_dialog_addfolder_viewimage);
                    imageView.setImageURI(selectedImage);

                    if (selectedImage != null)
                        Log.e(TAG, "onActivityResult: " + selectedImage.toString());

                }

                break;

        }
    }

    @Override
    public void onItemClick(int index) {
        Log.e(TAG, "clicked" + index);
        Intent intent = new Intent(this, ViewingActivity.class);
        Folder f1 = folderArrayList.get(index);

        //:TODO use gson to pass folder
        GsonBuilder builder = new GsonBuilder();
        builder.serializeNulls();
        Gson gson = builder.create();
        Uri temp = f1.getIcon();
        f1.setIcon(null);
        String json = gson.toJson(f1);
        f1.setIcon(temp);
        intent.putExtra("myjson", json);

        startActivity(intent);
    }

    public void setDefaultFields(ArrayList<Folder> Folders) {
        this.folderArrayList.clear();
        this.folderArrayList.addAll(Folders);

        folderAdapter.notifyDataSetChanged();

        Log.e(TAG, "setDefaultFields: " + folderArrayList.size() + " " + folderArrayList.toString());


        TextView welcome = findViewById(R.id.welcome);
        SharedPreferences sharedPreferences = getSharedPreferences("infoFile", MODE_PRIVATE);
        String user = sharedPreferences.getString("username", "");
        String result = user.replaceAll("@.+$", "");
        user = result.replaceAll("\\W+"," ");

        welcome.setText("Welcome " + user);

    }

    public String GetSubject() {
        EditText subject = dialog.findViewById(R.id.dialog_editText_homepage_subject);
        return subject.getText().toString();
    }
    public Uri getUri() {
        return imageUri;
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
        }
        return super.onOptionsItemSelected(item);
    }
    public void sendToLogin() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    public void updateDisplay(String message) {
        //update the display
        Toast.makeText(this, message + "%", Toast.LENGTH_SHORT).show();
        //progressDialog.dismiss();
    }

    public void notifydatasetwaschanged() {

        folderAdapter.notifyDataSetChanged();

    }
}