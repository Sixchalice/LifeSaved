package com.example.lifesaved.UI.Settings;

import static android.content.Context.MODE_PRIVATE;

import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.lifesaved.UI.Folders.HomePageActivity;
import com.example.lifesaved.persistence.Repository;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.core.Repo;

public class SettingsPresenter {
    private SettingsActivity view;

    public SettingsPresenter(SettingsActivity view) {
        this.view = view;
    }

    public void updateEmail(String OldEmail) {
        String newEmail = view.getNewEmail();
        String password = view.getPassword();

        Log.e("Settings activity: ", "User re-authenticating." + OldEmail + " " + password);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        // Get auth credentials from the user for re-authentication
        AuthCredential credential = EmailAuthProvider
                .getCredential(OldEmail, password); // Current Login Credentials \\
        user.reauthenticate(credential)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Log.e("Settings activity: ", "User re-authenticated.");
                        //----------------Code for Changing Email Address----------\\
                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        user.updateEmail(newEmail)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Log.e("Settings activity: ", "User email address updated.");
                                            view.showSuccessMessage("Email address updated successfully.");
                                            updateEmailInFirebase(newEmail);
                                        }
                                    }
                                });
                        //----------------------------------------------------------\\
                    }
                });
    }

    public void updateEmailInFirebase(String email){
        SharedPreferences sharedPreferences = view.getSharedPreferences("infoFile", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("email", email);
        editor.commit();

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Repository.getInstance().updateUserEmail(uid, email);

    }
}
