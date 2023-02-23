package com.example.lifesaved.persistence;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.graphics.Bitmap;
import android.icu.text.CaseMap;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;


import com.example.lifesaved.UI.Folders.Folder;
import com.example.lifesaved.UI.Folders.HomePageActivity;
import com.example.lifesaved.UI.Viewing.Image;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class Repository { // this class will be used to get data from firebase.

    public interface FolderListener{

        void updateFolders(ArrayList<Folder> folderArrayList);

    }
    private FolderListener folderListener;
    public void setFolderListener(FolderListener folderListener) {
        this.folderListener = folderListener;
    }


    public interface FolderImageListener {
        void updateImage(String txt);

        void notifydatasetchanged();
    }
    private FolderImageListener folderImageListener;
    public void setFolderImageListener(FolderImageListener folderImageListener) {
        this.folderImageListener = folderImageListener;
    }


    public interface MultipleImagesListener{
        void updateImages(ArrayList<Image> images);
    }
    private MultipleImagesListener multipleImagesListener;
    public void setMultipleImagesListener(MultipleImagesListener multipleImagesListener) {
        this.multipleImagesListener = multipleImagesListener;
    }

    //:TODO add all the logic in Viewing-activity for these methods
    public void uploadMultipleImages(Folder f1,Uri uri){
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        StorageReference storageImg = storageRef.child("folders/"+f1.getFid()+"/"+ f1.getAmntOfImages() +".jpg");
        if(uri == null){
            Log.e(TAG, "UploadNormalImages: " + "uri is null");
            return;
        }
        storageImg.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                f1.setAmntOfImages(f1.getAmntOfImages()+1);
                //:TODO idk if i need to call an interface here
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "onFailure: "+e.getMessage());
            }
        });
    }

    public void readAllImagesInFolder(Folder f1) {
        ArrayList<Image> images = new ArrayList<>();
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        StorageReference storageImg = storageRef.child("folders/"+f1.getFid());

        storageImg.listAll().addOnCompleteListener(new OnCompleteListener<ListResult>() {
            @Override
            public void onComplete(@NonNull Task<ListResult> task) {
                if(task.isSuccessful()){
                    for(StorageReference ref : task.getResult().getItems()){
                        if((ref.getName().equals("mainimg.jpg"))){
                        }
                        else {
                            ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    Image image = new Image(uri);
                                    images.add(image);
                                }
                            });
                        }
                    }

                    multipleImagesListener.updateImages(images);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "onFailure: "+e.getMessage());
            }
        });
    }


    public void updateUserids(String newUid, String folderName) {
        String uid = FirebaseAuth.getInstance().getUid();
        DatabaseReference myRef = database.getReference("Folders");
        Query myQuery = database.getReference("Folders")
                .orderByChild("uid")
                .equalTo(uid);

        myQuery.orderByChild("name")
                .equalTo(folderName);

        //doesn't work

        myQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot snap : snapshot.getChildren()){
                    Folder f1 = snap.getValue(Folder.class);
                    f1.getUserIds().add(newUid);
                    myRef.child(snap.getKey()).setValue(f1);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public Folder addFolder(Folder f1) {
        DatabaseReference myRef = database.getReference("Folders").push();
        String uid = FirebaseAuth.getInstance().getUid();
        String Fid = myRef.getKey();
        f1.setFid(Fid);

        UploadFolderImage(f1.getIcon(), Fid);

        //might be redundant (probably is)
        ArrayList<String> userIds = f1.getUserIds();
        if(!userIds.contains(uid)){
            userIds.add(uid);
            f1.setUserIds(userIds);
        }
        FolderDTO folderDTO = new FolderDTO(f1);
        myRef.setValue(folderDTO);

        return f1;
    }

    //read all folders for user
    public void readfolderforUser() {
        Log.e(TAG, "readfolderforUser: I AM IN THE READ FUNCTION NOW" );

        String uid = FirebaseAuth.getInstance().getUid();
        ArrayList<Folder> folderArrayList = new ArrayList<>();
        DatabaseReference myRef = database.getReference("Folders");
        Query myQuery = database.getReference("Folders");

        myQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                folderArrayList.clear();

                for(DataSnapshot snap : snapshot.getChildren()){
                    Log.e(TAG, "onDataChange: " + snap.toString());
                    FolderDTO folderDTO = snap.getValue(FolderDTO.class);
                    Folder folder = folderDTO.toFolder();
                    folder.setFid(snap.getKey());

                    StorageReference storageRef = FirebaseStorage.getInstance().getReference().child("folders/" + folder.getFid() + "/mainimg.jpg");

                    try {
                        File localFile = File.createTempFile("images", "jpg");
                        final Uri fileUri = Uri.fromFile(localFile);
                        storageRef.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                // Local temp file has been created
                                folder.setIcon(fileUri);
                                Log.e(TAG, "onSuccess: " + "got image");
                                folderImageListener.notifydatasetchanged();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // Handle any errors
                                Log.e(TAG, "onFailure: " + "failed to get image");
                                Log.e(TAG, "onFailure: " + e.getMessage());
                            }
                        });

                    } catch (IOException e) {
                        e.printStackTrace();
                    }


                    folderArrayList.add(folder);
                }

                folderListener.updateFolders(folderArrayList);
                //get all the folders, put into arraylist, and then send to homepage
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    public void UploadFolderImage(Uri uri, String folderId) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        StorageReference folderImg = storageRef.child("folders/" + folderId + "/mainimg.jpg");
        if(uri == null){
            Log.e(TAG, "UploadFolderImage: " + "uri is null");
            return;
        }
        folderImg.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                //dismiss progress dialog
                Log.e(TAG, "onSuccess: " + "image uploaded");

                folderImageListener.updateImage("image uploaded");
                readfolderforUser();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //dismiss progress dialog
                Log.e(TAG, "onFailure: " + "image upload failed");

                folderImageListener.updateImage("image upload failed");
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                //show progress dialog
                double progress = (100.0 * snapshot.getBytesTransferred() / snapshot.getTotalByteCount());
                Log.e(TAG, "onProgress: " + progress);
                //send to progress dialog
                folderImageListener.updateImage(String.valueOf(progress));
            }
        });
    }

    public interface UserListener {
        public void onUserChanged(String user);
    }


    private static Repository instance = null; // functions written here will have access to the database
    FirebaseDatabase database = FirebaseDatabase.getInstance("https://lifesaved2-default-rtdb.europe-west1.firebasedatabase.app/");

    private UserListener userListener;

    public void setUserListener(UserListener listener) {
        this.userListener = listener;
    }



    // and will then return the info needed to the page that called it.
    private Repository() {
    }

    public static Repository getInstance() {
        if (instance == null) {
            instance = new Repository();
        }
        return instance;
    }


}