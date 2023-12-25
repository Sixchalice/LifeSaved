package com.example.lifesaved.persistence;

import static android.content.ContentValues.TAG;

import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;


import com.example.lifesaved.models.Folder;
import com.example.lifesaved.models.Image;
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
import com.google.firebase.database.core.view.QuerySpec;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

public class Repository {


    public interface FolderListener{

        void updateFolders(ArrayList<Folder> folderArrayList);
    }
    private FolderListener folderListener;
    public void setFolderListener(FolderListener folderListener) {
        this.folderListener = folderListener;
    }


    public interface FolderImageListener {

        void updateImageProgress(String txt);
        void notifydatasetchanged();
    }
    private FolderImageListener folderImageListener;
    public void setFolderImageListener(FolderImageListener folderImageListener) {
        this.folderImageListener = folderImageListener;
    }


    public interface MultipleImagesListener{

        void updateImages(ArrayList<Image> images);
        void notifydatasetchanged();
        void progress(String message);
    }
    private MultipleImagesListener multipleImagesListener;
    public void setMultipleImagesListener(MultipleImagesListener multipleImagesListener) {
        this.multipleImagesListener = multipleImagesListener;
    }


    public interface UserIdListener {

        void onUidChanged(String uid);
    }
    private UserIdListener userIdListener;
    public void setUserIdListener(UserIdListener listener) {
        this.userIdListener = listener;
    }


    public interface DeletedFolderListener {

        void onFolderDeleted();
    }
    private DeletedFolderListener deletedFolderListener;
    public void setDeletedFolderListener(DeletedFolderListener listener) {
        this.deletedFolderListener = listener;
    }



