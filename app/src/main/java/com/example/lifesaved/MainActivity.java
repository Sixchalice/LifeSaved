package com.example.lifesaved;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.material.textfield.TextInputEditText;

public class MainActivity extends AppCompatActivity {

    private Dialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_createaccount);

        Button openDialog = findViewById(R.id.button_login_signup);
        openDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.show();
            }
        });

    }

    public void Check(View v){
        EditText email = findViewById(R.id.editText_login_email);
        String eMail = email.getText().toString();

        TextInputEditText password = findViewById(R.id.textInputLayout_login_password);
        String pass = password.getText().toString();

    }
}