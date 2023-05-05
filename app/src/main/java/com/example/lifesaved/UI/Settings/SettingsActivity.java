package com.example.lifesaved.UI.Settings;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import android.Manifest;
import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.lifesaved.MyWorker;
import com.example.lifesaved.R;
import com.example.lifesaved.UI.Folders.HomePageActivity;
import com.example.lifesaved.UI.Login.MainActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.concurrent.TimeUnit;

public class SettingsActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener{
    private int worker_repeat_interval = 1440; //24hrs in minutes
    private static final String TAG = "SettingsActivity";
    private static final int NOTIFICATION_REQUEST_ID = 9;
    private FirebaseUser user;

    private String[] SpinnerItems = {"Every day", "Every 2 days", "Every 3 days", "Every 4 days", "Every 5 days", "Every 6 days", "Every 7 days"};
    private int[] SpinnerItemsIntervals = {1440, 2880, 4320, 5760, 7200, 8640, 10080};
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();

    private Dialog dialog;
    private SettingsPresenter presenter;
    private Switch notifsSwitch;
    private int spinnerSelectedItem = -1;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        presenter = new SettingsPresenter(this);

        dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_update_email);

        notifsSwitch = findViewById(R.id.switch_settings_push_notifications);

        CheckIfNotificationsAreOn();

        notifsSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (notifsSwitch.isChecked()){
                    SharedPreferences sharedPreferences = getSharedPreferences("infoFile", MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean("notifications", true);
                    editor.commit();

                    notifsSwitch.setText("Notifications are on");
                    Log.e(TAG, "onClick: Switch is checked on" );
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        if (ActivityCompat.checkSelfPermission(SettingsActivity.this,Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                            requestPermissions(new String[] {Manifest.permission.POST_NOTIFICATIONS}, 1);
                        }
                        else {
                            PeriodicWorkRequest periodicWorkRequest = new
                                    PeriodicWorkRequest.Builder(MyWorker.class, worker_repeat_interval, TimeUnit.MINUTES)
//                                    .setInitialDelay(2000, TimeUnit.MILLISECONDS)
                                    .addTag("WORKER_PUSH_NOTIFICATION")
                                    .build();
                            WorkManager.getInstance(SettingsActivity.this).enqueue(periodicWorkRequest);
                        }
                    } else {
                        PeriodicWorkRequest periodicWorkRequest = new
                                PeriodicWorkRequest.Builder(MyWorker.class, worker_repeat_interval, TimeUnit.MINUTES)
//                                .setInitialDelay(2000, TimeUnit.MILLISECONDS)
                                .addTag("WORKER_PUSH_NOTIFICATION")
                                .build();
                        WorkManager.getInstance(SettingsActivity.this).enqueue(periodicWorkRequest);
                    }
                }
                else{
                    SharedPreferences sharedPreferences = getSharedPreferences("infoFile", MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean("notifications", false);
                    editor.commit();

                    notifsSwitch.setText("Notifications are off");
                    Log.e(TAG, "onClick: Switch is checked off" );

                    WorkManager.getInstance(SettingsActivity.this).cancelAllWorkByTag("WORKER_PUSH_NOTIFICATION");
                }
            }
        });

        mAuth.addAuthStateListener(new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (firebaseAuth.getCurrentUser() == null) {
                    // If user is not logged in, redirect to login page
                    Intent loginIntent = new Intent(SettingsActivity.this, MainActivity.class);
                    startActivity(loginIntent);
                    Log.e(TAG, "onAuthStateChanged: User is not logged in" );
                }
            }
        });

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.page_settings);
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.page_home:
                        Intent intent = new Intent(SettingsActivity.this, HomePageActivity.class);
                        startActivity(intent);

                        break;

                    case R.id.page_settings:
                        break;

                    case R.id.page_logout:
                        FirebaseAuth.getInstance().signOut();
                        Intent intent1 = new Intent(SettingsActivity.this, MainActivity.class);
                        startActivity(intent1);
                        break;
                }
                return true;
            }
        });
        Spinner dropdown = findViewById(R.id.spinner_settings_notif_intervals);
        //create a list of items for the spinner.
        //create an adapter to describe how the items are displayed, adapters are used in several places in android.
        //There are multiple variations of this, but this is the basic variant.
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, SpinnerItems){
            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent)
            {
                View v = null;
                v = super.getDropDownView(position, null, parent);
                // If this is the selected item position
                if (position == spinnerSelectedItem) {
                    v.setBackgroundColor(Color.BLUE);
                }
                else {
                    // for other views
                    v.setBackgroundColor(Color.WHITE);

                }
                return v;
            }
        };
        //set the spinners adapter to the previously created one.
        dropdown.setAdapter(adapter);

        dropdown.setOnItemSelectedListener(this);



        Button ChangeEmail = findViewById(R.id.button_settings_change_email);
        ChangeEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.show();
                Button submit = dialog.findViewById(R.id.button_dialog_settings_submit);
                submit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        SharedPreferences sharedPreferences = getSharedPreferences("infoFile", MODE_PRIVATE);
                        String oldemail = sharedPreferences.getString("username", "");
                        presenter.updateEmail(oldemail);
                        dialog.dismiss();
                    }
                });
            }
        });
    }

    private void CheckIfNotificationsAreOn() {
        SharedPreferences sharedPreferences = getSharedPreferences("infoFile", MODE_PRIVATE);
        boolean notifications = sharedPreferences.getBoolean("notifications", false);
        if (notifications){
            notifsSwitch.setChecked(true);
            notifsSwitch.setText("Notifications are on");
        }
        else{
            notifsSwitch.setChecked(false);
            notifsSwitch.setText("Notifications are off");
        }
    }


    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        worker_repeat_interval = SpinnerItemsIntervals[position];
        spinnerSelectedItem = position;
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    public String getNewEmail() {
        TextInputLayout email = dialog.findViewById(R.id.textInputLayout_dialog_settings_email);
        return email.getEditText().getText().toString();
    }

    public String getPassword() {
        TextInputLayout password = dialog.findViewById(R.id.textInputLayout_dialog_settings_password);
        Log.e(TAG, "getPassword: " + password.getEditText().getText().toString());
        return password.getEditText().getText().toString();
    }

    public void showSuccessMessage(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

}