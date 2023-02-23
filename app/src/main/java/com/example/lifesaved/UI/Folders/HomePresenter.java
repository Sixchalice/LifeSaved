package com.example.lifesaved.UI.Folders;


import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.example.lifesaved.persistence.Repository;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;

public class HomePresenter implements Repository.FolderListener, Repository.FolderImageListener {
    private HomePageActivity view;

//    FirebaseStorage storage = FirebaseStorage.getInstance();

    public ArrayList<Folder> folderArrayList = new ArrayList<>();

    public HomePresenter(HomePageActivity view) {


        this.view = view;

        Repository.getInstance().setFolderListener(this);

        Repository.getInstance().setFolderImageListener(this);

        Repository.getInstance().readfolderforUser();

        view.setDefaultFields(this.folderArrayList);
    }

    public void add() {

        String subject = view.GetSubject();
        Uri uri = view.getUri();


        Folder f1 = new Folder(subject, uri);

        folderArrayList.add(f1);

        Repository.getInstance().setFolderImageListener(this);

        f1 = Repository.getInstance().addFolder(f1);

        Log.e("Folder", "Folder added" + f1.getFid());
        view.setDefaultFields(this.folderArrayList);

    }

    @Override
    public void updateFolders(ArrayList<Folder> folderArrayList) {



        view.setDefaultFields(folderArrayList);
    }

    public void updateUserIds(String newUid, String folderName) {
        Repository.getInstance().updateUserids(newUid, folderName);
    }

    @Override
    public void updateImage(String txt) {
        //used to receive things from repository
        view.updateDisplay(txt);
    }

    @Override
    public void notifydatasetchanged() {
        view.notifydatasetwaschanged();

    }
}

