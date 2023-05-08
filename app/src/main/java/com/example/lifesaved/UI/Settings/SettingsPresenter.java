package com.example.lifesaved.UI.Settings;

import static android.content.Context.MODE_PRIVATE;


import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.example.lifesaved.MyWorker;
import com.example.lifesaved.persistence.Repository;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.concurrent.TimeUnit;

public class SettingsPresenter {
    private static final String TAG = "SettingsPresenter";
    private SettingsActivity view;

    public SettingsPresenter(SettingsActivity view) {
        this.view = view;
    }



    public void applyNotifInterval(int worker_repeat_interval) {
        if(worker_repeat_interval != 0){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ActivityCompat.checkSelfPermission(view, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                    view.requestPermissions(new String[] {Manifest.permission.POST_NOTIFICATIONS}, 1);
                }
                else {
                    PeriodicWorkRequest periodicWorkRequest = new
                            PeriodicWorkRequest.Builder(MyWorker.class, worker_repeat_interval, TimeUnit.MINUTES)
//                                    .setInitialDelay(2000, TimeUnit.MILLISECONDS)
                            .addTag("WORKER_PUSH_NOTIFICATION")
                            .build();
                    WorkManager.getInstance(view).enqueue(periodicWorkRequest);
                }
            } else {
                PeriodicWorkRequest periodicWorkRequest = new
                        PeriodicWorkRequest.Builder(MyWorker.class, worker_repeat_interval, TimeUnit.MINUTES)
//                                .setInitialDelay(2000, TimeUnit.MILLISECONDS)
                        .addTag("WORKER_PUSH_NOTIFICATION")
                        .build();
                WorkManager.getInstance(view).enqueue(periodicWorkRequest);
            }
        }else{
            WorkManager.getInstance(view).cancelAllWorkByTag("WORKER_PUSH_NOTIFICATION");
        }

    }

    public void resetPassword() {
        String password = view.getOldPassword();
        String newPassword = view.getNewPassword();
        boolean passwordsMatch = view.passwordsMatch();

        if(passwordsMatch){
            updatePassword(password, newPassword);
        }
        else{
            view.passwordsDontMatch();
        }
    }

    private void updatePassword(String password, String newPassword) {

        if (password == null || password.isEmpty() || newPassword == null || newPassword.isEmpty()) {
            view.showMessage("Please enter all the details");
            return;
        }
        if(newPassword.length() < 6){
            view.showMessage("Password must be at least 6 characters long");
            return;
        }
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        AuthCredential credential = EmailAuthProvider
                .getCredential(user.getEmail(), password); // Current Login Credentials \\

            user.reauthenticate(credential)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            user.updatePassword(newPassword).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Log.d(TAG, "Password updated");
                                        view.showMessage("Password updated");
                                        view.closeDialog();
                                    } else {
                                        Log.d(TAG, "Error password not updated");
                                    }
                                }
                            });
                        } else {
                            Log.d(TAG, "Error auth failed");
                            view.showMessage("Authentication failed.");
                        }
                    }
                });
    }
}
