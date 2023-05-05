package com.example.lifesaved.UI.Login;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import com.example.lifesaved.R;

public class LoadingScreenActivity extends AppCompatActivity {


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startActivity(new Intent(LoadingScreenActivity.this, MainActivity.class));
    }
}
