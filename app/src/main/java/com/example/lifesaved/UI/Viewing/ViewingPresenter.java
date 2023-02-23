package com.example.lifesaved.UI.Viewing;

import android.net.Uri;

import com.example.lifesaved.persistence.Repository;
import com.google.firebase.database.core.Repo;

import java.util.ArrayList;

public class ViewingPresenter implements Repository.MultipleImagesListener{
    private ViewingActivity view;

    public ArrayList<Image> imageArrayList = new ArrayList<>();

    public ViewingPresenter(ViewingActivity view) {
        this.view = view;

        Repository.getInstance().setMultipleImagesListener(this);



        view.setDefaultFields(this.imageArrayList);
    }

    public void add() {
        Uri uri = view.getUri();
        Image img = new Image(uri);

        imageArrayList.add(img);

        Repository.getInstance().setMultipleImagesListener(this);

        Repository.getInstance().uploadMultipleImages(view.getFolder(), uri);

        view.setDefaultFields(this.imageArrayList);

    }

    @Override
    public void updateImages(ArrayList<Image> images) {
        view.setDefaultFields(images);
    }
}
