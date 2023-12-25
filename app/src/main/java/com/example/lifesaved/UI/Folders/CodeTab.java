package com.example.lifesaved.UI.Folders;


import static android.content.Context.CLIPBOARD_SERVICE;

import android.content.ClipboardManager;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;

import com.example.lifesaved.R;
import com.example.lifesaved.persistence.Repository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.gson.Gson;

import java.util.Random;


public class CodeTab extends Fragment {

    int shareCode;
    AutoCompleteTextView actv;
    TextView codeDisplay;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle  savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_code_tab, container, false);

        codeDisplay = view.findViewById(R.id.textView_codetab_display_code);

        actv = (AutoCompleteTextView) view.findViewById(R.id.autoCompleteTextView_codetab_folder_name);

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("infoFile", getActivity().MODE_PRIVATE);
        String jsonFnames = sharedPreferences.getString("folderNames", "null");
        Gson gson = new Gson();
        String[] fNames = gson.fromJson(jsonFnames, String[].class);

        ArrayAdapter<String> adapter3 = HomePageActivity.adapterNames;
        actv.setThreshold(0);
        actv.setAdapter(adapter3);


        Button generateCode = view.findViewById(R.id.button_codetab_generate_code);
        generateCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String folderName = actv.getText().toString();

                createCode(folderName);
                //:TODO maybe need
//                actv.setText("");
            }
        });

        Button submitCode = view.findViewById(R.id.button_codetab_submit_code);
        submitCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String uid = FirebaseAuth.getInstance().getUid();
                Repository.getInstance().addUserWithCode(uid, shareCode);
            }
        });

        codeDisplay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(CLIPBOARD_SERVICE);
                clipboard.setText(shareCode + "");
            }
        });


        return view;
    }

    public void createCode(String folderName){
        int min = 111111;
        int max = 999999;
        Random generator = new Random();
        shareCode = generator.nextInt(900000) + 100000;

        codeDisplay.setText("Your code is: " + shareCode);

        String uid = FirebaseAuth.getInstance().getUid();
        Repository.getInstance().connectFolderWithShareCode(uid,folderName, shareCode);

        //:TODO send with whatsapp


    }
}