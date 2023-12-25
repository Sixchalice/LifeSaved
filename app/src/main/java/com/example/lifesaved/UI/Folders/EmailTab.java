package com.example.lifesaved.UI.Folders;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;

import com.example.lifesaved.R;
import com.example.lifesaved.persistence.Repository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.gson.Gson;

public class EmailTab extends Fragment implements Repository.UserIdListener{

    AutoCompleteTextView actv;
    private String currFolderName = "xxx";

    public EmailTab() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_email_tab, container, false);

        actv = (AutoCompleteTextView) view.findViewById(R.id.autoCompleteTextView);

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("infoFile", getActivity().MODE_PRIVATE);
        String jsonFnames = sharedPreferences.getString("folderNames", "null");
        Gson gson = new Gson();
        String[] fNames = gson.fromJson(jsonFnames, String[].class);


        //:TODO im trying to get the folder names to show up in the autocomplete text view. the adapter wont work. dont know if the listener will work either?
        ArrayAdapter<String> adapter3 = HomePageActivity.adapterNames;

        actv.setThreshold(0);
        actv.setAdapter(adapter3);


        Button submit = (Button) view.findViewById(R.id.button_dialolg_sharing_submit);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("EmailTab", "onClick: " + actv.getText().toString());
                EditText email = view.findViewById(R.id.editText_emailtab_sharing_email);
                AddUserIdToFolder(email.getText().toString(), actv.getText().toString());
                email.setText("");
                actv.setText("");
            }
        });


        return view;
    }

    public void AddUserIdToFolder(String email, String folderName) {
        Log.e("Folder", "Folder added" + folderName + " Email: " + email);
        currFolderName = folderName;
        Repository.getInstance().setUserIdListener(this);
        Log.e("Folder", "Folder added" + folderName + " Email: " + email);
        Repository.getInstance().readUserId(email);
    }

    @Override
    public void onUidChanged(String newuid) {
        Repository.getInstance().addUserIdToFolder(newuid, currFolderName, FirebaseAuth.getInstance().getUid());
    }

}