package com.example.lifesaved.UI.Folders;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import android.Manifest;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager2.widget.ViewPager2;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.lifesaved.UI.Login.MainActivity;
import com.example.lifesaved.R;
import com.example.lifesaved.UI.OnItemClickListener;
import com.example.lifesaved.UI.OnLongClickListener;
import com.example.lifesaved.UI.Settings.SettingsActivity;
import com.example.lifesaved.UI.Viewing.ViewingActivity;
import com.example.lifesaved.models.Folder;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

public class HomePageActivity extends AppCompatActivity implements OnItemClickListener, OnLongClickListener {

    private static final int REQUEST_WRITE_EXTERNAL_STORAGE = 2;
    public static final int GALLERY_REQUEST_CODE = 1;

    private static final String TAG = "FolderActivity";
    public ArrayList<Folder> folderArrayList = new ArrayList<>();

    private HomePresenter presenter;
    private Dialog dialogAddFolder;
    private Dialog dialogShare;

    private FoldersAdapter folderAdapter;
    AutoCompleteTextView actv;
    private Uri imageUri = null;
    private int recentFolderNumber = 0;

    TabLayout tabLayout;
    ViewPager2 viewPager2;
    ViewPagerAdapter viewPagerAdapter;
    FrameLayout frameLayout;

    public static ArrayAdapter<String> adapterNames;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);



        dialogShare = new Dialog(this);
        dialogShare.setContentView(R.layout.dialog_sharing_folder);
        dialogShare.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
//        actv = (AutoCompleteTextView) dialogShare.findViewById(R.id.autoCompleteTextView);


        tabLayout = dialogShare.findViewById(R.id.tabLayout_sharing);
        viewPager2 = dialogShare.findViewById(R.id.viewPager2);
        frameLayout = dialogShare.findViewById(R.id.frameLayout);
        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(this);
        viewPager2.setAdapter(viewPagerAdapter);

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                Log.e(TAG, "onTabSelected: " + tab.getPosition());
                viewPager2.setVisibility(View.VISIBLE);
                frameLayout.setVisibility(View.GONE);
                viewPager2.setCurrentItem(tab.getPosition());
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }
            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                viewPager2.setVisibility(View.VISIBLE);
                frameLayout.setVisibility(View.GONE );
            }
        });

        viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                switch (position){
                    case 0:
                        tabLayout.selectTab(tabLayout.getTabAt(0));
                        break;
                    case 1:
                        tabLayout.selectTab(tabLayout.getTabAt(1));
                        break;
                }
                super.onPageSelected(position);
            }
        });


        dialogAddFolder = new Dialog(this);
        dialogAddFolder.setContentView(R.layout.dialog_add_folder);
        dialogAddFolder.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        RecyclerView recyclerView = findViewById(R.id.recyclerview_folders);

        int numberOfColumns = 2;
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(this, numberOfColumns);
        recyclerView.setLayoutManager(layoutManager);

        folderAdapter = new FoldersAdapter(folderArrayList);
        folderAdapter.setListener(this);
        folderAdapter.setLongClickListener(this);
        recyclerView.setAdapter(folderAdapter);

        presenter = new HomePresenter(this);

        FloatingActionButton add = findViewById(R.id.floatingActionButton_homePage_add_button);
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialogAddFolder.show();

                Button submit = dialogAddFolder.findViewById(R.id.dialog_button_homepage_addfolder_confirm);
                submit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        presenter.add();
                        EditText editText = dialogAddFolder.findViewById(R.id.dialog_editText_homepage_subject);
                        editText.setText("");
                        ImageView imageView = dialogAddFolder.findViewById(R.id.imageView_dialog_addfolder_viewimage);
                        imageView.setImageResource(android.R.color.transparent);
                        //progressDialog.setTitle("Uploading...");
                        //progressDialog.show();

                        dialogAddFolder.dismiss();
                    }
                });
            }
        });

        FloatingActionButton updateUserIds = findViewById(R.id.floatingActionButton_homepage_share);
        updateUserIds.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialogShare.show();


