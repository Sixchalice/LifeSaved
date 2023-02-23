package com.example.lifesaved.UI.Login;

import static android.content.Context.MODE_PRIVATE;

import android.app.Dialog;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import com.example.lifesaved.persistence.Repository;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class MainPresenter {
    private MainActivity view;
    private FirebaseAuth mAuth;
    private Dialog dialog;

    public MainPresenter(MainActivity view, FirebaseAuth mAuth, Dialog dialog) {
        this.view = view;
        this.mAuth = mAuth;
        this.dialog = dialog;
    }


    public void loginClick() {
        boolean correctCredentials = true;
        String email = view.getEmail();
        String password = view.getPassword();


        try {
            mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(view, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        view.navigateToNextPage();
                    } else {
                        view.notifyOfError();
                    }
                }
            });
            SharedPreferences sharedPreferences = view.getSharedPreferences("infoFile", MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("username", email);
            editor.commit();
        }
        catch (Exception e) {
            e.printStackTrace();

        }

    }

    public void RegisterClick() {
        String email = view.getDialogEmail();
        String password = view.getDialogPassword();

        String regex = "^(.+)@(.+)$";
        Pattern pattern = Pattern.compile(regex);

        Matcher matcher = pattern.matcher(email);


        if (matcher.matches()) {
            mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(view, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        view.navigateToNextPage();
                    } else {
                        //if sign in fails, display a message to the user
                        view.notifyOfError();
                    }
                }
            });

            SharedPreferences sharedPreferences = view.getSharedPreferences("infoFile", MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("username", email);
            editor.commit();

        } else {
            view.emailError();
        }

    }
}