    public void setNumberOfImage(int number, String fid) {

        DatabaseReference myRef = FirebaseDatabase.getInstance().getReference("Folders/"+fid+"/amntOfImages");
        myRef.setValue(number);

//        readAllImagesInFolder(new Folder("name", fid));
        readAllImagesInFolder(fid);
    } //done
    public void uploadTheImage(int number, String fid, Uri uri) {
        int newplace = number + 1;
        StorageReference storageRef = FirebaseStorage.getInstance()
                .getReference().child("folders/" + fid + "/" + newplace + ".jpg");

        storageRef.putFile(uri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
//                Log.e(TAG, "LINE 84 onSuccess: added image to: # " + newplace);
                setNumberOfImage(newplace, fid);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });
    } //done

    public void uploadMultipleImages(Folder f1,Uri uri){

        Query myQuery = database.getReference("Folders/"+f1.getFid()+"/amntOfImages");
        myQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int number = snapshot.getValue(Integer.class);
                uploadTheImage(number, f1.getFid(), uri);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }

        });

    } //done


    public void readAllImagesInFolder(String fid) {

        Log.e(TAG, "readAllImagesInFolder: " + fid);
        ArrayList<Image> images = new ArrayList<>();

        StorageReference storageRef = FirebaseStorage.getInstance()
                .getReference().child("folders/" + fid);

        storageRef.listAll().addOnCompleteListener(new OnCompleteListener<ListResult>() {
            @Override
            public void onComplete(@NonNull Task<ListResult> task) {
                if (task.isSuccessful()) {
                    ListResult result = task.getResult();

                    for (StorageReference ref : result.getItems()) {
                        // All the items under listRef.

                        ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                try {
                                    File localFile = File.createTempFile("images", "jpg");
                                    final Uri fileUri = Uri.fromFile(localFile);
                                    ref.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                                        @Override
                                        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                            // Local temp file has been created
                                            String name = ref.getName();
                                            int id = 0;
                                            if(!name.equals("mainimg.jpg"))
                                                id = Integer.parseInt(name.substring(0, name.length() - 4));

                                            // Local temp file has been created
                                            Image image = new Image();
                                            image.setImgUri(fileUri);
                                            image.setId(id);
                                            images.add(image);

                                            multipleImagesListener.updateImages(images);
                                            multipleImagesListener.notifydatasetchanged();
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
//                                            Log.e(TAG, "LINE 161 onFailure: read all images from folder: " + e.getMessage());
                                        }
                                    }).addOnProgressListener(new OnProgressListener<FileDownloadTask.TaskSnapshot>() {
                                        @Override
                                        public void onProgress(@NonNull FileDownloadTask.TaskSnapshot taskSnapshot) {
                                            double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                                            multipleImagesListener.progress("Downloading: " + (int) progress + "%");
                                        }
                                    });
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.e(TAG, "LINE 177 onFailure: read all images from folder: " + e.getMessage());
                            }
                        });
                    }
                    // Uh-oh, an error occurred!
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG, "LINE 188 onFailure: read all images from folder: " + e.getMessage());
            }
        });
    } //done


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
    } //done

    //read all folders for user
    public void readfolderforUser() {
//        Log.e(TAG, "readfolderforUser: I AM IN THE READ FUNCTION NOW" );

        String uid = FirebaseAuth.getInstance().getUid();
        ArrayList<Folder> folderArrayList = new ArrayList<>();
        DatabaseReference myRef = database.getReference("Folders");
        Query myQuery = database.getReference("Folders");


        myQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                folderArrayList.clear();

                for(DataSnapshot snap : snapshot.getChildren()){
//                    Log.e(TAG, "onDataChange: " + snap.toString());
                    FolderDTO folderDTO = snap.getValue(FolderDTO.class);
                    Folder folder = folderDTO.toFolder();
                    folder.setFid(snap.getKey());
                    if(folder.getUserIds().contains(uid)){
                        StorageReference storageRef = FirebaseStorage.getInstance().getReference().child("folders/" + folder.getFid() + "/mainimg.jpg");

                        try {
                            File localFile = File.createTempFile("images", "jpg");
                            final Uri fileUri = Uri.fromFile(localFile);
                            storageRef.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                    // Local temp file has been created
                                    folder.setIcon(fileUri);
//                                Log.e(TAG, "onSuccess: " + "got image");

                                    folderImageListener.notifydatasetchanged();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    // Handle any errors
//                                Log.e(TAG, "onFailure: " + "failed to get image");
//                                Log.e(TAG, "onFailure: " + e.getMessage());
                                }
                            });

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        folderArrayList.add(folder);
                    }
                }
                folderListener.updateFolders(folderArrayList);
                //get all the folders, put into arraylist, and then send to homepage
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    } //done

    public void UploadFolderImage(Uri uri, String folderId) {
        Log.e(TAG, "UploadFolderImage: " + uri.toString() + " " + folderId);

        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        StorageReference folderImg = storageRef.child("folders/" + folderId + "/mainimg.jpg");
        if(uri == null){
//            Log.e(TAG, "UploadFolderImage: " + "uri is null");
            return;
        }
        folderImg.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                //dismiss progress dialog
//                Log.e(TAG, "onSuccess: " + "image uploaded");

                folderImageListener.updateImageProgress("image uploaded");
                readfolderforUser();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //dismiss progress dialog
//                Log.e(TAG, "onFailure: " + "image upload failed");

                folderImageListener.updateImageProgress("image upload failed");
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                //show progress dialog
                double progress = (100.0 * snapshot.getBytesTransferred() / snapshot.getTotalByteCount());

                folderImageListener.updateImageProgress(String.valueOf(progress));
            }
        });
    } //done


    public void addUserIdToFolder(String uid, String folderName, String ownerId) {
        DatabaseReference myRef = database.getReference("Folders");
        Query myQuery = database.getReference("Folders");
        myQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot snap : snapshot.getChildren()){
                    FolderDTO folderDTO = snap.getValue(FolderDTO.class);
                    Folder folder = folderDTO.toFolder();
                    folder.setFid(snap.getKey());
                    if(folder.getName().equals(folderName)){
                        ArrayList<String> userIds = folder.getUserIds();
                        if(userIds.contains(ownerId)){
                            if(!userIds.contains(uid)){
                                userIds.add(uid);
                                folder.setUserIds(userIds);
                            }
                            else{
                                //uid is already in the folder
                            }
                            FolderDTO folderDTO1 = new FolderDTO(folder);
                            myRef.child(folder.getFid()).setValue(folderDTO1);
                            break;
                        }
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    } //done

    public void readUserId(String email){
        Query myQuery = database.getReference("Users");
        myQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot snap : snapshot.getChildren()){
                    User user = snap.getValue(User.class);
                    if(user.getEmail().equals(email)){
                        String uid = user.getUid();
                        Log.e(TAG, "onReadUserId: uid: " + uid);
                        userIdListener.onUidChanged(uid);
                        break;
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    } //done
    public void addUser(String uid, String email) {
        DatabaseReference myRef = FirebaseDatabase.getInstance().getReference().child("Users").push();
        User user = new User(uid, email);
        myRef.setValue(user);
    } //done
    static class User{
        private String uid;
        private String email;
        public User(String uid, String email) {
            this.uid = uid;
            this.email = email;
        }
        public User(){}
        public String getUid() { return uid; }
        public void setUid(String uid) { this.uid = uid; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
    } // done
    public void deleteFolder(Folder f1, String uid) {
        DatabaseReference myRef = database.getReference("Folders");
        Query myQuery = database.getReference("Folders");
        myQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot snap : snapshot.getChildren()){
                    FolderDTO folderDTO = snap.getValue(FolderDTO.class);
                    Folder folder = folderDTO.toFolder();
                    folder.setFid(snap.getKey());
                    if(folder.getFid() == f1.getFid()){
                        ArrayList<String> userIds = folder.getUserIds();
                        if(userIds.contains(uid) && userIds.size() > 1){
                            userIds.remove(uid);
                            folder.setUserIds(userIds);
                            FolderDTO folderDTO1 = new FolderDTO(folder);
                            myRef.child(folder.getFid()).setValue(folderDTO1);

                            deletedFolderListener.onFolderDeleted();
                            break;
                        }
                        else if(userIds.contains(uid) && userIds.size() == 1){
                            DatabaseReference myRef = database.getReference("Folders/" + folder.getFid());
                            DeleteImagesOfFolder(f1);
                            myRef.removeValue();
                            break;
                        }
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    } //done
    private void DeleteImagesOfFolder(Folder f1) {
        Log.e(TAG, "DeleteImagesOfFolder: " + f1.getFid());
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        StorageReference folderRef = storageRef.child("folders/" + f1.getFid());

        folderRef.listAll()
                .addOnSuccessListener(new OnSuccessListener<ListResult>() {
                    @Override
                    public void onSuccess(ListResult listResult) {
                        for (StorageReference item : listResult.getItems()) {
                            item.delete();
                        }
                        Log.e(TAG, "onSuccess: " + "deleted all images of folder" + f1.getFid());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Uh-oh, an error occurred!
                        Log.e(TAG, "onFailure: " + e.getMessage());
                    }
                });
    } //done

    public void deleteImage(Image img, Folder folder) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        Log.e(TAG, "deleteImage: " + "folders/" + folder.getFid() + "/" + img.getId());
        StorageReference imageRef = storageRef.child("folders/" + folder.getFid() + "/" + img.getId() + ".jpg");
        imageRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Log.e(TAG, "onSuccess: " + "image deleted");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG, "onFailure: " + "image not deleted");
            }
        });
    }
    //////


    public void addUserWithCode(String currentUid, int shareCode){

        DatabaseReference codesRef = database.getReference("Codes");
        codesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot snap : snapshot.getChildren()){
                    Code code = snap.getValue(Code.class);
                    if(code.getShareCode() == shareCode){
                        String fid = code.getFid();
                        addFolderToUser(currentUid, fid);
                        break;
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }
    public void addFolderToUser(String uid, String fid){

        DatabaseReference foldersRef = database.getReference("Folders");
        Query foldersQuery = database.getReference("Folders");
        foldersQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot snap : snapshot.getChildren()){
                    FolderDTO folderDTO = snap.getValue(FolderDTO.class);
                    Folder folder = folderDTO.toFolder();
                    folder.setFid(snap.getKey());
                    if(folder.getFid().equals(fid)){
                        ArrayList<String> userIds = folder.getUserIds();
                        userIds.add(uid);
                        folder.setUserIds(userIds);
                        FolderDTO folderDTO1 = new FolderDTO(folder);
                        foldersRef.child(folder.getFid()).setValue(folderDTO1);
                        break;
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }


    public void connectFolderWithShareCode(String uid,String folderName, int shareCode){

        final Boolean[] codeExists = {false};
        final Boolean[] fidExists = {false};

        Query codesQuery = database.getReference("Codes");
        codesQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot snap : snapshot.getChildren()){
                    Code code = snap.getValue(Code.class);
                    if(code.shareCode == shareCode){
                        codeExists[0] = true;
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
        if(codeExists[0] == false){


            String fid = "";
            DatabaseReference foldersRef = database.getReference("Folders");
            Query myQuery = database.getReference("Folders");
            myQuery.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    //making sure that the folder belongs to the person sharing it also.
                    for(DataSnapshot snap : snapshot.getChildren()) {
                        FolderDTO folderDTO = snap.getValue(FolderDTO.class);
                        Folder folder = folderDTO.toFolder();
                        folder.setFid(snap.getKey());
                        if(folder.getName().equals(folderName)){
                            ArrayList<String> userIds = folder.getUserIds();
                            if(userIds.contains(uid)){
                                //check to see if fid already exists:
                                codesQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        for(DataSnapshot snap : snapshot.getChildren()){
                                            Code code = snap.getValue(Code.class);
                                            if(code.fid.equals(folder.getFid())){
                                                fidExists[0] = true;
                                                DatabaseReference codesRef = database.getReference("Codes/" + snap.getKey()+"/shareCode");
                                                codesRef.setValue(shareCode);

                                                break;
                                            }
                                        }
                                        if(fidExists[0] == false){
                                            //create the connection between the code and the folder.
                                            DatabaseReference myRef = FirebaseDatabase.getInstance().getReference().child("Codes").push();
                                            Code code = new Code(folder.getFid(), shareCode);
                                            myRef.setValue(code);
                                        }
                                    }
                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                    }
                                });
                            }
                        }
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
        else{
            Log.e(TAG, "connectFolderWithShareCode: " + "code already exists");
        }
    }
    static class Code{
        private String fid;
        private int shareCode;
        public Code(String fid, int shareCode) {
            this.fid = fid;
            this.shareCode = shareCode;
        }
        public Code(){}
        public String getFid() { return fid; }
        public void setFid(String fid) { this.fid = fid; }
        public int getShareCode() { return shareCode; }
        public void setShareCode(int shareCode) { this.shareCode = shareCode; }
    }
    private static Repository instance = null;
    FirebaseDatabase database = FirebaseDatabase.getInstance("https://lifesaved2-default-rtdb.europe-west1.firebasedatabase.app/");


    private Repository() {
    }

    public static Repository getInstance() {
        if (instance == null) {
            instance = new Repository();
        }
        return instance;
    }


}