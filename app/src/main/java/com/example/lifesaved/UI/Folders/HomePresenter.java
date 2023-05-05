package com.example.lifesaved.UI.Folders;


import android.net.Uri;
import android.util.Log;

import com.example.lifesaved.models.Folder;
import com.example.lifesaved.persistence.Repository;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

public class HomePresenter implements Repository.FolderListener, Repository.FolderImageListener, Repository.UserIdListener, Repository.DeletedFolderListener {
    private HomePageActivity view;


    public ArrayList<Folder> folderArrayList = new ArrayList<>();

    private String folderName = "x83ds6fk9";
    public HomePresenter(HomePageActivity view) {

        this.view = view;

        Repository.getInstance().setFolderListener(this);
        Repository.getInstance().setFolderImageListener(this);

        Repository.getInstance().readfolderforUser();

        Repository.getInstance().setDeletedFolderListener(this);

        view.setDefaultFields(this.folderArrayList);
    }

    public void add() {

        String subject = view.GetSubject();
        Uri uri = view.getUri();
        Folder f1 = new Folder(subject, uri);

        folderArrayList.add(f1);
        //:TODO if it breaks, its because of this

        Repository.getInstance().setFolderImageListener(this);


        f1 = Repository.getInstance().addFolder(f1);

        Log.e("Folder", "Folder added" + f1.getFid());
        view.setDefaultFields(this.folderArrayList);
    }

    @Override
    public void updateFolders(ArrayList<Folder> folderArrayList) {
        view.setDefaultFields(folderArrayList);
    }



    public void AddUserIdToFolder(String email) {
        folderName = view.getFolderName();
        Repository.getInstance().setUserIdListener(this);
        Log.e("Folder", "Folder added" + folderName + " Email: " + email);
        Repository.getInstance().readUserId(email);
    }

    @Override
    public void updateImageProgress(String txt) {
        //used to receive things from repository
        view.updateDisplay(txt);
    }

    @Override
    public void notifydatasetchanged() {
        view.notifydatasetwaschanged();
    }



    @Override
    public void onUidChanged(String newuid) {
        Repository.getInstance().addUserIdToFolder(newuid, folderName, FirebaseAuth.getInstance().getUid());
    }

    public void deleteFolder(Folder f1) {
        String uid = FirebaseAuth.getInstance().getUid();
        Repository.getInstance().deleteFolder(f1, uid);
    }

    @Override
    public void onFolderDeleted() {
        view.notifydatasetwaschanged();
    }
}