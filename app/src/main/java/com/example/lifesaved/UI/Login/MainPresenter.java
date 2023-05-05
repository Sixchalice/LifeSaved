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

    public MainPresenter(MainActivity view, FirebaseAuth mAuth) {
        this.view = view;
        this.mAuth = mAuth;

    }

    public boolean checkEmailValidity(String email) {
        String regex = "^(.+)@(.+)$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    public void loginClick() {
        String email = view.getEmail();
        String password = view.getPassword();
        boolean validity = checkEmailValidity(email);
        if (validity) {
            try {
                mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(view, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            view.navigateToNextPage();
                        } else {
                            view.notifyOfError("Incorrect email or password");
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
    }

    public void RegisterClick() {
        String email = view.getEmail();
        String password = view.getPassword();
        boolean validity = checkEmailValidity(email);

        if (validity) {
            mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(view, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        addUser(email);
                        view.navigateToNextPage();
                    } else {
                        //if sign in fails, display a message to the user
                        view.notifyOfError("Email already in use");
                    }
                }
            });
            SharedPreferences sharedPreferences = view.getSharedPreferences("infoFile", MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("username", email);
//            editor.putString("password", password);
            editor.commit();
        } else {
            view.emailError();
        }
    }
    public void addUser(String email) {
        Repository.getInstance().addUser(FirebaseAuth.getInstance().getUid(), email);
    }
}


