package com.example.lifesaved.UI.Login;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.lifesaved.R;
import com.example.lifesaved.UI.Folders.HomePageActivity;
import com.example.lifesaved.persistence.Repository;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity implements Repository.UserListener {

    private Dialog dialog;
    private FirebaseAuth mAuth;

    private MainPresenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAuth = FirebaseAuth.getInstance();

        presenter = new MainPresenter(this, mAuth, dialog);

        dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_createaccount);

        Button openDialog = findViewById(R.id.button_login_signup);
        openDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.show();
            }
        });

        Button LogIn = findViewById(R.id.button_login_SignIn);
        LogIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                presenter.loginClick();
            }
        });

        Button Register = dialog.findViewById(R.id.button_register_makeaccount);
        Register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                presenter.RegisterClick();
            }
        });
    }

    public String getEmail() {
        TextInputLayout user = findViewById(R.id.textInputLayout_login_email);
        return user.getEditText().getText().toString();
    }

    public void setEmail(String username) {
        TextInputLayout email = findViewById(R.id.textInputLayout_login_email);
        email.getEditText().setText(username);
    }

    public String getPassword() {
        TextInputLayout password = findViewById(R.id.textInputLayout_login_password);
        return password.getEditText().getText().toString();
    }

    public String getDialogEmail() {
        TextInputLayout email = dialog.findViewById(R.id.textInputLayout_register_email);
        return email.getEditText().getText().toString();
    }

    public String getDialogPassword() {
        TextInputLayout password = dialog.findViewById(R.id.textInputLayout_register_password);
        return password.getEditText().getText().toString();
    }

    public void navigateToNextPage() {
        Intent intent = new Intent(MainActivity.this, HomePageActivity.class);
        startActivity(intent);
    }

//public  void register(View view){
//        String email = getEmail();
//        String password = getPassword();
//    mAuth.createUserWithEmailAndPassword(email, password)
//            .addOnCompleteListener(MainActivity.this, new OnCompleteListener<AuthResult>() {
//                @Override
//                public void onComplete(@NonNull Task<AuthResult> task) {
//                    if(task.isSuccessful()){
//                        navigateToNextPage();
//                    }
//                    else{
//                        //if sign in fails, display a message to the user
//                        NotifyOfError();
//                    }
//                }
//            });
//}

    public void notifyOfError() {
        Toast.makeText(MainActivity.this, "register failed", Toast.LENGTH_SHORT).show();
    }

    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            navigateToNextPage();
        }
    }

    public void onUserChanged(String user) {
        //idk
    }

    public void emailError() {

        Toast.makeText(this, "Check email validity", Toast.LENGTH_SHORT).show();
    }
}