package com.example.lifesaved.UI.Settings;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.text.util.Linkify;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.lifesaved.R;
import com.example.lifesaved.UI.Folders.HomePageActivity;
import com.example.lifesaved.UI.Login.MainActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SettingsActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    private int worker_repeat_interval = 1440; //24hrs in minutes
    private static final String TAG = "SettingsActivity";
    private static final int NOTIFICATION_REQUEST_ID = 9;
    private FirebaseUser user;

    private String[] SpinnerItems = {"Never", "Every day", "Every 2 days", "Every 3 days", "Every 4 days", "Every 5 days", "Every 6 days", "Every 7 days"};
    private int[] SpinnerItemsIntervals = {0, 1440, 2880, 4320, 5760, 7200, 8640, 10080}; //in minutes
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();

    private Dialog dialog;
    private SettingsPresenter presenter;
    private int spinnerSelectedItem = -1;
    private Spinner dropdown;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        presenter = new SettingsPresenter(this);

        mAuth.addAuthStateListener(new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (firebaseAuth.getCurrentUser() == null) {
                    // If user is not logged in, redirect to login page
                    Intent loginIntent = new Intent(SettingsActivity.this, MainActivity.class);
                    startActivity(loginIntent);
                    Log.e(TAG, "onAuthStateChanged: User is not logged in");
                }
            }
        });

        dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_change_password);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));


        TextView resetPassword = (TextView) findViewById(R.id.textView_settings_reset_password);
        SpannableString content = new SpannableString("Reset password");
        content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
        resetPassword.setText(content);

        resetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.show();
                dialog.findViewById(R.id.button_dialog_settings_submit_password_change).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        presenter.resetPassword();
                    }
                });
            }
        });


        TextView contactUs = (TextView) findViewById(R.id.textView_settings_contact_us);
        SpannableString content1 = new SpannableString("Contact us");
        content1.setSpan(new UnderlineSpan(), 0, content1.length(), 0);
        contactUs.setText(content1);

        contactUs.setAutoLinkMask(Linkify.EMAIL_ADDRESSES);
        contactUs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setData(Uri.parse("mailto:noamramny@gmail.com"));
                intent.setPackage("com.google.android.gm");
                startActivity(intent);
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

        Button applyNotifInterval = findViewById(R.id.button_settings_submit_notif_interval_selected);
        applyNotifInterval.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (spinnerSelectedItem != -1) {
                    presenter.applyNotifInterval(worker_repeat_interval);
                    SaveNotifSetting();
                }
            }
        });


        dropdown = findViewById(R.id.spinner_settings_notif_intervals);
        //create a list of items for the spinner.
        //create an adapter to describe how the items are displayed, adapters are used in several places in android.
        //There are multiple variations of this, but this is the basic variant.

        ArrayAdapter adapter = new ArrayAdapter<String>(this, com.bumptech.glide.R.layout.support_simple_spinner_dropdown_item, SpinnerItems);
        //set the spinners adapter to the previously created one.
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dropdown.setAdapter(adapter);

        dropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                worker_repeat_interval = SpinnerItemsIntervals[position];
                spinnerSelectedItem = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        SharedPreferences sharedPreferences = getSharedPreferences("infoFile", MODE_PRIVATE);
        int notificationsindex = sharedPreferences.getInt("notificationsindex", 0);
        dropdown.setSelection(notificationsindex);

    }

    public void SaveNotifSetting() {
        SharedPreferences sharedPreferences = getSharedPreferences("infoFile", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("notificationsindex", spinnerSelectedItem);
        editor.commit();
    }


    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        worker_repeat_interval = SpinnerItemsIntervals[position];
        spinnerSelectedItem = position;
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }


    public String getOldPassword() {
        TextInputLayout password = dialog.findViewById(R.id.textInputLayout_dialog_settings_old_password);
        Log.e(TAG, "getPassword: " + password.getEditText().getText().toString());
        return password.getEditText().getText().toString();
    }

    public void showSuccessMessage(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    public String getNewPassword() {
        TextInputLayout newPassword = dialog.findViewById(R.id.textInputLayout_dialog_settings_first_password);
        String firstPassword = newPassword.getEditText().getText().toString();

        return firstPassword;

    }


    public boolean passwordsMatch() {
        TextInputLayout newPassword = dialog.findViewById(R.id.textInputLayout_dialog_settings_first_password);
        String firstPassword = newPassword.getEditText().getText().toString();

        TextInputLayout newPasswordConfirm = dialog.findViewById(R.id.textInputLayout_dialog_settings_second_password);
        String secondPassword = newPasswordConfirm.getEditText().getText().toString();

        if (firstPassword.equals(secondPassword)) {
            return true;
        } else {
            return false;
        }
    }

    public void passwordsDontMatch() {
        //Passwords dont match: show error message
        Toast.makeText(this, "Passwords dont match", Toast.LENGTH_SHORT).show();
    }

    public void showMessage(String s) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }

    public void closeDialog() {
        dialog.dismiss();
    }

    public void passwordsError() {
        TextInputLayout newPassword = dialog.findViewById(R.id.textInputLayout_dialog_settings_first_password);
        newPassword.setError("Password must be at least 6 characters long");
        TextInputLayout newPasswordConfirm = dialog.findViewById(R.id.textInputLayout_dialog_settings_second_password);
        newPasswordConfirm.setError("Password must be at least 6 characters long");
    }
}