//                Button submit = dialogShare.findViewById(R.id.button_dialolg_sharing_submit);
//                submit.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//                        EditText email = dialogShare.findViewById(R.id.editText_dialog_sharing_email);
//                        dialogShare.dismiss();
//                        presenter.AddUserIdToFolder(email.getText().toString());
//                        email.setText("");
//                        actv.setText("");
//                    }
//                });
            }

        });
        //button 1
        ImageView addFromGallery = dialogAddFolder.findViewById(R.id.imageview_dialog_add_img_to_folder_from_gallery);
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
        ImageView addFromCamera = dialogAddFolder.findViewById(R.id.imageview_dialog_add_img_to_folder_from_camera);
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
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.page_home);
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                GsonBuilder builder = new GsonBuilder();
                builder.serializeNulls();
                Gson gson = builder.create();

                switch (item.getItemId()) {
                    case R.id.page_home:
                        break;

                    case R.id.page_settings:
                        Intent intent2 = new Intent(HomePageActivity.this, SettingsActivity.class);
                        startActivity(intent2);
                        break;

                    case R.id.page_logout:
                        FirebaseAuth.getInstance().signOut();
                        Intent intent1 = new Intent(HomePageActivity.this, MainActivity.class);
                        startActivity(intent1);
                        break;
                }
                return true;
            }
        });

//        Button share = dialogShare.findViewById(R.id.);
//        share.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                presenter.shareMessage();
//            }
//        });









    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);
        Log.e(TAG, "onActivityResult: " + requestCode + " " + resultCode + " " + imageReturnedIntent);
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
                    ImageView imageView = dialogAddFolder.findViewById(R.id.imageView_dialog_addfolder_viewimage);
                    imageView.setImageURI(selectedImage);
                }
                break;
            case GALLERY_REQUEST_CODE: //gallery
                if (resultCode == RESULT_OK) {
                    Uri selectedImage = imageReturnedIntent.getData();
                    /*  def a better way   */
                    imageUri = selectedImage;
                    ImageView imageView = dialogAddFolder.findViewById(R.id.imageView_dialog_addfolder_viewimage);
                    imageView.setImageURI(selectedImage);

                    if (selectedImage != null)
                        Log.e(TAG, "onActivityResult: " + selectedImage.toString());
                }
                break;
        }
    }

    @Override
    public void onItemClick(int index) {
        recentFolderNumber = index;
        Log.e(TAG, "clicked" + index);
        Intent intent = new Intent(this, ViewingActivity.class);
        Folder f1 = folderArrayList.get(index);

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

        String[] fNames = new String[Folders.size()];
        for (int i = 0; i < fNames.length; i++) {
            fNames[i] = Folders.get(i).getName();
        }
        adapterNames = new ArrayAdapter<String>
                (this, android.R.layout.select_dialog_item, fNames);
//        actv.setThreshold(0);
//        actv.setAdapter(adapterNames);//setting the adapter data into the AutoCompleteTextView

        Gson gson = new Gson();
        String json = gson.toJson(fNames);
        SharedPreferences sharedprefs = getSharedPreferences("infoFile", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedprefs.edit();
        editor.putString("folderNames", json);
        editor.commit();



        folderAdapter.notifyDataSetChanged();

        Log.e(TAG, "setDefaultFields: " + folderArrayList.size() + " " + folderArrayList.toString());


        TextView welcome = findViewById(R.id.welcome);
        SharedPreferences sharedPreferences = getSharedPreferences("infoFile", MODE_PRIVATE);
        String user = sharedPreferences.getString("username", "");
        String result = user.replaceAll("@.+$", "");
        user = result.replaceAll("\\W+", " ");

        welcome.setText("Welcome " + user);

    }

    public String GetSubject() {
        EditText subject = dialogAddFolder.findViewById(R.id.dialog_editText_homepage_subject);
        return subject.getText().toString();
    }

    public Uri getUri() {
        return imageUri;
    }

    void sendToLogin() {
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

    public String getFolderName() {
        return actv.getText().toString();
    }


    @Override
    public void passImageButton(ImageButton buttonView, int index) {
        buttonView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.e(TAG, "onDeleteClick: " + "clicked");
                Folder f1 = folderArrayList.get(index);
                presenter.deleteFolder(f1);
            }
        });
        ConstraintLayout constraintLayout = findViewById(R.id.constraintLayout_homepage);
        constraintLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.e(TAG, "onConstraintLayoutClick: " + "clicked");
                buttonView.setVisibility(View.INVISIBLE);
            }
        });
    }
}