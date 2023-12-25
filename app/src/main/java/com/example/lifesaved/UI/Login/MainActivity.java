package com.example.lifesaved.UI.Login;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.lifesaved.R;
import com.example.lifesaved.UI.Folders.HomePageActivity;
import com.example.lifesaved.persistence.Repository;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class  MainActivity extends AppCompatActivity{

    private FirebaseAuth mAuth;
    private MainPresenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        mAuth = FirebaseAuth.getInstance();

        presenter = new MainPresenter(this, mAuth);


        ImageView submit = findViewById(R.id.imageview_main_submit);
        TabLayout tabLayout = findViewById(R.id.tab_layout);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("TAG", "onClick: submit");
                tabLayout.getSelectedTabPosition();
                if (tabLayout.getSelectedTabPosition() == 0) { //login
                    presenter.loginClick();
                } else if (tabLayout.getSelectedTabPosition() == 1) { // register
                    presenter.RegisterClick();
                }
            }
        });

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    submit.setImageResource(R.drawable.login_btn);
                } else if (tab.getPosition() == 1) {
                    submit.setImageResource(R.drawable.signup_btn);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

    }

    public String getEmail() {
        TextInputLayout user = findViewById(R.id.textInputLayout_login_email);
        user.setError(null);
        return user.getEditText().getText().toString();
    }

    public void setEmail(String username) {
        TextInputLayout email = findViewById(R.id.textInputLayout_login_email);
        email.getEditText().setText(username);
    }

    public String getPassword() {
        TextInputLayout password = findViewById(R.id.textInputLayout_login_password);
        password.setError(null);
        return password.getEditText().getText().toString();
    }


    public void navigateToNextPage() {
        Intent intent = new Intent(MainActivity.this, HomePageActivity.class);
        startActivity(intent);


    }

    public void notifyOfError(String msg) {
        Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
    }

    public void onStart() {
        super.onStart();
        // Check if user is signed in
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            navigateToNextPage();
        }
    }

    public void passwordError() {
        TextInputLayout password = findViewById(R.id.textInputLayout_login_password);
        password.setError("Password must be at least 6 characters");
    }
    public void emailError() {
        TextInputLayout user = findViewById(R.id.textInputLayout_login_email);
        user.setError("Invalid email");
    }
}
